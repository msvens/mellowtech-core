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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mellowtech.core.collections.SortedDiscMap;
import org.mellowtech.core.util.MapEntry;

import java.util.*;
import java.util.Map.Entry;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by msvens on 11/11/15.
 */
abstract class SortedDiscMapTemplate extends DiscMapTemplate {


  SortedDiscMap<String, Integer> sdm() {
    return (SortedDiscMap<String, Integer>) map;
  }

  ;

  void verifyKeyIter(String[] words, Iterator<String> iter) {
    int i = 0;
    while (iter.hasNext()) {
      assertEquals(words[i], iter.next());
      i++;
    }
    assertEquals(words.length, i);
  }

  void verifyEntryIter(String[] words, Iterator<Map.Entry<String, Integer>> iter) {
    int i = 0;
    while (iter.hasNext()) {
      assertEquals(new MapEntry(words[i], words[i].length()), iter.next());
      i++;
    }
    assertEquals(words.length, i);
  }


  @Nested
  @DisplayName("A sorted map with no key")
  class SDMZero {
    @Test
    void zeroIteratorFrom() {
      assertFalse(sdm().iterator(swords[0]).hasNext());
    }

    @Test
    void zeroCeilingEntry() {
      assertNull(sdm().ceilingEntry(swords[0]));
    }

    @Test
    void zeroCeilingKey() {
      assertNull(sdm().ceilingKey(swords[0]));
    }

    @Test
    void zeroDescendingKeySet() {
      assertTrue(sdm().descendingKeySet().isEmpty());
    }

    @Test
    void zeroDescendingMap() {
      assertTrue(sdm().descendingMap().isEmpty());
    }

    @Test
    void zeroFirstEntry() {
      assertNull(sdm().firstEntry());
    }

    @Test
    void zeroFloorEntry() {
      assertNull(sdm().floorEntry(swords[0]));
    }

    @Test
    void zeroFloorKey() {
      assertNull(sdm().floorKey(swords[0]));
    }

    @Test
    void zeroHeadMap() {
      SortedMap<String, Integer> hm = sdm().headMap(swords[0]);
      assertTrue(sdm().headMap(swords[0]).isEmpty());
    }

    @Test
    void zeroHeadMapInclusive() {
      assertTrue(sdm().headMap(swords[0], true).isEmpty());
    }

    @Test
    void zeroHigherEntry() {
      assertNull(sdm().higherEntry(swords[0]));
    }

    @Test
    void zeroHigherKey() {
      assertNull(sdm().higherKey(swords[0]));
    }

    @Test
    void zeroLastEntry() {
      assertNull(sdm().lastEntry());
    }

    @Test
    void zeroLowerEntry() {
      assertNull(sdm().lowerEntry(swords[3]));
    }

    @Test
    void zeroLowerKey() {
      assertNull(sdm().lowerKey(swords[3]));
    }

    @Test
    void zeroNavigableKeySet() {
      assertTrue(sdm().navigableKeySet().isEmpty());
    }

    @Test
    void zeroPollFirstEntry() {
      assertNull(sdm().pollFirstEntry());
    }

    @Test
    void zeroPollLastEntry() {
      assertNull(sdm().pollLastEntry());
    }

    @Test
    void zeroSubMap() {
      assertTrue(sdm().subMap(swords[0], swords[3]).isEmpty());
    }

    @Test
    void zeroSubMapInclusive() {
      assertTrue(sdm().subMap(swords[0], true, swords[3], true).isEmpty());
    }

    @Test
    void zeroTailMap() {
      assertTrue(sdm().tailMap(swords[0]).isEmpty());
    }

    @Test
    void zeroTailMapInclusive() {
      assertTrue(sdm().tailMap(swords[0], true).isEmpty());
    }

    @Test
    void oneCeilingEntry() {
      onePut();
      assertEquals(new MapEntry<>(swords[0], swords[0].length()), sdm().ceilingEntry(swords[0]));
    }
  }

  @Nested
  @DisplayName("A sorted map with one key")
  class SDMOne {
    @Test
    void oneIteratorFrom() {
      onePut();
      assertTrue(sdm().iterator(swords[0]).hasNext());
    }

