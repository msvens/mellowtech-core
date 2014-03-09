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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteComparable;
import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.collections.mappings.BCMapping;
import com.mellowtech.core.collections.mappings.BSMapping;
import com.mellowtech.core.collections.tree.BPlusTree;
import com.mellowtech.core.disc.MapEntry;
import com.mellowtech.core.disc.blockfile.DynamicFile;
import com.mellowtech.core.disc.blockfile.DynamicFilePointer;

/**
 * Implementation of a Disc Base Map. Based on the Mellowtech BTree. The difference
 * between this class and the ordinary DiscBasedMap is that values in this can
 * be of any size. 
 * @author Martin Svensson
 *
 */
@Deprecated
public class BlobMap<K, V> implements NavigableMap<K, V>, DiscMap<K,V> {
  
  private DynamicFile valueFile;
  private DynamicFilePointer fileIndexTemplate;

  private NavigableMap <K, DynamicFilePointer> pointerMap;

  
  //private BPlusTree btree;
  private BCMapping <K> keyMapping;
  private BSMapping <V> valueMapping;
  private String fileName;
  //public static int DEFAULT_KEY_BLOCK = 2048;
  //public static int DEFAULT_VALUE_BLOCK = 2048;
  
  
  
  
  public BlobMap(BCMapping<K> keyMapping, BSMapping<V> valueMapping,
                 String fileName, boolean discKeyMap, boolean memMapped) throws IOException{
    this.keyMapping = keyMapping;
    this.valueMapping = valueMapping;
    this.fileName = fileName;
    if(discKeyMap)
      this.pointerMap = new DiscBasedMap<K, DynamicFilePointer>(keyMapping,
              new DynamicFilePointer(), fileName, false, memMapped);
    else
      this.pointerMap = new TreeMap<K, DynamicFilePointer> ();

    this.valueFile = new DynamicFile(fileName+"-valueFiles",
            this.valueMapping.getTemplate());
  }

  @Override
  public void save() throws IOException{
    if(pointerMap instanceof DiscMap)
      ((DiscMap) pointerMap).save();
    this.valueFile.flush();
  }

  @Override
  public void compact() throws IOException, UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete() throws IOException {
    if(pointerMap instanceof DiscMap)
      ((DiscMap) pointerMap).delete();
    else
      pointerMap.clear();
    this.valueFile.delete();

  }

  @Override
  public Iterator<Entry<K, V>> iterator() {
    return new BlobMapIterator();
  }

  @Override
  public Iterator<Entry<K, V>> iterator(K key) throws UnsupportedOperationException {
    return new BlobMapIterator(key);
  }


  @Override
  protected void finalize() throws Throwable {
    this.save();
  }

  @Override
  public java.util.Map.Entry<K, V> ceilingEntry(K key) {
    K k = this.ceilingKey(key);
    return k == null ? null : new MapEntry<K, V>(k, this.get(k));
  }

  @Override
  public K ceilingKey(K key) {
    return pointerMap.ceilingKey(key);
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
    return pointerMap.floorKey(key);
  }

  @Override
  public SortedMap<K, V> headMap(K toKey) {
    return null;
  }

