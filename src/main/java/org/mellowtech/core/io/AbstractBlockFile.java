
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

import com.google.common.base.Objects;
import org.mellowtech.core.CoreLog;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

/**
 * Format of File
 * MAGIC_MARKER//FILE_VERSION//BLOCK_SIZE//MAX_BLOCKS//RESERVED_SIZE//BITSET_NUM_LONGS//BITSET//RESERVED//BLOCKS
 * Created by msvens on 30/10/15.
 */
abstract class AbstractBlockFile implements RecordFile {

  public static String FILE_EXT = ".blf";
  public static int MIN_BLOCK_SIZE = 256;
  public static String MAGIC = "MSBF";
  public static int FILE_VERSION = 2;
  protected Path p;
  protected FileChannel fc;
  protected int maxBlocks;
  protected BitSet bitSet;

  @Override
  public boolean isOpen() {
    return fc.isOpen();
  }

  protected MappedByteBuffer bitBuffer;
  private int blockSize;
  private int reserve;

  public AbstractBlockFile(Path p) throws IOException {
    openFile(p);
  }

  public AbstractBlockFile(Path p, int blockSize, int maxBlocks, int reserve) throws IOException {
    try {
      if (openFile(p)) return;
    } catch (IOException e) {
      CoreLog.L().log(Level.FINER, "Could not open block file", e);
    }

    if (reserve < 0) reserve = 0;
    this.reserve = reserve;
    this.maxBlocks = maxBlocks;
    this.blockSize = (blockSize < MIN_BLOCK_SIZE) ? MIN_BLOCK_SIZE : blockSize;
    this.p = p;

    createFile(p);
    openFile(p);
  }

  protected void truncate() throws IOException {
    fc.truncate(blocksOffset());
  }

  @Override
  public void clear() throws IOException {
    bitSet.clear();
  }

  @Override
  public void close() throws IOException {
    if(isOpen()){
      save();
      fc.close();
    }
  }

  @Override
  public Map<Integer, Integer> compact() throws IOException {
    return null;
  }

  @Override
  public boolean contains(int record) {
    return bitSet.get(record);
  }

  @Override
  public boolean delete(int record) throws IOException {
    //if(fileName.endsWith(".idx"))
    if (bitSet.get(record)) {
      bitSet.flip(record);
      saveBitSet();
      return true;
    } else
      return false;
  }

  @Override
  public void deleteAll() throws IOException {
    bitSet.clear();
    saveBitSet();
  }


  @Override
  public int getBlockSize() {
    return blockSize;
  }

  @Override
  public int getFirstRecord() {
    return bitSet.nextSetBit(0);
  }

  @Override
  public int getFreeBlocks() {
    return maxBlocks - bitSet.cardinality();
  }

  @Override
  public MappedByteBuffer getMapped(int record) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public byte[] getReserve() throws IOException {
    if (reserve < 1) return null;
    ByteBuffer bb = ByteBuffer.allocate(reserve);
    fc.read(bb, reservedOffset());
    return bb.array();
  }

  @Override
  public long fileSize() throws IOException{
    return fc.size();
  }

  @Override
  public Iterator<Record> iterator() {
    return new BlockFileIterator(null);
  }

  @Override
  public Iterator<Record> iterator(int record) {
    return new BlockFileIterator(record > -1 ? record : null);
  }

  public MappedByteBuffer mapReserve() throws IOException {
    return fc.map(FileChannel.MapMode.READ_WRITE, reservedOffset(), reserve);
  }

  @Override
  public boolean save() throws IOException {
    ByteBuffer bb = ByteBuffer.allocate(headerSize());
    bb.put(MAGIC.getBytes());
    bb.putInt(BlockFile.FILE_VERSION);
    bb.putInt(blockSize);
    bb.putInt(maxBlocks);
    bb.putInt(reserve);
    bb.flip();
    fc.write(bb, headerOffset());
    saveBitSet();
    bitBuffer.force();
    fc.force(true);
    return true;
  }

