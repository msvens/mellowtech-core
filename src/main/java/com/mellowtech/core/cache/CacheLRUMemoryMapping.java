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
import com.mellowtech.core.collections.mappings.BSMapping;

/**
 * Date: 2012-11-02
 * Time: 10:45
 *
 * @author Martin Svensson
 */
public class CacheLRUMemoryMapping <K,V> extends CacheLRU <K, V> {

   private long memoryFootPrint;
   private BSMapping <K> keyMapping = null;
   private BSMapping <V> valueMapping = null;


  public CacheLRUMemoryMapping(Remover<K, V> remover, Loader<K, V> loader, long size,
                               boolean countMemoryFootPrint, BSMapping <K> keyMapping,
                               BSMapping <V> valueMapping) {
    this.keyMapping = keyMapping;
    this.valueMapping = valueMapping;
    setRemover(remover);
    setSize(size);
    setLoader(loader);
    this.memory = countMemoryFootPrint;
    lru = this.buildCache();
  }

  public long getMemoryFootPrint(){
    return this.memoryFootPrint;
  }

  @Override
  protected LoadingCache<K, CacheValue<V>> buildCache() {

    Weigher<K, CacheValue <V>> weighter = new Weigher<K, CacheValue<V>> () {
      @Override
      public int weigh(K key, CacheValue <V> value) {
        return keyMapping.byteSize(key) + valueMapping.byteSize(value.getValue());
      }
    };

    RemovalListener<K, CacheValue <V>> removalListener = this.getRemoveListener();
    CacheLoader<K, CacheValue<V>> cacheLoader = this.getCacheLoader();

    if(removalListener != null){
      return CacheBuilder.newBuilder().maximumWeight(maxSize()).
              weigher(weighter).removalListener(removalListener).build(cacheLoader);
    }
    else
      return CacheBuilder.newBuilder().maximumWeight(maxSize()).weigher(weighter).build(cacheLoader);
  }

  private CacheLoader <K, CacheValue<V>> getCacheLoader(){

    if(this.memory){
       return new CacheLoader<K, CacheValue<V>>() {
        @Override
        public CacheValue<V> load(K key) throws Exception {
          V value = loader.get(key);
          memoryFootPrint += keyMapping.byteSize(key);
          memoryFootPrint += valueMapping.byteSize(value);
          return new CacheValue<>(value);
        }
       };
    }
    else{
      return new CacheLoader<K, CacheValue<V>>() {
        @Override
        public CacheValue<V> load(K key) throws Exception {
          V value = loader.get(key);
          return new CacheValue<>(value);
        }
      };
    }

  }

  private RemovalListener <K, CacheValue<V>> getRemoveListener(){
    if(this.memory && remover != null){
        return new RemovalListener<K, CacheValue<V>>() {
          @Override
          public void onRemoval(RemovalNotification<K, CacheValue<V>> notification) {
            memoryFootPrint -= keyMapping.byteSize(notification.getKey());
            memoryFootPrint -= valueMapping.byteSize(notification.getValue().getValue());
            remover.remove(notification.getKey(), notification.getValue());
          }
        };
      }
    else if(this.memory){
      return new RemovalListener<K, CacheValue<V>>() {
        @Override
        public void onRemoval(RemovalNotification<K, CacheValue<V>> notification) {
          memoryFootPrint -= keyMapping.byteSize(notification.getKey());
          memoryFootPrint -= valueMapping.byteSize(notification.getValue().getValue());
        }
      };
    }
    else if(this.remover != null){
      return new RemovalListener<K, CacheValue<V>>() {
        @Override
        public void onRemoval(RemovalNotification<K, CacheValue<V>> notification) {
          remover.remove(notification.getKey(), notification.getValue());
        }
      };
    }
    return null;
  }
}
