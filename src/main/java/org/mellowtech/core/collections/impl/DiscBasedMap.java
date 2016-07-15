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

/**
 * DiscBasedMap.java, org.mellowtech.core.collections
 * 
 * This is a first shot of a disc based hmap that is compliant with
 * the java.util.collections package. It is based on the Mellowtech
 * BTree implementation. The BTree implementation should later be
 * updated to offer more efficient key iterators.
 * 
 * @author Martin Svensson
 */
package org.mellowtech.core.collections.impl;


import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.collections.*;
import org.mellowtech.core.util.MapEntry;

/**
 * Implementation of a Disc Base Map. Based on the Mellowtech BTree. 
 * @author Martin Svensson
 *
 */
public class DiscBasedMap <A,B extends BComparable<A,B>, 
  C, D extends BStorable<C,D>> implements SortedDiscMap<A, C> {
  
  
  protected BTree<A,B,C,D> btree;
  
  protected B keyMapping;
  protected D valueMapping;
  
  //public static int DEFAULT_KEY_BLOCK = 1024*8;
  //public static int DEFAULT_VALUE_BLOCK = 1024*8;


  
  

  /*public DiscBasedMap(Class <B> keyClass, Class <D> valueClass,
                      String fileName, boolean blobValues, boolean inMemory) throws Exception{
    this(keyClass, valueClass, fileName, DEFAULT_VALUE_BLOCK, DEFAULT_KEY_BLOCK, blobValues, true, inMemory);
  }

  public DiscBasedMap(Class <B> keyClass, Class <D> valueClass,
                      String fileName, int valueBlockSize, boolean blobValues, boolean inMemory) throws Exception{
    this(keyClass, valueClass, fileName, valueBlockSize, DEFAULT_KEY_BLOCK, blobValues, true, inMemory);
  }
  
  public DiscBasedMap(Class <B> keyClass, Class <D> valueClass,
      String fileName, int valueBlockSize, int keyBlockSize, boolean blobValues, boolean keysMemoryMapped, boolean valsMemoryMapped) throws Exception{
    this(keyClass, valueClass, fileName, new BTreeBuilder().blobValues(blobValues).memoryMappedValues(valsMemoryMapped)
        .memoryMappedIndex(keysMemoryMapped).valueBlockSize(valueBlockSize).indexBlockSize(keyBlockSize));
  }*/
  
  public DiscBasedMap(Class <B> keyClass, Class <D> valueClass, String fileName,
      BTreeBuilder builder) throws Exception {
    this.keyMapping = keyClass.newInstance();
    this.valueMapping = valueClass.newInstance();
    this.btree = builder.build(keyClass, valueClass, fileName);
  }
  
  public void save() throws IOException{
    this.btree.save();
  }
  
  public void close() throws IOException{
	  this.btree.close();
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
  public Iterator<Entry<A, C>> iterator(boolean descending, A from,
                                        boolean fromInclusive, A to, boolean toInclusive) {
    return new DiscBasedMapIterator(descending, from, fromInclusive, to, toInclusive);
  }


  /*************Override NavigableMap Methods**************************/
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.save();
  }

  @Override
  public java.util.Map.Entry<A, C> ceilingEntry(A key) {
    A k = this.ceilingKey(key);
    return k == null ? null : new MapEntry<>(k, this.get(k));
  }

  @Override
  public A ceilingKey(A key) {
    TreePosition tp;
    try {
      tp = this.btree.getPositionWithMissing(keyMapping.create(key));
    } catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    }
    int pos = tp.getSmaller();
    /*if(tp.exists())
      pos = tp.getSmaller();
    else
      pos = tp.getSmaller() + 1;*/
    int higher = this.size() - tp.getSmaller();
    if(higher > 0){
      try {
        //return this.keyMapping.fromByteComparable(this.btree.getKey(pos));
        return this.btree.getKey(pos).get();
      } catch (IOException e) {
        CoreLog.L().log(Level.WARNING, "", e);
        return null;
      }
    }
    return null;
  }

  @Override
  public NavigableSet<A> descendingKeySet() {
    return new DescendingKeySet<>(this, null, false, null, false);
  }

  @Override
  public NavigableMap<A, C> descendingMap() {
    return new DescendingMap<>(this, null, false, null, false);
  }

  @Override
  public java.util.Map.Entry<A, C> firstEntry() {
    A k = this.firstKey();
    return k == null ? null : new MapEntry <> (k, this.get(k));
  }

  @Override
  public java.util.Map.Entry<A, C> floorEntry(A key) {
    A k = this.floorKey(key);
    return k == null ? null : new MapEntry <> (k, this.get(k));
  }

  @Override
  public A floorKey(A key) {
    try {
      TreePosition tp = this.btree.getPositionWithMissing(keyMapping.create(key));
      if(tp.isExists())
        return btree.getKey(tp.getSmaller()).get();
      else if(tp.getSmaller() > 0)
        return btree.getKey(tp.getSmaller() - 1).get();
      return null;
      
    }
    catch(IOException e){
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    }
  }

  @Override
  public SortedMap<A, C> headMap(A toKey) {
    return headMap(toKey, false);
  }

  @Override
  public NavigableMap<A, C> headMap(A toKey, boolean inclusive) {
    return subMap(null, false, toKey, inclusive);
  }

  @Override
  public java.util.Map.Entry<A, C> higherEntry(A key) {
    A k = this.higherKey(key);
    return k == null ? null : new MapEntry <> (k, this.get(k));
  }

  @Override
  public A higherKey(A key) {
	try {
      TreePosition tp = btree.getPositionWithMissing(keyMapping.create(key));
      if(tp == null) return null;
      // tp.getSmaller() is the total number of smaller elements than "key", so the number of elements 
      // higher than "key" is size() - (tp.getSmaller() + 1)
      //int higher = this.size() - (tp.getSmaller()+1);
      int pos = tp.exists() ? tp.getSmaller() + 1 : tp.getSmaller();
      int higher = this.size() - pos;
      if (higher > 0) {
        return btree.getKey(pos).get();
      }
      return null;
    } catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    }
  }

  @Override
  public java.util.Map.Entry<A, C> lastEntry() {
    A k = this.lastKey();
    return k == null ? null : new MapEntry <> (k, this.get(k));
  }

  @Override
  public java.util.Map.Entry<A, C> lowerEntry(A key) {
    A k = this.lowerKey(key);
    return k == null ? null : new MapEntry <> (k, this.get(k));
  }

  @Override
  public A lowerKey(A key) {
    try {
      TreePosition tp = btree.getPositionWithMissing(keyMapping.create(key));
      if(tp.getSmaller() > 0) {
        return btree.getKey(tp.getSmaller() - 1).get();
      }
      return null;
      
    }
    catch(IOException e){
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    }
  }

  @Override
  public NavigableSet<A> navigableKeySet() {
    return new RangeKeySet<>(this, null, false, null, false);
  }

  @Override
  public java.util.Map.Entry<A,C> pollFirstEntry() {
    A key = this.firstKey();
    if(key == null) return null;
    return new MapEntry<A,C>(key, remove(key));
  }

  @Override
  public java.util.Map.Entry<A,C> pollLastEntry() {
    A key = this.lastKey();
    if(key == null) return null;
    return new MapEntry<A,C>(key, remove(key));
  }

  @Override
  public SortedMap<A,C> subMap(A fromKey, A toKey) {
    return subMap(fromKey, true, toKey, false);
  }

  @Override
  public NavigableMap<A,C> subMap(A fromKey, boolean fromInclusive, A toKey,
      boolean toInclusive) {
    return new RangeMap<>(this, fromKey, fromInclusive, toKey, toInclusive);
  }

  @Override
  public SortedMap<A,C> tailMap(A fromKey) {
    return tailMap(fromKey, true);
  }

  @Override
  public NavigableMap<A, C> tailMap(A fromKey, boolean inclusive) {
    return subMap(fromKey, inclusive, null, false);
  }

  /**
   * Always relies on the compare function defined in the keyMapping
   */
  @Override
  public Comparator<? super A> comparator() {
    return null;
  }

  @Override
  public Set<java.util.Map.Entry<A, C>> entrySet() {
    return new DBEntrySet<>(this);
  }

  @Override
  public A firstKey() {
    if(this.isEmpty()) return null;
    try {
      return btree.getKey(0).get();
    } catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return null;
  }

  @Override
  public Set<A> keySet() {
    return new DBKeySet<>(this);
  }

  @Override
  public A lastKey() {
    if(this.isEmpty()) return null;
    try{
      return btree.getKey(btree.size()-1).get();
    } catch(IOException e){
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return null;
  }

  @Override
  public Collection <C> values() {
    return new DBValueCollection<>(this);
  }

  @Override
  public void clear() {
    try {
      btree.truncate();
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean containsKey(Object key) {
    try {
      return btree.containsKey(keyMapping.create((A) key));
    } catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return false;
  }

  @Override
  public boolean containsValue(Object value) {
    for(Iterator <KeyValue<B,D>> iter = this.btree.iterator(); iter.hasNext();){
      C c = iter.next().getValue().get();
      if(c.equals(value))
        return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public C get(Object key) {
    try {
      D value = this.btree.get(keyMapping.create((A)key));
      return value != null ? value.get() : null;
    } catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return null;
  }

  @Override
  public boolean isEmpty() {
    return this.size() < 1;
  }

  @Override
  public C put(A key, C value) {
    //C toRet = null;
    try {
      this.btree.put(keyMapping.create(key), valueMapping.create(value));
    } catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return null;
  }

  @Override
  public void putAll(Map<? extends A, ? extends C> m) {
    for(Entry<? extends A, ? extends C> entry : m.entrySet()){
      this.put(entry.getKey(), entry.getValue());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public C remove(Object key) {
    try{
      D prev = this.btree.remove(keyMapping.create((A)key));
      return prev != null ? prev.get() : null;
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

  class DiscBasedMapIterator implements Iterator<Entry<A, C>>{

    Iterator <KeyValue<B, D>> iter;

    public DiscBasedMapIterator(){
      iter = btree.iterator();
    }

    public DiscBasedMapIterator(boolean descending,
                                A from, boolean fromInclusive,
                                A to, boolean toInclusive){
      B fKey = from != null ? keyMapping.create(from) : null;
      B tKey = to != null ? keyMapping.create(to) : null;
      iter = btree.iterator(descending, fKey, fromInclusive,
          tKey, toInclusive);
    }


    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public Entry<A, C> next() {
      KeyValue <B,D> next = iter.next();
      if(next == null) return null;
      MapEntry <A,C> entry = new MapEntry<>();
      entry.setKey(next.getKey().get());
      if(next.getValue() != null)
        entry.setValue(next.getValue().get());
      return entry;
    }

  }


}
