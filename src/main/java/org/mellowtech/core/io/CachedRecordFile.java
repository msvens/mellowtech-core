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
import java.nio.MappedByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.cache.*;

/**
 * Date: 2013-03-24
 * Time: 12:09
 *
 * @author Martin Svensson
 */
@Deprecated
public class CachedRecordFile implements RecordFile {

  private final RecordFile file;
  private AbstractCache<Integer, byte[]> cache;
  //private int writeOps = 0, readOps = 0;

  public CachedRecordFile(RecordFile file, boolean readOnly, boolean mem, int size){
    this.file = file;
    setCache(readOnly, size, mem);
  }

  public void flush(){
    if(cache != null && !cache.isReadOnly()){
      CoreLog.L().finest("Closing Block File with "+cache.getCurrentSize()+" blocks in the cache");
      cache.emptyCache();
    }
  }

  @Override
  public Map<Integer, Integer> compact() throws IOException {
    flush();
    return file.compact();
  }

  @Override
  public boolean save() throws IOException {
    flush();
    return file.save();
  }

  @Override
  public void close() throws IOException {
    flush();
    file.close();
  }

  @Override
  public void clear() throws IOException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public int size() {
    return file.size();
  }

  @Override
  public int getBlockSize() {
    return file.getBlockSize();
  }

  @Override
  public int getFreeBlocks() {
    return file.getFreeBlocks();
  }

  @Override
  public void setReserve(byte[] bytes) throws IOException {
    file.setReserve(bytes);
  }
  
  @Override
  public MappedByteBuffer mapReserve() throws IOException {
    return file.mapReserve();
  }

  @Override
  public byte[] getReserve() throws IOException {
    return file.getReserve();
  }

  @Override
  public int getFirstRecord() {
    return 0;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public byte[] get(int record) throws IOException {

      try {
        return cache.get(record);
      } catch (NoSuchValueException e) {
        CoreLog.L().log(Level.SEVERE, "could not read block", e);
        throw new IOException(e.toString());
      }

  }

  @Override
  public boolean get(int record, byte[] buffer) throws IOException {

      try {
        byte b[] = cache.get(record);
        if(b != null){
          System.arraycopy(b, 0, buffer, 0, Math.min(b.length, buffer.length));
          return true;
        }
        return false;
      } catch (NoSuchValueException e) {
        CoreLog.L().log(Level.SEVERE, "could not read block", e);
        throw new IOException(e.toString());
      }

  }

  @Override
  public boolean update(int record, byte[] bytes) throws IOException {
    return update(record, bytes, 0, bytes.length);
  }

  @Override
  public boolean update(int record, byte[] bytes, int offset, int length) throws IOException {
    if(cache.isReadOnly()){
      cache.remove(record);
      return file.update(record, bytes, offset, length);
    }
    else{
      byte b[] = new byte[file.getBlockSize()];
      System.arraycopy(bytes, offset, b, 0, Math.min(file.getBlockSize(), length));
      cache.put(record, b);
      return true;
    }
  }

  @Override
  public int insert(byte[] bytes) throws IOException {
    return file.insert(bytes);
  }

  @Override
  public int insert(byte[] bytes, int offset, int length) throws IOException {
    return file.insert(bytes, offset, length);
  }

  @Override
  public void insert(int record, byte[] bytes) throws IOException {
    file.insert(record, bytes);
  }

  @Override
  public boolean delete(int record) throws IOException {
    cache.remove(record);
    return file.delete(record);
  }

  @Override
  public boolean contains(int record) throws IOException {
    return file.contains(record);
  }

  @Override
  public Iterator<Record> iterator() {
    return file.iterator();
  }

  @Override
  public Iterator<Record> iterator(int record) {
    return file.iterator(record);
  }

  public void setCache(boolean readOnly, int size, boolean mem){

    int numCacheItems = mem ? size / file.getBlockSize() : size;

    Remover<Integer, byte[]> remover = null;
    if(!readOnly){
      remover = new Remover<Integer, byte[]>() {
        @Override
        public void remove(Integer key, CacheValue<byte[]> value) {
          if(value.isDirty())
            try {
              file.update(key, value.getValue());
              //writeOps++;
            } catch (IOException e) {
              CoreLog.L().log(Level.SEVERE, "could not write block", e);
            }
        }
      };
    }

    Loader<Integer, byte[]> loader = new Loader<Integer, byte[]>() {
      @Override
      public byte[] get(Integer key) throws Exception, NoSuchValueException {
        byte b[] = file.get(key);
        //readOps++;
        return b;
      }
    };
    cache = new CacheLRU<Integer, byte[]> (remover, loader, numCacheItems);
  }
}
