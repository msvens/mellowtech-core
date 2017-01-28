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

package org.mellowtech.core.codec.io;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mellowtech.core.codec.BBuffer;
import org.mellowtech.core.codec.StringCodec;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * Created by msvens on 25/11/15.
 */
public class BBufferTest {

  public static String[] words = new String[]{"hotel", "delta", "alpha", "bravo", "india",
  "echo", "foxtrot", "juliet", "charlie", "golf"};

  public static String[] ascend = new String[]{"alpha", "bravo", "charlie", "delta", "echo",
  "foxtrot", "golf", "hotel", "india", "juliet"};

  public static String[] descend = new String[]{"juliet", "india", "hotel", "golf", "foxtrot", "echo",
  "delta", "charlie", "bravo", "alpha"};

  public static int blockSize;
  public static int wordLen;

  public static StringCodec codec = new StringCodec();

  static {
    int len = 0;
    for(String str : words){
      len += codec.byteSize(str);
    }
    wordLen = len;
    blockSize = len + BBuffer.bytesNeeded(10, BBuffer.PtrType.NORMAL);
  }

  BBuffer<String> sb;


  public BBuffer<String> newBlock() {
    //Allocate a larger block and set limit...etc
    ByteBuffer bb = ByteBuffer.allocate(blockSize+100);
    bb.position(50);
    bb.limit(bb.position()+blockSize);
    return new BBuffer<String>(bb.slice(), new StringCodec(), BBuffer.PtrType.NORMAL, (short) 0);
  }

  @Before
  public void before(){
    sb = newBlock();
  }

