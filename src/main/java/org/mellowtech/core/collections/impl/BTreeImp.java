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

import org.mellowtech.core.codec.BBuffer;
import org.mellowtech.core.codec.BCodec;
import org.mellowtech.core.codec.CodecUtil;
import org.mellowtech.core.collections.BTree;
import org.mellowtech.core.collections.KeyValue;
import org.mellowtech.core.collections.TreePosition;
import org.mellowtech.core.io.*;
import org.mellowtech.core.util.MapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.0
 */
@SuppressWarnings("unchecked")
public class BTreeImp<A,B> implements BTree<A,B> {


  private final String IDX_EXT = ".idx";
  private final String VALUE_EXT = ".val";
  private final Logger logger = LoggerFactory.getLogger(BTreeImp.class);
  /**
   * Filename for the IndexFile.
   */
  protected Path dir;
  /**
   * The name of this BPlusTree.
   */
  protected String name;
  /**
   * Block number for the root of the index.
   */
  int rootPage;
  /**
   * The depth of the tree (including the leaf level).
   */
  int leafLevel;
  /**
   * Used for reading and writing key/value pairs to the key file.
   */
  private final BCodec<A> keyCodec;
  private final BCodec<B> valueCodec;
  private final KeyValueCodec<A,B> kvCodec;
  private final BTreeKeyCodec<A> btCodec;

  /**
   * Used for reading and writing keys to the index.
   */
  private int size = 0;

  //How the tree is stored on disc
  private RecordFile idxFile = null;
  private RecordFile valueFile = null;

  //How disc data should be accessed
  private boolean useMappedValue = false;

  public BTreeImp(Path dir, String name, BCodec<A> keyCodec, BCodec<B> valueCodec,
                  int indexBlockSize, int maxIndexBlocks, RecordFileBuilder valueFileBuilder) throws Exception{

    this.keyCodec = keyCodec;
    this.valueCodec = valueCodec;
    this.kvCodec = new KeyValueCodec<>(keyCodec,valueCodec);
    this.btCodec = new BTreeKeyCodec<A>(keyCodec);

    this.dir = dir;
    this.name = name;
    this.useMappedValue = valueFileBuilder.isMapped();
    openTree(indexBlockSize, maxIndexBlocks, valueFileBuilder);

  }

  @Override
  public void close() throws IOException {
    save();
    valueFile.close();
    idxFile.close();
  }

  @Override
  public void compact() throws IOException {
    //valueFile.compact();
    //indexFile.compact();
  }

  @Override
  public boolean containsKey(A key) throws IOException {
    return getKeyValue(key) != null;
  }

  /**
   * Create an index from an iterator of key/value pairs. This method is much
   * faster than doing individual insertions. If you you have a large number
   * of keys from which you want to build an index, the best way is to
   * externally sort them and then calling createIndex.
   *
   * @param iterator a sorted stream of key/value pairs.
   * @throws java.io.IOException if an error occurs
   */
  @Override
  public void createTree(Iterator<KeyValue<A,B>> iterator) throws IOException {
    if (!iterator.hasNext()) {
      truncate();
      return;
    }
    //clear file (cannot use truncate because it creates a block in the file)
    valueFile.clear();
    idxFile.clear();
    leafLevel = -1;

    ValueBlock<A,B> vb = newValueBlock();
    rootPage = vb.bNo;
    int bNo = rootPage;

    KeyValue<A,B> tmpKV;
    @SuppressWarnings("unchecked")
    IdxBlock<A>[] levels = (IdxBlock<A>[]) new IdxBlock<?>[20];
    int s = 0;
    while (iterator.hasNext()) {
      tmpKV = iterator.next();
      if (!vb.sb.fits(tmpKV)) {
        updateValueBlock(vb.bNo, vb.sb);
        BTreeKey<A> sep = generateSeparator(vb.sb, tmpKV);
        //TODO: This must be a bug...should be leftNode = bNo and rightNode the new node
        sep.leftNode = bNo - 1;
        insertSeparator(sep, levels, 0, bNo);
        vb = newValueBlock();
      }
      s++;
      vb.sb.insertUnsorted(tmpKV);
    }
    size = s;
    updateValueBlock(vb.bNo, vb.sb);
    if (levels[0] != null) // we have to write the index levels
      writeIndexBlocks(levels);
  }

  /**
   * Delete this tree on disc
   *
   * @throws IOException if an error occurs
   */
  @Override
  public void delete() throws IOException {
    idxFile.remove();
    valueFile.remove();
    idxFile = null;
    valueFile = null;
  }

  @Override
  public B get(A key) throws IOException {
    KeyValue<A,B> kv = getKeyValue(key);
    return (kv == null) ? null : kv.getValue();
  }

  @Override
  public A getKey(int pos) throws IOException {
    if (pos < 0 || pos >= size)
      throw new IOException("position out of bounds");
    List<Integer> blocks = getLogicalBlocks(rootPage, leafLevel);
    int curr = 0;
    for (; curr < blocks.size(); curr++) {
      int bNo = blocks.get(curr);
      BBuffer<KeyValue<A,B>> sb = getValueBlock(bNo);
      if (pos < sb.getNumberOfElements()) {
        return sb.get(pos).getKey();
      }
      pos -= sb.getNumberOfElements();
    }
    return null;
  }

  @Override
  public KeyValue<A,B> getKeyValue(A key) throws IOException {
    int block = searchBlock(key);
    if (block == -1)
      return null;
    return searchValueFile(key, block);
  }

  @Override
  public TreePosition getPosition(A key) throws IOException {
    int block = searchBlock(key);
    if (block == -1)
      return null;
    return searchValueFilePosition(key, block);
  }

  @Override
  public TreePosition getPositionWithMissing(A key) throws IOException {
    int block = searchBlock(key);
    if (block == -1)
      return null;
    return searchValueFilePositionNoStrict(key, block);
  }

  @Override
  public boolean isEmpty() {
    return size < 1;
  }

  @Override
  public Iterator<KeyValue<A,B>> iterator(boolean descending,
                                          A from, boolean inclusive,
                                          A to, boolean toInclusive) {
    return new BTreeIterator<>(this, descending, from, inclusive, to, toInclusive);
  }