    @Test
    void oneCeilingKey() {
      onePut();
      assertEquals(swords[0], sdm().ceilingKey(swords[0]));
    }

    @Test
    void oneDescendingKeySet() {
      onePut();
      NavigableSet<String> ks = sdm().descendingKeySet();
      assertEquals(1, ks.size());
      assertEquals(swords[0], ks.last());
      assertEquals(swords[0], ks.first());
    }

    @Test
    void oneDescendingMap() {
      onePut();
      NavigableMap<String, Integer> nm = sdm().descendingMap();
      assertEquals(1, nm.size());
      assertEquals(swords[0], nm.firstKey());
      assertEquals(swords[0], nm.lastKey());
    }

    @Test
    void oneFirstEntry() {
      onePut();
      assertEquals(swords[0], sdm().firstEntry().getKey());
    }

    @Test
    void oneFloorEntry() {
      onePut();
      assertEquals(swords[0], sdm().floorEntry(swords[0]).getKey());
    }

    @Test
    void oneFloorKey() {
      onePut();
      assertEquals(swords[0], sdm().floorKey(swords[0]));
    }

    @Test
    void oneHeadMap() {
      onePut();
      SortedMap<String, Integer> hm = sdm().headMap(swords[0]);
      assertEquals(0, hm.size());
    }

    @Test
    void oneHeadMapInclusive() {
      onePut();
      assertEquals(1, sdm().headMap(swords[0], true).size());
    }

    @Test
    void oneHigherEntry() {
      onePut();
      assertNull(sdm().higherEntry(swords[0]));
    }

    @Test
    void oneHigherKey() {
      onePut();
      assertNull(sdm().higherKey(swords[0]));
    }

    @Test
    void oneLastEntry() {
      onePut();
      assertEquals(swords[0], sdm().lastEntry().getKey());
    }

    @Test
    void oneLowerEntry() {
      onePut();
      assertNull(sdm().lowerEntry(swords[0]));
    }

    @Test
    void oneLowerKey() {
      onePut();
      assertNull(sdm().lowerKey(swords[0]));
    }

    @Test
    void oneNavigableKeySet() {
      onePut();
      assertEquals(1, sdm().navigableKeySet().size());
    }

    @Test
    void onePollFirstEntry() {
      onePut();
      assertNotNull(sdm().pollFirstEntry());
      assertEquals(0, sdm().size());
    }

    @Test
    void onePollLastEntry() {
      onePut();
      assertNotNull(sdm().pollLastEntry());
      assertEquals(0, sdm().size());
    }

    @Test
    void oneSubMap() {
      onePut();
      assertFalse(sdm().subMap(swords[0], swords[3]).isEmpty());
    }

    @Test
    void oneSubMapInclusive() {
      onePut();
      assertFalse(sdm().subMap(swords[0], true, swords[3], true).isEmpty());
    }

    @Test
    void oneTailMap() {
      onePut();
      assertEquals(1, sdm().tailMap(swords[0]).size());
    }

    @Test
    void oneTailMapExclusive() {
      onePut();
      assertEquals(0, sdm().tailMap(swords[0], false).size());
    }
  }

  @Nested
  @DisplayName("A sorted map with 10 keys")
  class SDMTen {
    @Test
    void tenIteratorFrom() {
      tenPut();
      int tot = 0;
      Iterator iter = sdm().iterator(swords[3]);
      while (iter.hasNext()) {
        tot++;
        iter.next();
      }
      assertEquals(7, tot);
    }

    @Test
    void tenCeilingEntry() {
      tenPut();
      assertEquals(new MapEntry<>(swords[3], swords[0].length()), sdm().ceilingEntry(treeHalfWord));
      assertEquals(new MapEntry<>(swords[3], swords[0].length()), sdm().ceilingEntry(swords[3]));
    }

    @Test
    void tenCeilingKey() {
      tenPut();
      assertEquals(swords[3], sdm().ceilingKey(treeHalfWord));
      assertEquals(swords[3], sdm().ceilingKey(swords[3]));
    }

