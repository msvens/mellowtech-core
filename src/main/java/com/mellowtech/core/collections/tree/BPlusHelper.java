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

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteComparable;
import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.collections.KeyValue;
import com.mellowtech.core.disc.MapEntry;
import com.mellowtech.core.disc.SortedBlock;
import com.mellowtech.core.io.Record;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Date: 2013-03-16
 * Time: 07:36
 *
 * @author Martin Svensson
 */
public class BPlusHelper <K extends ByteComparable,V extends ByteStorable> {
  private final BPTreeImp<K,V> tree;

  /*RecordFile indexFile, valueFile;
  ByteStorable indexTemplate;
  KeyValue <K,V> valueTemplate;
  */

  public BPlusHelper(BPTreeImp<K, V> tree){
    this.tree = tree;
    /*this.indexFile = indexFile;
    this.valueFile = valueFile;
    this.indexTemplate = indexTemplate;
    this.valueTemplate = valueTemplate;*/

  }

  public void putValueBlock(int blockNo, SortedBlock sb)
    throws IOException{
    tree.valueFile.update(blockNo, sb.getBlock());
  }

  public void putIndexBlock(int blockNo, SortedBlock sb) throws IOException{
    tree.indexFile.update(blockNo, sb.getBlock());
  }

  public SortedBlock toValueBlock(byte[] data){
    SortedBlock sb = new SortedBlock();
    sb.setBlock(data, tree.keyValues, false, SortedBlock.PTR_NORMAL, (short) 0);
    return sb;
  }

  public SortedBlock toIndexBlock(byte[] data){
    SortedBlock sb = new SortedBlock();
    sb.setBlock(data, tree.indexKeys, false, SortedBlock.PTR_NORMAL, (short) 0);
    return sb;
  }

