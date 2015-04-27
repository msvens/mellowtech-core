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
public class CacheLRUMemory <A,B extends BStorable <A,B>,C,D extends BStorable <C,D>> extends CacheLRU <B,D> {

   private long memoryFootPrint;
   //private BSMapping <?> keyMapping = null;
   //private BSMapping <?> valueMapping = null;


  public CacheLRUMemory(Remover<B, D> remover, Loader<B, D> loader, long size,
                        boolean countMemoryFootPrint) {
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