    @Test
    void tenDescendingKeySet() {
      tenPut();
      NavigableSet<String> ks = sdm().descendingKeySet();
      assertEquals(swords.length, ks.size());
      assertEquals(swords[swords.length - 1], ks.first());
      assertEquals(swords[0], ks.last());
      verifyKeyIter(rwords, ks.iterator());
    }

    @Test
    void tenDescendingMap() {
      tenPut();
      NavigableMap<String, Integer> nm = sdm().descendingMap();
      assertEquals(swords.length, nm.size());
      assertEquals(swords[swords.length - 1], nm.firstKey());
      assertEquals(swords[0], nm.lastKey());
      verifyKeyIter(rwords, nm.keySet().iterator());
    }

    @Test
    void tenFirstEntry() {
      tenPut();
      assertEquals(swords[0], sdm().firstEntry().getKey());
    }

    @Test
    void tenFloorEntry() {
      tenPut();
      assertEquals(new MapEntry<>(swords[3], swords[0].length()), sdm().floorEntry(fourHalfWord));
      assertEquals(new MapEntry<>(swords[3], swords[0].length()), sdm().floorEntry(swords[3]));
    }

    @Test
    void tenFloorKey() {
      tenPut();
      assertEquals(swords[3], sdm().floorKey(fourHalfWord));
      assertEquals(swords[3], sdm().floorKey(swords[3]));
    }

    @Test
    void tenHeadMap() {
      tenPut();
      SortedMap<String, Integer> subMap = sdm().headMap(swords[3]);
      assertEquals(swords[0], subMap.firstKey());
      assertEquals(swords[2], subMap.lastKey());
      assertEquals(3, subMap.size());
    }

    @Test
    void tenHeadMapInclusive() {
      tenPut();
      SortedMap<String, Integer> subMap = sdm().headMap(swords[3], true);
      assertEquals(swords[0], subMap.firstKey());
      assertEquals(swords[3], subMap.lastKey());
      assertEquals(4, subMap.size());
    }

    @Test
    void tenHigherEntry() {
      tenPut();
      assertEquals(swords[9], sdm().higherEntry("j").getKey());
      assertNull(sdm().higherEntry(swords[9]));
    }

    @Test
    void tenHigherKey() {
      tenPut();
      assertEquals(swords[9], sdm().higherKey("j"));
      assertNull(sdm().higherKey(swords[9]));
    }

    @Test
    void tenLastEntry() {
      tenPut();
      assertEquals(swords[9], sdm().lastEntry().getKey());
    }

    @Test
    void tenLowerEntry() {
      tenPut();
      assertEquals(swords[0], sdm().lowerEntry("b").getKey());
      assertNull(sdm().lowerEntry(swords[0]));
    }

    @Test
    void tenLowerKey() {
      tenPut();
      assertEquals(swords[0], sdm().lowerKey("b"));
      assertNull(sdm().lowerKey(swords[0]));
    }

    @Test
    void tenNavigableKeySet() {
      tenPut();
      assertEquals(10, sdm().navigableKeySet().size());
    }

    @Test
    void tenPollFirstEntry() {
      tenPut();
      assertNotNull(sdm().pollFirstEntry());
      assertEquals(9, sdm().size());
    }

    @Test
    void tenPollLastEntry() {
      tenPut();
      assertNotNull(sdm().pollLastEntry());
      assertEquals(9, sdm().size());
    }

    @Test
    void tenSubMap() {
      tenPut();
      SortedMap<String, Integer> subMap = sdm().subMap(swords[0], swords[3]);
      assertEquals(swords[0], subMap.firstKey());
      assertEquals(swords[2], subMap.lastKey());
      assertEquals(3, subMap.size());
    }

    @Test
    void tenSubMapInclusive() {
      tenPut();
      SortedMap<String, Integer> subMap = sdm().subMap(swords[0], true, swords[3], true);
      assertEquals(swords[0], subMap.firstKey());
      assertEquals(swords[3], subMap.lastKey());
      assertEquals(4, subMap.size());
    }

