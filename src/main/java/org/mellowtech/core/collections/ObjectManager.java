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
import org.mellowtech.core.bytestorable.ByteStorable;
import org.mellowtech.core.bytestorable.ByteStorableException;
import org.mellowtech.core.bytestorable.CBList;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.bytestorable.io.StorableFile;
import org.mellowtech.core.collections.tree.BTree;
import org.mellowtech.core.collections.tree.BTreeBuilder;
import org.mellowtech.core.disc.MapEntry;




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
public class ObjectManager implements DiscMap <String, ByteStorable <?>>{
  
 
  

  
  private BTree <CBString, PointerClassId> mIndex;
  
  private CBList <String> mClassIds;
  
  private final boolean inMemory;
  
  private final String mFileName;
  
  //private final boolean mBlobs;
  
  private final HashMap <Short, ByteStorable <?>> templates;


  /**
   * Creates a new <code>ObjectManager</code> instance.
   * 
   * @param fileName
   *          name of the physical file
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
  public ByteStorable <?> get(Object key) {
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
  public ByteStorable <?> put(String key, ByteStorable <?> value) {
    ByteStorable <?> prev = get(key);
    try{
      short id = this.getTemplateId(value);
      byte[] b = value.toBytes().array();
      PointerClassId pci = new PointerClassId(id, b);
      mIndex.put(new CBString(key), pci);
    }
    catch(Exception e){
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return prev;
    
  }



  @Override
  public ByteStorable <?> remove(Object key) {
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
  public void putAll(Map<? extends String, ? extends ByteStorable <?>> m) {
    for(Map.Entry<? extends String, ? extends ByteStorable <?>> entry : m.entrySet()){
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
  public Collection<ByteStorable <?>> values() {
    ArrayList <ByteStorable <?>> al = new ArrayList <> ();
    KeyValue <CBString, PointerClassId> kv;
    for(Iterator <KeyValue <CBString, PointerClassId>>iter = mIndex.iterator(); iter.hasNext();){
      kv = iter.next();
      al.add(getObject(kv.getValue()));
    }
    return al; 
  }



  @Override
  public Set<java.util.Map.Entry<String, ByteStorable <?>>> entrySet() {
    Set <Map.Entry<String, ByteStorable <?>>> toRet = new TreeSet <> ();
    for(Iterator <KeyValue <CBString, PointerClassId>> iter = mIndex.iterator(); iter.hasNext();){
      KeyValue <CBString, PointerClassId> keyValue = iter.next();
      toRet.add(new MapEntry <String, ByteStorable <?>>(keyValue.getKey().get(), getObject(keyValue.getValue())));
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
  public Iterator<java.util.Map.Entry<String, ByteStorable <?>>> iterator() {
    return new ObjectManagerIterator();
  }



  @Override
  public Iterator<java.util.Map.Entry<String, ByteStorable <?>>> iterator(String key)
      throws UnsupportedOperationException {
    return new ObjectManagerIterator(key);
  }
  
  class ObjectManagerIterator implements Iterator<Entry<String, ByteStorable <?>>>{

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
    public Entry<String, ByteStorable <?>> next() {
      KeyValue <CBString, PointerClassId> next = iter.next();
      if(next == null) return null;
      MapEntry <String,ByteStorable <?>> entry = new MapEntry<>();
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
  private ByteStorable <?> getObject(PointerClassId id){
    try{
      return getTemplate(id.getClassId()).fromBytes(id.get().bytes, 0);
    }
    catch(Exception e){
      throw new ByteStorableException(e);
    }
  }
  
  private short getTemplateId(ByteStorable <?> obj){
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
  
  private ByteStorable <?> getTemplate(short id) throws Exception{
    ByteStorable <?> toRet = templates.get(id);
    if(toRet == null){
      toRet = (ByteStorable <?>) Class.forName(this.mClassIds.get(id)).newInstance();
      templates.put(id, toRet);
    }
    return toRet;
  }
  
  
  private void open() throws Exception {
    //this.mIndex = BTreeFactory.openMemMappedBlob(mFileName, new CBString(), new PointerClassId(), inMemory);
    BTreeBuilder b = new BTreeBuilder().blobValues(true).valuesInMemory(inMemory);
    mIndex = b.build(new CBString(), new PointerClassId(), mFileName);
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
