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

import com.google.common.base.Objects;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;

/**
 *
 * Format of File
 * MAGIC_MARKER//BLOCK_SIZE//MAX_BLOCKS//RESERVED_SIZE//BITSET_NUM_LONGS//BITSET//RESERVED//BLOCKS
 * Date: 2013-03-11
 * Time: 08:24
 *
 * @author Martin Svensson
 */
public class BlockFile implements RecordFile {

  public static final String FILE_EXT = ".blf";
  public static final int MIN_BLOCK_SIZE = 256;
  public static final String MAGIC = "MSBF";


  private int blockSize;
  private int bitSize;

  private String fileName;
  private FileChannel fc;
  private int maxBlocks;


  private BitSet bitSet;
  private MappedByteBuffer bitBuffer;


  private long start;
  private int reserve;


  protected boolean openFile(String fileName) throws IOException{
    File f = new File(fileName);
    if(!f.exists())
      return false;

    @SuppressWarnings("resource")
    RandomAccessFile raf = new RandomAccessFile(f, "rw");
    fc = raf.getChannel();

    //Magic Marker, BlockSize, MaxBlocks, Reserved, BitSetSize

    ByteBuffer bb = ByteBuffer.allocate(20);
    fc.read(bb, 0);
    bb.flip();
    if(bb.limit() < 20)
      throw new IOException("not a block file");

    byte[] b = new byte[4];
    bb.get(b);
    String marker = new String(b);

    if(!marker.equals(MAGIC))
      throw new IOException("not a block file");

    blockSize = bb.getInt();

    maxBlocks = bb.getInt();

    reserve = bb.getInt();

    this.fileName = fileName;

    bitSize = maxBlocks/8;

    start = 20 + bitSize + reserve;


    bitBuffer = fc.map(FileChannel.MapMode.READ_WRITE, 16, start - reserve);
    int numLongs = bitBuffer.getInt();
    LongBuffer lb = bitBuffer.asLongBuffer();

    lb.limit(numLongs);
    bitSet = BitSet.valueOf(lb);

    return true;
  }

  public BlockFile(String fileName) throws IOException{
    openFile(fileName);
  }


  public BlockFile(String fileName, int blockSize, int maxBlocks, int reserve) throws IOException{
    try{
    if(openFile(fileName)){
      return;
      }
    }
    catch(IOException e){
      CoreLog.L().log(Level.FINER, "Could Not Open Old File", e);
    }

    if(reserve < 0) reserve = 0;
    this.reserve = reserve;

    this.maxBlocks = maxBlocks;
    this.bitSize = maxBlocks / 8;
    this.start = 20 + bitSize + reserve;

    this.blockSize = (blockSize < MIN_BLOCK_SIZE) ? MIN_BLOCK_SIZE : blockSize;
    this.fileName = fileName;
    RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
    raf.setLength(0);
    fc = raf.getChannel();

    bitBuffer = fc.map(FileChannel.MapMode.READ_WRITE, 16, start-reserve);
    bitSet = new BitSet();

  }


  @Override
  public Map<Integer, Integer> compact() throws IOException {
    return null;
  }

  @Override
  public boolean save() throws IOException{
    ByteBuffer bb = ByteBuffer.allocate(16);
    bb.put(MAGIC.getBytes());
    bb.putInt(blockSize);
    bb.putInt(maxBlocks);
    bb.putInt(reserve);
    bb.flip();
    fc.write(bb, 0);
    saveBitSet();
    bitBuffer.force();
    fc.force(true);
    return true;
  }

  @Override
  public void close() throws IOException {
    save();
    fc.close();
  }

  @Override
  public void clear() throws IOException {
    bitSet.clear();
  }

  @Override
  public int size() {
    return bitSet.cardinality();
  }

  @Override
  public int getFreeBlocks(){
    return maxBlocks - bitSet.cardinality();
  }

  @Override
  public int getBlockSize() {
    return blockSize;
  }
  