  @Override
  public void put(A key, B value) throws IOException {
    insertUpdate(key, value, true);
  }

  @Override
  public void putIfNotExists(A key, B value) throws IOException {
    insertUpdate(key, value, false);
  }

  @Override
  public void rebuildIndex() throws IOException {
    //just return if there are no value blocks
    if (valueFile.size() == 0) {
      truncate();
      return;
    }

    BBuffer<KeyValue<A,B>> tmp;
    SmallLarge[] blocks = new SmallLarge[valueFile.size()];
    Iterator<Record> iter = valueFile.iterator();
    int i = 0;
    int s = 0;
    while (iter.hasNext()) {
      Record r = iter.next();
      tmp = new BBuffer<>(ByteBuffer.wrap(r.data), kvCodec);
      KeyValue<A,B> first = tmp.getFirst();
      KeyValue<A,B> last = tmp.getLast();
      SmallLarge sl = new SmallLarge<>(first.getKey(), last.getKey(), r.record);
      blocks[i] = sl;
      i++;
      s += tmp.getNumberOfElements();
    }

    //Sort the blocks and set all initial values
    Arrays.sort(blocks);
    leafLevel = -1;
    size = 0;
    rootPage = blocks[0].bNo;
    size = s;
    idxFile.clear();
    IdxBlock<A>[] levels = (IdxBlock<A>[]) new IdxBlock<?>[20];
    for (i = 0; i < blocks.length - 1; i++) {
      SmallLarge<A> left = blocks[i];
      SmallLarge<A> right = blocks[i + 1];
      BTreeKey<A> sep = generateSeparator(left.large, right.small);
      sep.leftNode = left.bNo;
      insertSeparator(sep, levels, 0, right.bNo);
    }
    if (levels[0] != null)
      writeIndexBlocks(levels);
  }

