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

package org.mellowtech.core.bytestorable;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Date: 2013-04-16
 * Time: 20:12
 *
 * @author Martin Svensson
 */
public class CBSortedMap<K,V> extends BStorableImp <SortedMap<K,V>, CBSortedMap<K,V>> implements SortedMap<K,V> {



  public CBSortedMap(){
    super(new TreeMap<>());
  }

  public CBSortedMap(SortedMap <K, V> map){
    super(map);
  }
  
  @Override
  public CBSortedMap <K,V> create(SortedMap <K,V> map){return new CBSortedMap <> (map);}
  
  private Optional<PrimitiveType> getKeyTemp() throws ByteStorableException{
    return value.keySet().stream().findAny().map(v -> PrimitiveType.type(v));
  }
  
  private Optional<PrimitiveType> getValueTemp(){
    return value.values().stream().findAny().map(v -> PrimitiveType.type(v));
  }

  @Override
  public CBSortedMap <K,V> from(ByteBuffer bb) {
    CBUtil.getSize(bb, false); //read past size
    int elems = bb.getInt();
    if(elems < 1) return new CBSortedMap <K,V> ();
    
    //get the key and value types
    PrimitiveType keyType = PrimitiveType.fromOrdinal(bb.get());
    PrimitiveType valueType = PrimitiveType.fromOrdinal(bb.get());

    if(keyType == null || valueType == null)
      throw new ByteStorableException("Unrecognized types");
    
    BStorable <K,?> keyTemp = PrimitiveType.fromType(keyType);
    BStorable <V,?> valueTemp = PrimitiveType.fromType(valueType);
    
    SortedMap <K,V> tmpMap = new TreeMap <> ();
    
    while(elems-- != 0){
      K k = keyTemp.from(bb).get();
      if(bb.get() != 0){ //has value
        tmpMap.put(k, valueTemp.from(bb).get());
      } else {
        tmpMap.put(k, null);
      }
    }
    return new CBSortedMap <> (tmpMap);
 
  }

  @Override
  public void to(ByteBuffer bb) {
    CBUtil.putSize(internalSize(), bb, false);
    bb.putInt(value.size());
    
    Optional <PrimitiveType> keyTemp = this.getKeyTemp();
    Optional <PrimitiveType> valueTemp = this.getValueTemp();
    
    if(value.size() < 1)
      return;
    
    PrimitiveType ptKey = keyTemp.orElseThrow(() -> new ByteStorableException("no valid primitive key type"));
      
    bb.put(ptKey.getByte());
    bb.put(valueTemp.isPresent() ? valueTemp.get().getByte() : -1);

    for(Map.Entry <K,V> entry : value.entrySet()){
      PrimitiveType.fromType(ptKey, entry.getKey()).to(bb);
      if(entry.getValue() != null){
        bb.put((byte)1);
        PrimitiveType.fromType(valueTemp.get(), entry.getValue()).to(bb);
      } else {
        bb.put((byte)0);
      }
    }
  }

  @Override
  public int byteSize() {
    return CBUtil.byteSize(internalSize(), false);
  }
  
  private int internalSize() {
    int size = 4 + 2; //size + elems + types
    
    if(value.size() < 1)
      return size;
    
    PrimitiveType keyType = this.getKeyTemp().orElseThrow(() -> new ByteStorableException("no known key type"));
    Optional <PrimitiveType> valueTemp = this.getValueTemp();

    for(Map.Entry <K,V> entry : value.entrySet()){
      size += PrimitiveType.fromType(keyType, entry.getKey()).byteSize();
      size++;
      if(entry.getValue() != null){
        size += PrimitiveType.fromType(valueTemp.get(), entry.getValue()).byteSize();
      }
    }
    return size;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CBUtil.peekSize(bb, false);
  }


  @Override
  public int size() {
    return value.size();
  }

  @Override
  public boolean isEmpty() {
    return value.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return value.containsKey(key);
  }

  @Override
  public boolean containsValue(Object v) {
    return value.containsValue(value);
  }

  @Override
  public V get(Object key) {
    return value.get(key);
  }

  @Override
  public V put(K key, V v) {
    return value.put(key, v);
  }

  @Override
  public V remove(Object key) {
    return value.remove(key);
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    value.putAll(m);
  }

  @Override
  public void clear() {
    value.clear();
  }

  @Override
  public Comparator<? super K> comparator() {
    return value.comparator();
  }

  @Override
  public SortedMap<K, V> subMap(K fromKey, K toKey) {
    SortedMap <K, V> sub = value.subMap(fromKey, toKey);
    CBSortedMap <K,V> toRet = new CBSortedMap <K,V>(sub);
    return toRet;
  }

  @Override
  public SortedMap<K, V> headMap(K toKey) {
    SortedMap <K, V> sub = value.headMap(toKey);
    CBSortedMap <K,V> toRet = new CBSortedMap <K,V>(sub);
    return toRet;
  }

  @Override
  public SortedMap<K, V> tailMap(K fromKey) {
    SortedMap <K, V> sub = value.tailMap(fromKey);
    CBSortedMap <K,V> toRet = new CBSortedMap <K,V>(sub);
    return toRet;
  }

  @Override
  public K firstKey() {
      return value.firstKey();
  }

  @Override
  public K lastKey() {
    return value.lastKey();
  }

  @Override
  public Set<K> keySet() {
    return value.keySet();
  }

  @Override
  public Collection<V> values() {
    return value.values();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return value.entrySet();
  }
}
