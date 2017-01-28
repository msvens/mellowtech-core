
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


/**
 * The BBuffer keeps a sorted byte array of BComparables. The buffer
 * into two sections, the first section is the pointers
 * to the actual keys stored in the array. All sorting, searching, rearranging is
 * done in the pointers section. Thus, keys does not have to be physically
 * sorted in the block, it is only the pointers that are sorted. The SortedBlock
 * do not have to be defragmented. Whenever keys are deleted the SortedBlock
 * automatically move that space to the unused space section. So using very long
 * keys with heavy insert/delete can reduce performance.
 * <p>
 * The overhead for using the buffer depends on the pointer size. It can
 * either be 4, 2, 1 byte extra for each key stored in the sorted block.
 * </p>
 * <p><strong>Observe</strong> that this class always assume the underlying
 * ByteBuffer starts at position 0. So if you want to use this class over a larger
 * buffer you need to use the split/submap operations in ByteBuffer
 * </p>
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.4
 * @param <A> type of value
 */
public class BCBuffer<A> implements RangeIterable<BComparable<A>, BComparable<A>> {


  private ByteBuffer block;
  private BComparable<A> keyType;
  private PtrType ptrType;
  //TODO: maybe remove tmpArr. Unnecessary optimization?
  private byte[] tmpArr = new byte[128];
  private int high;
  private int bytesWritten;
  private short reservedSpace;
  private byte ptrSize;
  private int headerSize;
  private int capacity;

  /**
   * Open an existing buffer
   *
   * @param block data
   * @param template BComparable template
   */
  public BCBuffer(ByteBuffer block, BComparable<A> template) {
    this(block, template, false, null, (short) -1);
  }

  /**
   * Creates a new buffer
   * @param blockSize the size of the buffer
   * @param template BComparable template
   * @param ptrType size of the pointer
   * @param reservedSpace allocate some reserved space in the beginning of the buffer
   */
  public BCBuffer(int blockSize, BComparable<A> template, PtrType ptrType, short reservedSpace) {
    this(ByteBuffer.allocate(blockSize), template, true, ptrType, reservedSpace);
  }

  /**
   * Creates a new buffer with no reserved space
   * @param blockSize the size of the buffer
   * @param template BComparable template
   * @param ptrType ptr type
   */
  public BCBuffer(int blockSize, BComparable<A> template, PtrType ptrType) {
    this(ByteBuffer.allocate(blockSize), template, true, ptrType, (short) 0);
  }

  /**
   * Create a ByteBuffer with no resvered space
   * @param block byte array to wrap
   * @param template BComparable template
   * @param ptrType ptr type
   */
  public BCBuffer(byte[] block, BComparable<A> template, PtrType ptrType){
    this(ByteBuffer.wrap(block), template, ptrType, (short) 0);
  }

  /**
   * Create a ByteBuffer with reserved space
   * @param block buffer to use
   * @param template BComparable template
   * @param ptrType ptr type
   * @param reservedSpace bytes to reserve
   */
  public BCBuffer(ByteBuffer block, BComparable<A> template, PtrType ptrType, short reservedSpace) {
    this(block, template, true, ptrType, reservedSpace);
  }

  /**
   * Create a ByteBuffer with no reserved space
   * @param block buffer to use
   * @param template BComparable template
   * @param ptrType ptr type
   */
  public BCBuffer(ByteBuffer block, BComparable<A> template, PtrType ptrType){
    this(block, template, ptrType, (short) 0);
  }


