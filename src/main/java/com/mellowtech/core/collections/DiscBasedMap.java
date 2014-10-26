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

/**
 * DiscBasedMap.java, com.mellowtech.core.collections
 * 
 * This is a first shot of a disc based hmap that is compliant with
 * the java.util.collections package. It is based on the Mellowtech
 * BTree implementation. The BTree implementation should later be
 * updated to offer more efficient key iterators.
 * 
 * @author Martin Svensson
 */
package com.mellowtech.core.collections;


import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteComparable;
import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.collections.mappings.BCMapping;
import com.mellowtech.core.collections.mappings.BSMapping;
import com.mellowtech.core.collections.tree.BTree;
import com.mellowtech.core.collections.tree.BTreeFactory;
import com.mellowtech.core.collections.tree.TreePosition;
import com.mellowtech.core.disc.MapEntry;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Implementation of a Disc Base Map. Based on the Mellowtech BTree. 
 * @author Martin Svensson
 *
 */
public class DiscBasedMap <K, V> implements NavigableMap<K, V>, DiscMap <K,V> {
  
  
  
  private BTree <ByteComparable <K>, ByteStorable <V>> btree;
  private BCMapping <K> keyMapping;
  private BSMapping <V> valueMapping;
  
  public static int DEFAULT_KEY_BLOCK = 2048*1;
  public static int DEFAULT_VALUE_BLOCK = 2048*2;
  
  

  public DiscBasedMap(BCMapping <K> keyMapping, BSMapping <V> valueMapping,
                      String fileName, boolean blobValues, boolean memMapped) throws IOException{
    this(keyMapping, valueMapping, fileName, DEFAULT_VALUE_BLOCK, DEFAULT_KEY_BLOCK, blobValues, memMapped);
  }

  public DiscBasedMap(BCMapping <K> keyMapping, BSMapping <V> valueMapping,
                      String fileName, int valueBlockSize, boolean blobValues, boolean memMapped) throws IOException{
    this(keyMapping, valueMapping, fileName, valueBlockSize, DEFAULT_KEY_BLOCK, blobValues, memMapped);
  }
  
  public DiscBasedMap(BCMapping <K> keyMapping, BSMapping <V> valueMapping,
      String fileName, int valueBlockSize, int keyBlockSize, boolean blobValues, boolean memMapped) throws IOException{
    this.keyMapping = keyMapping;
    this.valueMapping = valueMapping;

    this.btree = blobValues ? BTreeFactory.openMemMappedBlob(fileName,
        keyMapping.getTemplate(), valueMapping.getTemplate(), keyBlockSize,
        valueBlockSize, memMapped, -1) :
          BTreeFactory.openMemMapped(fileName,
              keyMapping.getTemplate(), valueMapping.getTemplate(), keyBlockSize,
              valueBlockSize, memMapped, -1);    
    
  }
  
  public void save() throws IOException{
    this.btree.save();
  }

  @Override
  public void compact() throws IOException, UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete() throws IOException {
    this.btree.delete();
  }

  @Override
  public Iterator<Entry<K, V>> iterator() {
    return new DiscBasedMapIterator();
  }

  @Override
  public Iterator<Entry<K, V>> iterator(K key) {
    return new DiscBasedMapIterator(key);
  }


