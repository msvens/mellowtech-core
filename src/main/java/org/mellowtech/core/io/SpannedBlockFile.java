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
import java.nio.MappedByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

/**
 * @deprecated
 * The SpannedBlockFile is complex in its implementation and unsafe in usage.
 *
 *
 * Date: 2013-04-06
 * Time: 12:27
 *
 * @author Martin Svensson
 */
@Deprecated
public class SpannedBlockFile implements RecordFile{

  protected RecordFile bf;

  public SpannedBlockFile(RecordFile rf){
    this.bf = rf;
  }

  public SpannedBlockFile(Path p) throws IOException {
    bf = new BlockFile(p);
  }

  public SpannedBlockFile(Path p, int blockSize, int maxBlocks, int reserve) throws IOException{
    bf = new BlockFile(p, blockSize, maxBlocks, reserve);
  }

  @Override
  public void clear() throws IOException {
    bf.clear();
  }

  @Override
  public void close() throws IOException{
    bf.close();
  }

  @Override
  public Map<Integer, Integer> compact() throws IOException {
    return bf.compact();
  }
  
  @Override
  public boolean contains(int record) throws IOException {
    return bf.contains(record);
  }
  
  private void delButFirst(int record) throws IOException{
    //Delete record
    ByteBuffer bb;
    byte b[] = bf.get(record);
    bb = ByteBuffer.wrap(b);
    bb.getInt(); //length
    int next = bb.getInt();
    //bf.delete(record);
    while(next != -1){
      bb = ByteBuffer.wrap(bf.get(next));
      bf.delete(next);
      next = bb.getInt();
    }
  }
  
  @Override
  public boolean delete(int record) throws IOException{
    if(!bf.contains(record)) return false;
    //Delete record
    ByteBuffer bb;
    byte b[] = bf.get(record);
    bb = ByteBuffer.wrap(b);
    bb.getInt(); //length
    int next = bb.getInt();
    boolean toRet;
    toRet = bf.delete(record);
    while(next != -1){
      bb = ByteBuffer.wrap(bf.get(next));
      toRet = bf.delete(next);
      next = bb.getInt();
    }
    return toRet;
  }

  @Override
  public void deleteAll() throws IOException{
    clear();
  }
  
  @Override
  public byte[] get(int record) throws IOException{
    ByteBuffer bb;
    //read first block
    byte b[] = bf.get(record);
    if(b == null) return null;
    bb = ByteBuffer.wrap(b);
    int length = bb.getInt();
    int nextBlock = bb.getInt();

    byte toRet[] = new byte[length];
    int bs = bf.getBlockSize();
    int offset = 0;
    int toRead = length >= bs - 8 ? bs - 8 : length;

    System.arraycopy(b, 8, toRet, offset, toRead);

    offset += toRead;

    while(nextBlock != -1){

      b = bf.get(nextBlock);
      bb = ByteBuffer.wrap(b);
      nextBlock = bb.getInt();
      toRead = length >= offset + bs - 4 ? bs - 4 : length - offset;
      System.arraycopy(b, 4, toRet, offset, toRead);
      offset += toRead;
    }

    return toRet;
  }

  @Override
  public ByteBuffer getMapped(int record) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean get(int record, byte[] buffer) throws IOException {
    ByteBuffer bb;
    //read first block
    byte b[] = bf.get(record);
    if(b == null) return false;
    bb = ByteBuffer.wrap(b);
    int length = bb.getInt();
    int nextBlock = bb.getInt();

    //byte toRet[] = new byte[length];
    int bs = bf.getBlockSize();
    int offset = 0;
    int toRead = length >= bs - 8 ? bs - 8 : length;

    System.arraycopy(b, 8, buffer, offset, toRead);

    offset += toRead;

    while(nextBlock != -1){

      b = bf.get(nextBlock);
      bb = ByteBuffer.wrap(b);
      nextBlock = bb.getInt();
      toRead = length >= offset + bs - 4 ? bs - 4 : length - offset;
      System.arraycopy(b, 4, buffer, offset, toRead);
      offset += toRead;
    }
    return true;
  }

