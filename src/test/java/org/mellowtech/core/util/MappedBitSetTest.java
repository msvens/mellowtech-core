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

package org.mellowtech.core.util;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.LongBuffer;
import java.util.*;

/**
 * @author msvens
 * @since 02/05/16
 */
@DisplayName("A Mapped BitSet")
class MappedBitSetTest {

  private static int maxbits = 64*2;

  int[] numsToSet = new int[64];
  int nextNum;

  @BeforeEach
  void initNums(){
    int j = 0;
    for(int i = 1; i < maxbits; i+=2){
      numsToSet[j++] = i;
    }
    nextNum = 64;
  }


  private MappedBitSet createEmptySet(){
    return new MappedBitSet(maxbits);
  }

  private MappedBitSet createFull(){
    MappedBitSet mbs = createEmptySet();
    for(int i = 0; i < maxbits; i++)
      mbs.set(i);
    return mbs;
  }

  private MappedBitSet createHalfFull(){
    MappedBitSet mbs = createEmptySet();
    for(Integer i : numsToSet){
      mbs.set(i);
    }
    return mbs;
  }

  private MappedBitSet reopen(MappedBitSet bs){
    LongBuffer lb = bs.getBuffer();
    return new MappedBitSet(lb);
  }

  private int lastNum(){
    return numsToSet[numsToSet.length-1];
  }

  private int firstNum(){
    return numsToSet[0];
  }



  @Nested
  @DisplayName("When empty")
  class Empty {
    @Test
    void emptyLength() {
      MappedBitSet bs = createEmptySet();
      assertEquals(0, bs.length());
    }

    @Test
    void emptyCardinality() {
      MappedBitSet bs = createEmptySet();
      assertEquals(0, bs.cardinality());
    }

    @Test
    void emptyNextSetBit() {
      MappedBitSet bs = createEmptySet();
      assertEquals(-1, bs.nextSetBit(0));
    }

    @Test
    void emptyPreviousSetBit() {
      MappedBitSet bs = createEmptySet();
      assertEquals(-1, bs.previousSetBit(bs.capacity()));
    }

    @Test
    void emptyNextClearBit() {
      MappedBitSet bs = createEmptySet();
      assertEquals(0, bs.previousClearBit(0));
    }

    @Test
    void emptyPreviousClearBit() {
      MappedBitSet bs = createEmptySet();
      assertEquals(bs.capacity() - 1, bs.previousClearBit(bs.capacity()));
    }

    @Test
    void emptyContains() {
      MappedBitSet bs = createEmptySet();
      assertFalse(bs.contains(0));
    }

    @Test
    void emptyCapacity() {
      MappedBitSet bs = createEmptySet();
      assertEquals(64 * 2, bs.capacity());
    }

    @Test
    void emptyClearAll() {
      MappedBitSet bs = createEmptySet();
      bs.clear();
      assertEquals(0, bs.cardinality());
    }

    @Test
    void emptyFlip() {
      MappedBitSet bs = createEmptySet();
      bs.flip(0);
      assertTrue(bs.contains(0));
    }

    @Test
    void emptySet() {
      MappedBitSet bs = createEmptySet();
      bs.set(0);
      assertTrue(bs.contains(0));
    }

    @Test
    void emptyClear() {
      MappedBitSet bs = createEmptySet();
      bs.clear(0);
      assertFalse(bs.contains(0));
    }

    @Test
    void emptyIterator() {
      MappedBitSet bs = createEmptySet();
      Iterator<Integer> iter = bs.iterator();
      assertFalse(iter.hasNext());
    }

    @Test
    void emptyReopen() {
      MappedBitSet bs = reopen(createEmptySet());
      assertFalse(bs.contains(0));
    }
  }



  @Nested
  @DisplayName("When half full")
  class HalfFull {
    @Test
    void halfLength() {
      MappedBitSet bs = createHalfFull();
      assertEquals(lastNum(), bs.length() - 1);
    }

    @Test
    void halfCardinality() {
      MappedBitSet bs = createHalfFull();
      assertEquals(numsToSet.length, bs.cardinality());
    }

    @Test
    void halfNextSetBit() {
      MappedBitSet bs = createHalfFull();
      assertEquals(firstNum(), bs.nextSetBit(0));
    }


