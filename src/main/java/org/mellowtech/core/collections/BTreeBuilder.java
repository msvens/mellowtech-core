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

import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.collections.impl.BPBlobTreeImp;
import org.mellowtech.core.collections.impl.BPTreeImp;
import org.mellowtech.core.collections.impl.MemMappedBPBlobTreeImp;
import org.mellowtech.core.collections.impl.MemMappedBPTreeImp;

/**
 * @author msvens
 *
 */
public class BTreeBuilder {
  
  public static final int DEFAULT_INDEX_BLOCK = 1024 * 8;
  public static final int DEFAULT_VALUE_BLOCK = 1024 * 8;
  public static final int DEFAULT_MAX_BLOCKS = 1024*1024;
  public static final int DEFAULT_MAX_INDEX_BLOCKS = 1024*10;
  
  
  private int indexBlockSize = DEFAULT_INDEX_BLOCK;
  private int valueBlockSize = DEFAULT_VALUE_BLOCK;
  private boolean indexInMemory = true;
  private boolean valuesInMemory = false;
  private int maxBlocks = DEFAULT_MAX_BLOCKS;
  private int maxIndexBlocks = DEFAULT_MAX_INDEX_BLOCKS;
  private boolean forceNew = false;
  private boolean blobValues = false;
  
  public BTreeBuilder indexBlockSize(int size) {
    this.indexBlockSize = size;
    return this;
  }
  
  public BTreeBuilder valueBlockSize(int size) {
    this.valueBlockSize = size;
    return this;
  }
  
  public BTreeBuilder blobValues(boolean blobs) {
    this.blobValues = blobs;
    return this;
  }
  
  public BTreeBuilder indexInMemory(boolean inMemory){
    this.indexInMemory = inMemory;
    return this;
  }
  
  public BTreeBuilder valuesInMemory(boolean inMemory){
    this.valuesInMemory = inMemory;
    return this;
  }
  
  public BTreeBuilder maxBlocks(int max) {
    this.maxBlocks = max;
    return this;
  }
  
  public BTreeBuilder maxIndexBlocks(int max){
    this.maxIndexBlocks = max;
    return this;
  }
  
  public BTreeBuilder forceNew(boolean force){
    this.forceNew = force;
    return this;
  }

  public <A,B extends BComparable<A,B>, C, D extends BStorable<C,D>> BTree <A,B,C,D> 
    build(Class<B> keyType, Class<D> valueType, String fileName) throws Exception{
    if(blobValues) return buildBlob(keyType, valueType, fileName);
    BTree <A,B,C,D> toRet = null;
    //first try to open
    try {
      toRet = indexInMemory ? new MemMappedBPTreeImp<>(fileName, keyType, valueType, valuesInMemory) :
        new BPTreeImp<>(fileName, keyType, valueType);
    } catch (Exception e){
      if(indexInMemory){
        return new MemMappedBPTreeImp <> (fileName, keyType, valueType, indexBlockSize, valueBlockSize,
            valuesInMemory, maxBlocks, maxIndexBlocks);
      } else {
        return new BPTreeImp <> (fileName, keyType, valueType, valueBlockSize, indexBlockSize, maxBlocks, maxIndexBlocks);
      }
    }
    if(!forceNew) return toRet;
    
    //delete old and create new:
    toRet.delete();
    if(indexInMemory){
      return new MemMappedBPTreeImp <> (fileName, keyType, valueType, indexBlockSize, valueBlockSize,
          valuesInMemory, maxBlocks, maxIndexBlocks);
    } else {
      return new BPTreeImp <> (fileName, keyType, valueType, valueBlockSize, indexBlockSize, maxBlocks, maxIndexBlocks);
    }
  }
  
  public <A,B extends BComparable<A,B>,C,D extends BStorable<C,D>> BTree <A,B,C,D> 
    buildBlob(Class<B> keyType, Class<D> valueType, String fileName) throws Exception{
    BTree<A,B,C,D> toRet = null;
    //first try to open
    try {
      toRet = indexInMemory ? new MemMappedBPBlobTreeImp<>(fileName, keyType, valueType, valuesInMemory) :
        new BPBlobTreeImp<>(fileName, keyType, valueType);
    } catch (Exception e){
      if(indexInMemory){
        return new MemMappedBPBlobTreeImp <> (fileName, keyType, valueType, indexBlockSize, valueBlockSize,
            valuesInMemory, maxBlocks, maxIndexBlocks);
      } else {
        return new BPBlobTreeImp <> (fileName, keyType, valueType, valueBlockSize, indexBlockSize, maxBlocks, maxIndexBlocks);
      }
    }
    if(!forceNew) return toRet;
    
    //delete old and create new:
    toRet.delete();
    if(indexInMemory){
      return new MemMappedBPBlobTreeImp <> (fileName, keyType, valueType, indexBlockSize, valueBlockSize,
          valuesInMemory, maxBlocks, maxIndexBlocks);
    } else {
      return new BPBlobTreeImp <> (fileName, keyType, valueType, valueBlockSize, indexBlockSize, maxBlocks, maxIndexBlocks);
    }
  }
  
  
}
