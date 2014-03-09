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

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.bytestorable.CBString;

/**
 * A Block contains a number of variable-length records, where each record is
 * accessed via a unique id.
 * <p>
 * The structure of a Block is
 * <code>[NUM][LEN][ID ][OFF][ID ][OFF]..[ID ][OFF][DATA1..]..[DATAn..]</code>
 * where <br>
 * <code>[NUM]</code> is the number of records <br>
 * <code>[LEN]</code> is the total length in bytes of all data <br>
 * <code>[ID ]</code> is the id of a record <br>
 * <code>[OFF]</code> is the offset of the data for the corresponding id <br>
 * <code>[DATAi..]</code> is variable-length data
 * <p>
 * A single Block should not be larger than 64KB, if the blocks are stored on
 * disk.
 * 
 * @author rickard.coster@asimus.se
 * @version 1.0
 */
public class BlockWithId extends ByteStorable {

  /**
   * Creates a new <code>Block</code> instance.
   * 
   * @param template
   *          an instance of the <code>ByteStorable</code> type stored in the
   *          block
   */
  public BlockWithId(ByteStorable template) {
    this.template = template;
  }

  /**
   * Creates a new <code>Block</code> instance.
   * 
   * @param blockSize
   *          the block size in bytes
   * @param template
   *          an instance of the <code>ByteStorable</code> type stored in the
   *          block
   */
  public BlockWithId(int blockSize, ByteStorable template) {
    buffer = new byte[blockSize];
    this.template = template;
  }

  // ids
  int[] ids = null;

  // offsets
  int[] offsets = null;

  // record data is stored in this buffer
  byte[] buffer = null;

  // number of inserted records
  int count = 0;

  // first free index in buffer
  int bufferLength = 0;

  // template used for reading objects
  ByteStorable template = null;

  protected final static int GROW_SIZE = 10;

  protected final static int binarySearch(int[] a, int key, int low, int high) {
    while (low <= high) {
      int mid = (low + high) >> 1;
      int midVal = a[mid];

      if (midVal < key)
        low = mid + 1;
      else if (midVal > key)
        high = mid - 1;
      else
        return mid; // key found
    }
    return -(low + 1); // key not found.
  }

  /**
   * Returns true if this Block contains the id.
   * 
   * @param id
   *          the id
   * @return true if the Block contains the id, false otherwise.
   */
  public boolean contains(int id) {
    return binarySearch(ids, id, 0, count - 1) >= 0 ? true : false;
  }

  /**
   * Retrieve the data associated with id in this Block, returns null if there
   * is no such id.
   * 
   * @param id
   *          the id
   * @return the data, or null.
   */
  public ByteStorable get(int id) {
    int index = binarySearch(ids, id, 0, count - 1);
    if (index >= 0) {
      ByteStorable bs = (ByteStorable) template.fromBytes(buffer,
          offsets[index]);
      return bs;
    }
    return null;
  }

  private void expand() {
    if (ids == null) {
      ids = new int[GROW_SIZE];
      offsets = new int[GROW_SIZE];
      return;
    }
    int[] newids = new int[ids.length + GROW_SIZE];
    int[] newoffsets = new int[ids.length + GROW_SIZE];
    System.arraycopy(ids, 0, newids, 0, ids.length);
    System.arraycopy(offsets, 0, newoffsets, 0, ids.length);
    ids = newids;
    offsets = newoffsets;
  }

  /**
   * Returns the minimum number of free data bytes in this Block. The real
   * number of free bytes may be larger than freeBytes(), since the information
   * about the data length and offset is stored in a compact format, and this
   * method calculates the worst case scenario.
   * 
   * @return the number of free data bytes.
   */
  public int freeBytes() {
    int byteSize = byteSize() // old byte size
        - sizeBytesNeeded(count) // old count
        + sizeBytesNeeded(count + 1) // new count
        + 4 // the max id RLE as a variable byte
        + 4; // the max offset RLE as a variable byte
    if (byteSize >= buffer.length)
      return 0;
    return buffer.length - byteSize; // min remaining for data
  }

