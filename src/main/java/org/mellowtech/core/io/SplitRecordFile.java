/**
 * 
 */
package org.mellowtech.core.io;

import java.io.IOException;
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

  default boolean updateRegion(int record, byte[] bytes) throws IOException{
    return updateRegion(record, bytes, 0, bytes.length);
  }

  boolean updateRegion(int record, byte[] bytes, int offset, int length) throws IOException;

  default int insertRegion(byte[] bytes) throws IOException {
    return insertRegion(bytes, 0, bytes != null ? bytes.length : 0);
  }
  int insertRegion(byte[] bytes, int offset, int length) throws IOException;
  void insertRegion(int record, byte[] bytes) throws IOException;
  boolean deleteRegion(int record) throws IOException;

  boolean containsRegion(int record) throws IOException;

  Iterator<Record> iteratorRegion() throws UnsupportedOperationException;
  Iterator<Record> iteratorRegion(int record) throws UnsupportedOperationException;
  
}