  public MappedByteBuffer mapReserve() throws IOException {
    return fc.map(MapMode.READ_WRITE, 20+bitSize, reserve);
  }

  @Override
  public void setReserve(byte[] bytes) throws IOException {
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    if(bytes.length > reserve) bb.limit(reserve);
    fc.write(bb, 20 + bitSize);
  }

  @Override
  public byte[] getReserve() throws IOException {
    if(reserve < 1) return null;
    ByteBuffer bb = ByteBuffer.allocate(reserve);
    fc.read(bb, 20 + bitSize);
    return bb.array();
  }

  @Override
  public int getFirstRecord() {
    return bitSet.nextSetBit(0);
  }

  @Override
  public byte[] get(int record) throws IOException{
    byte[] bytes = new byte[blockSize];
    return get(record, bytes) == true ? bytes : null;
  }

  @Override
  public boolean get(int record, byte[] buffer) throws IOException{
    if(bitSet.get(record)){
      ByteBuffer bb = ByteBuffer.wrap(buffer);
      long offset = getOffset(record);
      fc.read(bb, offset);
      return true;
    }
    return false;
  }



  @Override
  public boolean update(int record, byte[] bytes) throws IOException {
    return update(record, bytes, 0, bytes.length);
  }

  @Override
  public boolean update(int record, byte[] bytes, int offset, int length) throws IOException {
    if(bitSet.get(record)){
      long off = getOffset(record);
      ByteBuffer bb = ByteBuffer.wrap(bytes, offset, length > blockSize ? blockSize : length);
      fc.write(bb, off);
      return true;
    }
    return false;
  }

  @Override
  public int insert(byte[] bytes) throws IOException{
    return insert(bytes, 0, bytes != null ? bytes.length : -1);
  }

  @Override
  public int insert(byte[] bytes, int offset, int length) throws IOException {
    int index = bitSet.nextClearBit(0);
    if(bytes != null && bytes.length > 0){
      long off = getOffset(index);
      if(length > blockSize) length = blockSize;
      ByteBuffer data =  ByteBuffer.wrap(bytes, offset, length);
      fc.write(data, off);
    }
    bitSet.set(index, true);
    saveBitSet();

    return index;
  }

  @Override
  public void insert(int record, byte[] bytes) throws IOException {
    bitSet.set(record, true);
    saveBitSet();
    update(record, bytes);
  }

  @Override
  public boolean delete(int record) throws IOException {
    //if(fileName.endsWith(".idx"))
     if(bitSet.get(record)){
      bitSet.flip(record);
      saveBitSet();
    }
    return false;
  }


  @Override
  public boolean contains(int record) {
    return bitSet.get(record);
  }

  @Override
  public Iterator<Record> iterator() {
    return new BlockFileIterator(null);
  }

  @Override
  public Iterator<Record> iterator(int record) {
    return new BlockFileIterator(record > -1 ? record : null);
  }

  public String toString(){
    return Objects.toStringHelper(this).add("maxBlocks", maxBlocks).
            add("fileName", fileName).
            add("start", start).
            add("blockSize", blockSize).
            add("reserve", reserve).
            add("bitSet", bitSet.toString()).toString();
  }

  protected long getOffset(int record){
    return start + (record * (blockSize));
  }

  protected void saveBitSet() throws IOException{
    bitBuffer.clear();
    long[] bits = bitSet.toLongArray();
    bitBuffer.putInt(bits.length);
    bitBuffer.asLongBuffer().put(bits);
  }

  class BlockFileIterator implements Iterator<Record>{


    Record next = null;
    Integer nextRec;
    int record;
    boolean stop = false;
    int currIndex;

    public BlockFileIterator(Integer from){
      record = from != null ? from : 0;
      getNext();

    }

    private void getNext(){
      if(record < 0) return;
      record = bitSet.nextSetBit(record);
      if(record > -1){
        try{
          next = new Record(record, get(record));
          record++;
          return;
        }
        catch(IOException e){record = -1;}
      }
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

    @Override
    public void remove() {
    }
  }


}
