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
import org.mellowtech.core.io.compress.BlockPointer;
import org.mellowtech.core.io.compress.CFileBuilder;
import org.mellowtech.core.io.compress.CFileReader;
import org.mellowtech.core.io.compress.CFileWriter;
import org.mellowtech.core.util.MapEntry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.7
 */
public class ReadOnlyHybridTree<A,B> implements BTree<A,B> {

  private static final String FILE_EXT = ".cval";

  private final TreeMap<A,BlockPointer> idx;
  private final CFileReader values;
  private final KeyValueCodec<A,B> kvCodec;
  private BlockPointer rightPtr;
  private long size;
  private BlockPointer currentPtr;
  private BBuffer<KeyValue<A,B>> currentBlock;


  public static <A,B> ReadOnlyHybridTree<A,B> createReadOnlyTree(BTree<A,B> tree, Path dir, String name, BCodec<A> keyCodec,
                                                                 BCodec<B> valueCodec, CFileBuilder builder) throws IOException{

    //Create compressed file
    KeyValueCodec<A,B> codec = new KeyValueCodec<>(keyCodec,valueCodec);
    Iterator<KeyValue<A,B>> iter = tree.iterator();
    BBuffer<KeyValue<A,B>> buffer = new BBuffer<>(ByteBuffer.allocateDirect(builder.blockSize()), codec, BBuffer.PtrType.BIG);
    Path file = dir.resolve(name+FILE_EXT);
    CFileWriter writer = builder.path(file).writer();

    while(iter.hasNext()){
      KeyValue<A,B> kv = iter.next();
      if(!buffer.fits(kv)){ //flush current buffer
        writer.add(buffer.getBlock(), 0, builder.blockSize());
        buffer.clear();
      }
      buffer.insertUnsorted(kv); //since it is already sorted
    }
    //write out the last
    if(buffer.getNumberOfElements() > 0)
      writer.add(buffer.getBlock(), 0, builder.blockSize());
    writer.close();

    //open tree:
    return new ReadOnlyHybridTree<>(dir, name, keyCodec,valueCodec,builder);
  }


  public ReadOnlyHybridTree(Path dir, String name,
                            BCodec<A> keyCodec,
                            BCodec<B> valueCodec,
                            CFileBuilder cFileBuilder){
    try {
      this.values = cFileBuilder.path(dir.resolve(name+".cval")).reader();
      this.kvCodec = new KeyValueCodec<>(keyCodec,valueCodec);
      idx = new TreeMap<>();
      rebuildIndex();
    } catch(Exception e){
      e.printStackTrace();
      throw new Error(e);
    }
  }

  @Override
  public void close() throws IOException {
    values.close();
  }

  @Override
  public boolean containsKey(A key) throws IOException {
    return getKeyValue(key) != null;
  }

  @Override
  public void createTree(Iterator<KeyValue<A,B>> iterator) throws IOException {
    throw new IOException("read only tree cannot crate a Tree");
  }

  @Override
  public void delete() throws IOException {
    //Todo: Should we allow for delete?
    throw new IOException("Read Only tree");
  }

  @Override
  public A getKey(int position) throws IOException {
    if (position < 0 || position >= size())
      throw new IOException("position out of bounds");
    Iterator <BlockPointer> ptrs = blockPointers().iterator();
    while(ptrs.hasNext()){
      BlockPointer bNo = ptrs.next();
      BBuffer<KeyValue<A,B>> sb = getBlock(bNo);
      if (position < sb.getNumberOfElements()) {
        return sb.get(position).getKey();
      }
      position -= sb.getNumberOfElements();
    }
    return null;
  }

  @Override
  public KeyValue<A,B> getKeyValue(A key) throws IOException{
    return getBlock(key).get(new KeyValue<>(key));
  }

  @Override
  public TreePosition getPosition(A key) throws IOException {
    TreePosition tp = getPositionWithMissing(key);
    return tp.exists() ? tp : null;
  }

  @Override
  public TreePosition getPositionWithMissing(A key) throws IOException {
    BlockPointer block = findBlock(key);
    if(block.getOffset() < 0) return null;
    return getFilePositionNoStrict(key, block);
  }

  @Override
  public boolean isEmpty(){
    return size() < 1;
  }

  @Override
  public Iterator<KeyValue<A, B>> iterator(boolean descend, A from, boolean fromInclusive,
                                           A to, boolean toInclusive) {
    return new ReadOnlyHybridTreeIterator(descend,from,fromInclusive,to,toInclusive);
  }