    @Test
    void halfPreviousSetBit() {
      MappedBitSet bs = createHalfFull();
      assertEquals(lastNum(), bs.previousSetBit(bs.capacity()));
    }


    @Test
    void halfNextClearBit() {
      MappedBitSet bs = createHalfFull();
      assertEquals(firstNum() + 1, bs.nextClearBit(firstNum()));
    }


    @Test
    void halfPreviousClearBit() {
      MappedBitSet bs = createHalfFull();
      assertEquals(lastNum() - 1, bs.previousClearBit(lastNum()));
    }


    @Test
    void halfContains() {
      MappedBitSet bs = createHalfFull();
      for (int i = 1; i < maxbits; i += 2)
        assertTrue(bs.contains(i));
    }


    @Test
    void halfCapacity() {
      MappedBitSet bs = createHalfFull();
      assertEquals(maxbits, bs.capacity());
    }


    @Test
    void halfClearAll() {
      MappedBitSet bs = createHalfFull();
      bs.clear();
      assertEquals(0, bs.cardinality());
    }


    @Test
    void halfFlip() {
      MappedBitSet bs = createHalfFull();
      bs.flip(firstNum());
      assertFalse(bs.contains(firstNum()));
    }


    @Test
    void halfSet() {
      MappedBitSet bs = createHalfFull();
      bs.set(nextNum);
      assertTrue(bs.contains(nextNum));
    }


    @Test
    void halfClear() {
      MappedBitSet bs = createHalfFull();
      bs.clear(firstNum());
      assertFalse(bs.contains(firstNum()));
    }


    @Test
    void halfIterator() {
      MappedBitSet bs = createHalfFull();
      int i = 1;
      for (Integer n : bs) {
        assertEquals(i, (int) n);
        i += 2;
      }
      assertEquals(i - 2, lastNum());
    }

    @Test
    void halfReopen() {
      MappedBitSet bs = reopen(createHalfFull());
      for (int i = 1; i < maxbits; i += 2)
        assertTrue(bs.contains(i));
    }
  }


  @Nested
  @DisplayName("When full")
  class Full {
    @Test
    void fullLength() {
      MappedBitSet bs = createFull();
      assertEquals(maxbits, bs.length());
    }


    @Test
    void fullCardinality() {
      MappedBitSet bs = createFull();
      assertEquals(maxbits, bs.cardinality());
    }


    @Test
    void fullNextSetBit() {
      MappedBitSet bs = createFull();
      assertEquals(0, bs.nextSetBit(0));
    }


    @Test
    void fullPreviousSetBit() {
      MappedBitSet bs = createFull();
      assertEquals(maxbits - 1, bs.previousSetBit(bs.capacity()));
    }


    @Test
    void fullNextClearBit() {
      MappedBitSet bs = createFull();
      assertEquals(-1, bs.nextClearBit(0));
    }


    @Test
    void fullPreviousClearBit() {
      MappedBitSet bs = createFull();
      assertEquals(-1, bs.previousClearBit(127));
    }


    @Test
    void fullContains() {
      MappedBitSet bs = createFull();
      for (int i = 1; i < maxbits; i++)
        assertTrue(bs.contains(i));
    }


    @Test
    void fullCapacity() {
      MappedBitSet bs = createFull();
      assertEquals(maxbits, bs.capacity());
    }


    @Test
    void fullClearAll() {
      MappedBitSet bs = createFull();
      bs.clear();
      assertEquals(0, bs.cardinality());
    }


    @Test
    void fullFlip() {
      MappedBitSet bs = createFull();
      bs.flip(64);
      assertFalse(bs.contains(64));
    }


    @Test
    void fullSet() {
      MappedBitSet bs = createFull();
      bs.set(nextNum);
      assertTrue(bs.contains(nextNum));
    }


    @Test
    void fullClear() {
      MappedBitSet bs = createFull();
      bs.clear(firstNum());
      assertFalse(bs.contains(firstNum()));
    }


    @Test
    void fullIterator() {
      MappedBitSet bs = createFull();
      int i = 0;
      for (Integer n : bs) {
        assertEquals(i, (int) n);
        i++;
      }
      assertEquals(i - 1, lastNum());
    }

    @Test
    void fullReopen() {
      MappedBitSet bs = reopen(createFull());
      for (int i = 1; i < maxbits; i++)
        assertTrue(bs.contains(i));
    }
  }






}
