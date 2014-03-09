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
package com.mellowtech.core.collections.tree;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.bytestorable.CBString;
import com.mellowtech.core.disc.IntArrayFile;
import com.mellowtech.core.collections.KeyValue;
import com.mellowtech.core.disc.BlockFile;
import com.mellowtech.core.disc.SortedBlock;



/**
 * <code>AbstractBPlusTree</code> is an abstract representation of a minimal
 * BPlusTree. It implements most of the functions in a BPlusTree.<br>
 * <p>
 * The BPlus tree structure is suitable for storing large indices on disc. The
 * minimal BPlusTree is divided into two parts (index and key file) with the
 * index and key file divided into blocks. The index contains minimal separator
 * between key blocks and the key file is sorted on key for fast sequential
 * access. Thus, the index only keeps minimal separators between keys, yielding
 * a much more compact index. Depending on the block size the index is more or
 * less deep, however a typical BPlusTree should not reach a depth greater than
 * 3. A resonable blocksize is somewhere between 512-2048 bytes.
 * </p>
 * <p>
 * An BPlusTree that can store 512 separators in each block will at worst (i.e
 * maximum height and minimal breath) have 3 levels for an index with 1000000
 * keys. Thus, at maximum it will only take three disc access for searching a
 * BPlusTree containing 10000000 keys and one additional disc access for
 * retrieving the key. With use of buffers it is often possible to keep portions
 * of the index in memory, yielding much better performance. An LRU cache of
 * size 20 allows for on average less than 1 disc access per search.
 * </p>
 * <p>
 * In general, the maximum depth of a BPlusTree is
 * </p>
 * <p>
 * d <= 1 + log[m/2]((N + 1)/2)
 * </p>
 * <p>
 * where m is the order of the tree and N is the number of keys in the three.
 * Blocks in this BPlusTree is searched binary so that will take:
 * </p>
 * <p>
 * log[2](m/2)
 * </p>
 * <p>
 * Implementing classes only has to implement three functions searchBlock,
 * insert, and delete (see below). Implementing classes should use the provided
 * functions and fields in this class when implementing the above three
 * functions. The main reason for separating searching, insertion, and deletion
 * from the rest of the functionality is because they can be implemented with
 * various schemes. Especially splitting and merging nodes in the tree can be
 * elaborated with. For a general discussion on BTrees the interested reader
 * should turn to <i>File Structures</i> by Michael J. Folk and Bill Zoellick.
 * Most implementation ideas were taken from that book
 * </p>
 * <p>
 * This BPlusTree uses the scheme in com.mellowtech.Disc for disc access.
 * Notably it implements the ByteStorable interface.
 * </p>
 * 
 * 
 * @author Martin Svensson
 * @version 1.0
 * @see AbstractBPlusTree#searchBlock
 * @see AbstractBPlusTree#delete
 * @see AbstractBPlusTree#insert
 */
@Deprecated
public class AbstractBPlusTree extends ByteStorable <AbstractBPlusTree> {
  /**
   * Block number for the root of the index.
   */
  protected int rootPage;
  /**
   * The depth of the tree (including the leaf level).
   */
  protected int leafLevel;
  /**
   * File to store index.
   */
  protected BlockFile indexFile;
  /**
   * File to store key/values.
   */
  public BlockFile valueFile;
  /**
   * Used for reading and writing key/value pairs to the key file.
   */
  protected KeyValue<ByteStorable, ByteStorable> keyValues;
  /**
   * Used for reading and writing keys to the index.
   */
  protected BTreeKey indexKeys;
  /**
   * Filename for the IndexFile.
   */
  protected String indexName;
  /**
   * Filename for the key/value file.
   */
  protected String valueName;
  /**
   * The name of this BPlusTree.
   */
  protected String fName;
  /**
   * File to store block count information
   */
  protected IntArrayFile blockCountFile;
  /**
   * Name of block count file
   */
  protected String blockCountFileName;
  private static final boolean FORCE_INTEGRITY = false;


  //For Caching
  boolean useCache = false;
  boolean fullIndex = true;
  boolean readOnly = true;

  /** ***************CONSTRUCTORS************************** */
  /**
   * Empty constructor, does nothing. Used if the read/write methods of bytale
   * should be used to bootstrap the BTree.
   * 
   */
  public AbstractBPlusTree() {
    ;
  }

  /**
   * Opens an existing <code>AbstractBPlusTree</code>.
   * 
   * @param fName
   *          Name of the BPlusTree to open.
   * @param useOldPaths
   *          true if old paths to index and key file should be used.
   * @exception Exception
   *              if an error occurs
   */
  public AbstractBPlusTree(String fName, boolean useOldPaths) throws Exception {
    // open the file:
    RandomAccessFile file = new RandomAccessFile(fName + ".btree", "r");
    byte b[] = new byte[(int) file.length()];
    file.readFully(b);
    this.fName = fName;
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.clear();
    file.close();
    initBPlusTree(bb, useOldPaths);
    return;
  }

  /**
   * Opens an existing <code>AbstractBPlusTree</code>. Effectily calls
   * <code>this(fName, true);</code>
   * 
   * @param fName
   *          a <code>String</code> value
   * @exception Exception
   *              if an error occurs
   * @see AbstractBPlusTree#AbstractBPlusTree(String, boolean)
   */
  public AbstractBPlusTree(String fName) throws Exception {
    this(fName, true);
  }

  /**
   * Creates a new <code>AbstractBPlusTree</code>.
   * 
   * @param fName
   *          the name of the BPlusTree
   * @param keyType
   *          The type of keys stored in this tree
   * @param valueType
   *          The type of values stored in this tree
   * @param valueBlockSize
   *          Block size in key/value file. The size has to be at least twize
   *          that of the maximum size of a key+value
   * @param indexBlockSize
   *          Size of index blocks. The larger the size the more separators can
   *          fit in each block. The size has to be at least twice the size that
   *          of the maximum size of a key
   * @exception IOException
   *              if an error occurs
   */
  public AbstractBPlusTree(String fName, ByteStorable keyType,
      ByteStorable valueType, int valueBlockSize, int indexBlockSize)
      throws IOException {
    indexName = fName + ".Index";
    valueName = fName + ".Values";
    keyValues = new KeyValue(keyType, valueType);
    indexKeys = new BTreeKey(keyType, 0);
    leafLevel = -1;
    openIndex(true, indexBlockSize);
    openValues(true, valueBlockSize);
    blockCountFileName = fName + ".bc";
    openBlockCountFile(true);
    // create the initial block in the value file...cannot be deleted:
    valueFile.insertBlock(-1);
    blockCountFile.put(0, 0);
    SortedBlock sb = new SortedBlock();
    sb.setBlock(new byte[valueFile.getBlockSize()], keyValues, true,
        SortedBlock.PTR_NORMAL);
    writePVBlock(0, sb);
    rootPage = valueFile.getPhysicalBlockNo(0);
    this.fName = fName;
  }

