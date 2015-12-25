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

package org.mellowtech.core.bytestorable.io;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.CBString;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by msvens on 25/11/15.
 */
public class SortedBlockTest {

  public static CBString[] words = new CBString[]{new CBString("hotel"), new CBString("delta"),
      new CBString("alpha"), new CBString("bravo"), new CBString("india"), new CBString("echo"),
      new CBString("foxtrot"), new CBString("juliet"), new CBString("charlie"), new CBString("golf")};

  public static CBString[] ascend = new CBString[]{new CBString("alpha"), new CBString("bravo"),
      new CBString("charlie"), new CBString("delta"), new CBString("echo"),
      new CBString("foxtrot"), new CBString("golf"), new CBString("hotel"), new CBString("india"),
      new CBString("juliet")};

  public static CBString[] descend = new CBString[]{new CBString("juliet"), new CBString("india"),
      new CBString("hotel"), new CBString("golf"),
      new CBString("foxtrot"), new CBString("echo"), new CBString("delta"),
      new CBString("charlie"), new CBString("bravo"), new CBString("alpha")};

  public static int blockSize;
  public static int wordLen;

  static {
    int len = 0;
    for(CBString str : words){
      len += str.byteSize();
    }
    wordLen = len;
    blockSize = len + SortedBlock.extraBytes(10, SortedBlock.PTR_NORMAL);
  }

  SortedBlock<CBString> sb;


  public SortedBlock<CBString> newBlock() {
    SortedBlock <CBString> temp = new SortedBlock<>();
    temp.setBlock(new byte[blockSize], new CBString(), true, SortedBlock.PTR_NORMAL, (short) 0);
    return temp;
  }

  @Before
  public void before(){
    sb = newBlock();
  }

  //common tests
  @Test
  public void testPointerSize() {
    Assert.assertEquals(SortedBlock.PTR_NORMAL, sb.getPointerSize());
  }

  @Test
  public void testReservedSpace(){
    Assert.assertEquals(0, sb.getReservedSpace());
  }

  @Test
  public void testGetReservedSpaceStart(){
    Assert.assertEquals(2, sb.getReservedSpaceStart());
  }

  @Test
  public void testGetBlock(){
    Assert.assertNotNull(sb.getBlock());
  }


  @Test
  public void testGetBuffer(){
    Assert.assertNotNull(sb.getByteBuffer());
  }


  @Test
  public void testGetKeyType(){
    Assert.assertEquals(CBString.class, sb.getKeyType().getClass());
  }


  @Test
  public void testStorageCapacity(){
    Assert.assertEquals(sb.storageCapacity(), wordLen + (SortedBlock.PTR_NORMAL * words.length));
  }

  //Tests on empty block:
  @Test
  public void zeroBinarySearchBC() {
    Assert.assertEquals(-1, sb.binarySearchBC(new CBString("first")));
  }

  //Tests on empty block:
  @Test
  public void zeroBinarySearch() {
    Assert.assertEquals(-1, sb.binarySearch(new CBString("first")));
  }

  @Test
  public void zeroCanMerge() {
    Assert.assertTrue(sb.canMerge(newBlock()));
  }

  @Test
  public void zeroCanMergePlusOne(){
    Assert.assertTrue(sb.canMerge(newBlock(), new CBString("additional")));
  }

  @Test
  public void zeroContainsKey(){
    Assert.assertFalse(sb.containsKey(ascend[0]));
  }

  @Test
  public void zeroDeleteKeyAtPos(){
    Assert.assertNull(sb.deleteKey(0));
  }

  @Test
  public void zeroDeleteKey(){
    Assert.assertNull(sb.deleteKey(ascend[0]));
  }

  @Test
  public void zeroFitsKey(){
    Assert.assertTrue(sb.fitsKey(ascend[0]));
  }

  @Test
  public void zeroBytesWritten(){
    //resevedSpace (2), 1 (pointer type), 2 (num elemens), 2 (bytes written)
    Assert.assertEquals(7, sb.getBytesWritten());
    //think about how to test
    //should be reservedSpace + headerSize

  }

  @Test
  public void zeroDataAndPointersBytes(){
    Assert.assertEquals(0, sb.getDataAndPointersBytes());
  }

  @Test
  public void zeroDataBytes(){
    Assert.assertEquals(0, sb.getDataBytes());
  }

