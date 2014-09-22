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

package com.mellowtech.core.io;

import com.google.common.base.Objects;
import com.mellowtech.core.CoreLog;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

/**
 * A Block file that keeps a separate mapped region with block accessed via the
 * (mapped... methods). Two separate bitsets are used to mark which blocks have been used
 * Format of File
 * MAGIC_MARKER (4 bytes)
 * BLOCK_SIZE   (4 bytes)
 * MAX_BLOCKS   (4 bytes)
 * MAPPED_BLOCK_SIZE (4 bytes)
 * MAX_MAPPED_BLOCKS (4 bytes)
 * RESERVED_SIZE (4 bytes)
 * MAPPED_BLOCK_BIT_SET (4 bytes + bitset)
 * BLOCK_BIT_SET (4 bytes + bitset)
 * RESERVED
 * MAPPED_BLOCKS
 * BLOCKS
 *
 * @author Martin Svensson
 */
public class SplitBlockFile implements RecordFile {

  public static final String FILE_EXT = ".mlf";
  public static final int MIN_BLOCK_SIZE = 1024;
  public static final String MAGIC = "SABF";


  private String fileName;
  private FileChannel fc;


  private int blockSize;
  private int mappedBlockSize;

  private int maxBlocks;
  private int mappedMaxBlocks;

  private int bitSize;
  private int mappedBitSize;

  private BitSet bitSet;
  private BitSet mappedBitSet;

  private MappedByteBuffer bitBuffer;
  private MappedByteBuffer mappedBitBuffer;
  private MappedByteBuffer mappedBlocks;


  private final int headerSize = 24;

  private long startMappedBlocks, startBlocks;
  private int reserve;


  protected boolean openFile(String fileName) throws IOException{
    File f = new File(fileName);
    if(!f.exists())
      return false;

    @SuppressWarnings("resource")
    RandomAccessFile raf = new RandomAccessFile(f, "rw");
    fc = raf.getChannel();

    //Magic Marker, BlockSize, MaxBlocks, Reserved, BitSetSize

    ByteBuffer bb = ByteBuffer.allocate(headerSize);
    fc.read(bb, 0);
    bb.flip();
    if(bb.limit() < 24)
      throw new IOException("not a block file");

    //magic marker
    byte[] b = new byte[4];
    bb.get(b);
    String marker = new String(b);

    if(!marker.equals(MAGIC))
      throw new IOException("not a block file");

    //ordinary blocks:
    blockSize = bb.getInt();
    maxBlocks = bb.getInt();

    //mapped blocks:
    mappedBlockSize = bb.getInt();
    mappedMaxBlocks = bb.getInt();

    //size of reserved space:
    reserve = bb.getInt();
    this.fileName = fileName;

    init();

    mappedBitSet = getBitSet(mappedBitBuffer);
    bitSet = getBitSet(bitBuffer);
    return true;
  }

  private void init() throws IOException{
    //number of bytes for bitSets
    bitSize = maxBlocks/8;
    mappedBitSize = mappedMaxBlocks/8;

    //mapped blocks start just after the reserved space
    startMappedBlocks = headerSize + bitSize + 4 + mappedBitSize + 4 + reserve;

    //normal blocks start just after mapped blocks
    startBlocks = startMappedBlocks + (mappedMaxBlocks * mappedBlockSize);

    //buffers for bitsets:
    mappedBitBuffer = fc.map(FileChannel.MapMode.READ_WRITE, headerSize, mappedBitSize + 4);
    bitBuffer = fc.map(FileChannel.MapMode.READ_WRITE,
            headerSize + mappedBitSize + 4,
            bitSize + 4);

    //finally map mapped region
    mappedBlocks = fc.map(FileChannel.MapMode.READ_WRITE, startMappedBlocks,
            mappedMaxBlocks * mappedBlockSize);
  }

  private BitSet getBitSet(ByteBuffer bb){
    int longs = bb.getInt();
    LongBuffer lb = bb.asLongBuffer();
    lb.limit(longs);
    return BitSet.valueOf(lb);
  }

  public SplitBlockFile(String fileName) throws IOException{
    openFile(fileName);
  }


