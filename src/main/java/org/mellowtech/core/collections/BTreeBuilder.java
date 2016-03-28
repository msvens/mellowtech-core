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
 * A Builder for sorted disc based maps. There are basically 4 different parameters to consider
 * <ul>
 *   <li>Block Sizes - both for index blocks and key-value blocks</li>
 *   <li>Max Blocks - the maximum number of index and value blocks</li>
 *   <li>Blob values - if this map will hold very large values</li>
 *   <li>Memory mapping - if the index and key-values should be memory mapped</li>
 * </ul>
 * <p>
 * Out of these the value-block size is most important to consider. As a rule of thumb you need
 * to be able to store at minimum 20 key-value pairs in a block and it does not make
 * a lot of sense to have blocks larger than 64k. Thus if you have values that are very large
 * you should opt for a blob based tree.
 * </p>
 * <p>The Blocks size and max blocks will together put a hard limit on how many keys this
 * map can store</p>
 * <pre>
 * {@code
 * BTreeBuilder myBuilder = new BTreeBuilder();
 * BTree <String,CBString,String,CBString> tree =
 *   myBuilder.build(CBString.class, CBString.class, somePath, "someName")
 * }
 * </pre>
 * Will create a new tree with the default configuration of:
 * <ul>
 *   <li>8k index and value blocks</li>
 *   <li>1024*10 index blocks</li>
 *   <li>1024*1024 value blocks</li>
 *   <li>Both index and values memory mapped</li>
 *   <li>Index and values stored in separate files</li>
 *   <li>no blob values</li>
 * </ul>
 * The default configuration can roughly hold key-values equaling
 * 1024*1024*1024*8 bytes, or 8gb worth of data
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 */
public class BTreeBuilder {
  
  public static final int DEFAULT_INDEX_BLOCK = 1024 * 8;
  public static final int DEFAULT_VALUE_BLOCK = 1024 * 8;
  public static final int DEFAULT_MAX_BLOCKS = 1024*1024;
  public static final int DEFAULT_MAX_INDEX_BLOCKS = 1024*10;
  
  
  private int indexBlockSize = DEFAULT_INDEX_BLOCK;
  private int valueBlockSize = DEFAULT_VALUE_BLOCK;
  private boolean indexInMemory = true;
  private boolean valuesInMemory = true;
  private int maxBlocks = DEFAULT_MAX_BLOCKS;
  private int maxIndexBlocks = DEFAULT_MAX_INDEX_BLOCKS;
  private boolean forceNew = false;
  private boolean blobValues = false;
  private boolean oneFileTree = false;

  /**
   * Based on usage hints calculates block sizes and maximum blocks as well as
   * if this tree should store blobs
   * @param maxKeys maximum number of keys this tree will hold
   * @param maxKeySize maximum size of a key
   * @param maxValueSize maximum size of a value
   * @param avgKeySize average size of a key
   * @param avgValueSize average size of a value
   * @return this builder
   */
  public BTreeBuilder hint(long maxKeys, int maxKeySize, int maxValueSize, int avgKeySize, int avgValueSize){
    long totalBytes = maxKeys * avgKeySize * avgValueSize;

    return this;
  }
  
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

  public BTreeBuilder oneFile(boolean oneFile){
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

  @Deprecated
  public <A,B extends BComparable<A,B>, C, D extends BStorable<C,D>> BTree <A,B,C,D>
  build(Class<B> keyType, Class<D> valueType, String fileName) throws Exception{
    Path p = Paths.get(fileName);
    Path dir = p.getParent();
    return build(keyType, valueType,dir,p.getFileName().toString());

  }

  public <A,B extends BComparable<A,B>, C, D extends BStorable<C,D>> BTree <A,B,C,D>
  build(Class<B> keyType, Class<D> valueType, Path dir, String name) throws Exception{
    if(blobValues) return buildBlob(keyType, valueType, dir, name);

    BTree <A,B,C,D> toRet;

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

  private <A,B extends BComparable<A,B>,C,D extends BStorable<C,D>> BTree <A,B,C,D>
  buildBlob(Class<B> keyType, Class<D> valueType, Path dir, String name) throws Exception{
    BTree<A,B,C,D> toRet;
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
  
}
