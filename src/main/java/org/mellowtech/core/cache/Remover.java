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
