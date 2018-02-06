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

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mellowtech.core.codec.BBuffer;
import org.mellowtech.core.codec.StringCodec;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * Created by msvens on 25/11/15.
 */
public class BBufferTest {

  static String[] words = new String[]{"hotel", "delta", "alpha", "bravo", "india",
  "echo", "foxtrot", "juliet", "charlie", "golf"};

  static String[] ascend = new String[]{"alpha", "bravo", "charlie", "delta", "echo",
  "foxtrot", "golf", "hotel", "india", "juliet"};

  static String[] descend = new String[]{"juliet", "india", "hotel", "golf", "foxtrot", "echo",
  "delta", "charlie", "bravo", "alpha"};

  static int blockSize;
  static int wordLen;

  static StringCodec codec = new StringCodec();

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

  //Utility
  private void putFirst(){
    sb.insert(ascend[0]);
  }

  //Tests on block with max entries:
  private void putTen(){
    for(String s : words)
      sb.insert(s);
  }

  @BeforeEach
  void before(){
    sb = newBlock();
  }

  //common tests
  @Test
  void testPointerType() {
    assertEquals(BBuffer.PtrType.NORMAL, sb.getPointerType());
  }

  @Test
  void testReservedSpace(){
    assertEquals(0, sb.getReservedSpace());
  }

  @Test
  void testGetReservedSpaceStart(){
    assertEquals(2, sb.getReservedSpaceStart());
  }

  @Test
  void testGetBlock(){
    assertNotNull(sb.getBlock());
  }

  @Test
  void testStorageCapacity(){
    assertEquals(sb.storageCapacity(), wordLen + (BBuffer.PtrType.NORMAL.size() * words.length));
  }

  @Nested
  @DisplayName("An empty buffer")
  class Empty {
    @Test
    void zeroBinarySearchBC() {
      assertEquals(-1, sb.searchBC("first"));
    }

    //Tests on empty block:
    @Test
    void zeroBinarySearch() {
      assertEquals(-1, sb.search("first"));
    }

    @Test
    void zeroCanMerge() {
      assertTrue(sb.fits(newBlock()));
    }

    @Test
    void zeroCanMergePlusOne() {
      assertTrue(sb.fits(newBlock(), "additional"));
    }

    @Test
    void zeroContainsKey() {
      assertFalse(sb.contains(ascend[0]));
    }

    @Test
    void zeroDeleteKeyAtPos() {
      assertNull(sb.delete(0));
    }

    @Test
    void zeroDeleteKey() {
      assertNull(sb.delete(ascend[0]));
    }

    @Test
    void zeroFitsKey() {
      assertTrue(sb.fits(ascend[0]));
    }

    @Test
    void zeroBytesWritten() {
      //resevedSpace (2), 1 (pointer type), 2 (num elemens), 2 (bytes written)
      assertEquals(7, sb.getBytesWritten());
      //think about how to test
      //should be reservedSpace + headerSize

    }

    @Test
    void zeroDataAndPointersBytes() {
      assertEquals(0, sb.getDataAndPointersBytes());
    }

    @Test
    void zeroDataBytes() {
      assertEquals(0, sb.getDataBytes());
    }

    @Test
    void zeroFirstKey() {
      assertNull(sb.getFirst());
    }

    @Test
    void zeroGetKeyAtPos() {
      assertNull(sb.get(0));
    }

    @Test
    void zeroGetKey() {
      assertNull(sb.get(ascend[0]));
    }

    @Test
    void zeroGetLastKey() {
      assertNull(sb.getLast());
    }

    @Test
    void zeroGetNumberOfElements() {
      assertEquals(0, sb.getNumberOfElements());
    }

    @Test
    void zeroIterator() {
      assertFalse(sb.iterator().hasNext());
    }

    @Test
    void zeroIteratorRangeInclusive() {
      assertFalse(sb.iterator(false, ascend[1], true, ascend[8], true).hasNext());
    }

    @Test
    void zeroIteratorRangeExclusive() {
      assertFalse(sb.iterator(false, ascend[1], false, ascend[8], false).hasNext());
    }

    @Test
    void zeroReverseIterator() {
      assertFalse(sb.iterator(true).hasNext());
    }

    @Test
    void zeroReverseIteratorRangeInclusive() {
      assertFalse(sb.iterator(true, ascend[8], true, ascend[1], true).hasNext());
    }

    @Test
    void zeroReverseIteratorRangeExclusive() {
      assertFalse(sb.iterator(true, ascend[8], false, ascend[1], false).hasNext());
    }

    @Test
    void zeroMergeBlock() {
      BBuffer<String> other = newBlock();
      assertEquals(0, sb.merge(other).getNumberOfElements());
      other.insert(ascend[0]);
      assertEquals(ascend[0], sb.merge(other).getFirst());
    }

    @Test
    void zeroReopen() {
      ByteBuffer bytes = sb.getBlock();
      BBuffer<String> other = new BBuffer<>(bytes, new StringCodec());
      assertEquals(0, other.getNumberOfElements());
    }

