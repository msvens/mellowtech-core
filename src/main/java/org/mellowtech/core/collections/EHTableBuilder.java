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
public class EHTableBuilder {

  public static final int DEFAULT_MAX_BUCKETS = 1024 * 1024;
  public static final int DEFAULT_BUCKET_SIZE = 1024 * 8;


  private int maxBuckets = DEFAULT_MAX_BUCKETS;
  private int bucketSize = DEFAULT_BUCKET_SIZE;
  private boolean inMemory = false;
  private boolean forceNew = false;
  private boolean blobValues = false;

  public EHTableBuilder maxBuckets(int max) {
    this.maxBuckets = max;
    return this;
  }

  public EHTableBuilder bucketSize(int size) {
    this.bucketSize = size;
    return this;
  }

  public EHTableBuilder blobValues(boolean blobs) {
    this.blobValues = blobs;
    return this;
  }

  public EHTableBuilder inMemory(boolean inMemory){
    this.inMemory = inMemory;
    return this;
  }
  public EHTableBuilder forceNew(boolean force){
    this.forceNew = force;
    return this;
  }

  public final <A,B> BMap <A,B>
  build(BCodec<A> keyCodec, BCodec<B> valueCodec, String fileName) throws Exception {
    return build(keyCodec, valueCodec, Paths.get(fileName));
  }

  public final <A,B> BMap <A,B>
  build(BCodec<A> keyCodec, BCodec<B> valueCodec, Path fileName) throws Exception{
    if(blobValues) return buildBlob(keyCodec, valueCodec, fileName);
    EHTableImp<A,B> toRet = null;
    //first try to open
    try {
      toRet = new EHTableImp <> (fileName, keyCodec, valueCodec, inMemory);
    } catch (Exception e){
      return new EHTableImp <> (fileName, keyCodec, valueCodec, inMemory, bucketSize, maxBuckets);
    }
    if(!forceNew) return toRet;

    //delete old and create new:
    toRet.delete();
    return new EHTableImp <> (fileName, keyCodec, valueCodec, inMemory, bucketSize, maxBuckets);
  }

  private final <A,B> BMap <A,B>
  buildBlob(BCodec<A> keyCodec, BCodec<B> valueCodec, Path fileName) throws Exception{
    BMap <A,B> toRet = null;
    //first try to open
    try {
      toRet = new EHBlobTableImp<>(fileName, keyCodec, valueCodec, inMemory);
    } catch (Exception e){
      return new EHBlobTableImp <> (fileName, keyCodec, valueCodec, inMemory, bucketSize, maxBuckets);
    }
    if(!forceNew) return toRet;

    //delete old and create new:
    toRet.delete();
    return new EHBlobTableImp <> (fileName, keyCodec, valueCodec, inMemory, bucketSize, maxBuckets);
  }


}
