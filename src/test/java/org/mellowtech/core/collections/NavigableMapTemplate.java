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

package org.mellowtech.core.collections;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mellowtech.core.util.MapEntry;

import java.io.IOException;
import java.util.*;

/**
 * Created by msvens on 11/11/15.
 */
public abstract class NavigableMapTemplate extends MapTemplate {
  
  public NavigableMap<String, Integer> nm(){
    return (NavigableMap<String, Integer>) map;
  }

  protected void verifyKeyIter(String[] words, Iterator<String> iter){
    int i = 0;
    while(iter.hasNext()){
      assertEquals(words[i], iter.next());
      i++;
    }
    assertEquals(words.length, i);
  }
  protected void verifyEntryIter(String[] words, Iterator<Map.Entry<String,Integer>> iter){
    int i = 0;
    while(iter.hasNext()){
      assertEquals(new MapEntry(words[i], words[i].length()), iter.next());
      i++;
    }
    assertEquals(words.length, i);
  }

  @Test
  public void zeroCeilingEntry() throws IOException {
    assertNull(nm().ceilingEntry(swords[0]));
  }

  @Test
  public void zeroCeilingKey() throws IOException {
    assertNull(nm().ceilingKey(swords[0]));
  }

  @Test
  public void zeroDescendingKeySet() throws IOException {
    assertTrue(nm().descendingKeySet().isEmpty());
  }

  @Test
  public void zeroDescendingMap() throws IOException {
    assertTrue(nm().descendingMap().isEmpty());
  }

  @Test
  public void zeroFirstEntry() throws IOException {
    assertNull(nm().firstEntry());
  }

  @Test
  public void zeroFloorEntry() throws IOException {
    assertNull(nm().floorEntry(swords[0]));
  }

  @Test
  public void zeroFloorKey() throws IOException {
    assertNull(nm().floorKey(swords[0]));
  }

  @Test
  public void zeroHeadMap() throws IOException {
    SortedMap<String,Integer> hm = nm().headMap(swords[0]);
    assertTrue(nm().headMap(swords[0]).isEmpty());
  }

  @Test
  public void zeroHeadMapInclusive() throws IOException {
    assertTrue(nm().headMap(swords[0], true).isEmpty());
  }

  @Test
  public void zeroHigherEntry() throws IOException {
    assertNull(nm().higherEntry(swords[0]));
  }

  @Test
  public void zeroHigherKey() throws IOException {
    assertNull(nm().higherKey(swords[0]));
  }

  @Test
  public void zeroLastEntry() throws IOException {
    assertNull(nm().lastEntry());
  }

  @Test
  public void zeroLowerEntry() throws IOException {
    assertNull(nm().lowerEntry(swords[3]));
  }

  @Test
  public void zeroLowerKey() throws IOException {
    assertNull(nm().lowerKey(swords[3]));
  }

  @Test
  public void zeroNavigableKeySet() throws IOException {
    assertTrue(nm().navigableKeySet().isEmpty());
  }

  @Test
  public void zeroPollFirstEntry() throws IOException {
    assertNull(nm().pollFirstEntry());
  }

  @Test
  public void zeroPollLastEntry() throws IOException {
    assertNull(nm().pollLastEntry());
  }

  @Test
  public void zeroSubMap() throws IOException {
    assertTrue(nm().subMap(swords[0], swords[3]).isEmpty());
  }

  @Test
  public void zeroSubMapInclusive() throws IOException {
    assertTrue(nm().subMap(swords[0], true, swords[3], true).isEmpty());
  }

  @Test
  public void zeroTailMap() throws IOException {
    assertTrue(nm().tailMap(swords[0]).isEmpty());
  }

  @Test
  public void zeroTailMapInclusive() throws IOException {
    assertTrue(nm().tailMap(swords[0], true).isEmpty());
  }

  @Test
  public void oneCeilingEntry() throws IOException {
    onePut();
    assertEquals(new MapEntry<>(swords[0], swords[0].length()), nm().ceilingEntry(swords[0]));
  }

