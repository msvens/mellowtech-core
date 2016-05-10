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

package org.mellowtech.core.io.impl;

import org.mellowtech.core.io.Record;
import org.mellowtech.core.io.RecordFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;

/**
 * @author msvens
 * @since 19/03/16
 */
public class MultiBlockFile implements RecordFile {

  public final static int DEFAULT_FILE_SIZE = 1024*1024*64;
  public final static int DEFAULT_BLOCK_SIZE = 1024*8;
  private static final int DELETED_BLOCK = Integer.MIN_VALUE;
  final Path dir;
  final String name;

  private int high = 0;

  private final SortedMap<Integer, FileRecord> files;
  private final int fileSize;
  private final int blockSize;
  private final int blocksPerFile;
  private boolean opened = false;
  private final int reserve;
  private FileChannel fc = null;

  public MultiBlockFile(int blockSize, Path name) throws IOException {
    this(DEFAULT_FILE_SIZE, blockSize, 0, name);
  }

  public MultiBlockFile(int fileSize, int blockSize, int reserve, Path name) throws IOException{
    files = new TreeMap <> ();
    this.dir = name.getParent();
    this.name = name.getFileName().toString();
    this.fileSize = fileSize;
    this.blockSize = blockSize;
    this.blocksPerFile = fileSize / blockSize;
    this.reserve = reserve < 0 ? 0 : reserve;
    open();
  }

  @Override
  public void clear() throws IOException {
    for(FileRecord fr : files.values()){
      fr.delete();
    }
    files.clear();
    files.put(0, createDataFile(0));
    high = 0;
  }

  @Override
  public void close() throws IOException {
    for(FileRecord fr : files.values())
      fr.close();
    files.clear();
    opened = false;
  }

  @Override
  public Map<Integer, Integer> compact() throws IOException {
    return null;
  }

  @Override
  public boolean contains(int record) throws IOException {
    return getMapped(record) != null;

  }

  @Override
  public boolean delete(int record) throws IOException {
    ByteBuffer bb = getMapped(record);
    if(bb == null)
      return false;
    bb.putInt(0, DELETED_BLOCK);
    return true;
  }

  @Override
  public void remove() throws IOException {
    for(FileRecord fr : files.values()){
      fr.delete();
    }
    files.clear();
    if(fc != null) {
      fc.close();
      Files.delete(dir.resolve(name));
    }
    opened = false;
  }

  @Override
  public MappedByteBuffer getMapped(int record) throws UnsupportedOperationException {
    if(record >= high){
      return null;
    }
    FileIdRec fid = new FileIdRec(record);
    FileRecord fr = files.get(fid.fileId);
    if(fr == null){
      throw new Error("file did not exist");
    }
    MappedByteBuffer mbb = fr.slice(fid.rec);
    return isDeleted(mbb) ? null : mbb;
  }

  @Override
  public boolean get(int record, byte[] buffer) throws IOException {
    ByteBuffer bb = getMapped(record);
    if(bb == null) return false;
    if (buffer.length > getBlockSize())
      bb.get(buffer, 0, getBlockSize());
    else
      bb.get(buffer);
    return true;
  }

  @Override
  public int getBlockSize() {
    return blockSize;
  }

  @Override
  public int getFirstRecord() {
    Iterator <Record> iter = iterator();
    if(iter.hasNext()){
      return iter.next().record;
    } else
      return -1;
  }

  @Override
  public int getFreeBlocks() {
    return Integer.MAX_VALUE - size();
  }

  @Override
  public byte[] getReserve() throws IOException, UnsupportedOperationException {
    if(reserve < 1) return null;
    ByteBuffer bb = ByteBuffer.allocate(reserve);
    fc.read(bb,0);
    return bb.array();
  }

  @Override
  public int insert(byte[] bytes, int offset, int length) throws IOException {
    int rec = high;
    insert(rec, bytes, offset, length);
    return rec;
  }

  @Override
  public void insert(int record, byte[] bytes, int offset, int length) throws IOException {
    FileIdRec fid = new FileIdRec(record);
    while(fid.fileId > files.lastKey()){
      int newFile = files.lastKey() + blocksPerFile;
      files.put(newFile, createDataFile(newFile));
    }
    FileRecord fr = files.get(fid.fileId);
    ByteBuffer bb = fr.slice(fid.rec);
    if(bytes != null && length > 0) {
      bb.put(bytes, offset, length > getBlockSize() ? getBlockSize() : length);
    }
    else { //just override the deleted marker
      bb.putInt(Integer.MAX_VALUE);
    }
    if(record >= high)
      high = record + 1;
  }

  @Override
  public boolean isOpen() {
    return opened;
  }

  @Override
  public Iterator<Record> iterator() throws UnsupportedOperationException {
    return new MBFIterator();
  }

  @Override
  public Iterator<Record> iterator(int record) throws UnsupportedOperationException {
    return new MBFIterator(record);
  }

  @Override
  public MappedByteBuffer mapReserve() throws IOException, UnsupportedOperationException {
    return fc.map(FileChannel.MapMode.READ_WRITE, 0, reserve);
  }

  @Override
  public boolean save() throws IOException {
    for(FileRecord fr : files.values())
      fr.save();
    return true;
  }

