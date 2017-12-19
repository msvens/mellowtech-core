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

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mellowtech.core.TestUtils;
import org.mellowtech.core.collections.BMap;
import org.mellowtech.core.collections.KeyValue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by msvens on 01/11/15.
 */
public abstract class BMapTemplate {

  BMap<String, Integer> tree;
  public static String dir = "rftests";
  public static String chars = "abcdefghijklmn0123456789";
  public static int MAX_WORD_LENGTH = 20;
  public static int MAX_BYTES = 4000;

  public static String[] manyWords, mAscend, mDescend;

  static {
    manyWords = TestUtils.randomWords(chars, MAX_WORD_LENGTH, MAX_BYTES);
    mAscend = Arrays.copyOf(manyWords, manyWords.length);
    mDescend = Arrays.copyOf(manyWords, manyWords.length);
    Arrays.sort(mAscend);
    Arrays.sort(mDescend, Collections.reverseOrder());
  }

  public static String[] words = new String[]{
      "hotel","delta","alpha","bravo","india","echo","foxtrot","juliet","charlie","golf"};

  public static String[] ascend = new String[]{
      "alpha","bravo","charlie","delta","echo","foxtrot","golf","hotel","india","juliet"};

  public static String[] descend = new String[]{
      "juliet","india","hotel","golf","foxtrot","echo","delta","charlie","bravo","alpha"};


  public static String firstWord = "alpha";
  public static String forthWord = "delta";


  public abstract String fName();

  public abstract BMap<String, Integer>
  init(Path fileName, int bucketSize, int maxBuckets) throws Exception;

  public abstract BMap<String, Integer> reopen(Path fileName) throws Exception;

  @BeforeClass
  public static void createDir() {
    TestUtils.deleteTempDir(dir);
    TestUtils.createTempDir(dir);
  }

  @Before
  public void setup() throws Exception {
    tree = init(TestUtils.getAbsolutePath(dir + "/" + fName()), 1024, 16);
  }

  @After
  public void after() throws Exception {
    tree.close();
    tree.delete();
  }

  //IO:
  /*void save() throws IOException;
  void close() throws IOException;
  void delete() throws IOException;*/


  //Maintenance:

  /**
   * Over time a disc based tree can be fragmented. Ensures optimal
   * disc representation
   * @throws IOException if an error occurs
   */
  //void compact() throws IOException, UnsupportedOperationException;

  /***********
   * empty tree tests
   *****************************/
  @Test
  public void emptySize() throws IOException {
    Assert.assertEquals(0, tree.size());
  }

  @Test
  public void emptyIsEmpty() throws IOException {
    Assert.assertTrue(tree.isEmpty());
  }

  @Test
  public void emptyContainsKey() throws IOException {
    Assert.assertFalse(tree.containsKey(firstWord));
  }

  @Test
  public void emptyRemove() throws IOException {
    Assert.assertNull(tree.remove(firstWord));
  }

  @Test
  public void emptyGet() throws IOException {
    Assert.assertNull(tree.get(firstWord));
  }

  @Test
  public void emptyReopen() throws Exception {
    tree.close();
    tree = reopen(TestUtils.getAbsolutePath(dir + "/" + fName()));
    Assert.assertNull(tree.get(firstWord));
  }

  @Test
  public void emptyGetKeyValue() throws IOException {
    Assert.assertNull(tree.getKeyValue(firstWord));
  }

  @Test
  public void emptyIterator() throws IOException {
    Assert.assertFalse(tree.iterator().hasNext());
  }

  /***********
   * one item tree tests
   *****************************/
  protected Integer val(String key) {
    return key.length();
  }

  @Test
  public void oneSize() throws IOException {
    tree.put(firstWord, val(firstWord));
    Assert.assertEquals(1, tree.size());
  }

  @Test
  public void oneIsEmpty() throws IOException {
    tree.put(firstWord, val(firstWord));
    Assert.assertFalse(tree.isEmpty());
  }

  @Test
  public void oneContainsKey() throws IOException {
    tree.put(firstWord, val(firstWord));
    Assert.assertTrue(tree.containsKey(firstWord));
  }

  @Test
  public void oneRemove() throws IOException {
    tree.put(firstWord, val(firstWord));
    Assert.assertEquals(val(firstWord), tree.remove(firstWord));
  }

  @Test
  public void oneGet() throws IOException {
    tree.put(firstWord, val(firstWord));
    Assert.assertEquals(val(firstWord), tree.get(firstWord));
  }

  @Test
  public void oneReopen() throws Exception {
    tree.put(firstWord, val(firstWord));
    tree.close();
    tree = reopen(TestUtils.getAbsolutePath(dir + "/" + fName()));
    Assert.assertEquals(val(firstWord), tree.get(firstWord));
  }

