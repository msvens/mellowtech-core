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
import java.nio.file.Path;
import java.util.*;

import org.mellowtech.core.codec.BCodec;
import org.mellowtech.core.collections.*;
import org.mellowtech.core.util.MapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a Disc Base Map. Based on the Mellowtech BTree. 
 * @author Martin Svensson
 *
 */
public class DiscBasedMap <A,B> implements SortedDiscMap<A,B> {
  
  
  protected BTree<A,B> btree;

  final private Logger logger = LoggerFactory.getLogger(DiscBasedMap.class);
  private final BCodec<A> keyCodec;
  private final BCodec<B> valueCodec;


  public DiscBasedMap(BCodec<A> keyCodec, BCodec<B> valueCodec, Path fileName,
      BTreeBuilder builder) throws Exception {
    this.keyCodec = keyCodec;
    this.valueCodec = valueCodec;
    this.btree = builder.build(keyCodec, valueCodec, fileName.getParent(), fileName.getFileName().toString());
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
  public Iterator<Entry<A,B>> iterator(boolean descending, A from,
                                        boolean fromInclusive, A to, boolean toInclusive) {
    //return btree.iterator(descending, from, fromInclusive, to, toInclusive);
    return new DiscBasedMapIterator(descending, from, fromInclusive, to, toInclusive);
  }


  /*************Override NavigableMap Methods**************************/
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.save();
  }

  @Override
  public java.util.Map.Entry<A,B> ceilingEntry(A key) {
    A k = this.ceilingKey(key);
    return k == null ? null : new MapEntry<>(k, this.get(k));
  }

