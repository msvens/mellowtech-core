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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.LongBuffer;
import java.util.*;

/**
 * @author msvens
 * @since 02/05/16
 */
public class MappedBitSetTest {

  private static int maxbits = 64*2;

  public int[] numsToSet = new int[64];
  public int nextNum;

  @Before public void initNums(){
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



  @Test public void emptyLength(){
    MappedBitSet bs = createEmptySet();
    Assert.assertEquals(0, bs.length());
  }

  @Test public void emptyCardinality(){
    MappedBitSet bs = createEmptySet();
    Assert.assertEquals(0, bs.cardinality());
  }

  @Test public void emptyNextSetBit(){
    MappedBitSet bs = createEmptySet();
    Assert.assertEquals(-1, bs.nextSetBit(0));
  }

  @Test public void emptyPreviousSetBit(){
    MappedBitSet bs = createEmptySet();
    Assert.assertEquals(-1, bs.previousSetBit(bs.capacity()));
  }

  @Test public void emptyNextClearBit(){
    MappedBitSet bs = createEmptySet();
    Assert.assertEquals(0, bs.previousClearBit(0));
  }

  @Test public void emptyPreviousClearBit(){
    MappedBitSet bs = createEmptySet();
    Assert.assertEquals(bs.capacity()-1, bs.previousClearBit(bs.capacity()));
  }

  @Test public void emptyContains(){
    MappedBitSet bs = createEmptySet();
    Assert.assertFalse(bs.contains(0));
  }

  @Test public void emptyCapacity(){
    MappedBitSet bs = createEmptySet();
    Assert.assertEquals(64*2, bs.capacity());
  }

  @Test public void emptyClearAll(){
    MappedBitSet bs = createEmptySet();
    bs.clear();
    Assert.assertEquals(0, bs.cardinality());
  }

  @Test public void emptyFlip(){
    MappedBitSet bs = createEmptySet();
    bs.flip(0);
    Assert.assertTrue(bs.contains(0));
  }

  @Test public void emptySet(){
    MappedBitSet bs = createEmptySet();
    bs.set(0);
    Assert.assertTrue(bs.contains(0));
  }

  @Test public void emptyClear(){
    MappedBitSet bs = createEmptySet();
    bs.clear(0);
    Assert.assertFalse(bs.contains(0));
  }

  @Test public void emptyIterator(){
    MappedBitSet bs = createEmptySet();
    Iterator<Integer> iter = bs.iterator();
    Assert.assertFalse(iter.hasNext());
  }

  @Test public void emptyReopen(){
    MappedBitSet bs = reopen(createEmptySet());
    emptyContains();
  }


  //HALF FULL

  @Test public void halfLength(){
    MappedBitSet bs = createHalfFull();
    Assert.assertEquals(lastNum(), bs.length()-1);
  }

  @Test public void halfCardinality(){
    MappedBitSet bs = createHalfFull();
    Assert.assertEquals(numsToSet.length, bs.cardinality());
  }

  @Test public void halfNextSetBit(){
    MappedBitSet bs = createHalfFull();
    Assert.assertEquals(firstNum(), bs.nextSetBit(0));
  }


  @Test public void halfPreviousSetBit(){
    MappedBitSet bs = createHalfFull();
    Assert.assertEquals(lastNum(), bs.previousSetBit(bs.capacity()));
  }


  @Test public void halfNextClearBit(){
    MappedBitSet bs = createHalfFull();
    Assert.assertEquals(firstNum()+1, bs.nextClearBit(firstNum()));
  }


  @Test public void halfPreviousClearBit(){
    MappedBitSet bs = createHalfFull();
    Assert.assertEquals(lastNum()-1, bs.previousClearBit(lastNum()));
  }


  @Test public void halfContains(){
    MappedBitSet bs = createHalfFull();
    for(int i = 1; i < maxbits; i+=2)
      Assert.assertTrue(bs.contains(i));
  }


  @Test public void halfCapacity(){
    MappedBitSet bs = createHalfFull();
    Assert.assertEquals(maxbits, bs.capacity());
  }


  @Test public void halfClearAll(){
    MappedBitSet bs = createHalfFull();
    bs.clear();
    Assert.assertEquals(0, bs.cardinality());
  }


  @Test public void halfFlip(){
    MappedBitSet bs = createHalfFull();
    bs.flip(firstNum());
    Assert.assertFalse(bs.contains(firstNum()));
  }


  @Test public void halfSet(){
    MappedBitSet bs = createHalfFull();
    bs.set(nextNum);
    Assert.assertTrue(bs.contains(nextNum));
  }


  @Test public void halfClear(){
    MappedBitSet bs = createHalfFull();
    bs.clear(firstNum());
    Assert.assertFalse(bs.contains(firstNum()));
  }


  @Test public void halfIterator(){
    MappedBitSet bs = createHalfFull();
    int i = 1;
    for(Integer n : bs){
      Assert.assertEquals(i, (int) n);
      i += 2;
    }
    Assert.assertEquals(i-2, lastNum());
  }

  @Test public void halfReopen(){
    MappedBitSet bs = reopen(createHalfFull());
    halfContains();
  }

  //FULL

  @Test public void fullLength(){
    MappedBitSet bs = createFull();
    Assert.assertEquals(maxbits, bs.length());
  }


  @Test public void fullCardinality(){
    MappedBitSet bs = createFull();
    Assert.assertEquals(maxbits, bs.cardinality());
  }


  @Test public void fullNextSetBit(){
    MappedBitSet bs = createFull();
    Assert.assertEquals(0, bs.nextSetBit(0));
  }


  @Test public void fullPreviousSetBit(){
    MappedBitSet bs = createFull();
    Assert.assertEquals(maxbits-1, bs.previousSetBit(bs.capacity()));
  }


  @Test public void fullNextClearBit(){
    MappedBitSet bs = createFull();
    Assert.assertEquals(-1, bs.nextClearBit(0));
  }


  @Test public void fullPreviousClearBit(){
    MappedBitSet bs = createFull();
    Assert.assertEquals(-1, bs.previousClearBit(127));
  }


  @Test public void fullContains(){
    MappedBitSet bs = createFull();
    for(int i = 1; i < maxbits; i++)
      Assert.assertTrue(bs.contains(i));
  }


  @Test public void fullCapacity(){
    MappedBitSet bs = createFull();
    Assert.assertEquals(maxbits, bs.capacity());
  }


  @Test public void fullClearAll(){
    MappedBitSet bs = createFull();
    bs.clear();
    Assert.assertEquals(0, bs.cardinality());
  }


  @Test public void fullFlip(){
    MappedBitSet bs = createFull();
    bs.flip(64);
    Assert.assertFalse(bs.contains(64));
  }


  @Test public void fullSet(){
    MappedBitSet bs = createFull();
    bs.set(nextNum);
    Assert.assertTrue(bs.contains(nextNum));
  }


  @Test public void fullClear(){
    MappedBitSet bs = createFull();
    bs.clear(firstNum());
    Assert.assertFalse(bs.contains(firstNum()));
  }


  @Test public void fullIterator(){
    MappedBitSet bs = createFull();
    int i = 0;
    for(Integer n : bs){
      Assert.assertEquals(i, (int) n);
      i++;
    }
    Assert.assertEquals(i-1, lastNum());
  }

  @Test public void fullReopen(){
    MappedBitSet bs = reopen(createFull());
    fullContains();
  }






}
