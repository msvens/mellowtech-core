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
  public void insert(int record, byte[] bytes) throws IOException {
    if (record >= maxBlocks)
      throw new IOException("record out of range");
    bmap.maybeExpand(record);

    bitSet.set(record, true);
    saveBitSet();
    update(record, bytes);

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
