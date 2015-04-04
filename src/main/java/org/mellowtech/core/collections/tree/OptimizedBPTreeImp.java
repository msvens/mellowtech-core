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
package org.mellowtech.core.collections.tree;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.ByteComparable;
import org.mellowtech.core.bytestorable.ByteStorable;
import org.mellowtech.core.bytestorable.CBBoolean;
import org.mellowtech.core.bytestorable.io.SortedBlock;
import org.mellowtech.core.collections.KeyValue;
import org.mellowtech.core.io.*;


/**
 * <code>BPTree</code> is a representation of a minimal
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
 * @author Martin Svensson
 * @version 1.0
 */
@Deprecated
@SuppressWarnings("rawtypes")
public class OptimizedBPTreeImp<K extends ByteComparable, V extends ByteStorable>
        implements BTree<K, V> {
  private static final boolean FORCE_INTEGRITY = false;

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
  protected SplitRecordFile splitFile;
  /**
   * Used for reading and writing key/value pairs to the key file.
   */
  protected KeyValue<K, V> keyValues;
  /**
   * Used for reading and writing keys to the index.
   */
  protected BTreeKey <K> indexKeys;
  /**
   * Filename for the IndexFile.
   */
  protected String indexName;

  /**
   * The name of this BPlusTree.
   */
  protected String fName;
  protected OptimizedBPlusHelper <K,V> helper;
  protected K keyType;
  protected V valueType;
  protected int size = 0;
  //For Caching
  boolean useCache = false;
  boolean fullIndex = true;
  boolean readOnly = true;

  /** ***************CONSTRUCTORS************************** */

  /**
   * Opens an existing <code>BPTreeImp</code>.
   *
   * @param fName Name of the BPlusTree to open.
   * @throws Exception if an error occurs
   */
  public OptimizedBPTreeImp(String fName, K keyType, V valueType) throws Exception {
    // open the file:
    this.fName = fName;
    indexName = fName + ".idx";
    keyValues = new KeyValue <> (keyType, valueType);
    indexKeys = new BTreeKey <> (keyType, 0);

    openIndex(false, -1, -1);

    initBPlusTree(ByteBuffer.wrap(splitFile.getReserve()));
    this.helper = new OptimizedBPlusHelper <> (this);
    return;
  }

  /**
   * Creates a new <code>BPTreeImp</code>.
   *
   * @param fName          the name of the BPlusTree
   * @param keyType        The type of keys stored in this tree
   * @param valueType      The type of values stored in this tree
   * @param indexBlockSize Size of index blocks holding key separators. The larger the size the more separators can
   *                       fit in each block. The size has to be at least twice the size that
   *                       of the maximum size of a key
   * @param valueBlockSize Size of blocks holding key/values.
   * @throws java.io.IOException if an error occurs
   */
  public OptimizedBPTreeImp(String fName, K keyType,
                            V valueType, int indexBlockSize, int valueBlockSize)
          throws IOException {

    indexName = fName + ".idx";
    this.keyType = keyType;
    this.valueType = valueType;

    keyValues = new KeyValue <> (keyType, valueType);
    indexKeys = new BTreeKey <> (keyType, 0);
    leafLevel = -1;

    openIndex(true, indexBlockSize, valueBlockSize);


    this.helper = new OptimizedBPlusHelper <> (this);


    SortedBlock <KeyValue <K,V>> sb = new SortedBlock <> ();
    sb.setBlock(new byte[splitFile.getBlockSize()], keyValues, true,
            SortedBlock.PTR_NORMAL);

    rootPage = splitFile.insert(sb.getBlock());
    //helper.putValueBlock(0, sb);
    this.fName = fName;

  }

  /**
   * Delete this btree on disc
   *
   * @throws java.io.IOException
   */
  public void delete() throws IOException {
    splitFile.close();
    File f = new File(indexName);
    f.delete();
    size = 0;
  }

  /**
   * Save the tree. After a call to this method the tree has to be
   * reopened. Either saveTree or save has to be called before "closing" the
   * tree.
   *
   * @throws java.io.IOException if an error occurs
   * @see #save
   */
  @Override
  public void save() throws IOException {
    //save index info:
    ByteBuffer bb = ByteBuffer.allocate(20);
    bb.putInt(rootPage);
    bb.putInt(leafLevel);
    bb.putInt(size);

    //Cache Info:
    CBBoolean tmp = new CBBoolean(useCache);
    tmp.toBytes(bb);
    tmp.set(fullIndex);
    tmp.toBytes(bb);
    tmp.set(readOnly);
    tmp.toBytes(bb);
    splitFile.setReserve(bb.array());
    splitFile.save();
  }

  @Override
  public void close() throws IOException {
    save();
    splitFile.close();
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return size < 1;
  }

  // PRINT METHODS:


  /**
   * Check if a key is stored in this tree.
   *
   * @param key key to search for
   * @return true if the key exists.
   * @throws java.io.IOException if an error occurs
   */
  @Override
  public boolean containsKey(K key) throws IOException {
    return getKeyValue(key) != null;
  }

  @Override
  public void put(K key, V value) throws IOException {
    insertUpdate(key, value, true);
  }

  @Override
  public void putIfNotExists(K key, V value) throws IOException {
    insertUpdate(key, value, false);
  }


  // ABSTRACT METHODS:

  @Override
  public V remove(K key) {
    try {
      KeyValue<K, V> kv = new KeyValue <> (key, null);
      BPlusReturn<K, V> ret;
      if (leafLevel == -1) { // no index...delete directly to value file:
        ret = deleteKeyValue(kv, rootPage, -1, -1);
        if (ret.returnKey != null) {
          size--;
          return ret.returnKey.getValue();
        } else
          return null;
      }
      BTreeKey <K> searchKey = new BTreeKey <> (key, -1);
      ret = delete(rootPage, -1, -1, searchKey, kv, 0);

      if (ret != null && ret.action == BPlusReturn.SPLIT) { // the underlying
        // root split
        createRoot(ret.promo);
      }

      if (ret.returnKey != null) {
        size--;
        return ret.returnKey.getValue();
      }
      return null;
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public V get(K key) throws IOException {
    KeyValue <K,V> kv = getKeyValue(key);
    return (kv == null) ? null : (V) kv.getValue();
  }

  @Override
  public K getKey(int pos) throws IOException {
    if (pos < 0 || pos >= size)
      throw new IOException("position out of bounds");

    List<Integer> blocks = helper.getLogicalBlocks(rootPage, leafLevel);
    int curr = 0;
    for (; curr < blocks.size(); curr++) {
      int bNo = blocks.get(curr);
      SortedBlock <KeyValue <K,V>> sb = helper.getValueBlock(bNo);
      if (pos < sb.getNumberOfElements()) {
        return sb.getKey(pos).getKey();
      }
      pos -= sb.getNumberOfElements();
    }
    return null;
  }

  // SEARCHING:
  @Override
  public KeyValue<K, V> getKeyValue(K key) throws IOException {
    int block = searchBlock(key);
    if (block == -1)
      return null;
    return searchValueFile(key, block);
  }

  @Override
  public TreePosition getPosition(K key) throws IOException {
    int block = searchBlock(key);
    if (block == -1)
      return null;
    return searchValueFilePosition(key, block);
  }

  @Override
  public TreePosition getPositionWithMissing(K key) throws IOException {
    int block = searchBlock(key);
    if (block == -1)
      return null;
    return searchValueFilePositionNoStrict(key, block);
  }

  // ITERATORS:
  @Override
  public Iterator<KeyValue<K, V>> iterator() {
    return new BPIterator();
  }

  @Override
  public Iterator<KeyValue<K, V>> iterator(K from) {
    return new BPIterator(from);
  }

  @Override
  public void compact() throws IOException {
    //valueFile.compact();
    //indexFile.compact();
  }

  public void useCache(boolean fullIndex, boolean readOnly) {
    this.useCache = true;
    this.fullIndex = fullIndex;
    this.readOnly = readOnly;
    /*
    if(fullIndex)
      this.indexFile.setCache(readOnly, Integer.MAX_VALUE, false);
    else
      this.indexFile.setCache(readOnly, 1024*1024, true);
    */
  }

  public String toString() {
    StringBuffer sbuff = new StringBuffer();
    // first print the index:
    try {
      helper.buildOutputTree(rootPage, sbuff, 0, true);
    } catch (IOException e) {
      CoreLog.L().warning("could not build index tree");
    }
    // then print the keys:
    sbuff.append("\n*****************VALUE FILE***********************\n\n");
    //SortedBlock sb;
    for (Iterator<Record> iter = splitFile.iterator(); iter.hasNext(); ) {
      Record next = iter.next();
      sbuff.append("\n\n");
      sbuff.append("physical block: " + next.record);
      sbuff.append("\n" + helper.toValueBlock(next.data) + "\n");
    }
    return sbuff.toString();
  }

  /**
   * Implementing classes has to implement this method. It should return the
   * physical blocknumber in the valuefile where the key/value pair should be
   * stored if it exists.
   *
   * @param key the key to search.
   * @return a blocknumber
   */
  protected int searchBlock(K key) {
    try {
      if (leafLevel == -1)
        return rootPage;
      BTreeKey <K> bTreeKey = new BTreeKey <> (key, 0);
      return searchBlock(rootPage, bTreeKey, 0);
    } catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "could not find block", e);
      return -1;
    }
  }

  private int searchBlock(int pNo, BTreeKey <K> key, int level) throws IOException {
    SortedBlock <BTreeKey <K>> sb = helper.getIndexBlock(pNo);
    if (level == leafLevel) {
      return helper.getNode(sb.binarySearch(key), sb);
    }
    return searchBlock(helper.getNode(sb.binarySearch(key), sb), key, level + 1);
  }

  private void insertUpdate(K key, V value, boolean update)
          throws IOException {
    KeyValue<K, V> kv = new KeyValue <> (key, value);
    if (leafLevel == -1) {
      // no index...to insert directly to value file...in first logical block:
      BPlusReturn <K,V> ret = insertKeyValue(kv, rootPage, update);
      if (ret != null && ret.action == BPlusReturn.SPLIT) {
        // we now have to value blocks time for index
        ret.promo.get().leftNode = ret.newBlockNo;
        createRoot(ret.promo);
      }
      return;
    }
    BTreeKey <K> searchKey = new BTreeKey <> (key, -1);
    BTreeKey <K> rootKey = insert(rootPage, searchKey, kv, 0, update);
    if (rootKey != null)
      createRoot(rootKey);

  }

  private BTreeKey <K> insert(int pNo, BTreeKey <K> key, KeyValue <K,V> kv, int level,
                          boolean update) throws IOException {
    SortedBlock <BTreeKey <K>> sb = helper.getIndexBlock(pNo);
    BTreeKey <K> keyIndex = null;
    if (level == leafLevel) {
      try {
        BPlusReturn <K,V> ret = insertKeyValue(kv, helper.getNode(sb.binarySearch(key), sb),
                update);
        if (ret != null) { // this forced a split...
          keyIndex = ret.promo;
          keyIndex.get().leftNode = ret.newBlockNo;
        }
      } catch (Exception e) {
        CoreLog.L().log(Level.SEVERE, pNo + " " + key + " " + kv, e);
        throw new IOException(e);
      }
    } else
      keyIndex = insert(helper.getNode(sb.binarySearch(key), sb), key, kv, level + 1,
              update);
    // insert the key into the index and split if necessary:
    if (keyIndex == null)
      return null;
    else
      return insertKey(sb, pNo, keyIndex);
  }

  private BTreeKey <K> insertKey(SortedBlock <BTreeKey <K>> sb, int pNo, BTreeKey <K> keyIndex)
          throws IOException {
    if (sb.fitsKey(keyIndex)) {
      helper.insertAndReplace(keyIndex, sb);
      helper.putIndexBlock(pNo, sb);
      return null;
    }
    SortedBlock <BTreeKey <K>> sb1 = sb.splitBlock();
    BTreeKey <K> first = sb1.getFirstKey();
    helper.setLastPointer(sb, first.get().leftNode);
    if (keyIndex.compareTo(sb.getLastKey()) < 0) {
      helper.insertAndReplace(keyIndex, sb);
    } else
      helper.insertAndReplace(keyIndex, sb1);
    // find the shortest separator:
    BTreeKey <K> promo = sb1.getFirstKey();
    helper.deleteAndReplace(promo, sb1);
    helper.putIndexBlock(pNo, sb);
    promo.get().leftNode = splitFile.insertRegion(sb1.getBlock());
    return promo;
  }

  private BPlusReturn <K,V> delete(int pNo, int pBlock, int pSearch, BTreeKey <K> key,
                             KeyValue<K, V> kv, int level) throws IOException {
    SortedBlock <BTreeKey <K>> sb = helper.getIndexBlock(pNo);
    BPlusReturn <K,V> ret;
    int search = sb.binarySearch(key);
    int node = helper.getNode(search, sb);
    if (level == leafLevel) {
      int left = helper.getPreviousNeighbor(search, sb);
      int right = helper.getNextNeighbor(search, sb);
      ret = deleteKeyValue(kv, node, left, right);
      if (ret == null) {

        return null;
      }
      if (ret.newBlockNo == left)
        ret.keyPos = helper.getPreviousPos(search);
      else
        ret.keyPos = helper.getPos(search);
    } else
      ret = delete(node, pNo, search, key, kv, level + 1);
    // now move up the tree:
    // nothing to be done:
    if (ret == null || ret.action == BPlusReturn.NONE)
      return ret;
    // remove old key...posible underflow...results in a new merge/redistribute
    if (ret.action == BPlusReturn.MERGE) {
      if (pNo == rootPage)
        return collapseRoot(ret);
      handleMerge(sb, ret, pNo, pSearch, pBlock);
      return ret;
    }
    // replace old key with new key...results in a possible split.
    if (ret.action == BPlusReturn.REDISTRIBUTE) {
      handleRedistribute(sb, pNo, ret);
      return ret;
    }
    // the child caused a split...add new key...continue as insert...
    if (ret.action == BPlusReturn.SPLIT) { // since keys are variable length
      BTreeKey <K> promo = insertKey(sb, pNo, ret.promo);
      if (promo != null) {
        ret.action = BPlusReturn.SPLIT;
        ret.promo = promo;
      } else
        ret.action = BPlusReturn.NONE;
      return ret;
    }
    return null;
  }

  // remove the old key and transform into regular insert...

  /**
   * When a deletion caused a redistribution of keys between blocks we could
   * possibly end up having to split the parent node (since keys are variable in
   * size). This method deletes the old separator and add the new separator in a
   * sorted block. It then possible splits the block (and consequently reverts
   * to an insert in the index).
   *
   * @param sb     the sorted block conting the separator that should be replace.
   * @param cBlock the physical address of the block.
   * @param ret    the value returned from the underlying deletion...containing
   *               notably the new separator and position of the key that should be
   *               deleted.
   * @throws java.io.IOException if an error occurs
   */
  protected void handleRedistribute(SortedBlock <BTreeKey <K>> sb, int cBlock, BPlusReturn <K,V> ret)
          throws IOException {
    int pos = ret.keyPos;
    BTreeKey <K> changed, next;
    int tmp;
    changed = sb.deleteKey(pos);
    // no need to do some more work?
    if (changed.get().key.byteSize() >= ret.promo.get().key.byteSize()) {
      changed.get().key = ret.promo.get().key;
      sb.insertKey(changed);
      helper.putIndexBlock(cBlock, sb);
    } else { // treat as normal insert...tweak pointers to fit insertKey scheme
      changed.get().key = ret.promo.get().key;
      if (pos < sb.getNumberOfElements()) {
        next = sb.getKey(pos);
        tmp = next.get().leftNode;
        next.get().leftNode = changed.get().leftNode;
        changed.get().leftNode = tmp;
        sb.updateKey(next, pos);
      } else {
        tmp = changed.get().leftNode;
        changed.get().leftNode = helper.getLastPointer(sb);
        helper.setLastPointer(sb, tmp);
      }

      BTreeKey <K> promo = insertKey(sb, cBlock, changed);
      if (promo != null) {
        ret.promo = promo;
        ret.action = BPlusReturn.SPLIT;
        return;
      }
    }
    ret.action = BPlusReturn.NONE;
  }

  /**
   * Hadles a merge. This could either result in a redistribution of keys, just
   * deletion of the key, or another merge.
   *
   * @param sb      Sorted block containing the old separator
   * @param ret     propagate upwards, containg among things the position of the to be
   *                deleted.
   * @param cBlock  the block number for the current sorted block.
   * @param pSearch the search for the parent key
   * @param pBlock  the block number for the parent block.
   * @throws java.io.IOException if an error occurs
   */
  protected void handleMerge(SortedBlock <BTreeKey <K>> sb, BPlusReturn <K,V> ret, int cBlock,
                             int pSearch, int pBlock) throws IOException {
    // get position to remove:
    int pos = ret.keyPos;
    if (pos == sb.getNumberOfElements()) {
      pos--;
      helper.setLastPointer(sb, sb.getKey(pos).get().leftNode);
      sb.deleteKey(pos);
    } else
      sb.deleteKey(pos);
    // no underflow?:
    if (helper.checkUnderflow(sb)) {
      helper.putIndexBlock(cBlock, sb);
      ret.action = BPlusReturn.NONE;
      return;
    }
    // reblance blocks...start with redistribute:
    SortedBlock <BTreeKey <K>> parent = helper.getIndexBlock(pBlock);
    int leftSib, rightSib;
    SortedBlock <BTreeKey <K>> sib = null;
    // redistribute:
    leftSib = helper.getPreviousNeighbor(pSearch, parent);
    if (leftSib != -1) {
      sib = helper.getIndexBlock(leftSib);
      if (helper.checkUnderflow(sib)) {
        BTreeKey <K> pKey = parent.getKey(helper.getPreviousPos(pSearch));
        //BTreeKey pKey = (BTreeKey) parent.getKey(helper.getPos(pSearch));
        if (helper.shiftRight(sib, sb, pKey)) {
          helper.putIndexBlock(leftSib, sib);
          helper.putIndexBlock(cBlock, sb);
          ret.promo = pKey;
          ret.action = BPlusReturn.REDISTRIBUTE;
          ret.keyPos = helper.getPreviousPos(pSearch);
          //ret.keyPos = helper.getPos(pSearch);
          return;
        }
      }
    }

    rightSib = helper.getNextNeighbor(pSearch, parent);
    if (rightSib != -1) {
      sib = helper.getIndexBlock(rightSib);
      if (helper.checkUnderflow(sib)) {

        BTreeKey <K> pKey = parent.getKey(helper.getPos(pSearch));


        if (helper.shiftLeft(sb, sib, pKey)) {
          helper.putIndexBlock(cBlock, sb);
          helper.putIndexBlock(rightSib, sib);
          ret.promo = pKey;

          ret.action = BPlusReturn.REDISTRIBUTE;
          ret.keyPos = helper.getPos(pSearch);
          return;
        }
      }
    }
    // worst case scenario...merge:
    BTreeKey <K> pKey;
    if (leftSib != -1) {
      sib = helper.getIndexBlock(leftSib);
      pKey = parent.getKey(helper.getPreviousPos(pSearch));
      pKey.get().leftNode = helper.getLastPointer(sib);
      if (sb.canMerge(sib, pKey)) {
        sb.mergeBlock(sib);
        sb.insertKey(pKey);
        splitFile.deleteRegion(leftSib);
        helper.putIndexBlock(cBlock, sb);
        ret.action = BPlusReturn.MERGE;
        ret.keyPos = helper.getPreviousPos(pSearch);
        return;
      }
    }
    if (rightSib != -1) {
      sib = helper.getIndexBlock(rightSib);
      pKey = parent.getKey(helper.getPos(pSearch));
      pKey.get().leftNode = helper.getLastPointer(sb);
      if (sib.canMerge(sb, pKey)) {
        sib.mergeBlock(sb);
        sib.insertKey(pKey);
        splitFile.deleteRegion(cBlock);
        helper.putIndexBlock(rightSib, sib);
        ret.action = BPlusReturn.MERGE;
        ret.keyPos = helper.getPos(pSearch);
        return;
      }
    }
    helper.putIndexBlock(cBlock, sb);
    ret.action = BPlusReturn.NONE;
    return;
  }

  // CREATING INDEX FROM SORTED DATA:

  /**
   * Create an index from a sorted array of key/value pairs. This method is much
   * faster than doing individual insertations. If you you have a large number
   * of keys from which you want to build an index, the best way is to
   * externally sort them and then calling createIndex.
   *
   * @param keysAndValues a sorted array of key/value pairs.
   * @throws java.io.IOException if an error occurs
   */
  public void createIndex(KeyValue <K,V> [] keysAndValues) throws IOException {
    splitFile.clear();
    SortedBlock <KeyValue <K,V>> sb = new SortedBlock <> ();
    byte b[] = new byte[splitFile.getBlockSize()];
    sb.setBlock(b, keyValues, true, SortedBlock.PTR_NORMAL);
    KeyValue <K,V> tmpKV = new KeyValue <> ();
    SBBNo <K> [] levels = (SBBNo <K> []) new SBBNo <?> [20];
    // valueFile.insertBlock(-1);
    //int bNo = 0;
    int bNo = 0 ;
    for (int i = 0; i < keysAndValues.length; i++) {
      tmpKV = keysAndValues[i];
      if (!sb.fitsKey(tmpKV)) {
        splitFile.insert(bNo, sb.getBlock());
        bNo++;
        BTreeKey <K> sep = helper.generateSeparator(sb, tmpKV);
        sep.get().leftNode = bNo - 1;
        insertSeparator(sep, levels, 0, bNo);
        sb.setBlock(b, keyValues, true, SortedBlock.PTR_NORMAL);
      }
      sb.insertKeyUnsorted(tmpKV);
    }
    //write last block
    splitFile.insert(bNo, sb.getBlock());
    if (levels[0] != null) // we have to write the index levels
      writeIndexBlocks(levels);
  }

  /**
   * Create an index from an iterator of key/value pairs. This method is much
   * faster than doing individual insertations. If you you have a large number
   * of keys from which you want to build an index, the best way is to
   * externally sort them and then calling createIndex.
   *
   * @param iterator a sorted stream of key/value pairs.
   * @throws java.io.IOException if an error occurs
   */
  public void createIndex(Iterator<KeyValue <K,V>> iterator) throws IOException {
    splitFile.clear();
    SortedBlock <KeyValue <K,V>> sb = new SortedBlock <> ();
    byte b[] = new byte[splitFile.getBlockSize()];
    sb.setBlock(b, keyValues, true, SortedBlock.PTR_NORMAL);
    KeyValue <K,V> tmpKV = null;
    SBBNo <K> [] levels = (SBBNo <K> []) new SBBNo <?> [20];
    //int bNo = blockFile.getRecordStart();
    int bNo = 0;
    while (iterator.hasNext()) {
      tmpKV = iterator.next();
      if (!sb.fitsKey(tmpKV)) {
        splitFile.insert(bNo, sb.getBlock());
        bNo++;
        BTreeKey <K> sep = helper.generateSeparator(sb, tmpKV);
        sep.get().leftNode = bNo - 1;
        insertSeparator(sep, levels, 0, bNo);
        sb.setBlock(b, keyValues, true, SortedBlock.PTR_NORMAL);
      }
      sb.insertKeyUnsorted(tmpKV);
    }
    splitFile.insert(bNo, sb.getBlock());
    if (levels[0] != null) // we have to write the index levels
      writeIndexBlocks(levels);
  }

  /**
   * CreateIndex calls this method to write all created index blocks to file.
   */
  private void writeIndexBlocks(SBBNo <K> [] levels) throws IOException {
    //boolean removeLast = false;
    int rPage = 0;
    int i = 0;

    for (; i < levels.length; i++) {
      if (levels[i] == null)
        break;
      rPage = levels[i].bNo;
      helper.putIndexBlock(rPage, levels[i].sb);
    }
    leafLevel = i - 1;
    rootPage = rPage;
  }

  /**
   * Insert a separator into the index when creating index from sorted data. If
   * the current block can not hold the data the block will be split into two
   * and the levels possibly increased.
   */
  private void insertSeparator(BTreeKey <K> sep, SBBNo <K> [] levels, int current,
                               int rightNode) throws IOException {
    SortedBlock <BTreeKey <K>> sb = null;
    if (levels[current] == null) { // we have to create a new level
      levels[current] = new SBBNo <> ();
      levels[current].sb = new SortedBlock <> ();
      levels[current].sb.setBlock(new byte[splitFile.getBlockSizeRegion()],
              indexKeys, true, SortedBlock.PTR_NORMAL, (short) 4);
      levels[current].bNo = splitFile.insertRegion(null);
    }
    sb = levels[current].sb;
    if (!sb.fitsKey(sep)) { // save and promote the last key up...
      BTreeKey <K> promo = sb.deleteKey(sb.getNumberOfElements() - 1);
      helper.setLastPointer(sb, promo.get().leftNode);
      promo.get().leftNode = levels[current].bNo;
      helper.putIndexBlock(levels[current].bNo, sb);

      // create the new block:
      sb.setBlock(new byte[splitFile.getBlockSizeRegion()], indexKeys, true,
              SortedBlock.PTR_NORMAL, (short) 4);
      levels[current].sb = sb;
      levels[current].bNo = splitFile.insertRegion(null);
      // promote the last key in the previous block:
      insertSeparator(promo, levels, current + 1, levels[current].bNo);
    }
    // finally insert the separator:
    helper.setLastPointer(sb, rightNode);
    sb.insertKeyUnsorted(sep);
  }

  // PROTECTED METHODS:
  // insertion/deletion/search in value file:

  /**
   * Given a key and blockNumber retrieves the key/value pair for the given key.
   *
   * @param key the key to search for
   * @param bNo the physical blocknumber in the key/value file.
   * @return the Key/value pair or null if the key did not exist
   * @throws java.io.IOException if an error occurs
   */
  protected KeyValue <K,V> searchValueFile(K key, int bNo)
          throws IOException {
    if (splitFile.size() == 0)
      return null;
    SortedBlock <KeyValue <K,V>> sb = helper.getValueBlock(bNo);
    return sb.getKey(new KeyValue <K,V> (key, null));
  }

  /**
   * Given a key and blockNumber retrieves the key/value pair for the given key.
   * Stores the position of the key/value pair in parameter 'pos'. Its position
   * is the number of smaller and greater key/value pairs in the block and in
   * the tree as a whole
   *
   * @param key the key to search for
   * @param bNo the physical blocknumber in the key/value file.
   * @return the Key/value pair or null if the key did not exist
   * @throws java.io.IOException if an error occurs
   */
  protected TreePosition searchValueFilePosition(K key, int bNo)
          throws IOException {
    if (splitFile.size() == 0)
      return null;
    SortedBlock <KeyValue <K,V>> sb = helper.getValueBlock(bNo);
    int smallerInBlock = sb.binarySearch(new KeyValue <K,V> (key, null));
    if (smallerInBlock < 0) return null;
    int elements = size;
    int elementsInBlock = sb.getNumberOfElements();
    Map.Entry<Integer, Integer> cnt = helper.countSmaller(rootPage, 0, leafLevel, bNo);
    int smaller = cnt.getValue() + smallerInBlock;
    return new TreePosition(smaller, elements, smallerInBlock, elementsInBlock);
  }

  /**
   * Given a key and blockNumber retrieves the key/value pair for the given key.
   * Stores the position of the key/value pair in parameter 'pos'. Its position
   * is the number of smaller and greater key/value pairs in the block and in
   * the tree as a whole
   *
   * @param key the key to search for
   * @param bNo the physical blocknumber in the key/value file.
   * @return the Key/value pair or null if the key did not exist
   * @throws java.io.IOException if an error occurs
   */
  protected TreePosition searchValueFilePositionNoStrict(K key, int bNo)
          throws IOException {
    if (splitFile.size() == 0)
      return null;
    SortedBlock <KeyValue <K,V>> sb = helper.getValueBlock(bNo);
    int smallerInBlock = sb.binarySearch(new KeyValue <K,V> (key, null));
    boolean exists = true;
    if (smallerInBlock < 0) { //not found
      exists = false;
      smallerInBlock = Math.abs(smallerInBlock);
      smallerInBlock--; //readjust
    }
    int elementsInBlock = sb.getNumberOfElements();
    int elements = size;
    Map.Entry<Integer, Integer> cnt = helper.countSmaller(rootPage, 0, leafLevel, bNo);
    int smaller = cnt.getValue() + smallerInBlock;
    return new TreePosition(smaller, elements, smallerInBlock, elementsInBlock, exists);
  }

  /**
   * Inserts a key/value pair into the key/value file. A write will return the
   * following: <code>BPlusReturn(BPlusReturn.SPLIT,
   * the key/value,
   * a separator between the old and new block,
   * the new physical block number);</code>
   *
   * @param keyValue the key/value to insert
   * @param bNo      the physical block to inset the key/value
   * @param update   if true only insert if the key did not previously exist
   * @return if the insertation forced a split a BPlusReturn object will be
   *         returned.
   * @throws java.io.IOException if an error occurs
   */
  protected BPlusReturn <K,V> insertKeyValue(KeyValue<K, V> keyValue, int bNo,
                                       boolean update) throws IOException {
    SortedBlock <KeyValue <K,V>> sb = null;
    SortedBlock <KeyValue <K,V>> sb1 = null;
    try {
      sb = helper.getValueBlock(bNo);
      if (sb.containsKey(keyValue)) {
        if (!update)
          return null;
        else {
          sb.deleteKey(keyValue);
          size--;
        }
      }

      size++;

      if (sb.fitsKey(keyValue)) {
        sb.insertKey(keyValue);
        helper.putValueBlock(bNo, sb);
        return null;
      }

      sb1 = new SortedBlock <> ();
      sb1 = sb.splitBlock();
      if (keyValue.compareTo(sb.getLastKey()) <= 0)
        sb.insertKey(keyValue);
      else
        sb1.insertKey(keyValue);
      helper.putValueBlock(bNo, sb);
      int newBlockNo = splitFile.insert(sb1.getBlock());
      return new BPlusReturn <> (BPlusReturn.SPLIT, keyValue, helper.generateSeparator(sb,
              sb1), newBlockNo);
    } catch (Exception e) {
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
   * either left or right node);</code>
   *
   * @param key     the key to delete
   * @param bNo     the physical block number where the key is stored.
   * @param leftNo  the block just left to the current block (i.e just smaller).
   * @param rightNo the block just right to the current block (i.e just larger.
   * @return depending on what action this delete triggered returns either of
   *         three things (see above).
   * @throws java.io.IOException if an error occurs
   */
  protected BPlusReturn <K,V> deleteKeyValue(KeyValue<K, V> key, int bNo, int leftNo,
                                       int rightNo) throws IOException {
    SortedBlock <KeyValue <K,V>> sb = helper.getValueBlock(bNo);
    KeyValue <K,V> deletedKey = sb.deleteKey(key);
    if (deletedKey == null) {
      return null;
    }
    if (helper.checkUnderflow(sb)) {
      helper.putValueBlock(bNo, sb);
      return new BPlusReturn <> (BPlusReturn.NONE, deletedKey, null, -1);
    }
    // reblance...first redistribute:
    SortedBlock <KeyValue <K,V>> sib;
    if (leftNo != -1) {
      sib = helper.getValueBlock(leftNo);
      if (helper.checkUnderflow(sib)) {
        helper.redistributeValueBlocks(sib, sb, leftNo, bNo);
        return new BPlusReturn <> (BPlusReturn.REDISTRIBUTE, deletedKey,
                helper.generateSeparator(sib, sb), leftNo);
      }
    }
    if (rightNo != -1) {
      sib = helper.getValueBlock(rightNo);
      if (helper.checkUnderflow(sib)) {
        helper.redistributeValueBlocks(sb, sib, bNo, rightNo);
        return new BPlusReturn <> (BPlusReturn.REDISTRIBUTE, deletedKey,
                helper.generateSeparator(sb, sib), rightNo);
      }
    }

    // rebalance (merge):
    // try left:
    if (leftNo != -1) {
      sib = helper.getValueBlock(leftNo);
      if (sb.canMerge(sib)) {
        sb.mergeBlock(sib);
        splitFile.delete(leftNo);
        helper.putValueBlock(bNo, sb);
        return new BPlusReturn <> (BPlusReturn.MERGE, deletedKey, null, leftNo);
      }
    }
    if (rightNo != -1) {
      sib = helper.getValueBlock(rightNo);
      if (sib.canMerge(sb)) {
        sib.mergeBlock(sb);
        splitFile.delete(bNo);
        helper.putValueBlock(rightNo, sib);
        return new BPlusReturn <> (BPlusReturn.MERGE, deletedKey, null, rightNo);
      }
    }
    //default no merge or redistribute
    helper.putValueBlock(bNo, sb);
    return new BPlusReturn <> (BPlusReturn.NONE, deletedKey, null, -1);
  }

  // ROOT HANDELING:

  /**
   * Remove a key from the root, possibly altering the root pointer.
   *
   * @param ret contians the keyPos for the key that should be deleted.
   * @return ret where actions is set to BPlusReturn.NONE
   * @throws java.io.IOException if an error occurs
   */
  protected BPlusReturn <K,V> collapseRoot(BPlusReturn <K,V> ret) throws IOException {

    if (leafLevel == -1) {
      return null;
    }
    SortedBlock <BTreeKey <K>> sb = helper.getIndexBlock(rootPage);
    if (sb.getNumberOfElements() > 1) {
      int pos = ret.keyPos;
      // this case should not happen...but keep it just in case:
      if (pos == sb.getNumberOfElements()) {
        pos--;
        helper.setLastPointer(sb, sb.getKey(pos).get().leftNode);
        sb.deleteKey(pos);
      } else
        sb.deleteKey(pos);
      helper.putIndexBlock(rootPage, sb);
      ret.action = BPlusReturn.NONE;
      return ret;
    }
    // we have to collapse the root:
    splitFile.deleteRegion(rootPage);

    //very unsure here!!!
    if (leafLevel == 0) { // we just removed the only block we had
      rootPage = splitFile.getFirstRecord();
    } else
      rootPage = helper.getLastPointer(sb);
    leafLevel--;
    ret.action = BPlusReturn.NONE;
    return ret;
  }

  /**
   * Create a new root containing one key. The leftKey in the new rootKey will
   * contain the old root pointer.
   *
   * @param rootKey key in the new root.
   * @throws java.io.IOException if an error occurs
   */
  protected void createRoot(BTreeKey <K> rootKey) throws IOException {
    int blockNo = splitFile.insertRegion(null);
    SortedBlock <BTreeKey <K>> sb = new SortedBlock <> ();
    sb.setBlock(new byte[splitFile.getBlockSizeRegion()], indexKeys, true,
            SortedBlock.PTR_NORMAL, (short) 4);
    helper.setLastPointer(sb, rootKey.get().leftNode);
    rootKey.get().leftNode = rootPage;
    sb.insertKey(rootKey);
    rootPage = blockNo;
    helper.putIndexBlock(rootPage, sb);
    leafLevel++;
  }

  // INITIALIZERS:
  private void initBPlusTree(ByteBuffer bb)
          throws IOException, ClassNotFoundException, InstantiationException,
          IllegalAccessException {

    rootPage = bb.getInt();
    leafLevel = bb.getInt();
    size = bb.getInt();

    //Cache Info:
    CBBoolean tmp = new CBBoolean();
    tmp.fromBytes(bb, false);
    useCache = tmp.get();
    tmp.fromBytes(bb, false);
    fullIndex = tmp.get();
    tmp.fromBytes(bb, false);
    readOnly = tmp.get();

  }

  private void openIndex(boolean newIndex, int indexBlockSize,
                         int valueBlockSize) throws IOException {
    if (newIndex) {
      splitFile = new SplitBlockFile(indexName, valueBlockSize, 1024*1024, 1024, 1024, indexBlockSize);
      splitFile.clear();
    } else {
      splitFile = new SplitBlockFile(indexName);
    }
  }

  /**
   * *****************INNER CLASSES****************************************
   */
  static class SBBNo <K extends ByteComparable> {
    SortedBlock <BTreeKey <K>> sb;
    int bNo;
  }

  class BPIterator implements Iterator<KeyValue<K, V>> {

    ArrayList<Integer> blocks = new ArrayList<>();
    Iterator <KeyValue <K,V>> sbIterator;
    int currblock = 0;

    public BPIterator() {
      try {
        helper.buildPointers(rootPage, blocks, 0, leafLevel);
      } catch (IOException e) {
        CoreLog.L().log(Level.WARNING, "could not traverse blocks", e);
      }
      nextIter();
    }

    public BPIterator(K from) {
      this();
      int bNo = searchBlock(from);
      for (; currblock < blocks.size(); currblock++) {
        if (blocks.get(currblock) == bNo)
          break;
      }
      //now we should have found the block we are looking
      nextIter(from);
    }

    @Override
    public boolean hasNext() {
      return sbIterator != null ? sbIterator.hasNext() : false;
    }

    @Override
    public KeyValue<K, V> next() {
      KeyValue<K, V> toRet = null;
      if (sbIterator == null) return null;

      toRet = sbIterator.next();

      if (!sbIterator.hasNext()) {
        nextIter();
      }

      return toRet;
    }

    @Override
    public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException();

    }

    private void nextIter(K from) {
      KeyValue <K,V> search = new KeyValue <> (from, null);
      SortedBlock <KeyValue <K,V>> sb;
      if (currblock >= blocks.size()) {
        sbIterator = null;
        return;
      }
      try {
        sb = helper.getValueBlock(blocks.get(currblock));
        sbIterator = sb.iterator(search);
        currblock++;
        if (sbIterator.hasNext())
          return;
        //advance
        nextIter();
      } catch (IOException e) {
        CoreLog.L().log(Level.WARNING, "Could not retrieve block", e);
      }
    }

    private void nextIter() {
      if (currblock >= blocks.size()) {
        sbIterator = null;
        return;
      }
      try {
        sbIterator = helper.getValueBlock(blocks.get(currblock)).iterator();
        currblock++;
      } catch (IOException e) {
        CoreLog.L().log(Level.WARNING, "Could not retrieve block", e);
      }
    }
  }

}
