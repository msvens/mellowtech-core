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
import org.mellowtech.core.collections.impl.*;

import java.nio.file.Path;
import java.nio.file.Paths;

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
  private boolean oneFileTree = true;
  
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

  public BTreeBuilder oneFileTree(boolean oneFile){
    this.oneFileTree = oneFile;
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

  private boolean isOneFileTree(){
    return oneFileTree && indexInMemory;
  }

  public <A,B extends BComparable<A,B>, C, D extends BStorable<C,D>> BTree <A,B,C,D>
  build(Class<B> keyType, Class<D> valueType, String fileName) throws Exception{
    Path p = Paths.get(fileName);
    Path dir = p.getParent();
    return build(keyType, valueType,dir,p.getFileName().toString());

  }

  public <A,B extends BComparable<A,B>, C, D extends BStorable<C,D>> BTree <A,B,C,D>
  build(Class<B> keyType, Class<D> valueType, Path dir, String name) throws Exception{
    if(blobValues) return buildBlob(keyType, valueType, dir, name);
    BTree <A,B,C,D> toRet = null;

    try {
      //first try to open
      toRet = new BTreeImp<>(dir,name,keyType,valueType,isOneFileTree(),indexInMemory,valuesInMemory);
    } catch (Exception e){
      //try create
      return new BTreeImp<>(dir,name,keyType,valueType,indexBlockSize,valueBlockSize,
          maxIndexBlocks,maxBlocks,isOneFileTree(),indexInMemory,valuesInMemory,true);
    }
    if(!forceNew) return toRet;

    //delete old and create new:
    toRet.delete();
    return new BTreeImp<>(dir,name,keyType,valueType,indexBlockSize,valueBlockSize,
        maxIndexBlocks,maxBlocks,isOneFileTree(),indexInMemory,valuesInMemory,true);
  }

  public <A,B extends BComparable<A,B>,C,D extends BStorable<C,D>> BTree <A,B,C,D>
  buildBlob(Class<B> keyType, Class<D> valueType, Path dir, String name) throws Exception{
    BTree<A,B,C,D> toRet = null;
    //first try to open
    try {
      toRet = new BTreeBlobImp<>(dir, name, keyType, valueType,isOneFileTree(),indexInMemory,valuesInMemory);
    } catch (Exception e){
      //try create
      return new BTreeBlobImp<>(dir,name, keyType, valueType,indexBlockSize,valueBlockSize,
          maxIndexBlocks,maxBlocks,isOneFileTree(),indexInMemory,valuesInMemory);
    }

    if(!forceNew) return toRet;
    //delete old and create new:
    toRet.delete();
    return new BTreeBlobImp<>(dir,name, keyType, valueType,indexBlockSize,valueBlockSize,
        maxIndexBlocks,maxBlocks,isOneFileTree(),indexInMemory,valuesInMemory);
  }

  @Deprecated
  public <A,B extends BComparable<A,B>, C, D extends BStorable<C,D>> BTree <A,B,C,D> 
    buildOld(Class<B> keyType, Class<D> valueType, String fileName) throws Exception{
    if(blobValues) return buildBlobOld(keyType, valueType, fileName);
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

  @Deprecated
  public <A,B extends BComparable<A,B>,C,D extends BStorable<C,D>> BTree <A,B,C,D> 
    buildBlobOld(Class<B> keyType, Class<D> valueType, String fileName) throws Exception{
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
