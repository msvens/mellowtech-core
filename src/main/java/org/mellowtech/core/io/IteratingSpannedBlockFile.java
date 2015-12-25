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
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.Iterator;

/**
 * @deprecated
 * Complex implementation and error prone since It breaks the general contract of a record file.
 * Use VariableRecordFile instead
 *
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
  
  

  public IteratingSpannedBlockFile(Path p, int blockSize, int maxBlocks)
      throws IOException {
    super(p, blockSize, maxBlocks, (maxBlocks / 8) + 4);
    openRecords();
  }

  public IteratingSpannedBlockFile(Path p) throws IOException {
    super(p);
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