  @Test
  public void oneCeilingKey() throws IOException {
    onePut();
    assertEquals(swords[0], nm().ceilingKey(swords[0]));
  }

  @Test
  public void oneDescendingKeySet() throws IOException {
    onePut();
    NavigableSet<String> ks = nm().descendingKeySet();
    assertEquals(1, ks.size());
    assertEquals(swords[0], ks.last());
    assertEquals(swords[0], ks.first());
  }

  @Test
  public void oneDescendingMap() throws IOException {
    onePut();
    //assertEquals(1, nm().descendingMap().size());
    NavigableMap<String, Integer> nm = nm().descendingMap();
    assertEquals(1, nm.size());
    assertEquals(swords[0], nm.firstKey());
    assertEquals(swords[0], nm.lastKey());
  }

  @Test
  public void oneFirstEntry() throws IOException {
    onePut();
    assertEquals(swords[0], nm().firstEntry().getKey());
  }

  @Test
  public void oneFloorEntry() throws IOException {
    onePut();
    assertEquals(swords[0], nm().floorEntry(swords[0]).getKey());
  }

  @Test
  public void oneFloorKey() throws IOException {
    onePut();
    assertEquals(swords[0], nm().floorKey(swords[0]));
  }

  @Test
  public void oneHeadMap() throws IOException {
    onePut();
    SortedMap<String,Integer> hm = nm().headMap(swords[0]);
    assertEquals(0, hm.size());
  }

  @Test
  public void oneHeadMapInclusive() throws IOException {
    onePut();
    assertEquals(1, nm().headMap(swords[0], true).size());
  }

  @Test
  public void oneHigherEntry() throws IOException {
    onePut();
    assertNull(nm().higherEntry(swords[0]));
  }

  @Test
  public void oneHigherKey() throws IOException {
    onePut();
    assertNull(nm().higherKey(swords[0]));
  }

  @Test
  public void oneLastEntry() throws IOException {
    onePut();
    assertEquals(swords[0], nm().lastEntry().getKey());
  }

  @Test
  public void oneLowerEntry() throws IOException {
    onePut();
    assertNull(nm().lowerEntry(swords[0]));
  }

  @Test
  public void oneLowerKey() throws IOException {
    onePut();
    assertNull(nm().lowerKey(swords[0]));
  }

  @Test
  public void oneNavigableKeySet() throws IOException {
    onePut();
    assertEquals(1, nm().navigableKeySet().size());
  }

  @Test
  public void onePollFirstEntry() throws IOException {
    onePut();
    assertNotNull(nm().pollFirstEntry());
    assertEquals(0, nm().size());
  }

  @Test
  public void onePollLastEntry() throws IOException {
    onePut();
    assertNotNull(nm().pollLastEntry());
    assertEquals(0, nm().size());
  }

  @Test
  public void oneSubMap() throws IOException {
    onePut();
    assertFalse(nm().subMap(swords[0], swords[3]).isEmpty());
  }

  @Test
  public void oneSubMapInclusive() throws IOException {
    onePut();
    assertFalse(nm().subMap(swords[0], true, swords[3], true).isEmpty());
  }

  @Test
  public void oneTailMap() throws IOException {
    onePut();
    assertEquals(1, nm().tailMap(swords[0]).size());
  }

  @Test
  public void oneTailMapExclusive() throws IOException {
    onePut();
    assertEquals(0, nm().tailMap(swords[0], false).size());
  }

  //Test 10 element
  @Test
  public void tenCeilingEntry() throws IOException {
    tenPut();
    assertEquals(new MapEntry<>(swords[3], swords[0].length()), nm().ceilingEntry(treeHalfWord));
    assertEquals(new MapEntry<>(swords[3], swords[0].length()), nm().ceilingEntry(swords[3]));
  }

  @Test
  public void tenCeilingKey() throws IOException {
    tenPut();
    assertEquals(swords[3], nm().ceilingKey(treeHalfWord));
    assertEquals(swords[3], nm().ceilingKey(swords[3]));
  }