  /**
   * Delete this btree on disc
   * @throws IOException
   */
  public void deleteTree() throws IOException {
    valueFile.deleteFile();
    indexFile.deleteFile();
    File f = new File(fName + ".btree");
    f.delete();
  }

  /**
   * Save and close the tree. After a call to this method the tree has to be
   * reopened. Either saveTree or save has to be called before "closing" the
   * tree.
   * 
   * @exception IOException
   *              if an error occurs
   * @see #save
   */
  public void saveTree() throws IOException {
    RandomAccessFile raf = new RandomAccessFile(fName + ".btree", "rw");
    raf.setLength(0);
    byte[] b = new byte[byteSize()];
    ByteBuffer bb = ByteBuffer.wrap(b);
    toBytes(bb);
    raf.write(b);
    raf.close();
    valueFile.closeFile();
    indexFile.closeFile();
    blockCountFile.close();
  }


  public void useCache(boolean fullIndex, boolean readOnly){
    this.useCache = true;
    this.fullIndex = fullIndex;
    this.readOnly = readOnly;
    if(fullIndex)
      this.indexFile.setCache(readOnly, Integer.MAX_VALUE, false);
    else
      this.indexFile.setCache(readOnly, 1024*1024, true);
    //this.valueFile.setCache(readOnly, 1024*1024*10, true);
  }

  /**
   * Save the tree without closing it.
   * 
   * @exception IOException
   *              if an error occurs
   */
  public void save() throws IOException {
    RandomAccessFile raf = new RandomAccessFile(fName + ".btree", "rw");
    raf.setLength(0);
    byte[] b = new byte[byteSize()];
    ByteBuffer bb = ByteBuffer.wrap(b);
    toBytes(bb);
    raf.write(b);
    valueFile.closeFile();
    indexFile.closeFile();
    blockCountFile.close();
    // reopen valuFile and indexFile;
    openBlockCountFile(false);
    openIndex(false, -1);
    openValues(false, -1);
    if(useCache)
      useCache(fullIndex, readOnly);
  }

  public void compact() throws IOException {
    valueFile.defragment();
    indexFile.defragment();
  }

  /**
   * Set the cache for this btree (both for index and key file). By default it
   * uses an url cache. Note that caching is under development and for now this
   * method does nothing.
   * 
   * @param size
   *          max blocks in cache
   */
  public void setCache(int size) {
    // CacheLRU tmp1 = new CacheLRU(null, size);
    // CacheLRU tmp2 = new CacheLRU(null, size);
    // indexFile.setCache(tmp1);
    // valueFile.setCache(tmp1);
  }

  /**
   * Drops the index and key file cache. Caching is under development and this
   * method is effectivly ignored.
   */
  public void dropCache() {
    // indexFile.setCache(null);
    // valueFile.setCache(null);
  }

  // PRINT METHODS:
  /**
   * Prints this index whitout the leaf level, i.e it will not print the
   * contents of the key file.
   * 
   * @return a <code>String</code> value
   */
  public String printIndex(boolean leafLevel) {
    StringBuffer sbuff = new StringBuffer();
    buildOutputTree(rootPage, sbuff, 0, leafLevel);
    return sbuff.toString();
  }

  public List<Integer> getPointers(){
    ArrayList <Integer> list = new ArrayList<>();
    buildPointers(rootPage, list, 0);
    return list;
  }

  public String toString() {
    StringBuffer sbuff = new StringBuffer();
    // first print the index:
    buildOutputTree(rootPage, sbuff, 0, true);
    // then print the keys:
    sbuff.append("\n*****************VALUE FILE***********************\n\n");
    SortedBlock sb;
    for (int i = 0; i < valueFile.getNumberOfBlocks(); i++) {
      sb = getVBlock(i);
      sbuff.append("\n\n");
      sbuff.append("logical block: " + i);
      sbuff.append("\nphysical block: " + valueFile.getPhysicalBlockNo(i));
      sbuff.append("\n" + sb + "\n");
    }
    return sbuff.toString();
  }

  // ITERATORS:
  /**
   * Returns an iterator over the key/value pairs in this btree. The next method
   * returns objects of type KeyValue.
   * 
   * @return this tree's iterator
   * @see KeyValue
   */
  public Iterator <KeyValue <ByteStorable, ByteStorable>> iterator() {
    return new ABPIterator();
  }

  /**
   * Returns an iterator over the key/value pairs in this btree starting at a
   * given position.
   * 
   * @param key
   *          where to start iterating. If the key does not exist start at
   *          position just greater than key
   * @return this tree's iterator
   * @see KeyValue
   */
  public Iterator <KeyValue <ByteStorable, ByteStorable>> iterator(ByteStorable key) {
    return new ABPIterator(key);
  }


  // ABSTRACT METHODS:
  /**
   * Implementing classes has to implement this method. It should return the
   * physical blocknumber in the valuefile where the key/value pair should be
   * stored if it exists.
   * 
   * @param key
   *          the key to search.
   * @return a blocknumber
   */
  protected int searchBlock(ByteStorable key) {
    return -1;
  }

  /**
   * Implementing classes has to implement this function. The AbstractBPlusTree
   * provides utility functions for actual insertion in the tree. To always
   * update a key/value pair set update to true.
   * 
   * @param key
   *          the key to be inserted
   * @param value
   *          the value to be inserted
   * @param update
   *          if false only update the tree if the key did not exist.
   * @exception IOException
   *              if an error occurs
   */
  public void insert(ByteStorable key, ByteStorable value, boolean update)
      throws IOException {
    ;
  }

  // SEARCHING:
  /**
   * Returns the key/value for a given key or null if the key was not found.
   * 
   * @param key
   *          key to search for
   * @return value/key pair mapped to key.
   * @exception IOException
   *              if an error occurs
   */
  public KeyValue searchKeyValue(ByteStorable key) throws IOException {
    int block = searchBlock(key);
    if (block == -1)
      return null;
    return searchValueFile(key, block);
  }

  /**
   * Returns the value for a given key. Returns null either if the key was not
   * found or the value is null. Use containsKey if you want to find out if the
   * key is stored int the hmap.
   * 
   * @param key
   *          key to search for.
   * @return value mapped to key.
   * @exception IOException
   *              if an error occurs
   * @see AbstractBPlusTree#containsKey
   */
  public ByteStorable search(ByteStorable key) throws IOException {
    KeyValue kv = searchKeyValue(key);
    return (kv == null) ? null : kv.getValue();
  }

