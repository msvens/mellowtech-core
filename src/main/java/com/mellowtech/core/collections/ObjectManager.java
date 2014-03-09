/*
 * Copyright (c) 2013 mellowtech.org.
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 *
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 *
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 */
package com.mellowtech.core.collections;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.logging.Level;

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.bytestorable.ext.CBArrayList;
import com.mellowtech.core.bytestorable.CBString;
import com.mellowtech.core.collections.tree.BPlusTree;
import com.mellowtech.core.collections.tree.TreePosition;
import com.mellowtech.core.disc.MapEntry;
import com.mellowtech.core.disc.SpanningBlockFile;



/**
 * A disc-based hmap. Keys are stored in a BPlusTree and the values in a variable
 * length record file.
 * <p>
 * The keys are expected to be of type<code>CBString</code>, the values of
 * type <code>ByteStorable</code>. In order to re-create objects from bytes
 * this class also stores the fully qualified class name together with each
 * object(value). Since objects are created from the fully qualified name of the
 * class, the class must have an empty constructor.
 * 
 * @author rickard.coster@asimus.se
 */
public class ObjectManager {
  private SpanningBlockFile mObjectFile;
  private String mFileName;
  protected BPlusTree mIndex;
  private CBArrayList mClassIdList = new CBArrayList(new CBString());
  private String mClassIdListName = new CBArrayList(new CBString()).getClass()
      .getName();

  /**
   * Creates a new <code>ObjectManager</code> instance.
   * 
   * @param fileName
   *          name of the physical file
   * @exception Exception
   *              if an error occurs
   */
  public ObjectManager(String fileName) throws Exception {
    mFileName = fileName;
    open();
  }

  /**
   * Return the list of unique class identifiers for each object in list.
   * 
   * @return a list of class identifiers
   */
  public CBArrayList getClassIdList() {
    return mClassIdList;
  }

  /**
   * Open a list of objects stored on disc. If the list was not found a new list
   * is created.
   * 
   * @exception Exception
   *              if an error occurs
   */
  protected void open() throws Exception {
    boolean newSPF = false;
    boolean newIDX = false;
    try {
      mObjectFile = new SpanningBlockFile(mFileName);
      // Read class id list object
      openObjectClassIdList();
    }
    catch (IOException e) {
      newSPF = true;
      mObjectFile = new SpanningBlockFile(512, mFileName);
      // Create class id list object (will get zero as pointer)
      int pointer = createObject(mClassIdList);
    }
    try {
      mIndex = new BPlusTree(mFileName);
    }
    catch (IOException e) {
      newIDX = true;
      mIndex = new BPlusTree(mFileName, new CBString(), new PointerClassId(),
          2048, 1024);
    }
    if (!newSPF && !newIDX) {
      CoreLog.L().log(Level.FINE, "opened "+ mFileName);
    }
    else if (newSPF && newIDX) {
      CoreLog.L().log(Level.FINE, "created " + mFileName);
    }
    else {
      StringBuilder sb = new StringBuilder();
      // some files are missing. notify this.
      sb.append("Some files were missing when opening: ");
      sb.append(mFileName);
      sb.append(" SPF opened = " + !newSPF);
      sb.append(" IDX opened = " + !newIDX);
      sb.append(" One could be opened, the other could not");
      sb.append(" (and was therefore created)");
      CoreLog.L().warning(sb.toString());
    }
  }

  /**
   * Save a list of objects to disc and then reopen it.
   * 
   * @exception Exception
   *              if an error occurs
   */
  public void save() throws Exception {
    saveObjectClassIdList();
    if (mObjectFile != null)
      mObjectFile.closeFile();
    if (mIndex != null)
      mIndex.saveTree();
    open();
  }

  /**
   * Retrive an objects position in the list of object, i.e the bplus tree.
   * 
   * @param object
   *          the object to search for
   * @return a the objects position in the tree
   * @exception Exception
   *              if an error occurs
   */
  public TreePosition getPosition(ByteStorable object) throws Exception {
    return mIndex.getPosition(object);
  }

  public CBString getKeyAtPostion(int pos) throws Exception {
    return (CBString) mIndex.getKeyAtPosition(pos);
  }

  public int getNumberOfKeys() throws Exception {
    return mIndex.getNumberOfElements();
  }

  /**
   * Get the class id associated with an object.
   * 
   * @param object
   *          the object to search for
   * @return the unique class identifer
   */
  protected int getObjectClassId(ByteStorable object) {
    int id = 0;
    CBString className = new CBString(object.getClass().getName());
    for (ByteStorable byteStorable : mClassIdList.getArrayList()) {
      CBString cn = (CBString) byteStorable;
      if (cn.compareTo(className) == 0)
        return id;
      id++;
    }
    return -1;
  }

  /**
   * Save the list of class names and unique ids.
   * 
   * @exception Exception
   *              if an error occurs
   */
  protected void saveObjectClassIdList() throws Exception {
    writeObject(mClassIdList, 0);
  }

