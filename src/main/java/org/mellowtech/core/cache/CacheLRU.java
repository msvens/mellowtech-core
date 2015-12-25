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
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.collections.DiscMap;

/**
 * Simple cache that use the least resently used scheme. When the cache limit is
 * reached and an item has to be unloaded the cache chooses the item that was
 * accessed longest ago. put, get, and remove all work in log(n) time.
 */
public class CacheLRU <K, V> extends AbstractCache <K,V> {

  LoadingCache<K,CacheValue <V>> lru;
  protected boolean memory = false;
  DiscMap <K,V> backend;

  protected CacheLRU(){
    ;
  }

  public CacheLRU(Remover<K,V> remover, Loader<K,V> loader, long size) {
    setRemover(remover);
    setSize(size);
    setLoader(loader);
    lru = this.buildCache();
  }

  public void clearCache() {
    lru.asMap().clear();
  }

  public void dirty(K key, V value){
    CacheValue <V> cv = null;
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

  public void notDirty(K key) {
    CacheValue <V> cv = null;
    try {
      cv = lru.get(key);
      if (cv == null)
        return;
      cv.setDirty(false);
    } catch (ExecutionException e) {
      CoreLog.L().log(Level.FINE, "", e);
    }
  }

  public V get(K key) throws NoSuchValueException{
    try{
      return lru.get(key).getValue();
    }
    catch(ExecutionException ee){
      throw new NoSuchValueException();
    }
  }

  @Override
  public V getIfPresent(K key) {
    CacheValue <V> v = lru.getIfPresent(key);
    if(v != null)
      return v.getValue();
    else
      return null;
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

  public void put(K key, V value){
    lru.put(key, new CacheValue<V>(value, true));
  }

  @Override
  public void refresh(K key) {
    lru.refresh(key);
  }

  public void remove(K key){
    lru.invalidate(key);
  }

  @Override
  public void emptyCache(){
    lru.invalidateAll();
  }

  public void setSize(long size) {
    maxSize = size;
    /*if (lru != null) {
      Map.CompResult entry;
      for (Iterator it = iterator(); it.hasNext();) {
        entry = (Map.CompResult) it.next();
        remover.remove(entry.getKey(), entry.getValue());
      }
    }
    lru = new LRUCache(size, 1.0f, true);
    */
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
                return new CacheValue<V>(value);
              }
            });

    }
    else {
      RemovalListener<K, CacheValue <V>> rl =
              new RemovalListener<K, CacheValue <V>>() {
                @Override
                public void onRemoval(RemovalNotification<K, CacheValue <V>> notification) {
                  remover.remove(notification.getKey(), notification.getValue());
                }
              };
      return CacheBuilder.newBuilder().maximumSize(maxSize).
              removalListener(rl).build(
              new CacheLoader<K, CacheValue<V>>() {
                public CacheValue<V> load(K key) throws Exception {
                  V value = loader.get(key);
                  return new CacheValue<V>(value);
                }
              });
    }
  }


}
