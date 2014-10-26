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
package com.mellowtech.core.disc.blockfile;

import com.mellowtech.core.bytestorable.ByteStorable;

import java.nio.ByteBuffer;

/**
 * Implementation of First Fit and Best Fit algorithms. <br>
 * TODO: store the ids in a separate sorted structure for fast access.
 * 
 * @author rickard.coster@asimus.se
 * @version 1.0
 */
public class MemoryFit extends ByteStorable <MemoryFit> {

  protected int[] ids;
  protected int[] bytearray;
  protected int INIT_SIZE = 100;
  protected float GROW_FACTOR = 1.75f;
  protected int count = 0;

  public MemoryFit() {
    ids = new int[INIT_SIZE];
    bytearray = new int[INIT_SIZE];
    count = 0;
  }

  protected static final int findId(int[] idarray, int id, int high) {
    for (int i = 0; i < high; i++)
      if (idarray[i] == id)
        return i;
    return -1;
  }

  protected static final int binarySearch(int[] ba, int bytes, int low, int high) {

    while (low <= high) {
      int mid = (low + high) >> 1;
      int midBytes = ba[mid];

      if (midBytes < bytes)
        low = mid + 1;
      else if (midBytes > bytes)
        high = mid - 1;
      else
        return mid;
    }
    return -(low + 1); // bytes not found.
  }

  protected static final int firstFitBinarySearch(int[] ba, int bytes, int low,
      int high) {
    while (low <= high) {
      int mid = (low + high) >> 1;
      int midBytes = ba[mid];

      if (midBytes < bytes)
        low = mid + 1;
      else
        /* (midBytes >= bytes) */
        return mid;
    }
    return -1;
  }

  /**
   * Find the address that first fits the bytes.
   * 
   * @param bytes
   *          the bytes to fit
   * @return the adress that first fits the bytes, or -1 if none does
   */
  public int firstFit(int bytes) {
    int index = firstFitBinarySearch(bytearray, bytes, 0, count - 1);
    if (index >= 0)
      return ids[index];
    return -1;
  }

  /**
   * Find the address that best fits the bytes. Since there may be many ids that
   * have the same bytes free, this methods scans these ids linearly to find the
   * lowest id among the best fits.
   * 
   * @param bytes
   *          the bytes to fit
   * @return the address that best fits the bytes, or -1 if none does
   */
  public int bestFit(int bytes) {
    int index = binarySearch(bytearray, bytes, 0, count - 1);

    if (index < 0)
      index = -(index + 1);

    // do a linear scan to the right and left
    // to find the lowest id.
    int lowestid = ids[index];

    // scan to the 'left'
    for (int i = index - 1; i >= 0; i--) {
      if (bytearray[i] != bytearray[index])
        break;
      else if (ids[i] < lowestid)
        lowestid = ids[i];
    }

    // scan to the 'right'
    for (int i = index + 1; i < count; i++) {
      if (bytearray[i] != bytearray[index])
        break;
      else if (ids[i] < lowestid)
        lowestid = ids[i];
    }
    return lowestid;
  }

  protected void grow() {
    int newlen = (int) (ids.length * GROW_FACTOR);
    int[] newids = new int[newlen];
    int[] newbytearray = new int[newlen];
    System.arraycopy(ids, 0, newids, 0, ids.length);
    System.arraycopy(bytearray, 0, newbytearray, 0, ids.length);
    ids = newids;
    bytearray = newbytearray;
  }

  /**
   * Set id and bytes. Returns old bytes if it existed, 0 if it did not exist.
   * 
   * @param id
   *          the id
   * @param bytes
   *          the bytes associated with id
   * @return the old bytes associated with id, 0 if id did not exist,
   */
  public int set(int id, int bytes) {
    int index = findId(ids, id, count);
    if (index >= 0) {
      int oldbytes = bytearray[index];
      // remove, and then insert id and bytes again.
      clear(id);
      set(id, bytes);
      return oldbytes;
    }
    else {
      // find where to insert bytes (duplicates of bytes are ok).
      index = binarySearch(bytearray, bytes, 0, count - 1);

      // did bytes match exactly?
      if (index < 0)
        index = -(index + 1);

      // check if we need to expand the arrays
      if (count >= ids.length)
        grow();

      // move all entries from index one step to the right
      System.arraycopy(ids, index, ids, index + 1, count - index);
      System.arraycopy(bytearray, index, bytearray, index + 1, count - index);
      // insert new values.
      ids[index] = id;
      bytearray[index] = bytes;
      count++;
      return 0;
    }
  }

  /**
   * Clears id and associated bytes. Returns false if id was not found.
   * 
   * @param id
   *          the id
   * @return true, if id and associated bytes was cleared, false if id could not
   *         be found.
   */
  public boolean clear(int id) {
    int index = findId(ids, id, count);
    if (index < 0)
      return false;

    // move all entries from index + 1 to index, or clear last one.
    if (index == count - 1) {
      ids[index] = 0;
      bytearray[index] = 0;
    }
    else {
      System.arraycopy(ids, index + 1, ids, index, count - 1 - index);
      System.arraycopy(bytearray, index + 1, bytearray, index, count - 1
          - index);
    }
    count--;
    return true;
  }

  public int byteSize() {
    return 4 + 4 + (count * 8);
  }

  public int byteSize(ByteBuffer bb) {
    int pos = bb.position();
    int cnt = bb.getInt();
    int byteSize = 4 + 4 + (cnt * 8);
    bb.position(pos);
    return byteSize;
  }

  public void toBytes(ByteBuffer bb) {
    bb.putInt(count);
    for (int i = 0; i < count; i++) {
      bb.putInt(ids[i]);
      bb.putInt(bytearray[i]);
    }
  }

  public ByteStorable <MemoryFit> fromBytes(ByteBuffer bb) {
    MemoryFit m = new MemoryFit();
    m.count = bb.getInt();
    m.ids = new int[m.count];
    m.bytearray = new int[m.count];
    for (int i = 0; i < m.count; i++) {
      m.ids[i] = bb.getInt();
      m.bytearray[i] = bb.getInt();
    }
    return m;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("count     = " + count + "\n");
    for (int i = 0; i < count; i++) {
      sb.append(ids[i] + "\t" + bytearray[i] + "\n");
    }
    return sb.toString();
  }

  public ByteStorable <MemoryFit> fromBytes(ByteBuffer bb, boolean doNew) {
    return fromBytes(bb);
  }

}
