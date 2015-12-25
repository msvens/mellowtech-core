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

package org.mellowtech.core.collections;

import java.nio.ByteBuffer;
import java.util.Map.Entry;

import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BComparableImp;
import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.bytestorable.CBUtil;


/**
 * Container for a key and value pair. The AbstractBPlusTree returns these when
 * iterating over the index.
 * 
 * @author Martin Svensson
 * @version 1.0
 */


public class KeyValue<K extends BComparable<?,K>, V extends BStorable<?,V>>  
  extends BComparableImp <KeyValue.KV <K,V>, KeyValue<K,V>> implements Entry<K,V>{

  public static class KV< K extends BStorable<?,K>, V extends BStorable<?,V>> {
    public KV(){}
    public KV(K k, V v){key = k; value = v;}
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
    super(new KV <K,V> (key,value));
  }

  public KeyValue(K key){this(key, null);}

  /**
   * Does nothing. Used when reading and writing KeyValues using the implemented
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
    return get().key;
  }

  /**
   * Set the key of this key/value pair.
   * 
   * @param key
   *          the new key
   */
  public void setKey(K key) {
    get().key = key;
  }

  /**
   * Returns the value of this key/value pair.
   * 
   * @return the value
   */
  public V getValue() {
    return get().value;
  }

  /**
   * Set the value of this key/value pair.
   * 
   * @param value
   *          the value
   */
  public V setValue(V value) {
    V oldvalue = get().value;
    get().value = value;
    return oldvalue;
  }

  // *******************STORABLE METHODS:
  public int byteSize() {
    return CBUtil.byteSize(get().key.byteSize() + get().value.byteSize(), true);
  }

  public int byteSize(ByteBuffer bb) {
    return CBUtil.peekSize(bb, true);
  }

  public void to(ByteBuffer bb) {
    BComparable <?,K> k = get().key;
    BStorable <?,V> v = get().value;
    CBUtil.putSize(k.byteSize()+v.byteSize(), bb, true);
    k.to(bb);
    v.to(bb);
  }

  public KeyValue <K,V> from(ByteBuffer bb) {
    CBUtil.getSize(bb, true);
    K k = get().key.from(bb);
    V v = get().value.from(bb);
    return new KeyValue <K,V> (k,v);
  }

  @Override
  public int compareTo(KeyValue <K,V> t) throws UnsupportedOperationException {
    return value.key.compareTo(t.value.key);
  }

  @Override
  public int hashCode() {
    return value.key.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof KeyValue && obj != null) {
      KeyValue other = (KeyValue) obj;
      return value.key.equals(other.getKey());
    }
    return false;
  }
  
  
  
  
}
