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

package org.mellowtech.core.io.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


/**
 * A Guava cache backed up block file.
 * Date: 2013-03-24
 * Time: 12:09
 *
 * @author Martin Svensson
 */
public class CachedRecordFile extends AbstractBlockFile {

  LoadingCache<Integer,byte[]> lru;
  private int cacheSize;

  CacheLoader<Integer, byte[]> loader = new CacheLoader<Integer, byte[]>() {
    @Override
    public byte[] load(Integer key) throws Exception {
      if (bitSet.get(key)) {
        ByteBuffer bb = ByteBuffer.allocate(getBlockSize());
        long offset = getOffset(key);
        fc.read(bb, offset);
        return bb.array();
      }
      else
        throw new NoSuchElementException();
    }
  };

  private void initCache(){
    lru = CacheBuilder.newBuilder().maximumSize(cacheSize).build(loader);
  }

  public CachedRecordFile(Path p, int cacheSize) throws IOException {
    super(p);
    this.cacheSize = cacheSize;
    initCache();
  }

  public CachedRecordFile(Path p, int blockSize, int maxBlocks, int reserve, int cacheSize) throws IOException {
    super(p, blockSize, maxBlocks, reserve);
    this.cacheSize = cacheSize;
    initCache();
  }

  @Override
  public void clear() throws IOException {
    super.clear();
    lru.invalidateAll();
    truncate();
  }

  @Override
  public boolean delete(int record) throws IOException {
    if(super.delete(record)){
      lru.invalidate(record);
      return true;
    }
    return false;
  }

  @Override
  public boolean get(int record, byte[] buffer) throws IOException {
    if(!contains(record)) return false;
    try {
      byte b[] = lru.get(record);
      if(b != null){
        int len = Math.min(b.length, buffer.length);
        System.arraycopy(b, 0, buffer, 0, len);
        return true;
      }
      return false;
    }
    catch(ExecutionException ee){
      throw new IOException(ee);
    }
  }

  @Override
  public byte[] get(int record) throws IOException {
    if(!contains(record)) return null;
    try {
      return lru.get(record);
    } catch (ExecutionException e) {
      throw new IOException(e);
    }
  }

  @Override
  public int insert(byte[] bytes, int offset, int length) throws IOException {
    if (getFreeBlocks() < 1) throw new IOException("no free blocks");
    int index = bitSet.nextClearBit(0);
    bitSet.set(index, true);
    update(index, bytes, offset, length);
    saveBitSet();
    return index;
  }

  @Override
  public void insert(int record, byte[] bytes, int offset, int length) throws IOException {
    if (record >= maxBlocks) throw new IOException("record out of block range");
    bitSet.set(record, true);
    update(record, bytes, offset, length);
    saveBitSet();
  }

  @Override
  public boolean update(int record, byte[] bytes, int offset, int length) throws IOException {
    if (bitSet.get(record) && bytes != null && bytes.length > 0) {
      long off = getOffset(record);
      ByteBuffer bb = ByteBuffer.wrap(bytes, offset, length > getBlockSize() ? getBlockSize() : length);
      fc.write(bb, off);
      lru.invalidate(record);
      return true;
    }
    return false;
  }


}