  @Override
  public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
    return null;
  }

  @Override
  public java.util.Map.Entry<K, V> higherEntry(K key) {
    K k = this.higherKey(key);
    TreeMap tm;
    return k == null ? null : new MapEntry <K, V> (k, this.get(k));
  }

  @Override
  public K higherKey(K key) {
    return pointerMap.higherKey(key);
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
    return pointerMap.lowerKey(key);
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
    return null;
  }

  @Override
  public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey,
      boolean toInclusive) {
    return null;
  }

  @Override
  public SortedMap<K, V> tailMap(K fromKey) {
    return null;
  }

  @Override
  public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
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
    Iterator <Entry<K, DynamicFilePointer>> iter;
    if(pointerMap instanceof DiscMap)
      iter = ((DiscMap) pointerMap).iterator();
    else
      iter = pointerMap.entrySet().iterator();
    while(iter.hasNext()){
      Entry<K, DynamicFilePointer> e = iter.next();
      V v = this.getPointerValue(e.getValue());
      MapEntry <K, V> me = new MapEntry<K,V>(e.getKey(), v);
      toRet.add(me);
    }
    return toRet;
  }

  @Override
  public K firstKey() {
    return pointerMap.firstKey();
  }

  @Override
  public Set<K> keySet() {
    return pointerMap.keySet();
  }

  @Override
  public K lastKey() {
    return pointerMap.lastKey();
  }

  @Override
  public Collection <V> values() {
    ArrayList <V> al = new ArrayList <V> ();
    Iterator<Entry<K,DynamicFilePointer>> iter;
    if(pointerMap instanceof DiscMap)
      iter = ((DiscMap) pointerMap).iterator();
    else
      iter = pointerMap.entrySet().iterator();
    while(iter.hasNext()){
      V v = this.getPointerValue(iter.next().getValue());
      if(v != null) al.add(v);
    }
    return al;
  }

  @Override
  public void clear() {
    pointerMap.clear();
    try {
      this.valueFile.delete();
    } catch (IOException e) {
      CoreLog.L().log(Level.SEVERE, "could not delete value file", e);
    }
    this.valueFile = new DynamicFile(fileName+"-valueFiles",
            this.valueMapping.getTemplate());
    
  }

  @Override
  public boolean containsKey(Object key) {
    return pointerMap.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    Iterator<Entry<K, DynamicFilePointer>> iter;

    if(pointerMap instanceof DiscMap){
      iter = ((DiscMap)pointerMap).iterator();
    }
    else{
      iter = pointerMap.entrySet().iterator();
    }
    while(iter.hasNext()){
      Entry<K, DynamicFilePointer> entry = iter.next();
      try{
        ByteStorable bs = this.valueFile.get(entry.getValue());
        V v = valueMapping.fromByteStorable(bs);
        if(v.equals(value))
          return true;
      } catch (IOException e) {
        CoreLog.L().log(Level.SEVERE, "", e);
        return false;
      }
    }
    return false;
  }

  @Override
  public V get(Object key) {
    return this.getPointerValue(pointerMap.get(key));
  }

  @Override
  public boolean isEmpty() {
    return this.size()< 1 ? true : false;
  }

  @Override
  public V put(K key, V value) {
    try{
      DynamicFilePointer dfp = pointerMap.get(key);
      if(dfp != null){
        V toRet = this.getPointerValue(dfp);
        dfp = this.valueFile.update(dfp, valueMapping.toByteStorable(value));
        pointerMap.put(key, dfp);
        return toRet;
      }
      else{
        dfp = this.valueFile.insert(valueMapping.toByteStorable(value));
        pointerMap.put(key, dfp);
        return null;
      }
    }
    catch(Exception e){
      CoreLog.L().log(Level.SEVERE, "", e);
      return null;
    }
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
      DynamicFilePointer dfp = pointerMap.remove(key);
      if(dfp == null) return null;
      V toRet = this.getPointerValue(dfp);
      this.valueFile.delete(dfp);
      return toRet;
    }
    catch(IOException e){
      CoreLog.L().log(Level.SEVERE, "", e);
    }
    return null;
  }

  @Override
  public int size() {
    return this.pointerMap.size();
  }

  
  private V getPointerValue(ByteStorable pointer){
    if(pointer == null) return null;
    try{
      DynamicFilePointer dfp = (DynamicFilePointer) pointer;
      return this.valueMapping.fromByteStorable(this.valueFile.get(dfp));
    }
    catch(Exception e){
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return null;
  }

  class BlobMapIterator implements Iterator<Entry<K, V>>{

    //Iterator <KeyValue<ByteStorable, ByteStorable>> iter;
    Iterator <Map.Entry <K, DynamicFilePointer>> iter;

    public BlobMapIterator(){
      if(pointerMap instanceof DiscBasedMap)
        iter = ((DiscBasedMap) pointerMap).iterator();
      else
        iter = pointerMap.entrySet().iterator();
    }

    public BlobMapIterator(K key){
      if(pointerMap instanceof DiscBasedMap)
        iter = ((DiscBasedMap) pointerMap).iterator(key);
      else
        iter = pointerMap.tailMap(key).entrySet().iterator();
    }


    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public Entry<K, V> next() {
      Entry <K, DynamicFilePointer> next = iter.next();
      if(next == null) return null;
      V v = null;
      if(next.getValue() != null)
        v = getPointerValue(next.getValue());

      MapEntry <K,V> entry = new MapEntry<K, V>();
      entry.setKey(next.getKey());
      entry.setValue(v);
      return entry;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
  

}