  @Test
  public void tenDescendingKeySet() throws IOException {
    tenPut();
    NavigableSet<String> ks = nm().descendingKeySet();
    assertEquals(swords.length, ks.size());
    assertEquals(swords[swords.length-1], ks.first());
    assertEquals(swords[0], ks.last());
    verifyKeyIter(rwords, ks.iterator());
  }

  @Test
  public void tenDescendingMap() throws IOException {
    tenPut();
    NavigableMap<String, Integer> nm = nm().descendingMap();
    assertEquals(swords.length, nm.size());
    assertEquals(swords[swords.length-1], nm.firstKey());
    assertEquals(swords[0], nm.lastKey());
    verifyKeyIter(rwords, nm.keySet().iterator());
  }

  @Test
  public void tenFirstEntry() throws IOException {
    tenPut();
    assertEquals(swords[0], nm().firstEntry().getKey());
  }

  @Test
  public void tenFloorEntry() throws IOException {
    tenPut();
    assertEquals(new MapEntry<>(swords[3], swords[0].length()), nm().floorEntry(fourHalfWord));
    assertEquals(new MapEntry<>(swords[3], swords[0].length()), nm().floorEntry(swords[3]));
  }

  @Test
  public void tenFloorKey() throws IOException {
    tenPut();
    assertEquals(swords[3], nm().floorKey(fourHalfWord));
    assertEquals(swords[3], nm().floorKey(swords[3]));
  }

  @Test
  public void tenHeadMap() throws IOException {
    tenPut();
    SortedMap <String, Integer> subMap = nm().headMap(swords[3]);
    assertEquals(swords[0], subMap.firstKey());
    assertEquals(swords[2], subMap.lastKey());
    assertEquals(3, subMap.size());
  }

  @Test
  public void tenHeadMapInclusive() throws IOException {
    tenPut();
    SortedMap <String, Integer> subMap = nm().headMap(swords[3], true);
    assertEquals(swords[0], subMap.firstKey());
    assertEquals(swords[3], subMap.lastKey());
    assertEquals(4, subMap.size());
  }

  @Test
  public void tenHigherEntry() throws IOException {
    tenPut();
    assertEquals(swords[9], nm().higherEntry("j").getKey());
    assertNull(nm().higherEntry(swords[9]));
  }

  @Test
  public void tenHigherKey() throws IOException {
    tenPut();
    assertEquals(swords[9], nm().higherKey("j"));
    assertNull(nm().higherKey(swords[9]));
  }

  @Test
  public void tenLastEntry() throws IOException {
    tenPut();
    assertEquals(swords[9], nm().lastEntry().getKey());
  }

  @Test
  public void tenLowerEntry() throws IOException {
    tenPut();
    assertEquals(swords[0], nm().lowerEntry("b").getKey());
    assertNull(nm().lowerEntry(swords[0]));
  }

  @Test
  public void tenLowerKey() throws IOException {
    tenPut();
    assertEquals(swords[0], nm().lowerKey("b"));
    assertNull(nm().lowerKey(swords[0]));
  }

  @Test
  public void tenNavigableKeySet() throws IOException {
    tenPut();
    assertEquals(10, nm().navigableKeySet().size());
  }

  @Test
  public void tenPollFirstEntry() throws IOException {
    tenPut();
    assertNotNull(nm().pollFirstEntry());
    assertEquals(9, nm().size());
  }

  @Test
  public void tenPollLastEntry() throws IOException {
    tenPut();
    assertNotNull(nm().pollLastEntry());
    assertEquals(9, nm().size());
  }

  @Test
  public void tenSubMap() throws IOException {
    tenPut();
    SortedMap <String, Integer> subMap = nm().subMap(swords[0], swords[3]);
    assertEquals(swords[0], subMap.firstKey());
    assertEquals(swords[2], subMap.lastKey());
    assertEquals(3, subMap.size());
  }

  @Test
  public void tenSubMapInclusive() throws IOException {
    tenPut();
    SortedMap <String, Integer> subMap = nm().subMap(swords[0], true, swords[3], true);
    assertEquals(swords[0], subMap.firstKey());
    assertEquals(swords[3], subMap.lastKey());
    assertEquals(4, subMap.size());
  }

