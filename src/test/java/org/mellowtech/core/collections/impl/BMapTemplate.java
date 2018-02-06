package org.mellowtech.core.collections.impl;


import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mellowtech.core.TestUtils;
import org.mellowtech.core.collections.BMap;
import org.mellowtech.core.collections.KeyValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by msvens on 01/11/15.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BMapTemplate {

  BMap<String, Integer> tree;
  static String dir = "bmaptests";
  static String chars = "abcdefghijklmn0123456789";
  static int MAX_WORD_LENGTH = 20;
  static int MAX_BYTES = 4000;

  static int MAX_VALUE_BLOCKS = 20;
  static int MAX_INDEX_BLOCKS = 5;
  static int VALUE_BLOCK_SIZE = 1024;
  static int INDEX_BLOCK_SIZE = 1024;
  static int BUCKET_SIZE = 1024;
  static int MAX_BUCKETS = 16;

  static String[] manyWords, mAscend, mDescend;

  static {
    manyWords = TestUtils.randomWords(chars, MAX_WORD_LENGTH, MAX_BYTES);
    mAscend = Arrays.copyOf(manyWords, manyWords.length);
    mDescend = Arrays.copyOf(manyWords, manyWords.length);
    Arrays.sort(mAscend);
    Arrays.sort(mDescend, Collections.reverseOrder());
  }

  static String[] words = new String[]{
      "hotel","delta","alpha","bravo","india","echo","foxtrot","juliet","charlie","golf"};

  static String[] ascend = new String[]{
      "alpha","bravo","charlie","delta","echo","foxtrot","golf","hotel","india","juliet"};

  static String[] descend = new String[]{
      "juliet","india","hotel","golf","foxtrot","echo","delta","charlie","bravo","alpha"};


  static String firstWord = "alpha";
  static String forthWord = "delta";
  static String manySmaller = "0";
  static String manyLarger = "o";

  static Path getDir(Path fName){
    return fName.getParent();
  }

  abstract String fName();

  abstract BMap<String, Integer> init(Path fileName,
                                      int bucketSize, int maxBuckets,
                                      int indexBlockSize, int valueBlockSize,
                                      int maxIndexBlocks, int maxValueBlocks) throws Exception;

  BMap<String, Integer> reopen(Path fileName,
                                        int bucketSize, int maxBuckets,
                                        int indexBlockSize, int valueBlockSize,
                                        int maxIndexBlocks, int maxValueBlocks) throws Exception {
    return init(fileName, bucketSize, maxBuckets, indexBlockSize, valueBlockSize, maxIndexBlocks, maxValueBlocks);
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

  @BeforeEach
  void setup() throws Exception {
    tree = init(TestUtils.getAbsolutePath(dir + "/" + fName()),
        BUCKET_SIZE, MAX_BUCKETS, INDEX_BLOCK_SIZE, VALUE_BLOCK_SIZE, MAX_INDEX_BLOCKS, MAX_VALUE_BLOCKS);
  }

  @AfterEach
  void after() throws Exception {
    tree.close();
    tree.delete();
  }

  //Utility methods
  Integer val(String key) {
    return key.length();
  }

  protected void onePut() throws IOException{
    tree.put(ascend[0], val(ascend[0]));
  }

  void tenPut() throws IOException {
    for (String w : words) {
      tree.put(w, val(w));
    }
  }

  void manyPut() throws IOException {
    for (String w : manyWords) {
      tree.put(w, val(w));
    }
  }

  TreeMap<String, Integer> getManyTree() {
    TreeMap<String, Integer> m = new TreeMap<>();
    for (String w : manyWords) {
      m.put(w, val(w));
    }
    return m;
  }

  

  @Nested
  @DisplayName("A bmap with zero keys")
  class Zero {

    @Test
    void zeroTruncate() throws IOException {
      tree.truncate();
      assertEquals(0, tree.size());
    }

    @Test
    void zeroSize() throws IOException {
      assertEquals(0, tree.size());
    }

    @Test
    void zeroIsEmpty() throws IOException {
      assertTrue(tree.isEmpty());
    }

    @Test
    void zeroContainsKey() throws IOException {
      assertFalse(tree.containsKey(firstWord));
    }

    @Test
    void zeroRemove() throws IOException {
      assertNull(tree.remove(firstWord));
    }

    @Test
    void zeroGet() throws IOException {
      assertNull(tree.get(firstWord));
    }

    @Test
    void zeroReopen() throws Exception {
      tree.close();
      tree = reopen(TestUtils.getAbsolutePath(dir + "/" + fName()),
          BUCKET_SIZE, MAX_BUCKETS, INDEX_BLOCK_SIZE, VALUE_BLOCK_SIZE, MAX_INDEX_BLOCKS, MAX_VALUE_BLOCKS);
      assertNull(tree.get(firstWord));
    }

    @Test
    void zeroGetKeyValue() throws IOException {
      assertNull(tree.getKeyValue(firstWord));
    }

    @Test
    void zeroIterator() {
      assertFalse(tree.iterator().hasNext());
    }
  }
  


  @Nested
  @DisplayName("A bmap with one key")
  class One {
    @Test
    void oneTruncate() throws IOException {
      onePut();
      tree.truncate();
      assertEquals(0, tree.size());
    }

    @Test
    void oneSize() throws IOException {
      onePut();
      assertEquals(1, tree.size());
    }

    @Test
    void oneIsEmpty() throws IOException {
      onePut();
      assertFalse(tree.isEmpty());
    }

    @Test
    void oneContainsKey() throws IOException {
      onePut();
      assertTrue(tree.containsKey(firstWord));
    }

    @Test
    void oneRemove() throws IOException {
      onePut();
      assertEquals(val(firstWord), tree.remove(firstWord));
    }

    @Test
    void oneGet() throws IOException {
      onePut();
      assertEquals(val(firstWord), tree.get(firstWord));
    }

    @Test
    void oneReopen() throws Exception {
      onePut();
      tree.close();
      tree = reopen(TestUtils.getAbsolutePath(dir + "/" + fName()),
          BUCKET_SIZE, MAX_BUCKETS, INDEX_BLOCK_SIZE, VALUE_BLOCK_SIZE, MAX_INDEX_BLOCKS, MAX_VALUE_BLOCKS);
      assertEquals(val(firstWord), tree.get(firstWord));
    }

    @Test
    void oneGetKeyValue() throws IOException {
      onePut();
      KeyValue<String, Integer> kv = tree.getKeyValue(firstWord);
      assertEquals(firstWord, kv.getKey());
      assertEquals(val(firstWord), kv.getValue());
    }

    @Test
    void oneIterator() throws IOException {
      onePut();
      assertTrue(tree.iterator().hasNext());
    }
  }


  @Nested
  @DisplayName("A bmap with ten keys")
  class Ten {
    @Test
    void tenTruncate() throws IOException {
      tree.truncate();
      assertEquals(0, tree.size());
    }

    @Test
    void tenSize() throws IOException {
      tenPut();
      assertEquals(words.length, tree.size());
    }

    @Test
    void tenIsEmpty() throws IOException {
      tenPut();
      assertFalse(tree.isEmpty());
    }

    @Test
    void tenContainsKey() throws IOException {
      tenPut();
      for (String w : words) {
        assertTrue(tree.containsKey(w));
      }
    }

    @Test
    void tenRemove() throws IOException {
      tenPut();
      for (String w : words)
        assertEquals(val(w), tree.remove(w));
    }

    @Test
    void tenGet() throws IOException {
      tenPut();
      for (String w : words)
        assertEquals(val(w), tree.get(w));
    }

    @Test
    void tenReopen() throws Exception {
      tenPut();
      tree.close();
      tree = reopen(TestUtils.getAbsolutePath(dir + "/" + fName()),
          BUCKET_SIZE, MAX_BUCKETS, INDEX_BLOCK_SIZE, VALUE_BLOCK_SIZE, MAX_INDEX_BLOCKS, MAX_VALUE_BLOCKS);
      for (String w : words)
        assertEquals(val(w), tree.get(w));
    }

    @Test
    void tenGetKeyValue() throws IOException {
      tenPut();
      for (String w : words) {
        KeyValue<String, Integer> kv = tree.getKeyValue(w);
        assertEquals(w, kv.getKey());
        assertEquals(val(w), kv.getValue());
      }
    }

    @Test
    void tenIterator() throws IOException {
      tenPut();
      Iterator iter = tree.iterator();
      int tot = 0;
      while (iter.hasNext()) {
        tot++;
        iter.next();
      }
      assertEquals(10, tot);
    }
  }


  @Nested
  @DisplayName("A bmap with many keys")
  class Many {
    @Test
    void manyTruncate() throws IOException {
      manyPut();
      tree.truncate();
      assertEquals(0, tree.size());
    }

    @Test
    void manySize() throws IOException {
      manyPut();
      assertEquals(manyWords.length, tree.size());
    }

    @Test
    void manyIsEmpty() throws IOException {
      manyPut();
      assertFalse(tree.isEmpty());
    }

    @Test
    void manyContainsKey() throws IOException {
      manyPut();
      for (String w : manyWords) {
        assertTrue(tree.containsKey(w));
      }
    }

    @Test
    void manyRemove() throws IOException {
      manyPut();
      for (String w : manyWords) {
        assertEquals(val(w), tree.remove(w));
      }
    }

    @Test
    void manyGet() throws IOException {
      manyPut();
      for (String w : manyWords)
        assertEquals(val(w), tree.get(w));
    }

    @Test
    void manyReopen() throws Exception {
      manyPut();
      tree.close();
      tree = reopen(TestUtils.getAbsolutePath(dir + "/" + fName()),
          BUCKET_SIZE, MAX_BUCKETS, INDEX_BLOCK_SIZE, VALUE_BLOCK_SIZE, MAX_INDEX_BLOCKS, MAX_VALUE_BLOCKS);
      for (String w : manyWords) {
        assertEquals(val(w), tree.get(w));
      }
    }

    @Test
    void manyGetKeyValue() throws IOException {
      manyPut();
      for (String w : manyWords) {
        KeyValue<String, Integer> kv = tree.getKeyValue(w);
        assertEquals(w, kv.getKey());
        assertEquals(val(w), kv.getValue());
      }
    }

    @Test
    void manyIterator() throws IOException {
      manyPut();
      TreeMap<String, Integer> m = getManyTree();
      Iterator<KeyValue<String, Integer>> iter = tree.iterator();
      int items = 0;
      while (iter.hasNext()) {
        items++;
        String w = iter.next().getKey();
        assertTrue(m.containsKey(w));
      }
      assertEquals(m.size(), items);
    }
  }


}
