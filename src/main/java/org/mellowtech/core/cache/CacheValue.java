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
package org.mellowtech.core.cache;

public class CacheValue <V> {
  V value = null;
  boolean dirty = false;

  public CacheValue(V value) {
    this(value, false);
  }

  public CacheValue(V value, boolean dirty){
   this.value = value;
    this.dirty = dirty;
  }

  /**
   * Return true if the value has changed since it was added to cache.
   * 
   * @return a value of type 'boolean'
   */
  public boolean isDirty() {
    return dirty;
  }

  /**
   * Mark this value as changed from entering the cache.
   * 
   */
  public void setDirty() {
    dirty = true;
  }

  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }

  /**
   * Get the value. If the isDirty returns false it should be safe to do nothing
   * with the value, i.e. it has not changed since it entered the cache.
   * 
   * @return a value of type 'Object'
   */
  public V getValue() {
    return value;
  }

  /**
   * Set the value and keep the dirty bit as is
   * @param value value to set
   */
  public void setValue(V value) {
    this.value = value;
  }

  /**
   * Set the value and set the the dirty bit to false
   * @param value value to set
   */
  public void setAndRestore(V value) {
    this.value = value;
    dirty = false;
  }
}
