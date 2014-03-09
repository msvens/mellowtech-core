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



import com.mellowtech.core.CoreLog;
import com.mellowtech.core.cache.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;

/**
 * The BlockFile is a simple representation of a file divided into equal size
 * blocks. The blocks in the file can be of any size. The BlockFile allows for
 * insertion/deletion of blocks anywhere in the file, without any rearrangement
 * of the actual data. This guaratees for fast inserts and deletes. However,
 * this also means that the BlockFile over time becomes defragmented.
 * Defragementation can be taken care of by calling the method defragment().
 * Once a BlockFile has been created the size of each block can't be changed.<br>
 * <br>
 * 
 * The BlockFile works by using a logical and physical mapping. The logical
 * mapping of blocks is kept in memory and it is only the logical mapping that
 * is changed when blocks are inserted and deleted. Thus, the logical and
 * physical mapping does not nessecarily have to be the same. So the first
 * logical block could be at the last physical position in the file. To rearange
 * the blocks the defragment() method should be used.<br>
 * <br>
 * 
 * The BlockFile use two physical files. One to store the data and one to store
 * the logical/physical mapping and other header information. The average
 * overhead when using the block file are 2 bytes extra for each block. The
 * BlockFile will, however, allocate room for 256 blocks to start with.
 * 
 * @author Martin Svensson
 * @version 1.0
 */
@Deprecated
public class BlockFile {


  /**
   * Describe constant <code>DEFAULT_BLOCK_SIZE</code> here.
   * 
   */
  public static final int DEFAULT_BLOCK_SIZE = 1024;

  /**
   * Describe constant <code>BLOCK_FILE_EXTENSION</code> here.
   * 
   */
  public static final String BLOCK_FILE_EXTENSION = ".blf";

  /**
   * Describe constant <code>POINTER_FILE_EXTENSION</code> here.
   * 
   */
  public static final String POINTER_FILE_EXTENSION = ".ptr";

  public int[] pointers;

  private int blockSize, currentBlock, numBlocks, maxBlocs;

  private int highBlock;

  private String fileName;

  private RandomAccessFile file;

  private AbstractCache<Integer, byte[]> cache;


  // private AbstractCache fileCache;
  private int writeOps = 0, readOps = 0;

  /**
   * Opens an already existing blockfile.
   * 
   * @param fileName
   *          Physical filename. Do not use file extension.
   * @exception IOException
   *              if the the blockfile could not be read from disc
   */
  public BlockFile(String fileName) throws IOException {
    this(DEFAULT_BLOCK_SIZE, fileName, false);
  }

  /**
   * Eiter opens or creates a new blockfile.
   * 
   * @param fileName
   *          Physical filename. Do not use file extension.
   * @param create
   *          If true a new block file will be created.
   * @exception IOException
   *              if the the blockfile could not be read from disc
   */
  public BlockFile(String fileName, boolean create) throws IOException {
    this(DEFAULT_BLOCK_SIZE, fileName, create);
  }

  /**
   * Use this primarily for create a new blockfile.
   * 
   * @param blockSize
   *          the size of each block in this file. This is ignored when opening
   *          an existing file. The size is in bytes.
   * @param fileName
   *          pysical filename. Do not use extension.
   * @param create
   *          if true create a new file.
   * @exception IOException
   *              if the the blockfile could not be read from disc
   */
  public BlockFile(int blockSize, String fileName, boolean create)
      throws IOException {
    this.blockSize = blockSize;
    this.fileName = fileName;

    if (create) {
      this.pointers = new int[256];
      // blockSize = blockSize;
      createFile();
    }
    else {
      openFile();
    }
  }

  /**
   * Closes the blockfile. This method has to be called in order to guarantee
   * that the block file stays intact. Specifically closeFile write header
   * information to disc.
   * 
   * @exception IOException
   *              if read/write operations on the blockfile fails.
   */
  public void closeFile() throws IOException {
    // flush possible cache:
    /*
     * if(fileCache != null) fileCache.emptyCache();
     */
    writeHeader();
    if(cache != null && !cache.isReadOnly()){
      CoreLog.L().finest("Closing Block File with "+cache.getCurrentSize()+" blocks in the cache");
      cache.emptyCache();
    }
    file.close();
  }

  /**
   * Delete this blockfile on disc
   * @throws IOException
   */
  public void deleteFile() throws IOException{
    //delete header:
    String fName = fileName + POINTER_FILE_EXTENSION;
    File f = new File(fName);
    f.delete();

    //delete blocks:
    if(cache != null) cache.emptyCache();
    file.close();
    fName = fileName + BLOCK_FILE_EXTENSION;
    f = new File(fName);
    f.delete();


  }

  /**
   * Returns the block size.
   * 
   * @return block size.
   */
  public int getBlockSize() {
    return blockSize;
  }

