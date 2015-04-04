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
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * Simple disc-based integer array. This class effectivly is an array of ints
 * that is stored on disc. Furthermore this is a dynamic array, meaning that it
 * is always as large as the number of elements in it.
 * 
 * @author rickard.coster@asimus.se
 */
public class IntArrayFile {
  private boolean failSafe;
  private FileChannel fChannel;
  private RandomAccessFile file;
  private ByteBuffer buffer;
  private IntBuffer intBuffer;
  private int highRecord;
  private boolean direct;
  private int[] tmpArr;

  /**
   * Creates a new disc based int array.
   * 
   * @param fileName
   *          file name of this array
   * @param failSafe
   *          always write values to disc
   * @param deleteOldFile
   *          if false the old (if it exists) array will be used
   * @param direct
   *          use direct allocation for the buffer holding the array.
   * @see ByteBuffer
   * @exception IOException
   *              if an error occurs
   */
  public IntArrayFile(String fileName, boolean failSafe, boolean deleteOldFile,
      boolean direct) throws IOException {
    file = new RandomAccessFile(fileName, "rw");
    if (deleteOldFile)
      file.setLength(0);
    fChannel = file.getChannel();
    this.direct = direct;
    int length = fChannel.size() == 0 ? 256 * 4 : (int) fChannel.size();
    buffer = createBuffer(length);
    intBuffer = buffer.asIntBuffer();
    if (fChannel.size() > 0) {
      fChannel.read(buffer);
      highRecord = length / 4;
    }
    else
      highRecord = 0;
    this.failSafe = failSafe;
    tmpArr = new int[256];
  }

  /**
   * Close this array, i.e write all data to file
   * 
   * @exception IOException
   *              if an error occurs
   */
  public void close() throws IOException {
    if (!failSafe && highRecord > 0) {
      buffer.position(0);
      buffer.limit(highRecord * 4);
      fChannel.write(buffer, 0);
    }
    file.close();
  }

  /**
   * (Follows the general contract in Arrays) Searches the array for the
   * specified value using the binary search algorithm. The array <strong>must</strong>
   * be sorted (as by the <tt>sort</tt> method, below) prior to making this
   * call. If it is not sorted, the results are undefined. If the array contains
   * multiple elements with the specified value, there is no guarantee which one
   * will be found.
   * 
   * @param key
   *          the value to be searched for.
   * @return index of the search key, if it is contained in the array;
   *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>. The
   *         <i>insertion point</i> is defined as the point at which the key
   *         would be inserted into the list: the index of the first element
   *         greater than the key, or <tt>list.size()</tt>, if all elements
   *         in the list are less than the specified key. Note that this
   *         guarantees that the return value will be &gt;= 0 if and only if the
   *         key is found.
   * @see Arrays#sort(long[])
   */
  public int binarySearch(int key) {
    int low = 0;
    int high = highRecord - 1;
    while (low <= high) {
      int mid = (low + high) >> 1;
      int midVal = intBuffer.get(mid);
      if (midVal < key)
        low = mid + 1;
      else if (midVal > key)
        high = mid - 1;
      else
        return mid; // key found
    }
    return -(low + 1); // key not found.
  }

  public void sort() throws IOException {
    int[] arr = intBuffer.array();
    Arrays.sort(arr);
    intBuffer.reset();
    intBuffer.put(arr);
    doFailSafe(0, false, true);
  }

  /**
   * Get the value of a position in the array
   * 
   * @param record
   *          position
   * @return an the value
   * @exception IOException
   *              if an error occurs
   */
  public int get(int record) throws IOException {
    if (record < 0 || record >= highRecord)
      throw new IOException("Record out of bounds " + record + " " + highRecord);
    return intBuffer.get(record);
  }

  /**
   * Check if a record is within the bounds of this array.
   * 
   * @param record
   *          the record or index
   * @return true if the recored is not out of bounds
   */
  public boolean contains(int record) {
    return record >= 0 && record < highRecord;
  }

  /**
   * Set the value of a postion in this array. If this is a failSafe array the
   * value will be written immediatly to disc
   * 
   * @param record
   *          position or index
   * @param value
   *          the new value
   * @exception IOException
   *              if an error occurs
   */
  public void updateRecord(int record, int value) throws IOException {
    if (record < 0 || record >= highRecord)
      throw new IOException("Record out of bounds");
    intBuffer.put(record, value);
    doFailSafe(record, true, false);
  }

