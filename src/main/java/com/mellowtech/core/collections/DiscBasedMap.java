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
import com.mellowtech.core.collections.tree.BPlusTree;
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
  
  
  
  private BTree btree;
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

    if(!blobValues){
      if(memMapped)
        this.btree = BTreeFactory.openMemMapped(fileName,
                (ByteComparable) keyMapping.getTemplate(), valueMapping.getTemplate(), keyBlockSize,
                valueBlockSize);
      else
        this.btree = BTreeFactory.openOptimized(fileName,
            (ByteComparable) keyMapping.getTemplate(), valueMapping.getTemplate(), keyBlockSize,
            valueBlockSize);
    }
    else{
      if(memMapped)
        this.btree = BTreeFactory.openMemMappedBlob(fileName,
                (ByteComparable) keyMapping.getTemplate(), valueMapping.getTemplate(), keyBlockSize,
                valueBlockSize);
      else
      this.btree = BTreeFactory.openMemMappedBlob(fileName,
              (ByteComparable) keyMapping.getTemplate(), valueMapping.getTemplate(), keyBlockSize,
              valueBlockSize);
    }
    //first try to open the hmap as it was already created:
    /*try{
      this.btree = new BPlusTree(fileName);
      this.btree.useCache(true, false);
    }
    catch(Exception e){
      
        this.btree = new BPlusTree(fileName, this.keyMapping.getTemplate(), this.valueMapping.getTemplate(),
            valueBlockSize, keyBlockSize);
      this.btree.useCache(true, false);
    }*/
    
    
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
    return k == null ? null : new MapEntry<K, V>(k, this.get(k));
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
        return this.keyMapping.fromByteComparable((ByteComparable)this.btree.getKey(pos));
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
    return k == null ? null : new MapEntry <K, V> (k, this.get(k));
  }

  @Override
  public java.util.Map.Entry<K, V> floorEntry(K key) {
    K k = this.floorKey(key);
    return k == null ? null : new MapEntry <K, V> (k, this.get(k));
  }

  @Override
  public K floorKey(K key) {
    try {
      TreePosition tp = this.btree.getPositionWithMissing(this.keyMapping.toByteComparable(key));
      if(tp.isExists())
        return this.keyMapping.fromByteComparable((ByteComparable)this.btree.getKey(tp.getSmaller()));
      else if(tp.getSmaller() > 0)
        return this.keyMapping.fromByteComparable((ByteComparable)this.btree.getKey(tp.getSmaller() - 1));
      return null;
      
    }
    catch(IOException e){
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    }
  }

  @Override
  public SortedMap<K, V> headMap(K toKey) {
    return null;
  }

  @Override
  public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public java.util.Map.Entry<K, V> higherEntry(K key) {
    K k = this.higherKey(key);
    return k == null ? null : new MapEntry <K, V> (k, this.get(k));
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
        return this.keyMapping.fromByteComparable((ByteComparable)this.btree.getKey(tp.getSmaller()+1));
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
    return k == null ? null : new MapEntry <K, V> (k, this.get(k));
  }

  @Override
  public java.util.Map.Entry<K, V> lowerEntry(K key) {
    K k = this.lowerKey(key);
    return k == null ? null : new MapEntry <K, V> (k, this.get(k));
  }

  @Override
  public K lowerKey(K key) {
    try {
      TreePosition tp = this.btree.getPositionWithMissing(this.keyMapping.toByteComparable(key));
      if(tp.getSmaller() > 0)
        return this.keyMapping.fromByteComparable((ByteComparable)this.btree.getKey(tp.getSmaller() - 1));
      return null;
      
    }
    catch(IOException e){
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    }
  }

  @Override
  public NavigableSet<K> navigableKeySet() {
    return (TreeSet) this.keySet();
  }

  @Override
  public java.util.Map.Entry<K, V> pollFirstEntry() {
    K key = this.firstKey();
    V value = this.get(key);
    MapEntry <K, V> me = new MapEntry <K, V> (key, value);
    this.remove(key);
    return me;
  }

  @Override
  public java.util.Map.Entry<K, V> pollLastEntry() {
    K key = this.lastKey();
    V value = this.get(key);
    MapEntry <K, V> me = new MapEntry <K, V> (key, value);
    this.remove(key);
    return me;
  }

  @Override
  public SortedMap<K, V> subMap(K fromKey, K toKey) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey,
      boolean toInclusive) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SortedMap<K, V> tailMap(K fromKey) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
    // TODO Auto-generated method stub
    return null;
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
    Set <Map.Entry<K, V>> toRet = new TreeSet <Map.Entry<K, V>> ();
    for(Iterator <KeyValue <ByteStorable, ByteStorable>> iter = this.btree.iterator(); iter.hasNext();){
      KeyValue <ByteStorable, ByteStorable> keyValue = iter.next();
      K key = this.keyMapping.fromByteComparable((ByteComparable) keyValue.getKey());
      V value = this.valueMapping.fromByteStorable(keyValue.getValue());
      toRet.add(new MapEntry<K, V> (key, value));
    }
    return toRet;
  }

  @Override
  public K firstKey() {
    if(this.isEmpty()) return null;
    try {
      return this.keyMapping.fromByteComparable(((ByteComparable)this.btree.getKey(0)));
    } catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return null;
  }

  @Override
  public Set<K> keySet() {
    TreeSet <K> ts = new TreeSet <K> ();
    for(Iterator <KeyValue <ByteStorable, ByteStorable>> iter = this.btree.iterator(); iter.hasNext();){
      ts.add(this.keyMapping.fromByteComparable((ByteComparable)iter.next().getKey()));
    }
    return ts;
  }

  @Override
  public K lastKey() {
    return null;
  }

  @Override
  public Collection <V> values() {
    ArrayList <V> al = new ArrayList <V> ();
    KeyValue <ByteStorable, ByteStorable> kv;
    for(Iterator <KeyValue <ByteStorable, ByteStorable>>iter = this.btree.iterator(); iter.hasNext();){
      kv = iter.next();
      al.add(this.valueMapping.fromByteStorable(kv.getValue()));
    }
    return al; 
  }

  @Override
  public void clear() {
    
  }

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
    for(Iterator <KeyValue <ByteStorable, ByteStorable>> iter = this.btree.iterator(); iter.hasNext();){
      V v = this.valueMapping.fromByteStorable(iter.next().getValue());
      if(v.equals(value))
        return true;
    }
    return false;
  }

  @Override
  public V get(Object key) {
    ByteComparable  bc = this.keyMapping.toByteComparable((K) key);
    try {
      ByteStorable bs = this.btree.get(bc);
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
    ByteComparable bc = keyMapping.toByteComparable(key);
    ByteStorable bs = valueMapping.toByteStorable(value);
    V toRet = null;
    /*if(this.containsKey(key))
      toRet = this.get(key);*/
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

  @Override
  public V remove(Object key) {
    try{
      ByteStorable prevValue = this.btree.remove(this.keyMapping.toByteComparable((K)key));
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

    Iterator <KeyValue<ByteStorable, ByteStorable>> iter;

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
      KeyValue <ByteStorable, ByteStorable> next = iter.next();
      if(next == null) return null;
      MapEntry <K,V> entry = new MapEntry<K, V>();
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
