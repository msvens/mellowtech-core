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

package org.mellowtech.core.collections.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.bytestorable.CBUtil;
import org.mellowtech.core.bytestorable.io.BCBlock;
import org.mellowtech.core.collections.KeyValue;
import org.mellowtech.core.io.Record;
import org.mellowtech.core.util.MapEntry;

/**
 * Date: 2013-03-16
 * Time: 07:36
 *
 * @author Martin Svensson
 */
@SuppressWarnings("rawtypes")
public class BPlusHelper <A, B extends BComparable<A,B>, C, D extends BStorable <C,D>> {
  
  private final BPTreeImp<A,B,C,D> tree;

  /*RecordFile indexFile, valueFile;
  ByteStorable indexTemplate;
  KeyValue <K,V> valueTemplate;
  */

  public BPlusHelper(BPTreeImp<A,B,C,D> tree){
    this.tree = tree;
    /*this.indexFile = indexFile;
    this.valueFile = valueFile;
    this.indexTemplate = indexTemplate;
    this.valueTemplate = valueTemplate;*/

  }

  public void putValueBlock(int blockNo, BCBlock <KeyValue.KV <B,D>, KeyValue <B,D>> sb)
    throws IOException{
    tree.valueFile.update(blockNo, sb.getBlock());
  }

  public void putIndexBlock(int blockNo, BCBlock <BTreeKey.Entry<B>, BTreeKey <B>> sb) throws IOException{
    tree.indexFile.update(blockNo, sb.getBlock());
  }

  public BCBlock <KeyValue.KV<B,D>, KeyValue<B,D>> toValueBlock(byte[] data){
    return new BCBlock<>(data, tree.keyValues);
  }

  public BCBlock <BTreeKey.Entry<B>, BTreeKey <B>> toIndexBlock(byte[] data){
    return new BCBlock<>(data, tree.indexKeys);
  }

