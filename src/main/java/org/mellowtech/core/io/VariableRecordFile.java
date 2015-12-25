
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
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * Format
 * MAGIC_MARKER//FILE_VERSION//NUM_RECORDS//RESERVED_SIZE//RESERVED//RECORD_INDEX//BLOCKS
 * Created by msvens on 26/10/15.
 */
public class VariableRecordFile implements RecordFile {


  public static final int MAGIC_MARKER = 3333;
  public static final int FILE_VERSION = 1;

  public static final Long MAX_OFFSET_START = 4294967295L;


  private Path p;
  private FileChannel fc;
  private int numRecords;
  private int reserved;

  private IntBuffer indexBuffer;
  private MappedByteBuffer mbb;

  protected boolean open(Path path) throws IOException {

    if (Files.notExists(path)) return false;

    p = path;
    fc = FileChannel.open(p, StandardOpenOption.READ, StandardOpenOption.WRITE);

    //read header:
    ByteBuffer bb = ByteBuffer.allocate(headerSize());
    fc.read(bb, 0);
    bb.flip();
    if (bb.limit() < headerSize())
      throw new IOException("not a varialbe record file");

    //Magic Marker, FileVersion, Num Records, Reserved
    if(bb.getInt() != MAGIC_MARKER)
      throw new IOException("file marker does not match");

    int fversion = bb.getInt();
    if(fversion != FILE_VERSION)
      throw new IOException("file version does not match: "+fversion+"::"+FILE_VERSION);

    numRecords = bb.getInt();
    reserved = bb.getInt();

    //map the record buffer
    mbb = fc.map(FileChannel.MapMode.READ_WRITE, indexOffset(), indexSize());
    indexBuffer = mbb.asIntBuffer();

    return true;
  }

  public VariableRecordFile(Path path) throws IOException{
    if(!open(path))
      throw new IOException("could not open file");
  }

  public VariableRecordFile(Path path, int initialRecords, int reserve) throws IOException{
    if(open(path)) return;
    p = path;
    //fc = FileChannel.open(p, StandardOpenOption.READ, StandardOpenOption.WRITE);
    fc = FileChannel.open(p, StandardOpenOption.CREATE_NEW, StandardOpenOption.READ, StandardOpenOption.WRITE);
    numRecords = initialRecords;
    reserved = reserve;

    //map the record buffer
    mbb = fc.map(FileChannel.MapMode.READ_WRITE, indexOffset(), indexSize());
    indexBuffer = mbb.asIntBuffer();
  }


  @Override
  public Map<Integer, Integer> compact() throws IOException {
    return null;
  }

  @Override
  public long fileSize() throws IOException{
    return fc.size();
  }

  @Override
  public boolean save() throws IOException {
    ByteBuffer bb = ByteBuffer.allocate(20);
    bb.putInt(MAGIC_MARKER);
    bb.putInt(FILE_VERSION);
    bb.putInt(numRecords);
    bb.putInt(reserved);
    bb.flip();
    fc.write(bb, headerOffset());
    return true;
  }

  @Override
  public void close() throws IOException {
    if(isOpen()) {
      save();
      mbb.force();
      fc.close();
    }
  }

  @Override
  public void clear() throws IOException {
    for(int i = 0; i < numRecords; i++){
      delete(i);
    }
    fc.truncate(indexOffset()+indexSize());
  }

  @Override
  public int size() {
    int s = 0;
    for(int i = 0; i < numRecords; i++){
      if(indexBuffer.get(i*2) != 0)
        s++;
    }
    return s;
  }

  @Override
  public int getBlockSize() {
    return 0;
  }

  @Override
  public int getFreeBlocks() {
    return numRecords - size();
  }

  @Override
  public void setReserve(byte[] bytes) throws IOException {
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    if (bytes.length > reserved) bb.limit(reserved);
    fc.write(bb, reservedOffset());
  }

  @Override
  public byte[] getReserve() throws IOException {
    if (reserved < 1) return null;
    ByteBuffer bb = ByteBuffer.allocate(reserved);
    fc.read(bb, reservedOffset());
    return bb.array();
  }

  @Override
  public MappedByteBuffer mapReserve() throws IOException, UnsupportedOperationException {
    return fc.map(FileChannel.MapMode.READ_WRITE, reservedOffset(), reserved);
  }

  @Override
  public int getFirstRecord() {
    for(int i = 0; i < numRecords; i++){
      int r = indexBuffer.get(i*2);
      if(r > 0) return i;
    }
    return -1;
  }

