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

import java.util.Iterator;
import java.util.Map;

/**
 * Generic model for caches. The user of a cache provides a Remover
 * function that is called everytime something is unloaded from cache. Also note
 * that the remover function should only be called if the value of the key has
 * been changed. A key that has been "put" into the cache more than one time
 * (i.e. the key/value pair is update) is considered to be changed.
 *
 * @param <K> key type
 * @param <V> value type
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 */
public abstract class AbstractCache <K, V>{

  Remover<K, V> remover;
  Loader<K, V> loader;
  long maxSize;

  /**
   * Clears the cache without calling any remover functions. Should be used
   * with care
   */
  public abstract void clearCache();

  /**
   * Tell the cache that when removing this key from cache the remover's remove
   * function should be called. Always call this method if the content in the
   * value has changed. If the key has been unloaded the key/value is reinserted
   * into the cache. The value will only be reinserted into the cache if the key
   * did not exist. i.e. it is presumed that value object is the same as the old
   * one.
   * 
   * @param key
   *          the effect key
   * @param value
   *          the value that has been changed
   */
  public abstract void dirty(K key, V value);

  /**
   * Marks the value for a key as not changed
   * 
   * @param key the key to find
   */
  public abstract void notDirty(K key);

  /**
   * Set the remover for cache
   * @param remover a Remover object
   */
  public void setRemover(Remover<K, V> remover) {
    this.remover = remover;
  }

  public void setLoader(Loader<K,V> loader) {
    this.loader = loader;
  }

  /**
   * Empty the cache. For each item in the cache the Remover.remove function will be
   * called.
   */
  public void emptyCache() {
    if(!isReadOnly()){
      Map.Entry <K, CacheValue <V>> entry;
      Iterator <Map.Entry<K, CacheValue <V>>> it = iterator();
      while(it.hasNext()) {
        entry = it.next();
        remover.remove(entry.getKey(), entry.getValue());
      }
    }
    clearCache();
  }


  /**
   * Get a value
   * @param key key to find
   * @return value
   * @throws NoSuchValueException if value is not present
   */
  public abstract V get(K key) throws NoSuchValueException;

  /**
   * Get a value only if it is present in the cache
   * @param key key to find
   * @return value associated with key in this cache, or null if there is no cached value for key
   */
  public abstract V getFromCache(K key);

  /**
   * Checks if this cache is readOnly. Default implement checks
   * if this cache has remover
   * @return true if read only
   */
  public boolean isReadOnly(){
    return this.remover == null;
  }

  /**
   * Return an iterator over the cache. The iterator is sorted over keys. The
   * returned objects are Map.CompResult objects.
   * 
   * @return an iterator over this cache
   * @see java.util.Map.Entry
   */
  public abstract Iterator <Map.Entry<K,CacheValue <V>>> iterator();

  /**
   * Put a key/value pair directly into the cache (bypassing any loaded value).
   * If the value was previously in the cache the value will be set to dirty.
   * 
   * @param key
   *          the key
   * @param value
   *          corresponding value, can be null
   */
  public abstract void put(K key, V value);

  /**
   * Refresh a value in the cache by loading it again
   * @param key key to load
   */
  public abstract void refresh(K key);

  /**
   * Removes a key/value pair from the cache.
   * 
   * @param key
   *          the key to remove
   */
  public abstract void remove(K key);

  /**
   * Set the new size of the cache. A negative value indicates a cache with no
   * limit. If the new size is less than the oldsize keys should be removed from
   * the cache.
   * 
   * @param size
   *          can be negative
   */
  public abstract void setSize(long size);

  /**
   * Approximate number of elements in this cache
   * @return number of elements
   */
  public abstract long getCurrentSize();

  /**
   * Return the maxCapacity of the cache. A negative value indicates a cache
   * with no limit.
   * 
   * @return an <code>int</code> value
   */
  public long maxSize() {
    return maxSize;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    Map.Entry<K, CacheValue<V>> entry;
    for (Iterator <Map.Entry<K, CacheValue<V>>> it = iterator(); it.hasNext();) {
      entry = it.next();
      sb.append(entry.getKey() + " " + entry.getValue() + "\n");
    }
    return sb.toString();
  }

  class Callback implements Remover<K,V> {
    public void remove(K key, CacheValue <V> value) {
      ;
    }
  }
}