    @Test
    void zeroSort() {
      assertEquals(0, sb.sort(false).getNumberOfElements());
    }

    @Test
    void zeroSplitBlock() {
      assertEquals(0, sb.split().getNumberOfElements());
    }
  }

  @Nested
  @DisplayName("A buffer with one item")
  class One {
    @Test
    void oneBinarySearch() {
      putFirst();
      assertEquals(0, sb.search(ascend[0]));
    }

    @Test
    void oneBinarySearchBC() {
      putFirst();
      assertEquals(0, sb.searchBC(ascend[0]));
    }

    @Test
    void oneCanMerge() {
      putFirst();
      assertTrue(sb.fits(newBlock()));
    }

    @Test
    void oneCanMergePlusOne() {
      putFirst();
      assertTrue(sb.fits(newBlock(), ascend[1]));
    }

    @Test
    void oneContainsKey() {
      putFirst();
      assertTrue(sb.contains(ascend[0]));
    }

    @Test
    void oneDeleteKeyAtPos() {
      putFirst();
      assertEquals(ascend[0], sb.delete(0));
    }

    @Test
    void oneDeleteKey() {
      putFirst();
      assertEquals(ascend[0], sb.delete(ascend[0]));
    }

    @Test
    void oneFitsKey() {
      putFirst();
      assertTrue(sb.fits(ascend[1]));
    }

    @Test
    void oneBytesWritten() {
      putFirst();
      int bytes = 7 + 2 + codec.byteSize(ascend[0]);
      assertEquals(bytes, sb.getBytesWritten());
    }

    @Test
    void oneDataAndPointersBytes() {
      putFirst();
      assertEquals(2 + codec.byteSize(ascend[0]), sb.getDataAndPointersBytes());
    }

    @Test
    void oneDataBytes() {
      putFirst();
      assertEquals(codec.byteSize(ascend[0]), sb.getDataBytes());
    }

    @Test
    void oneFirstKey() {
      putFirst();
      assertEquals(ascend[0], sb.getFirst());
    }

    @Test
    void oneGetKeyAtPos() {
      putFirst();
      assertEquals(ascend[0], sb.get(0));
    }

    @Test
    void oneGetKey() {
      putFirst();
      assertEquals(ascend[0], sb.get(ascend[0]));
    }

    @Test
    void oneGetLastKey() {
      putFirst();
      assertEquals(ascend[0], sb.getLast());
    }

    @Test
    void oneGetNumberOfElements() {
      putFirst();
      assertEquals(1, sb.getNumberOfElements());
    }

    @Test
    void oneIterator() {
      putFirst();
      assertTrue(sb.iterator().hasNext());
    }

    @Test
    void oneIteratorRangeInclusive() {
      putFirst();
      assertFalse(sb.iterator(false, ascend[1], true, ascend[8], true).hasNext());
    }

    @Test
    void oneIteratorRangeExclusive() {
      putFirst();
      assertFalse(sb.iterator(false, ascend[1], false, ascend[8], false).hasNext());
    }

    @Test
    void oneReverseIterator() {
      putFirst();
      assertTrue(sb.iterator(true).hasNext());
    }

    @Test
    void oneReverseIteratorRangeInclusive() {
      putFirst();
      assertFalse(sb.iterator(true, ascend[8], true, ascend[1], true).hasNext());
    }

    @Test
    void oneReverseIteratorRangeExclusive() {
      putFirst();
      assertFalse(sb.iterator(true, ascend[8], false, ascend[1], false).hasNext());
    }

    @Test
    void oneMergeBlock() {
      putFirst();
      BBuffer<String> other = newBlock();
      assertEquals(1, sb.merge(other).getNumberOfElements());
      other.insert(ascend[1]);
      assertEquals(ascend[1], sb.merge(other).getLast());
    }

    @Test
    void oneReopen() {
      putFirst();
      ByteBuffer bytes = sb.getBlock();
      BBuffer<String> other = new BBuffer<>(bytes, new StringCodec());
      assertEquals(1, other.getNumberOfElements());
    }

    @Test
    void oneSort() {
      putFirst();
      assertEquals(1, sb.sort(false).getNumberOfElements());
    }

    @Test
    void oneSplitBlock() {
      putFirst();
      BBuffer<String> nb = sb.split();
      assertEquals(0, sb.getNumberOfElements());
      assertEquals(1, nb.getNumberOfElements());
    }
  }


  @Nested
  @DisplayName("A buffer with ten items")
  class Ten {
    @Test
    void tenBinarySearch() {
      putTen();
      assertEquals(0, sb.search(ascend[0]));
    }

    @Test
    void tenBinarySearchBC() {
      putTen();
      assertEquals(0, sb.searchBC(ascend[0]));
    }

    @Test
    void tenCanMerge() {
      putTen();
      assertTrue(sb.fits(newBlock()));
    }

