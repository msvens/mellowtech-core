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
 * Date: 2013-03-11
 * Time: 08:24
 *
 * @author Martin Svensson
 */
public class BlockFile extends AbstractBlockFile {


  public BlockFile(Path p) throws IOException {
    super(p);
  }


  public BlockFile(Path p, int blockSize, int maxBlocks, int reserve) throws IOException {
    super(p, blockSize, maxBlocks, reserve);
  }

  @Override
  public void clear() throws IOException{
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
      return true;
    }
    return false;
  }

}