  //common tests
  @Test
  public void testPointerType() {
    Assert.assertEquals(BBuffer.PtrType.NORMAL, sb.getPointerType());
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


  /*@Test
  public void testGetKeyType(){
    Assert.assertEquals(CBString.class, sb);
  }*/


  @Test
  public void testStorageCapacity(){
    Assert.assertEquals(sb.storageCapacity(), wordLen + (BBuffer.PtrType.NORMAL.size() * words.length));
  }

  //Tests on empty block:
  @Test
  public void zeroBinarySearchBC() {
    Assert.assertEquals(-1, sb.searchBC("first"));
  }

  //Tests on empty block:
  @Test
  public void zeroBinarySearch() {
    Assert.assertEquals(-1, sb.search("first"));
  }

  @Test
  public void zeroCanMerge() {
    Assert.assertTrue(sb.fits(newBlock()));
  }

  @Test
  public void zeroCanMergePlusOne(){
    Assert.assertTrue(sb.fits(newBlock(), "additional"));
  }

  @Test
  public void zeroContainsKey(){
    Assert.assertFalse(sb.contains(ascend[0]));
  }

  @Test
  public void zeroDeleteKeyAtPos(){
    Assert.assertNull(sb.delete(0));
  }

  @Test
  public void zeroDeleteKey(){
    Assert.assertNull(sb.delete(ascend[0]));
  }

  @Test
  public void zeroFitsKey(){
    Assert.assertTrue(sb.fits(ascend[0]));
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
    Assert.assertNull(sb.getFirst());
  }

  @Test
  public void zeroGetKeyAtPos(){
    Assert.assertNull(sb.get(0));
  }

  @Test
  public void zeroGetKey(){
    Assert.assertNull(sb.get(ascend[0]));
  }

  @Test
  public void zeroGetLastKey(){
    Assert.assertNull(sb.getLast());
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
    Assert.assertFalse(sb.iterator(false,ascend[1], true, ascend[8], true).hasNext());
  }

  @Test
  public void zeroIteratorRangeExclusive(){
    Assert.assertFalse(sb.iterator(false,ascend[1], false, ascend[8], false).hasNext());
  }

  @Test
  public void zeroReverseIterator(){
    Assert.assertFalse(sb.iterator(true).hasNext());
  }

  @Test
  public void zeroReverseIteratorRangeInclusive(){
    Assert.assertFalse(sb.iterator(true, ascend[8], true, ascend[1], true).hasNext());
  }

  @Test
  public void zeroReverseIteratorRangeExclusive(){
    Assert.assertFalse(sb.iterator(true, ascend[8], false, ascend[1], false).hasNext());
  }

  @Test
  public void zeroMergeBlock(){
    BBuffer <String> other = newBlock();
    Assert.assertEquals(0, sb.merge(other).getNumberOfElements());
    other.insert(ascend[0]);
    Assert.assertEquals(ascend[0], sb.merge(other).getFirst());
  }

  @Test
  public void zeroReopen(){
    ByteBuffer bytes = sb.getBlock();
    BBuffer <String> other = new BBuffer<>(bytes, new StringCodec());
    Assert.assertEquals(0, other.getNumberOfElements());
  }

  @Test
  public void zeroSort(){
    Assert.assertEquals(0, sb.sort(false).getNumberOfElements());
  }

  @Test
  public void zeroSplitBlock(){
    Assert.assertEquals(0, sb.split().getNumberOfElements());
  }

  //Tests on block with 1 entry:
  public void putFirst(){
    sb.insert(ascend[0]);
  }

  @Test
  public void oneBinarySearch() {
    putFirst();
    Assert.assertEquals(0, sb.search(ascend[0]));
  }

  @Test
  public void oneBinarySearchBC() {
    putFirst();
    Assert.assertEquals(0, sb.searchBC(ascend[0]));
  }

  @Test
  public void oneCanMerge() {
    putFirst();
    Assert.assertTrue(sb.fits(newBlock()));
  }

  @Test
  public void oneCanMergePlusOne(){
    putFirst();
    Assert.assertTrue(sb.fits(newBlock(), ascend[1]));
  }

  @Test
  public void oneContainsKey(){
    putFirst();
    Assert.assertTrue(sb.contains(ascend[0]));
  }

  @Test
  public void oneDeleteKeyAtPos(){
    putFirst();
    Assert.assertEquals(ascend[0],sb.delete(0));
  }

  @Test
  public void oneDeleteKey(){
    putFirst();
    Assert.assertEquals(ascend[0],sb.delete(ascend[0]));
  }

  @Test
  public void oneFitsKey(){
    putFirst();
    Assert.assertTrue(sb.fits(ascend[1]));
  }

  @Test
  public void oneBytesWritten(){
    putFirst();
    int bytes = 7 + 2 + codec.byteSize(ascend[0]);
    Assert.assertEquals(bytes, sb.getBytesWritten());
  }

  @Test
  public void oneDataAndPointersBytes(){
    putFirst();
    Assert.assertEquals(2 + codec.byteSize(ascend[0]), sb.getDataAndPointersBytes());
  }

  @Test
  public void oneDataBytes(){
    putFirst();
    Assert.assertEquals(codec.byteSize(ascend[0]), sb.getDataBytes());
  }

  @Test
  public void oneFirstKey(){
    putFirst();
    Assert.assertEquals(ascend[0],sb.getFirst());
  }

  @Test
  public void oneGetKeyAtPos(){
    putFirst();
    Assert.assertEquals(ascend[0],sb.get(0));
  }

  @Test
  public void oneGetKey(){
    putFirst();
    Assert.assertEquals(ascend[0], sb.get(ascend[0]));
  }

  @Test
  public void oneGetLastKey(){
    putFirst();
    Assert.assertEquals(ascend[0], sb.getLast());
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
    Assert.assertFalse(sb.iterator(false, ascend[1], true, ascend[8], true).hasNext());
  }

  @Test
  public void oneIteratorRangeExclusive(){
    putFirst();
    Assert.assertFalse(sb.iterator(false, ascend[1], false, ascend[8], false).hasNext());
  }

  @Test
  public void oneReverseIterator(){
    putFirst();
    Assert.assertTrue(sb.iterator(true).hasNext());
  }

  @Test
  public void oneReverseIteratorRangeInclusive(){
    putFirst();
    Assert.assertFalse(sb.iterator(true, ascend[8], true, ascend[1], true).hasNext());
  }

  @Test
  public void oneReverseIteratorRangeExclusive(){
    putFirst();
    Assert.assertFalse(sb.iterator(true, ascend[8], false, ascend[1], false).hasNext());
  }

  @Test
  public void oneMergeBlock(){
    putFirst();
    BBuffer <String> other = newBlock();
    Assert.assertEquals(1, sb.merge(other).getNumberOfElements());
    other.insert(ascend[1]);
    Assert.assertEquals(ascend[1], sb.merge(other).getLast());
  }

  @Test
  public void oneReopen(){
    putFirst();
    ByteBuffer bytes = sb.getBlock();
    BBuffer <String> other = new BBuffer<>(bytes, new StringCodec());
    Assert.assertEquals(1, other.getNumberOfElements());
  }

  @Test
  public void oneSort(){
    putFirst();
    Assert.assertEquals(1, sb.sort(false).getNumberOfElements());
  }

  @Test
  public void oneSplitBlock(){
    putFirst();
    BBuffer<String> nb = sb.split();
    Assert.assertEquals(0, sb.getNumberOfElements());
    Assert.assertEquals(1, nb.getNumberOfElements());
  }

  //Tests on block with max entries:
  public void putAll(){
    for(String s : words)
      sb.insert(s);
  }

  @Test
  public void tenBinarySearch() {
    putAll();
    Assert.assertEquals(0, sb.search(ascend[0]));
  }

  @Test
  public void tenBinarySearchBC() {
    putAll();
    Assert.assertEquals(0, sb.searchBC(ascend[0]));
  }

  @Test
  public void tenCanMerge() {
    putAll();
    Assert.assertTrue(sb.fits(newBlock()));
  }

  @Test
  public void tenCanMergePlusOne(){
    putAll();
    Assert.assertFalse(sb.fits(newBlock(), "additional"));
  }

  @Test
  public void tenContainsKey(){
    putAll();
    Assert.assertTrue(sb.contains(ascend[0]));
  }

  @Test
  public void tenDeleteKeyAtPos(){
    putAll();
    Assert.assertEquals(ascend[0],sb.delete(0));
  }

  @Test
  public void tenDeleteKey(){
    putAll();
    Assert.assertEquals(ascend[0],sb.delete(ascend[0]));
  }

  @Test
  public void tenFitsKey(){
    putAll();
    Assert.assertFalse(sb.fits(ascend[1]));
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
    Assert.assertEquals(ascend[0],sb.getFirst());
  }

  @Test
  public void tenGetKeyAtPos(){
    putAll();
    Assert.assertEquals(ascend[0],sb.get(0));
  }

  @Test
  public void tenGetKey(){
    putAll();
    Assert.assertEquals(ascend[0], sb.get(ascend[0]));
  }

  @Test
  public void tenGetLastKey(){
    putAll();
    Assert.assertEquals(ascend[9], sb.getLast());
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
    Iterator<String> iter = sb.iterator();
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
    Iterator<String> iter = sb.iterator(false, ascend[1], true, ascend[8], true);
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
    Iterator<String> iter = sb.iterator(false, ascend[1], false, ascend[8], false);
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
    Iterator<String> iter = sb.iterator(true);
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
    Iterator<String> iter = sb.iterator(true, ascend[8], true, ascend[1], true);
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
    Iterator<String> iter = sb.iterator(true, ascend[8], false, ascend[1], false);
    while(iter.hasNext()){
      Assert.assertEquals(ascend[i], iter.next());
      i--;
    }
    Assert.assertEquals(1, i);
  }

  @Test
  public void tenMergeEmptyBlock(){
    putAll();
    BBuffer <String> other = newBlock();
    Assert.assertEquals(10, sb.merge(other).getNumberOfElements());
  }

  @Test(expected = BufferOverflowException.class)
  public void tenMergeNonEmptyBlock(){
    putAll();
    BBuffer <String> other = newBlock();
    other.insert("newItem");
    Assert.assertEquals(10, sb.merge(other).getNumberOfElements());
  }

  @Test
  public void tenReopen(){
    putAll();
    ByteBuffer bytes = sb.getBlock();
    BBuffer <String> other = new BBuffer<>(bytes, new StringCodec());
    Assert.assertEquals(10, other.getNumberOfElements());
  }

  @Test
  public void tenSort(){
    //putAll();
    for(String w : words){
      sb.insertUnsorted(w);
    }
    sb.sort(false);
    Iterator<String> iter = sb.iterator();
    int i = 0;
    while(iter.hasNext()){
      Assert.assertEquals(ascend[i], iter.next());
      i++;
    }
  }

  @Test
  public void tenSplitBlock(){
    putAll();
    BBuffer<String> nb = sb.split();
    Assert.assertEquals(5, sb.getNumberOfElements());
    Assert.assertEquals(5, nb.getNumberOfElements());
  }

}