  /**
   * Return the length of the array.
   * 
   * @return length
   * @exception IOException
   *              if an error occurs
   */
  public int numRecords() throws IOException {
    return highRecord;
  }

  /**
   * Delete a position in this array.
   * 
   * @param record
   *          the recored to delete.
   * @exception IOException
   *              if an error occurs
   * @return the deleted value
   */
  public int delete(int record) throws IOException {
    if (record < 0 || record >= highRecord)
      throw new IOException("Record out of bounds " + record + " highRecord: "
          + highRecord);
    int retVal = get(record);
    highRecord--;
    if (record < highRecord) { // we have to move pointers
      int length = highRecord - record;
      if (length > tmpArr.length)
        tmpArr = new int[length];
      intBuffer.position(record + 1);
      intBuffer.get(tmpArr, 0, length);
      intBuffer.position(record);
      intBuffer.put(tmpArr, 0, length);
    }
    doFailSafe(record, false, true); // only need to shrink file:
    return retVal;
  }

  /**
   * Adds a record to the end of this array.
   * 
   * @param value
   *          value for the new record
   * @exception IOException
   *              if an error occurs
   */
  public void put(int value) throws IOException {
    put(highRecord, value);
  }

  /**
   * Adds a record to this array. If the record was previously taken the array
   * is shifted to the right to make room for the new record, i.e we want to
   * insert 34 in record 1<br>
   * 1 9 8 yields 1 34 9 8
   * 
   * @param record
   *          the new record
   * @param value
   *          the valud for that record
   * @exception IOException
   *              if an error occurs
   */
  public void put(int record, int value) throws IOException {
    // highRecord++;
    if (record > highRecord)
      highRecord = record;
    if (highRecord >= intBuffer.capacity()) {
      ByteBuffer tmpBuff = createBuffer((highRecord * 4) * 2);
      buffer.position(0);
      tmpBuff.put(buffer);
      buffer = tmpBuff;
      buffer.position(0);
      intBuffer = buffer.asIntBuffer();
    }
    // make room for new record if needed:
    if (record < highRecord) {
      int length = highRecord - record;
      if (length > tmpArr.length)
        tmpArr = new int[length];
      intBuffer.position(record);
      intBuffer.get(tmpArr, 0, length);
      intBuffer.position(record + 1);
      intBuffer.put(tmpArr, 0, length);
      intBuffer.put(record, value);
      // highRecord++;
      // doFailSafe(record, false, true);
      // return;
    }
    else {
      intBuffer.put(record, value);
    }
    highRecord++;
    doFailSafe(record, true, true);
  }

  /**
   * d * Count the values up to (not including) record.
   * 
   * @param record
   *          the number of records to count
   * @return the value for the records
   */
  public int count(int record) throws IOException{
      int count = 0;
      for (int i = 0; i < record; i++)
        count += get(i);
      return count;
  }

  /**
   * Count the values of all record in this array
   * 
   * @return the added values
   */
  public int count() throws IOException{
    return count(highRecord);
  }

  public String toString() {
    try {
      StringBuilder sbuff = new StringBuilder();
      for (int i = 0; i < highRecord; i++)
        sbuff.append("[").append(i).append(",").append(get(i)).append("] ");
      sbuff.append("\n");
      return sbuff.toString();
    }
    catch (IOException e) {
      return "";
    }
  }

  private ByteBuffer createBuffer(int size) {
    // ByteBuffer bb;
    if (direct)
      return ByteBuffer.allocate(size);
    return ByteBuffer.allocate(size);
  }

  private void doFailSafe(int record, boolean onlyRecord, boolean resize)
      throws IOException {
    if (!failSafe)
      return;
    int recpos = record * 4;
    buffer.limit((onlyRecord) ? recpos + 4 : highRecord * 4);
    // maybe this should be added?
    // buffer.position((onlyRecord) ? recpos : 0);
    buffer.position(recpos);
    fChannel.write(buffer, recpos);
    if (resize)
      file.setLength(highRecord * 4);
  }
}
