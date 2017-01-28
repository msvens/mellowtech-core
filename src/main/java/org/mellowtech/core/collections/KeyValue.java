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

import java.util.Map;


/**
 * Container for a key and value pair. The AbstractBPlusTree returns these when
 * iterating over the index.
 *
 * @author Martin Svensson
 * @version 1.0
 */


public class KeyValue<A, B> implements Map.Entry<A, B>, Comparable<KeyValue<A, B>> {

  private A key;
  private B value;


  public KeyValue() {
  }

  public KeyValue(A key){
    this(key,null);
  }

  public KeyValue(A key, B value) {
    this.key = key;
    this.value = value;
  }

  @Override
  @SuppressWarnings("unchecked")
  public int compareTo(KeyValue<A, B> o) {
    return ((Comparable<? super A>) key).compareTo(o.key);
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof KeyValue) {
      KeyValue other = (KeyValue) obj;
      return key.equals(other.key);
    }
    return false;
  }

  @Override
  public A getKey() {
    return key;
  }

  @Override
  public B getValue() {
    return value;
  }

  public void setKey(A key) {
    this.key = key;
  }

  @Override
  public B setValue(B value) {
    B oldvalue = this.value;
    this.value = value;
    return oldvalue;
  }
}