  @Override
  public void put(A key, B value) throws IOException {
    put(key,value, true);
  }

  public void put(A key, B value, boolean update) throws IOException{
    throw new IOException("Read Only Tree");
  }

  @Override
  public void putIfNotExists(A key, B value) throws IOException{
    put(key,value,false);
  }

  @Override
  public void rebuildIndex() throws IOException, UnsupportedOperationException {
    //just return if there are no value blocks
    Iterator<BlockPointer> iter = values.iterator();
    if(!iter.hasNext()){
      size = 0;
      idx.clear();
      rightPtr = BlockPointer.empty();
      return;

    }
    idx.clear();

    BBuffer<KeyValue<A,B>> tmp;
    ArrayList <ReadOnlyHybridTree.FirstLastKey<A>> blocks = new ArrayList<>();
    int s = 0;
    while (iter.hasNext()) {
      BlockPointer ptr = iter.next();
      ByteBuffer bb = values.read(ptr);
      tmp = new BBuffer<>(bb, kvCodec);
      if(tmp.getNumberOfElements() == 0)
        break;
      KeyValue<A,B> first = tmp.getFirst();
      KeyValue<A,B> last = tmp.getLast();
      ReadOnlyHybridTree.FirstLastKey<A> sl =
          new ReadOnlyHybridTree.FirstLastKey<>(first.getKey(), last.getKey(), ptr);
      blocks.add(sl);
      s += tmp.getNumberOfElements();
    }
    if(blocks.isEmpty()) { //should never happen most likely
      size = 0;
      idx.clear();
      rightPtr = BlockPointer.empty();
      return;
    }

    //Sort the blocks and set all initial values
    Collections.sort(blocks);
    size = 0;
    rightPtr = blocks.get(0).ptr;
    size = s;

    //System.out.println("blocks length: "+blocks.length);
    for (int i = 0; i < blocks.size() - 1; i++) {
      ReadOnlyHybridTree.FirstLastKey<A> left = blocks.get(i);
      ReadOnlyHybridTree.FirstLastKey<A> right = blocks.get(i + 1);

      A sep = generateSeparator(left.last, right.first);
      //System.out.println(left.last+" "+right.first+" "+sep);
      addPointer(sep, right.ptr);
    }
  }

  @Override
  public B remove(A key) throws IOException {
    throw new IOException("Read Only Tree");
  }

  @Override
  public void save(){
  }

  @Override
  public int size(){
    return (int) size;
  }

  @Override
  public void truncate() throws IOException{
    throw new IOException("Read Only Tree");
  }


  //Utility methods
  public void printBlocks() throws IOException{
    Iterator<BlockPointer> iter = blockPointers().iterator();
    while(iter.hasNext()){
      BlockPointer ptr = iter.next();
      BBuffer<KeyValue<A,B>> block = getBlock(ptr);
      KeyValue<A,B> first = block.getFirst();
      KeyValue<A,B> last = block.getLast();
      System.out.println("block: "+ptr+" "+first.getKey()+":::"+last.getKey());
    }
  }

