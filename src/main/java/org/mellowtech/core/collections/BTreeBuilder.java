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
import org.mellowtech.core.collections.impl.*;
import org.mellowtech.core.io.RecordFileBuilder;
import org.mellowtech.core.io.impl.MultiBlockFile;

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
 *   <li>no blob values</li>
 * </ul>
 * The default configuration can roughly hold key-values equaling
 * 1024*1024*1024*8 bytes, or 4gb worth of data
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 * @param <A> key type
 * @param <B> value type
 */
public class BTreeBuilder<A,B> extends CollectionBuilder<A,B,BTreeBuilder<A,B>>{
  
  public static final int DEFAULT_INDEX_BLOCK = 1024 * 8;
  public static final int DEFAULT_VALUE_BLOCK = 1024 * 8;
  public static final int DEFAULT_MAX_BLOCKS = 1024*1024;
  public static final int DEFAULT_MAX_INDEX_BLOCKS = 1024*10;
  
  
  private int indexBlockSize = DEFAULT_INDEX_BLOCK;
  private int valueBlockSize = DEFAULT_VALUE_BLOCK;
  private boolean memoryIndex = false;
  private boolean memoryMappedValues = true;
  private int maxBlocks = DEFAULT_MAX_BLOCKS;
  private int maxIndexBlocks = DEFAULT_MAX_INDEX_BLOCKS;
  private boolean blobValues = false;
  private boolean multiFileValues = false;
  private int multiFileSize = 1024*1024*64;


  /**
   * Based on usage hints calculates block sizes and maximum blocks as well as
   * if this tree should store blobs
   * @param maxKeyValues maximum number of keys this tree will hold
   * @param maxKeySize maximum size of a key
   * @param maxValueSize maximum size of a value
   * @param avgKeySize average size of a key
   * @param avgValueSize average size of a value
   * @return this builder
   */
  public BTreeBuilder<A,B> hint(long maxKeyValues, int maxKeySize, int maxValueSize, int avgKeySize, int avgValueSize){
    long totalBytes = maxKeyValues * avgKeySize * avgValueSize;
    return this;
  }

  /**
   * If key/values should be stored in a multiFile recordFile.
   * @param multiFile true if multiFile
   * @return this
   * @see MultiBlockFile
   */
  public BTreeBuilder<A,B> multiFileValues(boolean multiFile){
    this.multiFileValues = multiFile;
    return this;
  }

  /**
   * Size (in bytes) of file parts for a multiFile recordFile. Only used if fileValues are
   * stored in a multiFile
   * @param size size in bytes
   * @return this
   */
  public BTreeBuilder<A,B> multiFileSize(int size){
    this.multiFileSize = size;
    return this;
  }

  /**
   * Indicate if the index should only be kept in-memory and recreated on startup. This is the
   * fastest tree type but has an additional startup cost and unpredictable memory usage. However,
   * memory should not be a major problem as the index does not tend to get very large
   * @param memIndex indicate memoryIndex
   * @return this
   */
  public BTreeBuilder<A,B> memoryIndex(boolean memIndex){
    this.memoryIndex = memIndex;
    return this;
  }

  /**
   * Size of the blocks that store the index part of this tree. Only used with no-memory based indices
   * @param size size in bytes
   * @return this
   */
  public BTreeBuilder<A,B> indexBlockSize(int size) {
    this.indexBlockSize = size;
    return this;
  }

  /**
   * Size of the blocks that hold key/value pairs
   * @param size size in bytes
   * @return this
   */
  public BTreeBuilder<A,B> valueBlockSize(int size) {
    this.valueBlockSize = size;
    return this;
  }

  /**
   * If this tree sore large values, typically 256+ bytes or so. Actual values will be stored in a separate file
   * @param blobs true if large values
   * @return this
   */
  public BTreeBuilder<A,B> blobValues(boolean blobs) {
    this.blobValues = blobs;
    return this;
  }


  /**
   * If this tree will memory map key/value blocks. If the tree is set to use a multiFile valuefile this
   * parameter is ignored as the multiFile is memoryMapped.
   * @param inMemory true if key/values should be memory mapped
   * @return this
   */
  public BTreeBuilder<A,B> memoryMappedValues(boolean inMemory){
    this.memoryMappedValues = inMemory;
    return this;
  }

  /**
   * Max number of key/value blocks this tree can hold. This will effectively determine the max number of
   * bytes of data stored in this tree (maxBlocks * valueBlockSize). For a multiFile tree this value is
   * ignored
   * @param max max number of key/value blocks
   * @return this
   */
  public BTreeBuilder<A,B> maxBlocks(int max) {
    this.maxBlocks = max;
    return this;
  }

  /**
   * Max number of index blocks of this tree. This value will determine the max number of leaves this tree
   * can hold. Index blocks only store key/value block separators so this value does not have to be that large.
   * For a tree with its index only in memory this value is ignored.
   * @param max max number of index blocks
   * @return this
   */
  public BTreeBuilder<A,B> maxIndexBlocks(int max){
    this.maxIndexBlocks = max;
    return this;
  }

  /**
   * Build (create or open) a disc based tree
   * @return a new disc based tree
   * @throws Exception if tree could not be created/opened
   */
  public BTree build() throws Exception{
    checkParameters();

    RecordFileBuilder vfb = new RecordFileBuilder();
    vfb.maxBlocks(maxBlocks).blockSize(valueBlockSize).multiFileSize(multiFileSize);
    if(multiFileValues)
      vfb.multi();
    else if(memoryMappedValues)
      vfb.mem();
    else
      vfb.disc();

    DirAndName dn = filePathSplit();

    if(memoryIndex){
      return blobValues ? new HybridBlobTree<>(dn.dir,dn.name,keyCodec,valueCodec,vfb) :
          new HybridTree<>(dn.dir,dn.name,keyCodec,valueCodec,vfb);
    } else {
      return blobValues ? new BTreeBlobImp<>(dn.dir,dn.name,keyCodec,valueCodec,indexBlockSize,maxIndexBlocks,vfb) :
          new BTreeImp<>(dn.dir,dn.name,keyCodec,valueCodec,indexBlockSize,maxIndexBlocks,vfb);
    }
  }
  
}