  @Test
  public void zeroFirstKey(){
    Assert.assertNull(sb.getFirstKey());
  }

  @Test
  public void zeroGetKeyAtPos(){
    Assert.assertNull(sb.getKey(0));
  }

  @Test
  public void zeroGetKey(){
    Assert.assertNull(sb.getKey(ascend[0]));
  }

  @Test
  public void zeroGetLastKey(){
    Assert.assertNull(sb.getLastKey());
  }

  @Test
  public void zeroGetNumberOfElements(){
    Assert.assertEquals(0, sb.getNumberOfElements());
  }

  @Test
  public void zeroIterator(){
    Assert.assertFalse(sb.iterator().hasNext());
  }

  @Test
  public void zeroIteratorRangeInclusive(){
    Assert.assertFalse(sb.iterator(ascend[1], true, ascend[8], true).hasNext());
  }

  @Test
  public void zeroIteratorRangeExclusive(){
    Assert.assertFalse(sb.iterator(ascend[1], false, ascend[8], false).hasNext());
  }

  @Test
  public void zeroReverseIterator(){
    Assert.assertFalse(sb.reverseIterator().hasNext());
  }

  @Test
  public void zeroReverseIteratorRangeInclusive(){
    Assert.assertFalse(sb.reverseIterator(ascend[8], true, ascend[1], true).hasNext());
  }

  @Test
  public void zeroReverseIteratorRangeExclusive(){
    Assert.assertFalse(sb.reverseIterator(ascend[8], false, ascend[1], false).hasNext());
  }

  @Test
  public void zeroMergeBlock(){
    SortedBlock <CBString> other = newBlock();
    Assert.assertEquals(0, sb.mergeBlock(other).getNumberOfElements());
    other.insertKey(ascend[0]);
    Assert.assertEquals(ascend[0], sb.mergeBlock(other).getFirstKey());
  }

  @Test
  public void zeroReopen(){
    byte[] bytes = sb.getBlock();
    SortedBlock <CBString> other = new SortedBlock<>();
    Assert.assertEquals(0, other.setBlock(bytes, new CBString()).getNumberOfElements());
  }

  @Test
  public void zeroSort(){
    Assert.assertEquals(0, sb.sort(false).getNumberOfElements());
  }

  @Test
  public void zeroSplitBlock(){
    Assert.assertEquals(0, sb.splitBlock().getNumberOfElements());
  }

  //Tests on block with 1 entry:
  public void putFirst(){
    sb.insertKey(ascend[0]);
  }

  @Test
  public void oneBinarySearch() {
    putFirst();
    Assert.assertEquals(0, sb.binarySearch(ascend[0]));
  }

  @Test
  public void oneBinarySearchBC() {
    putFirst();
    Assert.assertEquals(0, sb.binarySearchBC(ascend[0]));
  }

  @Test
  public void oneCanMerge() {
    putFirst();
    Assert.assertTrue(sb.canMerge(newBlock()));
  }

  @Test
  public void oneCanMergePlusOne(){
    putFirst();
    Assert.assertTrue(sb.canMerge(newBlock(), ascend[1]));
  }

  @Test
  public void oneContainsKey(){
    putFirst();
    Assert.assertTrue(sb.containsKey(ascend[0]));
  }

  @Test
  public void oneDeleteKeyAtPos(){
    putFirst();
    Assert.assertEquals(ascend[0],sb.deleteKey(0));
  }

  @Test
  public void oneDeleteKey(){
    putFirst();
    Assert.assertEquals(ascend[0],sb.deleteKey(ascend[0]));
  }

  @Test
  public void oneFitsKey(){
    putFirst();
    Assert.assertTrue(sb.fitsKey(ascend[1]));
  }

  @Test
  public void oneBytesWritten(){
    putFirst();
    int bytes = 7 + 2 + ascend[0].byteSize();
    Assert.assertEquals(bytes, sb.getBytesWritten());
  }

  @Test
  public void oneDataAndPointersBytes(){
    putFirst();
    Assert.assertEquals(2 + ascend[0].byteSize(), sb.getDataAndPointersBytes());
  }

  @Test
  public void oneDataBytes(){
    putFirst();
    Assert.assertEquals(ascend[0].byteSize(), sb.getDataBytes());
  }

