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

package org.mellowtech.core.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

/**
 * Same as SplitBlockFile but with the difference that
 * the second portion of the file is also memory mapped.
 * The file will increase in regular intervals
 * @author Martin Svensson
 */
public class MemSplitBlockFile extends AbstractSplitBlockFile {

  private BlockMapper bmap;

  public MemSplitBlockFile(Path path) throws IOException{
    super(path);
    bmap = new BlockMapper(fc, blocksOffset(), getBlockSize(), maxBlocks);
    bmap.map(fc.size());
  }

  public MemSplitBlockFile(Path path, int blockSize, int maxBlocks,
                           int reserve, int mappedMaxBlocks, int mappedBlockSize) throws IOException{
    super(path, blockSize, maxBlocks, reserve, mappedMaxBlocks, mappedBlockSize);
    bmap = new BlockMapper(fc, blocksOffset(), getBlockSize(), maxBlocks);
    bmap.map(fc.size());

  }

  @Override
  public void clear() throws IOException {
    super.clear();
    bmap = null;
    truncate();
    bmap = new BlockMapper(fc, blocksOffset(), getBlockSize(), maxBlocks);
    bmap.map(fc.size());
  }

  @Override
  public boolean delete(int record) throws IOException {
    if (super.delete(record)) {
      bmap.shrink(getLastRecord());
      return true;
    }
    return false;
  }

  @Override
  public boolean get(int record, byte[] buffer) throws IOException{
    if(bitSet.get(record)){
      ByteBuffer bb = bmap.find(record);
      record = bmap.truncate(record);
      bb.position(record * getBlockSize());
      if(buffer.length > getBlockSize()){
        bb.get(buffer, 0, getBlockSize());
      }
      else
        bb.get(buffer);
      return true;
    }
    return false;
  }

  public int getLastMappedRecord(){
    return mappedBitSet.length() - 1;
  }

  public int getLastRecord(){
    return bitSet.length() - 1;
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
    saveBitSet(bitSet, bitBuffer);
    return index;

  }

  @Override
  public void insert(int record, byte[] bytes) throws IOException {
    if (record >= maxBlocks)
      throw new IOException("record out of range");
    bmap.maybeExpand(record);

    bitSet.set(record, true);
    saveBitSet(bitSet, bitBuffer);
    update(record, bytes);

  }


  @Override
  public boolean save() throws IOException{
    if (bmap != null) bmap.force();
    super.save();
    return true;
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

}
