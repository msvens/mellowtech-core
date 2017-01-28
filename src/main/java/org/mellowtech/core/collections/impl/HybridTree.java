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
import org.mellowtech.core.io.Record;
import org.mellowtech.core.io.RecordFile;
import org.mellowtech.core.io.RecordFileBuilder;
import org.mellowtech.core.util.MapEntry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author msvens
 * @since 3.0.7
 */
public class HybridTree <A,B> implements BTree<A,B> {

  protected final Path dir;
  protected final String name;
  private final TreeMap<A,Integer> idx;
  private final RecordFile values;
  private final BCodec<A> keyCodec;
  private final BCodec<B> valueCodec;
  private final BCodec<KeyValue<A,B>> kvCodec;

  //private final KeyValue<A, C> keyValues;
  //private final KeyValueCodec<A,C> keyValueStorable;
  private final boolean mapped;
  private Integer rightPtr;
  private long size;

  private long modCount = 0;


  public HybridTree(Path dir, String name,
                    BCodec<A> keyCodec,
                    BCodec<B> valueCodec,
                    RecordFileBuilder valueFileBuilder){
    try {
      this.dir = dir;
      this.name = name;
      this.mapped = valueFileBuilder.isMapped();
      this.values = valueFileBuilder.build(dir.resolve(name+".val"));
      this.keyCodec = keyCodec;
      this.valueCodec = valueCodec;
      this.kvCodec = new KeyValueCodec<A, B>(keyCodec,valueCodec);
      idx = new TreeMap<>();
      rebuildIndex();
    } catch(Exception e){
      throw new Error(e);
    }
  }

  @Override
  public void close() throws IOException {
    values.save();
    values.close();
  }

  @Override
  public boolean containsKey(A key) throws IOException {
    return getKeyValue(key) != null;
  }

  @Override
  public void createTree(Iterator<KeyValue<A,B>> iterator) throws IOException {
    if (!iterator.hasNext()) {
      truncate();
      return;
    }
    values.clear();
    idx.clear();

    HybridTree.Block<A,B> vb = newBlock(mapped);

    int s = 0;
    KeyValue<A,B> tmpKV;

    while(iterator.hasNext()){
      tmpKV = iterator.next();
      if(!vb.sb.fits(tmpKV)){
        updateBlock(vb.bNo, vb.sb);
        A sep = generateSeparator(vb.sb, tmpKV);
        vb = newBlock(mapped);
        int rightNode = vb.bNo;
        addPointer(sep,rightNode);
      }
      s++;
      vb.sb.insertUnsorted(tmpKV);
    }
    size = s;
    updateBlock(vb.bNo, vb.sb);
  }

  @Override
  public void delete() throws IOException {
    modCount++;
    values.remove();
    rightPtr = null;
    idx.clear();
  }

  @Override
  public A getKey(int position) throws IOException {
    if (position < 0 || position >= size())
      throw new IOException("position out of bounds");
    Iterator <Integer> ptrs = blockPointers().iterator();
    while(ptrs.hasNext()){
      Integer bNo = ptrs.next();
      BBuffer<KeyValue<A,B>> sb = getBlock(bNo);
      if (position < sb.getNumberOfElements()) {
        return sb.get(position).getKey();
      }
      position -= sb.getNumberOfElements();
    }
    return null;
  }

  @Override
  public KeyValue<A,B> getKeyValue(A key) throws IOException {
    return getBlock(key).get(new KeyValue<>(key));
  }

  @Override
  public TreePosition getPosition(A key) throws IOException {
    TreePosition tp = getPositionWithMissing(key);
    return tp.exists() ? tp : null;
  }

  @Override
  public TreePosition getPositionWithMissing(A key) throws IOException {
    int block = findBlock(key);
    if(block < 0) return null;
    return getFilePositionNoStrict(key, block);
  }

  @Override
  public boolean isEmpty() throws IOException {
    return size() < 1;
  }

  @Override
  public Iterator<KeyValue<A, B>> iterator(boolean descend, A from, boolean fromInclusive,
                                           A to, boolean toInclusive) {
    return new HybridTreeIterator(descend,from,fromInclusive,to,toInclusive);
  }

