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
import org.mellowtech.core.TestUtils;
import org.mellowtech.core.collections.DiscMap;
import org.mellowtech.core.util.MapEntry;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class DiscMapTemplate{

  DiscMap<String, Integer> map;

  static int IDX_BLK_SIZE = 1024;
  static int VAL_BLK_SIZE = 1024;
  static int IDX_BLKS = 5;
  static int VAL_BLKS = 15;

  static String dir = "discmaptests";
  static Path absPath(String fName){
    return TestUtils.getAbsolutePath(dir+"/"+fName);
  }
  static String chars = "abcdefghijklmn0123456789";
  static int MAX_WORD_LENGTH = 20;
  static int MAX_BYTES = 4000;

  static String[] manyWords;
  static String[] mwSort, mwDesc;

  static {
    manyWords = TestUtils.randomWords(chars, MAX_WORD_LENGTH, MAX_BYTES);
    mwSort = Arrays.copyOf(manyWords, manyWords.length);
    mwDesc = Arrays.copyOf(manyWords, manyWords.length);
    Arrays.sort(mwSort);
    Arrays.sort(mwDesc, Collections.reverseOrder());
  }



  static String[] words = new String[]{"hotel", "delta", "alpha", "bravo",
      "india","echo", "foxtrot", "juliet", "charlie", "golf"};

  static String[] swords = new String[]{"alpha", "bravo", "charlie",
      "delta", "echo", "foxtrot", "golf", "hotel", "india", "juliet"};

  static String[] rwords = new String[]{"juliet", "india", "hotel", "golf",
      "foxtrot", "echo", "delta", "charlie", "bravo", "alpha"};

  static String treeHalfWord = "dalta";
  static String fourHalfWord = "dilta";
  static String manySmaller = "0";
  static String manyLarger = "o";


  abstract DiscMap <String, Integer> init() throws Exception;
  abstract DiscMap <String, Integer> reopen() throws Exception;

  //Utility methods
  void onePut() {
    map.put(swords[0], swords[0].length());
  }

  void tenPut() {
    for (String w : words) {
      map.put(w, w.length());
    }
  }

  void manyPut(){
    for(String w : manyWords){
      map.put(w, w.length());
    }
  }

  @BeforeAll
  static void createDir(){
    if(Files.exists(TestUtils.getAbsolutePath(dir)))
      TestUtils.deleteTempDir(dir);
    TestUtils.createTempDir(dir);
  }

  @AfterAll
  static void deleteDir(){
    TestUtils.deleteTempDir(dir);
  }

  @AfterEach
  void after() throws Exception{
    map.close();
    map.delete();
  }

  @BeforeEach
  void setup() throws Exception{
    map = init();
  }

  @Nested
  @DisplayName("A map with no key")
  class Empty {

    @Test
    void emptyIterator() {
      assertFalse(map.iterator().hasNext());
    }

    @Test
    void emptyReopen() throws Exception{
      map.close();
      map = reopen();
      assertNull(map.get(swords[0]));
    }
    @Test
    void emptySize() {
      assertEquals(0, map.size());
    }

    @Test
    void emptyIsEmpty() {
      assertTrue(map.isEmpty());
    }

    @Test
    void emptyContainsKey() {
      assertFalse(map.containsKey(swords[0]));
    }

    @Test
    void emptyContainsValue() {
      assertFalse(map.containsValue(swords[0].length()));
    }

    @Test
    void emptyGet() {
      assertNull(map.get(swords[0]));
    }

    @Test
    void emptyRemove() {
      assertNull(map.remove(swords[0]));
    }

    @Test
    void emptyClear() {
      map.clear();
      assertTrue(map.isEmpty());
    }

    @Test
    void emptyKeySet() {
      Set<String> keySet = map.keySet();
      assertEquals(0, keySet.size());
    }

    @Test
    void emptyValues() {
      Collection<Integer> values = map.values();
      assertEquals(0, values.size());
    }

    @Test
    void emptyEntrySet() {
      Set<Entry<String, Integer>> entrySet = map.entrySet();
      assertEquals(0, entrySet.size());
    }
  }

  @Nested
  @DisplayName("A map with one key")
  class One {

    @Test
    void oneIterator() {
      onePut();
      assertTrue(map.iterator().hasNext());
    }

    @Test
    void oneReopen() throws Exception{
      onePut();
      map.close();
      map = reopen();
      assertEquals((Integer) swords[0].length(), map.get(swords[0]));
    }

    @Test
    void oneSize() {
      onePut();
      assertEquals(1, map.size());
    }

    @Test
    void oneIsEmpty() {
      onePut();
      assertFalse(map.isEmpty());
    }

    @Test
    void oneContainsKey() {
      onePut();
      assertTrue(map.containsKey(swords[0]));
    }

    @Test
    void oneContainsValue() {
      onePut();
      assertTrue(map.containsValue(swords[0].length()));
    }

    @Test
    void oneGet() {
      onePut();
      assertEquals((Integer) swords[0].length(), map.get(swords[0]));
    }

    @Test
    void oneRemove() {
      onePut();
      assertEquals((Integer) swords[0].length(), map.remove(swords[0]));
    }

    @Test
    void oneClear() {
      onePut();
      map.clear();
      assertTrue(map.isEmpty());
    }

    @Test
    void oneKeySet() {
      onePut();
      Set<String> keySet = map.keySet();
      assertTrue(keySet.contains(swords[0]));
    }

    @Test
    void oneValues() {
      onePut();
      Collection<Integer> values = map.values();
      assertTrue(values.contains(swords[0].length()));
    }

    @Test
    void oneEntrySet() {
      onePut();
      Set<Entry<String, Integer>> entrySet = map.entrySet();
      List<Entry<String, Integer>> list = new ArrayList<>();
      list.addAll(entrySet);
      assertEquals(swords[0], list.get(0).getKey());
      assertEquals((Integer) swords[0].length(), list.get(0).getValue());
    }
  }

  @Nested
  @DisplayName("A map with 10 keys")
  class Ten {

    @Test
    void tenIterator() {
      tenPut();
      int tot = 0;
      Iterator<Map.Entry<String,Integer>>  iter = map.iterator();
      while(iter.hasNext()){
        tot++;
        iter.next();
      }
      assertEquals(10, tot);
    }

    @Test
    void tenReopen() throws Exception {
      tenPut();
      map.close();
      map = reopen();
      for (String w : words) {
        assertEquals((Integer) w.length(), map.get(w));
      }
    }

    @Test
    void tenSize() {
      tenPut();
      assertEquals(10, map.size());
    }

    @Test
    void tenIsEmpty() {
      tenPut();
      assertFalse(map.isEmpty());
    }

    @Test
    void tenContainsKey() {
      tenPut();
      for (String w : words) {
        assertTrue(map.containsKey(w));
      }
    }

    @Test
    void tenContainsValue() {
      tenPut();
      for (String w : words) {
        assertTrue(map.containsValue(w.length()));
      }

    }

    @Test
    void tenGet() {
      tenPut();
      for (String w : words) {
        assertEquals((Integer) w.length(), map.get(w));
      }
    }

    @Test
    void tenRemove() {
      tenPut();
      for (String w : words) {
        assertEquals((Integer) w.length(), map.remove(w));
      }
      assertTrue(map.isEmpty());
    }

    @Test
    void tenClear() {
      tenPut();
      map.clear();
      assertTrue(map.isEmpty());
    }

    @Test
    void tenKeySet() {
      tenPut();
      Set<String> keySet = map.keySet();
      for (String w : words) {
        assertTrue(keySet.contains(w));
      }
      assertEquals(10, keySet.size());
    }

    @Test
    void tenValues() {
      tenPut();
      Collection<Integer> values = map.values();
      for (String w : words) {
        assertTrue(values.contains(w.length()));
      }
      assertEquals(10, values.size());
    }

    @Test
    void tenEntrySet() {
      tenPut();
      Set<Entry<String, Integer>> entrySet = map.entrySet();
      for (String w : words) {
        Entry<String, Integer> e = new MapEntry<>(w, w.length());
        assertTrue(entrySet.contains(e));
      }
      assertEquals(10, entrySet.size());
    }
  }


  @Nested
  @DisplayName("A map with many keys")
  class Many {
    @Test
    public void manyReopen() throws Exception{
      manyPut();
      map.close();
      map = reopen();
      for(String w : manyWords){
        assertEquals((Integer)w.length(), map.get(w));
      }
    }

    @Test
    public void manyIterator() {
      manyPut();
      Iterator <Entry<String, Integer>> iter = map.iterator();
      int items = 0;
      while(iter.hasNext()){
        String w = iter.next().getKey();
        assertTrue(Arrays.binarySearch(mwSort, w) >= 0);
        items++;
      }
      assertEquals(mwSort.length, items);
    }

    @Test
    void manySize() {
      manyPut();
      assertEquals(manyWords.length, map.size());
    }

    @Test
    void manyIsEmpty() {
      manyPut();
      assertFalse(map.isEmpty());
    }

    @Test
    void manyContainsKey() {
      manyPut();
      for (String w : manyWords) {
        assertTrue(map.containsKey(w));
      }
    }

    @Test
    void manyContainsValue() {
      manyPut();
      for (String w : manyWords) {
        assertTrue(map.containsValue(w.length()));
      }

    }

    @Test
    void manyGet() {
      manyPut();
      for (String w : manyWords) {
        assertEquals((Integer) w.length(), map.get(w));
      }
    }

    @Test
    void manyRemove() {
      manyPut();
      for (String w : manyWords) {
        assertEquals((Integer) w.length(), map.remove(w));
      }
      assertTrue(map.isEmpty());
    }

    @Test
    void manyClear() {
      manyPut();
      map.clear();
      assertTrue(map.isEmpty());
    }

    @Test
    void manyKeySet() {
      manyPut();
      Set<String> keySet = map.keySet();
      for (String w : manyWords) {
        assertTrue(keySet.contains(w));
      }
      assertEquals(manyWords.length, keySet.size());
    }

    @Test
    void manyValues() {
      manyPut();
      Collection<Integer> values = map.values();
      for (String w : manyWords) {
        assertTrue(values.contains(w.length()));
      }
      assertEquals(manyWords.length, values.size());
    }

    @Test
    void manyEntrySet() {
      manyPut();
      Set<Entry<String, Integer>> entrySet = map.entrySet();
      for (String w : manyWords) {
        Entry<String, Integer> e = new MapEntry<>(w, w.length());
        assertTrue(entrySet.contains(e));
      }
      assertEquals(manyWords.length, entrySet.size());
    }
  }
}