  /**
   * Return current logical block index
   * 
   * @return logical block index.
   */
  public int getCurrentBlockNo() {
    return currentBlock;
  }

  /**
   * The logical index to the first block is always 0. However, due to
   * fragmentation the physical index of the first block can be something else
   * than 0.
   * 
   * @return the index to the first block in this file.
   */
  public int getFirstBlockNo() {
    return 0;
  }

  /**
   * The number of blocks currently in the block (not counting deleted blocks).
   * 
   * @return number of blocks.
   */
  public int getNumberOfBlocks() {
    return numBlocks;
  }

  /**
   * The last logical index to the last block in this file.
   * 
   * @return the index of the last block in this file.
   */
  public int getLastBlockNo() {
    return numBlocks - 1;
  }

  /**
   * Retrieves the logical index that corresponds to a physical postion. O(n)
   * performance. It scans throuch the logcial mapping array until it finds the
   * physical mapping.
   * 
   * @param physicalPos
   *          a physical block postion.
   * @return -1 if the physicalPosition does not have a logical mapping in this
   *         file
   */
  public int getLogicalBlockNo(int physicalPos) {
    if (physicalPos < 0 || physicalPos > highBlock) {
      return -1;
    }
    for (int i = 0; i < numBlocks; i++) {
      if (pointers[i] == physicalPos)
        return i;
    }
    return -1;
  }

  /**
   * Retrieves the physical index that corresponds to a logical position. O(1)
   * performance
   * 
   * @param logicalPos
   *          an <code>int</code> value
   * @return -1 if the logicalPosition is out of range
   */
  public int getPhysicalBlockNo(int logicalPos) {
    if (logicalPos < 0 || logicalPos >= numBlocks)
      return -1;
    return pointers[logicalPos];
  }

  /**
   * Iterator over the logical blocks in this file
   * 
   * @return an <code>Iterator</code> value
   */
  public Iterator iterator() {
    return new BFIterator();
  }

  /**
   * Iterator over the logical blocks in this file starting from the given
   * block.
   * 
   * @param blockNo
   *          starting block
   * @param logical
   *          true if block index is logical, false if it is physical
   * @return an <code>Iterator</code> value
   */
  public Iterator iterator(int blockNo, boolean logical) {
    return new BFIterator(blockNo, logical);
  }

  /**
   * Reads the current block from file.
   * 
   * @return an array of bytes
   * @exception IOException
   *              if physical reading fails.
   * @see BlockFile#getCurrentBlockNo()
   */
  public byte[] readCurrentBlock() throws IOException {
    return readBlock(currentBlock);
  }

  /**
   * Reads the physical block at the specified logical position in the file.
   * 
   * @param blockNo
   *          the logcial index.
   * @return an array of bytes.
   * @exception IOException
   *              if physical reading fails.
   */
  public byte[] readBlock(int blockNo) throws IOException {
    byte[] b = readPhysicalBlock(pointers[blockNo]);
    currentBlock = blockNo;
    return b;
  }

  /**
   * Reads the block at the specified physical position. Note that the block
   * read can be a deleted block since the BlockFile does no such checking. The
   * operation will not change the current block position.
   * 
   * @param blockNo
   *          the logcial index.
   * @return an array of bytes.
   * @exception IOException
   *              if physical reading fails.
   */
  public byte[] readPhysicalBlock(int blockNo) throws IOException {

    if (blockNo < 0 || blockNo > highBlock)
      return null;

    if(cache != null){
      try {
        return cache.get(blockNo);
      } catch (NoSuchValueException e) {
        CoreLog.L().log(Level.SEVERE, "could not read block", e);
        throw new IOException(e.toString());
      }
    }
    else{
      byte[] b;
      b = new byte[blockSize];
      file.seek(blockNo * blockSize);
      file.readFully(b);
      readOps++;
      return b;
    }
  }

  /*
   * public void setCache(AbstractCache cache){ if(this.fileCache != null){
   * //flush old cache fileCache.emptyCache(); } this.fileCache = cache;
   * if(this.fileCache != null) fileCache.setRemover(new BlockFile.Callback()); }
   */

  /**
   * Write to the current logical index.
   * 
   * @param b
   *          the bytes to be written.
   * @exception IOException
   *              if physical writing fails.
   */
  public void writeCurrentBlock(byte[] b) throws IOException {
    writeBlock(b, currentBlock);
  }

  /**
   * Write to the specified logical position.
   * 
   * @param b
   *          the bytes to be written.
   * @param blockNo
   *          the logical index.
   * @exception IOException
   *              if physical writing fails.
   */
  public void writeBlock(byte[] b, int blockNo) throws IOException {
    writePhysicalBlock(b, pointers[blockNo]);
  }