  @Override
  public void put(A key, B value) throws IOException {
    put(key,value, true);
  }

  public void put(A key, B value, boolean update) {
    KeyValue<A,B> kv = new KeyValue<>(key,value);
    try {
      Integer bNo = findBlock(kv.getKey());
      BBuffer<KeyValue<A,B>> block = getBlock(bNo);
      modCount++;
      if (block.contains(kv)) {
        if (!update)
          return;
        else {
          block.delete(kv);
          size--;
        }
      }
      size++;
      if (block.fits(kv)) {
        block.insert(kv);
        updateBlock(bNo, block);
        return;
      }
      HybridTree.Block<A,B> vb = newBlock(mapped);
      block.split(vb.sb);
      if (kv.compareTo(block.getLast()) <= 0)
        block.insert(kv);
      else
        vb.sb.insert(kv);
      updateBlock(bNo, block);
      updateBlock(vb.bNo, vb.sb);
      A sep = generateSeparator(block, vb.sb);
      addPointer(sep,vb.bNo);
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  @Override
  public void putIfNotExists(A key, B value){
    put(key,value,false);
  }

  @Override
  public void rebuildIndex() throws IOException, UnsupportedOperationException {
    //just return if there are no value blocks
    if (values.size() == 0) {
      size = 0;
      rightPtr = newBlock(mapped).bNo;
      idx.clear();
      return;
    }
    idx.clear();
    rightPtr = 0;

    BBuffer<KeyValue<A,B>> tmp;
    @SuppressWarnings("unchecked")
    HybridTree.FirstLastKey<A>[] blocks = new HybridTree.FirstLastKey[values.size()];
    Iterator<Record> iter = values.iterator();
    int i = 0;
    int s = 0;
    while (iter.hasNext()) {
      Record r = iter.next();
      tmp = new BBuffer<>(ByteBuffer.wrap(r.data), kvCodec);
      if(tmp.getNumberOfElements() == 0)
        break;
      KeyValue<A,B> first = tmp.getFirst();
      KeyValue<A,B> last = tmp.getLast();
      HybridTree.FirstLastKey<A> sl =
          new HybridTree.FirstLastKey<>(first.getKey(), last.getKey(), r.record);
      blocks[i] = sl;
      i++;
      s += tmp.getNumberOfElements();
    }
    if(blocks[0] == null)
      return;
    //Sort the blocks and set all initial values
    Arrays.sort(blocks);
    size = 0;
    rightPtr = blocks[0].bNo;
    size = s;

    for (i = 0; i < blocks.length - 1; i++) {
      HybridTree.FirstLastKey<A> left = blocks[i];
      HybridTree.FirstLastKey<A> right = blocks[i + 1];

      A sep = generateSeparator(left.last, right.first);
      addPointer(sep, right.bNo);
    }
  }

  @Override
  public B remove(A key) throws IOException {
    try{
      modCount++;
      Map.Entry<A,Integer> entry = entry(key);
      BBuffer<KeyValue<A,B>> block = getBlock(entry.getValue());
      KeyValue<A,B> deleted = block.delete(new KeyValue<>(key));
      if(deleted == null) {
        return null;
      }
      size--;
      //first check if block is not underflowed...
      if(!isUnderflowed(block)){
        //System.out.println("no underflow...just return");
        updateBlock(entry.getValue(), block);
        return deleted.getValue();
      }
      //get left and right sibling
      Map.Entry<A,Integer> leftSib = leftEntry(key);
      Map.Entry<A,Integer> rightSib = rightEntry(key);
      BBuffer<KeyValue<A,B>> left = null;
      BBuffer<KeyValue<A,B>> right = null;

      //try to redistribute
      if(leftSib != null){
        left = getBlock(leftSib.getValue());
        if(!isUnderflowed(left)){
          redistributeBlocks(left,block,leftSib.getValue(),entry.getValue());
          A sep = generateSeparator(left,block);
          replacePointer(leftSib.getKey(),sep);
          return deleted.getValue();
        }
      }
      if(rightSib != null){
        right = getBlock(rightSib.getValue());
        if(!isUnderflowed(right)){
          redistributeBlocks(block,right,entry.getValue(),rightSib.getValue());
          A sep = generateSeparator(block,right);
          replacePointer(entry.getKey(), sep);
          return deleted.getValue();
        }
      }
      //try merge:
      if(left != null && block.fits(left)){
        block.merge(left);
        values.delete(leftSib.getValue());
        updateBlock(entry.getValue(), block);
        deletePointer(leftSib);
        return deleted.getValue();
      }
      if(right != null && right.fits(block)){
        right.merge(block);
        values.delete(entry.getValue());
        updateBlock(rightSib.getValue(), right);
        deletePointer(entry);
        return deleted.getValue();
      }
      return deleted.getValue();
    }catch(IOException e){
      throw new Error(e);
    }

  }

  @Override
  public void save() throws IOException {
    values.save();
  }

  @Override
  public int size() throws IOException {
    return (int) size;
  }

  @Override
  public void truncate() throws IOException {
    modCount++;
    values.clear();
    size = 0;
    rightPtr = newBlock(mapped).bNo;
    idx.clear();
  }


  //Utility methods
  public void printBlocks(){
    Iterator<Integer> iter = blockPointers().iterator();
    while(iter.hasNext()){
      int blockNo = iter.next();
      BBuffer<KeyValue<A,B>> block = getBlock(blockNo);
      KeyValue<A,B> first = block.getFirst();
      KeyValue<A,B> last = block.getLast();
      System.out.println("block: "+blockNo+" "+first.getKey()+":::"+last.getKey());
    }
  }

  public void printTree(){
    Iterator<Map.Entry<A,Integer>> iter = idx.entrySet().iterator();
    StringBuilder sbuilder = new StringBuilder();
    while(iter.hasNext()){
      Map.Entry<A,Integer> e = iter.next();
      sbuilder.append(e.getValue()+"::"+e.getKey()+"::");
    }
    sbuilder.append(rightPtr);
    System.out.println(sbuilder.toString());
  }

  public boolean verifyOrder(){
    Iterator<KeyValue<A,B>> iter = iterator();
    if(iter.hasNext()) {
      KeyValue<A,B> prev = iter.next();
      while (iter.hasNext()) {
        KeyValue<A,B> next = iter.next();
        if (prev.compareTo(next) >= 0) {
          System.out.println("wrong order: " + prev.getKey() + " " + next.getKey());
          return false;
        }
        prev = next;
      }
    }
    return true;
  }

  public boolean verifyReverseOrder(){
    Iterator<KeyValue<A,B>> iter = iterator(true);
    if(iter.hasNext()) {
      KeyValue<A,B> prev = iter.next();
      while (iter.hasNext()) {
        KeyValue<A,B> next = iter.next();
        if (prev.compareTo(next) <= 0) {
          System.out.println("wrong order: " + prev.getKey() + " " + next.getKey());
          return false;
        }
        prev = next;
      }
    }
    return true;
  }

  private void addPointer(A sep, int right){
    if(idx.isEmpty()){
      idx.put(sep, rightPtr);
      this.rightPtr = right;
      return;
    }
    //add right
    if(idx.containsKey(sep)) throw new Error("node already contains key: "+sep);
    Map.Entry<A,Integer> higher = idx.higherEntry(sep);
    if(higher != null){
      idx.put(sep, higher.getValue());
      idx.put(higher.getKey(), right);
    } else {
      idx.put(sep, rightPtr);
      rightPtr = right;
    }
  }

  private Stream<Integer> blockPointers(){
    return Stream.concat(idx.values().stream(), Stream.of(rightPtr));
  }

  private MapEntry<Integer,Integer> countKeyValues(int highBlock){
    int i = 0;
    int cnt = 0;
    Iterator<Integer> all = blockPointers().iterator();
    while(all.hasNext()){
      if(i == highBlock) break;
      i++;
      cnt = cnt + getBlock(all.next()).getNumberOfElements();
    }
    return new MapEntry<>(i,cnt);
  }

  private void deletePointer(Map.Entry<A,Integer> entry){
    if(!idx.containsKey(entry.getKey())) throw new Error("key: "+entry.getKey()+" does not exist");
    idx.remove(entry.getKey());
  }

  private Map.Entry<A,Integer> entry(A key){
    Map.Entry<A,Integer> ptr = idx.higherEntry(key);
    return ptr != null ? new MapEntry<>(ptr.getKey(), ptr.getValue()) : new MapEntry<>(null, rightPtr);
  }

  private Integer findBlock(A key){
    Map.Entry<A,Integer> ptr = idx.higherEntry(key);
    return ptr != null ? ptr.getValue() : rightPtr;
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
  private A generateSeparator(BBuffer<KeyValue<A,B>> small,
                              BBuffer<KeyValue<A,B>> large) {
    return generateSeparator(small.getLast().getKey(), large.getFirst().getKey());
  }

  /**
   * Generates a separator between a block of smaller keys and one larger key.
   *
   * @param small block with smaller keys.
   * @param large the larger value to compare with
   * @return a separator.
   */
  private A generateSeparator(BBuffer<KeyValue<A,B>> small, KeyValue<A,B> large) {
    return generateSeparator(small.getLast().getKey(), large.getKey());
  }

  private A generateSeparator(A small, A large) {
    return CodecUtil.separate(small, large, keyCodec);
  }

  private BBuffer<KeyValue<A,B>> getBlock(A key){
    return getBlock(findBlock(key));
  }

  private BBuffer<KeyValue<A,B>> getBlock(int bno) {
    try {
      return mapped ? new BBuffer<>(values.getMapped(bno), kvCodec) :
          new BBuffer<>(ByteBuffer.wrap(values.get(bno)), kvCodec);
    } catch (IOException e) {
      throw new Error("could not read value block: " + e);
    }
  }

  private TreePosition getFilePositionNoStrict(A key, int bNo)
      throws IOException {
    if (values.size() == 0)
      return null;
    BBuffer<KeyValue<A,B>> sb = getBlock(bNo);
    int smallerInBlock = sb.search(new KeyValue<>(key));
    boolean exists = true;
    if (smallerInBlock < 0) { //not found
      exists = false;
      smallerInBlock = Math.abs(smallerInBlock);
      smallerInBlock--; //readjust
    }
    int elementsInBlock = sb.getNumberOfElements();
    int elements = (int) size;
    Map.Entry<Integer, Integer> cnt = countKeyValues(bNo);
    int smaller = cnt.getValue() + smallerInBlock;
    return new TreePosition(smaller, elements, smallerInBlock, elementsInBlock, exists);
  }

  private boolean isUnderflowed(BBuffer<?> sb) {
    return sb.getDataAndPointersBytes() < (sb.storageCapacity() / 2);
  }

  private Map.Entry<A,Integer> leftEntry(A key){
    return idx.floorEntry(key);
  }

  private HybridTree.Block<A,B> newBlock(boolean mapped) throws IOException {
    int bNo;
    BBuffer<KeyValue<A,B>> buff;
    if (mapped) {
      bNo = values.insert(null);
      buff = new BBuffer<>(values.getMapped(bNo), kvCodec, BBuffer.PtrType.NORMAL);
    } else {
      buff = new BBuffer<>(values.getBlockSize(), kvCodec, BBuffer.PtrType.NORMAL);
      bNo = values.insert(buff.getArray());
    }
    return new HybridTree.Block<>(buff, bNo);
  }

  private void redistributeBlocks(BBuffer<KeyValue<A,B>> small,
                                  BBuffer<KeyValue<A,B>> large,
                                  int bSmall, int bLarge) throws IOException {
    @SuppressWarnings("unchecked")
    BBuffer<KeyValue<A,B>> blocks[] = (BBuffer<KeyValue<A,B>>[]) new BBuffer[2];
    blocks[0] = small;
    blocks[1] = large;
    BBuffer.redistribute(blocks);
    updateBlock(bSmall, small);
    updateBlock(bLarge, large);
  }

  private void replacePointer(A oldSep, A newSep){
    if(!idx.containsKey(oldSep)) throw new Error("oldSep does not exist");
    Integer ptr = idx.remove(oldSep);
    idx.put(newSep, ptr);
  }

  private Map.Entry<A,Integer> rightEntry(A key){
    A higher = idx.higherKey(key);
    return higher != null ? new MapEntry<>(higher, findBlock(higher)) : null;
  }

  private void updateBlock(int blockNo, BBuffer<KeyValue<A,B>> sb)
      throws IOException {
    if (!mapped)
      values.update(blockNo, sb.getArray());
  }

  private static class Block<A,B> {
    BBuffer<KeyValue<A,B>> sb;
    int bNo;

    Block(BBuffer<KeyValue<A,B>> buffer, int blockNo) {
      this.sb = buffer;
      this.bNo = blockNo;
    }
  }

  static class FirstLastKey<A> implements Comparable<HybridTree.FirstLastKey<A>> {
    int bNo;
    A first;
    A last;

    FirstLastKey(A first, A last, int bNo) {
      this.bNo = bNo;
      this.first = first;
      this.last = last;
    }

    @Override
    public int compareTo(HybridTree.FirstLastKey<A> o) {
      return ((Comparable<? super A>)first).compareTo(o.first);
    }

    @Override
    public String toString(){
      return first+" "+last+" "+bNo;
    }
  }

  private class HybridTreeIterator implements Iterator<KeyValue<A,B>> {
    Iterator<KeyValue<A,B>> sbIterator;
    List<Integer> blocks;
    boolean inclusive = true;
    boolean reverse = false;
    boolean endInclusive = true;
    KeyValue<A,B> end = null;
    int currblock = 0;
    KeyValue<A,B> next = null;


    HybridTreeIterator(boolean reverse, A from, boolean inclusive, A to, boolean endInclusive) {
      this.inclusive = inclusive;
      this.reverse = reverse;
      this.end = to == null ? null : new KeyValue<A,B>(to, null);
      this.endInclusive = endInclusive;
      initPtrs();
      setCurrentBlock(from);
      nextIter(from);
      //Hack for now
      if(sbIterator != null && !sbIterator.hasNext()) //can happen when from not included
          nextIter(null);

      getNext();
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public KeyValue<A,B> next() {
      KeyValue<A,B> toRet = next;
      getNext();
      return toRet;
    }

    private boolean checkEnd(KeyValue<A,B> toCheck) {
      if (end == null) return true;
      int cmp = reverse ? end.compareTo(toCheck) : toCheck.compareTo(end);
      return cmp < 0 || (endInclusive && cmp == 0);
    }

    private void getNext() {
      if (sbIterator == null) {
        next = null;
        return;
      }

      KeyValue<A,B> toRet = sbIterator.next();
      if (toRet == null) {
        sbIterator = null;
        next = null;
      } else {
        if (!checkEnd(toRet)) {
          sbIterator = null;
          next = null;
        } else {
          next = toRet;
          if (!sbIterator.hasNext()) {
            nextIter(null);
          }
        }
      }

    }

    private void initPtrs() {
      blocks = blockPointers().collect(Collectors.toList());
    }

    private void nextBlock(A from) {
      if (currblock >= blocks.size())
        sbIterator = null;
      else {
        sbIterator = from == null ?
            getBlock(blocks.get(currblock)).iterator() :
            getBlock(blocks.get(currblock)).iterator(false, new KeyValue<A,B>(from), inclusive, null, false);
        currblock++;
      }
    }

    private void nextIter(A from) {
      if (reverse)
        prevBlock(from);
      else
        nextBlock(from);
    }

    private void prevBlock(A from) {
      if (currblock < 0)
        sbIterator = null;
      else {
        sbIterator = from == null ?
            getBlock(blocks.get(currblock)).iterator(true) :
            getBlock(blocks.get(currblock)).iterator(true, new KeyValue(from), inclusive, null, false);
        currblock--;

      }
    }

    private void setCurrentBlock(A from) {
      if (reverse) {
        this.currblock = blocks.size() - 1;
        if (from != null) {
          int bNo = findBlock(from);
          for (; currblock > 0; currblock--) {
            if (blocks.get(currblock) == bNo)
              break;
          }
        }
      } else {
        this.currblock = 0;
        if (from != null) {
          int bNo = findBlock(from);
          for (; currblock < blocks.size(); currblock++) {
            if (blocks.get(currblock) == bNo)
              break;
          }
        }
      }
    }
  }
}