    @Test
    void tenSubMapExclusive() {
      tenPut();
      SortedMap<String, Integer> subMap = sdm().subMap(swords[0], false, swords[3], false);
      assertEquals(swords[1], subMap.firstKey());
      assertEquals(swords[2], subMap.lastKey());
      assertEquals(2, subMap.size());
    }

    @Test
    void tenTailMap() {
      tenPut();
      SortedMap<String, Integer> subMap = sdm().tailMap(swords[3]);
      assertEquals(swords[3], subMap.firstKey());
      assertEquals(swords[9], subMap.lastKey());
      assertEquals(7, subMap.size());
    }

    @Test
    void tenTailMapExclusive() {
      tenPut();
      SortedMap<String, Integer> subMap = sdm().tailMap(swords[3], false);
      assertEquals(swords[4], subMap.firstKey());
      assertEquals(swords[9], subMap.lastKey());
      assertEquals(6, subMap.size());
    }
  }

  @Nested
  @DisplayName("A sorted map with many keys")
  class SDMMany {
    @Test
    void manyIteratorFrom() {
      manyPut();
      int item = 10;
      String key = mwSort[item];
      Iterator<Entry<String, Integer>> iter = sdm().iterator(key);

      while (iter.hasNext()) {
        assertEquals(mwSort[item], iter.next().getKey());
        item++;
      }
      assertEquals(mwSort.length, item);
    }

    @Test
    void manyCeilingEntry() {
      manyPut();
      assertEquals(null, sdm().ceilingEntry(manyLarger));
      assertEquals(mwSort[0], sdm().ceilingEntry(manySmaller).getKey());
    }

    @Test
    void manyCeilingKey() {
      manyPut();
      assertEquals(null, sdm().ceilingKey(manyLarger));
      assertEquals(mwSort[0], sdm().ceilingKey(manySmaller));
    }

    @Test
    void manyDescendingKeySet() {
      manyPut();
      NavigableSet<String> ks = sdm().descendingKeySet();
      assertEquals(mwSort.length, ks.size());
      assertEquals(mwSort[mwSort.length - 1], ks.first());
      assertEquals(mwSort[0], ks.last());
    }

    @Test
    void manyDescendingMap() {
      manyPut();
      NavigableMap<String, Integer> nm = sdm().descendingMap();
      assertEquals(mwSort.length, nm.size());
      assertEquals(mwSort[mwSort.length - 1], nm.firstKey());
      assertEquals(mwSort[0], nm.lastKey());
    }

    @Test
    void manyFirstEntry() {
      manyPut();
      assertEquals(mwSort[0], sdm().firstEntry().getKey());
    }

    @Test
    void manyFloorEntry() {
      manyPut();
      assertEquals(null, sdm().floorEntry(manySmaller));
      assertEquals(mwSort[mwSort.length - 1], sdm().floorEntry(manyLarger).getKey());
    }

    @Test
    void manyFloorKey() {
      manyPut();
      assertEquals(null, sdm().floorKey(manySmaller));
      assertEquals(mwSort[mwSort.length - 1], sdm().floorKey(manyLarger));
    }

    @Test
    void manyHeadMap() {
      manyPut();
      String key = mwSort[mwSort.length - 10];
      SortedMap<String, Integer> subMap = sdm().headMap(key);
      assertEquals(mwSort[0], subMap.firstKey());
      assertEquals(mwSort[mwSort.length - 11], subMap.lastKey());
      assertEquals(mwSort.length - 10, subMap.size());
    }

    @Test
    void manyHeadMapInclusive() {
      manyPut();
      String key = mwSort[mwSort.length - 10];
      SortedMap<String, Integer> subMap = sdm().headMap(key, true);
      assertEquals(mwSort[0], subMap.firstKey());
      assertEquals(mwSort[mwSort.length - 10], subMap.lastKey());
      assertEquals(mwSort.length - 9, subMap.size());
    }

    @Test
    void manyHigherEntry() {
      manyPut();
      int lastElemn = mwSort.length - 1;
      assertEquals(mwSort[lastElemn], sdm().higherEntry(mwSort[lastElemn - 1]).getKey());
      assertNull(sdm().higherEntry(mwSort[lastElemn]));
    }

