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
import java.nio.MappedByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A file that that stores byte records. Implementations of this class is free
 * to use any number of underlying files
 *
 * Date: 2013-03-11
 * Time: 07:55
 *
 * @author Martin Svensson
 */
public interface RecordFile {


  void clear() throws IOException;

  void close() throws IOException;

  Map<Integer, Integer> compact() throws IOException;

  boolean contains(int record) throws IOException;

  boolean delete(int record) throws IOException;

  default void forEach(Consumer<Record> action){
    Iterator <Record> iter = iterator();
    while(iter.hasNext()){
      action.accept(iter.next());
    }
  }

  default byte[] get(int record) throws IOException{
    byte[] bytes = new byte[getBlockSize()];
    return get(record, bytes) ? bytes : null;
  }

  boolean get(int record, byte[] buffer) throws IOException;

  int getBlockSize();

  int getFirstRecord();

  int getFreeBlocks();

  byte[] getReserve() throws IOException, UnsupportedOperationException;

  default int insert(byte[] bytes) throws IOException{
    return insert(bytes, 0, bytes != null ? bytes.length : -1);
  }

  int insert(byte[] bytes, int offset, int length) throws IOException;

  void insert(int record, byte[] bytes) throws IOException;

  boolean isOpen();

  Iterator<Record> iterator() throws UnsupportedOperationException;

  Iterator<Record> iterator(int record) throws UnsupportedOperationException;

  MappedByteBuffer mapReserve() throws IOException, UnsupportedOperationException;

  boolean save() throws IOException;

  void setReserve(byte[] bytes) throws IOException, UnsupportedOperationException;

  /**
   * Get the number of records currently stored
   * @return records
   */
  int size();

  long fileSize() throws IOException;

  default boolean update(int record, byte[] bytes) throws IOException{
    return update(record, bytes, 0, bytes.length);
  }
  
  boolean update(int record, byte[] bytes, int offset, int length) throws IOException;





}
