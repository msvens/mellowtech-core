/*
 * Copyright 2015 mellowtech.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mellowtech.core.collections.impl;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.bytestorable.ByteStorableException;
import org.mellowtech.core.collections.BMap;
import org.mellowtech.core.collections.DiscMap;
import org.mellowtech.core.collections.EHTableBuilder;
import org.mellowtech.core.collections.KeyValue;
import org.mellowtech.core.util.MapEntry;

/**
 * User: Martin Svensson
 * Date: 2012-10-20
 * Time: 16:55
 * To change this template use File | Settings | File Templates.
 */
public class DiscBasedHashMap <A,B extends BComparable<A,B>, 
  C, D extends BStorable<C,D>> implements DiscMap<A,C> {

  private final B keyMapping;
  private final D valueMapping;
  private BMap<A,B,C,D> eht;
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
    Iterator <KeyValue<B,D>> iter = eht.iterator();
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
    try {
      eht.truncate();
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  @Override
  public Set <A> keySet() {
    return new DBKeySet<A,C> (this);
  }

  @Override
  public Collection <C> values() {
    return new DBValueCollection<>(this);
  }

  @Override
  public  Set<Map.Entry<A,C>> entrySet() {
    return new DBEntrySet<>(this);
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
