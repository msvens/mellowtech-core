/**
 * 
 */
package org.mellowtech.core.collections.tree;

import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;

/**
 * @author msvens
 *
 */
public class BTreeBuilder {
  
  public static final int DEFAULT_INDEX_BLOCK = 1024 * 2;
  public static final int DEFAULT_VALUE_BLOCK = 1024 * 4;
  public static final int DEFAULT_MAX_BLOCKS = 1024*1014;
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
      toRet = indexInMemory ? new MemMappedBPTreeImp <> (fileName, keyType, valueType, valuesInMemory) :
        new BPTreeImp <> (fileName, keyType, valueType);
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
    BTree <A,B,C,D> toRet = null;
    //first try to open
    try {
      toRet = indexInMemory ? new MemMappedBPBlobTreeImp <> (fileName, keyType, valueType, valuesInMemory) :
        new BPBlobTreeImp <> (fileName, keyType, valueType);
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
