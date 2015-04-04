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

package org.mellowtech.core.bytestorable;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Date: 2013-04-16
 * Time: 20:12
 *
 * @author Martin Svensson
 */
public class CBMap <K,V> extends ByteStorable <Map<K,V>> implements Map<K,V> {

  private Map<K,V> map;


  public CBMap (){
    this.map = new HashMap<>();
  }

  @Override
  public ByteStorable <Map<K,V>> fromBytes(ByteBuffer bb, boolean doNew) {
    CBMap <K,V> toRet = doNew ? new CBMap <K,V>() : this;
    toRet.clear();
    bb.getInt(); //past size;
    int elems = bb.getInt();
    if(elems < 1) return toRet;
    PrimitiveType keyType = PrimitiveType.fromOrdinal(bb.get());
    PrimitiveType valType = PrimitiveType.fromOrdinal(bb.get());

    if(keyType == null || valType == null)
      throw new ByteStorableException("Unrecognized types");

    ByteStorable <K> keyTemp = PrimitiveType.fromType(keyType);
    ByteStorable <V> valTemp = PrimitiveType.fromType(valType);

    for(int i = 0; i < elems; i++){
      K k = (K) keyTemp.fromBytes(bb).get();
      byte b = bb.get();
      if(b != 0)
        toRet.map.put(k, (V) valTemp.fromBytes(bb).get());
      else
        toRet.map.put(k, null);
    }
    return toRet;
  }

  @Override
  public void toBytes(ByteBuffer bb) {
    bb.putInt(byteSize()); //this could be done more efficiently
    bb.putInt(map.size());

    if(map.size() < 1)
      return;

    ByteStorable <K> keyTemp = null;
    ByteStorable <V> valTemp = null;
    byte keyType = 0, valType = 0;

    int pos = bb.position(); //we will later write in here:
    bb.put(keyType); bb.put(valType); //just write dummy values;

    for(Map.Entry <K,V> entry : map.entrySet()){
      if(keyTemp == null){ //should be the first:
        PrimitiveType pt = PrimitiveType.type(entry.getKey());
        if(pt == null) throw new ByteStorableException("Unrecognized key Type");
        keyType = pt.getByte();
        keyTemp = PrimitiveType.fromType(pt);
      }
      if(valTemp == null && entry.getValue() != null){
        PrimitiveType pt = PrimitiveType.type(entry.getValue());
        if(pt == null) throw new ByteStorableException("Unrecognized value Type");
        valType = pt.getByte();
        valTemp = PrimitiveType.fromType(pt);
      }
      keyTemp.set(entry.getKey());
      keyTemp.toBytes(bb);
      if(entry.getValue() != null){
        bb.put((byte)1);
        valTemp.set(entry.getValue());
        valTemp.toBytes(bb);
      }
      else{
        bb.put((byte)0);
      }
    }
    bb.put(pos, keyType);
    bb.put(pos+1, valType);
  }

  @Override
  public int byteSize() {
    int size = 8 + 2; //size + elems + types
    if(map.size() < 1)
      return size;
    ByteStorable <K> keyTemp = null;
    ByteStorable <V> valTemp = null;
    byte keyType, valType;

    for(Map.Entry <K,V> entry : map.entrySet()){
      if(keyTemp == null){
        PrimitiveType pt = PrimitiveType.type(entry.getKey());
        if(pt == null) throw new ByteStorableException("Unrecognized key Type");
        keyType = pt.getByte();
        keyTemp = PrimitiveType.fromType(pt);
      }
      if(valTemp == null && entry.getValue() != null){
        PrimitiveType pt = PrimitiveType.type(entry.getValue());
        if(pt == null) throw new ByteStorableException("Unrecognized value Type");
        valType = pt.getByte();
        valTemp = PrimitiveType.fromType(pt);
      }
      keyTemp.set(entry.getKey());
      size += keyTemp.byteSize();
      size++; //null indicator
      if(entry.getValue() != null){
        valTemp.set(entry.getValue());
        size += valTemp.byteSize();
      }
    }
    return size;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return getSizeFour(bb);
  }

  @Override
  public Map <K,V> get(){
    return this.map;
  }

  @Override
  public void set(Map <K,V> map){
    this.map = map;
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  @Override
  public V get(Object key) {
    return map.get(key);
  }

  @Override
  public V put(K key, V value) {
    return map.put(key, value);
  }

  @Override
  public V remove(Object key) {
    return map.remove(key);
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    map.putAll(m);
  }

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public Set<K> keySet() {
    return map.keySet();
  }

  @Override
  public Collection<V> values() {
    return map.values();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return map.entrySet();
  }
}