  @Test
  public void tenSubMapExclusive() throws IOException {
    tenPut();
    SortedMap <String, Integer> subMap = nm().subMap(swords[0], false, swords[3], false);
    assertEquals(swords[1], subMap.firstKey());
    assertEquals(swords[2], subMap.lastKey());
    assertEquals(2, subMap.size());
  }

  @Test
  public void tenTailMap() throws IOException {
    tenPut();
    SortedMap <String, Integer> subMap = nm().tailMap(swords[3]);
    assertEquals(swords[3], subMap.firstKey());
    assertEquals(swords[9], subMap.lastKey());
    assertEquals(7, subMap.size());
  }

  @Test
  public void tenTailMapExclusive() throws IOException {
    tenPut();
    SortedMap <String, Integer> subMap = nm().tailMap(swords[3], false);
    assertEquals(swords[4], subMap.firstKey());
    assertEquals(swords[9], subMap.lastKey());
    assertEquals(6, subMap.size());
  }

  //Test many elements
  @Test
  public void manyCeilingEntry() throws IOException {
    manyPut();
    assertEquals(null, nm().ceilingEntry(manyLarger));
    assertEquals(mwSort[0], nm().ceilingEntry(manySmaller).getKey());
  }

  @Test
  public void manyCeilingKey() throws IOException {
    manyPut();
    assertEquals(null, nm().ceilingKey(manyLarger));
    assertEquals(mwSort[0], nm().ceilingKey(manySmaller));
  }

  @Test
  public void manyDescendingKeySet() throws IOException {
    manyPut();
    NavigableSet<String> ks = nm().descendingKeySet();
    assertEquals(mwSort.length, ks.size());
    assertEquals(mwSort[mwSort.length-1], ks.first());
    assertEquals(mwSort[0], ks.last());
  }

  @Test
  public void manyDescendingMap() throws IOException {
    manyPut();
    NavigableMap<String, Integer> nm = nm().descendingMap();
    assertEquals(mwSort.length, nm.size());
    assertEquals(mwSort[mwSort.length-1], nm.firstKey());
    assertEquals(mwSort[0], nm.lastKey());
  }

  @Test
  public void manyFirstEntry() throws IOException {
    manyPut();
    assertEquals(mwSort[0], nm().firstEntry().getKey());
  }

  @Test
  public void manyFloorEntry() throws IOException {
    manyPut();
    assertEquals(null, nm().floorEntry(manySmaller));
    assertEquals(mwSort[mwSort.length-1], nm().floorEntry(manyLarger).getKey());
  }

  @Test
  public void manyFloorKey() throws IOException {
    manyPut();
    assertEquals(null, nm().floorKey(manySmaller));
    assertEquals(mwSort[mwSort.length-1], nm().floorKey(manyLarger));
  }

  @Test
  public void manyHeadMap() throws IOException {
    manyPut();
    String key = mwSort[mwSort.length-10];
    SortedMap <String, Integer> subMap = nm().headMap(key);
    assertEquals(mwSort[0], subMap.firstKey());
    assertEquals(mwSort[mwSort.length-11], subMap.lastKey());
    assertEquals(mwSort.length-10, subMap.size());
  }

  @Test
  public void manyHeadMapInclusive() throws IOException {
    manyPut();
    String key = mwSort[mwSort.length-10];
    SortedMap <String, Integer> subMap = nm().headMap(key, true);
    assertEquals(mwSort[0], subMap.firstKey());
    assertEquals(mwSort[mwSort.length-10], subMap.lastKey());
    assertEquals(mwSort.length-9, subMap.size());
  }

  @Test
  public void manyHigherEntry() throws IOException {
    manyPut();
    int lastElemn = mwSort.length - 1;;
    assertEquals(mwSort[lastElemn], nm().higherEntry(mwSort[lastElemn-1]).getKey());
    assertNull(nm().higherEntry(mwSort[lastElemn]));
  }

