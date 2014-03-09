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

import java.io.IOException;
import java.util.logging.Level;

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.collections.KeyValue;
import com.mellowtech.core.disc.SortedBlock;



/**
 * <p>
 * General implementation of the AbstractBPlusTree.java. The ideas for how to
 * implement insertion and deletion is to a large extent based on those
 * discussed in <i>File Structures</i> by Michae J. Folk and Bill Zoellick.
 * </p>
 * <p>
 * This implementation offers a minimal BPlusTree with varaible length keys and
 * values. It uses caching and merging and redistribution when deleting keys.
 * </p>
 * 
 * @author Martin Svensson
 * @version 1.0
 */
@Deprecated
public class BPlusTree extends AbstractBPlusTree {
  /** ******************constructors***************** */
  /**
   * Empty constructor, does nothing. Used if the read/write methods of bytale
   * should be used to bootstrap the BTree.
   * 
   */
  public BPlusTree() {
    super();
  }

  /**
   * Opens an existing <code>BPlusTree</code>. Effectily calls
   * <code>this(fName, true); </code>
   * 
   * @param fName
   *          a <code>String</code> value
   * @exception Exception
   *              if an error occurs
   * @see AbstractBPlusTree#AbstractBPlusTree(String, boolean)
   */
  public BPlusTree(String fName) throws Exception {
    super(fName);
  }

  /**
   * Opens an existing <code>BPlusTree</code>.
   * 
   * @param fName
   *          Name of the BPlusTree to open.
   * @param useOldPaths
   *          true if old paths to index and key file should be used.
   * @exception Exception
   *              if an error occurs
   */
  public BPlusTree(String fName, boolean useOldPaths) throws Exception {
    super(fName, useOldPaths);
  }