  @Override
  public A ceilingKey(A key) {
    TreePosition tp;
    try {
      tp = this.btree.getPositionWithMissing(key);
    } catch (IOException e) {
      logger.warn("",e);
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
        return this.btree.getKey(pos);
      } catch (IOException e) {
        logger.warn("",e);
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
  public NavigableMap<A,B> descendingMap() {
    return new DescendingMap<>(this, null, false, null, false);
  }

  @Override
  public java.util.Map.Entry<A,B> firstEntry() {
    A k = this.firstKey();
    return k == null ? null : new MapEntry <> (k, this.get(k));
  }

  @Override
  public java.util.Map.Entry<A,B> floorEntry(A key) {
    A k = this.floorKey(key);
    return k == null ? null : new MapEntry <> (k, this.get(k));
  }

  @Override
  public A floorKey(A key) {
    try {
      TreePosition tp = this.btree.getPositionWithMissing(key);
      if(tp.isExists())
        return btree.getKey(tp.getSmaller());
      else if(tp.getSmaller() > 0)
        return btree.getKey(tp.getSmaller() - 1);
      return null;
      
    }
    catch(IOException e){
      logger.warn("",e);
      return null;
    }
  }

  @Override
  public SortedMap<A,B> headMap(A toKey) {
    return headMap(toKey, false);
  }

  @Override
  public NavigableMap<A,B> headMap(A toKey, boolean inclusive) {
    return subMap(null, false, toKey, inclusive);
  }

  @Override
  public java.util.Map.Entry<A,B> higherEntry(A key) {
    A k = this.higherKey(key);
    return k == null ? null : new MapEntry <> (k, this.get(k));
  }

  @Override
  public A higherKey(A key) {
	try {
      TreePosition tp = btree.getPositionWithMissing(key);
      if(tp == null) return null;
      // tp.getSmaller() is the total number of smaller elements than "key", so the number of elements 
      // higher than "key" is size() - (tp.getSmaller() + 1)
      //int higher = this.size() - (tp.getSmaller()+1);
      int pos = tp.exists() ? tp.getSmaller() + 1 : tp.getSmaller();
      int higher = this.size() - pos;
      if (higher > 0) {
        return btree.getKey(pos);
      }
      return null;
    } catch (IOException e) {
      logger.warn("",e);
      return null;
    }
  }

  @Override
  public java.util.Map.Entry<A,B> lastEntry() {
    A k = this.lastKey();
    return k == null ? null : new MapEntry <> (k, this.get(k));
  }

  @Override
  public java.util.Map.Entry<A,B> lowerEntry(A key) {
    A k = this.lowerKey(key);
    return k == null ? null : new MapEntry <> (k, this.get(k));
  }

  @Override
  public A lowerKey(A key) {
    try {
      TreePosition tp = btree.getPositionWithMissing(key);
      if(tp.getSmaller() > 0) {
        return btree.getKey(tp.getSmaller() - 1);
      }
      return null;
      
    }
    catch(IOException e){
      logger.warn("",e);
      return null;
    }
  }

  @Override
  public NavigableSet<A> navigableKeySet() {
    return new RangeKeySet<>(this, null, false, null, false);
  }

  @Override
  public java.util.Map.Entry<A,B> pollFirstEntry() {
    A key = this.firstKey();
    if(key == null) return null;
    return new MapEntry<>(key, remove(key));
  }

  @Override
  public java.util.Map.Entry<A,B> pollLastEntry() {
    A key = this.lastKey();
    if(key == null) return null;
    return new MapEntry<>(key, remove(key));
  }

  @Override
  public SortedMap<A,B> subMap(A fromKey, A toKey) {
    return subMap(fromKey, true, toKey, false);
  }

  @Override
  public NavigableMap<A,B> subMap(A fromKey, boolean fromInclusive, A toKey,
      boolean toInclusive) {
    return new RangeMap<>(this, fromKey, fromInclusive, toKey, toInclusive);
  }

  @Override
  public SortedMap<A,B> tailMap(A fromKey) {
    return tailMap(fromKey, true);
  }

  @Override
  public NavigableMap<A,B> tailMap(A fromKey, boolean inclusive) {
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
  public Set<java.util.Map.Entry<A,B>> entrySet() {
    return new DBEntrySet<>(this);
  }

  @Override
  public A firstKey() {
    if(this.isEmpty()) return null;
    try {
      return btree.getKey(0);
    } catch (IOException e) {
      logger.warn("",e);
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
      return btree.getKey(btree.size()-1);
    } catch(IOException e){
      logger.warn("",e);
    }
    return null;
  }

  @Override
  public Collection <B> values() {
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
        return btree.containsKey((A) key);
    } catch (IOException e) {
      logger.warn("",e);
    }
    return false;
  }

  @Override
  public boolean containsValue(Object value) {
    for(Iterator <KeyValue<A,B>> iter = this.btree.iterator(); iter.hasNext();){
      B c = iter.next().getValue();
      if(c.equals(value))
        return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public B get(Object key) {
    try {
      return this.btree.get((A)key);
    } catch (IOException e) {
      logger.warn("",e);
    }
    return null;
  }

  @Override
  public boolean isEmpty() {
    return this.size() < 1;
  }

  @Override
  public B put(A key, B value) {
    //C toRet = null;
    try {
      this.btree.put(key, value);
    } catch (IOException e) {
      logger.warn("",e);
    }
    return null;
  }

  @Override
  public void putAll(Map<? extends A, ? extends B> m) {
    for(Entry<? extends A, ? extends B> entry : m.entrySet()){
      this.put(entry.getKey(), entry.getValue());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public B remove(Object key) {
    try{
      return btree.remove((A)key);
      //BStorable<C> prev = this.btree.remove(keyMapping.create((A)key));
      //return prev != null ? prev.get() : null;
    }
    catch(Exception e){
      logger.warn("",e);
      return null;
    }
  }

  @Override
  public int size() {
    try{
      return this.btree.size();
    }
    catch(Exception e){
      logger.warn("",e);
    }
    return 0;
  }

  private class DiscBasedMapIterator implements Iterator<Entry<A,B>>{

    Iterator <KeyValue<A,B>> iter;

    DiscBasedMapIterator(boolean descending,
                                A from, boolean fromInclusive,
                                A to, boolean toInclusive){

      iter = btree.iterator(descending, from, fromInclusive,
          to, toInclusive);
    }


    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public Entry<A,B> next() {
      KeyValue <A,B> next = iter.next();
      if(next == null) return null;
      return new MapEntry<A, B>(next.getKey(),next.getValue());
      /*MapEntry <A,B> entry = new MapEntry<>();
      entry.setKey(next.getKey().get());
      if(next.getValue() != null)
        entry.setValue(next.getValue().get());
      return entry;*/
    }

  }


}