  /**
   * Returns true if the data value can be stored in this Block.
   * 
   * @param id
   *          the id
   * @param value
   *          the data value
   * @return true if value can be inserted, false otherwise.
   */
  public boolean fitsValue(int id, ByteStorable value) {
    // new value and id size
    int valueSize = value.byteSize();

    // Don't understand the code below...
    int byteSize = sizeBytesNeeded(count + 1)
        + sizeBytesNeeded(bufferLength + valueSize);
    int lastid = 0;
    boolean found = false;
    for (int i = 0; i < count; i++) {
      if (lastid < id && id < ids[i]) {
        byteSize += sizeBytesNeeded(id - lastid);
        byteSize += sizeBytesNeeded(bufferLength); // the offset
        lastid = id;
        found = true;
        byteSize += sizeBytesNeeded(ids[i] - lastid);
        byteSize += sizeBytesNeeded(offsets[i]);
        lastid = ids[i];
      }
      else {
        byteSize += sizeBytesNeeded(ids[i] - lastid);
        byteSize += sizeBytesNeeded(offsets[i]);
        lastid = ids[i];
      }
    }
    if (!found && id > lastid) {
      byteSize += sizeBytesNeeded(id - lastid);
      byteSize += sizeBytesNeeded(bufferLength); // the offset
    }
    byteSize += bufferLength + valueSize;
    return byteSize <= buffer.length ? true : false;

  }

  /**
   * Inserts a new id, value pair in this Block. Returns false if the pair did
   * not fit in the Block.
   * 
   * @param id
   *          the id
   * @param value
   *          the data value
   * @return true if id, value pair was inserted, false if id existed or value could not fit
   */
  public boolean insert(int id, ByteStorable value) {
    if (!fitsValue(id, value)) {
      return false;
    }
    int index = binarySearch(ids, id, 0, count - 1);
    if (index >= 0) {
      return false; // id already in Block
    }
    if (count == 0 || count == ids.length)
      expand(); // expand ids and offsets

    index = -index - 1; // index where to insert id and offset
    System.arraycopy(ids, index, ids, index + 1, count - index);
    System.arraycopy(offsets, index, offsets, index + 1, count - index);
    ids[index] = id;

    // append to buffer
    offsets[index] = bufferLength;
    value.toBytes(buffer, bufferLength);
    bufferLength += value.byteSize();
    count++;
    return true;
  }

  /**
   * Remove a id, value pair from this Block. Returns the removed value, or null
   * if there was no such id.
   * 
   * @param id
   *          the id
   * @return the removed value, or null if there was no such id
   */
  public ByteStorable remove(int id) {
    int index = binarySearch(ids, id, 0, count - 1);
    if (index < 0)
      return null; // id not found

    // the offset
    int off = offsets[index];

    // move ids and offsets one notch to the left
    System.arraycopy(ids, index + 1, ids, index, count - 1 - index);
    System.arraycopy(offsets, index + 1, offsets, index, count - 1 - index);

    // get the object
    ByteStorable bs = template.fromBytes(buffer, off);
    int byteSize = template.byteSize(buffer, off);

    // move bytes byteSize bytes back
    System.arraycopy(buffer, off + byteSize, buffer, off, bufferLength - off
        - byteSize);

    // decrease buffer usage by size removed
    bufferLength -= byteSize;
    // number of objects decreased
    count--;
    // subtract size removed from all offsets starting at index
    for (int i = index; i < count; i++)
      offsets[i] -= byteSize;
    return bs;
  }

  /**
   * Updates the value for an id in this Block. Returns true if update was
   * successful, false if the new value did not fit. In the case where the new
   * value did not fit in this Block, the old value is left unchanged.
   * 
   * @param id
   *          the id
   * @param value
   *          the new data value
   * @return true if update was successful, false of the new value did not fit.
   *         In the case the new value did not fit, the old value is left
   *         unchanged.
   */
  public boolean update(int id, ByteStorable value) {
    ByteStorable oldvalue = remove(id);
    if (oldvalue != null) {
      if (insert(id, value))
        return true;
      else {
        // re-insert old value
        insert(id, oldvalue);
        return false;
      }
    }
    return false;
  }

  public int byteSize() {
    int byteSize = sizeBytesNeeded(count) + sizeBytesNeeded(bufferLength);
    int lastid = 0;
    for (int i = 0; i < count; i++) {
      byteSize += sizeBytesNeeded(ids[i] - lastid);
      byteSize += sizeBytesNeeded(offsets[i]);
      lastid = ids[i];
    }
    byteSize += bufferLength;
    return byteSize;
  }

