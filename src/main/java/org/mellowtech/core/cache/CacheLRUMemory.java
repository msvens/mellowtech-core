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


import org.mellowtech.core.bytestorable.BStorable;

import com.google.common.cache.*;

/**
 * Simple cache that use the least recently used scheme. When the cache limit is
 * reached and an item has to be unloaded the cache chooses the item that was
 * accessed longest ago. put, get, and remove all work in log(n) time.
 * <p>
 * Cache size is based on memory footprint rather than number of items
 * </p>
 *
 * @param <A> wrapped key type
 * @param <B> bstorable key type
 * @param <C> wrapped value type
 * @param <D> bstorable value type
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 */
public class CacheLRUMemory <A,B extends BStorable <A,B>,C,D extends BStorable <C,D>> extends CacheLRU <B,D> {

   private long memoryFootPrint;


  /**
   * Create a new cache with a remover and loader
   * @param remover remover object or null
   * @param loader loader object
   * @param size maximum bytes for cache
   * @param countMemoryFootPrint if true count the current memory foot print
   */
  public CacheLRUMemory(Remover<B, D> remover, Loader<B, D> loader, long size,
                        boolean countMemoryFootPrint) {
    setRemover(remover);
    setSize(size);
    setLoader(loader);
    this.memory = countMemoryFootPrint;
    lru = this.buildCache();
  }

  /**
   * Get the current memory footprint
   * @return footprint in bytes
   */
  public long getMemoryFootPrint(){
    return this.memoryFootPrint;
  }

  @Override
  protected LoadingCache<B, CacheValue<D>> buildCache() {

    Weigher<B, CacheValue <D>> weighter = new Weigher<B, CacheValue<D>> () {
      @Override
      public int weigh(B key, CacheValue <D> value) {
        return key.byteSize() + value.getValue().byteSize();
      }
    };

    RemovalListener<B, CacheValue <D>> removalListener = this.getRemoveListener();
    CacheLoader<B, CacheValue<D>> cacheLoader = this.getCacheLoader();

    if(removalListener != null){
      return CacheBuilder.newBuilder().maximumWeight(maxSize()).
              weigher(weighter).removalListener(removalListener).build(cacheLoader);
    }
    else
      return CacheBuilder.newBuilder().maximumWeight(maxSize()).weigher(weighter).build(cacheLoader);
  }

  private CacheLoader <B, CacheValue<D>> getCacheLoader(){

    if(this.memory){
       return new CacheLoader<B, CacheValue<D>>() {
        @Override
        public CacheValue<D> load(B key) throws Exception {
          D value = loader.get(key);
          memoryFootPrint += key.byteSize();
          memoryFootPrint += value.byteSize();
          return new CacheValue<D>(value);
        }
       };
    }
    else{
      return new CacheLoader<B, CacheValue<D>>() {
        @Override
        public CacheValue<D> load(B key) throws Exception {
          D value = loader.get(key);
          return new CacheValue<D>(value);
        }
      };
    }

  }

  private RemovalListener <B, CacheValue<D>> getRemoveListener(){
    if(this.memory && remover != null){
        return new RemovalListener<B, CacheValue<D>>() {
          @Override
          public void onRemoval(RemovalNotification<B, CacheValue<D>> notification) {
            memoryFootPrint -= notification.getKey().byteSize();
            memoryFootPrint -= notification.getValue().getValue().byteSize();
            remover.remove(notification.getKey(), notification.getValue());
          }
        };
      }
    else if(this.memory){
      return new RemovalListener<B, CacheValue<D>>() {
        @Override
        public void onRemoval(RemovalNotification<B, CacheValue<D>> notification) {
          memoryFootPrint -= notification.getKey().byteSize();
          memoryFootPrint -= notification.getValue().getValue().byteSize();
        }
      };
    }
    else if(this.remover != null){
      return new RemovalListener<B, CacheValue<D>>() {
        @Override
        public void onRemoval(RemovalNotification<B, CacheValue<D>> notification) {
          remover.remove(notification.getKey(), notification.getValue());
        }
      };
    }
    return null;
  }
}
