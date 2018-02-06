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

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mellowtech.core.collections.BTree;
import org.mellowtech.core.collections.KeyValue;

import java.io.IOException;
import java.util.*;

/**
 * Created by msvens on 01/11/15.
 */
abstract class BTreeTemplate extends BMapTemplate{

  BTree<String, Integer> btree(){
    return (BTree<String,Integer>) tree;
  }

  @Nested
  @DisplayName("A btree with zero keys")
  class BTreeZero {
    @Test
    void zeroIteratorRangeInclusive() throws Exception {
      assertFalse(btree().iterator(false, ascend[0], true, ascend[9], true).hasNext());
    }

    @Test
    void zeroIteratorRangeExclusive() throws Exception {
      assertFalse(btree().iterator(false, ascend[0], false, ascend[9], false).hasNext());
    }

    @Test
    void zeroReverseIterator() throws Exception {
      assertFalse(btree().iterator(true).hasNext());
    }

    @Test
    void zeroReverseIteratorRangeInclusive() throws Exception {
      assertFalse(btree().iterator(true, ascend[9], true, ascend[0], true).hasNext());
    }

    @Test
    void zeroReverseIteratorRangeExclusive() throws Exception {
      assertFalse(btree().iterator(true, ascend[9], false, ascend[0], false).hasNext());
    }
    
    @Test
    void emptyGetKey() throws IOException {
      assertThrows(IOException.class, () -> {
        btree().getKey(0);
      });
    }

    @Test
    void emptyGetPosition() throws IOException {
      assertNull(btree().getPosition(firstWord));
    }

    @Test
    void emptyGetPositionWithMissing() throws IOException {
      assertEquals(0, btree().getPositionWithMissing(firstWord).getSmaller());
    }
  }


  @Nested
  @DisplayName("A btree with one key")
  class BTreeOne {
    @Test
    void oneIterator() throws Exception {
      onePut();
      int i = 0;
      Iterator<KeyValue<String, Integer>> iter = tree.iterator();
      while (iter.hasNext()) {
        assertEquals(ascend[0], iter.next().getKey());
        i++;
      }
      assertEquals(1, i);
    }

    @Test
    void oneIteratorRangeInclusive() throws Exception {
      onePut();
      Iterator<KeyValue<String, Integer>> iter = btree().iterator(false, ascend[0], true, ascend[9], true);
      assertTrue(iter.hasNext());
    }

    @Test
    void oneIteratorRangeExclusive() throws Exception {
      onePut();
      Iterator<KeyValue<String, Integer>> iter = btree().iterator(false, ascend[0], false, ascend[9], false);
      assertFalse(iter.hasNext());
    }

    @Test
    void oneReverseIterator() throws Exception {
      onePut();
      int i = 0;
      Iterator<KeyValue<String, Integer>> iter = btree().iterator(true);
      while (iter.hasNext()) {
        assertEquals(ascend[i], iter.next().getKey());
        i--;
      }
      assertEquals(-1, i);
    }

    @Test
    void oneReverseIteratorRangeInclusive() throws Exception {
      onePut();
      Iterator<KeyValue<String, Integer>> iter = btree().iterator(true, ascend[9], true, ascend[0], true);
      assertTrue(iter.hasNext());
    }

    @Test
    void oneReverseIteratorRangeExclusive() throws Exception {
      onePut();
      Iterator<KeyValue<String, Integer>> iter = btree().iterator(true, ascend[9], false, ascend[0], false);
      assertFalse(iter.hasNext());
    }

    @Test
    void oneGetKey() throws IOException {
      tree.put(firstWord, val(firstWord));
      assertEquals(firstWord, btree().getKey(0));
    }

    @Test
    void oneGetPosition() throws IOException {
      tree.put(firstWord, val(firstWord));
      assertNotNull(btree().getPosition(firstWord));
    }

    @Test
    void oneGetPositionWithMissing() throws IOException {
      tree.put(firstWord, val(firstWord));
      assertEquals(0, btree().getPositionWithMissing(firstWord).getSmaller());
    }
  }
  

  @Nested
  @DisplayName("A btree with ten keys")
  class BTreeTen {
    @Test
    void tenIterator() throws Exception {
      tenPut();
      int i = 0;
      Iterator<KeyValue<String, Integer>> iter = tree.iterator();
      while (iter.hasNext()) {
        assertEquals(ascend[i], iter.next().getKey());
        i++;
      }
      assertEquals(10, i);
    }

    @Test
    void tenIteratorRangeInclusive() throws Exception {
      tenPut();
      int i = 1;
      Iterator<KeyValue<String, Integer>> iter = btree().iterator(false, ascend[1], true, ascend[8], true);
      while (iter.hasNext()) {
        assertEquals(ascend[i], iter.next().getKey());
        i++;
      }
      assertEquals(9, i);
    }

    @Test
    void tenIteratorRangeExclusive() throws Exception {
      tenPut();
      int i = 2;
      Iterator<KeyValue<String, Integer>> iter = btree().iterator(false, ascend[1], false, ascend[8], false);
      while (iter.hasNext()) {
        assertEquals(ascend[i], iter.next().getKey());
        i++;
      }
      assertEquals(8, i);
    }

