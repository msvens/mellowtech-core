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
import org.mellowtech.core.bytestorable.io.SortedBlock;
import org.mellowtech.core.collections.KeyValue;
import org.mellowtech.core.disc.MapEntry;
import org.mellowtech.core.io.Record;

/**
 * Date: 2013-03-16
 * Time: 07:36
 *
 * @author Martin Svensson
 */
public class MemMappedBPlusHelper<K extends ByteComparable,V extends ByteStorable> {
  private final MemMappedBPTreeImp<K,V> tree;

  public MemMappedBPlusHelper(MemMappedBPTreeImp<K, V> tree){
    this.tree = tree;
  }

  public void putValueBlock(int blockNo, SortedBlock <KeyValue <K,V>> sb)
    throws IOException{
    tree.splitFile.update(blockNo, sb.getBlock());
  }

  public void putIndexBlock(int blockNo, SortedBlock <BTreeKey <K>> sb) throws IOException{
    tree.splitFile.updateRegion(blockNo, sb.getBlock());
  }

  public SortedBlock <KeyValue <K,V>> toValueBlock(byte[] data){
    SortedBlock <KeyValue <K,V>> sb = new SortedBlock <> ();
    sb.setBlock(data, tree.keyValues, false, SortedBlock.PTR_NORMAL, (short) 0);
    return sb;
  }

  public SortedBlock <BTreeKey <K>> toIndexBlock(byte[] data){
    SortedBlock <BTreeKey <K>> sb = new SortedBlock <> ();
    sb.setBlock(data, tree.indexKeys, false, SortedBlock.PTR_NORMAL, (short) 0);
    return sb;
  }