    @Test
    void manyHigherKey() {
      manyPut();
      int lastElemn = mwSort.length - 1;
      assertEquals(mwSort[lastElemn], sdm().higherKey(mwSort[lastElemn - 1]));
      assertNull(sdm().higherKey(mwSort[lastElemn]));
    }

    @Test
    void manyLastEntry() {
      manyPut();
      assertEquals(mwSort[mwSort.length - 1], sdm().lastEntry().getKey());
    }

    @Test
    void manyLowerEntry() {
      manyPut();
      String firstKey = mwSort[0];
      String secondKey = mwSort[1];
      assertNull(sdm().lowerEntry(firstKey));
      assertEquals(firstKey, sdm().lowerEntry(secondKey).getKey());
    }

    @Test
    void manyLowerKey() {
      manyPut();
      String firstKey = mwSort[0];
      String secondKey = mwSort[1];
      assertNull(sdm().lowerKey(firstKey));
      assertEquals(firstKey, sdm().lowerKey(secondKey));
    }

    @Test
    void manyNavigableKeySet() {
      manyPut();
      assertEquals(manyWords.length, sdm().navigableKeySet().size());
    }

    @Test
    void manyPollFirstEntry() {
      manyPut();
      String firstKey = mwSort[0];
      assertEquals(new MapEntry<>(firstKey, firstKey.length()), sdm().pollFirstEntry());
      assertEquals(mwSort.length - 1, sdm().size());
    }

    @Test
    void manyPollLastEntry() {
      manyPut();
      String lastKey = mwSort[mwSort.length - 1];
      assertEquals(new MapEntry<>(lastKey, lastKey.length()), sdm().pollLastEntry());
      assertEquals(mwSort.length - 1, sdm().size());
    }

    @Test
    void manySubMap() {
      manyPut();
      String lowKey = mwSort[10]; //included
      String highKey = mwSort[mwSort.length - 10]; //excluded
      SortedMap<String, Integer> subMap = sdm().subMap(lowKey, highKey);
      assertEquals(mwSort[10], subMap.firstKey());
      assertEquals(mwSort[mwSort.length - 11], subMap.lastKey());
      assertEquals(mwSort.length - 20, subMap.size());
    }

    @Test
    void manySubMapInclusive() {
      manyPut();
      String lowKey = mwSort[10]; //included
      String highKey = mwSort[mwSort.length - 10]; //included
      SortedMap<String, Integer> subMap = sdm().subMap(lowKey, true, highKey, true);
      assertEquals(mwSort[10], subMap.firstKey());
      assertEquals(mwSort[mwSort.length - 10], subMap.lastKey());
      assertEquals(mwSort.length - 19, subMap.size());
    }

    @Test
    void manySubMapExclusive() {
      manyPut();
      String lowKey = mwSort[10]; //will not be included
      String highKey = mwSort[mwSort.length - 10]; //will not be included
      SortedMap<String, Integer> subMap = sdm().subMap(lowKey, false, highKey, false);
      assertEquals(mwSort[11], subMap.firstKey());
      assertEquals(mwSort[mwSort.length - 11], subMap.lastKey());
      assertEquals(mwSort.length - 21, subMap.size());
    }


    @Test
    void manyTailMap() {
      manyPut();
      String lowKey = mwSort[10];
      SortedMap<String, Integer> subMap = sdm().tailMap(lowKey);
      assertEquals(mwSort[10], subMap.firstKey());
      assertEquals(mwSort[mwSort.length - 1], subMap.lastKey());
      assertEquals(mwSort.length - 10, subMap.size());
    }

    @Test
    void manyTailMapExclusive() {
      manyPut();
      String lowKey = mwSort[10];
      SortedMap<String, Integer> subMap = sdm().tailMap(lowKey, false);
      assertEquals(mwSort[11], subMap.firstKey());
      assertEquals(mwSort[mwSort.length - 1], subMap.lastKey());
      assertEquals(mwSort.length - 11, subMap.size());
    }

  }


}