  /**
   * Returns the tree position for a given key. TreePosition contains the the
   * number of smaller and greater key/value pairs in the block and in the tree
   * as a whole.
   * 
   * @param key
   *          key to search for
   * @return null if the key was not found
   * @exception IOException
   *              if an error occurs
   */
  public TreePosition getPosition(ByteStorable key) throws IOException {
    int block = searchBlock(key);
    if (block == -1)
      return null;
    return searchValueFilePosition(key, block);
  }
  
  /**
   * Returns the tree position for a given key or the position in which the
   * key would have been. TreePosition contains the the
   * number of smaller and greater key/value pairs in the block and in the tree
   * as a whole.
   * 
   * @param key
   *          key to search for
   * @return null if the key was not found
   * @exception IOException
   *              if an error occurs
   */
  public TreePosition getPositionWithMissing(ByteStorable key) throws IOException {
    int block = searchBlock(key);
    if (block == -1)
      return null;
    return this.searchValueFilePositionNoStrict(key, block);
  }

  public ByteStorable getKeyAtPosition(int pos) throws IOException {
    int blockno = 0;
    int numKeys = 0;
    while (blockno < blockCountFile.numRecords()) {
      numKeys = blockCountFile.get(blockno);
      if (pos < numKeys) { // we have found our block!
        SortedBlock sb = this.getVBlock(blockno);
        return ((KeyValue) sb.getKey(pos)).getKey();
      }
      blockno++;
      pos -= numKeys;
    }
    return null;
  }

  /**
   * @return
   */
  public int getNumberOfElements() {
    try{
      return blockCountFile.count();
    }
    catch(IOException e){
      CoreLog.L().log(Level.SEVERE, "could not return number of elements", e);
      return -1;
    }
  }

  public void printValueFileCount(PrintWriter pw) {
    try {
      for (int blockno = 0; blockno < valueFile.getNumberOfBlocks(); blockno++) {
        SortedBlock sb = this.getVBlock(blockno);
        pw.println(blockno + "\t" + sb.getNumberOfElements());
      }
    }
    catch (Exception e) {
    }
  }

  public void printBlockCountFile(PrintWriter pw) {
    try {
      for (int blockno = 0; blockno < blockCountFile.numRecords(); blockno++) {
        pw.println(blockno + "\t" + blockCountFile.get(blockno));
      }
    }
    catch (Exception e) {
    }
  }

  /**
   * Check if a key is stored in this tree.
   * 
   * @param key
   *          key to search for
   * @return true if the key exists.
   * @exception IOException
   *              if an error occurs
   */
  public boolean containsKey(ByteStorable key) throws IOException {
    KeyValue kv = searchKeyValue(key);
    return (kv == null) ? false : true;
  }

  /**
   * Deletes a key from this tree. Implementing classes implement this method.
   * 
   * @param key
   *          the key to delete
   * @return the value corresponing to the key
   */
  public ByteStorable delete(ByteStorable key) {
    return null;
  }

  // CREATING INDEX FROM SORTED DATA:
  /**
   * Create an index from a sorted array of key/value pairs. This method is much
   * faster than doing individual insertations. If you you have a large number
   * of keys from which you want to build an index, the best way is to
   * externally sort them and then calling createIndex.
   * 
   * @param keysAndValues
   *          a sorted array of key/value pairs.
   * @exception IOException
   *              if an error occurs
   */
  public void createIndex(KeyValue[] keysAndValues) throws IOException {
    SortedBlock sb = new SortedBlock();
    byte b[] = new byte[valueFile.getBlockSize()];
    sb.setBlock(b, keyValues, true, SortedBlock.PTR_NORMAL);
    KeyValue tmpKV = new KeyValue();
    SBBNo[] levels = new SBBNo[20];
    // valueFile.insertBlock(-1);
    int bNo = 0;
    for (int i = 0; i < keysAndValues.length; i++) {
      tmpKV = keysAndValues[i];
      if (!sb.fitsKey(tmpKV)) {
        writeVBlock(bNo, sb);
        valueFile.insertBlock(bNo);
        blockCountFile.put(bNo + 1, 0);
        bNo++;
        BTreeKey sep = generateSeparator(sb, tmpKV);
        sep.leftNode = bNo - 1;
        insertSeparator(sep, levels, 0, bNo);
        sb.setBlock(b, keyValues, true, SortedBlock.PTR_NORMAL);
      }
      sb.insertKeyUnsorted(tmpKV);
    }
    writeVBlock(bNo, sb);
    if (levels[0] != null) // we have to write the index levels
      writeIndexBlocks(levels);
  }

  /**
   * Create an index from an iterator of key/value pairs. This method is much
   * faster than doing individual insertations. If you you have a large number
   * of keys from which you want to build an index, the best way is to
   * externally sort them and then calling createIndex.
   * 
   * @param iterator
   *          a sorted stream of key/value pairs.
   * @exception IOException
   *              if an error occurs
   */
  public void createIndex(Iterator<KeyValue> iterator) throws IOException {
    SortedBlock sb = new SortedBlock();
    byte b[] = new byte[valueFile.getBlockSize()];
    sb.setBlock(b, keyValues, true, SortedBlock.PTR_NORMAL);
    KeyValue tmpKV = new KeyValue();
    SBBNo[] levels = new SBBNo[20];
    // valueFile.insertBlock(-1);
    int bNo = 0;
    while (iterator.hasNext()) {
      tmpKV = iterator.next();
      if (!sb.fitsKey(tmpKV)) {
        writeVBlock(bNo, sb);
        valueFile.insertBlock(bNo);
        blockCountFile.put(bNo + 1, 0);
        bNo++;
        BTreeKey sep = generateSeparator(sb, tmpKV);
        sep.leftNode = bNo - 1;
        insertSeparator(sep, levels, 0, bNo);
        sb.setBlock(b, keyValues, true, SortedBlock.PTR_NORMAL);
      }
      sb.insertKeyUnsorted(tmpKV);
    }
    writeVBlock(bNo, sb);
    if (levels[0] != null) // we have to write the index levels
      writeIndexBlocks(levels);
  }

  /**
   * CreateIndex calls this method to write all created index blocks to file.
   */
  private void writeIndexBlocks(SBBNo[] levels) throws IOException {
    boolean removeLast = false;
    int rPage = 0;
    int i = 0;
    for (; i < levels.length; i++) {
      if (levels[i] == null)
        break;
      rPage = levels[i].bNo;
      writeBlock(levels[i].bNo, levels[i].sb);
    }
    leafLevel = i - 1;
    rootPage = rPage;
  }

