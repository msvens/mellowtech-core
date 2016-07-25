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

import com.google.common.cache.*;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.mellowtech.core.collections.DiscMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple cache that use the least recently used scheme. When the cache limit is
 * reached and an item has to be unloaded the cache chooses the item that was
 * accessed longest ago. put, get, and remove all work in log(n) time.
 *
 * Backed by a LoadingCache
 *
 * @param <K> key type
 * @param <V> value type
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 * @see com.google.common.cache.LoadingCache
 *
 */
public class CacheLRU <K, V> extends AbstractCache <K,V> {

  LoadingCache<K,CacheValue <V>> lru;
  protected boolean memory = false;
  DiscMap <K,V> backend;
  private final Logger logger = LoggerFactory.getLogger(CacheLRU.class);


  public CacheLRU(){
    ;
  }

  /**
   * Create a new cache with a remover and loader
   * @param remover Remover object or null
   * @param loader Loader object
   * @param maxSize maximum number of elements
   */
  public CacheLRU(Remover<K, V> remover, Loader<K, V> loader, long maxSize) {
    setRemover(remover);
    setSize(maxSize);
    setLoader(loader);
    lru = this.buildCache();
  }

  @Override
  public void clearCache() {
    lru.asMap().clear();
  }

  @Override
  public void dirty(K key, V value){
    CacheValue <V> cv;
    //try {
      cv = lru.getIfPresent(key);
      if(cv == null){
        this.put(key, value);
      }
      else{
        cv.setValue(value);
        cv.setDirty();
      }
  }

  @Override
  public void notDirty(K key) {
    CacheValue <V> cv;
    try {
      cv = lru.get(key);
      if (cv == null)
        return;
      cv.setDirty(false);
    } catch (ExecutionException e) {
      logger.debug("",e);
    }
  }

  @Override
  public V get(K key) throws NoSuchValueException{
    try{
      return lru.get(key).getValue();
    }
    catch(ExecutionException ee){
      throw new NoSuchValueException();
    }
  }

  @Override
  public V getFromCache(K key){
    CacheValue <V> cv = lru.getIfPresent(key);
    return cv != null ? cv.getValue() : null;
  }


  @Override
  public Iterator <Map.Entry<K,CacheValue <V>>> iterator() {
    return lru.asMap().entrySet().iterator();
  }

  @Override
  public void put(K key, V value){
    lru.put(key, new CacheValue<>(value, true));
  }

  @Override
  public void refresh(K key) {
    lru.refresh(key);
  }

  @Override
  public void remove(K key){
    lru.invalidate(key);
  }

  @Override
  public void emptyCache(){
    lru.invalidateAll();
  }

  @Override
  public void setSize(long size) {
    maxSize = size;
  }

  @Override
  public long getCurrentSize() {
    return lru.size();
  }

  @Override
  public String toString(){
    return lru.stats().toString();
  }

  protected LoadingCache <K, CacheValue<V>> buildCache(){
    if(remover == null){
     return CacheBuilder.newBuilder().maximumSize(maxSize).build(
            new CacheLoader<K, CacheValue<V>>() {
              public CacheValue<V> load(K key) throws Exception {
                V value = loader.get(key);
                return new CacheValue<>(value);
              }
            });

    }
    else {
      RemovalListener<K, CacheValue <V>> rl =
          notification -> remover.remove(notification.getKey(), notification.getValue());
      return CacheBuilder.newBuilder().maximumSize(maxSize).
              removalListener(rl).build(
              new CacheLoader<K, CacheValue<V>>() {
                public CacheValue<V> load(K key) throws Exception {
                  V value = loader.get(key);
                  return new CacheValue<>(value);
                }
              });
    }
  }


}
