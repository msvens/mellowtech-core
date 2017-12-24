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
import org.mellowtech.core.codec.Codecs;
import org.mellowtech.core.collections.impl.DiscBasedHashMap;
import org.mellowtech.core.collections.impl.DiscBasedMap;

import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by msvens on 22/10/15.
 */
public class DiscMapBuilder<A,B>  extends CollectionBuilder<A,B, DiscMapBuilder<A,B>>{

  public static int DEFAULT_KEY_BLOCK_SIZE = 1024 * 8;
  public static int DEFAULT_VALUE_BLOCK_SIZE = 1024 * 8;
  public static final int DEFAULT_BUCKET_SIZE = 1024*8;
  public static final int DEFAULT_MAX_BUCKETS = 1024*1024*2;

  private int keyBlockSize = DEFAULT_KEY_BLOCK_SIZE;
  private int valueBlockSize = DEFAULT_VALUE_BLOCK_SIZE;
  private int bucketSize = DEFAULT_BUCKET_SIZE;
  private int maxBuckets = DEFAULT_MAX_BUCKETS;

  //private boolean memMappedKeyBlocks = true;
  private boolean memMappedValueBlocks = false;
  private boolean blobValues = false;
  private boolean sorted = false;

  private Optional<Integer> maxKeySize = Optional.empty();
  private Optional<Integer> maxValueSize = Optional.empty();


  public DiscMapBuilder<A,B> bucketSize(int size){
    this.bucketSize = bucketSize;
    return this;
  }

  public DiscMapBuilder<A,B> maxBuckets(int max){
    this.maxBuckets = max;
    return this;
  }

  public DiscMapBuilder<A,B> sorted(boolean sorted){
    this.sorted = sorted;
    return this;
  }

  public DiscMapBuilder<A,B> keyBlockSize(int size) {
    this.keyBlockSize = size;
    return this;
  }

  public DiscMapBuilder<A,B> blobValues(boolean blobs){
    this.blobValues = blobs;
    return this;
  }

  public DiscMapBuilder<A,B> valueBlockSize(int size) {
    this.valueBlockSize = size;
    return this;
  }

  public DiscMapBuilder<A,B> memMappedValueBlocks(boolean memMapped) {
    this.memMappedValueBlocks = memMapped;
    return this;
  }

  public DiscMapBuilder<A,B> maxKeySize(int size) {
    this.maxKeySize = Optional.of(size);
    return this;
  }

  public DiscMapBuilder<A,B> maxValueSize(int size) {
    this.maxValueSize = Optional.of(size);
    return this;
  }

  public SortedDiscMap<A,B> treeMap() {
    sorted(true);
    return (SortedDiscMap<A,B>) build();
  }

  public DiscMap<A,B> hashMap() {
    sorted(false);
    return build();
  }

  public DiscMap<A,B> build() {
    try {
      checkParameters();
      if (calcSize(keyCodec, valueCodec) * 10 > valueBlockSize)
        blobValues = true;
      if (sorted) {
        BTreeBuilder<A,B> builder = new BTreeBuilder<>();
        builder.copyBuilder(this);
        builder.valueBlockSize(valueBlockSize).indexBlockSize(keyBlockSize).blobValues(blobValues).memoryMappedValues(memMappedValueBlocks);
        return new DiscBasedMap<>(builder);
      } else {
        EHTableBuilder<A,B> builder = new EHTableBuilder<>();
        builder.copyBuilder(this);
        builder.bucketSize(bucketSize).maxBuckets(maxBuckets).blobValues(blobValues).inMemory(memMappedValueBlocks);
        return new DiscBasedHashMap<>(builder);
      }
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  private int calc(BCodec<?> inst) {
    return inst.isFixed() ? inst.fixedSize() : Integer.MAX_VALUE;
  }

  private int calcSize(BCodec<?> key, BCodec<?> value) {
    int keySize = maxKeySize.isPresent() && !key.isFixed() ? maxKeySize.get() : calc(key);
    int valSize = maxValueSize.isPresent() && !value.isFixed() ? maxValueSize.get() : calc(value);
    return keySize + valSize;
  }


}
