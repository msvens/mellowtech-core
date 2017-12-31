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

package org.mellowtech.core.collections;

import org.mellowtech.core.codec.BCodec;
import org.mellowtech.core.collections.impl.EHBlobTableImp;
import org.mellowtech.core.collections.impl.EHTableImp;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author msvens
 *
 */
public class EHTableBuilder<A,B> extends CollectionBuilder<A,B,EHTableBuilder<A,B>>{

  public static final int DEFAULT_MAX_BUCKETS = 1024 * 1024;
  public static final int DEFAULT_BUCKET_SIZE = 1024 * 8;


  private int maxBuckets = DEFAULT_MAX_BUCKETS;
  private int bucketSize = DEFAULT_BUCKET_SIZE;
  private boolean inMemory = false;
  private boolean forceNew = false;
  private boolean blobValues = false;


  public EHTableBuilder<A,B> maxBuckets(int max) {
    this.maxBuckets = max;
    return this;
  }

  public EHTableBuilder<A,B> bucketSize(int size) {
    this.bucketSize = size;
    return this;
  }

  public EHTableBuilder<A,B> blobValues(boolean blobs) {
    this.blobValues = blobs;
    return this;
  }

  public EHTableBuilder<A,B> inMemory(boolean inMemory){
    this.inMemory = inMemory;
    return this;
  }
  public EHTableBuilder<A,B> forceNew(boolean force){
    this.forceNew = force;
    return this;
  }

  public final BMap <A,B> build() throws Exception{
    checkParameters();

    if(blobValues) return buildBlob();
    EHTableImp<A,B> toRet = null;
    /*try {
      toRet = new EHTableImp <> (filePath, keyCodec, valueCodec, inMemory);
    } catch (Exception e){
      return new EHTableImp <> (filePath, keyCodec, valueCodec, inMemory, bucketSize, maxBuckets);
    }*/
    toRet = new EHTableImp <> (filePath, keyCodec, valueCodec, inMemory, bucketSize, maxBuckets);
    if(!forceNew || toRet.size() == 0) return toRet;

    //delete old and create new:
    toRet.delete();
    return new EHTableImp <> (filePath, keyCodec, valueCodec, inMemory, bucketSize, maxBuckets);
  }

  private final BMap <A,B> buildBlob() throws Exception{
    BMap <A,B> toRet = new EHBlobTableImp <> (filePath, keyCodec, valueCodec, inMemory, bucketSize, maxBuckets);;
    //first try to open
    /*try {
      toRet = new EHBlobTableImp<>(filePath, keyCodec, valueCodec, inMemory);
    } catch (Exception e){
      return new EHBlobTableImp <> (filePath, keyCodec, valueCodec, inMemory, bucketSize, maxBuckets);
    }*/
    if(!forceNew || toRet.size() == 0) return toRet;

    //delete old and create new:
    toRet.delete();
    return new EHBlobTableImp <> (filePath, keyCodec, valueCodec, inMemory, bucketSize, maxBuckets);
  }


}
