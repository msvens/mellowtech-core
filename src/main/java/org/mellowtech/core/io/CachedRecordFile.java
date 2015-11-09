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

package org.mellowtech.core.io;

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
  public void insert(int record, byte[] bytes) throws IOException {
    if (record >= maxBlocks) throw new IOException("record out of block range");
    bitSet.set(record, true);
    update(record, bytes);
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