  /**
   * Write to the specified physical postion.
   * 
   * @param b
   *          the bytes to be written.
   * @param blockNo
   *          the logical index.
   * @exception IOException
   *              if physical writing fails.
   */
  public void writePhysicalBlock(byte[] b, int blockNo) throws IOException {
    /*
     * if(fileCache != null){ fileCache.put(new Integer(blockNo), b); return; }
     */
    if(cache != null && !cache.isReadOnly())
      cache.put(blockNo,  b);
    else{
      file.seek(blockNo * blockSize);
      file.write(b);
      writeOps++;
      if(cache != null) cache.remove(blockNo);
    }

  }

  /**
   * Read the next logical block from the current block index.
   * 
   * @return an array of bytes.
   * @exception IOException
   *              if physical reading fails.
   */
  public byte[] readNextBlock() throws IOException {
    if (currentBlock < numBlocks)
      return readBlock(currentBlock + 1);
    else
      return null;
  }

  /**
   * Read the previous logical block from the current block index.
   * 
   * @return an array of bytes.
   * @exception IOException
   *              if physical reading fails.
   */
  public byte[] readPreviousBlock() throws IOException {
    if (currentBlock == 0)
      return null;
    return readBlock(currentBlock - 1);
  }

  /**
   * Insert a new block after the specified logical index. So if the specified
   * index is 0 a the new block will have the logcial index of 1. To insert a
   * block at the first logical position in the file use -1.
   * 
   * @param blockNo
   *          the previous index of the current block.
   * @exception IOException
   *              if physical writing fails.
   */
  public void insertBlock(int blockNo) throws IOException {
    try {
      currentBlock = blockNo + 1;
      highBlock += 1;
      numBlocks++;

      if ((numBlocks) == pointers.length - 1) { // we have to grow the pointers
       CoreLog.L().finest("growing pointers "+pointers.length + " " + highBlock+" "+this.fileName);
        int[] tmp = new int[pointers.length * 2];
        System.arraycopy(pointers, 0, tmp, 0, pointers.length);
        pointers = tmp;
      }
      if (currentBlock == numBlocks)
        pointers[currentBlock] = highBlock;
      else {
        System.arraycopy(pointers, currentBlock, pointers, currentBlock + 1,
            numBlocks - currentBlock);
      }
      pointers[currentBlock] = highBlock;
    }
    catch (Exception e) {
      CoreLog.L().severe(pointers.length + " " + currentBlock + " " + numBlocks
          + " " + blockNo);
      throw new IOException(e.toString());
    }
  }

  /**
   * Remove the specified block from the file. It is only the logical reference
   * that will be unlinked, i.e. the method does not fill the block with zeros
   * or something. Also note that the physical space will be unavailable to new
   * blocks until a defragment has been issued.
   * 
   * @param blockNo
   *          the logical block to delete
   * @see BlockFile#defragment()
   */
  public void removeBlock(int blockNo) {
    /*
     * if(fileCache != null) fileCache.remove(new Integer(pointers[blockNo]));
     */
    if (numBlocks - 1 == 0) {
      try {
        file.setLength(0);
      }
      catch (IOException e) {
        CoreLog.L().log(Level.WARNING, "", e);
      }
      pointers = new int[256];
      currentBlock = -1;
      numBlocks = 0;
      highBlock = -1;
      return;
    }

    System.arraycopy(pointers, blockNo + 1, pointers, blockNo, numBlocks
        - blockNo + 1);
    numBlocks--;
  }

  public void setCache(boolean readOnly, int size, boolean mem){
    int numCacheItems = mem ? size / this.blockSize : size;

    Remover <Integer, byte[]> remover = null;
    if(!readOnly){
      remover = new Remover<Integer, byte[]>() {
        @Override
        public void remove(Integer key, CacheValue<byte[]> value) {
          if(value.isDirty())
            try {
              file.seek(key * blockSize);
              file.write(value.getValue());
              writeOps++;
            } catch (IOException e) {
              CoreLog.L().log(Level.SEVERE, "could not write block", e);
            }
        }
      };
    }

    Loader<Integer, byte[]> loader = new Loader<Integer, byte[]>() {
      @Override
      public byte[] get(Integer key) throws Exception, NoSuchValueException {
        byte b[] = new byte[blockSize];
        file.seek(key * blockSize);
        file.readFully(b);
        readOps++;
        return b;
      }
    };
    cache = new CacheLRU<Integer, byte[]> (remover, loader, numCacheItems);

  }


  /**
   * Prints header information of the block file and the logical and physical
   * mapping.
   */
  public String toString() {
    StringBuffer sbuff = new StringBuffer();
    sbuff.append("File name: " + fileName + '\n');
    sbuff.append("Block size: " + blockSize + '\n');
    sbuff.append("Num blocks: " + numBlocks + '\n');
    sbuff.append("Highest logical block: " + highBlock + '\n');
    sbuff.append("block capacity: " + pointers.length + '\n');
    sbuff.append("Logical and physical block order: ");
    for (int i = 0; i < numBlocks; i++) {
      sbuff.append(i + ":" + pointers[i] + " ");
    }
    sbuff.append('\n');
    return sbuff.toString();
  }

