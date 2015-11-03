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

/**
 * The Remover is used as the remover function when the various caches
 * remove a key/value from their cache.
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public interface Remover<K, V> {

  /**
   * Operation to perform on a key/value pair that just has been remove from a
   * cache. e.g, if the cache is used to keep often accepted values in memory
   * instead of on disc, this method should store the key/value back to disc to
   * keep the changes made in the cache.
   * 
   * @param key
   *          a value of type 'Object'
   * @param value
   *          the CacheValue object indicates wheater the value has changed
   */
  void remove(K key, CacheValue<V> value);
}
