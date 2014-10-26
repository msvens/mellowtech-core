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
package com.mellowtech.core.disc;

import java.util.Map;

/**
 * Utility class to create that implements the Map.CompResult interface. Useful for
 * iterating over collections.
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class MapEntry <K,V> implements Map.Entry<K,V> {

  /**
   * The entry key
   * 
   */
  protected K k;

  /**
   * The entry value
   * 
   */
  protected V v;

  /**
   * Creates a new <code>MapEntry</code> instance.
   * 
   */
  public MapEntry() {
    ;
  }

  public MapEntry(K key, V value){
    this.k = key;
    this.v = value;
  }

  /**
   * First compare the keys and if those are equal compre the values
   * 
   * @param o
   *          an object to compare with
   * @return true if both the key and value are equal
   */
  public boolean equals(Object o) {
    Map.Entry <K,V> e2 = (Map.Entry <K,V>) o;
    return (this.getKey() == null ? e2.getKey() == null : this.getKey().equals(
        e2.getKey()))
        && (this.getValue() == null ? e2.getValue() == null : this.getValue()
            .equals(e2.getValue()));
  }

  /**
   * Get the entry key
   * 
   * @return a key
   */
  public K getKey() {
    return k;
  }

  /**
   * Get the entry value
   * 
   * @return a value
   */
  public V getValue() {
    return v;
  }

  /**
   * Set key for this entry
   * 
   * @param k
   *          the key
   */
  public void setKey(K k) {
    this.k = k;
  }

  /**
   * Set the value for this entry
   * 
   * @param v
   *          the value
   */
  public V setValue(V v) {
    this.v = v;
    return this.v;
  }

  /**
   * Calculates hashCode value as follows.
   * 
   * <pre>
   * return (this.getKey() == null ? 0 : this.getKey().hashCode())
   *     &circ; (this.getValue() == null ? 0 : this.getValue().hashCode());
   * </pre>
   * 
   * @return hashCode
   */
  public int hashCode() {
    return (this.getKey() == null ? 0 : this.getKey().hashCode())
        ^ (this.getValue() == null ? 0 : this.getValue().hashCode());
  }

  /**
   * Describe <code>toString</code> method here.
   * 
   * @return a <code>String</code> value
   */
  public String toString() {
    return "key=" + getKey() + "\tvalue=" + getValue();
  }
}
