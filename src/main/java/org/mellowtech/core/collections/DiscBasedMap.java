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
 * DiscBasedMap.java, org.mellowtech.core.collections
 * 
 * This is a first shot of a disc based hmap that is compliant with
 * the java.util.collections package. It is based on the Mellowtech
 * BTree implementation. The BTree implementation should later be
 * updated to offer more efficient key iterators.
 * 
 * @author Martin Svensson
 */
package org.mellowtech.core.collections;


import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.collections.tree.BTree;
import org.mellowtech.core.collections.tree.BTreeBuilder;
import org.mellowtech.core.collections.tree.TreePosition;
import org.mellowtech.core.util.MapEntry;

/**
 * Implementation of a Disc Base Map. Based on the Mellowtech BTree. 
 * @author Martin Svensson
 *
 */
public class DiscBasedMap <A,B extends BComparable<A,B>, 
  C, D extends BStorable<C,D>> implements NavigableMap<A, C>, DiscMap <A,C> {
  
  
  private BTree <A,B,C,D> btree;
  
  private B keyMapping;
  private D valueMapping;
  
  public static int DEFAULT_KEY_BLOCK = 2048*1;
  public static int DEFAULT_VALUE_BLOCK = 2048*2;
  
  

  public DiscBasedMap(Class <B> keyClass, Class <D> valueClass,
                      String fileName, boolean blobValues, boolean inMemory) throws Exception{
    this(keyClass, valueClass, fileName, DEFAULT_VALUE_BLOCK, DEFAULT_KEY_BLOCK, blobValues, inMemory);
  }

  public DiscBasedMap(Class <B> keyClass, Class <D> valueClass,
                      String fileName, int valueBlockSize, boolean blobValues, boolean inMemory) throws Exception{
    this(keyClass, valueClass, fileName, valueBlockSize, DEFAULT_KEY_BLOCK, blobValues, inMemory);
  }
  
  public DiscBasedMap(Class <B> keyClass, Class <D> valueClass,
      String fileName, int valueBlockSize, int keyBlockSize, boolean blobValues, boolean inMemory) throws Exception{
    this(keyClass, valueClass, fileName, new BTreeBuilder().blobValues(blobValues).valuesInMemory(inMemory)
        .valueBlockSize(valueBlockSize).indexBlockSize(keyBlockSize));
  }
  
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
  public Iterator<Entry<A, C>> iterator() {
    return new DiscBasedMapIterator();
  }

  @Override
  public Iterator<Entry<A, C>> iterator(A key) {
    return new DiscBasedMapIterator(key);
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
    int pos;
    if(tp.exists())
      pos = tp.getSmaller();
    else
      pos = tp.getSmaller() + 1;
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
    return null;
  }

  @Override
  public NavigableMap<A, C> descendingMap() {
    return null;
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
    throw new Error("views not supported");
  }

  @Override
  public NavigableMap<A, C> headMap(A toKey, boolean inclusive) {
    throw new Error("views not supported");
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
      int higher = this.size() - (tp.getSmaller()+1); 
      if (higher > 0) {
        return btree.getKey(tp.getSmaller()+1).get();
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
      if(tp.getSmaller() > 0)
        return btree.getKey(tp.getSmaller() - 1).get();
      return null;
      
    }
    catch(IOException e){
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    }
  }

  @Override
  public NavigableSet<A> navigableKeySet() {
    return (TreeSet <A>) this.keySet();
  }

  @Override
  public java.util.Map.Entry<A,C> pollFirstEntry() {
    A key = this.firstKey();
    C value = this.get(key);
    MapEntry <A,C> me = new MapEntry <> (key, value);
    this.remove(key);
    return me;
  }

  @Override
  public java.util.Map.Entry<A,C> pollLastEntry() {
    A key = this.lastKey();
    C value = this.get(key);
    MapEntry <A,C> me = new MapEntry <> (key, value);
    this.remove(key);
    return me;
  }

  @Override
  public SortedMap<A,C> subMap(A fromKey, A toKey) {
    throw new Error("views not supported");
  }

  @Override
  public NavigableMap<A,C> subMap(A fromKey, boolean fromInclusive, A toKey,
      boolean toInclusive) {
    throw new Error("views not supported");
  }

  @Override
  public SortedMap<A,C> tailMap(A fromKey) {
    throw new Error("views not supported");
  }

  @Override
  public NavigableMap<A, C> tailMap(A fromKey, boolean inclusive) {
    //Iterator <Map.Entry<K, V>> iter = this.iterator(fromKey);
    throw new Error("views not supported");
    //return null;
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
    Set <Map.Entry<A, C>> toRet = new TreeSet <> ();
    for(Iterator <KeyValue <B,D>> iter = this.btree.iterator(); iter.hasNext();){
      KeyValue <B, D> keyValue = iter.next();
      A key = keyValue.getKey().get();
      C value = keyValue.getValue().get();
      toRet.add(new MapEntry <> (key, value));
    }
    return toRet;
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
    TreeSet <A> ts = new TreeSet <> ();
    for(Iterator <KeyValue <B, D>> iter = this.btree.iterator(); iter.hasNext();){
      ts.add(iter.next().getKey().get());
    }
    return ts;
  }

  @Override
  public A lastKey() {
    return null;
  }

  @Override
  public Collection <C> values() {
    ArrayList <C> al = new ArrayList <> ();
    for(Iterator <KeyValue <B,D>>iter = this.btree.iterator(); iter.hasNext();){
      al.add(iter.next().getValue().get());
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
      return btree.containsKey(keyMapping.create((A) key));
    } catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return false;
  }

  @Override
  public boolean containsValue(Object value) {
    for(Iterator <KeyValue <B,D>> iter = this.btree.iterator(); iter.hasNext();){
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
    return this.size()< 1 ? true : false;
  }

  @Override
  public C put(A key, C value) {
    //ByteComparable <K> bc = keyMapping.toByteComparable(key);
    //ByteStorableOld <V> bs = valueMapping.toByteStorable(value);
    C toRet = null;
    try {
      this.btree.put(keyMapping.create(key), valueMapping.create(value));
    } catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return toRet;
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

    public DiscBasedMapIterator(A key){
      iter = btree.iterator(keyMapping.create(key));
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

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }


}