  @Test
  public void oneFirstKey(){
    putFirst();
    Assert.assertEquals(ascend[0],sb.getFirstKey());
  }

  @Test
  public void oneGetKeyAtPos(){
    putFirst();
    Assert.assertEquals(ascend[0],sb.getKey(0));
  }

  @Test
  public void oneGetKey(){
    putFirst();
    Assert.assertEquals(ascend[0], sb.getKey(ascend[0]));
  }

  @Test
  public void oneGetLastKey(){
    putFirst();
    Assert.assertEquals(ascend[0], sb.getLastKey());
  }

  @Test
  public void oneGetNumberOfElements(){
    putFirst();
    Assert.assertEquals(1, sb.getNumberOfElements());
  }

  @Test
  public void oneIterator(){
    putFirst();
    Assert.assertTrue(sb.iterator().hasNext());
  }

  @Test
  public void oneIteratorRangeInclusive(){
    putFirst();
    Assert.assertFalse(sb.iterator(ascend[1], true, ascend[8], true).hasNext());
  }

  @Test
  public void oneIteratorRangeExclusive(){
    putFirst();
    Assert.assertFalse(sb.iterator(ascend[1], false, ascend[8], false).hasNext());
  }

  @Test
  public void oneReverseIterator(){
    putFirst();
    Assert.assertTrue(sb.reverseIterator().hasNext());
  }

  @Test
  public void oneReverseIteratorRangeInclusive(){
    putFirst();
    Assert.assertFalse(sb.reverseIterator(ascend[8], true, ascend[1], true).hasNext());
  }

  @Test
  public void oneReverseIteratorRangeExclusive(){
    putFirst();
    Assert.assertFalse(sb.reverseIterator(ascend[8], false, ascend[1], false).hasNext());
  }

  @Test
  public void oneMergeBlock(){
    putFirst();
    SortedBlock <CBString> other = newBlock();
    Assert.assertEquals(1, sb.mergeBlock(other).getNumberOfElements());
    other.insertKey(ascend[1]);
    Assert.assertEquals(ascend[1], sb.mergeBlock(other).getLastKey());
  }

  @Test
  public void oneReopen(){
    putFirst();
    byte[] bytes = sb.getBlock();
    SortedBlock <CBString> other = new SortedBlock<>();
    Assert.assertEquals(1, other.setBlock(bytes, new CBString()).getNumberOfElements());
  }

  @Test
  public void oneSort(){
    putFirst();
    Assert.assertEquals(1, sb.sort(false).getNumberOfElements());
  }

  @Test
  public void oneSplitBlock(){
    putFirst();
    SortedBlock<CBString> nb = sb.splitBlock();
    Assert.assertEquals(0, sb.getNumberOfElements());
    Assert.assertEquals(1, nb.getNumberOfElements());
  }

  //Tests on block with max entries:
  public void putAll(){
    for(CBString s : words)
      sb.insertKey(s);
  }

  @Test
  public void tenBinarySearch() {
    putAll();
    Assert.assertEquals(0, sb.binarySearch(ascend[0]));
  }

  @Test
  public void tenBinarySearchBC() {
    putAll();
    Assert.assertEquals(0, sb.binarySearchBC(ascend[0]));
  }

  @Test
  public void tenCanMerge() {
    putAll();
    Assert.assertTrue(sb.canMerge(newBlock()));
  }

  @Test
  public void tenCanMergePlusOne(){
    putAll();
    Assert.assertFalse(sb.canMerge(newBlock(), new CBString("additional")));
  }

  @Test
  public void tenContainsKey(){
    putAll();
    Assert.assertTrue(sb.containsKey(ascend[0]));
  }

  @Test
  public void tenDeleteKeyAtPos(){
    putAll();
    Assert.assertEquals(ascend[0],sb.deleteKey(0));
  }

  @Test
  public void tenDeleteKey(){
    putAll();
    Assert.assertEquals(ascend[0],sb.deleteKey(ascend[0]));
  }

  @Test
  public void tenFitsKey(){
    putAll();
    Assert.assertFalse(sb.fitsKey(ascend[1]));
  }

  @Test
  public void tenBytesWritten(){
    putAll();
    int bytes = blockSize;
    Assert.assertEquals(bytes, sb.getBytesWritten());
  }