  @Test
  public void manyHigherKey() throws IOException {
    manyPut();
    int lastElemn = mwSort.length - 1;;
    assertEquals(mwSort[lastElemn], nm().higherKey(mwSort[lastElemn-1]));
    assertNull(nm().higherKey(mwSort[lastElemn]));
  }

  @Test
  public void manyLastEntry() throws IOException {
    manyPut();
    assertEquals(mwSort[mwSort.length-1], nm().lastEntry().getKey());
  }

  @Test
  public void manyLowerEntry() throws IOException {
    manyPut();
    String firstKey = mwSort[0];
    String secondKey = mwSort[1];
    assertNull(nm().lowerEntry(firstKey));
    assertEquals(firstKey, nm().lowerEntry(secondKey).getKey());
  }

  @Test
  public void manyLowerKey() throws IOException {
    manyPut();
    String firstKey = mwSort[0];
    String secondKey = mwSort[1];
    assertNull(nm().lowerKey(firstKey));
    assertEquals(firstKey, nm().lowerKey(secondKey));
  }

  @Test
  public void manyNavigableKeySet() throws IOException {
    manyPut();
    assertEquals(manyWords.length, nm().navigableKeySet().size());
  }

  @Test
  public void manyPollFirstEntry() throws IOException {
    manyPut();
    String firstKey = mwSort[0];
    assertEquals(new MapEntry<>(firstKey, firstKey.length()), nm().pollFirstEntry());
    assertEquals(mwSort.length - 1, nm().size());
  }

  @Test
  public void manyPollLastEntry() throws IOException {
    manyPut();
    String lastKey = mwSort[mwSort.length - 1];
    assertEquals(new MapEntry<>(lastKey, lastKey.length()), nm().pollLastEntry());
    assertEquals(mwSort.length - 1, nm().size());
  }

  @Test
  public void manySubMap() throws IOException {
    manyPut();
    String lowKey = mwSort[10]; //included
    String highKey = mwSort[mwSort.length-10]; //excluded
    SortedMap <String, Integer> subMap = nm().subMap(lowKey, highKey);
    assertEquals(mwSort[10], subMap.firstKey());
    assertEquals(mwSort[mwSort.length-11], subMap.lastKey());
    assertEquals(mwSort.length - 20, subMap.size());
  }

  @Test
  public void manySubMapInclusive() throws IOException {
    manyPut();
    String lowKey = mwSort[10]; //included
    String highKey = mwSort[mwSort.length-10]; //included
    SortedMap <String, Integer> subMap = nm().subMap(lowKey, true, highKey, true);
    assertEquals(mwSort[10], subMap.firstKey());
    assertEquals(mwSort[mwSort.length-10], subMap.lastKey());
    assertEquals(mwSort.length - 19, subMap.size());
  }

  @Test
  public void manySubMapExclusive() throws IOException {
    manyPut();
    String lowKey = mwSort[10]; //will not be included
    String highKey = mwSort[mwSort.length-10]; //will not be included
    SortedMap <String, Integer> subMap = nm().subMap(lowKey, false, highKey, false);
    assertEquals(mwSort[11], subMap.firstKey());
    assertEquals(mwSort[mwSort.length-11], subMap.lastKey());
    assertEquals(mwSort.length - 21, subMap.size());
  }


  @Test
  public void manyTailMap() throws IOException {
    manyPut();
    String lowKey = mwSort[10];
    SortedMap <String, Integer> subMap = nm().tailMap(lowKey);
    assertEquals(mwSort[10], subMap.firstKey());
    assertEquals(mwSort[mwSort.length-1], subMap.lastKey());
    assertEquals(mwSort.length - 10, subMap.size());
  }

  @Test
  public void manyTailMapExclusive() throws IOException {
    manyPut();
    String lowKey = mwSort[10];
    SortedMap <String, Integer> subMap = nm().tailMap(lowKey, false);
    assertEquals(mwSort[11], subMap.firstKey());
    assertEquals(mwSort[mwSort.length-1], subMap.lastKey());
    assertEquals(mwSort.length - 11, subMap.size());
  }



}