  @Override
  public B remove(A key) {
    try {
      KeyValue<A,B> kv = new KeyValue<>(key);
      BPlusReturn<A,B> ret;
      if (leafLevel == -1) { // no index...delete directly to value file:
        ret = deleteKeyValue(kv, rootPage, -1, -1);
        if (ret == null) return null;
        else if (ret.returnKey != null) {
          size--;
          return ret.returnKey.getValue();
        } else
          return null;
      }
      BTreeKey<A> searchKey = new BTreeKey<>(key, -1);
      ret = delete(rootPage, -1, -1, searchKey, kv, 0);

      if (ret != null && ret.action == BPlusReturn.SPLIT) { // the underlying
        // root split
        createRoot(ret.promo);
      }
      if(ret == null) return null;
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
  public void save() throws IOException {
    setHeader();
    idxFile.save();
    valueFile.save();
  }

  @Override
  public int size() {
    return size;
  }

  public String toString() {
    StringBuffer sbuff = new StringBuffer();
    if (this.isEmpty()) {
      return "empty tree";
    }
    // first print the index:
    try {
      buildOutputTree(rootPage, sbuff, 0, true);
    } catch (IOException e) {
      logger.warn("could not build index tree");
    }
    // then print the keys:
    sbuff.append("\n*****************VALUE FILE***********************\n\n");

    for (Iterator<Record> iter = valueFile.iterator(); iter.hasNext(); ) {
      Record next = iter.next();
      sbuff.append("\n\n");
      sbuff.append("physical block: ").append(next.record);
      sbuff.append("\n").append(toValueBlock(next.data)).append("\n");
    }
    return sbuff.toString();
  }

  @Override
  public void truncate() throws IOException {
    idxFile.clear();
    valueFile.clear();
    leafLevel = -1;
    size = 0;
    rootPage = newValueBlock().bNo;
  }

  protected final BPlusReturn<A,B> delete(int pNo, int pBlock, int pSearch, BTreeKey<A> key,
                                           KeyValue<A,B> kv, int level) throws IOException {
    BBuffer<BTreeKey<A>> sb = getIndexBlock(pNo);
    BPlusReturn<A,B> ret;
    int search = sb.search(key);
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
      BTreeKey<A> promo = insertKey(sb, pNo, ret.promo);
      if (promo != null) {
        ret.action = BPlusReturn.SPLIT;
        ret.promo = promo;
      } else
        ret.action = BPlusReturn.NONE;
      return ret;
    }
    return null;
  }

  /**
   * Prints this index without the leaf level, i.e it will not print the
   * contents of the key file.
   *
   * @param leafLevel true if level is leaf
   * @return a <code>String</code> value
   */
  protected final String printIndex(boolean leafLevel) {
    StringBuffer sbuff = new StringBuffer();
    try {
      buildOutputTree(rootPage, sbuff, 0, leafLevel);
    } catch (IOException e) {
      logger.warn("Could not traverse index");
    }
    return sbuff.toString();
  }

  protected final void printIndexBlocks(StringBuilder sb) throws IOException {
    Iterator<Record> iter = idxFile.iterator();
    while (iter.hasNext()) {
      int record = iter.next().record;
      sb.append("RECORD: ").append(record).append('\n');
      BBuffer<BTreeKey<A>> block = getIndexBlock(record);
      sb.append(block.toString()).append('\n');
    }
  }

  protected final void printValueBlocks(StringBuilder sb) throws IOException {
    Iterator<Record> iter = valueFile.iterator();
    while (iter.hasNext()) {
      BBuffer<KeyValue<A,B>> block = getValueBlock(iter.next().record);
      sb.append(block.toString()).append('\n');
    }
  }

  // PRINTING:
  private void buildOutputTree(int pNo, StringBuffer buff, int level, boolean printLeaf)
      throws IOException {
    if (pNo == -1) // no root
      return;
    BBuffer<BTreeKey<A>> sb = getIndexBlock(pNo);
    BTreeKey<A> bKey;
    preTab(level, buff);
    if (level == leafLevel) { // final level
      if (printLeaf) {
        buff.append("\n LeafLevel: physical block:").append(pNo).append("\n");
        buff.append("rightMostPointer: ").append(getLastPointer(sb)).append("\n");
        buff.append(sb);
      }
    } else {
      buff.append("\n level: ").append(level).append(" physical block ").append(pNo).append("\n");
      buff.append("rightMostPointer: ").append(getLastPointer(sb)).append("\n");
      buff.append(sb);
      for (int i = 0; i < sb.getNumberOfElements(); i++) {
        bKey = sb.get(i);
        buildOutputTree(bKey.leftNode, buff, level + 1, printLeaf);
        if (i + 1 == sb.getNumberOfElements())
          buildOutputTree(getLastPointer(sb), buff, level + 1, printLeaf);
      }
    }
  }

  /**
   * Checks if a sorted block contains the minimm amount of information to not
   * be deemed "underflowed".
   *
   * @param sb a sorted block to check
   * @return true if the block has the minimum amount of information
   */
  private boolean checkUnderflow(BBuffer<?> sb) {
    return sb.getDataAndPointersBytes() > (sb.storageCapacity() / 2);
  }

  /**
   * Remove a key from the root, possibly altering the root pointer.
   *
   * @param ret contians the keyPos for the key that should be deleted.
   * @return ret where actions is set to BPlusReturn.NONE
   * @throws IOException if an error occurs
   */
  private BPlusReturn<A,B> collapseRoot(BPlusReturn<A,B> ret) throws IOException {

    if (leafLevel == -1) {
      return null;
    }
    BBuffer<BTreeKey<A>> sb = getIndexBlock(rootPage);
    if (sb.getNumberOfElements() > 1) {
      int pos = ret.keyPos;
      // this case should not happen...but keep it just in case:
      if (pos == sb.getNumberOfElements()) {
        pos--;
        setLastPointer(sb, sb.get(pos).leftNode);
        sb.delete(pos);
      } else
        sb.delete(pos);
      //updateIndexBlock(rootPage, sb);
      ret.action = BPlusReturn.NONE;
      return ret;
    }
    // we have to collapse the root:
    idxFile.delete(rootPage);

    //very unsure here!!!
    if (leafLevel == 0) { // we just removed the only block we had
      rootPage = valueFile.getFirstRecord();
    } else
      rootPage = getLastPointer(sb);
    leafLevel--;
    ret.action = BPlusReturn.NONE;
    return ret;
  }

  private boolean count(int pNo, int level, int leafLevel, MapEntry<Integer, Integer> result, int highBlock)
      throws IOException {
    if (leafLevel == -1) return true;
    if (pNo == -1) return true;
    BTreeKey<A> bKey;
    BBuffer<BTreeKey<A>> sb = getIndexBlock(pNo);
    if (level == leafLevel) {
      return countValueBlock(highBlock, result, sb);
    } else {
      for (int i = 0; i < sb.getNumberOfElements(); i++) {
        bKey = sb.get(i);
        if (count(bKey.leftNode, level + 1, leafLevel, result, highBlock))
          return true;

        if (i + 1 == sb.getNumberOfElements())
          return count(getLastPointer(sb), level + 1, leafLevel, result, highBlock);
      }
    }
    return true;
  }

  private Map.Entry<Integer, Integer> countSmaller(int pNo, int level,
                                                  int leafLevel, int stopBlock) throws IOException {
    MapEntry<Integer, Integer> entry = new MapEntry<>(0, 0);
    count(pNo, level, leafLevel, entry, stopBlock);
    return entry;
  }

  //Logical iteration
  private boolean countValueBlock(int stopBlock, MapEntry<Integer, Integer> entry,
                                  BBuffer<BTreeKey<A>> sb) throws IOException {
    boolean foundStop = false;
    BTreeKey<A> bKey;
    for (int i = 0; i < sb.getNumberOfElements(); i++) {
      bKey = sb.get(i);
      if (bKey.leftNode == stopBlock) {
        foundStop = true;
        break;
      }
      BBuffer<KeyValue<A,B>> valBlock = getValueBlock(bKey.leftNode);
      entry.setValue(entry.getValue() + valBlock.getNumberOfElements());
      entry.setKey(entry.getKey() + 1);
      if (i + 1 == sb.getNumberOfElements()) {
        int last = getLastPointer(sb);
        if (last == stopBlock) {
          foundStop = true;
          break;
        }
        valBlock = getValueBlock(bKey.leftNode);
        entry.setValue(entry.getValue() + valBlock.getNumberOfElements());
        entry.setKey(entry.getKey() + 1);
      }
    }
    return foundStop;
  }

  /**
   * Create a new root containing one key. The leftKey in the new rootKey will
   * contain the old root pointer.
   *
   * @param rootKey key in the new root.
   * @throws IOException if an error occurs
   */
  private void createRoot(BTreeKey<A> rootKey) throws IOException {
    IdxBlock<A> block = newIdxBlock();
    setLastPointer(block.sb, rootKey.leftNode);
    rootKey.leftNode = rootPage;
    block.sb.insert(rootKey);
    rootPage = block.bNo;
    leafLevel++;
  }

  /**
   * Deletes a key in a BTree block and replace (update) the child pointer to
   * reflect the change.
   *
   * @param keyIndex the key to recplace
   * @param sb       a block of sotred BTree keys.
   */
  private void deleteAndReplace(BTreeKey<A> keyIndex, BBuffer<BTreeKey<A>> sb) {
    if (keyIndex.compareTo(sb.getLast()) == 0)
      setLastPointer(sb, keyIndex.leftNode);
    sb.delete(keyIndex);
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
   * three things (see above).
   * @throws IOException if an error occurs
   */
  private BPlusReturn<A,B> deleteKeyValue(KeyValue<A,B> key, int bNo, int leftNo,
                                                   int rightNo) throws IOException {
    BBuffer<KeyValue<A,B>> sb = getValueBlock(bNo);
    KeyValue<A,B> deletedKey = sb.delete(key);
    if (deletedKey == null) {
      return null;
    }
    if (checkUnderflow(sb)) {
      updateValueBlock(bNo, sb);
      return new BPlusReturn<>(BPlusReturn.NONE, deletedKey, null, -1);
    }
    // reblance...first redistribute:
    BBuffer<KeyValue<A,B>> sib;
    if (leftNo != -1) {
      sib = getValueBlock(leftNo);
      if (checkUnderflow(sib)) {
        redistributeValueBlocks(sib, sb, leftNo, bNo);
        return new BPlusReturn<>(BPlusReturn.REDISTRIBUTE, deletedKey,
            generateSeparator(sib, sb), leftNo);
      }
    }
    if (rightNo != -1) {
      sib = getValueBlock(rightNo);
      if (checkUnderflow(sib)) {
        redistributeValueBlocks(sb, sib, bNo, rightNo);
        return new BPlusReturn<>(BPlusReturn.REDISTRIBUTE, deletedKey,
            generateSeparator(sb, sib), rightNo);
      }
    }

    // rebalance (merge):
    // try left:
    if (leftNo != -1) {
      sib = getValueBlock(leftNo);
      if (sb.fits(sib)) {
        sb.merge(sib);
        valueFile.delete(leftNo);
        updateValueBlock(bNo, sb);
        return new BPlusReturn<>(BPlusReturn.MERGE, deletedKey, null, leftNo);
      }
    }
    if (rightNo != -1) {
      sib = getValueBlock(rightNo);
      if (sib.fits(sb)) {
        sib.merge(sb);
        valueFile.delete(bNo);
        updateValueBlock(rightNo, sib);
        return new BPlusReturn<>(BPlusReturn.MERGE, deletedKey, null, rightNo);
      }
    }
    //default no merge or redistribute
    updateValueBlock(bNo, sb);
    return new BPlusReturn<>(BPlusReturn.NONE, deletedKey, null, -1);
  }

  private void extractPointers(List<Integer> list, BBuffer<BTreeKey<A>> sb) {
    BTreeKey<A> bKey;
    for (int i = 0; i < sb.getNumberOfElements(); i++) {
      bKey = sb.get(i);
      list.add(bKey.leftNode);
      if (i + 1 == sb.getNumberOfElements())
        list.add(getLastPointer(sb));
    }
  }

  /**
   * Generate a new separator between two blocks, i.e the smallest key that
   * would separate a a block with smaller keys and a block with larger keys. If
   * the BPlusTree does not contain a separator the smallest key in the larger
   * block will be returned.
   *
   * @param small a block with smaller keys
   * @param large a block with larger keys
   * @return a separator
   */
  private BTreeKey<A> generateSeparator(BBuffer<KeyValue<A,B>> small,
                                             BBuffer<KeyValue<A,B>> large) {
    return generateSeparator(small.getLast().getKey(), large.getFirst().getKey());
    /*BTreeKeyCodec<A> nKey = new BTreeKeyCodec<>();
    nKey.get().key = CBUtil.separate(small.getLast().get().getKey(), large.getFirst().get().getKey());
    return nKey;*/
  }

  /**
   * Generates a separator between a block of smaller keys and one larger key.
   *
   * @param small block with smaller keys.
   * @param large the larger value to compare with
   * @return a separator.
   */
  private BTreeKey<A> generateSeparator(BBuffer<KeyValue<A,B>> small, KeyValue<A,B> large) {
    return generateSeparator(small.getLast().getKey(), large.getKey());
    /*BTreeKeyCodec<A> nKey = new BTreeKeyCodec<>();
    nKey.get().key = CBUtil.separate(small.getLast().get().getKey(), large.getKey());
    return nKey;*/
  }

  private BTreeKey<A> generateSeparator(A small, A large) {
    BTreeKey<A> nKey = new BTreeKey<>(CodecUtil.separate(small, large, keyCodec),0);
    return nKey;
  }

  private BBuffer<BTreeKey<A>> getIndexBlock(int blockNo)
      throws IOException {
    return getMappedIndex(blockNo);
  }

  /**
   * Returns the last (right most) pointer in a BTree index block.
   *
   * @param sb sorted block of BTree keys.
   * @return the last pointer.
   */
  private int getLastPointer(BBuffer<BTreeKey<A>> sb) {
    return sb.getBlock().getInt(sb.getReservedSpaceStart());
  }

  private List<Integer> getLogicalBlocks(int rootPage, int leafLevel) throws IOException {
    ArrayList<Integer> toRet = new ArrayList<>();
    buildPointers(rootPage, toRet, 0, leafLevel);
    return toRet;
  }

  private BBuffer<BTreeKey<A>> getMappedIndex(int blockNo)
      throws IOException {
    return toIndexBlock(idxFile.getMapped(blockNo));
  }

  private BBuffer<KeyValue<A,B>> getMappedValue(int blockNo)
      throws IOException {
    return toValueBlock(valueFile.getMapped(blockNo));
  }

  /**
   * Returns the block number just right to the block containing a specific key.
   *
   * @param search a search for a key.
   * @param sb     the sorted block where to find the neighbor.
   * @return the node (i.e block number) to the right neighbor.
   */
  private int getNextNeighbor(int search, BBuffer<BTreeKey<A>> sb) {
    int pos = getNextPos(search);
    if (pos > sb.getNumberOfElements())
      return -1;
    if (pos == sb.getNumberOfElements())
      return getLastPointer(sb);
    return sb.get(pos).leftNode;
  }

  /**
   * Returns the position for the next key given a search.
   *
   * @param search a search for a key
   * @return position
   */
  private int getNextPos(int search) {
    int pos = getPos(search);
    return pos + 1;
  }

  /**
   * Given a search returns the node to follow in the index to find the given
   * key. At the leafLevel the node is the block number to the right block in the
   * key/value file.
   *
   * @param search a search for a key
   * @param sb     the block to search
   * @return node
   */
  private int getNode(int search, BBuffer<BTreeKey<A>> sb) {
    int pos = getPos(search);
    if (pos == sb.getNumberOfElements())
      return getLastPointer(sb);
    return sb.get(pos).leftNode;
  }

  /**
   * Given a search returns the position for the key just larger than the key
   * searched for.
   *
   * @param search a search for a key.
   * @return the position for the key just after the key searched for.
   */
  private int getPos(int search) {
    if (search >= 0)
      return search + 1;
    else
      return Math.abs(search) - 1;
  }

  /**
   * Returns the block number just left to the block containing a specific key.
   *
   * @param search a search for a key.
   * @param sb     the sorted block where to find the neighbor.
   * @return the node (i.e block number) to the left child or -1 if there are no
   * left child.
   */
  private int getPreviousNeighbor(int search, BBuffer<BTreeKey<A>> sb) {
    int pos = getPreviousPos(search);
    if (pos == -1)
      return -1;
    return sb.get(pos).leftNode;
  }

  /**
   * Position of the previous key given a search
   *
   * @param search a search for a key
   * @return position
   */
  private int getPreviousPos(int search) {
    int pos = getPos(search);
    return pos - 1;
  }

  private BBuffer<KeyValue<A,B>> getValue(int blockNo)
      throws IOException {
    return toValueBlock(valueFile.get(blockNo));
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
   * @throws IOException if an error occurs
   */
  private void handleMerge(BBuffer<BTreeKey<A>> sb, BPlusReturn<A,B> ret, int cBlock,
                           int pSearch, int pBlock) throws IOException {
    // get position to remove:
    int pos = ret.keyPos;
    if (pos == sb.getNumberOfElements()) {
      pos--;
      setLastPointer(sb, sb.get(pos).leftNode);
      sb.delete(pos);
    } else
      sb.delete(pos);
    // no underflow?:
    if (checkUnderflow(sb)) {
      ret.action = BPlusReturn.NONE;
      return;
    }
    // reblance blocks...start with redistribute:
    BBuffer<BTreeKey<A>> parent = getIndexBlock(pBlock);
    int leftSib, rightSib;
    BBuffer<BTreeKey<A>> sib;
    // redistribute:
    leftSib = getPreviousNeighbor(pSearch, parent);
    if (leftSib != -1) {
      sib = getIndexBlock(leftSib);
      if (checkUnderflow(sib)) {
        BTreeKey<A> pKey = parent.get(getPreviousPos(pSearch));
        //BTreeKeyCodec pKey = (BTreeKeyCodec) parent.getKey(helper.getPos(pSearch));
        if (shiftRight(sib, sb, pKey)) {
          ret.promo = pKey;
          ret.action = BPlusReturn.REDISTRIBUTE;
          ret.keyPos = getPreviousPos(pSearch);
          return;
        }
      }
    }

    rightSib = getNextNeighbor(pSearch, parent);
    if (rightSib != -1) {
      sib = getIndexBlock(rightSib);
      if (checkUnderflow(sib)) {
        BTreeKey<A> pKey = parent.get(getPos(pSearch));
        if (shiftLeft(sb, sib, pKey)) {
          ret.promo = pKey;
          ret.action = BPlusReturn.REDISTRIBUTE;
          ret.keyPos = getPos(pSearch);
          return;
        }
      }
    }
    // worst case scenario...merge:
    BTreeKey<A> pKey;
    if (leftSib != -1) {
      sib = getIndexBlock(leftSib);
      pKey = parent.get(getPreviousPos(pSearch));
      pKey.leftNode = getLastPointer(sib);
      if (sb.fits(sib, pKey)) {
        sb.merge(sib);
        sb.insert(pKey);
        idxFile.delete(leftSib);
        ret.action = BPlusReturn.MERGE;
        ret.keyPos = getPreviousPos(pSearch);
        return;
      }
    }
    if (rightSib != -1) {
      sib = getIndexBlock(rightSib);
      pKey = parent.get(getPos(pSearch));
      pKey.leftNode = getLastPointer(sb);
      if (sib.fits(sb, pKey)) {
        sib.merge(sb);
        sib.insert(pKey);
        idxFile.delete(cBlock);
        ret.action = BPlusReturn.MERGE;
        ret.keyPos = getPos(pSearch);
        return;
      }
    }
    ret.action = BPlusReturn.NONE;
  }

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
   * @throws IOException if an error occurs
   */
  private void handleRedistribute(BBuffer<BTreeKey<A>> sb, int cBlock, BPlusReturn<A,B> ret)
      throws IOException {
    int pos = ret.keyPos;
    BTreeKey<A> changed, next;
    int tmp;
    changed = sb.delete(pos);
    // no need to do some more work?
    if (keyCodec.byteSize(changed.key) >= keyCodec.byteSize(ret.promo.key)) {
      changed.key = ret.promo.key;
      sb.insert(changed);
    } else { // treat as normal insert...tweak pointers to fit insertKey scheme
      changed.key = ret.promo.key;
      if (pos < sb.getNumberOfElements()) {
        next = sb.get(pos);
        tmp = next.leftNode;
        next.leftNode = changed.leftNode;
        changed.leftNode = tmp;
        sb.update(next, pos);
      } else {
        tmp = changed.leftNode;
        changed.leftNode = getLastPointer(sb);
        setLastPointer(sb, tmp);
      }

      BTreeKey<A> promo = insertKey(sb, cBlock, changed);
      if (promo != null) {
        ret.promo = promo;
        ret.action = BPlusReturn.SPLIT;
        return;
      }
    }
    ret.action = BPlusReturn.NONE;
  }

  private Path indexPath() {
    return dir.resolve(name + IDX_EXT);
  }

  private BTreeKey<A> insert(int bNo, BTreeKey<A> key, KeyValue<A,B> kv, int level,
                                  boolean update) throws IOException {
    BBuffer<BTreeKey<A>> sb = getIndexBlock(bNo);
    BTreeKey<A> keyIndex = null;
    if (level == leafLevel) {
      try {
        BPlusReturn<A,B> ret = insertKeyValue(kv, getNode(sb.search(key), sb),
            update);
        if (ret != null) { // this forced a split...
          keyIndex = ret.promo;
          keyIndex.leftNode = ret.newBlockNo;
        }
      } catch (Exception e) {
        logger.error("cannot insert KeyValue into block {} with key {} and keyValue {}",bNo,key,kv);
        throw new IOException(e);
      }
    } else
      keyIndex = insert(getNode(sb.search(key), sb), key, kv, level + 1,
          update);
    // insert the key into the index and split if necessary:
    if (keyIndex == null)
      return null;
    else
      return insertKey(sb, bNo, keyIndex);
  }

  /**
   * Insert a key into the BTree index and update the child pointers to reflect
   * the change.
   *
   * @param keyIndex the key to insert
   * @param sb       a block of sorted BTree keys
   */
  private void insertAndReplace(BTreeKey<A> keyIndex, BBuffer<BTreeKey<A>> sb) {
    int index = sb.insert(keyIndex);
    int tmp = keyIndex.leftNode;
    if (index == sb.getNumberOfElements() - 1) { // last key
      keyIndex.leftNode = getLastPointer(sb);
      setLastPointer(sb, tmp);
      sb.update(keyIndex, index);
      return;
    }
    BTreeKey<A> nextKey = sb.get(index + 1);
    keyIndex.leftNode = nextKey.leftNode;
    nextKey.leftNode = tmp;
    sb.update(keyIndex, index);
    sb.update(nextKey, index + 1);
  }

  private BTreeKey<A> insertKey(BBuffer<BTreeKey<A>> sb, int pNo, BTreeKey<A> keyIndex)
      throws IOException {
    if (sb.fits(keyIndex)) {
      insertAndReplace(keyIndex, sb);
      return null;
    }
    //we need to expand index
    IdxBlock <A> ib = newIdxBlock();
    sb.split(ib.sb);
    BTreeKey<A> first = ib.sb.getFirst();
    setLastPointer(sb, first.leftNode);
    if(keyIndex.compareTo(sb.getLast()) < 0){
      insertAndReplace(keyIndex, sb);
    } else {
      insertAndReplace(keyIndex, ib.sb);
    }
    BTreeKey<A> promo = ib.sb.getFirst();
    deleteAndReplace(promo, ib.sb);
    promo.leftNode = ib.bNo;
    return promo;
  }

  //Manipulating the tree:

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
   * returned.
   * @throws IOException if an error occurs
   */
  private BPlusReturn<A,B> insertKeyValue(KeyValue<A,B> keyValue, int bNo,
                                                   boolean update) throws IOException {
    BBuffer<KeyValue<A,B>> sb;
    //KeyValue<A,B> kvs = new KeyValueCodec<A, C>(keyValue);
    try {
      sb = getValueBlock(bNo);
      if (sb.contains(keyValue)) {
        if (!update)
          return null;
        else {
          sb.delete(keyValue);
          size--;
        }
      }

      size++;

      if (sb.fits(keyValue)) {
        sb.insert(keyValue);
        updateValueBlock(bNo, sb);
        return null;
      }
      ValueBlock vb = newValueBlock();
      sb.split(vb.sb);
      if (keyValue.compareTo(sb.getLast()) <= 0)
        sb.insert(keyValue);
      else
        vb.sb.insert(keyValue);
      updateValueBlock(bNo, sb);
      updateValueBlock(vb.bNo, vb.sb);
      return new BPlusReturn<>(BPlusReturn.SPLIT, keyValue, generateSeparator(sb,
          vb.sb), vb.bNo);
    } catch (Exception e) {
      throw new IOException(e.toString(), e);
    }
  }

  /**
   * Insert a separator into the index when creating index from sorted data. If
   * the current block can not hold the data the block will be split into two
   * and the levels possibly increased.
   */
  private void insertSeparator(BTreeKey<A> sep, IdxBlock<A>[] levels, int current,
                               int rightNode) throws IOException {
    BBuffer<BTreeKey<A>> sb;
    if (levels[current] == null) { // we have to create a new level
      levels[current] = newIdxBlock();
    }
    sb = levels[current].sb;
    if (!sb.fits(sep)) { // save and promote the last key up...
      BTreeKey<A> promo = sb.delete(sb.getNumberOfElements() - 1);
      setLastPointer(sb, promo.leftNode);
      promo.leftNode = levels[current].bNo;

      // create the new block:
      levels[current] = newIdxBlock();
      sb = levels[current].sb;
      // promote the last key in the previous block:
      insertSeparator(promo, levels, current + 1, levels[current].bNo);
    }
    // finally insert the separator:
    setLastPointer(sb, rightNode);
    sb.insertUnsorted(sep);
  }

  private void insertUpdate(A key, B value, boolean update)
      throws IOException {
    KeyValue<A,B> kv = new KeyValue<>(key, value);
    if (leafLevel == -1) {
      // no index...to insert directly to value file...in first logical block:
      BPlusReturn<A,B> ret = insertKeyValue(kv, rootPage, update);
      if (ret != null && ret.action == BPlusReturn.SPLIT) {
        // we now have to value blocks time for index
        ret.promo.leftNode = ret.newBlockNo;
        createRoot(ret.promo);
      }
      return;
    }
    BTreeKey<A> searchKey = new BTreeKey<>(key, -1);
    BTreeKey<A> rootKey = insert(rootPage, searchKey, kv, 0, update);
    if (rootKey != null)
      createRoot(rootKey);
  }

  private IdxBlock<A> newIdxBlock() throws IOException {
    int bNo;
    BBuffer<BTreeKey<A>> buff;
    bNo = idxFile.insert(null);
    buff = new BBuffer<>(idxFile.getMapped(bNo), btCodec, BBuffer.PtrType.NORMAL, (short) 4);
    return new IdxBlock<>(buff, bNo);
  }

  private ValueBlock<A,B> newValueBlock() throws IOException {
    int bNo;
    BBuffer<KeyValue<A,B>> buff;
    if (useMappedValue) {
      bNo = valueFile.insert(null);
      buff = new BBuffer<>(valueFile.getMapped(bNo), kvCodec, BBuffer.PtrType.NORMAL);
    } else {
      buff = new BBuffer<>(valueFile.getBlockSize(), kvCodec, BBuffer.PtrType.NORMAL);
      bNo = valueFile.insert(buff.getArray());
    }
    return new ValueBlock<>(buff, bNo);
  }

  /*private void createTree(int indexBlockSize, int valueBlockSize,
                          int maxIndexBlocks, boolean multiFile,
                          Optional<Integer> maxValueBlocks, Optional<Integer> multiFileSize) throws IOException{
    Path ip = indexPath();
    Path vp = valuePath();
    //Path vp = !oneFileTree ? valuePath() : null;
    if(Files.exists(ip))
      throw new FileAlreadyExistsException(ip.toString());
    if(Files.exists(vp))
      throw new FileAlreadyExistsException(vp.toString());
    RecordFileBuilder sfb = new RecordFileBuilder();
    sfb.blockSize(indexBlockSize).maxBlocks(maxIndexBlocks).reserve(1024).mem();
    idxFile = sfb.build(ip);
    //value file:
    sfb.blockSize(valueBlockSize);
    if(multiFile){
      sfb.multi();
      if(multiFileSize.isPresent()) sfb.multiFileSize(multiFileSize.get());
    } else {
      if(useMappedValue)
        sfb.mem();
      else
        sfb.disc();
      if(maxValueBlocks.isPresent()) sfb.maxBlocks(maxValueBlocks.get());
    }
    valueFile = sfb.build(vp);
    leafLevel = -1;
    rootPage = newValueBlock().bNo;
    //save header:
  }*/

  private void openTree(int indexBlockSize, int maxIndexBlocks, RecordFileBuilder builder) throws IOException{
    Path ip = indexPath();
    Path vp = valuePath();
    RecordFileBuilder idxBuilder = new RecordFileBuilder().blockSize(indexBlockSize).maxBlocks(maxIndexBlocks).reserve(1024).mem();
    if(Files.exists(ip) && Files.exists(vp)){
      idxFile = idxBuilder.build(ip);
      valueFile = builder.build(vp);
      readHeader();
    } else {
      idxFile = idxBuilder.build(ip);
      valueFile = builder.build(vp);
      leafLevel = -1;
      rootPage = newValueBlock().bNo;
    }
  }

  private void preTab(int level, StringBuffer buff) {
    for (int i = 0; i < level; i++)
      buff.append('\t');
  }

  private void readHeader() throws IOException {
    ByteBuffer bb = ByteBuffer.wrap(idxFile.getReserve());
    //now read header:
    rootPage = bb.getInt();
    leafLevel = bb.getInt();
    size = bb.getInt();
  }

  private void redistributeValueBlocks(BBuffer<KeyValue<A,B>> small,
                                               BBuffer<KeyValue<A,B>> large,
                                               int bSmall, int bLarge) throws IOException {
    BBuffer<KeyValue<A,B>> blocks[] = (BBuffer<KeyValue<A,B>>[]) new BBuffer[2];
    blocks[0] = small;
    blocks[1] = large;
    BBuffer.redistribute(blocks);
    updateValueBlock(bSmall, small);
    updateValueBlock(bLarge, large);
  }

  /**
   * Given a key and blockNumber retrieves the key/value pair for the given key.
   *
   * @param key the key to search for
   * @param bNo the physical blocknumber in the key/value file.
   * @return the Key/value pair or null if the key did not exist
   * @throws IOException if an error occurs
   */
  private KeyValue<A,B> searchValueFile(A key, int bNo)
      throws IOException {
    if (valueFile.size() == 0)
      return null;
    BBuffer<KeyValue<A,B>> sb = getValueBlock(bNo);
    return sb.get(new KeyValue<>(key));
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
  private TreePosition searchValueFilePosition(A key, int bNo)
      throws IOException {
    if (valueFile.size() == 0)
      return null;
    BBuffer<KeyValue<A,B>> sb = getValueBlock(bNo);
    int smallerInBlock = sb.search(new KeyValue<A, B>(key));
    if (smallerInBlock < 0) return null;
    int elements = size;
    int elementsInBlock = sb.getNumberOfElements();
    Map.Entry<Integer, Integer> cnt = countSmaller(rootPage, 0, leafLevel, bNo);
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
  private TreePosition searchValueFilePositionNoStrict(A key, int bNo)
      throws IOException {
    if (valueFile.size() == 0)
      return null;
    BBuffer<KeyValue<A,B>> sb = getValueBlock(bNo);
    int smallerInBlock = sb.search(new KeyValue<A,B>(key));
    boolean exists = true;
    if (smallerInBlock < 0) { //not found
      exists = false;
      smallerInBlock = Math.abs(smallerInBlock);
      smallerInBlock--; //readjust
    }
    int elementsInBlock = sb.getNumberOfElements();
    int elements = size;
    Map.Entry<Integer, Integer> cnt = countSmaller(rootPage, 0, leafLevel, bNo);
    int smaller = cnt.getValue() + smallerInBlock;
    return new TreePosition(smaller, elements, smallerInBlock, elementsInBlock, exists);
  }

  private void setHeader() throws IOException {
    //save index info:
    ByteBuffer bb = ByteBuffer.allocate(20);
    bb.putInt(rootPage);
    bb.putInt(leafLevel);
    bb.putInt(size);
    idxFile.setReserve(bb.array());
  }

  /**
   * Set the last (right-most) pointer in a BTree index block.
   *
   * @param sb      a block of sorted BTree keys.
   * @param pointer a pointer (i.e block number).
   */
  private void setLastPointer(BBuffer<BTreeKey<A>> sb, int pointer) {
    sb.getBlock().putInt(sb.getReservedSpaceStart(), pointer);
  }

  /**
   * Given two blocks this methods moves keys from the right block to the left
   * (starting with moving the parent into the left block, moving the smallest
   * key in the right block to the parent and so on. The method only shifts if
   * it gains anything from it, i.e continously check the current shift.
   * <code>if(parent.byteSize()+left.getDataBytes() &gt;=
   * right.getDataBytes() - right.getFirstKey().byteSize()){
   * break;
   * }
   * </code>
   * Shifting is needed when balancing blocks in the BTree index.
   *
   * @param left   block with smaller values.
   * @param right  block with larger values.
   * @param parent the parent node in the index.
   * @return true if at least one key was shifted
   */
  private boolean shiftLeft(BBuffer<BTreeKey<A>> left,
                                    BBuffer<BTreeKey<A>> right,
                                    BTreeKey<A> parent) {
    // check if we gain anything from a shift, i.e. the minimum shift:
    if (btCodec.byteSize(parent) + left.getDataBytes() >= right.getDataBytes()
        - btCodec.byteSize(right.getFirst())) {
      return false;
    }
    // first set parent lefkey to first left key in right and save the old left:
    int parentLeft = parent.leftNode;
    for (; ; ) {
      parent.leftNode = getLastPointer(left);
      left.insertUnsorted(parent);
      BTreeKey<A> newParent = right.delete(0);
      parent.leftNode = newParent.leftNode;
      parent.key = newParent.key;
      setLastPointer(left, parent.leftNode);
      // now check if to continue:
      if (btCodec.byteSize(parent) + left.getDataBytes() >= right.getDataBytes()
          - btCodec.byteSize(right.getFirst()))
        break;
    }
    parent.leftNode = parentLeft;
    return true;
  }

  /**
   * The same as for shiftLeft but in the other direction
   *
   * @param left   block with smaller values
   * @param right  block with higher values
   * @param parent the parent node in the index
   * @return true if at least one key was shifted
   * @see #shiftLeft
   */
  private boolean shiftRight(BBuffer<BTreeKey<A>> left,
                                     BBuffer<BTreeKey<A>> right,
                                     BTreeKey<A> parent) {
    // check if we gain anything from a shift, i.e. the minimum shift:
    if (btCodec.byteSize(parent) + right.getDataBytes() >= left.getDataBytes()
        - btCodec.byteSize(left.getLast())) {
      return false;
    }
    // first set parent lefkey to first left key in right and save the old left:
    int parentLeft = parent.leftNode;
    for (; ; ) {
      parent.leftNode = getLastPointer(left);
      right.insert(parent);
      BTreeKey<A> newParent = left.delete(left.getNumberOfElements() - 1);
      parent.leftNode = newParent.leftNode;
      parent.key = newParent.key;
      setLastPointer(left, parent.leftNode);
      // now check if to continue:
      if (btCodec.byteSize(parent) + right.getDataBytes() >= left.getDataBytes()
          - btCodec.byteSize(left.getLast()))
        break;
    }
    parent.leftNode = parentLeft;
    return true;
  }

  private BBuffer<BTreeKey<A>> toIndexBlock(ByteBuffer data) {
    return new BBuffer<>(data, btCodec);
  }

  /*private BBuffer<BTreeKeyCodec.Entry<B>, BTreeKeyCodec<B>> toIndexBlock(byte[] data) {
    return toIndexBlock(ByteBuffer.wrap(data));
  }*/

  private BBuffer<KeyValue<A,B>> toValueBlock(byte[] data) {
    return toValueBlock(ByteBuffer.wrap(data));
  }

  private BBuffer<KeyValue<A,B>> toValueBlock(ByteBuffer data) {
    return new BBuffer<>(data, kvCodec);
  }

  private void updateValueBlock(int blockNo, BBuffer<KeyValue<A,B>> sb)
      throws IOException {
    if (!useMappedValue)
      valueFile.update(blockNo, sb.getArray());
  }

  private Path valuePath() {
    return dir.resolve(name + VALUE_EXT);
  }

  /**
   * CreateIndex calls this method to write all created index blocks to file.
   */
  private void writeIndexBlocks(IdxBlock<A>[] levels) throws IOException {
    //boolean removeLast = false;
    int rPage = 0;
    int i = 0;

    for (; i < levels.length; i++) {
      if (levels[i] == null)
        break;
      rPage = levels[i].bNo;
      //updateIndexBlock(rPage, levels[i].sb);
    }
    leafLevel = i - 1;
    rootPage = rPage;
  }

  final void buildPointers(int pNo, List<Integer> ptr, int level,
                                     int leafLevel) throws IOException {
    //Special case...no index
    if (leafLevel == -1) {
      ptr.add(rootPage);
      return;
    }

    if (pNo == -1) return;
    BTreeKey<A> bKey;
    BBuffer<BTreeKey<A>> sb = getIndexBlock(pNo);
    if (level == leafLevel) {
      extractPointers(ptr, sb);
    } else {
      for (int i = 0; i < sb.getNumberOfElements(); i++) {
        bKey = sb.get(i);
        buildPointers(bKey.leftNode, ptr, level + 1, leafLevel);
        if (i + 1 == sb.getNumberOfElements())
          buildPointers(getLastPointer(sb), ptr, level + 1, leafLevel);
      }
    }
  }

  final BBuffer<KeyValue<A,B>> getValueBlock(int blockNo)
      throws IOException {
    return useMappedValue ? getMappedValue(blockNo) : getValue(blockNo);
  }

  final int searchBlock(A key) {
    try {
      if (leafLevel == -1)
        return rootPage;
      BTreeKey<A> bTreeKey = new BTreeKey<>(key, 0);
      return searchBlock(rootPage, bTreeKey, 0);
    } catch (IOException e) {
      logger.warn("could not find block", e);
      return -1;
    }
  }

  private int searchBlock(int bNo, BTreeKey<A> key, int level) throws IOException {
    BBuffer<BTreeKey<A>> sb = getIndexBlock(bNo);
    if (level == leafLevel) {
      return getNode(sb.search(key), sb);
    }
    return searchBlock(getNode(sb.search(key), sb), key, level + 1);
  }

  private static class IdxBlock<A> {
    BBuffer<BTreeKey<A>> sb;
    int bNo;

    IdxBlock(BBuffer<BTreeKey<A>> buffer, int blockNo) {
      sb = buffer;
      bNo = blockNo;
    }
  }

  private static class ValueBlock<A,B> {
    BBuffer<KeyValue<A,B>> sb;
    int bNo;

    ValueBlock(BBuffer<KeyValue<A,B>> buffer, int blockNo) {
      this.sb = buffer;
      this.bNo = blockNo;
    }
  }

  static class SmallLarge<A> implements Comparable<SmallLarge<A>> {
    int bNo;
    A small;
    A large;

    SmallLarge(A small, A large, int bNo) {
      this.bNo = bNo;
      this.small = small;
      this.large = large;
    }

    @Override
    public int compareTo(SmallLarge<A> o) {
      return ((Comparable<? super A>)small).compareTo(o.small);
    }
  }


}
