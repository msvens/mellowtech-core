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
package com.mellowtech.core.cache;

import com.google.common.cache.*;
import com.mellowtech.core.CoreLog;
import com.mellowtech.core.collections.DiscMap;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

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