  public SplitBlockFile(String fileName, int blockSize, int maxBlocks,
                        int reserve, int mappedMaxBlocks, int mappedBlockSize) throws IOException{

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
    this.blockSize = blockSize < MIN_BLOCK_SIZE ? MIN_BLOCK_SIZE : blockSize;
    this.mappedBlockSize = mappedBlockSize;
    this.mappedMaxBlocks = mappedMaxBlocks;
    this.fileName = fileName;
    File f = new File(fileName);
    RandomAccessFile raf = new RandomAccessFile(f, "rw");
    fc = raf.getChannel();

    init();

    bitSet = new BitSet();
    mappedBitSet = new BitSet();


  }


  @Override
  public Map<Integer, Integer> compact() throws IOException {
    return null;
  }

  @Override
  public boolean save() throws IOException{
    ByteBuffer bb = ByteBuffer.allocate(headerSize);
    bb.put(MAGIC.getBytes());
    bb.putInt(blockSize);
    bb.putInt(maxBlocks);
    bb.putInt(mappedBlockSize);
    bb.putInt(mappedMaxBlocks);
    bb.putInt(reserve);
    bb.flip();
    fc.write(bb, 0);
    saveBitSet(mappedBitSet, mappedBitBuffer);
    mappedBitBuffer.force();
    saveBitSet(bitSet, bitBuffer);
    bitBuffer.force();
    mappedBlocks.force();
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
    mappedBitSet.clear();
  }

  @Override
  public int size() {
    return bitSet.cardinality();
  }
  public int sizeMapped() {
    return mappedBitSet.cardinality();
  }

  @Override
  public int getBlockSize() {
    return blockSize;
  }

  @Override
  public int getFreeBlocks() {
    return maxBlocks - size();
  }

  public int getMappedBlockSize(){
    return mappedBlockSize;
  }