  /**
   * Insert a separator into the index when creating index from sorted data. If
   * the current block can not hold the data the block will be split into two
   * and the levels possibly increased.
   */
  private void insertSeparator(BTreeKey sep, SBBNo[] levels, int current,
      int rightNode) throws IOException {
    SortedBlock sb = null;
    if (levels[current] == null) { // we have to create a new level
      levels[current] = new SBBNo();
      levels[current].sb = new SortedBlock();
      levels[current].sb.setBlock(new byte[indexFile.getBlockSize()],
          indexKeys, true, SortedBlock.PTR_NORMAL, (short) 4);
      indexFile.insertBlock(indexFile.getLastBlockNo());
      levels[current].bNo = indexFile.getPhysicalBlockNo(indexFile
          .getLastBlockNo());
    }
    sb = levels[current].sb;
    if (!sb.fitsKey(sep)) { // save and promote the last key up...
      BTreeKey promo = (BTreeKey) sb.deleteKey(sb.getNumberOfElements() - 1);
      setLastPointer(sb, promo.leftNode);
      promo.leftNode = levels[current].bNo;
      writeBlock(levels[current].bNo, sb);
      // create the new block:
      sb.setBlock(new byte[indexFile.getBlockSize()], indexKeys, true,
          SortedBlock.PTR_NORMAL, (short) 4);
      levels[current].sb = sb;
      indexFile.insertBlock(indexFile.getLastBlockNo());
      levels[current].bNo = indexFile.getPhysicalBlockNo(indexFile
          .getLastBlockNo());
      // promote the last key in the previous block:
      insertSeparator(promo, levels, current + 1, levels[current].bNo);
    }
    // finally insert the separator:
    setLastPointer(sb, rightNode);
    sb.insertKeyUnsorted(sep);
  }

  // IMPLEMENTED FROM BYTESTORABLE:
  public int byteSize() {
    int size = 0;
    CBString tmp;
    // key class size:
    tmp = new CBString(keyValues.getKey().getClass().getName());
    size += tmp.byteSize();
    // value class size:
    tmp = new CBString(keyValues.getValue().getClass().getName());
    size += tmp.byteSize();
    // index name size:
    tmp = new CBString(indexName);
    size += tmp.byteSize();
    // index/value file name:
    tmp = new CBString(valueName);
    size += tmp.byteSize();
    // block count file name:
    tmp = new CBString(blockCountFileName);
    size += tmp.byteSize();
    size += 8; // size of rootPage and leafPage
    size += 4; // the size indicator:
    return size;
  }

  /*
   * public int byteSize(byte[] b, int offset){ return ByteStorage.readInt(b,
   * offset); }
   */
  public int byteSize(ByteBuffer bb) {
    int size = bb.getInt();
    bb.position(bb.position() - 4);
    return size;
  }

  public void toBytes(ByteBuffer bb) {
    CBString tmp;
    bb.putInt(byteSize());
    // key class:
    tmp = new CBString(keyValues.getKey().getClass().getName());
    tmp.toBytes(bb);
    // value class:
    tmp = new CBString(keyValues.getValue().getClass().getName());
    tmp.toBytes(bb);
    // index file name:
    tmp = new CBString(indexName);
    tmp.toBytes(bb);
    // index/value file name:
    tmp = new CBString(valueName);
    tmp.toBytes(bb);
    // block count file name:
    tmp = new CBString(blockCountFileName);
    tmp.toBytes(bb);
    // root page:
    bb.putInt(rootPage);
    // leaf level:
    bb.putInt(leafLevel);
  }

  public ByteStorable fromBytes(ByteBuffer bb) {
    AbstractBPlusTree tmpTree = new AbstractBPlusTree();
    try {
      tmpTree.initBPlusTree(bb, true);
    }
    catch (Exception e) {
      CoreLog.L().log(Level.WARNING, "could not init tree", e);
    }
    return tmpTree;
  }

