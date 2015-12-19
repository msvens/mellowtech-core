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