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