  /**
   * The defragment removes unused space and remapps the logical/physical
   * mapping so that the logical block 0 will be the first block in the file,
   * and so on.
   * 
   * @exception IOException
   *              if physical read/write fails.
   */
  public void defragment() throws IOException {
    if(cache != null){
      cache.emptyCache();
    }
    // writeHeader();
    /*
     * if(fileCache != null) fileCache.emptyCache();
     */
    RandomAccessFile raf = new RandomAccessFile("defragment.tmp", "rw");
    RandomAccessFile raf1 = new RandomAccessFile("defragment_pointers.tmp",
        "rw");
    synchronized (file) {
      int i;
      for (i = 0; i < numBlocks; i++) {
        raf.write(readBlock(i));

      }
      raf.close();

      // Write header:
      ByteBuffer bb = ByteBuffer.allocate(20 + (numBlocks * 4));
      bb.putInt(numBlocks);
      bb.putInt(currentBlock);
      bb.putInt(pointers.length);
      bb.putInt(blockSize);
      bb.putInt((numBlocks - 1));

      for (i = 0; i < numBlocks; i++) {
        bb.putInt(i);
      }
      bb.flip();
      raf1.getChannel().write(bb);
      raf1.close();
      file.close();

      File f = new File(fileName + BLOCK_FILE_EXTENSION);
      f.delete();
      File f1 = new File("defragment.tmp");
      f1.renameTo(f);

      f = new File(fileName + POINTER_FILE_EXTENSION);
      f.delete();
      f1 = new File("defragment_pointers.tmp");
      f1.renameTo(f);
      openFile();
    }

  }

  /**
   * Tries to close the file if it is open
   * 
   */
  /** ******************PRIVATE SECTION**************************************** */
  private void writeHeader() throws IOException {
    RandomAccessFile raf = new RandomAccessFile(fileName
        + POINTER_FILE_EXTENSION, "rw");
    ByteBuffer bb = ByteBuffer.allocate(20 + (numBlocks * 4));
    bb.putInt(numBlocks);
    bb.putInt(currentBlock);
    bb.putInt(pointers.length);
    bb.putInt(blockSize);
    bb.putInt(highBlock);
    for (int i = 0; i < numBlocks; i++) {
      bb.putInt(pointers[i]);
    }
    bb.flip();
    raf.getChannel().write(bb);
    raf.close();
  }

  private void createFile() throws IOException {
    file = new RandomAccessFile(fileName + BLOCK_FILE_EXTENSION, "rw");
    file.setLength(0);
    currentBlock = -1;
    numBlocks = 0;
    highBlock = -1;
  }

  private void openFile() throws IOException {
    // read the pointers:
    RandomAccessFile raf = new RandomAccessFile(fileName
        + POINTER_FILE_EXTENSION, "r");
    ByteBuffer bb = ByteBuffer.allocate((int) raf.length());
    raf.getChannel().read(bb);
    bb.flip();
    numBlocks = bb.getInt();
    currentBlock = bb.getInt();
    pointers = new int[bb.getInt()];
    blockSize = bb.getInt();
    highBlock = bb.getInt();
    for (int i = 0; i < numBlocks; i++)
      pointers[i] = bb.getInt();

    raf.close();
    // open block file:
    file = new RandomAccessFile(fileName + BLOCK_FILE_EXTENSION, "rw");
  }

  /** *******************************INNER CLASSES************************* */
  class BFIterator implements Iterator {
    int count = numBlocks;

    int cursor = 0;

    int lastRet = -1;

    public BFIterator(int blockNo, boolean logical) {
      cursor = (logical) ? blockNo : getLogicalBlockNo(blockNo);
      if (cursor < 0) // the physical blockNo did not exsist
        cursor = numBlocks + 10;
    }

    public BFIterator() {
      ;
    }

    public boolean hasNext() {
      return (cursor < numBlocks);
    }

    public Object next() {
      try {
        check();
        if (cursor >= numBlocks)
          throw new NoSuchElementException();

        Object block = readBlock(cursor);
        lastRet = cursor++;
        return block;
      }
      catch (IOException e) {
        throw new NoSuchElementException();
      }

    }

    public void remove() {
      if (lastRet == -1)
        throw new IllegalStateException();
      check();
      removeBlock(lastRet);
      if (lastRet < cursor)
        cursor--;
      lastRet = -1;
      count = numBlocks;
    }

    private void check() {
      if (count != numBlocks)
        throw new ConcurrentModificationException();
    }
  }

}
