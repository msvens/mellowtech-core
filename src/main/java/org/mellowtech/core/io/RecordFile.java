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

  default boolean update(int record, byte[] bytes) throws IOException{
    return update(record, bytes, 0, bytes.length);
  }
  
  boolean update(int record, byte[] bytes, int offset, int length) throws IOException;





}