    @Test
    void tenReverseIterator() throws Exception {
      tenPut();
      int i = 9;
      Iterator<KeyValue<String, Integer>> iter = btree().iterator(true);
      while (iter.hasNext()) {
        assertEquals(ascend[i], iter.next().getKey());
        i--;
      }
      assertEquals(-1, i);
    }

    @Test
    void tenReverseIteratorRangeInclusive() throws Exception {
      tenPut();
      int i = 8;
      Iterator<KeyValue<String, Integer>> iter = btree().iterator(true, ascend[8], true, ascend[1], true);
      while (iter.hasNext()) {
        assertEquals(ascend[i], iter.next().getKey());
        i--;
      }
      assertEquals(0, i);
    }

    @Test
    void tenReverseIteratorRangeExclusive() throws Exception {
      tenPut();
      int i = 7;
      Iterator<KeyValue<String, Integer>> iter = btree().iterator(true, ascend[8], false, ascend[1], false);
      while (iter.hasNext()) {
        assertEquals(ascend[i], iter.next().getKey());
        i--;
      }
      assertEquals(1, i);
    }

    @Test
    void tenGetKey() throws IOException {
      tenPut();
      assertEquals(firstWord, btree().getKey(0));
    }

    @Test
    void tenGetPosition() throws IOException {
      tenPut();
      assertNotNull(btree().getPosition(firstWord));
    }

    @Test
    void tenGetPositionWithMissing() throws IOException {
      tenPut();
      assertEquals(3, btree().getPositionWithMissing(forthWord).getSmaller());
    }
  }

  @Nested
  @DisplayName("A btree with many keys")
  class BTreeMany {
    @Test
    void manyCreateIndex() throws IOException {
      manyPut();
      tree.save();
      try {
        btree().rebuildIndex();
      } catch (UnsupportedOperationException uoe) {
        return;
      }
    }


    @Test
    void manyIterator() throws Exception {
      manyPut();
      int from = 0;
      int to = mAscend.length - 1;
      Iterator<KeyValue<String, Integer>> iter = tree.iterator();
      while (iter.hasNext()) {
        assertEquals(mAscend[from], iter.next().getKey());
        from++;
      }
      from--;
      assertEquals(from, to);
    }

    @Test
    void manyIteratorRangeInclusive() throws Exception {
      manyPut();
      int from = 50;
      int to = mAscend.length - 50;
      Iterator<KeyValue<String, Integer>> iter = btree().iterator(false, mAscend[from], true, mAscend[to], true);
      while (iter.hasNext()) {
        assertEquals(mAscend[from], iter.next().getKey());
        from++;
      }
      from--;
      assertEquals(to, from);
    }

    @Test
    void manyIteratorRangeExclusive() throws Exception {
      manyPut();
      int from = 51;
      int to = mAscend.length - 51;
      Iterator<KeyValue<String, Integer>> iter = btree().iterator(false, mAscend[from - 1], false, mAscend[to + 1], false);
      while (iter.hasNext()) {
        assertEquals(mAscend[from], iter.next().getKey());
        from++;
      }
      from--;
      assertEquals(to, from);
    }

    @Test
    void manyReverseIterator() throws Exception {
      manyPut();
      int from = mAscend.length - 1;
      int to = 0;
      Iterator<KeyValue<String, Integer>> iter = btree().iterator(true);
      while (iter.hasNext()) {
        assertEquals(mAscend[from], iter.next().getKey());
        from--;
      }
      from++;
      assertEquals(to, from);
    }

    @Test
    void manyReverseIteratorRangeInclusive() throws Exception {
      manyPut();
      int from = mAscend.length - 50;
      int to = 50;
      Iterator<KeyValue<String, Integer>> iter = btree().iterator(true, mAscend[from], true, mAscend[to], true);
      while (iter.hasNext()) {
        assertEquals(mAscend[from], iter.next().getKey());
        from--;
      }
      from++;
      assertEquals(to, from);
    }

    @Test
    void manyReverseIteratorRangeExclusive() throws Exception {
      manyPut();
      int from = mAscend.length - 51;
      int to = 51;

      Iterator<KeyValue<String, Integer>> iter = btree().iterator(true, mAscend[from + 1], false, mAscend[to - 1], false);
      while (iter.hasNext()) {
        assertEquals(mAscend[from], iter.next().getKey());
        from--;
      }
      from++;
      if (from != to) {
        //print many tre...
      }
      assertEquals(from, to);
    }

    @Test
    void manyGetKey() throws IOException {
      manyPut();
      TreeMap<String, Integer> m = getManyTree();
      assertEquals(m.firstKey(), btree().getKey(0));
    }

    @Test
    void manyGetPosition() throws IOException {
      manyPut();
      TreeMap<String, Integer> m = getManyTree();
      assertEquals(0, btree().getPosition(m.firstKey()).getSmaller());
      assertEquals(m.size() - 1, btree().getPosition(m.lastKey()).getSmaller());
    }

    @Test
    void manyGetPositionWithMissing() throws IOException {
      manyPut();
      assertEquals(0, btree().getPositionWithMissing(manySmaller).getSmaller());
      assertEquals(tree.size(), btree().getPositionWithMissing(manyLarger).getSmaller());
    }
  }


}
