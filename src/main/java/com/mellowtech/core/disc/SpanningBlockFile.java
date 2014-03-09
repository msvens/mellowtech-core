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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import com.mellowtech.core.bytestorable.ByteStorable;

/**
 * The Spanning file allows for insertion of variable length records of any size
 * tha can be grown/shrinked and deleted. Deleted space will be reused
 * immediatley.
 */
public class SpanningBlockFile {
  public static final String SPANNING_FILE_EXTENSION = ".spf";
  public static final String SPANNING_FILE_HEADER_EXTENSION = ".sph";

  int[] dBlocks;
  TreeMap<Integer, Mapping> mapping;
  int highLBlock;
  int highPBlock;
  int blockSize;
  int deletedBlocks = 0;
  String fileName;
  RandomAccessFile file;

  /**
   * Creating a new spanning file.
   * 
   * @param blockSize
   *          the minimum size that will be allocated when inserting.
   * @param fileName
   *          given file name (without extension)
   */
  public SpanningBlockFile(int blockSize, String fileName) throws IOException {
    this.blockSize = blockSize;
    this.fileName = fileName;
    file = new RandomAccessFile(fileName + SPANNING_FILE_EXTENSION, "rw");
    highPBlock = highLBlock = 0;
    mapping = new TreeMap<Integer, Mapping>();
    dBlocks = new int[256];
  }

  /**
   * Open an existing spanning file.
   * 
   * @param fileName
   *          previously given file name (without extension)
   */
  public SpanningBlockFile(String fileName) throws IOException {
    this.fileName = fileName;
    file = new RandomAccessFile(fileName + SPANNING_FILE_EXTENSION, "rw");
    openFile(fileName);
  }

  /**
   * Iterator over the logical blocks in this file
   */
  public Iterator iterator() {
    return new SBFIterator();
  }

  /**
   * Iterator over the logical blocks. The iterator returns objects of type
   * Map.CompResult (java.util) with keys beeing recored numbers as Integers and
   * values either byte arrays or Objects converted using the provided template.
   * 
   * @param template
   *          a byte array converter
   * @return Iterator with Map.CompResult objects
   */
  public Iterator entryIterator(ByteStorable template) {
    return new MappingIterator(template);
  }

  /**
   * Iterator over the logical blocks starting from the given record. The
   * iterator returns objects of type Map.CompResult (java.util) with keys beeing
   * recored numbers as Integers and values either byte arrays or Objects
   * converted using the provided template.
   * 
   * @param template
   *          a byte array converter
   * @return Iterator with Map.CompResult objects
   */
  public Iterator entryIterator(int start, ByteStorable template) {
    return new MappingIterator(new Integer(start), template);
  }

  /**
   * Open an existing spanning block file.
   * 
   * @param fileName
   *          the name of the file...without extension
   * @exception IOException
   *              if the an IO error occured
   */
  public void openFile(String fileName) throws IOException {
    String headerFileName = fileName + SPANNING_FILE_HEADER_EXTENSION;

    StorableFile.readFileAsByteStorable(headerFileName, new HeaderFile());

  }

  public void deleteFile() throws IOException{
    //delete header file:
    String FileName = fileName + SPANNING_FILE_HEADER_EXTENSION;
    File f = new File(fileName);
    f.delete();

    //delete record file:
    file.close();
    fileName = fileName + SPANNING_FILE_EXTENSION;
    f = new File(fileName);
    f.delete();

  }

  /**
   * Close an open file. Header and pointer data will be saved. Always call this
   * method when closing a spanning file
   * 
   * @exception IOException
   *              if the file could not be written to disc.
   */
  public void closeFile() throws IOException {
    String headerFileName = fileName + SPANNING_FILE_HEADER_EXTENSION;

    StorableFile.writeFileAsByteStorable(headerFileName, new HeaderFile());
    file.close();
  }

  /**
   * Flush an open file. Header and pointer data will be saved.
   * 
   * @exception IOException
   *              if the file could not be written to disc.
   */
  public void flushFile() throws IOException {
    String headerFileName = fileName + SPANNING_FILE_HEADER_EXTENSION;

    StorableFile.writeFileAsByteStorable(headerFileName, new HeaderFile());

    file.close();
    file = new RandomAccessFile(fileName + SPANNING_FILE_EXTENSION, "rw");
  }

  /**
   * Append additional data to a relative record.
   * 
   * @param rrn
   *          record number of the record to write to.
   * @param b
   *          the bytes that will be appended to the record
   * @exception IOException
   *              if the bytes could not be written to disc
   */
  public void append(int rrn, byte[] b) throws IOException {
    update(rrn, b, b.length);
  }

  public void append(int rrn, byte[] b, int length) throws IOException {
    update(rrn, b, getMapping(rrn).size, length);
  }