  public BCBlock <KeyValue.KV<B,D>, KeyValue<B,D>> getValueBlock(int blockNo)
    throws IOException{
    try {
      return new BCBlock<>(tree.valueFile.get(blockNo), tree.keyValues);
    }
    catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "could not read block", e);
      return null;
    }
  }

  public BCBlock <BTreeKey.Entry<B>, BTreeKey <B>> getIndexBlock(int blockNo)
          throws IOException{
    try {
      return new BCBlock<>(tree.indexFile.get(blockNo), tree.indexKeys);
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
   * <code>
   *  if(parent.byteSize()+left.getDataBytes() &gt;=
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
  public boolean shiftLeft(BCBlock <BTreeKey.Entry<B>, BTreeKey <B>> left,
      BCBlock <BTreeKey.Entry<B>, BTreeKey <B>> right, BTreeKey <B> parent) {
    // check if we gain anything from a shift, i.e. the minimum shift:
    if (parent.byteSize() + left.getDataBytes() >= right.getDataBytes()
            - right.getFirst().byteSize()) {
      return false;
    }
    // first set parent lefkey to first left key in right and save the old left:
    //BTreeKey <K> oldParent = parent;
    int parentLeft = parent.get().leftNode;
    //int tmp;
    for (;;) {
      parent.get().leftNode = getLastPointer(left);
      left.insertUnsorted(parent);
      BTreeKey <B> newParent = right.delete(0);
      parent.get().leftNode = newParent.get().leftNode;
      parent.get().key = newParent.get().key;
      setLastPointer(left, parent.get().leftNode);
      // now check if to continue:
      if (parent.byteSize() + left.getDataBytes() >= right.getDataBytes()
              - right.getFirst().byteSize())
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
  public boolean shiftRight(BCBlock <BTreeKey.Entry<B>, BTreeKey <B>> left,
                            BCBlock <BTreeKey.Entry<B>, BTreeKey <B>> right, BTreeKey <B> parent) {
    // check if we gain anything from a shift, i.e. the minimum shift:
    if (parent.byteSize() + right.getDataBytes() >= left.getDataBytes()
            - left.getLast().byteSize()) {
      return false;
    }
    // first set parent lefkey to first left key in right and save the old left:
    int parentLeft = parent.get().leftNode;
    for (;;) {
      parent.get().leftNode = getLastPointer(left);
      right.insert(parent);
      BTreeKey <B> newParent = left.delete(left.getNumberOfElements() - 1);
      parent.get().leftNode = newParent.get().leftNode;
      parent.get().key = newParent.get().key;
      setLastPointer(left, parent.get().leftNode);
      // now check if to continue:
      if (parent.byteSize() + right.getDataBytes() >= left.getDataBytes()
              - left.getLast().byteSize())
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
  public int getLastPointer(BCBlock <BTreeKey.Entry<B>, BTreeKey <B>> sb) {
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
  public void setLastPointer(BCBlock <BTreeKey.Entry<B>, BTreeKey<B>> sb, int pointer) {
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
  public void deleteAndReplace(BTreeKey <B> keyIndex, BCBlock <BTreeKey.Entry<B>, BTreeKey <B>> sb) {
    if (keyIndex.compareTo(sb.getLast()) == 0)
      setLastPointer(sb, keyIndex.get().leftNode);
    sb.delete(keyIndex);
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
  public void insertAndReplace(BTreeKey <B> keyIndex, BCBlock <BTreeKey.Entry<B>, BTreeKey <B>> sb) {
    int index = sb.insert(keyIndex);
    int tmp = keyIndex.get().leftNode;
    if (index == sb.getNumberOfElements() - 1) { // last key
      keyIndex.get().leftNode = getLastPointer(sb);
      setLastPointer(sb, tmp);
      sb.update(keyIndex, index);
      return;
    }
    BTreeKey <B> nextKey = sb.get(index + 1);
    keyIndex.get().leftNode = nextKey.get().leftNode;
    nextKey.get().leftNode = tmp;
    sb.update(keyIndex, index);
    sb.update(nextKey, index + 1);
  }



  public void redistributeValueBlocks(BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> small,
                                      BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> large, int bSmall, int bLarge) throws IOException {
    @SuppressWarnings("unchecked")
    BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> blocks[] = (BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>>[]) new BCBlock[2];
    blocks[0] = small;
    blocks[1] = large;
    BCBlock.redistribute(blocks);
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
  @SuppressWarnings("unchecked")
  public BTreeKey <B> generateSeparator(BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> small,
                                        BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> large) {
    BTreeKey <B> nKey = new BTreeKey <> ();
    nKey.get().key = (B) CBUtil.separate(small.getLast().getKey(), large.getFirst().getKey());
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
  @SuppressWarnings("unchecked")
  public BTreeKey <B> generateSeparator(BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> small, KeyValue <B,D> large) {
    BTreeKey <B> nKey = new BTreeKey <>();
    nKey.get().key = (B) CBUtil.separate(small.getLast().getKey(), large.getKey());
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
  public boolean checkUnderflow(BCBlock <?,?> sb) {
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
  public int getPreviousNeighbor(int search, BCBlock <BTreeKey.Entry<B>, BTreeKey<B>> sb) {
    int pos = getPreviousPos(search);
    if (pos == -1)
      return -1;
    return sb.get(pos).get().leftNode;
  }

  /**
   * Postion of the previous key given a search
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
  public int getNextNeighbor(int search, BCBlock <BTreeKey.Entry<B>, BTreeKey<B>> sb) {
    int pos = getNextPos(search);
    if (pos > sb.getNumberOfElements())
      return -1;
    if (pos == sb.getNumberOfElements())
      return getLastPointer(sb);
    return sb.get(pos).get().leftNode;
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
  public int getNode(int search, BCBlock <BTreeKey.Entry<B>, BTreeKey<B>> sb) {
    int pos = getPos(search);
    if (pos == sb.getNumberOfElements())
      return getLastPointer(sb);
    return sb.get(pos).get().leftNode;
  }


  public void extractPointers(List<Integer> list, BCBlock <BTreeKey.Entry<B>, BTreeKey<B>> sb){
    BTreeKey <B> bKey;
    for (int i = 0; i < sb.getNumberOfElements(); i++) {
      bKey = sb.get(i);
      list.add(bKey.get().leftNode);
      if (i + 1 == sb.getNumberOfElements())
        list.add(getLastPointer(sb));
    }
  }

  //PRINTING:
  /**
   * Prints this index whitout the leaf level, i.e it will not print the
   * contents of the key file.
   * @param leafLevel true if level is a leaf
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
      BCBlock <BTreeKey.Entry<B>, BTreeKey<B>> block = getIndexBlock(record);
      sb.append(block+"\n");
    }
  }

  // PRINTING:
  public void buildOutputTree(int pNo, StringBuffer buff, int level, boolean printLeaf)
          throws IOException {
    if (pNo == -1) // no root
      return;

    BCBlock <BTreeKey.Entry<B>, BTreeKey <B>> sb = getIndexBlock(pNo);
    BTreeKey <B> bKey;
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
        bKey = sb.get(i);
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
    Iterator <Record> iter = tree.valueFile.iterator();
    while(iter.hasNext()){
      BCBlock<KeyValue.KV<B,D>, KeyValue<B,D>> block = getValueBlock(iter.next().record);
      sb.append(block+"\n");
    }
  }


  //Logical iteration
  private boolean countValueBlock(int stopBlock, MapEntry<Integer, Integer> entry,
                                  BCBlock <BTreeKey.Entry<B>,BTreeKey<B>> sb) throws IOException{
    boolean foundStop = false;
    BTreeKey <B> bKey;
    for(int i = 0; i < sb.getNumberOfElements(); i++){
      bKey = sb.get(i);
      if(bKey.get().leftNode == stopBlock){
        foundStop = true;
        break;
      }
      BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> valBlock = getValueBlock(bKey.get().leftNode);
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

  private boolean count(int pNo, int level, int leafLevel, MapEntry <Integer,Integer> result, int highBlock)
    throws IOException{
    if(leafLevel == -1) return true;
    if(pNo == -1) return true;
    BTreeKey <B> bKey;
    BCBlock <BTreeKey.Entry<B>, BTreeKey <B>> sb = getIndexBlock(pNo);
    if(level == leafLevel){
      return countValueBlock(highBlock, result, sb);
    }
    else {
      for (int i = 0; i < sb.getNumberOfElements(); i++) {
        bKey = sb.get(i);
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
      ptr.add(tree.valueFile.getFirstRecord());
      return;
    }

    if(pNo == -1) return;
    BTreeKey <B> bKey;
    BCBlock <BTreeKey.Entry<B>, BTreeKey <B>> sb = getIndexBlock(pNo);
    if(level == leafLevel){
      extractPointers(ptr, sb);
    }
    else{
      for (int i = 0; i < sb.getNumberOfElements(); i++) {
        bKey = sb.get(i);
        buildPointers(bKey.get().leftNode, ptr, level + 1, leafLevel);
        if (i + 1 == sb.getNumberOfElements())
          buildPointers(getLastPointer(sb), ptr, level + 1, leafLevel);
      }
    }
  }


}
