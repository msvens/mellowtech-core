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
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
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
public class MemBlockFile implements RecordFile {

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
  public static final String FILE_EXT = ".blf";
  public static final int MIN_BLOCK_SIZE = 256;
  
  public static final String MAGIC = "MSBF";


  //This will change to not be statically set
  private long blocksToMap;
  private int blockSize;

  private int bitSize;
  private String fileName;
  private FileChannel fc;


  private int maxBlocks;
  private BitSet bitSet;
  
  private MappedByteBuffer bitBuffer;


  private final List<MappedByteBuffer> blocks = new ArrayList<>();
  private long start;
  
  private int reserve;
  
  public MemBlockFile(String fileName) throws IOException{
    openFile(fileName);
  }
  
  public MemBlockFile(String fileName, int blockSize, int maxBlocks, int reserve) throws IOException{
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
    this.blocksToMap = this.calcBlocksToMap();
    this.fileName = fileName;
    RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
    raf.setLength(start+(blocksToMap*blockSize));
    
    fc = raf.getChannel();
    mapBlocks(start, fc.size());
    bitBuffer = fc.map(FileChannel.MapMode.READ_WRITE, 16, start-this.reserve);
    bitSet = new BitSet();

  }

  @Override
  public void clear() throws IOException {
    bitSet.clear();
  }
  
  @Override
  public void close() throws IOException {
    save();
    fc.close();
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
    if(bitSet.get(record)){
      bitSet.flip(record);
      saveBitSet();
      shrinkBlocks();
      return true;
    }
    return false;
  }

  private void expandBlocks(int toIndex) throws IOException{
    int pos = getBufferPos(toIndex);
    for(int i = blocks.size(); i <= pos; i++){
      long filePos = start + (pos * blockSize * blocksToMap);
      blocks.add(fc.map(FileChannel.MapMode.READ_WRITE, filePos, blockSize * blocksToMap));
    }
  }


  private MappedByteBuffer findBuffer(int record){
    return blocks.get(getBufferPos(record));
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
  public int getBlockSize() {
    return blockSize;
  }

  private int getBufferPos(int record){
    return record / (int) blocksToMap;
  }

  @Override
  public int getFirstRecord() {
    return bitSet.nextSetBit(0);
  }

  @Override
  public int getFreeBlocks(){
    return maxBlocks - bitSet.cardinality();
  }

  public int getLastRecord(){
    return bitSet.length() - 1;
  }

  /**
   * Calculates a good number of blocks to map
   * @param blockSize
   * @return
   */
  protected int calcBlocksToMap(){
    int region4 = 1024*1024*4;
    int region8 = region4 * 2;
    int region16 = region8 * 2;
    int region32 = region16 * 2;
    int region64 = region32 * 2; //64 MB
    
    
    long maxRecordSize = (long) blockSize * (long) maxBlocks;
    if(maxRecordSize < region64){
      return maxBlocks;
    }
    else if(maxRecordSize/region64 > 10){
      return (int) Math.ceil(region64/(double)blockSize);
    }
    else if(maxRecordSize/region32 > 10){
      return (int) Math.ceil(region32/(double)blockSize);
    }
    else if(maxRecordSize/region16 > 10){
      return (int) Math.ceil(region16/(double)blockSize);
    }
    else if(maxRecordSize/region8 > 10){
      return (int) Math.ceil(region8/(double)blockSize);
    }
    else{
      return (int) Math.ceil(region4/(double)blockSize);
    }
  }

  protected long getOffset(int record){
    return start + (record * (blockSize));
  }
  
  @Override
  public byte[] getReserve() throws IOException {
    if(reserve < 1) return null;
    ByteBuffer bb = ByteBuffer.allocate(reserve);
    fc.read(bb, 20 + bitSize);
    return bb.array();
  }

  @Override
  public int insert(byte[] bytes) throws IOException{
    return insert(bytes, 0, bytes != null ? bytes.length : -1);
  }

  @Override
  public int insert(byte[] bytes, int offset, int length) throws IOException {
    int index = bitSet.nextClearBit(0);
    if(index >= maxBlocks)
      throw new IOException("no blocks left");

    if(getBufferPos(index) >= blocks.size()){
      expandBlocks(index);
    }

    if(bytes != null && length > 0){
      ByteBuffer bb = findBuffer(index);
      int record = truncate(index);
      bb.position(record * blockSize);
      bb.put(bytes, offset, length > blockSize ? blockSize : length);
    }
    bitSet.set(index, true);
    saveBitSet();
    return index;
  }

  @Override
  public void insert(int record, byte[] bytes) throws IOException {
    if(record >= maxBlocks)
      throw new IOException("record out of bounce");
    if(getBufferPos(record) >= blocks.size())
      expandBlocks(record);

    bitSet.set(record, true);
    saveBitSet();
    update(record, bytes);
    
  }

  @Override
  public Iterator<Record> iterator() {
    return new BlockFileIterator(null);
  }

  @Override
  public Iterator<Record> iterator(int record) {
    return new BlockFileIterator(record > -1 ? record : null);
  }



  private void mapBlocks(long start, long end) throws IOException{
    int region = blockSize * (int) blocksToMap;
    long current = start;
    while(current < end){
      MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_WRITE, current, region);
      current += region;
      blocks.add(bb);
    }
  }

