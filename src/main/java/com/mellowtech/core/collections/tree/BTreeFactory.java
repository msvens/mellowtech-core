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

package com.mellowtech.core.collections.tree;

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteComparable;
import com.mellowtech.core.bytestorable.ByteStorable;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Date: 2013-03-22
 * Time: 16:03
 *
 * @author Martin Svensson
 */
public class BTreeFactory {

  public static final int DEFAULT_INDEX_BLOCK = 1024 * 2;
  public static final int DEFAULT_VALUE_BLOCK = 1024 * 4;

  /**
   * An Optimized Btree uses only one backing file to store
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
  public static BTree createOptimized(String fName, ByteComparable keyType, ByteStorable valueType) throws IOException {
    return createOptimized(fName, keyType, valueType, DEFAULT_INDEX_BLOCK, DEFAULT_VALUE_BLOCK);
  }

  /**
   * An Optimized Btree uses only one backing file to store
   * the index and key/value blocks. The index is kept in a
   * memory mapped buffer for fast access i.e. no need to do
   * any caching of the index.
   * @param fName filname (without) extension
   * @param keyType template for creating keys
   * @param valueType template for creating values
   * @return the newly created btree
   * @throws IOException
   */
  public static BTree createOptimized(String fName, ByteComparable keyType, ByteStorable valueType,
                                      int indexSize, int valueSize) throws IOException{
    return new OptimizedBPTreeImp(fName, keyType, valueType, indexSize, valueSize);
  }

  /**
   * An Optimized Btree uses only one backing file to store
   * the index and key/value blocks. The index is kept in a
   * memory mapped buffer for fast access i.e. no need to do
   * any caching of the index.
   * Opens an existing btree, if that fails creates a new one
   * @param fName filname (without) extension
   * @param keyType template for creating keys
   * @param valueType template for creating values
   * @return the newly created btree
   * @throws IOException
   */
  public static BTree openOptimized(String fName, ByteComparable keyType, ByteStorable valueType) throws IOException {
    return openOptimized(fName, keyType, valueType, DEFAULT_INDEX_BLOCK, DEFAULT_VALUE_BLOCK);
  }

  /**
   * An Optimized Btree uses only one backing file to store
   * the index and key/value blocks. The index is kept in a
   * memory mapped buffer for fast access i.e. no need to do
   * any caching of the index.
   * Opens an existing btree, if that fails creates a new one
   * @param fName filname (without) extension
   * @param keyType template for creating keys
   * @param valueType template for creating values
   * @return the newly created btree
   * @throws IOException
   */
  public static BTree openOptimized(String fName, ByteComparable keyType, ByteStorable valueType, int indexBlockSize,
                                    int valueBlockSize) throws IOException{
    //first try to open
    try{
      return new OptimizedBPTreeImp(fName, keyType, valueType);
    }
    catch(Exception e){CoreLog.L().log(Level.FINER, "", e);}

    return createOptimized(fName, keyType, valueType, indexBlockSize, valueBlockSize);
  }


  /**
   * An Optimized Btree uses only one backing file to store
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
  public static BTree createOptimizedBlob(String fName, ByteComparable keyType, ByteStorable valueType) throws IOException {
    return createOptimizedBlob(fName, keyType, valueType, DEFAULT_INDEX_BLOCK, DEFAULT_VALUE_BLOCK);
  }

  /**
   * An Optimized Btree uses only one backing file to store
   * the index and key/value blocks. The index is kept in a
   * memory mapped buffer for fast access i.e. no need to do
   * any caching of the index.
   * @param fName filname (without) extension
   * @param keyType template for creating keys
   * @param valueType template for creating values
   * @return the newly created btree
   * @throws IOException
   */
  public static BTree createOptimizedBlob(String fName, ByteComparable keyType, ByteStorable valueType,
                                      int indexSize, int valueSize) throws IOException{
    return new OptimizedBPBlobTreeImp(fName, keyType, valueType, indexSize, valueSize);
  }

  /**
   * An Optimized Btree uses only one backing file to store
   * the index and key/value blocks. The index is kept in a
   * memory mapped buffer for fast access i.e. no need to do
   * any caching of the index.
   * Opens an existing btree, if that fails creates a new one
   * @param fName filname (without) extension
   * @param keyType template for creating keys
   * @param valueType template for creating values
   * @return the newly created btree
   * @throws IOException
   */
  public static BTree openOptimizedBlob(String fName, ByteComparable keyType, ByteStorable valueType) throws IOException {
    return openOptimizedBlob(fName, keyType, valueType, DEFAULT_INDEX_BLOCK, DEFAULT_VALUE_BLOCK);
  }

  /**
   * An Optimized Btree uses only one backing file to store
   * the index and key/value blocks. The index is kept in a
   * memory mapped buffer for fast access i.e. no need to do
   * any caching of the index.
   * Opens an existing btree, if that fails creates a new one
   * @param fName filname (without) extension
   * @param keyType template for creating keys
   * @param valueType template for creating values
   * @return the newly created btree
   * @throws IOException
   */
  public static BTree openOptimizedBlob(String fName, ByteComparable keyType, ByteStorable valueType, int indexBlockSize,
                                    int valueBlockSize) throws IOException{
    //first try to open
    try{
      return new OptimizedBPBlobTreeImp(fName, keyType, valueType);
    }
    catch(Exception e){CoreLog.L().log(Level.FINER, "", e);}

    return createOptimizedBlob(fName, keyType, valueType, indexBlockSize, valueBlockSize);
  }

