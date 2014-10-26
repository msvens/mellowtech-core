/**
 * 
 */
package com.mellowtech.core.io;

import java.io.IOException;
import java.util.Iterator;

/**
 * A record file that adds an additional region of records
 * 
 * @author msvens
 *
 */
public interface SplitRecordFile extends RecordFile {
  
  public int sizeRegion();
  public int getBlockSizeRegion();
  public int getFreeBlocksRegion();
  
  public int getFirstRecordRegion();

  public byte[] getRegion(int record) throws IOException;
  public boolean getRegion(int record, byte[] buffer) throws IOException;

  public boolean updateRegion(int record, byte[] bytes) throws IOException;
  public boolean updateRegion(int record, byte[] bytes, int offset, int length) throws IOException;

  public int insertRegion(byte[] bytes) throws IOException;
  public int insertRegion(byte[] bytes, int offset, int length) throws IOException;
  public void insertRegion(int record, byte[] bytes) throws IOException;
  public boolean deleteRegion(int record) throws IOException;

  public boolean containsRegion(int record) throws IOException;

  public Iterator<Record> iteratorRegion() throws UnsupportedOperationException;
  public Iterator<Record> iteratorRegion(int record) throws UnsupportedOperationException;
  
}
