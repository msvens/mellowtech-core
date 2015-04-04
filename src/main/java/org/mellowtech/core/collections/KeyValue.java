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
package org.mellowtech.core.collections;

import java.nio.ByteBuffer;
import java.util.Map.Entry;

import org.mellowtech.core.bytestorable.ByteStorable;

/**
 * Container for a key and value pair. The AbstractBPlusTree returns these when
 * iterating over the index.
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class KeyValue<K extends ByteStorable, V extends ByteStorable>  
extends ByteStorable <KeyValue<K,V>.KV> implements Entry<K,V>{
  
  public class KV {
    public K key;
    public V value;
    public String toString() {return "key: " + key + " value: " + value;}
  }
  
  

  /**
   * Creates a new <code>KeyValue</code> instance.
   * 
   * @param key
   *          this pairs key
   * @param value
   *          the pairs value
   */
  public KeyValue(K key, V value) {
    obj = new KV();
    obj.key = key;
    obj.value = value;
  }

  /**
   * Does nothing. Used when reading and writing KeyValues using the implmented
   * ByteStorable methods.
   * 
   */
  public KeyValue() {
    this(null, null);
  }

  /**
   * Returns the key of this key/value pair.
   * 
   * @return the key
   */
  public K getKey() {
    return obj.key;
  }

  /**
   * Set the key of this key/value pair.
   * 
   * @param key
   *          the new key
   */
  public void setKey(K key) {
    obj.key = key;
  }

  /**
   * Returns the value of this key/value pair.
   * 
   * @return the value
   */
  public V getValue() {
    return obj.value;
  }

  /**
   * Set the value of this key/value pair.
   * 
   * @param value
   *          the value
   */
  public V setValue(V value) {
    V oldvalue = obj.value;
    obj.value = value;
    return oldvalue;
  }

  // *******************STORABLE METHODS:
  public int byteSize() {
    return obj.key.byteSize() + obj.value.byteSize() + 4;
  }

  public int byteSize(ByteBuffer bb) {
    int bSize = bb.getInt();
    bb.position(bb.position() - 4);
    return bSize;
  }

  public void toBytes(ByteBuffer bb) {
    bb.putInt(byteSize());
    obj.key.toBytes(bb);
    obj.value.toBytes(bb);
  }

  public ByteStorable <KV> fromBytes(ByteBuffer bb, boolean doNew) {
    KeyValue <K,V> keyValue = doNew ? new KeyValue <K,V> () : this;
    bb.position(bb.position() + 4); // read past byteSize;
    keyValue.obj.key = (K) obj.key.fromBytes(bb, true);
    keyValue.obj.value = (V) obj.value.fromBytes(bb, true);
    return keyValue;
  }

  public String toString() {
    return obj.toString();
  }

  @Override
  public int hashCode() {
    // TODO Auto-generated method stub
    return obj.key.hashCode();
  }

  @Override
  public int compareTo(ByteStorable<KeyValue<K,V>.KV> t) throws UnsupportedOperationException {
    return obj.key.compareTo(t.get().key);
  }
  
  
}