  @Override
  public byte[] get(int record) throws IOException {
    Idx idx = getIdx(record);
    if(idx == null) return null;
    ByteBuffer bb = ByteBuffer.allocate(idx.size);
    fc.read(bb, idx.offset);
    return bb.array();
  }

  @Override
  public boolean get(int record, byte[] buffer) throws IOException {
    Idx idx = getIdx(record);
    if(idx == null) return false;
    ByteBuffer bb = ByteBuffer.wrap(buffer);
    if(buffer.length > idx.size)
      bb.limit(idx.size);
    fc.read(bb, idx.offset);
    return true;
  }

  @Override
  public boolean update(int record, byte[] bytes, int offset, int length) throws IOException {
    Idx idx = getIdx(record);
    if(idx == null) return false;
    ByteBuffer bb = ByteBuffer.wrap(bytes, offset, length);
    if(length > idx.size) {
      idx.offset = nextRecordOffset();
    }
    idx.size = length;
    fc.write(bb, idx.offset);
    updateIdx(record, idx);
    return true;
  }

  @Override
  public int insert(byte[] bytes, int offset, int length) throws IOException {
    int record = nextFreeRecord();
    Idx idx = new Idx(nextRecordOffset(), length);
    if(length > 0) {
      ByteBuffer bb = ByteBuffer.wrap(bytes, offset, length);
      fc.write(bb, idx.offset);
    }
    updateIdx(record, idx);
    return record;
  }

  @Override
  public void insert(int record, byte[] bytes) throws IOException {
    if(record >= numRecords)
      throw new IOException("record out of range");
    Idx idx = getIdx(record);
    if(idx != null){
      update(record, bytes);
    } else {
      idx = new Idx(nextRecordOffset(), bytes.length);
      if(bytes.length > 0) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        fc.write(bb, idx.offset);
      }
      updateIdx(record, idx);
    }
  }

  @Override
  public boolean isOpen() {
    return fc.isOpen();
  }

  @Override
  public boolean delete(int record) throws IOException {
    if(contains(record)) {
      updateIdx(record, new NoIdx());
      return true;
    }
    return false;
  }

  @Override
  public boolean contains(int record) throws IOException {
    return getIdx(record) != null;
  }

  @Override
  public Iterator<Record> iterator() throws UnsupportedOperationException {
    return new VRFIterator();
  }

  @Override
  public Iterator<Record> iterator(int record) throws UnsupportedOperationException {
    return new VRFIterator(record);
  }

  protected long blocksOffset(){
    return indexOffset()+indexSize();
  }

  private int headerOffset(){return 0;}

  private int headerSize(){return 20;}

  private int reservedOffset(){return headerOffset()+headerSize();}

  private int reservedSize(){return reserved;}

  private int indexOffset(){return reservedOffset() + reservedSize();}

  private int indexSize() {return 8 * numRecords;}

  private long nextRecordOffset() throws IOException {
    long pos = fc.size();
    if(pos > MAX_OFFSET_START)
      throw new IOException("file offset to large");
    return pos;
  }

  private int nextFreeRecord() {
    for(int i = 0; i < numRecords; i++){
      int r = indexBuffer.get(i*2);
      if(r == 0) return i;
    }
    return -1;
  }

  private Idx getIdx(int record){
    int idxrecord = record * 2;
    long offset = Integer.toUnsignedLong(indexBuffer.get(idxrecord));
    if(offset == 0) return null;
    return new Idx(offset, indexBuffer.get(idxrecord+1));
  }

  private void updateIdx(int record, Idx idx){
    int idxrecord = record * 2;
    indexBuffer.put(idxrecord, (int) idx.offset);
    indexBuffer.put(idxrecord+1, idx.size);

  }

  class Idx {
    long offset;
    int size;

    public Idx(long offset, int size){
      this.offset = offset;
      this.size = size;
    }
  }

  class NoIdx extends Idx {
    public NoIdx(){
      super(0,0);
    }
  }

  class VRFIterator implements Iterator<Record> {

    Record nextR = null;
    int currIdx;

    public VRFIterator(){
      this(0);
    }

    public VRFIterator(int record){
      currIdx = record;
      getNext();
    }

    @Override
    public boolean hasNext() {
      return nextR != null;
    }

    @Override
    public Record next() {
      if(nextR == null) return null;
      Record toRet = nextR;
      getNext();
      return toRet;
    }

    private void getNext(){
      try {
        nextR = null;
        while (currIdx < numRecords) {
          byte b[] = get(currIdx++);
          if (b != null) {
            nextR = new Record(currIdx - 1, b);
            break;
          }
        }
      } catch(IOException e){
        throw new Error("something wrong: ",e);
      }
    }

  }


}