  /**
   * Open the list of class names and unique ids.
   * 
   * @exception Exception
   *              if an error occurs
   */
  protected void openObjectClassIdList() throws Exception {
    mClassIdList = new CBArrayList(new CBString());
    mClassIdList = (CBArrayList) getObject(0, mClassIdListName);
  }

  /**
   * Store the class name for an object and return its id.
   * 
   * @param object
   *          the objects class to store
   * @return the unique identifer
   * @exception Exception
   *              if an error occurs
   */
  protected int putObjectClass(ByteStorable object) throws Exception {
    mClassIdList.getArrayList().add(new CBString(object.getClass().getName()));
    saveObjectClassIdList();
    return mClassIdList.getArrayList().size() - 1;
  }

  /**
   * Get the class name corresponding to a given identifer.
   * 
   * @param id
   *          class identifer
   * @return class name
   */
  protected String getObjectClass(int id) {
    return ((CBString) mClassIdList.getArrayList().get(id)).toString();
  }

  /**
   * Create and store a key/object pair in the index. The object's class name
   * (if needed) is stored in the list of class names/ids and then the id is
   * attatched to the object and stored in the tree.
   * 
   * @param key
   *          the key
   * @param object
   *          the object to store
   * @return false if the key was already present, true otherwise.
   * @exception Exception
   *              if an error occurs
   */
  public boolean create(CBString key, ByteStorable object) throws Exception {
    if (mIndex.search(key) != null)
      return false;
    int pointer = createObject(object);
    int classId = getObjectClassId(object);
    if (classId < 0)
      classId = putObjectClass(object);
    PointerClassId cp = new PointerClassId(pointer, classId);
    mIndex.insert(key, cp, false);
    return true;
  }

  /**
   * Delete a key/value from this tree.
   * 
   * @param key
   *          the key to delete.
   * @return the object.
   * @exception Exception
   *              if an error occurs
   */
  public ByteStorable delete(CBString key) throws Exception {
    PointerClassId cp = (PointerClassId) mIndex.delete(key);
    ByteStorable bs = null;
    if (cp != null) {
      bs = getObject(cp.getPointer(), getObjectClass(cp.getClassId()));
      deleteObject(cp.getPointer());
    }
    return bs;
  }

  /**
   * Get an object from the tree.
   * 
   * @param key
   *          the key to search for
   * @return the value
   * @exception Exception
   *              if an error occurs
   */
  public ByteStorable get(CBString key) throws Exception {
    PointerClassId cp = (PointerClassId) mIndex.search(key);
    if (cp != null)
      return getObject(cp.getPointer(), getObjectClass(cp.getClassId()));
    return null;
  }

  /**
   * (Re)save an object in this tree.
   * 
   * @param key
   *          the key for the object
   * @param object
   *          the object to be (re)saved
   * @return false if the key was not in the tree, true otherwise
   * @exception Exception
   *              if an error occurs
   */
  public boolean save(CBString key, ByteStorable object) throws Exception {
    PointerClassId cp = (PointerClassId) mIndex.search(key);
    if (cp != null) {
      int classId = getObjectClassId(object);
      if (classId < 0)
        classId = putObjectClass(object);
      cp.setClassId(classId);
      mIndex.insert(key, cp, true);
      writeObject(object, cp.getPointer());
      return true;
    }
    return false;
  }

  /**
   * Check if this tree contains a given key.
   * 
   * @param key
   *          the key to search for
   * @return true if the key was found, false otherwise
   */
  public boolean contains(CBString key) {
    try {
      return mIndex.search(key) != null ? true : false;
    }
    catch (Exception e) {
      return false;
    }
  }

  /**
   * Get an iterator of <code>MapEntry</code> objects, with a
   * <code>CBString</code>) as key and a <code>PointerClassId</code> object
   * as value.
   * <p>
   * 
   * @return an <code>Iterator</code> of <code>MapEntry</code> objects.
   * 
   * @see com.mellowtech.core.collections.PointerClassId
   */
  public Iterator<MapEntry> iterator() {
    return new ObjectIndexIterator(this);
  }

  /**
   * Get an iterator of <code>MapEntry</code> objects, with a
   * <code>CBString</code> as key and the <code>ByteStorable</code> object
   * as value.
   * <p>
   * 
   * @return an <code>Iterator</code> of <code>MapEntry</code> objects.
   */
  public Iterator<MapEntry> keyValueIterator() {
    return new ObjectIterator(this);
  }
  
  /**
   * Get an iterator of <code>MapEntry</code> objects, with a
   * <code>CBString</code>  as key and the <code>ByteStorable</code> object
   * as value, starting at 'startingKey"
   * <p>
   * 
   * @param startingKey the starting key
   * @return an <code>Iterator</code> of <code>MapEntry</code> objects.
   */
  public Iterator<MapEntry> keyValueIterator(CBString startingKey) {
	  return new ObjectIterator(this, startingKey);
  }
    

