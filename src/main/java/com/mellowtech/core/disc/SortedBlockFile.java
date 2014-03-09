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
package com.mellowtech.core.disc;

import java.io.IOException;
import java.util.logging.Level;

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteStorable;

/**
 * The SortedBlockFile adds sorting to a BlockFile. It uses SortedBlocks to sort
 * keys within each block. <br>
 * <br>
 * In a production environment (with many keys) it is recommended, though, to
 * use some sort of external index to keep track of block boundaries, i.e, to
 * keep track of in which block keys should be inserted. The reason for this is
 * the fact that the SortedBlockFile has to read each block from file in the
 * binary search (which takes time).<br>
 * <br>
 * The overhead of using the SortedBlockFile is that of using SortedBlocks and
 * the additional information that a BlockFile keeps, i.e., logical/physical
 * mapping of blocks.
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class SortedBlockFile extends BlockFile {

  private ByteStorable keyType;

  /**
   * Creates a new <code>SortedBlockFile</code> instance. A sortedblock is
   * specified by its blocksize and keyType. The SortedBlockFile is a BlockFile
   * that uses SortedBlocks for internal storage.<br>
   * This constructor first calls super(blockSize, fileName, create) and then
   * sets the keyType.
   * 
   * @param blockSize
   *          the block size
   * @param fileName
   *          the name of the file without extensions
   * @param create
   *          indicate if a new file should be created
   * @param keyType
   *          the type of keys the blockFile handles.
   * @exception IOException
   *              if an error occurs
   * @see BlockFile#BlockFile(int, String, boolean)
   */
  public SortedBlockFile(int blockSize, String fileName, boolean create,
      ByteStorable keyType) throws IOException {
    super(blockSize, fileName, create);
    this.keyType = keyType;
  }

  /**
   * Inserts a key into the sorted file. This insert will use a specialized
   * binary search to find the correct position in the file. If the key exists
   * insert will do nothing
   * 
   * @param key
   *          The key to insert
   * @exception IOException
   *              If an error during reading/writing to disc occurs
   */
  public void insert(ByteStorable key) throws IOException {
    int blockNo = 0;
    SortedBlockIndex sbi = binaryFindBlock(key);
    SortedBlock sb;
    if (sbi == null) { // we do not have any blocks
      insertBlock(-1);
      sb = new SortedBlock();
      sb.setBlock(new byte[getBlockSize()], keyType, true,
          SortedBlock.PTR_NORMAL);
    }
    else {
      blockNo = sbi.index;
      sb = sbi.sb;
    }
    if (sb.containsKey(key))
      return;
    else if (sb.fitsKey(key))
      sb.insertKey(key);
    else { // perform split
      insertBlock(blockNo); // make room for the new block
      SortedBlock sb1 = sb.splitBlock();
      if (key.compareTo(sb.getLastKey()) < 0) // key should be inserted into the
        // first block
        sb.insertKey(key);
      else
        sb1.insertKey(key);
      writeBlock(sb1.getBlock(), blockNo + 1);
    }
    writeBlock(sb.getBlock(), blockNo);
  }

  /**
   * Deletes a key from this sorted file. Uses a specialized binary search to
   * find the correct key.
   * 
   * @param key
   *          Key to delete.
   * @return The deleted key (read from file)
   * @exception IOException
   *              if an error during read/write to file occurs
   */
  public ByteStorable delete(ByteStorable key) throws IOException {
    SortedBlockIndex sbi = binaryFindBlock(key);
    if (sbi == null)
      return null;
    ByteStorable deletedKey = sbi.sb.deleteKey(key);
    if (deletedKey == null)
      return null;

    // now do some tweaking if neccesary...if the block
    // is less than half full do a merge or redistribue;
    SortedBlock sb = sbi.sb;
    if (sb.getBytesWritten() >= (sb.getBlock().length / 2))
      return deletedKey;

    int prev = sbi.index - 1;
    int next = sbi.index + 1;

    // first check the previous:
    byte[] b = (prev >= 0) ? readBlock(prev) : null;
    SortedBlock prevB = null, nextB = null;
    if (b != null) {
      prevB = new SortedBlock();
      prevB.setBlock(b, keyType, false, sb.getPointerSize());
      if (appliedMerge(prevB, prev, sb, sbi.index))
        return deletedKey;
    }
    // next check the next:
    b = (next < getNumberOfBlocks()) ? readBlock(next) : null;
    if (b != null) {
      nextB = new SortedBlock();
      nextB.setBlock(b, keyType, false, sb.getPointerSize());
      if (appliedMerge(sb, sbi.index, nextB, next))
        return deletedKey;
    }
    // either of the two neighbors could be merged...redistribue
    // ugly hack...
    redistributeBlocks(prevB, sb, nextB, prev, sbi.index, next);
    return deletedKey;
  }

  /**
   * Perfoms a binary search in this file.
   * 
   * @param key
   *          the key to search for
   * @return the key read from disc
   * @exception IOException
   *              if an error during read/write to disc occurs
   */
  public ByteStorable search(ByteStorable key) throws IOException {
    SortedBlockIndex sbi = binaryFindBlock(key);
    if (sbi != null)
      return sbi.sb.getKey(key);
    else
      return null;
  }

  /**
   * Prints the keys in this blockfile in ascending order. Should not be used on
   * very large files.
   * 
   * @return keys in this file.
   */
  public String toString() {
    SortedBlock sb = new SortedBlock();
    StringBuffer sbuff = new StringBuffer();
    int numItems = 0;
    try {
      for (int i = getFirstBlockNo(); i < getNumberOfBlocks(); i++) {
        sb.setBlock(readBlock(i), keyType, false, sb.PTR_NORMAL);
        numItems += sb.getNumberOfElements();
        sbuff.append("**********BLOCK logical pos:" + i + " physical pos: "
            + +getPhysicalBlockNo(i) + "************************\n");
        sbuff.append(sb.toString());
        sbuff.append("**********************************************\n\n\n");
        sbuff.append("total elements: " + numItems);
      }
    }
    catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "could not print block file", e);
      return null;
    }

    return sbuff.toString();
  }

  private SortedBlockIndex binaryFindBlock(ByteStorable key) throws IOException {
    int low = 0, mid = 0, cmp1, cmp2;
    int high = getNumberOfBlocks() - 1;
    SortedBlock sb = new SortedBlock();
    while (low <= high) {
      mid = (low + high) / 2;
      sb.setBlock(readBlock(mid), keyType, false, sb.PTR_NORMAL);

      // there are no more blocks to scan, i.e we have reached either the first
      // or last block,
      // always return that block as the block where the key should be:

      // get the first and last keys in the current block:
      cmp1 = key.compareTo(sb.getFirstKey());
      cmp2 = key.compareTo(sb.getLastKey());

      if (cmp1 >= 0 && cmp2 <= 0) // it should be inserted in this block
        return new SortedBlockIndex(mid, sb);
      else if (cmp1 < 0 && mid > low) // the key is smaller and there are more
        // blocks to search
        high = mid - 1;
      else if (cmp2 > 0 && mid < high) { // the key is larger and there are
        // more blocks:
        low = mid + 1;
      }
      else
        return new SortedBlockIndex(mid, sb);
    }
    // in this special case we do not have any blocks...just return -1
    return null;
  }

  private boolean appliedMerge(SortedBlock sb1, int block1, SortedBlock sb2,
      int block2) throws IOException {
    if (sb1.canMerge(sb2)) {
      sb1.mergeBlock(sb2);
      removeBlock(block2);
      writeBlock(sb1.getBlock(), block1);
      return true;
    }
    return false;
  }

  private boolean redistributeBlocks(SortedBlock first, SortedBlock second,
      SortedBlock third, int fNo, int sNo, int tNo) throws IOException {
    SortedBlock blocks[] = new SortedBlock[2];
    if (first == null || third == null) {
      blocks[0] = (first != null) ? first : second;
      blocks[1] = (third != null) ? third : second;
      SortedBlock.redistribute(blocks);
      if (first != null) {
        writeBlock(first.getBlock(), fNo);
        writeBlock(second.getBlock(), sNo);
        return true;
      }
      else {
        writeBlock(second.getBlock(), sNo);
        writeBlock(third.getBlock(), tNo);
        return false;
      }
    }
    else {
      blocks[0] = (first.getDataBytes() >= third.getDataBytes()) ? first
          : second;
      blocks[1] = (first.getDataBytes() < third.getDataBytes()) ? third
          : second;
      SortedBlock.redistribute(blocks);
      if (first.getDataBytes() >= third.getDataBytes()) {
        writeBlock(first.getBlock(), fNo);
        writeBlock(second.getBlock(), sNo);
        return true;
      }
      else {
        writeBlock(second.getBlock(), sNo);
        writeBlock(third.getBlock(), tNo);
        return false;
      }
    }
  }
}

class SortedBlockIndex {
  SortedBlock sb;
  int index;

  public SortedBlockIndex(int index, SortedBlock sb) {
    this.sb = sb;
    this.index = index;
  }
}