  @Override
  public void setReserve(byte[] bytes) throws IOException {
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    if (bytes.length > reserve) bb.limit(reserve);
    fc.write(bb, reservedOffset());
  }

  @Override
  public int size() {
    return bitSet.cardinality();
  }

  public String toString() {
    return Objects.toStringHelper(this).add("maxBlocks", maxBlocks).
        add("fileName", p.toString()).
        add("blockstart", blocksOffset()).
        add("blockSize", blockSize).
        add("reserve", reserve).
        add("bitSet", bitSet.toString()).toString();
  }

  protected long align(long offset) {
    return offset + (blockSize - (offset % blockSize));
  }

  protected long bitSetOffset() {
    return blockSize;
  }

  protected int bitSetSize() {
    int bytes = (int) Math.ceil((maxBlocks / 8D));

    return 4 + bytes;
  }

  protected long blocksOffset() {
    return align(reservedOffset() + reservedSize());
  }

  protected void createFile(Path p) throws IOException {
    fc = FileChannel.open(p, StandardOpenOption.CREATE_NEW, StandardOpenOption.READ, StandardOpenOption.WRITE);
    bitBuffer = fc.map(FileChannel.MapMode.READ_WRITE, bitSetOffset(), bitSetSize());
    bitSet = new BitSet();
    close();
  }

  protected long getOffset(int record) {
    return blocksOffset() + ((long)record * (blockSize));
  }

  protected long headerOffset() {
    return 0;
  }

  protected int headerSize() {
    return 20;
  }

  protected boolean openFile(Path p) throws IOException {
    if (Files.notExists(p)) return false;

    fc = FileChannel.open(p, StandardOpenOption.READ, StandardOpenOption.WRITE);

    this.p = p;

    //First read header:
    ByteBuffer bb = ByteBuffer.allocate(headerSize());
    fc.read(bb, 0);
    bb.flip();
    if (bb.limit() < headerSize())
      throw new IOException("not a block file");

    //Magic Marker, FileVersion, BlockSize, MaxBlocks, Reserved
    byte[] b = new byte[4];
    bb.get(b);
    String marker = new String(b);

    if (!marker.equals(MAGIC))
      throw new IOException("not a block file");

    int fversion = bb.getInt();
    if (fversion != FILE_VERSION)
      throw new IOException("wrong version of file: " + fversion);

    blockSize = bb.getInt();
    maxBlocks = bb.getInt();
    reserve = bb.getInt();

    bitBuffer = fc.map(FileChannel.MapMode.READ_WRITE, bitSetOffset(), bitSetSize());
    int numBytes = bitBuffer.getInt();
    ByteBuffer lb = bitBuffer.slice();
    lb.limit(numBytes);
    bitSet = BitSet.valueOf(lb);
    return true;
  }

  protected long reservedOffset() {
    return align(bitSetOffset() + bitSetSize());
  }

  protected int reservedSize() {
    return this.reserve;
  }

  protected void saveBitSet() throws IOException {
    bitBuffer.clear();
    byte[] bits = bitSet.toByteArray();
    bitBuffer.putInt(bits.length);
    bitBuffer.put(bits);
    //bitBuffer.asLongBuffer().put(bits);
  }

  class BlockFileIterator implements Iterator<Record> {


    Record next = null;
    int record;

    public BlockFileIterator(Integer from) {
      record = from != null ? from : 0;
      getNext();
    }

    @Override
    public boolean hasNext() {
      return record > 0;
    }

    @Override
    public Record next() {
      Record toRet = next;
      getNext();
      return toRet;
    }

    private void getNext() {
      if (record < 0) return;
      record = bitSet.nextSetBit(record);
      if (record > -1) {
        try {
          next = new Record(record, get(record));
          record++;
        } catch (IOException e) {
          record = -1;
        }
      }
    }
  }

}
