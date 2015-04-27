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
package org.mellowtech.core.collections;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.ByteStorableException;
import org.mellowtech.core.bytestorable.CBList;
import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.bytestorable.io.StorableFile;
import org.mellowtech.core.collections.tree.BTree;
import org.mellowtech.core.collections.tree.BTreeBuilder;
import org.mellowtech.core.util.MapEntry;




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
 * @author msvens@gmail.com
 */
@SuppressWarnings("rawtypes")
public class ObjectManager implements DiscMap <String, BStorable>{
  
 
  

  
  private BTree <String, CBString, ?, PointerClassId> mIndex;
  
  private CBList <String> mClassIds;
  
  private final boolean inMemory;
  
  private final String mFileName;
  
  //private final boolean mBlobs;
  
  private final HashMap <Short, BStorable> templates;


  /**
   * Creates a new <code>ObjectManager</code> instance.
   * 
   * @param fileName name of the physical file
   * @param inMemory try to memory map the objects
   * @param blobs storing large objects
   * @exception Exception
   *              if an error occurs
   */
  public ObjectManager(String fileName, boolean inMemory, boolean blobs) throws Exception {
    this.mFileName = fileName;
    this.inMemory = inMemory;
    //this.mBlobs = blobs;
    this.templates = new HashMap <> ();
    open();
  }


  @Override
  public int size() {
    try {
      return mIndex.size();
    } catch (IOException e) {
      throw new ByteStorableException(e);
    }
  }



  @Override
  public boolean isEmpty() {
    try {
      return mIndex.isEmpty();
    } catch (IOException e) {
      throw new ByteStorableException(e);
    }
  }



  @Override
  public boolean containsKey(Object key) {
    try {
      return mIndex.containsKey((CBString)key);
    } catch (IOException e) {
      throw new ByteStorableException(e);
    }
  }



  @Override
  public boolean containsValue(Object value) {
    return false;
  }



  @Override
  public BStorable get(Object key) {
    PointerClassId id;
    try {
      id = mIndex.get(new CBString((String)key));
      return id != null ? getObject(id) : null;
    } catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return null;
  }