  public MappedByteBuffer mapReserve() throws IOException {
    return fc.map(MapMode.READ_WRITE, 20+bitSize, reserve);
  }

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
    blocksToMap = this.calcBlocksToMap();

    reserve = bb.getInt();

    this.fileName = fileName;

    bitSize = maxBlocks/8;

    start = 20 + bitSize + reserve;

    mapBlocks(start, fc.size());
    
    bitBuffer = fc.map(FileChannel.MapMode.READ_WRITE, 16, start-reserve);
    int numLongs = bitBuffer.getInt();
    LongBuffer lb = bitBuffer.asLongBuffer();

    lb.limit(numLongs);
    bitSet = BitSet.valueOf(lb);

    return true;
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
    //This is probably not needed:
    for(MappedByteBuffer mpp : blocks){
      mpp.force();
    }
    fc.force(true);
    return true;
  }

  protected void saveBitSet() throws IOException{
    bitBuffer.clear();
    long[] bits = bitSet.toLongArray();
    bitBuffer.putInt(bits.length);
    bitBuffer.asLongBuffer().put(bits);
  }


  @Override
  public void setReserve(byte[] bytes) throws IOException {
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    if(bytes.length > reserve) bb.limit(reserve);
    fc.write(bb, 20 + bitSize);
  }

  private void shrinkBlocks() throws IOException{
    int highSet = getLastRecord();
    int blockNo = getBufferPos(highSet);
    boolean removed = false;
    for(int i = blocks.size() - 1; i > blockNo; i--){
      blocks.remove(i);
      removed = true;
    }
    if(removed){
      int region = blockSize * (int) blocksToMap;
      fc.truncate(start + (blockNo+1 * region));
    }
  }

  @Override
  public int size() {
    return bitSet.cardinality();
  }

  public String toString(){
    return Objects.toStringHelper(this).add("maxBlocks", maxBlocks).
            add("fileName", fileName).
            add("start", start).
            add("blockSize", blockSize).
            add("reserve", reserve).
            add("bitSet", bitSet.toString()).toString();
  }

  private int truncate(int record){
    return record - (getBufferPos(record) * (int) blocksToMap);
  }

  @Override
  public boolean update(int record, byte[] bytes) throws IOException {
    return update(record, bytes, 0, bytes.length);
  }

  @Override
  public boolean update(int record, byte[] bytes, int offset, int length) throws IOException {
    if(!bitSet.get(record)) return false;
    ByteBuffer bb = findBuffer(record);
    record = truncate(record);
    bb.position(record * blockSize);
    bb.put(bytes, offset, length > blockSize ? blockSize : length);
    return true;
  }


}
