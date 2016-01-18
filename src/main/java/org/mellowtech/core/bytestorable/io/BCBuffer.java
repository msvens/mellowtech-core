
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

package org.mellowtech.core.bytestorable.io;

import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.util.RangeIterable;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;

import org.mellowtech.core.bytestorable.io.BCBlock.PtrType;

/**
 * @author msvens
 * @since 08/01/16
 */
public class BCBuffer<A, B extends BComparable<A, B>> implements RangeIterable<B, B> {


  private ByteBuffer block;
  private B keyType;
  private PtrType ptrType;
  private byte[] tmpArr = new byte[128];
  private int high;
  private int bytesWritten;
  private short reservedSpace;
  private byte ptrSize;
  private int headerSize;
  //private int offset;
  private int capacity;

  /**
   * Open an existing block
   *
   * @param block
   * @param template
   */
  public BCBuffer(ByteBuffer block, B template) {
    this(block, template, false, null, (short) -1);
  }

  public BCBuffer(int blockSize, B template, PtrType ptrType, short reservedSpace) {
    this(ByteBuffer.allocate(blockSize), template, true, ptrType, reservedSpace);
  }

  public BCBuffer(int blockSize, B template, PtrType ptrType) {
    this(ByteBuffer.allocate(blockSize), template, true, ptrType, (short) 0);
  }

  public BCBuffer(byte[] block, B template, PtrType ptrType){
    this(ByteBuffer.wrap(block), template, ptrType, (short) 0);
  }

  public BCBuffer(ByteBuffer block, B template, PtrType ptrType, short reservedSpace) {
    this(block, template, true, ptrType, reservedSpace);
  }

  public BCBuffer(ByteBuffer block, B template, PtrType ptrType){
    this(block, template, ptrType, (short) 0);
  }