  @Override
  public BStorable put(String key, BStorable value) {
    BStorable prev = get(key);
    try{
      short id = this.getTemplateId(value);
      byte[] b = value.to().array();
      PointerClassId pci = new PointerClassId(id, b);
      mIndex.put(new CBString(key), pci);
    }
    catch(Exception e){
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return prev;
    
  }



  @Override
  public BStorable remove(Object key) {
    try{
      PointerClassId pci = mIndex.remove((CBString) key);
      if(pci == null)
        return null;
      return getObject(pci);
    }
    catch(Exception e){
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return null;
  }



  @Override
  public void putAll(Map<? extends String, ? extends BStorable> m) {
    for(Map.Entry<? extends String, ? extends BStorable> entry : m.entrySet()){
      put(entry.getKey(), entry.getValue());
    }
    
  }



  @Override
  public void clear() { 
    
  }



  @Override
  public Set<String> keySet() {
    TreeSet <String> ts = new TreeSet <> ();
    for(Iterator <KeyValue <CBString, PointerClassId>> iter = mIndex.iterator(); iter.hasNext();){
      ts.add(iter.next().getKey().get());
    }
    return ts;
  }



  @Override
  public Collection<BStorable> values() {
    ArrayList <BStorable> al = new ArrayList <> ();
    KeyValue <CBString, PointerClassId> kv;
    for(Iterator <KeyValue <CBString, PointerClassId>>iter = mIndex.iterator(); iter.hasNext();){
      kv = iter.next();
      al.add(getObject(kv.getValue()));
    }
    return al; 
  }



  @Override
  public Set<java.util.Map.Entry<String, BStorable>> entrySet() {
    Set <Map.Entry<String, BStorable>> toRet = new TreeSet <> ();
    for(Iterator <KeyValue <CBString, PointerClassId>> iter = mIndex.iterator(); iter.hasNext();){
      KeyValue <CBString, PointerClassId> keyValue = iter.next();
      toRet.add(new MapEntry <String, BStorable>(keyValue.getKey().get(), getObject(keyValue.getValue())));
    }
    return toRet;
  }



  @Override
  public void save() throws IOException {
    this.mIndex.save();
    try {
      this.saveClassIds();
    } catch (Exception e) {
      throw new IOException(e);
    }
    
  }
  
  public void close() throws IOException {
	    this.mIndex.close();
	    try {
	      this.saveClassIds();
	    } catch (Exception e) {
	      throw new IOException(e);
	    }
	    
	  }



  @Override
  public void compact() throws IOException, UnsupportedOperationException {
    throw new UnsupportedOperationException();
    
  }



  @Override
  public void delete() throws IOException {
    this.mIndex.delete();
    File f = new File(mFileName+".classIds");
    f.delete();
    this.mClassIds.clear();
    this.templates.clear();
  }



  @Override
  public Iterator<java.util.Map.Entry<String, BStorable>> iterator() {
    return new ObjectManagerIterator();
  }



  @Override
  public Iterator<java.util.Map.Entry<String, BStorable>> iterator(String key)
      throws UnsupportedOperationException {
    return new ObjectManagerIterator(key);
  }
  
  class ObjectManagerIterator implements Iterator<Entry<String, BStorable>>{

    Iterator <KeyValue<CBString, PointerClassId>> iter;

    public ObjectManagerIterator(){
      iter = mIndex.iterator();
    }

    public ObjectManagerIterator(String key){
      iter = mIndex.iterator(new CBString(key));
    }


    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public Entry<String, BStorable> next() {
      KeyValue <CBString, PointerClassId> next = iter.next();
      if(next == null) return null;
      MapEntry <String,BStorable> entry = new MapEntry<>();
      entry.setKey(next.getKey().get());
      if(next.getValue() != null)
        entry.setValue(getObject(next.getValue()));
      return entry;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
  
  //open and save:
  private BStorable getObject(PointerClassId id){
    try{
      return getTemplate(id.getClassId()).from(id.get().bytes, 0);
    }
    catch(Exception e){
      throw new ByteStorableException(e);
    }
  }
  
  private short getTemplateId(BStorable obj){
    String name = obj.getClass().getName();
    short id = 0;
    for(String cls : mClassIds){
      if(cls.equals(name)) return id;
      id++;
    }
    //add and save
    mClassIds.add(name);
    try {
      this.saveClassIds();
    } catch (Exception e) {
      throw new ByteStorableException(e);
    }
    return id;
  }
  
  private BStorable getTemplate(short id) throws Exception{
    BStorable toRet = templates.get(id);
    if(toRet == null){
      toRet = (BStorable) Class.forName(this.mClassIds.get(id)).newInstance();
      templates.put(id, toRet);
    }
    return toRet;
  }
  
  
  private void open() throws Exception {
    //this.mIndex = BTreeFactory.openMemMappedBlob(mFileName, new CBString(), new PointerClassId(), inMemory);
    BTreeBuilder b = new BTreeBuilder().blobValues(true).valuesInMemory(inMemory);
    mIndex = b.build(CBString.class, PointerClassId.class, mFileName);
    openClassIds();
  }

  /**
   * Save the list of class names and unique ids.
   * 
   * @exception Exception
   *              if an error occurs
   */
  private void saveClassIds() throws Exception {
    StorableFile.writeFileAsByteStorable(mFileName+".classIds", mClassIds);
  }

  /**
   * Open the list of class names and unique ids.
   * 
   * @exception Exception
   *              if an error occurs
   */
  private void openClassIds() throws Exception {
    this.mClassIds = StorableFile.readFileAsByteStorable(mFileName+".classIds", new CBList <String> ());
    if(this.mClassIds == null){
      this.mClassIds = new CBList <> ();
    }
  }

 
}