  @Test
  public void oneGetKeyValue() throws IOException {
    tree.put(firstWord, val(firstWord));
    KeyValue<String, Integer> kv = tree.getKeyValue(firstWord);
    Assert.assertEquals(firstWord, kv.getKey());
    Assert.assertEquals(val(firstWord), kv.getValue());
  }

  @Test
  public void oneIterator() throws IOException {
    tree.put(firstWord, val(firstWord));
    Assert.assertTrue(tree.iterator().hasNext());
  }

  /***********
   * ten item tree tests
   *****************************/
  protected void fillTree() throws IOException {
    for (String w : words) {
      tree.put(w, val(w));
    }
  }

  @Test
  public void tenSize() throws IOException {
    fillTree();
    Assert.assertEquals(words.length, tree.size());
  }

  @Test
  public void tenIsEmpty() throws IOException {
    fillTree();
    Assert.assertFalse(tree.isEmpty());
  }

  @Test
  public void tenContainsKey() throws IOException {
    fillTree();
    for (String w : words) {
      Assert.assertTrue(tree.containsKey(w));
    }
  }

  @Test
  public void tenRemove() throws IOException {
    fillTree();
    for (String w : words)
      Assert.assertEquals(val(w), tree.remove(w));
  }

  @Test
  public void tenGet() throws IOException {
    fillTree();
    for (String w : words)
      Assert.assertEquals(val(w), tree.get(w));
  }

  @Test
  public void tenReopen() throws Exception {
    fillTree();
    tree.close();
    tree = reopen(TestUtils.getAbsolutePath(dir + "/" + fName()));
    for (String w : words)
      Assert.assertEquals(val(w), tree.get(w));
  }

  @Test
  public void tenGetKeyValue() throws IOException {
    fillTree();
    for (String w : words) {
      KeyValue<String, Integer> kv = tree.getKeyValue(w);
      Assert.assertEquals(w, kv.getKey());
      Assert.assertEquals(val(w), kv.getValue());
    }
  }

  @Test
  public void tenIterator() throws IOException {
    fillTree();
    Iterator iter = tree.iterator();
    int tot = 0;
    while (iter.hasNext()) {
      tot++;
      iter.next();
    }
    Assert.assertEquals(10, tot);
  }

  /**********
   * many item tree tests
   *****************************/
  protected void fillManyTree() throws IOException {
    for (String w : manyWords) {
      tree.put(w, val(w));
    }
  }

  protected TreeMap<String, Integer> getManyTree() {
    TreeMap<String, Integer> m = new TreeMap<>();
    for (String w : manyWords) {
      m.put(w, val(w));
    }
    return m;
  }

  @Test
  public void manySize() throws IOException {
    fillManyTree();
    Assert.assertEquals(manyWords.length, tree.size());
  }

  @Test
  public void manyIsEmpty() throws IOException {
    fillManyTree();
    Assert.assertFalse(tree.isEmpty());
  }

  @Test
  public void manyContainsKey() throws IOException {
    fillManyTree();
    for (String w : manyWords) {
      Assert.assertTrue(tree.containsKey(w));
    }
  }

  @Test
  public void manyRemove() throws IOException {
    fillManyTree();
    int i = 0;
    for (String w : manyWords) {
      Assert.assertEquals(val(w), tree.remove(w));
    }
  }

  @Test
  public void manyGet() throws IOException {
    fillManyTree();
    for (String w : manyWords)
      Assert.assertEquals(val(w), tree.get(w));
  }

  @Test
  public void manyReopen() throws Exception {
    fillManyTree();
    tree.close();
    tree = reopen(TestUtils.getAbsolutePath(dir + "/" + fName()));
    for (String w : manyWords) {
      Assert.assertEquals(val(w), tree.get(w));
    }
  }

  @Test
  public void manyGetKeyValue() throws IOException {
    fillManyTree();
    for (String w : manyWords) {
      KeyValue<String, Integer> kv = tree.getKeyValue(w);
      Assert.assertEquals(w, kv.getKey());
      Assert.assertEquals(val(w), kv.getValue());
    }
  }

  @Test
  public void manyIterator() throws IOException {
    fillManyTree();
    TreeMap<String, Integer> m = getManyTree();
    Iterator<KeyValue<String, Integer>> iter = tree.iterator();
    int items = 0;
    while (iter.hasNext()) {
      items++;
      String w = iter.next().getKey();
      Assert.assertTrue(m.containsKey(w));
    }
    Assert.assertEquals(m.size(), items);
  }


}
