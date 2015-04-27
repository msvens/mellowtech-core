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