  public int byteSize(ByteBuffer bb) {
    BlockWithId block = new BlockWithId(buffer.length, template);
    block.fromBytes(bb);
    return block.byteSize();
  }

  public void toBytes(ByteBuffer bb) {
    putSize(count, bb);
    putSize(bufferLength, bb);
    int lastid = 0;
    for (int i = 0; i < count; i++) {
      putSize(ids[i] - lastid, bb);
      putSize(offsets[i], bb);
      lastid = ids[i];
    }
    bb.put(buffer, 0, bufferLength);
  }

  public ByteStorable fromBytes(ByteBuffer bb) {
    BlockWithId block = new BlockWithId(buffer.length, template);
    block.count = getSize(bb);
    block.bufferLength = getSize(bb); 
    block.ids = new int[block.count];
    block.offsets = new int[block.count];
    int lastid = 0;
    for (int i = 0; i < block.count; i++) {
      block.ids[i] = getSize(bb) + lastid;
      block.offsets[i] = getSize(bb);
      lastid = block.ids[i];
    }
    bb.get(block.buffer, 0, block.bufferLength);
    return block;
  }

  public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
    return fromBytes(bb);
  }

  class Entry implements Map.Entry {
    int id;
    ByteStorable object;

    public Object getKey() {
      return new Integer(id);
    }

    public Object getValue() {
      return object;
    }

    public Object setValue(Object arg0) {
      object = (ByteStorable) arg0;
      return object;
    }
  }

  class EntryIterator implements Iterator {
    int i = 0;
    BlockWithId b;
    Entry e;

    EntryIterator(BlockWithId b) {
      this.b = b;
      this.e = new Entry();
    }

    public Object next() {
      if (i >= b.count)
        return null;
      e.id = b.ids[i];
      e.object = b.template.fromBytes(b.buffer, b.offsets[i]);
      i++;
      return e;
    }

    public boolean hasNext() {
      return (i < b.count) ? true : false;
    }

    public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException("remove not supported");
    }
  }

  public Iterator iterator() {
    return new EntryIterator(this);
  }

  public BlockUtilization utilization() {
    int dataLen = 0;
    int lastid = 0;
    int idLen = 0;
    for (int i = 0; i < count; i++) {
      idLen += sizeBytesNeeded(ids[i] - lastid);
      try {
        dataLen += template.byteSize(buffer, offsets[i]);
      }
      catch (Exception e) {
        CoreLog.L().log(Level.WARNING, "", e);
      }
      lastid = ids[i];
    }
    BlockUtilization u = new BlockUtilization();
    u.dataLen = dataLen;
    u.idLen = idLen;
    u.count = count;
    u.blockSize = buffer.length;
    u.numBlocks = 1;
    return u;
  }

  public String toString() {
    int byteSize = sizeBytesNeeded(count) + sizeBytesNeeded(bufferLength);
    StringBuffer sb = new StringBuffer();
    int datalen = 0;
    sb.append("count = " + count + "\tsize = " + sizeBytesNeeded(count) + "\n");
    sb.append("bufLen= " + bufferLength + "\tsize = "
        + sizeBytesNeeded(bufferLength) + "\n");
    int lastid = 0;
    for (int i = 0; i < count; i++) {
      byteSize += sizeBytesNeeded(ids[i] - lastid);
      byteSize += sizeBytesNeeded(offsets[i]);
      sb.append("ids[" + i + "]= " + ids[i] + "\tsize = "
          + sizeBytesNeeded(ids[i] - lastid) + "\trle = " + (ids[i] - lastid)
          + "\n");

      try {
        datalen += template.byteSize(buffer, offsets[i]);
        sb.append("off[" + i + "]= " + offsets[i] + "\tsize = "
            + sizeBytesNeeded(offsets[i]) + "\t"
            + template.fromBytes(buffer, offsets[i]).toString() + "\n");
      }
      catch (Exception e) {
        CoreLog.L().log(Level.WARNING, "", e);
      }
      lastid = ids[i];
    }
    byteSize += bufferLength;
    sb.append("\nbytesize = " + byteSize);
    sb.append("\ndatalen  = " + datalen);
    sb.append("\ndl/bs    = " + ((double) datalen) / ((double) byteSize));
    sb.append("\ndl/bs    = " + utilization());
    return sb.toString();
  }

  public int getCount() {
    return count;
  }
}