  public void update(int rrn, byte[] b, int offset) throws IOException {
    update(rrn, b, offset, b.length);
  }

  /**
   * Updateds an existing record by (over)writing bytes starting at the given
   * position and possibly expands/shrinks the record.
   * 
   * @param rrn
   *          the record to update.
   * @param b
   *          the bytes to update with. If it is 0 bytes the update method just
   *          returns.
   * @param offset
   *          the position in the record to start from.
   * @exception IOException
   *              if the physical writing to disc failed.
   */
  public void update(int rrn, byte[] b, int offset, int length)
      throws IOException {
    Mapping m = getMapping(rrn);
    if (m == null || offset > m.size || length == 0)
      return;
    // block to start from:
    int current = offset / blockSize;

    int fOffset = offset % blockSize;
    int bytesWritten = 0;
    while (bytesWritten < length) {
      if (current == m.ptrs.length)
        growPointers(m);
      if (current >= m.count) {
        m.ptrs[current] = getNextBlock();
      }
      bytesWritten += writeBytes(m.ptrs[current], b, bytesWritten, fOffset,
          length);
      fOffset = 0;
      current++;
    }
    m.size = offset + length;
    int i = getCount(m.size);
    if (i < m.count) { // now delete some records if possible
      for (; i < m.count; i++) {
        putDeletedBlock(m.ptrs[i]);
      }
    }
    m.count = getCount(m.size);
  }

  public int insert(byte[] b) throws IOException {
    return insert(b, b.length);
  }

  /**
   * Inserts a new record into this file.
   * 
   * @param b
   *          the bytes to write.
   * @return the newly created record number.
   * @exception IOException
   *              if the phsyical read/write operations failed
   */
  public int insert(byte[] b, int length) throws IOException {

    int rrn = highLBlock;
    int bytesWritten = 0;
    int pBlock;
    Mapping m = new Mapping();
    int i = 0;
    while (bytesWritten < length) {
      pBlock = getNextBlock();
      bytesWritten += writeBytes(pBlock, b, bytesWritten, length);
      if (i == m.ptrs.length)
        growPointers(m);
      m.ptrs[i] = pBlock;
      i++;
    }
    m.count = i;
    m.size = length;
    mapping.put(new Integer(highLBlock), m);
    highLBlock++;
    return highLBlock - 1;
  }

  /**
   * Returns the bytes for the given record.
   * 
   * @param rrn
   *          a record number
   * @return the bytes attatched to this record (exact)
   * @exception IOException
   *              if the data could not be read
   */
  public byte[] get(int rrn) throws IOException {
    Mapping m = getMapping(rrn);
    byte b[] = new byte[m.size];
    int offset = 0;
    for (int i = 0; i < m.count; i++) {
      offset += readBytes(m.ptrs[i], b, offset);
    }
    return b;
  }

  /**
   * Returns the bytes for the given record.
   * 
   * @param rrn
   *          a record number
   * @param b
   *          a byte array buffer
   * @return the number of bytes inserted in byte array, or a negative number
   *         that indicates two things: no bytes were read (the buffer was too
   *         small), and the returned value times -1 is the number of bytes that
   *         the buffer should be
   * @exception IOException
   *              if the data could not be read
   */
  public int get(int rrn, byte[] b) throws IOException {
    Mapping m = getMapping(rrn);
    if (b == null || b.length < m.size) {
      return -m.size;
    }
    int offset = 0;
    for (int i = 0; i < m.count; i++) {
      offset += readBytes(m.ptrs[i], b, offset);
    }
    return offset;
  }

  /**
   * Return number of blocks allocated for a given relative record number 'rrn'
   * 
   * @param rrn
   *          relative record number
   * @return number of blocks allocated for 'rrn'
   */
  public int getRecordBlockCount(int rrn) {
    Mapping m = getMapping(rrn);
    if (m == null)
      return 0;
    return m.count;
  }

  /**
   * Return number of data bytes written for a given relative record number
   * 'rrn', i.e the total number of bytes of data.
   * 
   * @param rrn
   *          relative record number
   * @return number of data bytes
   */
  public int getRecordSize(int rrn) {
    Mapping m = getMapping(rrn);
    if (m == null)
      return 0;
    return m.size;
  }

  /**
   * Deletes a record from this file and frees the previously blocks for reuse.
   * 
   * @param rrn
   *          the record to delete.
   */
  public void delete(int rrn) {
    Mapping m = (Mapping) mapping.remove(new Integer(rrn));
    if (m == null)
      return;
    for (int i = 0; i < m.count; i++) {
      putDeletedBlock(m.ptrs[i]);
    }
  }