  /**
   * Print the bplus tree index structure.
   * 
   * @return a string representation of the blus tree
   */
  public String printTree() {
    return (mIndex != null) ? mIndex.toString() : "tree is empty";
  }

  public String toString() {
    try {
      StringBuffer sb = new StringBuffer();
      if (mIndex == null) {
        sb.append("No index open");
        return sb.toString();
      }
      sb.append("\n******* ObjectManager contents **************\n");
      KeyValue kv;
      for (Iterator it = mIndex.iterator(); it.hasNext();) {
        kv = (KeyValue) it.next();
        sb.append("key   :" + kv.getKey() + "\n");
        sb.append("value :" + kv.getValue());
        sb.append("\n\n");
      }
      return sb.toString();
    }
    catch (Exception e) {
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    }
  }

  /**
   * Write the index tree's block count information to two text files
   * (.btree.txt and .count.txt), for debugging and inspection purposes.
   * 
   * @param fileNamePrefix
   *          the file name
   */
  public void writeBlockCounts(String fileNamePrefix) {
    try {
      PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
          fileNamePrefix + ".btree.txt")));
      mIndex.printValueFileCount(pw);
      pw.close();
    }
    catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
    try {
      PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
          fileNamePrefix + ".count.txt")));
      mIndex.printBlockCountFile(pw);
      pw.close();
    }
    catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
  }

  class ObjectIndexIterator implements Iterator<MapEntry> {
    Iterator iterator;
    MapEntry pe;

    public ObjectIndexIterator(ObjectManager manager) {
      iterator = manager.mIndex.iterator();
      pe = new MapEntry();
    }

    public boolean hasNext() {
      return iterator.hasNext();
    }

    public MapEntry next() {
      KeyValue entry = (KeyValue) iterator.next();
      pe.setKey(entry.getKey());
      pe.setValue(entry.getValue());
      return pe;
    }

    public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }
  }

  class ObjectIterator implements Iterator<MapEntry> {
    Iterator iterator;
    MapEntry pe;
    ObjectManager manager;

    public ObjectIterator(ObjectManager manager) {
      this.manager = manager;
      iterator = manager.mIndex.iterator();
      pe = new MapEntry();
    }

    public ObjectIterator(ObjectManager manager, ByteStorable startingKey) {
        this.manager = manager;
        iterator = manager.mIndex.iterator(startingKey);
        pe = new MapEntry();
      }

    public boolean hasNext() {
      return iterator.hasNext();
    }

    public MapEntry next() {
      KeyValue entry = (KeyValue) iterator.next();
      pe.setKey(entry.getKey());
      PointerClassId pi = (PointerClassId) entry.getValue();
      try {
        pe.setValue(manager.getObject(pi.getPointer(), manager
            .getObjectClass(pi.getClassId())));
      }
      catch (Exception e) {
       CoreLog.L().log(Level.WARNING, "", e);
      }
      return pe;
    }

    public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Stores an object in a SpanningBlockFile and resturns a pointer to its
   * location.
   * <p>
   * This method is used only once for an object to get a new pointer. For
   * updates of the object use writeObject
   * 
   * @param bsObject
   *          the object to store
   * @return the location in the file
   * @exception Exception
   *              if an error occurs
   * @see com.mellowtech.core.disc.SpanningBlockFile
   * @see ObjectManager#writeObject(ByteStorable, int)
   */
  public int createObject(ByteStorable bsObject) throws Exception {
    byte b[] = new byte[bsObject.byteSize()];
    bsObject.toBytes(b, 0);
    return mObjectFile.insert(b);
  }

  /**
   * Write an object at a specific location in a SpanningBlockFile.
   * 
   * @param bsObject
   *          the object to write
   * @param pointer
   *          pointer to locatin in the file
   * @exception Exception
   *              if an error occurs
   */
  public void writeObject(ByteStorable bsObject, int pointer) throws Exception {
    if (pointer < 0)
      throw new Exception("writeObject::Pointer not set");
    byte b[] = new byte[bsObject.byteSize()];
    bsObject.toBytes(b, 0);
    mObjectFile.update(pointer, b, 0);
  }

  /**
   * Delete an object from the SpanningBlockFile of objects.
   * 
   * @param pointer
   *          the location of the object
   * @exception Exception
   *              if an error occurs
   */
  public void deleteObject(int pointer) throws Exception {
    mObjectFile.delete(pointer);
  }

  /**
   * Given a class name and a location in the file of object, read up the bytes
   * for that object and create an object of the given class.
   * 
   * @param pointer
   *          location in the file
   * @param className
   *          the class of the object to create
   * @return the object
   * @exception Exception
   *              if an error occurs
   */
  public ByteStorable getObject(int pointer, String className) throws Exception {
    if (pointer < 0)
      throw new Exception("getObject::Pointer not set");
    byte[] b = mObjectFile.get(pointer);
    ByteStorable bs = (ByteStorable) Class.forName(className).newInstance();
    bs = bs.fromBytes(b, 0);
    return bs;
  }
}