  @Override
  public void setReserve(byte[] bytes) throws IOException, UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    int size = 0;
    Iterator<Record> iter = iterator();
    while(iter.hasNext()){
      size++;
      iter.next();
    }
    return size;
  }

  @Override
  public long fileSize() throws IOException {
    return files.size() * fileSize;
  }

  @Override
  public boolean update(int record, byte[] bytes, int offset, int length) throws IOException {
    if(record >= high)
      return false;
    FileIdRec fid = new FileIdRec(record);
    FileRecord fr = files.get(fid.fileId);
    ByteBuffer bb = fr.slice(fid.rec);
    if(bytes != null && length > 0)
      bb.put(bytes, offset, length > getBlockSize() ? getBlockSize() : length);
    return true;
  }

  private void open() throws IOException {
    if(Files.notExists(dir)){
      Files.createDirectory(dir);
    } else if(Files.isRegularFile(dir)){
      throw new IOException(dir+" is not a directory");
    }
    if(reserve > 0){
      fc = FileChannel.open(dir.resolve(name), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
      if(fc.size() == 0){
        fc.write(ByteBuffer.allocate(1),reserve-1);
      }
    }
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*-"+name)) {
      for(Path p : ds){
        String fname = p.getFileName().toString();
        fname = fname.substring(0,fname.length()-1-name.length());
        Integer rec = Integer.parseInt(fname);
        files.put(rec, new FileRecord(p,true,fileSize));
      }
    } catch(DirectoryIteratorException e){
      throw new IOException(e);
    }
    if(files.isEmpty()){
      //files.put(blocksPerFile(), new FileRecord(dir.resolve(blocksPerFile()+FILE_EXT),true,fileSize));
      files.put(0, createDataFile(0));
      opened = true;
      high = 0;
      return;
    }
    opened = true;
    Integer lastFile = files.lastKey();
    FileRecord fr = files.get(lastFile);
    int lastBlock = getLastBlockInDataFile(fr);
    lastBlock = lastBlock == -1 ? 0 : lastBlock + 1;
    high = lastFile + lastBlock;
  }



  private int getLastBlockInDataFile(FileRecord fr) throws IOException {
    MappedByteBuffer bb = fr.get();
    int blockNo = -1;
    for(int i = 0; i < blocksPerFile; i++){
      if(bb.getInt(i*getBlockSize()) != DELETED_BLOCK)
        blockNo = i;
    }
    return blockNo;
  }

  private boolean isDataFileEmpty(FileRecord fr) throws IOException {
    MappedByteBuffer bb = fr.get();
    for(int i = 0; i < blocksPerFile; i++){
      int marker = bb.getInt(i*getBlockSize());
      if(marker != DELETED_BLOCK)
        return false;
    }
    return true;
  }

  private FileRecord createDataFile(int blockStart) throws IOException{
    FileRecord fr = new FileRecord(dir.resolve(blockStart+"-"+name),true,fileSize);
    MappedByteBuffer bb = fr.get();
    for(int b = 0; b < blocksPerFile; b++){
      bb.putInt(b*getBlockSize(),DELETED_BLOCK);
    }
    return fr;
  }

  private void writeDeleted(ByteBuffer bb){
    bb.putInt(0, DELETED_BLOCK);
  }

  private boolean isDeleted(ByteBuffer bb){
    return bb.getInt(0) == DELETED_BLOCK;
  }

  class MBFIterator implements Iterator<Record>{

    private int nextRec;
    Record next = null;

    public MBFIterator(){
      this(0);
    }
    public MBFIterator(int record){
      nextRec = record;
      try {
        next = getNextRec();
      } catch(IOException e){
        throw new Error(e);
      }
    }



    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public Record next() {
      try {
        Record toRet = next;
        next = getNextRec();
        return toRet;
      }catch(IOException e){
        throw new Error(e);
      }
    }

    private Record getNextRec() throws IOException{
      Record r = null;
      if(nextRec >= high)
        return null;
      while(nextRec < high){
        r = new Record(nextRec, get(nextRec));
        nextRec++;
        if(r.data != null)
          break;
      }
      return r != null && r.data != null ? r : null;
    }
  }

  class FileIdRec {
    public final int fileId;
    public final int rec;

    public FileIdRec(int record){
      fileId = (record / blocksPerFile) * blocksPerFile;
      rec = record % blocksPerFile;
    }
  }


  class FileRecord {
    final Path path;
    MappedByteBuffer fileBuffer;
    FileChannel fc;
    final int size;

    FileRecord(final Path p, final boolean open, final int fileSize) throws IOException{
      path = p;
      size = fileSize;
      if(open) open();
    }

    public MappedByteBuffer get() {
      try {
        if (isClosed()) open();
        return fileBuffer;
      }catch (IOException e){
        throw new Error(e);
      }
    }

    public MappedByteBuffer sliceTo(int record, int size){
      return slice(record*blockSize,size);
    }

    public MappedByteBuffer slice(int record){
      return slice(record*blockSize, blockSize);
    }

    public MappedByteBuffer slice(int start, int size){
      MappedByteBuffer bb = (MappedByteBuffer) get().duplicate();
      bb.position(start);
      bb.limit(bb.position()+size);
      return (MappedByteBuffer) bb.slice();
    }

    public void close() throws IOException{
      if(fc != null) {
        save();
        fc.close();
      }
      fileBuffer = null;
      fc = null;
    }

    public void delete() throws IOException {
      if(fc != null)
        fc.close();
      fileBuffer = null;
      Files.delete(path);
    }

    public void save() {
      if(fc != null && fileBuffer != null){
        fileBuffer.force();
      }
    }

    private boolean isClosed() {
      return fc == null || !fc.isOpen();
    }

    private void open() throws IOException{
      if(Files.notExists(path)){
        fc = FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.READ, StandardOpenOption.WRITE);
      } else {
        fc = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
      }
      fileBuffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
    }

  }
}
