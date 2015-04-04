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
   */
  public void setValue(V value) {
    this.value = value;
  }

  /**
   * Set the value and set the the dirty bit to false
   */
  public void setAndRestore(V value) {
    this.value = value;
    dirty = false;
  }
}