  /**
   * Sets the length of a record. This can result in either that new blocks will
   * be added to the record or old blocks deleted and marked for reuse.
   * 
   * @param rrn
   *          a recored number
   * @param newLength
   *          the new length
   */
  public void setLength(int rrn, int newLength) {
    Mapping m = getMapping(rrn);
    int oldSize = m.size;
    m.size = newLength;
    int i = getCount(m.size);
    if (i < m.count) { // now delete some records if possible
      for (i = getCount(m.size); i < m.count; i++) {
        putDeletedBlock(m.ptrs[i]);
      }
      m.count = getCount(m.size);
    }
    else if (i > m.count) { // increase if nessecary:
      while (m.count < i) {
        if (m.ptrs.length == m.count)
          growPointers(m);
        m.ptrs[m.count] = getNextBlock();
        m.count++;
      }
    }
  }

  /**
   * Defragments this file.
   */
  public void defragment() {
  }

  /**
   * Prints the header information of this file.
   */
  public String toString() {
    StringBuffer sbuff = new StringBuffer();
    sbuff.append("\n\nFile name: " + fileName + "\n");
    sbuff.append("block size: " + blockSize + "\n");
    sbuff.append("deleted blocks: ");
    for (int i = 0; i < deletedBlocks; i++)
      sbuff.append(dBlocks[i] + " ");
    sbuff.append("\nMapping: ");
    Mapping m;
    Integer rrn;
    for (Iterator i = mapping.keySet().iterator(); i.hasNext();) {
      rrn = (Integer) i.next();
      m = (Mapping) mapping.get(rrn);
      sbuff.append("Logical Block: " + rrn + " size: " + m.size
          + " physical pointers: ");
      for (int j = 0; j < m.count; j++) {
        sbuff.append(m.ptrs[j] + " ");
      }
      sbuff.append("\n\n");
    }
    return sbuff.toString();
  }

  /** *************************PRIVATE SECTION***************** */
  private Mapping getMapping(int rrn) {
    Integer i = new Integer(rrn);
    return (Mapping) mapping.get(i);
  }

  private void growPointers(Mapping m) {
    int[] tmp = new int[m.ptrs.length * 2];
    System.arraycopy(m.ptrs, 0, tmp, 0, m.ptrs.length);
    m.ptrs = tmp;
  }

  private int getDeletedBlock() {
    if (deletedBlocks == 0)
      return -1;
    deletedBlocks--;
    int block = dBlocks[deletedBlocks];
    return block;
  }

  private int getCount(int size) {
    return (size % blockSize) == 0 ? size / blockSize : (size / blockSize) + 1;
  }

  private void putDeletedBlock(int block) {
    if (deletedBlocks == dBlocks.length) {
      int[] tmp = new int[dBlocks.length * 2];
      System.arraycopy(dBlocks, 0, tmp, 0, dBlocks.length);
      dBlocks = tmp;
    }
    dBlocks[deletedBlocks] = block;
    deletedBlocks++;
  }

  private int getNextBlock() {
    if (deletedBlocks == 0) {
      highPBlock++;
      return highPBlock - 1;
    }
    return getDeletedBlock();
  }

  private int writeBytes(int blockNo, byte[] b, int offset, int fOffset,
      int length) throws IOException {
    file.seek((blockNo * blockSize) + fOffset);
    int free = blockSize - fOffset;
    int toWrite = (offset + blockSize) <= length ? blockSize : length - offset;
    // calculate actual size:
    if (toWrite > free)
      toWrite = free;

    file.write(b, offset, toWrite);
    return toWrite;
  }

  private int writeBytes(int blockNo, byte[] b, int offset, int length)
      throws IOException {
    return writeBytes(blockNo, b, offset, 0, length);
  }

  private int readBytes(int blockNo, byte[] b, int offset) throws IOException {
    long fileLength = file.length();
    long pos = blockNo * blockSize;

    int toRead = (offset + blockSize) <= b.length ? blockSize : b.length
        - offset;
    // Since the last block may be non-full, only read
    // as much as left of the file if the block to read is
    // the last block
    if (pos + toRead > fileLength)
      toRead = (int) (fileLength - pos);

    synchronized (file) {
      file.seek(pos);
      file.readFully(b, offset, toRead);
    }
    return toRead;
  }

  /**
   * Encapsulation of storing and reading the header file for the BlockFile. In
   * the header all recno:blockno mappings are stored, together with an array of
   * deleted blocks and other state related data.
   */
  class HeaderFile extends ByteStorable {

    public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
      int bytesize = bb.getInt();
      blockSize = bb.getInt(); // block size
      highLBlock = bb.getInt(); // highest logical record number
      highPBlock = bb.getInt(); // highest pysical block

      // deletedBlocks:
      deletedBlocks = bb.getInt();
      dBlocks = (deletedBlocks > 256) ? new int[deletedBlocks] : new int[256];
      for (int i = 0; i < deletedBlocks; i++)
        dBlocks[i] = bb.getInt();

