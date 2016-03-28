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
import java.nio.MappedByteBuffer;
import java.nio.file.Path;


/**
 * @author Martin Svensson
 */
public class MemBlockFile extends AbstractBlockFile {

  private BlockMapper bmap;


  public MemBlockFile(Path p) throws IOException {
    super(p);
    bmap = new BlockMapper(fc, blocksOffset(), getBlockSize(), maxBlocks);
    bmap.map(fc.size());
  }

  public MemBlockFile(Path p, int blockSize, int maxBlocks, int reserve) throws IOException {
    super(p, blockSize, maxBlocks, reserve);
    bmap = new BlockMapper(fc, blocksOffset(), blockSize, maxBlocks);
    bmap.map(fc.size());
  }

  @Override
  public void clear() throws IOException{
    super.clear();
    bmap = null;
    truncate();
    bmap = new BlockMapper(fc, blocksOffset(), getBlockSize(), maxBlocks);
    bmap.map(fc.size());
  }

  @Override
  public MappedByteBuffer getMapped(int record){
    return bitSet.get(record) ? bmap.slice(record) : null;
  }

  @Override
  public boolean get(int record, byte[] buffer) throws IOException {
    if (bitSet.get(record)) {
      //ByteBuffer bb = findBuffer(record);
      ByteBuffer bb = bmap.find(record);
      record = bmap.truncate(record);
      bb.position(record * getBlockSize());
      if (buffer.length > getBlockSize())
        bb.get(buffer, 0, getBlockSize());
      else
        bb.get(buffer);
      return true;
    }
    return false;
  }

  @Override
  public int insert(byte[] bytes, int offset, int length) throws IOException {
    if (size() >= maxBlocks)
      throw new IOException("no blocks left");

    int index = bitSet.nextClearBit(0);

    bmap.maybeExpand(index);

    if (bytes != null && length > 0) {
      ByteBuffer bb = bmap.find(index);
      int record = bmap.truncate(index);
      bb.position(record * getBlockSize());
      bb.put(bytes, offset, length > getBlockSize() ? getBlockSize() : length);
    }
    bitSet.set(index, true);
    saveBitSet();
    return index;
  }

  @Override
  public void insert(int record, byte[] bytes, int offset, int length) throws IOException {
    if (record >= maxBlocks)
      throw new IOException("record out of range");
    bmap.maybeExpand(record);

    bitSet.set(record, true);
    saveBitSet();
    update(record, bytes, offset, length);

  }

  @Override
  public boolean update(int record, byte[] bytes, int offset, int length) throws IOException {
    if (!bitSet.get(record)) return false;
    ByteBuffer bb = bmap.find(record);
    record = bmap.truncate(record);
    bb.position(record * getBlockSize());
    bb.put(bytes, offset, length > getBlockSize() ? getBlockSize() : length);
    return true;
  }

  @Override
  public boolean save() throws IOException {
    if (bmap != null) bmap.force();
    super.save();
    return true;
  }

  @Override
  public boolean delete(int record) throws IOException {
    if (super.delete(record)) {
      bmap.shrink(getLastRecord());
      return true;
    }
    return false;
  }

  protected int getLastRecord() {
    return bitSet.length() - 1;
  }


}
