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
package org.mellowtech.core.disc.blockfile;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.ByteComparable;
import org.mellowtech.core.bytestorable.ByteStorable;

/**
 * A Block contains a number of variable-length records, where each record is
 * accessed via its compareTo() method.
 * <p>
 * The structure of a Block is
 * <code>[NUM][LEN][OFF][OFF]..[OFF][DATA1..]..[DATAn..]</code>
 * where <br>
 * <code>[NUM]</code> is the number of records <br>
 * <code>[LEN]</code> is the total length in bytes of all data <br>
 * <code>[OFF]</code> is the offset of the data for the corresponding id <br>
 * <code>[DATAi..]</code> is variable-length data
 * <p>
 * A single Block should not be larger than 64KB, if the blocks are stored on
 * disk.
 * 
 * @author rickard.coster@asimus.se
 * @version 1.0
 */
@Deprecated 
public class BlockNoId extends ByteStorable {

  /**
   * Creates a new <code>Block</code> instance.
   * 
   * @param template
   *          an instance of the <code>ByteStorable</code> type stored in the
   *          block
   */
  public BlockNoId(ByteStorable template) {
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
  public BlockNoId(int blockSize, ByteStorable template) {
    buffer = new byte[blockSize];
    this.template = template;
  }

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

  protected final static int GROW_SIZE = 64;

  protected final int binarySearch(ByteStorable key, int low, int high) {
    if (key instanceof ByteComparable) 
      return binarySearchBC((ByteComparable) key, low, high);
    while (low <= high) {
      int mid = (low + high) >> 1;
      int midValOffset = offsets[mid];
      ByteStorable midVal = template.fromBytes(buffer, midValOffset);
      int cmp = key.compareTo(midVal);
      if (cmp > 0)
        low = mid + 1;
      else if (cmp < 0)
        high = mid - 1;
      else
        return mid; // key found
    }
    return -(low + 1); // key not found.
  }
  
  protected final int binarySearchBC(ByteComparable key, int low, int high) {
    ByteBuffer bbKey = key.toBytes();
    ByteBuffer bbVal = ByteBuffer.wrap(buffer);
    while (low <= high) {
      int mid = (low + high) >> 1;
      int midValOffset = offsets[mid];
      int cmp = key.byteCompare(0, bbKey, midValOffset, bbVal);
      if (cmp > 0)
        low = mid + 1;
      else if (cmp < 0)
        high = mid - 1;
      else
        return mid; // key found
    }
    return -(low + 1); // key not found.
  }

  /**
   * Returns true if this Block contains the key.
   * 
   * @param key
   *          the key
   * @return true if the Block contains the key, false otherwise.
   */
  public boolean contains(ByteStorable key) {
    return binarySearch(key, 0, count - 1) >= 0 ? true : false;
  }

  /**
   * Retrieve the object at index 'index' within this Block, returns null if there
   * is no such object
   * 
   * @param index
   *          the index
   * @return the object, or null.
   */
  public ByteStorable get(int index) {
    if (index >= 0) {
      ByteStorable bs = (ByteStorable) template.fromBytes(buffer,
          offsets[index]);
      return bs;
    }
    return null;
  }
  
  /**
   * Retrieve the data associated with id in this Block, returns null if there
   * is no such id.
   * 
   * @param key
   *          the key
   * @return the data, or null.
   */
  public ByteStorable get(ByteStorable key) {
    int index = binarySearch(key, 0, count - 1);
    if (index >= 0) {
      ByteStorable bs = (ByteStorable) template.fromBytes(buffer,
          offsets[index]);
      return bs;
    }
    return null;
  }

  private void expand() {
    if (offsets == null) {
      offsets = new int[GROW_SIZE];
      return;
    }
    int[] newoffsets = new int[offsets.length + GROW_SIZE];
    System.arraycopy(offsets, 0, newoffsets, 0, offsets.length);
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
   * Returns true if the object can be stored in this Block.
   * 
   * @param key
   *          the object
   * @return true if the object can be inserted, false otherwise.
   */
  public boolean fitsValue(ByteStorable key) throws IllegalArgumentException {
    // new key 
    int keySize = key.byteSize();

    
    // New size indicators: one more element and total data length increases
    int byteSize = sizeBytesNeeded(count + 1)
        + sizeBytesNeeded(bufferLength + keySize);
    
    // Find the place where the new key is to be inserted, and make sure to sum 
    // up correct (new) offset lengths. 
    
    // Set 'lastCmp' to  -1 since it could be that the correct position for the 
    // new element is as the first element
    int lastCmp = -1;
    int addSize = 0;
    boolean found = false;
    for (int i = 0; i < count; i++) {
      
      int cmp = key.compareTo(template.fromBytes(buffer, offsets[i]));
      System.err.println("iter lastCmp: "+lastCmp+", count="+count+", cmp="+cmp);
      if (!found && lastCmp < 0 && cmp > 0) {
        // The new object should be inserted before the current object, add
        // the new key's size to offsets from now on. 
        found = true;
        addSize += keySize;
      }
      else if (cmp == 0) {
        throw new IllegalArgumentException("Object with this key already exists");
      }
      byteSize += sizeBytesNeeded(offsets[i] + addSize);
      lastCmp = cmp;
    }
    
    // If there was no place found for the element it should go last...
    // otherwise there is an error in this algorithm or the compareTo method
    if (!found ) {
      System.err.println("lastCmp: "+lastCmp+", count="+count);
      if (lastCmp > 0 || (lastCmp < 0 && count == 0)) 
        byteSize += sizeBytesNeeded(bufferLength); // the offset
      else
        throw new IllegalArgumentException("Error in fitsValue()");
    }
    
    // The actual data length increases
    byteSize += bufferLength + keySize;
    
    // Check if new element fits
    return byteSize <= buffer.length ? true : false;

  }

  /**
   * Inserts a new key in this Block. Returns false if it did not fit in the Block.
   * 
   * @param key 
   *          the object
   * @return true if key pair was inserted, false otherwise
   */
  public boolean insert(ByteStorable key) {
    if (!fitsValue(key)) {
      return false;
    }
    int index = binarySearch(key,  0, count - 1);
    if (index >= 0) {
      return false; // id already in Block
    }
    if (count == 0 || count == offsets.length)
      expand(); // expand offsets

    index = -index - 1; // index where to insert id and offset
    System.arraycopy(offsets, index, offsets, index + 1, count - index);
    // append to buffer
    offsets[index] = bufferLength;
    key.toBytes(buffer, bufferLength);
    bufferLength += key.byteSize();
    count++;
    return true;
  }

  /**
   * Remove a key from this Block. Returns the removed key, or null
   * if there was none
   * 
   * @param key 
   *          the key 
   *          
   * @return the removed key, or null if there was no such key
   */
  public ByteStorable remove(ByteStorable key) {
    int index = binarySearch(key, 0, count - 1);
    if (index >= 0) 
      return remove(index);
    return null;
  }
  
  /**
   * Remove a key from this Block. Returns the removed key, or null
   * if there was none
   * 
   * @param index 
   *          the index of the key
   *          
   * @return the removed key, or null if there was no such index
   */
  public ByteStorable remove(int index) {
    if (index < 0)
      return null; // not found

    // the offset
    int off = offsets[index];

    // move offsets one notch to the left
    System.arraycopy(offsets, index + 1, offsets, index, count - 1 - index);

    // get the object
    ByteStorable bs = template.fromBytes(buffer, off);
    int byteSize = bs.byteSize();

    // move bytes byteSize bytes back
    System.arraycopy(buffer, off + byteSize, buffer, off, bufferLength - off
        - byteSize);

    // decrease buffer usage by size removed
    bufferLength -= byteSize;
    // number of objects decreased
    count--;
    // subtract size removed from all offsets having offset larger than old offset
    for (int i = 0; i < count; i++)
      if (offsets[i] > off)
        offsets[i] -= byteSize;
    return bs;
  }

  /**
   * Updates the value for an id in this Block. Returns true if update was
   * successful, false if the new value did not fit. In the case where the new
   * value did not fit in this Block, the old value is left unchanged.
   * 
   * @param key
   *          the key
   * @return true if update was successful, false of the new value did not fit.
   *         In the case the new value did not fit, the old value is left
   *         unchanged.
   */
  public boolean update(ByteStorable key) {
    ByteStorable oldKey = remove(key);
    if (oldKey != null) {
      if (insert(key))
        return true;
      else {
        // re-insert old key
        insert(oldKey);
        return false;
      }
    }
    return false;
  }

  public int byteSize() {
    int byteSize = sizeBytesNeeded(count) + sizeBytesNeeded(bufferLength);
    for (int i = 0; i < count; i++) {
      byteSize += sizeBytesNeeded(offsets[i]);
    }
    byteSize += bufferLength;
    return byteSize;
  }

  public int byteSize(ByteBuffer bb) {
    BlockNoId block = new BlockNoId(buffer.length, template);
    block.fromBytes(bb);
    return block.byteSize();
  }

  public void toBytes(ByteBuffer bb) {
    putSize(count, bb);
    putSize(bufferLength, bb);
    for (int i = 0; i < count; i++) 
      putSize(offsets[i], bb);
    bb.put(buffer, 0, bufferLength);
  }

  public ByteStorable fromBytes(ByteBuffer bb) {
    BlockNoId block = new BlockNoId(buffer.length, template);
    block.count = getSize(bb);
    block.bufferLength = getSize(bb); 
    block.offsets = new int[block.count];
    for (int i = 0; i < block.count; i++) 
      block.offsets[i] = getSize(bb);
    bb.get(block.buffer, 0, block.bufferLength);
    return block;
  }

  public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
    return fromBytes(bb);
  }

  class EntryIterator implements Iterator {
    int i = 0;
    BlockNoId b;
    
    EntryIterator(BlockNoId b) {
      this.b = b;
    }

    public Object next() {
      if (i >= b.count)
        return null;
      ByteStorable object = b.template.fromBytes(b.buffer, b.offsets[i]);
      i++;
      return object;
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
    for (int i = 0; i < count; i++) {
      try {
        dataLen += template.byteSize(buffer, offsets[i]);
      }
      catch (Exception e) {
       CoreLog.L().log(Level.WARNING, "", e);
      }
    }
    BlockUtilization u = new BlockUtilization();
    u.dataLen = dataLen;
    u.idLen = 0;
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
    for (int i = 0; i < count; i++) {
      try {
        datalen += template.byteSize(buffer, offsets[i]);
        sb.append("off[" + i + "]= " + offsets[i] + "\tsize = "
            + sizeBytesNeeded(offsets[i]) + "\t"
            + template.fromBytes(buffer, offsets[i]).toString() + "\n");
      }
      catch (Exception e) {
        CoreLog.L().log(Level.WARNING, "", e);
      }
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
