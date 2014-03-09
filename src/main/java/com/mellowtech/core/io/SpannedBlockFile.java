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

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Date: 2013-04-06
 * Time: 12:27
 *
 * @author Martin Svensson
 */
public class SpannedBlockFile{

  private RecordFile bf;

  public SpannedBlockFile(String fileName) throws IOException {
    bf = new BlockFile(fileName);
  }

  public SpannedBlockFile(String fileName, int blockSize, int maxBlocks, int reserve) throws IOException{
    bf = new BlockFile(fileName, blockSize, maxBlocks, reserve);
  }

  public SpannedBlockFile(RecordFile rf){
    this.bf = rf;
  }

  public int getFreeBlocks(){
    return bf.getFreeBlocks();
  }

  public int size(){
    return bf.size();
  }

  public void save() throws IOException{
    bf.save();
  }

  public int insertBlock(byte[] block) throws IOException{

    int numBlocks = numBlocks(block);

    if(bf.getFreeBlocks() < numBlocks)
      throw new IOException("not enough space to store block");

    //store data:
    int currBlock = bf.insert(null);
    int toRet = currBlock;
    int nextBlock = numBlocks > 1 ? bf.insert(null) : -1;
    int offset = 0;
    int bs = bf.getBlockSize();
    ByteBuffer bb = ByteBuffer.allocate(bs);

    //first block (store size as well)
    int toStore = block.length > bs - 8 ? bs - 8 : block.length;
    bb.putInt(block.length);
    bb.putInt(nextBlock);
    bb.put(block, 0, toStore);
    bf.update(currBlock, bb.array());
    offset += toStore;

    while(nextBlock != -1){
      currBlock = nextBlock;
      toStore = offset + bs - 4 > block.length ? block.length - offset : bs - 4;
      bb.clear();
      nextBlock = offset + toStore >= block.length ? -1 : bf.insert(null);
      bb.putInt(nextBlock);
      bb.put(block, offset, toStore);
      offset+=toStore;
      bf.update(currBlock, bb.array(), 0, toStore+4);
    }
    return toRet;
  }

  public void update(int record, byte[] block) throws IOException{
    delButFirst(record);
    int numBlocks = numBlocks(block);
    if(bf.getFreeBlocks() < numBlocks - 1){ //we alrady have one block allocated
      bf.delete(record);
      throw new IOException("not enough space to store block");
    }

    //store data:
    int currBlock = record;
    int toRet = currBlock;
    int nextBlock = numBlocks > 1 ? bf.insert(null) : -1;
    int offset = 0;
    int bs = bf.getBlockSize();
    ByteBuffer bb = ByteBuffer.allocate(bs);

    //first block (store size as well)
    int toStore = block.length > bs - 8 ? bs - 8 : block.length;
    bb.putInt(block.length);
    bb.putInt(nextBlock);
    bb.put(block, 0, toStore);

    bf.update(currBlock, bb.array());
    offset += toStore;

    while(nextBlock != -1){
      currBlock = nextBlock;
      toStore = offset + bs - 4 > block.length ? block.length - offset : bs - 4;
      bb.clear();
      nextBlock = offset + toStore >= block.length ? -1 : bf.insert(null);
      bb.putInt(nextBlock);
      bb.put(block, offset, toStore);
      offset += toStore;
      bf.update(currBlock, bb.array(), 0, toStore+4);
    }
  }

  public byte[] get(int record) throws IOException{
    ByteBuffer bb;
    //read first block
    byte b[] = bf.get(record);
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

  public void del(int record) throws IOException{
    //Delete record
    ByteBuffer bb;
    byte b[] = bf.get(record);
    bb = ByteBuffer.wrap(b);
    int length = bb.getInt();
    int next = bb.getInt();
    bf.delete(record);
    while(next != -1){
      bb = ByteBuffer.wrap(bf.get(next));
      bf.delete(next);
      next = bb.getInt();
    }
  }

  public void delButFirst(int record) throws IOException{
    //Delete record
    ByteBuffer bb;
    byte b[] = bf.get(record);
    bb = ByteBuffer.wrap(b);
    int length = bb.getInt();
    int next = bb.getInt();
    //bf.delete(record);
    while(next != -1){
      bb = ByteBuffer.wrap(bf.get(next));
      bf.delete(next);
      next = bb.getInt();
    }
  }

  private int numBlocks(byte[] b){
    double bSize = b != null ? b.length : bf.getBlockSize();
    return (int) Math.ceil(bSize / (double) bf.getBlockSize());
  }


}
