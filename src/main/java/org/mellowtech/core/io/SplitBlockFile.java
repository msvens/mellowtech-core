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
 * @author Martin Svensson
 */
public class SplitBlockFile extends AbstractSplitBlockFile {

  public SplitBlockFile(Path path) throws IOException {
    super(path);
  }

  public SplitBlockFile(Path path, int blockSize, int maxBlocks,
                        int reserve, int mappedMaxBlocks, int mappedBlockSize) throws IOException {
    super(path, blockSize, maxBlocks, reserve, mappedMaxBlocks, mappedBlockSize);
    //System.out.println(mappedMaxBlocks+" "+maxBlocks);
  }

  @Override
  public void clear() throws IOException {
    super.clear();
    truncate();
  }

  @Override
  public boolean get(int record, byte[] buffer) throws IOException {
    if (bitSet.get(record)) {
      ByteBuffer bb = ByteBuffer.wrap(buffer);
      long offset = getOffset(record);
      fc.read(bb, offset);
      return true;
    }
    return false;
  }

  @Override
  public void insert(int record, byte[] bytes) throws IOException {
    if (record >= maxBlocks)
      throw new IOException("record out of bounce");
    bitSet.set(record, true);
    saveBitSet(bitSet, bitBuffer);
    update(record, bytes);
  }

  @Override
  public int insert(byte[] bytes, int offset, int length) throws IOException {
    //System.out.println(getFreeBlocks()+" "+size()+" "+maxBlocks);
    if (getFreeBlocks() < 1) throw new IOException("no free blocks");
    int index = bitSet.nextClearBit(0);
    if (index >= maxBlocks)
      throw new IOException("no blocks left");
    if (bytes != null && length > 0) {
      long off = getOffset(index);
      ByteBuffer data = ByteBuffer.wrap(bytes, offset, length > getBlockSize() ? getBlockSize() : length);
      fc.write(data, off);
    }
    bitSet.set(index, true);
    saveBitSet(bitSet, bitBuffer);
    return index;
  }

  @Override
  public boolean update(int record, byte[] bytes, int offset, int length) throws IOException {
    if (!bitSet.get(record)) return false;
    long off = getOffset(record);
    ByteBuffer bb = ByteBuffer.wrap(bytes, offset, length > getBlockSize() ? getBlockSize() : length);
    fc.write(bb, off);
    return true;
  }

}
