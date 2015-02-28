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

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.collections.hmap.ExtendibleHashTable;
import com.mellowtech.core.collections.mappings.BSMapping;
import com.mellowtech.core.disc.MapEntry;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * User: Martin Svensson
 * Date: 2012-10-20
 * Time: 16:55
 * To change this template use File | Settings | File Templates.
 */
public class DiscBasedHashMap <K, V> implements DiscMap<K,V>{

  private BSMapping<K> keyMapping;
  private BSMapping<V> valueMapping;
  ExtendibleHashTable eht;
  private String fName;

  public static int DEFAULT_BUCKET_SIZE = 10;
  public static int DEFAULT_KEY_VALUE_SIZE = 512;


  public DiscBasedHashMap(BSMapping <K> keyMapping, BSMapping <V> valueMapping,
                            String fileName) throws IOException{
    this(keyMapping, valueMapping, fileName, DEFAULT_BUCKET_SIZE, DEFAULT_KEY_VALUE_SIZE);
  }

  public DiscBasedHashMap(BSMapping <K> keyMapping, BSMapping <V> valueMapping,
                          String fileName, int maxKeyValueSize) throws IOException{
    this(keyMapping, valueMapping, fileName, DEFAULT_BUCKET_SIZE, maxKeyValueSize);
  }

  public DiscBasedHashMap(BSMapping <K> keyMapping, BSMapping <V> valueMapping,
                          String fileName, int bucketSize, int maxKeyValueSize) throws IOException{
    this.keyMapping = keyMapping;
    this.valueMapping = valueMapping;
    this.fName = fileName;

    try{
      eht = new ExtendibleHashTable(fName);
      return;
    }
    catch(Exception e){
      CoreLog.I().l().info("hash table did not exist");
    }
    try{
    eht = new ExtendibleHashTable(fName,keyMapping.getTemplate(), valueMapping.getTemplate(),
            bucketSize, maxKeyValueSize);
    }
    catch (Exception e){
      throw new IOException("could not instantiate disc hash");
    }

  }

  /****************overwritten disc hmap methods******************************/
  public void save() throws IOException{
    this.eht.saveHashTable();
  }
  
  public void close() throws IOException{
	  this.eht.saveHashTable();
  }

  @Override
  public void compact() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete() throws IOException {
    this.eht.deleteHashTable();
  }

  @Override
  public Iterator<Entry<K, V>> iterator() {
    return new DiscBasedHashIterator();
  }

  @Override
  public Iterator<Entry<K, V>> iterator(K key) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /********************OVERWRITTEN MAP METHODS************************************/
  @Override
  public int size() {
    return this.eht.getNumberOfElements();
  }

  @Override
  public boolean isEmpty() {
    return this.eht.isEmpty();
  }



  @Override
  public boolean containsKey(Object key){
    try{
      ByteStorable <K> bs = keyMapping.toByteStorable((K) key);
      return eht.containsKey(bs);
    }
    catch(Exception e){
      CoreLog.I().l().log(Level.SEVERE, "", e);
    }
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean containsValue(Object value) {
    Iterator <KeyValue> iter = eht.iterator();
    ByteStorable bs = valueMapping.toByteStorable((V) value);
    while(iter.hasNext()){
      KeyValue kv = iter.next();
      ByteStorable toComp = kv.getValue();
      if(toComp.compareTo(kv) == 0){
        return true;
      }
    }
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public V get(Object key) {
    try{
      ByteStorable k = keyMapping.toByteStorable((K)key);
      ByteStorable bs = eht.search(k);
      return valueMapping.fromByteStorable(bs);
    }
    catch(Exception e){
      CoreLog.L().log(Level.SEVERE, "", e);
    }
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public V put(K key, V value) {
    ByteStorable bsk = keyMapping.toByteStorable(key);
    ByteStorable bsv = valueMapping.toByteStorable(value);
    try{
      bsv = eht.insert(bsk, bsv, true);
      if(bsv == null) return null;
      return valueMapping.fromByteStorable(bsv);
    }
    catch(Exception e){
      CoreLog.L().log(Level.SEVERE, "", e);
    }
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public V remove(Object key) {
    ByteStorable bs = keyMapping.toByteStorable((K) key);
    try{
      KeyValue kv = eht.delete(bs);
      if(kv != null && kv.getValue() != null){
         return valueMapping.fromByteStorable(kv.getValue());
      }
    }
    catch(IOException e){
      CoreLog.L().log(Level.SEVERE, "", e);
    }
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void putAll(Map<? extends K,? extends V> m) {
    for (Entry<? extends K, ? extends V> e : m.entrySet()) {
      this.put(e.getKey(), e.getValue());
    }
  }

  @Override
  public void clear() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Set <K> keySet() {
    HashSet <K> toRet = new HashSet<K> ();
    Iterator<KeyValue> iter = eht.iterator();
    while(iter.hasNext()){
      toRet.add(keyMapping.fromByteStorable(iter.next().getKey()));
    }
    return toRet;
  }

  @Override
  public Collection <V> values() {
    ArrayList <V> toRet = new ArrayList<V> ();
    Iterator<KeyValue> iter = eht.iterator();
    while(iter.hasNext()){
      toRet.add(valueMapping.fromByteStorable(iter.next().getValue()));
    }
    return toRet;
  }

  @Override
  public  Set<Map.Entry<K,V>> entrySet() {
    HashSet <Map.Entry<K,V>> toRet = new HashSet<Entry<K, V>>();
    Iterator<KeyValue> iter = eht.iterator();
    while(iter.hasNext()){
      KeyValue kv = iter.next();
      K key = keyMapping.fromByteStorable(kv.getKey());
      V value = valueMapping.fromByteStorable(kv.getValue());
      Map.Entry <K, V> entry = new MapEntry<K, V>(key, value);
      toRet.add(entry);
    }
    return toRet;
  }

  class DiscBasedHashIterator implements Iterator<Entry<K,V>>{

    Iterator <KeyValue> iter;

    public DiscBasedHashIterator(){
      iter = eht.iterator();
    }

    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public Entry<K, V> next() {
      KeyValue next = iter.next();
      if(next == null) return null;
      MapEntry <K,V> toRet = new MapEntry<K, V> ();
      toRet.setKey(keyMapping.fromByteStorable(next.getKey()));
      if(next.getValue() != null)
        toRet.setValue(valueMapping.fromByteStorable(next.getValue()));
      return toRet;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
