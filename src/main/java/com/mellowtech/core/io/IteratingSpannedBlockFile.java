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
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.util.BitSet;
import java.util.Iterator;

/**
 * Date: 2013-04-06
 * Time: 12:27
 *
 * @author Martin Svensson
 */
public class IteratingSpannedBlockFile extends SpannedBlockFile{
  
  private BitSet records;
  private MappedByteBuffer mbb;
  
  private void openRecords() throws IOException{
    mbb = bf.mapReserve();
    int numLongs = mbb.getInt();
    if(numLongs < 1) {
      records = new BitSet();
    } else {
      LongBuffer lb = mbb.asLongBuffer();
      lb.limit(numLongs);
      records = BitSet.valueOf(lb);
    }
  }
  
  private void saveRecords() throws IOException{
    records.clear();
    long[] bits = records.toLongArray();
    mbb.putInt(bits.length);
    mbb.asLongBuffer().put(bits);
  }
  
  

  public IteratingSpannedBlockFile(String fileName, int blockSize, int maxBlocks)
      throws IOException {
    super(fileName, blockSize, maxBlocks, (maxBlocks / 8) + 4);
    openRecords();
  }

  public IteratingSpannedBlockFile(String fileName) throws IOException {
    super(fileName);
    openRecords();
  }
  
  public IteratingSpannedBlockFile(RecordFile rf) throws IOException {
    super(rf);
    openRecords();
  }

  @Override
  public boolean delete(int record) throws IOException {
    if(super.delete(record)){
      records.set(record, false);
      saveRecords();
      return true;
    }
    return false;
  }

  @Override
  public int insert(byte[] block, int offset, int length) throws IOException {
    int rec = super.insert(block, offset, length);
    records.set(rec, true);
    saveRecords();
    return rec;
  }

  @Override
  public Iterator<Record> iterator() throws UnsupportedOperationException {
    return new SpannedBlockFileIterator(null);
  }

  @Override
  public Iterator<Record> iterator(int record) throws UnsupportedOperationException {
    return new SpannedBlockFileIterator(record);
  }

  @Override
  public MappedByteBuffer mapReserve() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setReserve(byte[] b) {
    throw new UnsupportedOperationException();
  }
  
  
  class SpannedBlockFileIterator implements Iterator<Record>{
    Record next = null;
    Integer nextRec;
    int record;
    boolean stop = false;
    int currIndex;

    public SpannedBlockFileIterator(Integer from){
      record = from != null ? from : 0;
      getNext();

    }

    private void getNext(){
      if(record < 0) return;
      record = records.nextSetBit(record);
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