  protected BCBuffer(ByteBuffer block, B template, boolean newBlock,
                     PtrType ptrType, short reservedSpace) {
    try {
      this.keyType = template;
      setBlock(block, newBlock, ptrType, reservedSpace);
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  public static int bytesNeeded(int maxElements, byte ptrSize) {
    int bytes = 2; //resservedSpace length
    bytes += 1 + (ptrSize * 2); //headerSize ptrSize, numElements, bytesWritten
    bytes += ptrSize * maxElements;
    return bytes;
  }

  private static int calculateOptimum(int total, int divider) {
    int optimal = total / divider;
    int remainder = total % divider;
    optimal = (remainder > divider / 2) ? optimal + 1 : optimal;
    return optimal;
  }

  private static <A, B extends BComparable<A, B>> void getKeysPrevious(BCBuffer<A, B>[] blocks, int index,
                                                                       int optimal) {
    int blockBytes;
    B current;
    int diff;
    int diffPut;
    while (true) {
      blockBytes = blocks[index].getDataBytes();
      diff = Math.abs(blockBytes - optimal);
      current = blocks[index - 1].getLast();
      diffPut = Math.abs(blockBytes + current.byteSize() - optimal);
      // the action of removing the current key will get this block
      // closer to the optimal
      if (diffPut < diff && blocks[index].fits(current)) {
        blocks[index].insert(current);
        blocks[index - 1].delete(current);
      } else
        return;
    }
  }

  private static <A, B extends BComparable<A, B>> void putKeysPrevious(BCBuffer<A, B>[] blocks, int index,
                                                                       int optimal) {
    int blockBytes;
    B current;
    int diff;
    int diffPut;
    while (true) {
      blockBytes = blocks[index].getDataBytes();
      diff = Math.abs(blockBytes - optimal);
      current = blocks[index].getFirst();
      diffPut = Math.abs(blockBytes - current.byteSize() - optimal);
      // the action of removing the current key will get this block
      // closer to the optimal
      if (diffPut < diff && blocks[index - 1].fits(current)) {
        blocks[index].delete(current);
        blocks[index - 1].insert(current);
      } else
        return;
    }
  }

  /**
   * Redistribute the keys in a number of blocks as evenly as possible.
   *
   * @param blocks An array of sorted blocks that should be redistributed.
   * @param <A>
   * @param <B>
   */
  public static <A, B extends BComparable<A, B>> void redistribute(BCBuffer<A, B>[] blocks) {
    // first the total num bytes written and calculate the
    // optimal bytes in each block:
    int totalBytes = 0;
    int optimalBytes = 0;
    for (int i = 0; i < blocks.length; i++)
      totalBytes += blocks[i].getDataBytes();
    optimalBytes = calculateOptimum(totalBytes, blocks.length);

    // now loop from the end:
    int blockBytes;
    for (int i = blocks.length - 1; i > 0; i--) {
      blockBytes = blocks[i].getDataBytes();
      if (blockBytes > optimalBytes)
        putKeysPrevious(blocks, i, optimalBytes);
      else if (blockBytes < optimalBytes)
        getKeysPrevious(blocks, i, optimalBytes);
    }
  }

  /**
   * Clear this block from elements
   */
  public void clear() {
    high = 0;
    bytesWritten = 0;
    writeNumElements(0);
    writeBytesWritten(0);
  }

  /**
   * Returns true if this block cotains the given key.
   *
   * @param key the key to search for.
   * @return true if the key was found
   */
  public boolean contains(B key) {
    return search(key) >= 0;
  }

  /**
   * Deletes a key from this block.
   *
   * @param key The key to delete
   * @return The deleted key read from this block.
   */
  public B delete(B key) {
    return delete(search(key));
  }

  /**
   * Remove a key at a given position
   *
   * @param pos position
   * @return the deleted key
   */
  public B delete(int pos) {
    if (pos >= high || pos < 0)
      return null;

    // read the physical position:
    int pPos = getPhysicalPos(pos);
    block.position(pPos);
    B toDelete = keyType.from(block);
    int firstPos = capacity - bytesWritten;
    int byteSize = toDelete.byteSize();
    if (pos < high - 1) { // we have to compact the array:
      byteBufferCopy(getIndexPos(pos + 1), getIndexPos(pos), (high - 1 - pos)
          * ptrSize);
    }
    // now compact the data:
    byteBufferCopy(firstPos, firstPos + byteSize, pPos - firstPos);

    // finally update all positions less than pos by adding toDelete.byteSize()
    // to
    // their byte positions (we have to loop through the entire array):
    high--;
    for (int i = 0; i < high; i++) {
      int oPos = getPhysicalPos(i);
      if (oPos < pPos)
        setPhysicalPos(i, oPos + byteSize);
    }
    writeNumElements(high);
    bytesWritten -= byteSize;
    writeBytesWritten(bytesWritten);
    return toDelete;
  }

  /**
   * Check if this block has room for an additional key.
   *
   * @param key the key to check
   * @return True if the key can be stored in this block.
   */
  public boolean fits(B key) {
    return reservedSpace + headerSize + bytesWritten + ((high + 1) * ptrSize)
        + key.byteSize() <= capacity;
  }

  /**
   * Checks if this block can fit all keys in other block. Will not exclude any
   * duplicates.
   *
   * @param other the block to merge with.
   * @return true if the two blocks can be merged.
   */
  public boolean fits(BCBuffer<A, B> other) {
    int totDataBytes = other.getDataBytes() + getDataBytes();
    int totElements = other.getNumberOfElements() + getNumberOfElements();
    if (reservedSpace + headerSize + totDataBytes + (totElements * ptrSize) > capacity)
      return false;
    return true;
  }

  /**
   * Checks if this block can fit all keys in block and an additional key. Will not exclude any
   * duplicates.
   *
   * @param other      the block to merge with.
   * @param additional a <code>ByteStorable</code> value
   * @return true if the two blocks can be merged.
   */
  public boolean fits(BCBuffer<A, B> other, B additional) {
    int totDataBytes = other.getDataBytes() + getDataBytes()
        + additional.byteSize();
    int totElements = other.getNumberOfElements() + getNumberOfElements() + 1;
    return reservedSpace + headerSize + totDataBytes + ((totElements) * ptrSize) <= capacity;
  }

  /**
   * Key at position index.
   *
   * @param index positon of key
   * @return null if the block did not contain the key or the index was out of
   * range
   */
  public B get(int index) {
    if (index >= high || index < 0)
      return null;
    block.position(getPhysicalPos(index));
    return keyType.from(block);
  }

  /**
   * Binary search to find a key
   *
   * @param key Key to search for
   * @return the key read from the current block
   */
  public B get(B key) {
    return get(search(key));
  }

  /**
   * Return the current block. Be careful to manipulate a block directly (and
   * not via SortedBlock) since the sorted block stores pointers in each block.
   *
   * @return ByteBuffer of bytes containing the keys.
   */
  public ByteBuffer getBlock() {
    return block;
  }

  /**
   * Return the current block as an array. In case the ByteBuffer is not backed
   * by an array the returned array will be a copy of this block
   * @return array of bytes containg the keys stored in this block
   */
  public byte[] getArray() {
    if(block.hasArray())
      return block.array();
    else {
      byte b[] = new byte[capacity];
      block.position(0);
      block.get(b);
      return b;
    }
  }

  /**
   * Number of bytes actually written in this block. Including the reserved
   * space
   *
   * @return number of bytes
   */
  public int getBytesWritten() {
    return reservedSpace + headerSize + bytesWritten + (high * ptrSize);
  }

  /**
   * Number of databytes and pointer bytes written in this file.
   *
   * @return number of bytes
   */
  public int getDataAndPointersBytes() {
    return bytesWritten + (high * ptrSize);
  }

  /**
   * Number of bytes used to store the actual keys in this block (excluding
   * pointers).
   *
   * @return number of bytes used
   */
  public int getDataBytes() {
    return bytesWritten;
  }

  /**
   * The first key in this block.
   *
   * @return null if the block is empty
   */
  public B getFirst() {
    return get(0);
  }

  /**
   * The last key in this block.
   *
   * @return null if the block is empty
   */
  public B getLast() {
    return get(high - 1);
  }

  /**
   * Number of keys in this block.
   *
   * @return number of elements
   */
  public int getNumberOfElements() {
    return high;
  }

  /**
   * The pointer type in this block, i.e 4 for integer size pointers
   *
   * @return pointer type
   */
  public PtrType getPointerType() {
    return ptrType;
  }

  /**
   * Returns the number of bytes that has been reserved in this block.
   *
   * @return an <code>int</code> value
   */
  public int getReservedSpace() {
    return reservedSpace - 2;
  }

  /**
   * This returns the start position of the reserved space in this sorted block.
   * Be careful to read/write from the reserved space since the sorted block has
   * no control over this.
   *
   * @return an <code>int</code> value
   */
  public int getReservedSpaceStart() {
    return 2;
  }

  /**
   * Inserts a key in this block. Performs a binary search to find the correct
   * position.
   *
   * @param key The key to insert.
   * @return the index if successful, -1 otherwise.
   * @see SortedBlock#insertKeyUnsorted
   */
  public int insert(B key) {
    // check if it can be inserted here:
    if (!fits(key))
      return -1;
    int pos = search(key);
    if (pos >= 0)
      return -1;
    pos++;
    pos = Math.abs(pos);

    // calculate physical position:
    int pPos = capacity - bytesWritten - key.byteSize();

    // shift all the elments to the right of pos to fit the pPos (e.g. a short)
    if (pos < high)
      byteBufferCopy(getIndexPos(pos), getIndexPos(pos + 1), (high - pos)
          * ptrSize);

    setPhysicalPos(pos, pPos);
    high++;
    writeNumElements(high);
    bytesWritten += key.byteSize();
    writeBytesWritten(bytesWritten);
    block.position(pPos);
    key.to(block);
    return pos;
  }

  /**
   * Appends a key to this block. Note that this method does not guarantee that
   * the block stays sorted.
   *
   * @param key Key to insert
   * @return true if the insert was successfull
   */
  public boolean insertUnsorted(B key) {
    if (!fits(key))
      return false;
    int pPos = capacity - bytesWritten - key.byteSize();
    setPhysicalPos(high, pPos);
    high++;
    writeNumElements(high);
    bytesWritten += key.byteSize();
    writeBytesWritten(bytesWritten);
    block.position(pPos);
    key.to(block);
    return true;
  }

  public boolean isEmpty() {
    return getNumberOfElements() == 0;
  }

  @Override
  public Iterator<B> iterator(boolean descend, B from, boolean fromInclusive, B to, boolean toInclusive) {
    return descend ? new BCBufferDescendIter(this, from, fromInclusive, to, toInclusive) :
        new BCBufferIter(this, from, fromInclusive, to, toInclusive);
  }

  public BCBuffer<A, B> merge(BCBuffer<A, B> other) {
    //rewrite needs to always copy to this buffer
    if(other.isEmpty()) return this;
    else if(isEmpty() || other.getFirst().compareTo(getLast()) > 0){ //all keys in other are large...can insert unsorted
      for(B key : other){
        if(!insertUnsorted(key))
          throw new BufferOverflowException();
      }
    } else { //insert as usual
      for(B key : other){
        if(insert(key) < 0)
          throw new BufferOverflowException();
      }
    }
    return this;
  }

  /**
   * Binary search. Works in the same fashion as Arrays.binarySearch.
   *
   * @param key The key to search for
   * @return position
   * @see java.util.Arrays
   */
  public int search(B key) {
    int highSearch = high - 1;
    int low = 0, mid;
    B current;
    while (low <= highSearch) {
      mid = (low + highSearch) / 2;
      block.position(getPhysicalPos(mid));
      current = keyType.from(block);
      int cmp = current.compareTo(key);
      if (cmp < 0)
        low = mid + 1;
      else if (cmp > 0)
        highSearch = mid - 1;
      else
        return mid; // key found
    }
    return -(low + 1);
  }

  public int searchBC(B key) {
    int low = 0;
    int highSearch = high - 1, mid;
    ByteBuffer bbKey = key.to();
    while (low <= highSearch) {
      mid = (low + highSearch) / 2;
      int midValOffset = getPhysicalPos(mid);
      int cmp = key.byteCompare(midValOffset, block, 0, bbKey);
      if (cmp < 0)
        low = mid + 1;
      else if (cmp > 0)
        highSearch = mid - 1;
      else
        return mid; // key found
    }
    return -(low + 1); // key not found.
  }

  /**
   * If the keys in this block has been inserted unsorted use sort to sort the
   * contents. This method works as follows:<br>
   * it reads in every key into an CBytalbe[], and then either calls Arrays.sort
   * or sort.<br>
   * If you are reading in many keys at once it is a good idea to first store
   * them in an array than call sort, and then insert them using insertUnsorted
   * into this block.
   *
   * @param parallelSort If true use Arrays.parallelSort, otherwise use Arrays.sort
   * @see SortedBlock#sort
   * @see SortedBlock#insertKeyUnsorted
   */
  public BCBuffer<A, B> sort(boolean parallelSort) {
    BComparable toSort[] = new BComparable[high];
    int elems = high;
    for (int i = 0; i < elems; i++) {
      toSort[i] = get(i);
    }
    if (parallelSort)
      Arrays.parallelSort(toSort);
    else
      Arrays.sort(toSort);
    clear();
    for (int i = 0; i < elems; i++) {
      insertUnsorted((B) toSort[i]);
    }
    return this;
  }

  public BCBuffer<A, B> split() {
    return split(new BCBuffer<>(capacity, keyType, ptrType, (short) (reservedSpace - 2)));
  }

  public BCBuffer<A, B> split(BCBuffer<A, B> other) {
    //BCBlock<A, B> other = new BCBuffer<>(block.length, keyType, ptrType, (short) (reservedSpace - 2));
    int half = bytesWritten / 2;
    B lastKey;
    int numWritten = 0;

    //TODO: could be more efficient?
    while (numWritten < half) {
      //lastKey = getLast();
      //other.insertKey(lastKey);
      //deleteKey(lastKey);
      lastKey = delete(high - 1);
      other.insert(lastKey);
      numWritten += lastKey.byteSize();
    }
    // finally copy the reserved space (use tmp buffer for now
    if (tmpArr.length < getReservedSpace()) {
      tmpArr = new byte[getReservedSpace()];
    }
    block.position(getReservedSpaceStart());
    other.getBlock().position(getReservedSpaceStart());
    block.get(tmpArr, 0, getReservedSpace());
    other.getBlock().put(tmpArr, 0, getReservedSpace());
    return other;
  }

  /**
   * The actual storage capacity of this sortedblock. Remember that Each key
   * stored will have an additional number of bytes for its pointer.
   *
   * @return storage in bytes
   */
  public int storageCapacity() {
    return capacity - headerSize - reservedSpace;
  }

  public String toString() {

    StringBuffer sbuff = new StringBuffer();
    try {
      int high = readNumberOfElements();
      sbuff.append("number of items: " + high + "\n");
      sbuff.append("number of bytes written: " + readBytesWritten() + "\n");
      sbuff.append("load factor: "
          + (double) ((double) getBytesWritten() / (double) capacity));
      sbuff.append("\nsort order:\n");

      for (int i = 0; i < high; i++) {
        int offset = getPhysicalPos(i);
        sbuff.append("offset: " + offset);
        block.position(offset);
        sbuff.append(" item: " + keyType.from(block) + "\n");
      }
    } catch (Exception e) {/*e.printStackTrace();*/}
    return sbuff.toString();
  }

  /**
   * This will update a previously stored key. Use with caution since it will
   * just overwrite the current data at the given index. Thus, the keySize
   * cannot be changed.
   *
   * @param key   a value of type 'ByteStorable'
   * @param index a value of type 'int'
   */
  public void update(B key, int index) {
    block.position(getPhysicalPos(index));
    key.to(block);
  }

  private void byteBufferCopy(int srcPos, int destPos, int length) {
    if (tmpArr.length < length)
      tmpArr = new byte[length];
    block.position(srcPos);
    block.get(tmpArr, 0, length);
    block.position(destPos);
    block.put(tmpArr, 0, length);
  }

  private int getIndexPos(int index) {
    return reservedSpace + headerSize + (index * ptrSize);
  }

  private int getPhysicalPos(int index) {
    return read(getIndexPos(index));
  }

  private int read(int position) {
    int aligned = position;
    switch (ptrType) {
      case BIG:
        return block.getInt(aligned);
      case NORMAL:
        return block.getShort(aligned);
      case TINY:
        return block.get(aligned);
    }
    return -1;
  }

  private int readBytesWritten() {
    return read(1 + ptrSize + reservedSpace);
  }

  private int readNumberOfElements() {
    return read(1 + reservedSpace);
  }

  private PtrType readPtrType() {
    byte b = block.get(reservedSpace);
    return PtrType.from(b);
  }

  private short readReservedSpace() {
    return block.getShort(0);
  }

  private void setBlock(ByteBuffer block, boolean newBlock, PtrType ptrType, short reservedSpace) {
    this.block = block;
    this.capacity = block.limit();
    if (newBlock) {
      this.reservedSpace = (short) (reservedSpace + 2); // the capacity has to be
      // stored:
      high = 0;
      bytesWritten = 0;
      this.ptrType = ptrType;
      this.ptrSize = ptrType.size();
      writeNumElements(0);
      writeBytesWritten(0);
      writeReservedSpaceLength();
      writePtrSize();
    } else {
      this.reservedSpace = (short) (readReservedSpace() + 2);
      this.ptrType = readPtrType();
      this.ptrSize = this.ptrType.size();
      high = readNumberOfElements();
      bytesWritten = readBytesWritten();
    }
    headerSize = (this.ptrSize * 2) + 1;
  }

  private void setPhysicalPos(int index, int value) {
    write(getIndexPos(index), value);
  }

  private void write(int position, int value) {
    switch (ptrType) {
      case BIG:
        block.putInt(position, value);
        break;
      case NORMAL:
        block.putShort(position, (short) value);
        break;
      case TINY:
        block.put(position, (byte) value);
        break;
    }
  }

  private void writeBytesWritten(int bytesWritten) {
    write(1 + ptrSize + reservedSpace, bytesWritten);
  }

  private void writeNumElements(int numElements) {
    write(1 + reservedSpace, numElements);
  }

  private void writePtrSize() {
    block.put(reservedSpace, ptrType.size());
  }

  private void writeReservedSpaceLength() {
    block.putShort(0, (short) (reservedSpace - 2));
  }

  int getLowerBound(B key, boolean inclusive) {
    int pos = 0;
    if (key == null) return pos;
    pos = search(key);
    if (pos >= 0) { //item is present
      return inclusive ? pos : ++pos;
    } else { //item is not present
      pos++;
      return Math.abs(pos);
    }
  }

  int getUpperBound(B key, boolean inclusive) {
    int pos = high - 1;
    if (key == null || getNumberOfElements() < 1) return pos;
    pos = search(key);
    if (pos >= 0) {//item present
      return inclusive ? pos : --pos;
    } else {
      pos += 2;
      return Math.abs(pos);
    }
  }
}