  //FROM HERE

  /**
   * An Optimized Btree uses only one backing file to store
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
  public static BTree createMemMapped(String fName, ByteComparable keyType, ByteStorable valueType) throws IOException {
    return createMemMapped(fName, keyType, valueType, DEFAULT_INDEX_BLOCK, DEFAULT_VALUE_BLOCK);
  }

  /**
   * An Optimized Btree uses only one backing file to store
   * the index and key/value blocks. The index is kept in a
   * memory mapped buffer for fast access i.e. no need to do
   * any caching of the index.
   * @param fName filname (without) extension
   * @param keyType template for creating keys
   * @param valueType template for creating values
   * @return the newly created btree
   * @throws IOException
   */
  public static BTree createMemMapped(String fName, ByteComparable keyType, ByteStorable valueType,
                                      int indexSize, int valueSize) throws IOException{
    return new MemMappedBPTreeImp(fName, keyType, valueType, indexSize, valueSize);
  }

  /**
   * An Optimized Btree uses only one backing file to store
   * the index and key/value blocks. The index is kept in a
   * memory mapped buffer for fast access i.e. no need to do
   * any caching of the index.
   * Opens an existing btree, if that fails creates a new one
   * @param fName filname (without) extension
   * @param keyType template for creating keys
   * @param valueType template for creating values
   * @return the newly created btree
   * @throws IOException
   */
  public static BTree openMemMapped(String fName, ByteComparable keyType, ByteStorable valueType) throws IOException {
    return openMemMapped(fName, keyType, valueType, DEFAULT_INDEX_BLOCK, DEFAULT_VALUE_BLOCK);
  }

  /**
   * An Optimized Btree uses only one backing file to store
   * the index and key/value blocks. The index is kept in a
   * memory mapped buffer for fast access i.e. no need to do
   * any caching of the index.
   * Opens an existing btree, if that fails creates a new one
   * @param fName filname (without) extension
   * @param keyType template for creating keys
   * @param valueType template for creating values
   * @return the newly created btree
   * @throws IOException
   */
  public static BTree openMemMapped(String fName, ByteComparable keyType, ByteStorable valueType, int indexBlockSize,
                                    int valueBlockSize) throws IOException{
    //first try to open
    try{
      return new MemMappedBPTreeImp(fName, keyType, valueType);
    }
    catch(Exception e){CoreLog.L().log(Level.FINER, "", e);}

    return createMemMapped(fName, keyType, valueType, indexBlockSize, valueBlockSize);
  }


  /**
   * An Optimized Btree uses only one backing file to store
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
  public static BTree createMemMappedBlob(String fName, ByteComparable keyType, ByteStorable valueType) throws IOException {
    return createMemMappedBlob(fName, keyType, valueType, DEFAULT_INDEX_BLOCK, DEFAULT_VALUE_BLOCK);
  }

  /**
   * An Optimized Btree uses only one backing file to store
   * the index and key/value blocks. The index is kept in a
   * memory mapped buffer for fast access i.e. no need to do
   * any caching of the index.
   * @param fName filname (without) extension
   * @param keyType template for creating keys
   * @param valueType template for creating values
   * @return the newly created btree
   * @throws IOException
   */
  public static BTree createMemMappedBlob(String fName, ByteComparable keyType, ByteStorable valueType,
                                          int indexSize, int valueSize) throws IOException{
    return new MemMappedBPBlobTreeImp(fName, keyType, valueType, indexSize, valueSize);
  }

  /**
   * An Optimized Btree uses only one backing file to store
   * the index and key/value blocks. The index is kept in a
   * memory mapped buffer for fast access i.e. no need to do
   * any caching of the index.
   * Opens an existing btree, if that fails creates a new one
   * @param fName filname (without) extension
   * @param keyType template for creating keys
   * @param valueType template for creating values
   * @return the newly created btree
   * @throws IOException
   */
  public static BTree openMemMappedBlob(String fName, ByteComparable keyType, ByteStorable valueType) throws IOException {
    return openMemMappedBlob(fName, keyType, valueType, DEFAULT_INDEX_BLOCK, DEFAULT_VALUE_BLOCK);
  }

  /**
   * An Optimized Btree uses only one backing file to store
   * the index and key/value blocks. The index is kept in a
   * memory mapped buffer for fast access i.e. no need to do
   * any caching of the index.
   * Opens an existing btree, if that fails creates a new one
   * @param fName filname (without) extension
   * @param keyType template for creating keys
   * @param valueType template for creating values
   * @return the newly created btree
   * @throws IOException
   */
  public static BTree openMemMappedBlob(String fName, ByteComparable keyType, ByteStorable valueType, int indexBlockSize,
                                        int valueBlockSize) throws IOException{
    //first try to open
    try{
      return new MemMappedBPBlobTreeImp(fName, keyType, valueType);
    }
    catch(Exception e){CoreLog.L().log(Level.FINER, "", e);}

    return createMemMappedBlob(fName, keyType, valueType, indexBlockSize, valueBlockSize);
  }

}