    @Test
    void tenCanMergePlusOne() {
      putTen();
      assertFalse(sb.fits(newBlock(), "additional"));
    }

    @Test
    void tenContainsKey() {
      putTen();
      assertTrue(sb.contains(ascend[0]));
    }

    @Test
    void tenDeleteKeyAtPos() {
      putTen();
      assertEquals(ascend[0], sb.delete(0));
    }

    @Test
    void tenDeleteKey() {
      putTen();
      assertEquals(ascend[0], sb.delete(ascend[0]));
    }

    @Test
    void tenFitsKey() {
      putTen();
      assertFalse(sb.fits(ascend[1]));
    }

    @Test
    void tenBytesWritten() {
      putTen();
      int bytes = blockSize;
      assertEquals(bytes, sb.getBytesWritten());
    }

    @Test
    void tenDataAndPointersBytes() {
      putTen();
      assertEquals(wordLen + (2 * words.length), sb.getDataAndPointersBytes());
    }

    @Test
    void tenDataBytes() {
      putTen();
      assertEquals(wordLen, sb.getDataBytes());
    }

    @Test
    void tenFirstKey() {
      putTen();
      assertEquals(ascend[0], sb.getFirst());
    }

    @Test
    void tenGetKeyAtPos() {
      putTen();
      assertEquals(ascend[0], sb.get(0));
    }

    @Test
    void tenGetKey() {
      putTen();
      assertEquals(ascend[0], sb.get(ascend[0]));
    }

    @Test
    void tenGetLastKey() {
      putTen();
      assertEquals(ascend[9], sb.getLast());
    }

    @Test
    void tenGetNumberOfElements() {
      putTen();
      assertEquals(10, sb.getNumberOfElements());
    }

    @Test
    void tenIterator() {
      putTen();
      int i = 0;
      Iterator<String> iter = sb.iterator();
      while (iter.hasNext()) {
        assertEquals(ascend[i], iter.next());
        i++;
      }
      assertEquals(10, i);
    }

    @Test
    void tenIteratorRangeInclusive() {
      putTen();
      int i = 1;
      Iterator<String> iter = sb.iterator(false, ascend[1], true, ascend[8], true);
      while (iter.hasNext()) {
        assertEquals(ascend[i], iter.next());
        i++;
      }
      assertEquals(9, i);
    }

    @Test
    void tenIteratorRangeExclusive() {
      putTen();
      int i = 2;
      Iterator<String> iter = sb.iterator(false, ascend[1], false, ascend[8], false);
      while (iter.hasNext()) {
        assertEquals(ascend[i], iter.next());
        i++;
      }
      assertEquals(8, i);
    }

    @Test
    void tenReverseIterator() {
      putTen();
      int i = 9;
      Iterator<String> iter = sb.iterator(true);
      while (iter.hasNext()) {
        assertEquals(ascend[i], iter.next());
        i--;
      }
      assertEquals(-1, i);
    }

    @Test
    void tenReverseIteratorRangeInclusive() {
      putTen();
      int i = 8;
      Iterator<String> iter = sb.iterator(true, ascend[8], true, ascend[1], true);
      while (iter.hasNext()) {
        assertEquals(ascend[i], iter.next());
        i--;
      }
      assertEquals(0, i);
    }

    @Test
    void tenReverseIteratorRangeExclusive() {
      putTen();
      int i = 7;
      Iterator<String> iter = sb.iterator(true, ascend[8], false, ascend[1], false);
      while (iter.hasNext()) {
        assertEquals(ascend[i], iter.next());
        i--;
      }
      assertEquals(1, i);
    }

    @Test
    void tenMergeEmptyBlock() {
      putTen();
      BBuffer<String> other = newBlock();
      assertEquals(10, sb.merge(other).getNumberOfElements());
    }

    @Test
    void tenMergeNonEmptyBlock() {
      assertThrows(BufferOverflowException.class, () -> {
        putTen();
        BBuffer<String> other = newBlock();
        other.insert("newItem");
        assertEquals(10, sb.merge(other).getNumberOfElements());
      });
    }

    @Test
    void tenReopen() {
      putTen();
      ByteBuffer bytes = sb.getBlock();
      BBuffer<String> other = new BBuffer<>(bytes, new StringCodec());
      assertEquals(10, other.getNumberOfElements());
    }

    @Test
    void tenSort() {
      //putTen();
      for (String w : words) {
        sb.insertUnsorted(w);
      }
      sb.sort(false);
      Iterator<String> iter = sb.iterator();
      int i = 0;
      while (iter.hasNext()) {
        assertEquals(ascend[i], iter.next());
        i++;
      }
    }

    @Test
    void tenSplitBlock() {
      putTen();
      BBuffer<String> nb = sb.split();
      assertEquals(5, sb.getNumberOfElements());
      assertEquals(5, nb.getNumberOfElements());
    }
  }

}