  public void printTree(){
    Iterator<Map.Entry<A,BlockPointer>> iter = idx.entrySet().iterator();
    StringBuilder sbuilder = new StringBuilder();
    while(iter.hasNext()){
      Map.Entry<A,BlockPointer> e = iter.next();
      sbuilder.append(e.getValue()).append("::").append(e.getKey()).append("::");
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
          //return false;
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

  private void addPointer(A sep, BlockPointer right){
    if(idx.isEmpty()){
      idx.put(sep, rightPtr);
      this.rightPtr = right;
      return;
    }
    //add right
    if(idx.containsKey(sep)) throw new Error("node already contains key: "+sep);
    Map.Entry<A,BlockPointer> higher = idx.higherEntry(sep);
    if(higher != null){
      idx.put(sep, higher.getValue());
      idx.put(higher.getKey(), right);
    } else {
      idx.put(sep, rightPtr);
      rightPtr = right;
    }
  }

  private Stream<BlockPointer> blockPointers(){
    return Stream.concat(idx.values().stream(), Stream.of(rightPtr));
  }

  private MapEntry<BlockPointer, Integer> countKeyValues(BlockPointer highBlock) throws IOException{
    Iterator<BlockPointer> all = blockPointers().iterator();
    int cnt = 0;
    BlockPointer current = BlockPointer.empty();
    while(all.hasNext()){
      current = all.next();
      if(current == highBlock)
        break;
      cnt += getBlock(current).getNumberOfElements();
    }
    return new MapEntry<>(current,cnt);
  }

  /*
  private Map.Entry<A,BlockPointer> entry(A key){
    Map.Entry<A,BlockPointer> ptr = idx.higherEntry(key);
    return ptr != null ? new MapEntry<>(ptr.getKey(), ptr.getValue()) : new MapEntry<>(null, rightPtr);
  }
  */

  private BlockPointer findBlock(A key){
    Map.Entry<A,BlockPointer> ptr = idx.higherEntry(key);
    return ptr != null ? ptr.getValue() : rightPtr;
  }

  private A generateSeparator(A small, A large) {
    return CodecUtil.separate(small, large, kvCodec.keyCodec);
  }

  private BBuffer<KeyValue<A,B>> getBlock(A key) throws IOException{
    return getBlock(findBlock(key));
  }

  private BBuffer<KeyValue<A,B>> getBlock(BlockPointer ptr) throws IOException{

      if(currentPtr == null || currentPtr.getOffset() != ptr.getOffset()){
        currentPtr = ptr;
        currentBlock = new BBuffer<>(values.read(currentPtr), kvCodec);
      }
      return currentBlock;

  }

  private TreePosition getFilePositionNoStrict(A key, BlockPointer ptr) throws IOException{
    //Todo: make sure not a problem
    //if (values.size() == 0)
    //  return null;
    BBuffer<KeyValue<A,B>> sb = getBlock(ptr);
    int smallerInBlock = sb.search(new KeyValue<>(key));
    boolean exists = true;
    if (smallerInBlock < 0) { //not found
      exists = false;
      smallerInBlock = Math.abs(smallerInBlock);
      smallerInBlock--; //readjust
    }
    int elementsInBlock = sb.getNumberOfElements();
    int elements = (int) size;
    Map.Entry<BlockPointer, Integer> cnt = countKeyValues(ptr);
    int smaller = cnt.getValue() + smallerInBlock;
    return new TreePosition(smaller, elements, smallerInBlock, elementsInBlock, exists);
  }

  static class FirstLastKey<A> implements Comparable<ReadOnlyHybridTree.FirstLastKey<A>> {
    BlockPointer ptr;
    A first;
    A last;

    FirstLastKey(A first, A last, BlockPointer ptr) {
      this.ptr = ptr;
      this.first = first;
      this.last = last;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compareTo(ReadOnlyHybridTree.FirstLastKey<A> o) {
      return ((Comparable<? super A>)first).compareTo(o.first);
    }

    @Override
    public String toString(){
      return first+" "+last+" "+ptr;
    }
  }

  private class ReadOnlyHybridTreeIterator implements Iterator<KeyValue<A,B>> {
    Iterator<KeyValue<A,B>> sbIterator;
    List<BlockPointer> blocks;
    boolean inclusive;
    boolean reverse;
    boolean endInclusive;
    KeyValue<A,B> end;
    int currblock = 0;
    KeyValue<A,B> next = null;


    ReadOnlyHybridTreeIterator(boolean reverse, A from, boolean inclusive, A to, boolean endInclusive) {
      this.inclusive = inclusive;
      this.reverse = reverse;
      this.end = to == null ? null : new KeyValue<>(to, null);
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

    private void getNext(){
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

    private void nextBlock(A from) throws IOException{
      if (currblock >= blocks.size())
        sbIterator = null;
      else {
        sbIterator = from == null ?
            getBlock(blocks.get(currblock)).iterator() :
            getBlock(blocks.get(currblock)).iterator(false, new KeyValue<>(from), inclusive, null, false);
        currblock++;
      }
    }

    private void nextIter(A from){
      try {
        if (reverse)
          prevBlock(from);
        else
          nextBlock(from);
      } catch(IOException e){
        throw new Error(e);
      }
    }

    @SuppressWarnings("unchecked")
    private void prevBlock(A from) throws IOException{
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
          BlockPointer bNo = findBlock(from);
          for (; currblock > 0; currblock--) {
            if (blocks.get(currblock) == bNo)
              break;
          }
        }
      } else {
        this.currblock = 0;
        if (from != null) {
          BlockPointer bNo = findBlock(from);
          for (; currblock < blocks.size(); currblock++) {
            if (blocks.get(currblock) == bNo)
              break;
          }
        }
      }
    }
  }
}