  public SortedBlock getValueBlock(int blockNo)
    throws IOException{
    SortedBlock sb = new SortedBlock();
    try {
      sb.setBlock(tree.valueFile.get(blockNo), tree.keyValues, false,
              SortedBlock.PTR_NORMAL, (short) 0);
      return sb;
    }
    catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "could not read block", e);
      return null;
    }
  }

  public SortedBlock getIndexBlock(int blockNo)
          throws IOException{
    SortedBlock sb = new SortedBlock();
    try {
      sb.setBlock(tree.indexFile.get(blockNo), tree.indexKeys, false,
              SortedBlock.PTR_NORMAL, (short) 0);
      return sb;
    }
    catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "could not read block", e);
      return null;
    }
  }

  // MANIPULATING THE INDEX:
  /**
   * Given two blocks this methods moves keys from the right block to the left
   * (starting with moving the parent into the left block, moving the smallest
   * key in the right block to the parent and so on. The method only shifts if
   * it gains anything from it, i.e continously check the current shift.
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
  public static boolean shiftLeft(SortedBlock left, SortedBlock right,
                              BTreeKey parent) {
    // check if we gain anything from a shift, i.e. the minimum shift:
    if (parent.byteSize() + left.getDataBytes() >= right.getDataBytes()
            - right.getFirstKey().byteSize()) {
      return false;
    }
    // first set parent lefkey to first left key in right and save the old left:
    BTreeKey oldParent = parent;
    int parentLeft = parent.leftNode;
    int tmp;
    for (;;) {
      parent.leftNode = getLastPointer(left);
      left.insertKeyUnsorted(parent);
      BTreeKey newParent = (BTreeKey) right.deleteKey(0);
      parent.leftNode = newParent.leftNode;
      parent.key = newParent.key;
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
  public boolean shiftRight(SortedBlock left, SortedBlock right,
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
      BTreeKey newParent = (BTreeKey) left.deleteKey(left.getNumberOfElements() - 1);
      parent.leftNode = newParent.leftNode;
      parent.key = newParent.key;
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
  public static int getLastPointer(SortedBlock sb) {
    ByteBuffer buffer = sb.getByteBuffer();
    return buffer.getInt(sb.getReservedSpaceStart());
  }

  /**
   * Set the last (right-most) pointer in a BTree index block.
   *
   * @param sb
   *          a block of sorted BTree keys.
   * @param pointer
   *          a pointer (i.e block number).
   */
  public static void setLastPointer(SortedBlock sb, int pointer) {
    ByteBuffer buffer = sb.getByteBuffer();
    buffer.putInt(sb.getReservedSpaceStart(), pointer);
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
  public static void deleteAndReplace(BTreeKey keyIndex, SortedBlock sb) {
    if (keyIndex.compareTo(sb.getLastKey()) == 0)
      setLastPointer(sb, keyIndex.leftNode);
    sb.deleteKey(keyIndex);
  }

  /**
   * Insert a key into the BTree index and update the child pointers to reflect
   * the change.
   *
   * @param keyIndex
   *          the key to insert
   * @param sb
   *          a block of sorted BTree keys
   */
  public static void insertAndReplace(BTreeKey keyIndex, SortedBlock sb) {
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



  public void redistributeValueBlocks(SortedBlock small, SortedBlock large, int bSmall,
                            int bLarge) throws IOException {
    SortedBlock blocks[] = new SortedBlock[2];
    blocks[0] = small;
    blocks[1] = large;
    SortedBlock.redistribute(blocks);
    putValueBlock(bSmall, small);
    putValueBlock(bLarge, large);

  }

  //Utility
  // UTILITY:
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
  public BTreeKey generateSeparator(SortedBlock small, SortedBlock large) {
    BTreeKey nKey = new BTreeKey();
    nKey.key = tree.keyValues.getKey().separate(
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
  public BTreeKey generateSeparator(SortedBlock small, KeyValue large) {
    // this should change to use the provided separator function.
    // for now take the small one:
    BTreeKey nKey = new BTreeKey();
    nKey.key = tree.keyValues.getKey().separate(
            ((KeyValue) small.getLastKey()).getKey(), large.getKey());
    return nKey;
  }

  /**
   * Checks if a sorted block contains the minimm amount of information to not
   * be deemed "underflowed".
   *
   * @param sb
   *          a sorted block to check
   * @return true if the block has the minimum amount of information
   */
  public static boolean checkUnderflow(SortedBlock sb) {
    return sb.getDataAndPointersBytes() > (sb.storageCapacity() / 2) ? true
            : false;
  }

  /**
   * Returns the block number just left to the block containing a specific key.
   *
   * @param search
   *          a search for a key.
   * @param sb
   *          the sorted block where to find the neighbor.
   * @return the node (i.e block number) to the left child or -1 if there are no
   *         left child.
   */
  public static int getPreviousNeighbor(int search, SortedBlock sb) {
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
  public static int getPreviousPos(int search) {
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
  public static int getNextPos(int search) {
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
  public static int getNextNeighbor(int search, SortedBlock sb) {
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
  public static int getPos(int search) {
    if (search >= 0)
      return search + 1;
    else
      return Math.abs(search) - 1;
  }

  /**
   * Given a search returns the node to follow in the index to find the given
   * key. At the leafLevel the node is the block number to the right block in the
   * key/value file.
   *
   * @param search
   *          a search for a key
   * @param sb
   *          the block to search
   * @return node
   */
  public static int getNode(int search, SortedBlock sb) {
    int pos = getPos(search);
    if (pos == sb.getNumberOfElements())
      return getLastPointer(sb);
    return ((BTreeKey) sb.getKey(pos)).leftNode;
  }


  public static void extractPointers(List<Integer> list, SortedBlock sb){
    BTreeKey bKey;
    for (int i = 0; i < sb.getNumberOfElements(); i++) {
      bKey = (BTreeKey) sb.getKey(i);
      list.add(bKey.leftNode);
      if (i + 1 == sb.getNumberOfElements())
        list.add(getLastPointer(sb));
    }
  }

  //PRINTING:
  /**
   * Prints this index whitout the leaf level, i.e it will not print the
   * contents of the key file.
   *
   * @return a <code>String</code> value
   */
  public String printIndex(boolean leafLevel) {
    StringBuffer sbuff = new StringBuffer();
    try {
      buildOutputTree(tree.rootPage, sbuff, 0, leafLevel);
    } catch (IOException e) {
      CoreLog.L().warning("Could not traverse index");
    }
    return sbuff.toString();
  }

  public void printIndexBlocks(StringBuilder sb) throws IOException{
    Iterator<Record> iter = tree.indexFile.iterator();
    while(iter.hasNext()){
      int record = iter.next().record;
      sb.append("RECORD: "+record+"\n");
      SortedBlock block = getIndexBlock(record);
      sb.append(block+"\n");
    }
  }

  // PRINTING:
  public void buildOutputTree(int pNo, StringBuffer buff, int level, boolean printLeaf)
          throws IOException {
    if (pNo == -1) // no root
      return;

    SortedBlock sb = getIndexBlock(pNo);
    BTreeKey bKey;
    preTab(level, buff);
    if (level == tree.leafLevel) { // final level
      if (printLeaf) {
        buff.append("\n LeafLevel: physical block:" + pNo + "\n");
        buff.append("rightMostPointer: " + getLastPointer(sb) + "\n");
        buff.append(sb);
      }
    } else {
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

  public void printValueBlocks(StringBuilder sb) throws IOException{
    Iterator <Record> iter = tree.valueFile.iterator();
    while(iter.hasNext()){
      SortedBlock block = getValueBlock(iter.next().record);
      sb.append(block+"\n");
    }
  }


  //Logical iteration
  private boolean countValueBlock(int stopBlock, MapEntry<Integer, Integer> entry,
                                  SortedBlock sb) throws IOException{
    boolean foundStop = false;
    BTreeKey bKey;
    for(int i = 0; i < sb.getNumberOfElements(); i++){
      bKey = (BTreeKey) sb.getKey(i);
      if(bKey.leftNode == stopBlock){
        foundStop = true;
        break;
      }
      SortedBlock valBlock = getValueBlock(bKey.leftNode);
      entry.setValue(entry.getValue() + valBlock.getNumberOfElements());
      entry.setKey(entry.getKey()+1);
      if(i + 1 == sb.getNumberOfElements()){
        int last = getLastPointer(sb);
        if(last == stopBlock){
          foundStop = true;
          break;
        }
        valBlock = getValueBlock(bKey.leftNode);
        entry.setValue(entry.getValue() + valBlock.getNumberOfElements());
        entry.setKey(entry.getKey()+1);
      }
    }
    return foundStop;
  }

  private boolean count(int pNo, int level, int leafLevel, MapEntry result, int highBlock)
    throws IOException{
    if(leafLevel == -1) return true;
    if(pNo == -1) return true;
    BTreeKey bKey;
    SortedBlock sb = getIndexBlock(pNo);
    if(level == leafLevel){
      return countValueBlock(highBlock, result, sb);
    }
    else {
      for (int i = 0; i < sb.getNumberOfElements(); i++) {
        bKey = (BTreeKey) sb.getKey(i);
        if(count(bKey.leftNode, level + 1, leafLevel, result, highBlock))
          return true;

        if (i + 1 == sb.getNumberOfElements())
          return count(getLastPointer(sb), level + 1, leafLevel, result, highBlock);
      }
    }
    return true;
  }

  public Map.Entry <Integer, Integer> countSmaller(int pNo, int level,
                                                   int leafLevel, int stopBlock) throws IOException{
    MapEntry <Integer, Integer> entry = new MapEntry(0,0);
    count(pNo, level, leafLevel, entry, stopBlock);
    return entry;
  }


  public List <Integer> getLogicalBlocks(int rootPage, int leafLevel) throws IOException{
    ArrayList<Integer> toRet = new ArrayList();
    buildPointers(rootPage, toRet, 0, leafLevel);
    return toRet;
  }


  public void buildPointers(int pNo, List <Integer> ptr, int level,
                                   int leafLevel) throws IOException{
    //Special case...no index
    if(leafLevel == -1){
      ptr.add(tree.valueFile.getFirstRecord());
      return;
    }

    if(pNo == -1) return;
    BTreeKey bKey;
    SortedBlock sb = getIndexBlock(pNo);
    if(level == leafLevel){
      BPlusHelper.extractPointers(ptr, sb);
    }
    else{
      for (int i = 0; i < sb.getNumberOfElements(); i++) {
        bKey = (BTreeKey) sb.getKey(i);
        buildPointers(bKey.leftNode, ptr, level + 1, leafLevel);
        if (i + 1 == sb.getNumberOfElements())
          buildPointers(getLastPointer(sb), ptr, level + 1, leafLevel);
      }
    }
  }


}