  @Test
  public void tenDataAndPointersBytes(){
    putAll();
    Assert.assertEquals(wordLen + (2 * words.length), sb.getDataAndPointersBytes());
  }

  @Test
  public void tenDataBytes(){
    putAll();
    Assert.assertEquals(wordLen, sb.getDataBytes());
  }

  @Test
  public void tenFirstKey(){
    putAll();
    Assert.assertEquals(ascend[0],sb.getFirstKey());
  }

  @Test
  public void tenGetKeyAtPos(){
    putAll();
    Assert.assertEquals(ascend[0],sb.getKey(0));
  }

  @Test
  public void tenGetKey(){
    putAll();
    Assert.assertEquals(ascend[0], sb.getKey(ascend[0]));
  }

  @Test
  public void tenGetLastKey(){
    putAll();
    Assert.assertEquals(ascend[9], sb.getLastKey());
  }

  @Test
  public void tenGetNumberOfElements(){
    putAll();
    Assert.assertEquals(10, sb.getNumberOfElements());
  }

  @Test
  public void tenIterator(){
    putAll();
    int i = 0;
    Iterator<CBString> iter = sb.iterator();
    while(iter.hasNext()){
      Assert.assertEquals(ascend[i], iter.next());
      i++;
    }
    Assert.assertEquals(10, i);
  }

  @Test
  public void tenIteratorRangeInclusive(){
    putAll();
    int i = 1;
    Iterator<CBString> iter = sb.iterator(ascend[1], true, ascend[8], true);
    while(iter.hasNext()){
      Assert.assertEquals(ascend[i], iter.next());
      i++;
    }
    Assert.assertEquals(9, i);
  }

  @Test
  public void tenIteratorRangeExclusive(){
    putAll();
    int i = 2;
    Iterator<CBString> iter = sb.iterator(ascend[1], false, ascend[8], false);
    while(iter.hasNext()){
      Assert.assertEquals(ascend[i], iter.next());
      i++;
    }
    Assert.assertEquals(8, i);
  }

  @Test
  public void tenReverseIterator(){
    putAll();
    int i = 9;
    Iterator<CBString> iter = sb.reverseIterator();
    while(iter.hasNext()){
      Assert.assertEquals(ascend[i], iter.next());
      i--;
    }
    Assert.assertEquals(-1, i);
  }

  @Test
  public void tenReverseIteratorRangeInclusive(){
    putAll();
    int i = 8;
    Iterator<CBString> iter = sb.reverseIterator(ascend[8], true, ascend[1], true);
    while(iter.hasNext()){
      Assert.assertEquals(ascend[i], iter.next());
      i--;
    }
    Assert.assertEquals(0, i);
  }

  @Test
  public void tenReverseIteratorRangeExclusive(){
    putAll();
    int i = 7;
    Iterator<CBString> iter = sb.reverseIterator(ascend[8], false, ascend[1], false);
    while(iter.hasNext()){
      Assert.assertEquals(ascend[i], iter.next());
      i--;
    }
    Assert.assertEquals(1, i);
  }

  @Test
  public void tenMergeBlock(){
    putAll();
    SortedBlock <CBString> other = newBlock();
    Assert.assertEquals(10, sb.mergeBlock(other).getNumberOfElements());
    other.insertKey(new CBString("newItem"));
    Assert.assertEquals(10, sb.mergeBlock(other).getNumberOfElements());
  }

  @Test
  public void tenReopen(){
    putAll();
    byte[] bytes = sb.getBlock();
    SortedBlock <CBString> other = new SortedBlock<>();
    Assert.assertEquals(10, other.setBlock(bytes, new CBString()).getNumberOfElements());
  }

  @Test
  public void tenSort(){
    //putAll();
    for(CBString w : words){
      sb.insertKeyUnsorted(w);
    }
    sb.sort(false);
    Iterator <CBString> iter = sb.iterator();
    int i = 0;
    while(iter.hasNext()){
      Assert.assertEquals(ascend[i], iter.next());
      i++;
    }
  }

  @Test
  public void tenSplitBlock(){
    putAll();
    SortedBlock<CBString> nb = sb.splitBlock();
    Assert.assertEquals(5, sb.getNumberOfElements());
    Assert.assertEquals(5, nb.getNumberOfElements());
  }

}