  /**
   * Internal costructor. Either create or open a buffer
   * @param block buffer to use
   * @param template BComparable template
   * @param newBlock true if new block
   * @param ptrType pointer type
   * @param reservedSpace number of bytes to reserve
   */
  protected BCBuffer(ByteBuffer block, BComparable<A> template, boolean newBlock,
                     PtrType ptrType, short reservedSpace) {
    try {
      this.keyType = template;
      setBlock(block, newBlock, ptrType, reservedSpace);
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  /**
   * Calculates how many extra bytes will be required to store a number of
   * elements (not including the elements themselves)
   * @param maxElements maximum number of elements
   * @param ptrType ptr type
   * @return number of bytes
   */
  public static int bytesNeeded(int maxElements, PtrType ptrType) {
    int bytes = 2; //resservedSpace length
    bytes += 1 + (ptrType.size() * 2); //headerSize ptrSize, numElements, bytesWritten
    bytes += ptrType.size() * maxElements;
    return bytes;
  }

  private static int calculateOptimum(int total, int divider) {
    int optimal = total / divider;
    int remainder = total % divider;
    optimal = (remainder > divider / 2) ? optimal + 1 : optimal;
    return optimal;
  }

  private static <A> void getKeysPrevious(BCBuffer<A>[] blocks, int index,
                                                                       int optimal) {
    int blockBytes;
    BComparable<A> current;
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

  private static <A> void putKeysPrevious(BCBuffer<A>[] blocks, int index,
                                                                       int optimal) {
    int blockBytes;
    BComparable<A> current;
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
   * @param blocks An array of BCBuffers that should be redistributed.
   * @param <A> value type
   */
  public static <A> void redistribute(BCBuffer<A>[] blocks) {
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
   * Clear this buffer from elements
   */
  public void clear() {
    high = 0;
    bytesWritten = 0;
    writeNumElements(0);
    writeBytesWritten(0);
  }

  /**
   * Check if this buffer contains the given element
   *
   * @param element the element to search for.
   * @return true if found
   */
  public boolean contains(BComparable<A> element) {
    return search(element) >= 0;
  }

  /**
   * Deletes an element from this buffer
   *
   * @param element The element to delete
   * @return The deleted element or null
   */
  public BComparable<A> delete(BComparable<A> element) {
    return delete(search(element));
  }

  /**
   * Delete element at index.
   *
   * @param idx index
   * @return the deleted key or null if no such idx
   */
  public BComparable<A> delete(int idx) {
    if (idx >= high || idx < 0)
      return null;

    // read the physical position:
    int pPos = getPhysicalPos(idx);
    block.position(pPos);
    BComparable<A> toDelete = keyType.from(block);
    int firstPos = capacity - bytesWritten;
    int byteSize = toDelete.byteSize();
    if (idx < high - 1) { // we have to compact the array:
      byteBufferCopy(getIndexPos(idx + 1), getIndexPos(idx), (high - 1 - idx)
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
   * Check if this buffer has room for an additional element
   *
   * @param element the element to check
   * @return True if element can be stored.
   */
  public boolean fits(BComparable<A> element) {
    return reservedSpace + headerSize + bytesWritten + ((high + 1) * ptrSize)
        + element.byteSize() <= capacity;
  }

  /**
   * Checks if this buffer can fit all elements in another buffer. Will not exclude any
   * duplicates.
   *
   * @param other buffer to check.
   * @return true if the two buffers can be merged.
   */
  public boolean fits(BCBuffer<A> other) {
    int totDataBytes = other.getDataBytes() + getDataBytes();
    int totElements = other.getNumberOfElements() + getNumberOfElements();
    if (reservedSpace + headerSize + totDataBytes + (totElements * ptrSize) > capacity)
      return false;
    return true;
  }

  /**
   * Checks if this buffer can fit all elements in another buffer plus an additional elements. Will not exclude any
   * duplicates.
   *
   * @param other      the buffer to merge with.
   * @param additional additional element
   * @return true if the the buffers and element can be merged.
   */
  public boolean fits(BCBuffer<A> other, BComparable<A> additional) {
    int totDataBytes = other.getDataBytes() + getDataBytes()
        + additional.byteSize();
    int totElements = other.getNumberOfElements() + getNumberOfElements() + 1;
    return reservedSpace + headerSize + totDataBytes + ((totElements) * ptrSize) <= capacity;
  }

  /**
   * Element at index.
   *
   * @param idx index of element
   * @return element or null if the index was out of range
   */
  public BComparable<A> get(int idx) {
    if (idx >= high || idx < 0)
      return null;
    block.position(getPhysicalPos(idx));
    return keyType.from(block);
  }

  /**
   * Find an element in this buffer. This method triggers a binary search
   *
   * @param element Element to find
   * @return element or null if not found
   */
  public BComparable<A> get(BComparable<A> element) {
    return get(search(element));
  }

  /**
   * Get the underlying ByteBuffer.
   *
   * @return underlying ByteBuffer.
   */
  public ByteBuffer getBlock() {
    return block;
  }

  /**
   * Return the current buffer as an array. In case the ByteBuffer is not backed
   * by an array the returned array will be a copy of this block
   * @return array of bytes containing the keys stored in this block
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
   * Check if this buffer is backed by a byte[]
   * @return true if backed by byte[]
   */
  public boolean hasArray(){
    return block.hasArray();
  }

  /**
   * Number of bytes written in this Buffer including any reserved space.
   * @return bytes written
   */
  public int getBytesWritten() {
    return reservedSpace + headerSize + bytesWritten + (high * ptrSize);
  }

  /**
   * Number of data bytes and pointer bytes written in this file.
   *
   * @return bytes written
   */
  public int getDataAndPointersBytes() {
    return bytesWritten + (high * ptrSize);
  }

  /**
   * Number of bytes used to store the elements in this block (excluding
   * pointers).
   *
   * @return bytes written
   */
  public int getDataBytes() {
    return bytesWritten;
  }

  /**
   * Get the first (smallest) element in this buffer
   *
   * @return element or null if buffer is empty
   */
  public BComparable<A> getFirst() {
    return get(0);
  }

  /**
   * Get the last (largest) element in this buffer
   *
   * @return element or null if buffer is empty
   */
  public BComparable<A> getLast() {
    return get(high - 1);
  }

  /**
   * Number of elements in this buffer
   *
   * @return number of elements
   */
  public int getNumberOfElements() {
    return high;
  }

  /**
   * PtrType used in this buffer
   *
   * @return pointer type
   */
  public PtrType getPointerType() {
    return ptrType;
  }

  /**
   * Get the size of the resevered space
   *
   * @return number of bytes
   */
  public int getReservedSpace() {
    return reservedSpace - 2;
  }

  /**
   * Get the start position of the reserved space in this buffer. To read the
   * reserved space to an array
   * <pre>
   * {@code
   *  byte[] space = new byte[myBuffer.getReservedSpace()];
   *  ByteBuffer buffer = myBuffer.getBlock();
   *  buffer.position(myBuffer.getReservedSpaceStart());
   *  buffer.get(space);
   * }
   * </pre>
   *
   * @return position in buffer
   */
  public int getReservedSpaceStart() {
    return 2;
  }

  /**
   * Inserts an element in this block. Performs a binary search to find the correct
   * position.
   *
   * @param element The element to insert.
   * @return the index if successful, -1 otherwise.
   */
  public int insert(BComparable<A> element) {
    // check if it can be inserted here:
    if (!fits(element))
      return -1;
    int pos = search(element);
    if (pos >= 0)
      return -1;
    pos++;
    pos = Math.abs(pos);

    // calculate physical position:
    int pPos = capacity - bytesWritten - element.byteSize();

    // shift all the elments to the right of pos to fit the pPos (e.g. a short)
    if (pos < high)
      byteBufferCopy(getIndexPos(pos), getIndexPos(pos + 1), (high - pos)
          * ptrSize);

    setPhysicalPos(pos, pPos);
    high++;
    writeNumElements(high);
    bytesWritten += element.byteSize();
    writeBytesWritten(bytesWritten);
    block.position(pPos);
    element.to(block);
    return pos;
  }

  /**
   * Inserts an element at the end of this buffer. Observe that
   * this will not guarantee that the block stays sorted
   *
   * @param element element to insert
   * @return true if the element was inserted
   */
  public boolean insertUnsorted(BComparable<A> element) {
    if (!fits(element))
      return false;
    int pPos = capacity - bytesWritten - element.byteSize();
    setPhysicalPos(high, pPos);
    high++;
    writeNumElements(high);
    bytesWritten += element.byteSize();
    writeBytesWritten(bytesWritten);
    block.position(pPos);
    element.to(block);
    return true;
  }

  /**
   * Check if this buffer is empty
   * @return true if buffer is empty
   */
  public boolean isEmpty() {
    return getNumberOfElements() == 0;
  }

  @Override
  public Iterator<BComparable<A>> iterator(boolean descend, BComparable<A> from, boolean fromInclusive,
                                           BComparable<A> to, boolean toInclusive) {
    return descend ? new BCBufferDescendIter(this, from, fromInclusive, to, toInclusive) :
        new BCBufferIter(this, from, fromInclusive, to, toInclusive);
  }

  /**
   * Merge this buffer with another buffer
   * @param other buffer to merge with
   * @return this buffer
   */
  public BCBuffer<A> merge(BCBuffer<A> other) {
    //rewrite needs to always copy to this buffer
    if(other.isEmpty()) return this;
    else if(isEmpty() || other.getFirst().compareTo(getLast()) > 0){ //all keys in other are large...can insert unsorted
      for(BComparable<A> key : other){
        if(!insertUnsorted(key))
          throw new BufferOverflowException();
      }
    } else { //insert as usual
      for(BComparable<A> key : other){
        if(insert(key) < 0)
          throw new BufferOverflowException();
      }
    }
    return this;
  }

  /**
   * Binary search for element. Same contract as Arrays.binarySearch
   *
   * @param element The element to search for
   * @return index
   * @see java.util.Arrays#binarySearch(Object[], Object)
   */
  public int search(BComparable<A> element) {
    int highSearch = high - 1;
    int low = 0, mid;
    BComparable<A> current;
    while (low <= highSearch) {
      mid = (low + highSearch) / 2;
      block.position(getPhysicalPos(mid));
      current = keyType.from(block);
      int cmp = current.compareTo(element);
      if (cmp < 0)
        low = mid + 1;
      else if (cmp > 0)
        highSearch = mid - 1;
      else
        return mid; // key found
    }
    return -(low + 1);
  }

  /**
   * Binary search for element by using byte level comparison. Same contract as Arrays.binarySearch
   *
   * @param element The element to search for
   * @return index
   * @see java.util.Arrays#binarySearch(Object[], Object)
   */
  public int searchBC(BComparable<A> element) {
    int low = 0;
    int highSearch = high - 1, mid;
    ByteBuffer bbKey = element.to();
    while (low <= highSearch) {
      mid = (low + highSearch) / 2;
      int midValOffset = getPhysicalPos(mid);
      int cmp = element.byteCompare(midValOffset, block, 0, bbKey);
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
   * If the elements in this buffer has been inserted unsorted use sort to sort the
   * contents. This method works as follows:<br>
   * it reads in every element into an Bcomarable[], and then calls either
   * Arrays.sort or Arrays.parallelSort.
   * <p>
   * If you are reading in many keys at once it is a good idea to first store
   * them in an array than call sort, and then insert them using insertUnsorted
   * into this block.
   * </p>
   *
   * @param parallelSort If true use Arrays.parallelSort, otherwise use Arrays.sort
   * @return this buffer
   */
  public BCBuffer<A> sort(boolean parallelSort) {
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
      insertUnsorted(toSort[i]);
    }
    return this;
  }

  /**
   * Split this buffer in to. The returned buffer will contain
   * the larger elements and contain a copy of the reserved space
   * @return buffer with larger elements
   */
  public BCBuffer<A> split() {
    return split(new BCBuffer<>(capacity, keyType, ptrType, (short) (reservedSpace - 2)));
  }

  /**
   * Split this buffer in to. The returned buffer will contain
   * the larger elements and contain a copy of the reserved space
   * @param other buffer to split to
   * @return buffer with larger elements
   */
  public BCBuffer<A> split(BCBuffer<A> other) {
    int half = bytesWritten / 2;
    BComparable<A> lastKey;
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
   * The actual storage capacity of this buffer. Observe that each element
   * will store an additional number of bytes for its pointer.
   *
   * @return storage in bytes
   */
  public int storageCapacity() {
    return capacity - headerSize - reservedSpace;
  }

  @Override
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
   * This will update a previously stored element. Use with caution since it will
   * just overwrite the current data at the given index. Thus, the element size
   * cannot be changed.
   *
   * @param element   element to update
   * @param idx position of element
   */
  public void update(BComparable<A> element, int idx) {
    block.position(getPhysicalPos(idx));
    element.to(block);
  }

  /**
   * Check if this block is sorted
   * @return
   */
  public boolean isSorted(){
    if(getNumberOfElements() < 2)
      return true;
    BComparable<A> prev = get(0);
    BComparable<A> next;
    for(int i = 1; i < getNumberOfElements() -1; i++){
      next = get(i);
      if(prev.compareTo(next) >= 0)
        return false;
      prev = next;
    }
    return true;
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

  int getLowerBound(BComparable<A> key, boolean inclusive) {
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

  int getUpperBound(BComparable<A> key, boolean inclusive) {
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

  /**
   * Size of element pointer. The larger the size the larger the buffer one can have
   */
  public enum PtrType {
    /**
     * BIG has 4 byte pointer meaning it can address 2 gb buffers
     */
    BIG,
    /**
     * Normal is a 2 byte pointer and can address 32kb buffers
     */
    NORMAL,
    /**
     * Small is a 1 byte pointer and can address 128b buffers
     */
    TINY;

    public static PtrType from(byte b) {
      switch (b) {
        case 4:
          return BIG;
        case 2:
          return NORMAL;
        case 1:
          return TINY;
        default:
          throw new Error("unknown pointer size");
      }
    }

    public byte size() {
      switch (this) {
        case BIG:
          return 4;
        case NORMAL:
          return 2;
        case TINY:
          return 1;
      }
      throw new Error("error");
    }
  }
}