      // mapping:
      mapping = new TreeMap<Integer, Mapping>();
      int mSize = bb.getInt();
      Integer integer;
      Mapping m;
      for (int i = 0; i < mSize; i++) {
        m = new Mapping();
        integer = new Integer(bb.getInt());
        m.size = bb.getInt();
        m.count = bb.getInt();
        m.ptrs = new int[m.count];
        for (int j = 0; j < m.count; j++) {
          m.ptrs[j] = bb.getInt();
        }
        mapping.put(integer, m);
      }
      return this;
    }

    public void toBytes(ByteBuffer bb) {
      bb.putInt(byteSize()); // object size in bytes
      bb.putInt(blockSize); // block size
      bb.putInt(highLBlock); // highest logical record number
      bb.putInt(highPBlock); // highest pysical block

      // deleted blocks:
      bb.putInt(deletedBlocks);
      for (int i = 0; i < deletedBlocks; i++)
        bb.putInt(dBlocks[i]);

      // mapping:
      bb.putInt(mapping.size());
      Mapping m;
      Integer integer;
      for (Iterator it = mapping.keySet().iterator(); it.hasNext();) {
        integer = (Integer) it.next();
        m = (Mapping) mapping.get(integer);
        bb.putInt(integer.intValue());
        bb.putInt(m.size);
        bb.putInt(m.count);
        for (int i = 0; i < m.count; i++)
          bb.putInt(m.ptrs[i]);
      }
    }

    public int byteSize() {
      int size = 4 + 4 + 4; // block size, highest logical, highest physical
      size += 4; // deletedblocks
      size += deletedBlocks > 0 ? 4 * deletedBlocks : 0;
      size += 4; // mapping count
      Mapping m;
      Integer integer;
      for (Iterator it = mapping.keySet().iterator(); it.hasNext();) {
        integer = (Integer) it.next();
        m = (Mapping) mapping.get(integer);
        size += 4 * 3; // key, size, count
        size += m.count > 0 ? 4 * m.count : 0;
      }
      return 4 + size; // 4 for the byte size
    }

    public int byteSize(ByteBuffer bb) {
      int position = bb.position();
      int bytesize = bb.getInt();
      bb.position(position);
      return bytesize;
    }
  }

  /** *******************************INNER CLASSES************************* */
  class Mapping extends ByteStorable {
    int[] ptrs = new int[10];
    int count = 0;
    int size = 0;

    public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
      Mapping mp = doNew ? new Mapping() : this;
      mp.size = bb.getInt();
      mp.count = bb.getInt();
      for (int i = 0; i < mp.count; i++) {
        if (i == mp.ptrs.length)
          growPointers(mp);
        mp.ptrs[i] = bb.getInt();
      }
      return mp;
    }

    public void toBytes(ByteBuffer bb) {
      bb.putInt(size);
      bb.putInt(count);
      for (int i = 0; i < count; i++)
        bb.putInt(ptrs[i]);
    }

    public int byteSize() {
      return 4 + 4 + (count * 4);
    }

    public int byteSize(ByteBuffer bb) {
      int position = bb.position();
      int aSize = bb.getInt();
      int count = bb.getInt();
      bb.position(position);
      return 4 + 4 + (count * 4);
    }
  }

  class SBFIterator implements Iterator {
    Iterator mapIterator = mapping.values().iterator();

    public boolean hasNext() {
      return mapIterator.hasNext();
    }

    public Object next() {
      try {
        Mapping m = (Mapping) mapIterator.next();
        byte b[] = new byte[m.size];
        int offset = 0;
        for (int i = 0; i < m.count; i++) {
          offset += readBytes(m.ptrs[i], b, offset);
        }
        return b;
      }
      catch (IOException e) {
        throw new NoSuchElementException();
      }

    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  class MappingIterator implements Iterator {

    Iterator iterator;
    MapEntry me;
    Map.Entry realEntry;
    ByteStorable mTemplate;

    public MappingIterator(Integer startFrom, ByteStorable template) {
      iterator = mapping.tailMap(startFrom).entrySet().iterator();
      me = new MapEntry();
      mTemplate = template;
    }

    public MappingIterator(ByteStorable template) {
      iterator = mapping.entrySet().iterator();
      me = new MapEntry();
      mTemplate = template;
    }

    public boolean hasNext() {
      return iterator.hasNext();
    }

    public Object next() {
      try {
        realEntry = (Map.Entry) iterator.next();
        Mapping m = (Mapping) realEntry.getValue();
        byte b[] = new byte[m.size];
        int offset = 0;
        for (int i = 0; i < m.count; i++) {
          offset += readBytes(m.ptrs[i], b, offset);
        }
        me.k = realEntry.getKey();
        if (mTemplate == null)
          me.v = b;
        else
          me.v = mTemplate.fromBytes(b, 0);
        return me;
      }
      catch (IOException e) {
        throw new NoSuchElementException();
      }

    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

}