  /*************Override NavigableMap Methods**************************/
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.save();
  }

  @Override
  public java.util.Map.Entry<K, V> ceilingEntry(K key) {
    K k = this.ceilingKey(key);
    return k == null ? null : new MapEntry<>(k, this.get(k));
  }

  @Override
  public K ceilingKey(K key) {
    TreePosition tp;
    try {
      tp = this.btree.getPositionWithMissing(this.keyMapping.toByteComparable(key));
    } catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    }
    int pos;
    if(tp.exists())
      pos = tp.getSmaller();
    else
      pos = tp.getSmaller() + 1;
    int higher = this.size() - tp.getSmaller();
    if(higher > 0){
      try {
        return this.keyMapping.fromByteComparable(this.btree.getKey(pos));
      } catch (IOException e) {
        CoreLog.L().log(Level.WARNING, "", e);
        return null;
      }
    }
    return null;
  }

  @Override
  public NavigableSet<K> descendingKeySet() {
    return null;
  }

  @Override
  public NavigableMap<K, V> descendingMap() {
    return null;
  }

  @Override
  public java.util.Map.Entry<K, V> firstEntry() {
    K k = this.firstKey();
    return k == null ? null : new MapEntry <> (k, this.get(k));
  }

  @Override
  public java.util.Map.Entry<K, V> floorEntry(K key) {
    K k = this.floorKey(key);
    return k == null ? null : new MapEntry <> (k, this.get(k));
  }

  @Override
  public K floorKey(K key) {
    try {
      TreePosition tp = this.btree.getPositionWithMissing(this.keyMapping.toByteComparable(key));
      if(tp.isExists())
        return this.keyMapping.fromByteComparable(this.btree.getKey(tp.getSmaller()));
      else if(tp.getSmaller() > 0)
        return this.keyMapping.fromByteComparable(this.btree.getKey(tp.getSmaller() - 1));
      return null;
      
    }
    catch(IOException e){
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    }
  }

  @Override
  public SortedMap<K, V> headMap(K toKey) {
    throw new Error("viewss not supported");
  }

  @Override
  public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
    throw new Error("views not supported");
  }

  @Override
  public java.util.Map.Entry<K, V> higherEntry(K key) {
    K k = this.higherKey(key);
    return k == null ? null : new MapEntry <> (k, this.get(k));
  }

  @Override
  public K higherKey(K key) {
	try {
      TreePosition tp = this.btree.getPositionWithMissing(this.keyMapping.toByteComparable(key));
      if(tp == null) return null;
      // tp.getSmaller() is the total number of smaller elements than "key", so the number of elements 
      // higher than "key" is size() - (tp.getSmaller() + 1)
      int higher = this.size() - (tp.getSmaller()+1); 
      if (higher > 0) {
        return this.keyMapping.fromByteComparable(this.btree.getKey(tp.getSmaller()+1));
      }
      return null;
    } catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    }
  }

  @Override
  public java.util.Map.Entry<K, V> lastEntry() {
    K k = this.lastKey();
    return k == null ? null : new MapEntry <> (k, this.get(k));
  }

  @Override
  public java.util.Map.Entry<K, V> lowerEntry(K key) {
    K k = this.lowerKey(key);
    return k == null ? null : new MapEntry <> (k, this.get(k));
  }

  @Override
  public K lowerKey(K key) {
    try {
      TreePosition tp = this.btree.getPositionWithMissing(this.keyMapping.toByteComparable(key));
      if(tp.getSmaller() > 0)
        return this.keyMapping.fromByteComparable(this.btree.getKey(tp.getSmaller() - 1));
      return null;
      
    }
    catch(IOException e){
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    }
  }

  @Override
  public NavigableSet<K> navigableKeySet() {
    return (TreeSet <K>) this.keySet();
  }

  @Override
  public java.util.Map.Entry<K, V> pollFirstEntry() {
    K key = this.firstKey();
    V value = this.get(key);
    MapEntry <K, V> me = new MapEntry <> (key, value);
    this.remove(key);
    return me;
  }

  @Override
  public java.util.Map.Entry<K, V> pollLastEntry() {
    K key = this.lastKey();
    V value = this.get(key);
    MapEntry <K, V> me = new MapEntry <> (key, value);
    this.remove(key);
    return me;
  }

  @Override
  public SortedMap<K, V> subMap(K fromKey, K toKey) {
    throw new Error("views not supported");
  }

  @Override
  public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey,
      boolean toInclusive) {
    throw new Error("views not supported");
  }

  @Override
  public SortedMap<K, V> tailMap(K fromKey) {
    throw new Error("views not supported");
  }

  @Override
  public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
    //Iterator <Map.Entry<K, V>> iter = this.iterator(fromKey);
    throw new Error("views not supported");
    //return null;
  }

  /**
   * Always relies on the compare function defined in the keyMapping
   */
  @Override
  public Comparator<? super K> comparator() {
    return null;
  }

  @Override
  public Set<java.util.Map.Entry<K, V>> entrySet() {
    Set <Map.Entry<K, V>> toRet = new TreeSet <> ();
    for(Iterator <KeyValue <ByteComparable <K>, ByteStorable <V>>> iter = this.btree.iterator(); iter.hasNext();){
      KeyValue <ByteComparable <K>, ByteStorable <V>> keyValue = iter.next();
      K key = this.keyMapping.fromByteComparable(keyValue.getKey());
      V value = this.valueMapping.fromByteStorable(keyValue.getValue());
      toRet.add(new MapEntry <> (key, value));
    }
    return toRet;
  }

  @Override
  public K firstKey() {
    if(this.isEmpty()) return null;
    try {
      return this.keyMapping.fromByteComparable(this.btree.getKey(0));
    } catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return null;
  }

  @Override
  public Set<K> keySet() {
    TreeSet <K> ts = new TreeSet <> ();
    for(Iterator <KeyValue <ByteComparable <K>, ByteStorable <V>>> iter = this.btree.iterator(); iter.hasNext();){
      ts.add(this.keyMapping.fromByteComparable(iter.next().getKey()));
    }
    return ts;
  }

  @Override
  public K lastKey() {
    return null;
  }

  @Override
  public Collection <V> values() {
    ArrayList <V> al = new ArrayList <> ();
    KeyValue <ByteComparable <K>, ByteStorable <V>> kv;
    for(Iterator <KeyValue <ByteComparable <K>, ByteStorable <V>>>iter = this.btree.iterator(); iter.hasNext();){
      kv = iter.next();
      al.add(this.valueMapping.fromByteStorable(kv.getValue()));
    }
    return al; 
  }

  @Override
  public void clear() {
    
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean containsKey(Object key) {
    try {
      return this.btree.containsKey(this.keyMapping.toByteComparable((K)key));
    } catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return false;
  }

  @Override
  public boolean containsValue(Object value) {
    for(Iterator <KeyValue <ByteComparable <K>, ByteStorable <V>>> iter = this.btree.iterator(); iter.hasNext();){
      V v = this.valueMapping.fromByteStorable(iter.next().getValue());
      if(v.equals(value))
        return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public V get(Object key) {
    ByteComparable <K>  bc = this.keyMapping.toByteComparable((K) key);
    try {
      ByteStorable <V> bs = this.btree.get(bc);
      if(bs != null) return this.valueMapping.fromByteStorable(bs);
    } catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return null;
  }

  @Override
  public boolean isEmpty() {
    return this.size()< 1 ? true : false;
  }

  @Override
  public V put(K key, V value) {
    ByteComparable <K> bc = keyMapping.toByteComparable(key);
    ByteStorable <V> bs = valueMapping.toByteStorable(value);
    V toRet = null;
    try {
      this.btree.put(bc, bs);
    } catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return toRet;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    for(Entry<? extends K, ? extends V> entry : m.entrySet()){
      this.put(entry.getKey(), entry.getValue());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public V remove(Object key) {
    try{
      ByteStorable <V> prevValue = this.btree.remove(this.keyMapping.toByteComparable((K)key));
      return prevValue != null ? this.valueMapping.fromByteStorable(prevValue) : null;
    }
    catch(Exception e){
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    }
  }

  @Override
  public int size() {
    try{
      return this.btree.size();
    }
    catch(Exception e){
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return 0;
  }

  class DiscBasedMapIterator implements Iterator<Entry<K, V>>{

    Iterator <KeyValue<ByteComparable <K>, ByteStorable <V>>> iter;

    public DiscBasedMapIterator(){
      iter = btree.iterator();
    }

    public DiscBasedMapIterator(K key){
      iter = btree.iterator(keyMapping.toByteComparable(key));
    }


    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public Entry<K, V> next() {
      KeyValue <ByteComparable <K>, ByteStorable <V>> next = iter.next();
      if(next == null) return null;
      MapEntry <K,V> entry = new MapEntry<>();
      entry.setKey(keyMapping.fromByteStorable(next.getKey()));
      if(next.getValue() != null)
        entry.setValue(valueMapping.fromByteStorable(next.getValue()));
      return entry;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }


}