  public SortedBlock <KeyValue <K,V>> getValueBlock(int blockNo)
    throws IOException{
    SortedBlock <KeyValue <K,V>> sb = new SortedBlock <>();
    try {
      sb.setBlock(tree.splitFile.get(blockNo), tree.keyValues, false,
              SortedBlock.PTR_NORMAL, (short) 0);
      return sb;
    }
    catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "could not read block", e);
      return null;
    }
  }

  public SortedBlock <BTreeKey <K>> getIndexBlock(int blockNo)
          throws IOException{
    SortedBlock <BTreeKey <K>> sb = new SortedBlock <>();
    try {
      sb.setBlock(tree.splitFile.getRegion(blockNo), tree.indexKeys, false,
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
  public boolean shiftLeft(SortedBlock <BTreeKey <K>> left, SortedBlock <BTreeKey <K>> right,
                              BTreeKey <K> parent) {
    // check if we gain anything from a shift, i.e. the minimum shift:
    if (parent.byteSize() + left.getDataBytes() >= right.getDataBytes()
            - right.getFirstKey().byteSize()) {
      return false;
    }
    // first set parent lefkey to first left key in right and save the old left:
    //BTreeKey <K> oldParent = parent;
    int parentLeft = parent.get().leftNode;
    //int tmp;
    for (;;) {
      parent.get().leftNode = getLastPointer(left);
      left.insertKeyUnsorted(parent);
      BTreeKey <K> newParent = right.deleteKey(0);
      parent.get().leftNode = newParent.get().leftNode;
      parent.get().key = newParent.get().key;
      setLastPointer(left, parent.get().leftNode);
      // now check if to continue:
      if (parent.byteSize() + left.getDataBytes() >= right.getDataBytes()
              - right.getFirstKey().byteSize())
        break;
    }
    parent.get().leftNode = parentLeft;
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
  public boolean shiftRight(SortedBlock <BTreeKey <K>> left, SortedBlock <BTreeKey <K>> right,
                               BTreeKey <K> parent) {
    // check if we gain anything from a shift, i.e. the minimum shift:
    if (parent.byteSize() + right.getDataBytes() >= left.getDataBytes()
            - left.getLastKey().byteSize()) {
      return false;
    }
    // first set parent lefkey to first left key in right and save the old left:
    int parentLeft = parent.get().leftNode;
    for (;;) {
      parent.get().leftNode = getLastPointer(left);
      right.insertKey(parent);
      BTreeKey <K> newParent = left.deleteKey(left.getNumberOfElements() - 1);
      parent.get().leftNode = newParent.get().leftNode;
      parent.get().key = newParent.get().key;
      setLastPointer(left, parent.get().leftNode);
      // now check if to continue:
      if (parent.byteSize() + right.getDataBytes() >= left.getDataBytes()
              - left.getLastKey().byteSize())
        break;
    }
    parent.get().leftNode = parentLeft;
    return true;
  }

  /**
   * Returns the last (right most) pointer in a BTree index block.
   *
   * @param sb
   *          sorted block of BTree keys.
   * @return the last pointer.
   */
  public int getLastPointer(SortedBlock <BTreeKey <K>> sb) {
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
  public void setLastPointer(SortedBlock <BTreeKey <K>> sb, int pointer) {
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
  public void deleteAndReplace(BTreeKey <K> keyIndex, SortedBlock <BTreeKey<K>> sb) {
    if (keyIndex.compareTo(sb.getLastKey()) == 0)
      setLastPointer(sb, keyIndex.get().leftNode);
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
  public void insertAndReplace(BTreeKey <K> keyIndex, SortedBlock <BTreeKey <K>> sb) {
    int index = sb.insertKey(keyIndex);
    int tmp = keyIndex.get().leftNode;
    if (index == sb.getNumberOfElements() - 1) { // last key
      keyIndex.get().leftNode = getLastPointer(sb);
      setLastPointer(sb, tmp);
      sb.updateKey(keyIndex, index);
      return;
    }
    BTreeKey <K> nextKey = sb.getKey(index + 1);
    keyIndex.get().leftNode = nextKey.get().leftNode;
    nextKey.get().leftNode = tmp;
    sb.updateKey(keyIndex, index);
    sb.updateKey(nextKey, index + 1);
  }



  public void redistributeValueBlocks(SortedBlock <KeyValue <K,V>> small, SortedBlock <KeyValue <K,V>> large, 
      int bSmall, int bLarge) throws IOException {
    SortedBlock <KeyValue <K,V>> blocks[] = (SortedBlock <KeyValue <K,V>>[]) new SortedBlock[2];
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
  public BTreeKey <K> generateSeparator(SortedBlock <KeyValue <K,V>> small, 
      SortedBlock <KeyValue <K,V>> large) {
    BTreeKey <K> nKey = new BTreeKey <> ();
    nKey.get().key = (K) tree.keyValues.getKey().separate(
            small.getLastKey().get().key,
            large.getFirstKey().get().key);
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
  public BTreeKey <K> generateSeparator(SortedBlock <KeyValue <K,V>> small, KeyValue <K,V> large) {
    // this should change to use the provided separator function.
    // for now take the small one:
    BTreeKey <K> nKey = new BTreeKey <> ();
    nKey.get().key = (K) tree.keyValues.getKey().separate(
            small.getLastKey().getKey(), large.getKey());
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
  public boolean checkUnderflow(SortedBlock <?> sb) {
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
  public int getPreviousNeighbor(int search, SortedBlock <BTreeKey <K>> sb) {
    int pos = getPreviousPos(search);
    if (pos == -1)
      return -1;
    return sb.getKey(pos).get().leftNode;
  }

  /**
   * Position of the previous key given a search
   *
   * @param search
   *          a search for a key
   * @return position
   */
  public int getPreviousPos(int search) {
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
  public int getNextPos(int search) {
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
  public int getNextNeighbor(int search, SortedBlock <BTreeKey <K>> sb) {
    int pos = getNextPos(search);
    if (pos > sb.getNumberOfElements())
      return -1;
    if (pos == sb.getNumberOfElements())
      return getLastPointer(sb);
    return sb.getKey(pos).get().leftNode;
  }

  /**
   * Given a search returns the position for the key just larger than the key
   * searched for.
   *
   * @param search
   *          a search for a key.
   * @return the position for the key just after the key searched for.
   */
  public int getPos(int search) {
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
  public int getNode(int search, SortedBlock <BTreeKey <K>> sb) {
    int pos = getPos(search);
    if (pos == sb.getNumberOfElements())
      return getLastPointer(sb);
    return sb.getKey(pos).get().leftNode;
  }


  public void extractPointers(List<Integer> list, SortedBlock <BTreeKey <K>> sb){
    BTreeKey <K> bKey;
    for (int i = 0; i < sb.getNumberOfElements(); i++) {
      bKey = sb.getKey(i);
      list.add(bKey.get().leftNode);
      if (i + 1 == sb.getNumberOfElements())
        list.add(getLastPointer(sb));
    }
  }

  //PRINTING:
  /**
   * Prints this index without the leaf level, i.e it will not print the
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
    Iterator<Record> iter = tree.splitFile.iteratorRegion();
    while(iter.hasNext()){
      int record = iter.next().record;
      sb.append("RECORD: "+record+'\n');
      SortedBlock <BTreeKey <K>> block = getIndexBlock(record);
      sb.append(block.toString()+'\n');
    }
  }

  // PRINTING:
  public void buildOutputTree(int pNo, StringBuffer buff, int level, boolean printLeaf)
          throws IOException {
    if (pNo == -1) // no root
      return;
    SortedBlock <BTreeKey <K>> sb = getIndexBlock(pNo);
    BTreeKey <K> bKey;
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
        bKey = sb.getKey(i);
        buildOutputTree(bKey.get().leftNode, buff, level + 1, printLeaf);
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
    Iterator <Record> iter = tree.splitFile.iterator();
    while(iter.hasNext()){
      SortedBlock <KeyValue <K,V>> block = getValueBlock(iter.next().record);
      sb.append(block.toString()+'\n');
    }
  }


  //Logical iteration
  private boolean countValueBlock(int stopBlock, MapEntry<Integer, Integer> entry,
                                  SortedBlock <BTreeKey <K>> sb) throws IOException{
    boolean foundStop = false;
    BTreeKey <K> bKey;
    for(int i = 0; i < sb.getNumberOfElements(); i++){
      bKey = sb.getKey(i);
      if(bKey.get().leftNode == stopBlock){
        foundStop = true;
        break;
      }
      SortedBlock <KeyValue <K,V>> valBlock = getValueBlock(bKey.get().leftNode);
      entry.setValue(entry.getValue() + valBlock.getNumberOfElements());
      entry.setKey(entry.getKey()+1);
      if(i + 1 == sb.getNumberOfElements()){
        int last = getLastPointer(sb);
        if(last == stopBlock){
          foundStop = true;
          break;
        }
        valBlock = getValueBlock(bKey.get().leftNode);
        entry.setValue(entry.getValue() + valBlock.getNumberOfElements());
        entry.setKey(entry.getKey()+1);
      }
    }
    return foundStop;
  }

  private boolean count(int pNo, int level, int leafLevel, MapEntry <Integer, Integer> result, int highBlock)
    throws IOException{
    if(leafLevel == -1) return true;
    if(pNo == -1) return true;
    BTreeKey <K> bKey;
    SortedBlock <BTreeKey <K>> sb = getIndexBlock(pNo);
    if(level == leafLevel){
      return countValueBlock(highBlock, result, sb);
    }
    else {
      for (int i = 0; i < sb.getNumberOfElements(); i++) {
        bKey = sb.getKey(i);
        if(count(bKey.get().leftNode, level + 1, leafLevel, result, highBlock))
          return true;

        if (i + 1 == sb.getNumberOfElements())
          return count(getLastPointer(sb), level + 1, leafLevel, result, highBlock);
      }
    }
    return true;
  }

  public Map.Entry <Integer, Integer> countSmaller(int pNo, int level,
                                                   int leafLevel, int stopBlock) throws IOException{
    MapEntry <Integer, Integer> entry = new MapEntry <> (0,0);
    count(pNo, level, leafLevel, entry, stopBlock);
    return entry;
  }


  public List <Integer> getLogicalBlocks(int rootPage, int leafLevel) throws IOException{
    ArrayList<Integer> toRet = new ArrayList <> ();
    buildPointers(rootPage, toRet, 0, leafLevel);
    return toRet;
  }


  public void buildPointers(int pNo, List <Integer> ptr, int level,
                                   int leafLevel) throws IOException{
    //Special case...no index
    if(leafLevel == -1){
      ptr.add(tree.splitFile.getFirstRecord());
      return;
    }

    if(pNo == -1) return;
    BTreeKey <K> bKey;
    SortedBlock <BTreeKey <K>> sb = getIndexBlock(pNo);
    if(level == leafLevel){
      extractPointers(ptr, sb);
    }
    else{
      for (int i = 0; i < sb.getNumberOfElements(); i++) {
        bKey = sb.getKey(i);
        buildPointers(bKey.get().leftNode, ptr, level + 1, leafLevel);
        if (i + 1 == sb.getNumberOfElements())
          buildPointers(getLastPointer(sb), ptr, level + 1, leafLevel);
      }
    }
  }


}
