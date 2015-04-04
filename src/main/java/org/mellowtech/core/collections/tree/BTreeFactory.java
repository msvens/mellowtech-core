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

package org.mellowtech.core.collections.tree;

import java.io.IOException;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.ByteComparable;
import org.mellowtech.core.bytestorable.ByteStorable;

/**
 * Date: 2013-03-22
 * Time: 16:03
 *
 * @author Martin Svensson
 */
@Deprecated
public class BTreeFactory {

  public static final int DEFAULT_INDEX_BLOCK = 1024 * 2;
  public static final int DEFAULT_VALUE_BLOCK = 1024 * 4;


  /**
   * A MemMapped Btree uses only one backing file to store
   * the index and key/value blocks. The index is kept in a
   * memory mapped buffer for fast access i.e. no need to do
   * any caching of the index. The BlockSize is set to
   * DEFAULT_INDEX_SIZE AND DEFAULT_VALUE_SIZE
   * @param fName filname (without) extension
   * @param keyType template for creating keys
   * @param valueType template for creating values
   * @return the newly created btree
   * @throws IOException
   */
  public static <K extends ByteComparable, V extends ByteStorable> BTree <K,V> createMemMapped(String fName, K keyType, V valueType) throws IOException {
    return createMemMapped(fName, keyType, valueType, DEFAULT_INDEX_BLOCK, DEFAULT_VALUE_BLOCK,
        false, MemMappedBPTreeImp.DEFAULT_MAX_BLOCKS);
  }

  /**
   * A MemMapped Btree uses only one backing file to store
   * the index and key/value blocks. The index is kept in a
   * memory mapped buffer for fast access i.e. no need to do
   * any caching of the index.
   * @param fName filname (without) extension
   * @param keyType template for creating keys
   * @param valueType template for creating values
   * @param inMemory keep the entire btree in a memory mapped file
   * @param maxBlocks max number of value blocks
   * @return the newly created btree
   * @throws IOException
   */
  public static <K extends ByteComparable, V extends ByteStorable> BTree <K,V> createMemMapped(String fName, K keyType, V valueType,
                                      int indexSize, int valueSize, boolean inMemory, int maxBlocks) throws IOException{
    return new MemMappedBPTreeImp(fName, keyType, valueType, indexSize, valueSize, inMemory, maxBlocks, 1024*10);
  }


  public static <K extends ByteComparable, V extends ByteStorable> BTree<K,V> openMemMapped(String fName, K keyType, V valueType) throws IOException {
    return openMemMapped(fName, keyType, valueType, DEFAULT_INDEX_BLOCK, DEFAULT_VALUE_BLOCK, false,
        MemMappedBPTreeImp.DEFAULT_MAX_BLOCKS);
  }
  
  public static <K extends ByteComparable, V extends ByteStorable> BTree<K,V> openMemMapped(String fName, K keyType, V valueType, boolean inMemory) throws IOException {
    return openMemMapped(fName, keyType, valueType, DEFAULT_INDEX_BLOCK, DEFAULT_VALUE_BLOCK, inMemory,
        MemMappedBPTreeImp.DEFAULT_MAX_BLOCKS);
  }

  public static <K extends ByteComparable, V extends ByteStorable> BTree<K,V>  openMemMapped(String fName, K keyType, V valueType, int indexBlockSize,
                                    int valueBlockSize, boolean inMemory, int maxBlocks) throws IOException{
    //first try to open
    try{
      return new MemMappedBPTreeImp(fName, keyType, valueType, inMemory);
    }
    catch(Exception e){CoreLog.L().log(Level.FINER, "", e);}

    return createMemMapped(fName, keyType, valueType, indexBlockSize, valueBlockSize, inMemory, maxBlocks);
  }

  public static <K extends ByteComparable, V extends ByteStorable> BTree<K,V> createMemMappedBlob(String fName, K keyType, V valueType) throws IOException {
    return createMemMappedBlob(fName, keyType, valueType, DEFAULT_INDEX_BLOCK, DEFAULT_VALUE_BLOCK,
        false, MemMappedBPTreeImp.DEFAULT_MAX_BLOCKS);
  }

  public static <K extends ByteComparable, V extends ByteStorable> BTree<K,V> createMemMappedBlob(String fName, K keyType, V valueType,
                                          int indexSize, int valueSize, boolean inMemory, int maxBlocks) throws IOException{
    return new MemMappedBPBlobTreeImp(fName, keyType, valueType, indexSize, valueSize, inMemory, maxBlocks, 1024*10);
  }

  public static <K extends ByteComparable, V extends ByteStorable> BTree<K,V> openMemMappedBlob(String fName, K keyType, V valueType) throws IOException {
    return openMemMappedBlob(fName, keyType, valueType, DEFAULT_INDEX_BLOCK, DEFAULT_VALUE_BLOCK, 
        false, MemMappedBPTreeImp.DEFAULT_MAX_BLOCKS);
  }
  
  public static <K extends ByteComparable, V extends ByteStorable> BTree<K,V> openMemMappedBlob(String fName, K keyType, V valueType, boolean inMemory) throws IOException {
    return openMemMappedBlob(fName, keyType, valueType, DEFAULT_INDEX_BLOCK, DEFAULT_VALUE_BLOCK, 
        inMemory, MemMappedBPTreeImp.DEFAULT_MAX_BLOCKS);
  }

  public static <K extends ByteComparable, V extends ByteStorable> BTree<K,V> openMemMappedBlob(String fName, K keyType, V valueType, int indexBlockSize,
                                        int valueBlockSize, boolean inMemory, int maxBlocks) throws IOException{
    //first try to open
    try{
      return new MemMappedBPBlobTreeImp(fName, keyType, valueType, inMemory);
    }
    catch(Exception e){CoreLog.L().log(Level.FINER, "", e);}

    return createMemMappedBlob(fName, keyType, valueType, indexBlockSize, valueBlockSize, inMemory, maxBlocks);
  }

}
