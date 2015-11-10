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

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.bytestorable.ByteStorableException;
import org.mellowtech.core.collections.hmap.EHTableBuilder;
import org.mellowtech.core.util.MapEntry;

/**
 * User: Martin Svensson
 * Date: 2012-10-20
 * Time: 16:55
 * To change this template use File | Settings | File Templates.
 */
public class DiscBasedHashMap <A,B extends BComparable<A,B>, 
  C, D extends BStorable<C,D>> implements DiscMap<A,C>{

  private final B keyMapping;
  private final D valueMapping;
  private BMap <A,B,C,D> eht;
  //private String fName;

  public static final int DEFAULT_BUCKET_SIZE = 1024*8;
  public static final int MAX_BUCKETS = 1024*1024*2;


  public DiscBasedHashMap(Class <B> keyType, Class <D> valueType,
                            String fileName, boolean blobValues, boolean inMemory) throws Exception{
    this(keyType, valueType, fileName, blobValues, inMemory, DEFAULT_BUCKET_SIZE, MAX_BUCKETS);
  }

  public DiscBasedHashMap(Class <B> keyType, Class <D> valueType,
                          String fileName, boolean blobValues, boolean inMemory, int bucketSize,
                          int maxBuckets) throws Exception{
    
    this(keyType, valueType, fileName, new EHTableBuilder().inMemory(inMemory).blobValues(blobValues).bucketSize(bucketSize).maxBuckets(maxBuckets));
  }
  
  public DiscBasedHashMap(Class <B> keyType, Class <D> valueType, String fileName,
      EHTableBuilder builder) throws Exception{
    this.keyMapping = keyType.newInstance();
    this.valueMapping = valueType.newInstance();
    //this.fName = fileName;
    this.eht = builder.build(keyType, valueType, fileName);
  }

  /****************overwritten disc hmap methods******************************/
  public void save() throws IOException{
    eht.save();
  }
  
  public void close() throws IOException{
	  eht.close();
  }

  @Override
  public void compact() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete() throws IOException {
    eht.delete();
  }

  @Override
  public Iterator<Entry<A, C>> iterator() {
    return new DiscBasedHashIterator();
  }


  /********************OVERWRITTEN MAP METHODS************************************/
  @Override
  public int size() {
    try{
      return eht.size();
    }
    catch(Exception e){
      CoreLog.L().log(Level.SEVERE, "", e);
      throw new ByteStorableException(e);
    }
  }

  @Override
  public boolean isEmpty() {
    try {
      return eht.isEmpty();
    }
    catch(Exception e){
      CoreLog.L().log(Level.SEVERE, "", e);
      throw new ByteStorableException(e);
    }
  }



  @Override
  @SuppressWarnings("unchecked")
  public boolean containsKey(Object key){
    try{
      return eht.containsKey(keyMapping.create((A) key));
    }
    catch(Exception e){
      CoreLog.I().l().log(Level.SEVERE, "", e);
    }
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean containsValue(Object value) {
    Iterator <KeyValue <B,D>> iter = eht.iterator();
    D find = valueMapping.create((C)value);
    while(iter.hasNext()){
      KeyValue <B,D> kv = iter.next();
      D toComp = kv.getValue();
      if(toComp.equals(find))
        return true;
    }
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public C get(Object key) {
    try{
      D ret = eht.get(keyMapping.create((A)key));
      return ret != null ? ret.get() : null;
    }
    catch(Exception e){
      CoreLog.L().log(Level.SEVERE, "", e);
    }
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public C put(A key, C value) {
    B bsk = keyMapping.create(key);
    D vsk = valueMapping.create(value);
    try{
      eht.put(bsk, vsk);
    }
    catch(Exception e){
      CoreLog.L().log(Level.SEVERE, "", e);
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public C remove(Object key) {
    B bs = keyMapping.create((A) key);
    try{
      D v = eht.remove(bs);
      return v != null ? v.get() : null;
    }
    catch(IOException e){
      CoreLog.L().log(Level.SEVERE, "", e);
    }
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void putAll(Map<? extends A,? extends C> m) {
    for (Entry<? extends A, ? extends C> e : m.entrySet()) {
      this.put(e.getKey(), e.getValue());
    }
  }

  @Override
  public void clear() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Set <A> keySet() {
    HashSet <A> toRet = new HashSet<> ();
    Iterator<KeyValue <B,D>> iter = eht.iterator();
    while(iter.hasNext()){
      toRet.add(iter.next().getKey().get());
    }
    return toRet;
  }

  @Override
  public Collection <C> values() {
    ArrayList <C> toRet = new ArrayList <> ();
    Iterator<KeyValue<B,D>> iter = eht.iterator();
    while(iter.hasNext()){
      KeyValue <B,D> next = iter.next();
      if(next.getValue() != null)
        toRet.add(next.getValue().get());
    }
    return toRet;
  }

  @Override
  public  Set<Map.Entry<A,C>> entrySet() {
    HashSet <Map.Entry<A,C>> toRet = new HashSet<>();
    Iterator<KeyValue <B,D>> iter = eht.iterator();
    while(iter.hasNext()){
      KeyValue <B,D> kv = iter.next();
      A key = kv.getKey().get();
      C value = kv.getValue() != null ? kv.getValue().get() : null;
      Map.Entry <A, C> entry = new MapEntry<>(key, value);
      toRet.add(entry);
    }
    return toRet;
  }

  class DiscBasedHashIterator implements Iterator<Entry<A,C>>{

    Iterator <KeyValue <B,D>> iter;

    public DiscBasedHashIterator(){
      iter = eht.iterator();
    }

    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public Entry<A, C> next() {
      KeyValue <B,D> next = iter.next();
      if(next == null) return null;
      MapEntry <A,C> toRet = new MapEntry <> ();
      toRet.setKey(next.getKey().get());
      if(next.getValue() != null)
        toRet.setValue(next.getValue().get());
      return toRet;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