  public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
    return fromBytes(bb);
  }

  // PROTECTED METHODS:
  // insertion/deletion/search in value file:
  /**
   * Given a key and blockNumber retrieves the key/value pair for the given key.
   * 
   * @param key
   *          the key to search for
   * @param bNo
   *          the physical blocknumber in the key/value file.
   * @return the Key/value pair or null if the key did not exist
   * @exception IOException
   *              if an error occurs
   */
  protected KeyValue searchValueFile(ByteStorable key, int bNo)
      throws IOException {
    if (valueFile.getNumberOfBlocks() == 0)
      return null;
    SortedBlock sb = getPVBlock(bNo);
    return (KeyValue) sb.getKey(new KeyValue(key, null));
  }

  /**
   * Given a key and blockNumber retrieves the key/value pair for the given key.
   * Stores the position of the key/value pair in parameter 'pos'. Its position
   * is the number of smaller and greater key/value pairs in the block and in
   * the tree as a whole
   * 
   * @param key
   *          the key to search for
   * @param bNo
   *          the physical blocknumber in the key/value file.
   * @return the Key/value pair or null if the key did not exist
   * @exception IOException
   *              if an error occurs
   */
  protected TreePosition searchValueFilePosition(ByteStorable key, int bNo)
      throws IOException {
    if (valueFile.getNumberOfBlocks() == 0)
      return null;
    SortedBlock sb = getPVBlock(bNo);
    int logicalBlock = valueFile.getLogicalBlockNo(bNo);
    int smallerInBlock = sb.binarySearch(new KeyValue(key, null));
    if (smallerInBlock < 0)
      return null;
    int elementsInBlock = sb.getNumberOfElements();
    int smaller = blockCountFile.count(logicalBlock) + smallerInBlock;
    int elements = blockCountFile.count();
    return new TreePosition(smaller, elements, smallerInBlock, elementsInBlock);
  }
  
  /**
   * Given a key and blockNumber retrieves the key/value pair for the given key.
   * Stores the position of the key/value pair in parameter 'pos'. Its position
   * is the number of smaller and greater key/value pairs in the block and in
   * the tree as a whole
   * 
   * @param key
   *          the key to search for
   * @param bNo
   *          the physical blocknumber in the key/value file.
   * @return the Key/value pair or null if the key did not exist
   * @exception IOException
   *              if an error occurs
   */
  protected TreePosition searchValueFilePositionNoStrict(ByteStorable key, int bNo)
      throws IOException {
    if (valueFile.getNumberOfBlocks() == 0)
      return null;
    SortedBlock sb = getPVBlock(bNo);
    int logicalBlock = valueFile.getLogicalBlockNo(bNo);
    int smallerInBlock = sb.binarySearch(new KeyValue(key, null));
    boolean exists = true;
    if (smallerInBlock < 0){ //not found 
      exists = false;
      smallerInBlock = Math.abs(smallerInBlock);
      smallerInBlock--; //readjust
    }
    int elementsInBlock = sb.getNumberOfElements();
    int smaller = blockCountFile.count(logicalBlock) + smallerInBlock;
    int elements = blockCountFile.count();
    return new TreePosition(smaller, elements, smallerInBlock, elementsInBlock);
  }

  /**
   * Inserts a key/value pair into the key/value file. A write will return the
   * following: <code>BPlusReturn(BPlusReturn.SPLIT,
   the key/value,
   a separator between the old and new block,
   the new physical block number);</code>
   * 
   * @param keyValue
   *          the key/value to insert
   * @param bNo
   *          the physical block to inset the key/value
   * @param update
   *          if true only insert if the key did not previously exist
   * @return if the insertation forced a split a BPlusReturn object will be
   *         returned.
   * @exception IOException
   *              if an error occurs
   */
  protected BPlusReturn insertKeyValue(KeyValue keyValue, int bNo,
      boolean update) throws IOException {
    SortedBlock sb = null;
    SortedBlock sb1 = null;
    try {
      sb = getPVBlock(bNo);
      if (sb.containsKey(keyValue)) {
        if (!update)
          return null;
        else
          sb.deleteKey(keyValue);
      }
      if (sb.fitsKey(keyValue)) {
        sb.insertKey(keyValue);
        writePVBlock(bNo, sb);
        return null;
      }
      // get the logical block and insert new block after it:
      int logicalNo = valueFile.getLogicalBlockNo(bNo);
      valueFile.insertBlock(logicalNo);
      blockCountFile.put(logicalNo + 1, 0);
      sb1 = new SortedBlock();
      sb1 = sb.splitBlock();
      if (keyValue.compareTo(sb.getLastKey()) <= 0)
        sb.insertKey(keyValue);
      else
        sb1.insertKey(keyValue);
      writeVBlock(logicalNo, sb);
      writeVBlock(logicalNo + 1, sb1);
      return new BPlusReturn(BPlusReturn.SPLIT, keyValue, generateSeparator(sb,
          sb1), valueFile.getPhysicalBlockNo(logicalNo + 1));
    }
    catch (Exception e) {
      throw new IOException(e.toString(), e);
    }
  }

  /**
   * Deletes a key/value pair in the key/value file. Deletion can result in one
   * of three things: 1) just delete the key/value, 2) deletion caused an
   * underflow try to redistribute between the left and right block 3)
   * underflow, try to merge block with either the left or right key.<br>
   * Case 1 will return:
   * <code>BPlusReturn(BPlusReturn.NONE, deletedKey, null,-1);</code> Case 2
   * will return: <code>BPlusReturn(BPlusReturn.REDISTRIBUTE, deletedKey, 
   * a new separator, either left or right node);</code>
   * Case 3 will return: <code>BPlusReturn(BPlusReturn.MERGE, deletedKey, null, 
   *  either left or right node);</code>
   * 
   * @param key
   *          the key to delete
   * @param bNo
   *          the physical block number where the key is stored.
   * @param leftNo
   *          the block just left to the current block (i.e just smaller).
   * @param rightNo
   *          the block just right to the current block (i.e just larger.
   * @return depending on what action this delete triggered returns either of
   *         three things (see above).
   * @exception IOException
   *              if an error occurs
   */
  protected BPlusReturn deleteKeyValue(KeyValue key, int bNo, int leftNo,
      int rightNo) throws IOException {
    SortedBlock sb = getPVBlock(bNo);
    KeyValue deletedKey = (KeyValue) sb.deleteKey(key);
    if (deletedKey == null) {
      return null;
    }
    if (checkMinimum(sb)) {
      writePVBlock(bNo, sb);
      return new BPlusReturn(BPlusReturn.NONE, deletedKey, null, -1);
    }
    // reblance...first redistribute:
    SortedBlock sib;
    if (leftNo != -1) {
      sib = getPVBlock(leftNo);
      if (checkMinimum(sib)) {
        redistribute(sib, sb, leftNo, bNo);
        return new BPlusReturn(BPlusReturn.REDISTRIBUTE, deletedKey,
            generateSeparator(sib, sb), leftNo);
      }
    }
    if (rightNo != -1) {
      sib = getPVBlock(rightNo);
      if (checkMinimum(sib)) {
        redistribute(sb, sib, bNo, rightNo);
        return new BPlusReturn(BPlusReturn.REDISTRIBUTE, deletedKey,
            generateSeparator(sb, sib), rightNo);
      }
    }
    // rebalance (merge):
    // try left:
    if (leftNo != -1) {
      sib = getPVBlock(leftNo);
      if (sb.canMerge(sib)) {
        sb.mergeBlock(sib);
        blockCountFile.delete(valueFile.getLogicalBlockNo(leftNo));
        valueFile.removeBlock(valueFile.getLogicalBlockNo(leftNo));
        writePVBlock(bNo, sb);
        return new BPlusReturn(BPlusReturn.MERGE, deletedKey, null, leftNo);
      }
    }
    if (rightNo != -1) {
      sib = getPVBlock(rightNo);
      if (sib.canMerge(sb)) {
        sib.mergeBlock(sb);
        blockCountFile.delete(valueFile.getLogicalBlockNo(bNo));
        valueFile.removeBlock(valueFile.getLogicalBlockNo(bNo));
        writePVBlock(rightNo, sib);
        return new BPlusReturn(BPlusReturn.MERGE, deletedKey, null, rightNo);
      }
    }
    writePVBlock(bNo, sb);
    return new BPlusReturn(BPlusReturn.NONE, deletedKey, null, -1);
  }

  /**
   * Generate a new separator between two blocks, i.e the smallest key that
   * would separate a a block with smaller keys and a block with larger keys. If
   * the BPlusTree does not contain a separator the smallest key in the larger
   * block will be returned.
   * 
   * @param small
   *          a block with smaller keys
   * @param large
   *          a block with larger keys
   * @return a separator
   */
  protected BTreeKey generateSeparator(SortedBlock small, SortedBlock large) {
    BTreeKey nKey = new BTreeKey();
    nKey.key = keyValues.getKey().separate(
        ((KeyValue) small.getLastKey()).getKey(),
        ((KeyValue) large.getFirstKey()).getKey());
    return nKey;
  }

  /**
   * Generates a separator between a block of smaller keys and one larger key.
   * 
   * @param small
   *          block with smaller keys.
   * @param large
   *          the larger value to compare with
   * @return a separator.
   */
  protected BTreeKey generateSeparator(SortedBlock small, KeyValue large) {
    // this should change to use the provided separator function.
    // for now take the small one:
    BTreeKey nKey = new BTreeKey();
    nKey.key = keyValues.getKey().separate(
        ((KeyValue) small.getLastKey()).getKey(), large.getKey());
    return nKey;
  }

  // ROOT HANDELING:
  /**
   * Remove a key from the root, possibly altering the root pointer.
   * 
   * @param ret
   *          contians the keyPos for the key that should be deleted.
   * @return ret where actions is set to BPlusReturn.NONE
   * @exception IOException
   *              if an error occurs
   */
  protected BPlusReturn collapseRoot(BPlusReturn ret) throws IOException {
    if (leafLevel == -1) {
      return null;
    }
    SortedBlock sb = getBlock(rootPage);
    if (sb.getNumberOfElements() > 1) {
      int pos = ret.keyPos;
      // this case should not happen...but keep it just in case:
      if (pos == sb.getNumberOfElements()) {
        pos--;
        setLastPointer(sb, ((BTreeKey) sb.getKey(pos)).leftNode);
        sb.deleteKey(pos);
      }
      else
        sb.deleteKey(pos);
      writeBlock(rootPage, sb);
      ret.action = BPlusReturn.NONE;
      return ret;
    }
    // we have to collapse the root:
    indexFile.removeBlock(indexFile.getLogicalBlockNo(rootPage));
    if (leafLevel == 0) { // we just removed the only block we had
      rootPage = valueFile.getPhysicalBlockNo(0);
    }
    else
      rootPage = getLastPointer(sb);
    leafLevel--;
    ret.action = BPlusReturn.NONE;
    return ret;
  }

  /**
   * Create a new root containing one key. The leftKey in the new rootKey will
   * contain the old root pointer.
   * 
   * @param rootKey
   *          key in the new root.
   * @exception IOException
   *              if an error occurs
   */
  protected void createRoot(BTreeKey rootKey) throws IOException {
    if (leafLevel == -1)
      indexFile.insertBlock(-1);
    else
      indexFile.insertBlock(indexFile.getLastBlockNo());
    // create the new block:
    SortedBlock sb = new SortedBlock();
    sb.setBlock(new byte[indexFile.getBlockSize()], indexKeys, true,
        SortedBlock.PTR_NORMAL, (short) 4);
    // set right pointer to newly created KeyValue block and
    // left key to old rightmost key
    setLastPointer(sb, rootKey.leftNode);
    rootKey.leftNode = rootPage;
    sb.insertKey(rootKey);
    // set new root:
    rootPage = indexFile.getPhysicalBlockNo(indexFile.getLastBlockNo());
    writeBlock(rootPage, sb);
    leafLevel++;
  }

  // UTILITY:
  /**
   * Checks if a sorted block contains the minimm amount of information to not
   * be deemed "underflowed".
   * 
   * @param sb
   *          a sorted block to check
   * @return true if the block has the minimum amount of information
   */
  protected boolean checkMinimum(SortedBlock sb) {
    return sb.getDataAndPointersBytes() > (sb.storageCapacity() / 2) ? true
        : false;
  }

  /**
   * Returns the block number just left to the block contianing a specific key.
   * 
   * @param search
   *          a search for a key.
   * @param sb
   *          the sorted block where to find the neighbor.
   * @return the node (i.e block number) to the left child or -1 if there are no
   *         left child.
   */
  protected int getPreviousNeighbor(int search, SortedBlock sb) {
    int pos = getPreviousPos(search);
    if (pos == -1)
      return -1;
    return ((BTreeKey) sb.getKey(pos)).leftNode;
  }

  /**
   * Postion of the previous key given a search
   * 
   * @param search
   *          a search for a key
   * @return position
   */
  protected int getPreviousPos(int search) {
    int pos = getPos(search);
    return pos - 1;
  }

  /**
   * Returns the position for the next key given a search.
   * 
   * @param search
   *          a search for a key
   * @return position
   */
  protected int getNextPos(int search) {
    int pos = getPos(search);
    return pos + 1;
  }

  /**
   * Returns the block number just right to the block containing a specific key.
   * 
   * @param search
   *          a search for a key.
   * @param sb
   *          the sorted block where to find the neighbor.
   * @return the node (i.e block number) to the right neighbor.
   */
  protected int getNextNeighbor(int search, SortedBlock sb) {
    int pos = getNextPos(search);
    if (pos > sb.getNumberOfElements())
      return -1;
    if (pos == sb.getNumberOfElements())
      return getLastPointer(sb);
    return ((BTreeKey) sb.getKey(pos)).leftNode;
  }

  /**
   * Given a search returns the position for the key just larger than the key
   * searched for.
   * 
   * @param search
   *          a search for a key.
   * @return the position for the key just after the key searched for.
   */
  protected int getPos(int search) {
    if (search >= 0)
      return search + 1;
    else
      return Math.abs(search) - 1;
  }

  /**
   * Given a search returns the node to follow in the index to find the given
   * key. At the leafLevel the node is the blocknumber to the right block in the
   * key/value file.
   * 
   * @param search
   *          a search for a key
   * @param sb
   *          the block to search
   * @return node
   */
  protected int getNode(int search, SortedBlock sb) {
    int pos = getPos(search);
    if (pos == sb.getNumberOfElements())
      return getLastPointer(sb);
    return ((BTreeKey) sb.getKey(pos)).leftNode;
  }

  /**
   * Returns the block at the given block number in the index file.
   * 
   * @param blockNo
   *          the block to retrive
   * @return a sorted block of keys.
   */
  protected SortedBlock getBlock(int blockNo) {
    SortedBlock sb = new SortedBlock();
    try {
      sb.setBlock(indexFile.readPhysicalBlock(blockNo), indexKeys, false,
          SortedBlock.PTR_NORMAL, (short) 4);
      return sb;
    }
    catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "could not read block", e);
      return null;
    }
  }

  /**
   * Writes a given block in the index file.
   * 
   * @param blockNo
   *          where in the file the closk should be written.
   * @param sb
   *          the block to write
   * @exception IOException
   *              if an error occurs
   */
  protected void writeBlock(int blockNo, SortedBlock sb) throws IOException {
    indexFile.writePhysicalBlock(sb.getBlock(), blockNo);
  }

  // MANIPULATING THE INDEX:
  /**
   * Given two blocks this methods moves keys from the right block to the left
   * (starting with moving the parent into the left block, moving the smallest
   * key in the right block to the parent and so on. The method only shifts if
   * it gains anything from it, i.e continusly check the current shift.
   * <code>if(parent.byteSize()+left.getDataBytes() >=
   *    right.getDataBytes() - right.getFirstKey().byteSize()){
   *   break;
   *  }
   * </code>
   * Shifting is needed when balancing blocks in the BTree index.
   * 
   * @param left
   *          block with smaller values.
   * @param right
   *          block with larger values.
   * @param parent
   *          the parent node in the index.
   * @return true if at least one key was shifted
   */
  protected boolean shiftLeft(SortedBlock left, SortedBlock right,
      BTreeKey parent) {
    // check if we gain anything from a shift, i.e. the minimum shift:
    if (parent.byteSize() + left.getDataBytes() >= right.getDataBytes()
        - right.getFirstKey().byteSize()) {
      return false;
    }
    // first set parent lefkey to first left key in right and save the old left:
    int parentLeft = parent.leftNode;
    int tmp;
    for (;;) {
      parent.leftNode = getLastPointer(left);
      left.insertKeyUnsorted(parent);
      parent = (BTreeKey) right.deleteKey(0);
      setLastPointer(left, parent.leftNode);
      // now check if to continue:
      if (parent.byteSize() + left.getDataBytes() >= right.getDataBytes()
          - right.getFirstKey().byteSize())
        break;
    }
    parent.leftNode = parentLeft;
    return true;
  }

  /**
   * The same as for shiftLeft but in the other direction
   * 
   * @param left
   *          block with smaller values
   * @param right
   *          block with higher values
   * @param parent
   *          the parent node in the index
   * @return true if at least one key was shifted
   * @see #shiftLeft
   */
  protected boolean shiftRight(SortedBlock left, SortedBlock right,
      BTreeKey parent) {
    // check if we gain anything from a shift, i.e. the minimum shift:
    if (parent.byteSize() + right.getDataBytes() >= left.getDataBytes()
        - left.getLastKey().byteSize()) {
      return false;
    }
    // first set parent lefkey to first left key in right and save the old left:
    int parentLeft = parent.leftNode;
    for (;;) {
      parent.leftNode = getLastPointer(left);
      right.insertKey(parent);
      parent = (BTreeKey) left.deleteKey(left.getNumberOfElements() - 1);
      setLastPointer(left, parent.leftNode);
      // now check if to continue:
      if (parent.byteSize() + right.getDataBytes() >= left.getDataBytes()
          - left.getLastKey().byteSize())
        break;
    }
    parent.leftNode = parentLeft;
    return true;
  }

  /**
   * Returns the last (right most) pointer in a BTree index block.
   * 
   * @param sb
   *          sorted block of BTree keys.
   * @return the last pointer.
   */
  protected int getLastPointer(SortedBlock sb) {
    ByteBuffer buffer = sb.getByteBuffer();
    return buffer.getInt(sb.getReservedSpaceStart());
    // return ByteStorage.readInt(sb.getBlock(), sb.getReservedSpaceStart());
  }

  /**
   * Set the last (right-most) pointer in a BTree index block.
   * 
   * @param sb
   *          a block of sorted BTree keys.
   * @param pointer
   *          a pointer (i.e block number).
   */
  protected void setLastPointer(SortedBlock sb, int pointer) {
    ByteBuffer buffer = sb.getByteBuffer();
    buffer.putInt(sb.getReservedSpaceStart(), pointer);
    // ByteStorage.writeInt(sb.getBlock(), sb.getReservedSpaceStart(),
    // pointer);
  }

  /**
   * Deletes a key in a BTree block and replace (update) the child pointer to
   * reflect the change.
   *
   * @param keyIndex
   *          the key to recplace
   * @param sb
   *          a block of sotred BTree keys.
   */
  protected void deleteAndReplace(BTreeKey keyIndex, SortedBlock sb) {
    if (keyIndex.compareTo(sb.getLastKey()) == 0)
      setLastPointer(sb, keyIndex.leftNode);
    sb.deleteKey(keyIndex);
  }

  /**
   * Insert a key into the BTree index and update the childpointers to reflect
   * the change.
   *
   * @param keyIndex
   *          the key to insert
   * @param sb
   *          a block of sorted BTree keys
   */
  protected void insertAndReplace(BTreeKey keyIndex, SortedBlock sb) {
    int index = sb.insertKey(keyIndex);
    int tmp = keyIndex.leftNode;
    if (index == sb.getNumberOfElements() - 1) { // last key
      keyIndex.leftNode = getLastPointer(sb);
      setLastPointer(sb, tmp);
      sb.updateKey(keyIndex, index);
      return;
    }
    BTreeKey nextKey = (BTreeKey) sb.getKey(index + 1);
    keyIndex.leftNode = nextKey.leftNode;
    nextKey.leftNode = tmp;
    sb.updateKey(keyIndex, index);
    sb.updateKey(nextKey, index + 1);
  }

  /**
   * Read a block from the key/value file.
   * 
   * @param blockNo
   *          a logical block number
   * @return a sorted block of key/value pairs
   */
  protected SortedBlock getVBlock(int blockNo) {
    SortedBlock sb = new SortedBlock();
    try {
      sb.setBlock(valueFile.readBlock(blockNo), keyValues, false,
          SortedBlock.PTR_NORMAL, (short) 0);
      return sb;
    }
    catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "could not read block", e);
      return null;
    }
  }

  /**
   * Read a block from the key/value file.
   * 
   * @param blockNo
   *          a physical block number
   * @return a sorted block of key/value pairs
   */
  protected SortedBlock getPVBlock(int blockNo) {
    SortedBlock sb = new SortedBlock();
    try {
      sb.setBlock(valueFile.readPhysicalBlock(blockNo), keyValues, false,
          SortedBlock.PTR_NORMAL, (short) 0);
      return sb;
    }
    catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "could not read block", e);
      return null;
    }
  }

  private void writeVBlock(int blockNo, SortedBlock sb) throws IOException {
    valueFile.writeBlock(sb.getBlock(), blockNo);
    // int logicalBlockNo = blockNo;
    int count = sb.getNumberOfElements();
    blockCountFile.updateRecord(blockNo, count);
  }

  private void writePVBlock(int blockNo, SortedBlock sb) throws IOException {
    valueFile.writePhysicalBlock(sb.getBlock(), blockNo);
    int logicalBlockNo = valueFile.getLogicalBlockNo(blockNo);
    int count = sb.getNumberOfElements();
    blockCountFile.updateRecord(logicalBlockNo, count);
  }

  private void redistribute(SortedBlock small, SortedBlock large, int bSmall,
      int bLarge) throws IOException {
    SortedBlock blocks[] = new SortedBlock[2];
    blocks[0] = small;
    blocks[1] = large;
    SortedBlock.redistribute(blocks);
    writePVBlock(bSmall, small);
    writePVBlock(bLarge, large);
  }

  // INITIALIZERS:
  private void initBPlusTree(ByteBuffer bb, boolean useOldPaths)
      throws IOException, ClassNotFoundException, InstantiationException,
      IllegalAccessException {
    CBString tmp = new CBString();
    keyValues = new KeyValue();
    // byteSize:
    int bSize = bb.getInt(); // read for clarity
    // the key name:
    tmp = (CBString) tmp.fromBytes(bb);
    keyValues.setKey((ByteStorable) Class.forName(tmp.get())
        .newInstance());
    // the value name:
    tmp = (CBString) tmp.fromBytes(bb);
    keyValues.setValue((ByteStorable) Class.forName(tmp.get())
        .newInstance());
    // the index keys:
    indexKeys = new BTreeKey(keyValues.getKey(), -1);
    // the index file:
    tmp = (CBString) tmp.fromBytes(bb);
    indexName = tmp.get();
    // the value file:
    tmp = (CBString) tmp.fromBytes(bb);
    valueName = tmp.get();
    // block count file:
    tmp = (CBString) tmp.fromBytes(bb);
    blockCountFileName = tmp.get();
    // root page:
    rootPage = bb.getInt();
    // leaf level:
    leafLevel = bb.getInt();
    // finally open the index and value files:
    // possibley modified read paths:
    if (!useOldPaths) {
      indexName = fName + ".Index";
      valueName = fName + ".Values";
    }
    openIndex(false, -1);
    openValues(false, -1);
    openBlockCountFile(false);
  }

  private void blockCountFileIntegrityCheck() throws IOException{
    try {
      if (valueFile.getNumberOfBlocks() != blockCountFile.numRecords()) {
       CoreLog.L().info("Rebuilding block count file...num blocks differs "
                + valueFile.getNumberOfBlocks() + " "
                + blockCountFile.numRecords());
        rebuildBlockCountFile();
        return;
      }
      for (int blockno = 0; blockno < valueFile.getNumberOfBlocks(); blockno++) {
        SortedBlock sb = this.getVBlock(blockno);
        if (blockCountFile.get(blockno) != sb.getNumberOfElements()) {
          CoreLog.L().info("Rebuilding block count file...block count differs");
          rebuildBlockCountFile();
          return;
        }
      }
    }
    catch (Exception e) {

        CoreLog.L().warning("Trying to rebuild block count file...");
        rebuildBlockCountFile();
    }
  }

  private void rebuildBlockCountFile() throws IOException {
    // openBlockCountFile(true);
    blockCountFile = new IntArrayFile(blockCountFileName, false, true, false);
    for (int blockno = 0; blockno < valueFile.getNumberOfBlocks(); blockno++) {
      SortedBlock sb = this.getVBlock(blockno);
      blockCountFile.put(sb.getNumberOfElements());
    }
    blockCountFile.close();
    blockCountFile = new IntArrayFile(blockCountFileName, false, false, false);
  }

  private void openBlockCountFile(boolean newIndex) throws IOException {
    blockCountFile = new IntArrayFile(blockCountFileName, false, newIndex,
        false);
    if (FORCE_INTEGRITY)
      blockCountFileIntegrityCheck();
  }

  private void openIndex(boolean newIndex, int blockSize) throws IOException {
    indexFile = new BlockFile(blockSize, indexName, newIndex);
    // indexFile.setCache(new BlockFullCache());
  }

  private void openValues(boolean newIndex, int blockSize) throws IOException {
    valueFile = new BlockFile(blockSize, valueName, newIndex);
    // valueFile.setCache(new BlockFullCache());
  }

  private void buildPointers(int pNo, List <Integer> ptr, int level){
    if(pNo == -1) return;
    BTreeKey bKey;
    SortedBlock sb = getBlock(pNo);
    if(level == leafLevel){
      BPlusHelper.extractPointers(ptr, sb);
    }
    else{
      for (int i = 0; i < sb.getNumberOfElements(); i++) {
        bKey = (BTreeKey) sb.getKey(i);
        buildPointers(bKey.leftNode, ptr, level + 1);
        if (i + 1 == sb.getNumberOfElements())
          buildPointers(getLastPointer(sb), ptr, level + 1);
      }
    }
  }

  // PRINTING:
  private void buildOutputTree(int pNo, StringBuffer buff, int level, boolean printLeaf) {
    if (pNo == -1) // no root
      return;
    SortedBlock sb = getBlock(pNo);
    BTreeKey bKey;
    preTab(level, buff);
    if (level == leafLevel) { // final level
      if(printLeaf){
        buff.append("\n LeafLevel: physical block:" + pNo + "\n");
        buff.append("rightMostPointer: " + getLastPointer(sb) + "\n");
        buff.append(sb);
      }
    }
    else {
      buff.append("\n level: " + level + " physical block " + pNo + "\n");
      buff.append("rightMostPointer: " + getLastPointer(sb) + "\n");
      buff.append(sb);
      for (int i = 0; i < sb.getNumberOfElements(); i++) {
        bKey = (BTreeKey) sb.getKey(i);
        buildOutputTree(bKey.leftNode, buff, level + 1, printLeaf);
        if (i + 1 == sb.getNumberOfElements())
          buildOutputTree(getLastPointer(sb), buff, level + 1, printLeaf);
      }
    }
  }

  private void preTab(int level, StringBuffer buff) {
    for (int i = 0; i < level; i++)
      buff.append('\t');
  }

  /** ******************INNER CLASSES**************************************** */
  class SBBNo {
    SortedBlock sb;
    int bNo;
  }

  class ABPIterator implements Iterator <KeyValue <ByteStorable, ByteStorable>> {
    Iterator fileIterator;
    SortedBlock sb = new SortedBlock();
    Iterator sbIterator;
    byte b[];

    public ABPIterator(ByteStorable key) {
      int blockNo = searchBlock(key);
      fileIterator = valueFile.iterator(blockNo, false);
      if (!fileIterator.hasNext())
        return;
      b = (byte[]) fileIterator.next();
      sb.setBlock(b, keyValues, false, SortedBlock.PTR_NORMAL);
      sbIterator = sb.iterator(new KeyValue(key, null));
    }

    public ABPIterator() {
      fileIterator = valueFile.iterator();
      if (!fileIterator.hasNext())
        return;
      b = (byte[]) fileIterator.next();
      sb.setBlock(b, keyValues, false, SortedBlock.PTR_NORMAL);
      sbIterator = sb.iterator();
    }

    public boolean hasNext() {
      if (sbIterator == null)
        return false;
      if (sbIterator.hasNext())
        return true;
      while (fileIterator.hasNext()) {
        b = (byte[]) fileIterator.next();
        sb.setBlock(b, keyValues, false, SortedBlock.PTR_NORMAL);
        sbIterator = sb.iterator();
        if (sbIterator.hasNext())
          return true;
      }
      return false;
    }

    public KeyValue <ByteStorable, ByteStorable> next() {
      return (KeyValue <ByteStorable, ByteStorable>) sbIterator.next();
    }

    public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }
  }
}
