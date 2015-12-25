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
 * Date: 2012-11-02
 * Time: 10:45
 *
 * @author Martin Svensson
 */
public class CacheLRUMemoryMapping <A,B extends BStorable <A,B>,C,D extends BStorable <C,D>> extends CacheLRU <A,C> {
  
   private long memoryFootPrint;
   private final B keyMapping;
   private final D valueMapping;


  public CacheLRUMemoryMapping(Remover<A, C> remover, Loader<A, C> loader, long size,
                               boolean countMemoryFootPrint, Class <B> keyType,
                               Class <D> valueType) throws Exception{
    this.keyMapping = keyType.newInstance();
    this.valueMapping = valueType.newInstance();
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
  protected LoadingCache<A, CacheValue<C>> buildCache() {

    Weigher<A, CacheValue <C>> weighter = new Weigher<A, CacheValue<C>> () {
      @Override
      public int weigh(A key, CacheValue <C> value) {
        return keyMapping.create(key).byteSize() + valueMapping.create(value.getValue()).byteSize();
      }
    };

    RemovalListener<A, CacheValue <C>> removalListener = this.getRemoveListener();
    CacheLoader<A, CacheValue<C>> cacheLoader = this.getCacheLoader();

    if(removalListener != null){
      return CacheBuilder.newBuilder().maximumWeight(maxSize()).
              weigher(weighter).removalListener(removalListener).build(cacheLoader);
    }
    else
      return CacheBuilder.newBuilder().maximumWeight(maxSize()).weigher(weighter).build(cacheLoader);
  }

  private CacheLoader <A, CacheValue<C>> getCacheLoader(){

    if(this.memory){
       return new CacheLoader<A, CacheValue<C>>() {
        @Override
        public CacheValue<C> load(A key) throws Exception {
          C value = loader.get(key);
          memoryFootPrint += keyMapping.create(key).byteSize();
          memoryFootPrint += valueMapping.create(value).byteSize();
          return new CacheValue<>(value);
        }
       };
    }
    else{
      return new CacheLoader<A, CacheValue<C>>() {
        @Override
        public CacheValue<C> load(A key) throws Exception {
          C value = loader.get(key);
          return new CacheValue<>(value);
        }
      };
    }

  }

  private RemovalListener <A, CacheValue<C>> getRemoveListener(){
    if(this.memory && remover != null){
        return new RemovalListener<A, CacheValue<C>>() {
          @Override
          public void onRemoval(RemovalNotification<A, CacheValue<C>> notification) {
            memoryFootPrint -= keyMapping.create(notification.getKey()).byteSize();
            memoryFootPrint -= valueMapping.create(notification.getValue().getValue()).byteSize();
            remover.remove(notification.getKey(), notification.getValue());
          }
        };
      }
    else if(this.memory){
      return new RemovalListener<A, CacheValue<C>>() {
        @Override
        public void onRemoval(RemovalNotification<A, CacheValue<C>> notification) {
          memoryFootPrint -= keyMapping.create(notification.getKey()).byteSize();
          memoryFootPrint -= valueMapping.create(notification.getValue().getValue()).byteSize();
        }
      };
    }
    else if(this.remover != null){
      return new RemovalListener<A, CacheValue<C>>() {
        @Override
        public void onRemoval(RemovalNotification<A, CacheValue<C>> notification) {
          remover.remove(notification.getKey(), notification.getValue());
        }
      };
    }
    return null;
  }
}