  @Override
  public void reserve(int bytes) throws IOException {
    //To change body of implemented methods use File | Settings | File Templates.
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

  public int getFirstMappedRecord(){
    return mappedBitSet.nextSetBit(0);
  }

  @Override
  public byte[] get(int record) throws IOException{
    byte[] bytes = new byte[blockSize];
    return get(record, bytes) == true ? bytes : null;
  }

  public byte[] getMapped(int record) throws IOException{
    byte[] bytes = new byte[mappedBlockSize];
    return getMapped(record, bytes) == true ? bytes : null;
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

  public boolean getMapped(int record, byte[] buffer) throws IOException{
    if(mappedBitSet.get(record)){
      mappedBlocks.position(record * mappedBlockSize);
      if(buffer.length > mappedBlockSize){
        mappedBlocks.get(buffer, 0, mappedBlockSize);
      }
      else
        mappedBlocks.get(buffer);
      return true;
    }
    return false;
  }

  @Override
  public boolean update(int record, byte[] bytes) throws IOException {
    return update(record, bytes, 0, bytes.length);
    /*if(!bitSet.get(record)) return false;
    long offset = getOffset(record);
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    if(bytes.length > blockSize) bb.limit(blockSize);
    fc.write(bb, offset);
    return true;*/
  }

  @Override
  public boolean update(int record, byte[] bytes, int offset, int length) throws IOException {
    if(!bitSet.get(record)) return false;
    long off = getOffset(record);
    ByteBuffer bb = ByteBuffer.wrap(bytes, offset, length > blockSize ? blockSize : length);
    fc.write(bb, off);
    return true;
  }

  public boolean updateMapped(int record, byte[] bytes) throws IOException{
    if(!mappedBitSet.get(record)) return false;
    mappedBlocks.position(record * mappedBlockSize);
    if(bytes.length > mappedBlockSize)
      mappedBlocks.put(bytes, 0, mappedBlockSize);
    else
      mappedBlocks.put(bytes);
    return true;
  }

  @Override
  public int insert(byte[] bytes) throws IOException{
    return insert(bytes, 0, bytes != null ? bytes.length : -1);
    /*int index = bitSet.nextClearBit(0);
    if(index >= maxBlocks)
      throw new IOException("no blocks left");
    if(bytes != null && bytes.length > 0){
      long offset = getOffset(index);
      ByteBuffer data =  ByteBuffer.wrap(bytes);
      if(bytes.length > blockSize) data.limit(blockSize);
      fc.write(data, offset);
    }
    bitSet.set(index, true);
    saveBitSet(bitSet, bitBuffer);
    return index;*/
  }

  @Override
  public int insert(byte[] bytes, int offset, int length) throws IOException {
    int index = bitSet.nextClearBit(0);
    if(index >= maxBlocks)
      throw new IOException("no blocks left");
    if(bytes != null && length > 0){
      long off = getOffset(index);
      ByteBuffer data =  ByteBuffer.wrap(bytes, offset, length > blockSize ? blockSize : length);
      fc.write(data, off);
    }
    bitSet.set(index, true);
    saveBitSet(bitSet, bitBuffer);
    return index;
  }

  public int insertMapped(byte[] bytes) throws IOException{
    int index = mappedBitSet.nextClearBit(0);
    if(index >= mappedMaxBlocks)
      throw new IOException("no blocks left in mapped region");
    if(bytes != null && bytes.length > 0){
      mappedBlocks.position(index * mappedBlockSize);
      if(bytes.length <= mappedBlockSize){
        mappedBlocks.put(bytes);
      }
      else
        mappedBlocks.put(bytes, 0, mappedBlockSize);
    }
    mappedBitSet.set(index, true);
    saveBitSet(mappedBitSet, mappedBitBuffer);
    return index;
  }

  @Override
  public void insert(int record, byte[] bytes) throws IOException {
    if(record >= maxBlocks)
      throw new IOException("record out of bounce");
    bitSet.set(record, true);
    saveBitSet(bitSet, bitBuffer);
    update(record, bytes);
  }

  public void insertMapped(int record, byte[] bytes) throws IOException {
    if(record > mappedMaxBlocks)
      throw new IOException("record out of bounce");

    mappedBitSet.set(record, true);
    saveBitSet(mappedBitSet, mappedBitBuffer);
    updateMapped(record, bytes);
  }


  @Override
  public boolean delete(int record) throws IOException {
   if(bitSet.get(record)){
      bitSet.flip(record);
      saveBitSet(bitSet, bitBuffer);
    }
    return false;
  }

  public boolean deleteMapped(int record) throws IOException {
    if(mappedBitSet.get(record)){
      mappedBitSet.flip(record);
      saveBitSet(mappedBitSet, mappedBitBuffer);
    }
    return false;
  }


  @Override
  public boolean contains(int record) {
    return bitSet.get(record);
  }

  public boolean containsMapped(int record){
    return mappedBitSet.get(record);
  }

  @Override
  public Iterator<Record> iterator() {
    return new BlockIterator(null);
  }

  @Override
  public Iterator<Record> iterator(int record) {
    return new BlockIterator(record > -1 ? record : null);
  }

  public Iterator<Record> mappedIterator() {
    return new MappedBlockIterator(null);
  }

  public Iterator <Record> mappedIterator(int record){
    return new MappedBlockIterator(record > -1 ? record : null);
  }

  public String toString(){
    return Objects.toStringHelper(this).
            add("maxBlocks", maxBlocks).
            add("fileName", fileName).
            add("startMappedBlocks", startMappedBlocks).
            add("startBlocks", startBlocks).
            add("bitSize", bitSize).
            add("mappedBitSize", mappedBitSize).
            add("blockSize", blockSize).
            add("maxBlocks", maxBlocks).
            add("mappedMaxBlocks", mappedMaxBlocks).
            add("reserve", reserve).
            add("mappedBitSet", mappedBitSet.toString()).
            add("bitSet", bitSet.toString()).toString();
  }

  protected long getOffset(int record){
    return this.startBlocks + (record * blockSize);
  }


  protected void saveBitSet(BitSet toSave, ByteBuffer bb) throws IOException{
    bb.clear();
    long[] bits = toSave.toLongArray();
    bb.putInt(bits.length);
    bb.asLongBuffer().put(toSave.toLongArray());
  }

  class BlockIterator implements Iterator<Record>{


    Record next = null;
    Integer nextRec;
    int record;
    boolean stop = false;
    int currIndex;
    BitSet bits;
    int bSize;

    public BlockIterator(Integer from){
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

  class MappedBlockIterator implements Iterator<Record>{


    Record next = null;
    Integer nextRec;
    int record;
    boolean stop = false;
    int currIndex;
    BitSet bits;
    int bSize;

    public MappedBlockIterator(Integer from){
      record = from != null ? from : 0;
      getNext();

    }

    private void getNext(){
      if(record < 0) return;
      record = mappedBitSet.nextSetBit(record);
      if(record > -1){
        try{
          next = new Record(record, getMapped(record));
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