  @Override
  public int getBlockSize() {
    return bf.getBlockSize();
  }
  
  @Override
  public int getFirstRecord() {
    return bf.getFirstRecord();
  }

  @Override
  public int getFreeBlocks(){
    return bf.getFreeBlocks();
  }

  @Override
  public byte[] getReserve() throws IOException {
    return bf.getReserve();
  }

  @Override
  public int insert(byte[] block, int offset, int length) throws IOException{

    int numBlocks = numBlocks(block);

    if(bf.getFreeBlocks() < numBlocks)
      throw new IOException("not enough space to store block");

    //store data:
    int currBlock = bf.insert(null);
    int toRet = currBlock;
    int nextBlock = numBlocks > 1 ? bf.insert(null) : -1;
    //int offset = 0;
    int bs = bf.getBlockSize();
    ByteBuffer bb = ByteBuffer.allocate(bs);

    //first block (store size as well)
    int toStore = length > bs - 8 ? bs - 8 : length;
    bb.putInt(length);
    bb.putInt(nextBlock);
    bb.put(block, 0, toStore);
    bf.update(currBlock, bb.array());
    offset += toStore;

    while(nextBlock != -1){
      currBlock = nextBlock;
      toStore = offset + bs - 4 > length ? length - offset : bs - 4;
      bb.clear();
      nextBlock = offset + toStore >= length ? -1 : bf.insert(null);
      bb.putInt(nextBlock);
      bb.put(block, offset, toStore);
      offset+=toStore;
      bf.update(currBlock, bb.array(), 0, toStore+4);
    }
    return toRet;
  }

  @Override
  public int insert(byte[] bytes) throws IOException {
    return insert(bytes, 0, bytes.length);
  }

  @Override
  public void insert(int record, byte[] bytes) throws IOException {
    throw new IOException("cannot insert at specific record");
  }

  @Override
  public boolean isOpen() {
    return bf.isOpen();
  }

  @Override
  public Iterator<Record> iterator() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<Record> iterator(int record) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public MappedByteBuffer mapReserve() throws IOException {
    return bf.mapReserve();
  }

  private int numBlocks(byte[] b){
    double bSize = b != null ? b.length : bf.getBlockSize();
    return (int) Math.ceil(bSize / (double) bf.getBlockSize());
  }
  
  @Override
  public boolean save() throws IOException{
    return bf.save();
  }

  @Override
  public void setReserve(byte[] b) throws IOException {
    bf.setReserve(b);
  }

  @Override
  public int size(){
    return bf.size();
  }

  @Override
  public long fileSize() throws IOException {
    return 0;
  }

  @Override
  public boolean update(int record, byte[] block) throws IOException{
    return update(record, block, 0, block.length);
  }

  @Override
  public boolean update(int record, byte[] block, int offset, int length) throws IOException{
    if(!bf.contains(record)) return false;
    delButFirst(record);
    int numBlocks = numBlocks(block);
    if(bf.getFreeBlocks() < numBlocks - 1){ //we alrady have one block allocated
      bf.delete(record);
      throw new IOException("not enough space to store block");
    }

    //store data:
    int currBlock = record;
    //int toRet = currBlock;
    int nextBlock = numBlocks > 1 ? bf.insert(null) : -1;
    //int offset = 0;
    int bs = bf.getBlockSize();
    ByteBuffer bb = ByteBuffer.allocate(bs);

    //first block (store size as well)
    int toStore = length > bs - 8 ? bs - 8 : length;
    bb.putInt(length);
    bb.putInt(nextBlock);
    bb.put(block, 0, toStore);

    bf.update(currBlock, bb.array());
    offset += toStore;

    while(nextBlock != -1){
      currBlock = nextBlock;
      toStore = offset + bs - 4 > length ? length - offset : bs - 4;
      bb.clear();
      nextBlock = offset + toStore >= length ? -1 : bf.insert(null);
      bb.putInt(nextBlock);
      bb.put(block, offset, toStore);
      offset += toStore;
      bf.update(currBlock, bb.array(), 0, toStore+4);
    }
    return true;
  }


}
