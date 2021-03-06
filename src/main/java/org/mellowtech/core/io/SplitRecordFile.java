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
import java.util.Iterator;

/**
 * A record file that adds an additional region of records
 * 
 * @author msvens
 *
 */
public interface SplitRecordFile extends RecordFile {
  
  int sizeRegion();
  int getBlockSizeRegion();
  int getFreeBlocksRegion();
  
  int getFirstRecordRegion();

  default byte[] getRegion(int record) throws IOException{
    byte b[] = new byte[getBlockSizeRegion()];
    return getRegion(record, b) ? b : null;
  }

  boolean getRegion(int record, byte[] buffer) throws IOException;

  default MappedByteBuffer getRegionMapped(int record) throws UnsupportedOperationException{
    throw new UnsupportedOperationException();
  }

  default boolean updateRegion(int record, byte[] bytes) throws IOException{
    return updateRegion(record, bytes, 0, bytes.length);
  }

  boolean updateRegion(int record, byte[] bytes, int offset, int length) throws IOException;

  default int insertRegion(byte[] bytes) throws IOException {
    return insertRegion(bytes, 0, bytes != null ? bytes.length : 0);
  }
  int insertRegion(byte[] bytes, int offset, int length) throws IOException;
  default void insertRegion(int record, byte[] bytes) throws IOException{
    insertRegion(record, bytes, 0, bytes.length);
  }
  void insertRegion(int record, byte[] bytes, int offset, int length) throws IOException;
  boolean deleteRegion(int record) throws IOException;
  void deleteAllRegion() throws IOException;
  void deleteAll() throws IOException;

  boolean containsRegion(int record) throws IOException;

  Iterator<Record> iteratorRegion() throws UnsupportedOperationException;
  Iterator<Record> iteratorRegion(int record) throws UnsupportedOperationException;
  
}