  /**
   * Creates a new <code>BPlusTree</code>.
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
  public BPlusTree(String fName, ByteStorable keyType, ByteStorable valueType,
      int valueBlockSize, int indexBlockSize) throws IOException {
    super(fName, keyType, valueType, valueBlockSize, indexBlockSize);
  }

  /**
   * Returns the block number (physical) where a given key should be found.
   * 
   * @param key
   *          the key to search for
   * @return a blocknumber
   * @see AbstractBPlusTree#searchBlock(ByteStorable)
   */
  protected int searchBlock(ByteStorable key) {
    try {
      if (leafLevel == -1)
        return rootPage;
      BTreeKey bTreeKey = new BTreeKey(key, 0);
      return searchBlock(rootPage, bTreeKey, 0);
    }
    catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "could not find block", e);
      return -1;
    }
  }

  private int searchBlock(int pNo, BTreeKey key, int level) throws IOException {
    SortedBlock sb = getBlock(pNo);
    if (level == leafLevel) {
      return getNode(sb.binarySearch(key), sb);
    }
    return searchBlock(getNode(sb.binarySearch(key), sb), key, level + 1);
  }

  /**
   * Inserts a key into this the Btree. If update is set to false the value will
   * not be updated if the key already exists in the tree.
   * 
   * @param key
   *          the key to be inserted
   * @param value
   *          the value to be inserted/updated
   * @param update
   *          if true always update the value if it has changed.
   * @exception IOException
   *              if an error occurs
   */
  public void insert(ByteStorable key, ByteStorable value, boolean update)
      throws IOException {
    KeyValue kv = new KeyValue(key, value);
    if (leafLevel == -1) {
      // no index...to insert directly to value file...in first logical block:
      BPlusReturn ret = insertKeyValue(kv, rootPage, update);
      if (ret != null && ret.action == BPlusReturn.SPLIT) {
        // we now have to value blocks time for index
        ret.promo.leftNode = ret.newBlockNo;
        createRoot(ret.promo);
      }
      return;
    }
    BTreeKey searchKey = new BTreeKey(key, -1);
    BTreeKey rootKey = insert(rootPage, searchKey, kv, 0, update);
    if (rootKey != null)
      createRoot(rootKey);
  }

  private BTreeKey insert(int pNo, BTreeKey key, KeyValue kv, int level,
      boolean update) throws IOException {
    SortedBlock sb = getBlock(pNo);
    BTreeKey keyIndex = null;
    if (level == leafLevel) {
      try {
        BPlusReturn ret = insertKeyValue(kv, getNode(sb.binarySearch(key), sb),
            update);
        if (ret != null) { // this forced a split...
          keyIndex = ret.promo;
          keyIndex.leftNode = ret.newBlockNo;
        }
      }
      catch (Exception e) {
        CoreLog.L().log(Level.SEVERE, pNo+" "+key+" "+kv, e);
        throw new IOException(e);
      }
    }
    else
      keyIndex = insert(getNode(sb.binarySearch(key), sb), key, kv, level + 1,
          update);
    // insert the key into the index and split if necessary:
    if (keyIndex == null)
      return null;
    else
      return insertKey(sb, pNo, keyIndex);
  }

  /**
   * Implemented as defined by AbstractBPlusTree. Deletes a key from this btree.
   * 
   * @param key
   *          the key to delete
   * @return the value associated with the key or null if the key did not
   *         exsist.
   */
  public ByteStorable delete(ByteStorable key) {
    try {
      KeyValue kv = new KeyValue(key, null);
      BPlusReturn ret;
      if (leafLevel == -1) { // no index...to insert directly to value file:
        ret = deleteKeyValue(kv, rootPage, -1, -1);
        if (ret.returnKey != null) {
          return ret.returnKey.getValue();
        }
        else
          return null;
      }
      BTreeKey searchKey = new BTreeKey(key, -1);
      ret = delete(rootPage, -1, -1, searchKey, kv, 0);
      if (ret != null && ret.action == BPlusReturn.SPLIT) { // the underlying
        // root split
        createRoot(ret.promo);
      }

      if (ret.returnKey != null)
        return ret.returnKey.getValue();
      return null;
    }
    catch (IOException e) {
      return null;
    }
  }

  /** ****************PRIVATE STUFF***************************** */
  private BPlusReturn delete(int pNo, int pBlock, int pSearch, BTreeKey key,
      KeyValue kv, int level) throws IOException {
    SortedBlock sb = getBlock(pNo);
    BPlusReturn ret;
    int search = sb.binarySearch(key);
    int node = getNode(search, sb);
    if (level == leafLevel) {
      int left = getPreviousNeighbor(search, sb);
      int right = getNextNeighbor(search, sb);
      ret = deleteKeyValue(kv, node, left, right);
      if (ret == null) {
        return null;
      }
      if (ret.newBlockNo == left)
        ret.keyPos = getPreviousPos(search);
      else
        ret.keyPos = getPos(search);
    }
    else
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
      // this can happen in delete
      BTreeKey promo = insertKey(sb, pNo, ret.promo);
      if (promo != null) {
        ret.action = BPlusReturn.SPLIT;
        ret.promo = promo;
      }
      else
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
   * @param sb
   *          the sorted block conting the separator that should be replace.
   * @param cBlock
   *          the physical address of the block.
   * @param ret
   *          the value returned from the underlying deletion...containing
   *          notably the new separator and position of the key that should be
   *          deleted.
   * @exception IOException
   *              if an error occurs
   */
  protected void handleRedistribute(SortedBlock sb, int cBlock, BPlusReturn ret)
      throws IOException {
    int pos = ret.keyPos;
    BTreeKey changed, next;
    int tmp;
    changed = (BTreeKey) sb.deleteKey(pos);
    // no need to do some more work?
    if (changed.key.byteSize() >= ret.promo.key.byteSize()) {
      changed.key = ret.promo.key;
      sb.insertKey(changed);
      writeBlock(cBlock, sb);
    }
    else { // treat as normal insert...tweak pointers to fit insertKey scheme
      // get next pointer:
      changed.key = ret.promo.key;
      if (pos < sb.getNumberOfElements()) {
        next = (BTreeKey) sb.getKey(pos);
        tmp = next.leftNode;
        next.leftNode = changed.leftNode;
        changed.leftNode = tmp;
        sb.updateKey(next, pos);
      }
      else {
        tmp = changed.leftNode;
        changed.leftNode = getLastPointer(sb);
        setLastPointer(sb, tmp);
      }
      BTreeKey promo = insertKey(sb, cBlock, changed);
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
   * @param sb
   *          Sorted block containing the old separator
   * @param ret
   *          propagate upwards, containg among things the position of the to be
   *          deleted.
   * @param cBlock
   *          the block number for the current sorted block.
   * @param pSearch
   *          the search for the parent key
   * @param pBlock
   *          the block number for the parent block.
   * @exception IOException
   *              if an error occurs
   */
  protected void handleMerge(SortedBlock sb, BPlusReturn ret, int cBlock,
      int pSearch, int pBlock) throws IOException {
    // get position to remove:
    int pos = ret.keyPos;
    if (pos == sb.getNumberOfElements()) {
      pos--;
      setLastPointer(sb, ((BTreeKey) sb.getKey(pos)).leftNode);
      sb.deleteKey(pos);
    }
    else
      sb.deleteKey(pos);
    // no underflow?:
    if (checkMinimum(sb)) {
      writeBlock(cBlock, sb);
      ret.action = BPlusReturn.NONE;
      return;
    }
    // reblance blocks...start with redistribute:
    SortedBlock parent = getBlock(pBlock);
    int leftSib, rightSib;
    SortedBlock sib = null;
    // redistribute:
    leftSib = getPreviousNeighbor(pSearch, parent);
    if (leftSib != -1) {
      sib = getBlock(leftSib);
      if (checkMinimum(sib)) {
        BTreeKey pKey = (BTreeKey) parent.getKey(getPreviousPos(pSearch));
        if (shiftRight(sib, sb, pKey)) {
          writeBlock(leftSib, sib);
          writeBlock(cBlock, sb);
          ret.promo = pKey;
          ret.action = BPlusReturn.REDISTRIBUTE;
          ret.keyPos = getPreviousPos(pSearch);
          return;
        }
      }
    }
    rightSib = getNextNeighbor(pSearch, parent);
    if (rightSib != -1) {
      sib = getBlock(rightSib);
      if (checkMinimum(sib)) {
        BTreeKey pKey = (BTreeKey) parent.getKey(getNextPos(pSearch));
        if (shiftLeft(sb, sib, pKey)) {
          writeBlock(cBlock, sb);
          writeBlock(rightSib, sib);
          ret.promo = pKey;
          ret.action = BPlusReturn.REDISTRIBUTE;
          ret.keyPos = getPos(pSearch);
          return;
        }
      }
    }
    // worst case scenario...merge:
    BTreeKey pKey;
    if (leftSib != -1) {
      sib = getBlock(leftSib);
      pKey = (BTreeKey) parent.getKey(getPreviousPos(pSearch));
      pKey.leftNode = getLastPointer(sib);
      if (sb.canMerge(sib, pKey)) {
        sb.mergeBlock(sib);
        sb.insertKey(pKey);
        indexFile.removeBlock(indexFile.getLogicalBlockNo(leftSib));
        writeBlock(cBlock, sb);
        ret.action = BPlusReturn.MERGE;
        ret.keyPos = getPreviousPos(pSearch);
        return;
      }
    }
    if (rightSib != -1) {
      sib = getBlock(rightSib);
      pKey = (BTreeKey) parent.getKey(getPos(pSearch));
      pKey.leftNode = getLastPointer(sb);
      if (sib.canMerge(sb, pKey)) {
        sib.mergeBlock(sb);
        sib.insertKey(pKey);
        indexFile.removeBlock(indexFile.getLogicalBlockNo(cBlock));
        writeBlock(rightSib, sib);
        ret.action = BPlusReturn.MERGE;
        ret.keyPos = getPos(pSearch);
      }
      return;
    }
    ret.action = BPlusReturn.NONE;
    return;
  }

  private BTreeKey insertKey(SortedBlock sb, int pNo, BTreeKey keyIndex)
      throws IOException {
    if (sb.fitsKey(keyIndex)) {
      insertAndReplace(keyIndex, sb);
      writeBlock(pNo, sb);
      return null;
    }
    indexFile.insertBlock(indexFile.getLastBlockNo());
    SortedBlock sb1 = sb.splitBlock();
    BTreeKey first = (BTreeKey) sb1.getFirstKey();
    setLastPointer(sb, first.leftNode);
    if (keyIndex.compareTo(sb.getLastKey()) < 0) {
      insertAndReplace(keyIndex, sb);
    }
    else
      insertAndReplace(keyIndex, sb1);
    // find the shortest separator:
    BTreeKey promo = (BTreeKey) sb1.getFirstKey();
    deleteAndReplace(promo, sb1);
    writeBlock(pNo, sb);
    promo.leftNode = indexFile.getPhysicalBlockNo(indexFile.getLastBlockNo());
    writeBlock(promo.leftNode, sb1);
    return promo;
  }
}
