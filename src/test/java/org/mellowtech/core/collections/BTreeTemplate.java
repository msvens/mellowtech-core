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

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;

import org.junit.Test;
import org.mellowtech.core.TestUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by msvens on 01/11/15.
 */
public abstract class BTreeTemplate {

  BTree <String, Integer> tree;
  public String dir = "rftests";
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

  public abstract BTree<String,Integer>
    init(Path fileName, int valueBlockSize, int indexBlockSize, int maxValueBlocks, int maxIndexBlocks) throws Exception;

  public abstract BTree<String,Integer> reopen(Path fileName, int valueBlockSize, int indexBlockSize, int maxValueBlocks, int maxIndexBlocks) throws Exception;

  /*@BeforeClass
  public static void createDir(){
    TestUtils.deleteTempDir(dir);
    TestUtils.createTempDir(dir);
  }*/

  @Before
  public void setup() throws Exception{
    dir = "rftests"+ThreadLocalRandom.current().nextInt();
    TestUtils.createTempDir(dir);
    tree = init(TestUtils.getAbsolutePath(dir+"/"+fName()), 1024, 1024, 20, 5);
    //tree = init(TestUtils.getAbsolutDir(dir+"/"+fName()), 124, 124, 200, 200);
  }

  @After
  public void after() throws Exception{
    tree.close();
    tree.delete();
    TestUtils.deleteTempDir(dir);
    //TestUtils.deleteTempDir(dir);
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

  /***********empty tree tests*****************************/
  @Test
  public void emptySize() throws IOException{
    Assert.assertEquals(0, tree.size());
  }

  @Test
  public void emptyIsEmpty() throws IOException {
    Assert.assertTrue(tree.isEmpty());
  }

  @Test
  public void emptyTruncate() throws IOException {
    tree.truncate();
    Assert.assertEquals(0, tree.size());
  }

  @Test
  public void emptyContainsKey() throws IOException {
    Assert.assertFalse(tree.containsKey(firstWord));
  }

  @Test
  public void emptyRemove() throws IOException{
    Assert.assertNull(tree.remove(firstWord));
  }

  @Test
  public void emptyGet() throws IOException{
    Assert.assertNull(tree.get(firstWord));
  }

  @Test
  public void emptyReopen() throws Exception{
    tree.close();
    tree = reopen(TestUtils.getAbsolutePath(dir+"/"+fName()),1024, 1024, 20, 5);
    Assert.assertNull(tree.get(firstWord));
  }

  @Test
  public void emptyGetKeyValue() throws IOException{
    Assert.assertNull(tree.getKeyValue(firstWord));
  }

  @Test
  public void zeroIterator() throws Exception{
    Assert.assertFalse(tree.iterator().hasNext());

  }

  @Test
  public void zeroIteratorRangeInclusive() throws Exception{
    Assert.assertFalse(tree.iterator(false, ascend[0], true, ascend[9], true).hasNext());
  }

  @Test
  public void zeroIteratorRangeExclusive() throws Exception{
    Assert.assertFalse(tree.iterator(false, ascend[0], false, ascend[9], false).hasNext());
  }

  @Test
  public void zeroReverseIterator() throws Exception{
    Assert.assertFalse(tree.iterator(true).hasNext());
  }

  @Test
  public void zeroReverseIteratorRangeInclusive() throws Exception{
    Assert.assertFalse(tree.iterator(true, ascend[9], true, ascend[0], true).hasNext());
  }

  @Test
  public void zeroReverseIteratorRangeExclusive() throws Exception{
    Assert.assertFalse(tree.iterator(true, ascend[9], false, ascend[0], false).hasNext());
  }


  @Test(expected = IOException.class)
  public void emptyGetKey() throws IOException{
    tree.getKey(0);
  }

  @Test
  public void emptyGetPosition() throws IOException{
    Assert.assertNull(tree.getPosition(firstWord));
  }

  @Test
  public void emptyGetPositionWithMissing() throws IOException{
    Assert.assertEquals(0,tree.getPositionWithMissing(firstWord).getSmaller());
  }

  protected Integer val(String key){
    return key.length();
  }

  /***********one item tree tests*****************************/
  protected void onePut() throws IOException{
    tree.put(ascend[0], val(ascend[0]));
  }
  @Test
  public void oneSize() throws IOException{
    onePut();
    Assert.assertEquals(1, tree.size());
  }

  @Test
  public void oneTruncate() throws IOException {
    onePut();
    tree.truncate();
    Assert.assertEquals(0, tree.size());
  }

  @Test
  public void oneIsEmpty() throws IOException {
    onePut();
    Assert.assertFalse(tree.isEmpty());
  }

  @Test
  public void oneContainsKey() throws IOException {
    onePut();
    Assert.assertTrue(tree.containsKey(ascend[0]));
  }

  @Test
  public void oneRemove() throws IOException{
    onePut();
    Assert.assertEquals(val(ascend[0]), tree.remove(ascend[0]));
  }

  @Test
  public void oneGet() throws IOException{
    onePut();
    Assert.assertEquals(val(ascend[0]), tree.get(ascend[0]));
  }

  @Test
  public void oneReopen() throws Exception{
    onePut();
    tree.close();
    tree = reopen(TestUtils.getAbsolutePath(dir+"/"+fName()),1024, 1024, 20, 5);
    Assert.assertEquals(val(ascend[0]), tree.get(ascend[0]));
  }

  @Test
  public void oneGetKeyValue() throws IOException{
    onePut();
    KeyValue<String, Integer> kv = tree.getKeyValue(ascend[0]);
    Assert.assertEquals(ascend[0], kv.getKey());
    Assert.assertEquals(val(ascend[0]), kv.getValue());
  }

  @Test
  public void oneIterator() throws Exception{
    onePut();
    int i = 0;
    Iterator<KeyValue<String, Integer>> iter = tree.iterator();
    while(iter.hasNext()){
      org.junit.Assert.assertEquals(ascend[0], iter.next().getKey());
      i++;
    }
    org.junit.Assert.assertEquals(1, i);
  }

  @Test
  public void oneIteratorRangeInclusive() throws Exception{
    onePut();
    Iterator<KeyValue<String, Integer>> iter = tree.iterator(false, ascend[0], true, ascend[9], true);
    Assert.assertTrue(iter.hasNext());
  }

  @Test
  public void oneIteratorRangeExclusive() throws Exception{
    onePut();
    Iterator<KeyValue<String, Integer>> iter = tree.iterator(false, ascend[0], false, ascend[9], false);
    Assert.assertFalse(iter.hasNext());
  }

  @Test
  public void oneReverseIterator() throws Exception{
    onePut();
    int i = 0;
    Iterator<KeyValue<String, Integer>> iter = tree.iterator(true);
    while(iter.hasNext()){
      Assert.assertEquals(ascend[i], iter.next().getKey());
      i--;
    }
    Assert.assertEquals(-1, i);
  }

  @Test
  public void oneReverseIteratorRangeInclusive() throws Exception{
    onePut();
    Iterator<KeyValue<String, Integer>> iter = tree.iterator(true, ascend[9], true, ascend[0], true);
    Assert.assertTrue(iter.hasNext());
  }

  @Test
  public void oneReverseIteratorRangeExclusive() throws Exception{
    onePut();
    Iterator<KeyValue<String, Integer>> iter = tree.iterator(true, ascend[9], false, ascend[0], false);
    Assert.assertFalse(iter.hasNext());
  }

  @Test
  public void oneGetKey() throws IOException{
    tree.put(firstWord, val(firstWord));
    Assert.assertEquals(firstWord, tree.getKey(0));
  }

  @Test
  public void oneGetPosition() throws IOException{
    tree.put(firstWord, val(firstWord));
    Assert.assertNotNull(tree.getPosition(firstWord));
  }

  @Test
  public void oneGetPositionWithMissing() throws IOException{
    tree.put(firstWord, val(firstWord));
    Assert.assertEquals(0,tree.getPositionWithMissing(firstWord).getSmaller());
  }

  /***********ten item tree tests*****************************/
  protected void tenPut() throws IOException{
    for(String w : words){
      tree.put(w, val(w));
    }
  }
  @Test
  public void tenSize() throws IOException{
    tenPut();
    Assert.assertEquals(words.length, tree.size());
  }

  @Test
  public void tenTruncate() throws IOException {
    tree.truncate();
    Assert.assertEquals(0, tree.size());
  }

  @Test
  public void tenIsEmpty() throws IOException {
    tenPut();
    Assert.assertFalse(tree.isEmpty());
  }

  @Test
  public void tenContainsKey() throws IOException {
    tenPut();
    for(String w : words) {
      Assert.assertTrue(tree.containsKey(w));
    }
  }

  @Test
  public void tenRemove() throws IOException{
    tenPut();
    for(String w : words)
      Assert.assertEquals(val(w), tree.remove(w));
  }

  @Test
  public void tenGet() throws IOException{
    tenPut();
    for(String w : words)
      Assert.assertEquals(val(w), tree.get(w));
  }

  @Test
  public void tenReopen() throws Exception{
    tenPut();
    tree.close();
    tree = reopen(TestUtils.getAbsolutePath(dir+"/"+fName()),1024, 1024, 20, 5);
    for(String w : words)
      Assert.assertEquals(val(w), tree.get(w));
  }

  @Test
  public void tenGetKeyValue() throws IOException{
    tenPut();
    for(String w : words) {
      KeyValue<String, Integer> kv = tree.getKeyValue(w);
      Assert.assertEquals(w, kv.getKey());
      Assert.assertEquals(val(w), kv.getValue());
    }
  }

  @Test
  public void tenIterator() throws Exception{
    tenPut();
    int i = 0;
    Iterator<KeyValue<String, Integer>> iter = tree.iterator();
    while(iter.hasNext()){
      org.junit.Assert.assertEquals(ascend[i], iter.next().getKey());
      i++;
    }
    org.junit.Assert.assertEquals(10, i);
  }

  @Test
  public void tenIteratorRangeInclusive() throws Exception{
    tenPut();
    int i = 1;
    Iterator<KeyValue<String, Integer>> iter = tree.iterator(false, ascend[1], true, ascend[8], true);
    while(iter.hasNext()){
      org.junit.Assert.assertEquals(ascend[i], iter.next().getKey());
      i++;
    }
    org.junit.Assert.assertEquals(9, i);
  }

  @Test
  public void tenIteratorRangeExclusive() throws Exception{
    tenPut();
    int i = 2;
    Iterator<KeyValue<String, Integer>> iter = tree.iterator(false, ascend[1], false, ascend[8], false);
    while(iter.hasNext()){
      org.junit.Assert.assertEquals(ascend[i], iter.next().getKey());
      i++;
    }
    org.junit.Assert.assertEquals(8, i);
  }

  @Test
  public void tenReverseIterator() throws Exception{
    tenPut();
    int i = 9;
    Iterator<KeyValue<String, Integer>> iter = tree.iterator(true);
    while(iter.hasNext()){
      org.junit.Assert.assertEquals(ascend[i], iter.next().getKey());
      i--;
    }
    org.junit.Assert.assertEquals(-1, i);
  }

  @Test
  public void tenReverseIteratorRangeInclusive() throws Exception{
    tenPut();
    int i = 8;
    Iterator<KeyValue<String, Integer>> iter = tree.iterator(true, ascend[8], true, ascend[1], true);
    while(iter.hasNext()){
      org.junit.Assert.assertEquals(ascend[i], iter.next().getKey());
      i--;
    }
    org.junit.Assert.assertEquals(0, i);
  }

  @Test
  public void tenReverseIteratorRangeExclusive() throws Exception{
    tenPut();
    int i = 7;
    Iterator<KeyValue<String, Integer>> iter = tree.iterator(true, ascend[8], false, ascend[1], false);
    while(iter.hasNext()){
      org.junit.Assert.assertEquals(ascend[i], iter.next().getKey());
      i--;
    }
    org.junit.Assert.assertEquals(1, i);
  }

  @Test
  public void tenGetKey() throws IOException{
    tenPut();
    Assert.assertEquals(firstWord, tree.getKey(0));
  }

  @Test
  public void tenGetPosition() throws IOException{
    tenPut();
    Assert.assertNotNull(tree.getPosition(firstWord));
  }

  @Test
  public void tenGetPositionWithMissing() throws IOException{
    tenPut();
    Assert.assertEquals(3,tree.getPositionWithMissing(forthWord).getSmaller());
  }

  /**********many item tree tests*****************************/
  protected void putMany() throws IOException{
    for(String w : manyWords){
      tree.put(w, val(w));

    }
  }
  protected TreeMap <String,Integer> getManyTree(){
    TreeMap <String,Integer> m = new TreeMap<>();
    for(String w : manyWords){
      m.put(w, val(w));
    }
    return m;
  }

  @Test
  public void manyCreateIndex() throws IOException {
    putMany();
    tree.save();
    try{
      tree.rebuildIndex();
    } catch(UnsupportedOperationException uoe){
      return;
    }
  }

  @Test
  public void manySize() throws IOException{
    putMany();
    Assert.assertEquals(manyWords.length, tree.size());
  }

  @Test
  public void manyTruncate() throws IOException {
    putMany();
    tree.truncate();
    Assert.assertEquals(0, tree.size());
  }

  @Test
  public void manyIsEmpty() throws IOException {
    putMany();
    Assert.assertFalse(tree.isEmpty());
  }

  @Test
  public void manyContainsKey() throws IOException {
    putMany();
    //System.out.println(tree.toString());
    for(String w : manyWords) {
      Assert.assertTrue(tree.containsKey(w));
    }
  }

  @Test
  public void manyRemove() throws IOException{
    putMany();
    tree.save();
    for(String w : manyWords) {
      Integer rem = tree.remove(w);
      if(rem == null){
        System.out.println("this is word: "+w);
      }
      Assert.assertEquals(val(w), rem);
    }
  }

  @Test
  public void manyGet() throws IOException{
    putMany();
    for(String w : manyWords)
      Assert.assertEquals(val(w), tree.get(w));
  }

  @Test
  public void manyReopen() throws Exception{
    putMany();
    tree.close();
    tree = reopen(TestUtils.getAbsolutePath(dir+"/"+fName()),1024, 1024, 20, 5);
    for(String w : manyWords)
      Assert.assertEquals(val(w), tree.get(w));
  }

  @Test
  public void manyGetKeyValue() throws IOException{
    putMany();
    for(String w : manyWords) {
      KeyValue<String, Integer> kv = tree.getKeyValue(w);
      Assert.assertEquals(w, kv.getKey());
      Assert.assertEquals(val(w), kv.getValue());
    }
  }

  @Test
  public void manyIterator() throws Exception{
    putMany();
    int from = 0;
    int to = mAscend.length - 1;
    Iterator<KeyValue<String, Integer>> iter = tree.iterator();
    while(iter.hasNext()){
      org.junit.Assert.assertEquals(mAscend[from], iter.next().getKey());
      from++;
    }
    from--;
    org.junit.Assert.assertEquals(from, to);
  }

  @Test
  public void manyIteratorRangeInclusive() throws Exception{
    putMany();
    int from = 50;
    int to = mAscend.length - 50;
    Iterator<KeyValue<String, Integer>> iter = tree.iterator(false, mAscend[from], true, mAscend[to], true);
    while(iter.hasNext()){
      org.junit.Assert.assertEquals(mAscend[from], iter.next().getKey());
      from++;
    }
    from--;
    org.junit.Assert.assertEquals(to, from);
  }

  @Test
  public void manyIteratorRangeExclusive() throws Exception{
    putMany();
    int from = 51;
    int to = mAscend.length - 51;
    Iterator<KeyValue<String, Integer>> iter = tree.iterator(false, mAscend[from-1], false, mAscend[to+1], false);
    while(iter.hasNext()){
      org.junit.Assert.assertEquals(mAscend[from], iter.next().getKey());
      from++;
    }
    from--;
    org.junit.Assert.assertEquals(to, from);
  }

  @Test
  public void manyReverseIterator() throws Exception{
    putMany();
    int from = mAscend.length-1;
    int to = 0;
    Iterator<KeyValue<String, Integer>> iter = tree.iterator(true);
    while(iter.hasNext()){
      org.junit.Assert.assertEquals(mAscend[from], iter.next().getKey());
      from--;
    }
    from++;
    org.junit.Assert.assertEquals(to, from);
  }

  @Test
  public void manyReverseIteratorRangeInclusive() throws Exception{
    putMany();
    int from = mAscend.length - 50;
    int to = 50;
    Iterator<KeyValue<String, Integer>> iter = tree.iterator(true, mAscend[from], true, mAscend[to], true);
    while(iter.hasNext()){
      org.junit.Assert.assertEquals(mAscend[from], iter.next().getKey());
      from--;
    }
    from++;
    org.junit.Assert.assertEquals(to, from);
  }

  @Test
  public void manyReverseIteratorRangeExclusive() throws Exception{
    putMany();
    int from = mAscend.length - 51;
    int to = 51;

    Iterator<KeyValue<String, Integer>> iter = tree.iterator(true, mAscend[from+1], false, mAscend[to-1], false);
    while(iter.hasNext()){
      org.junit.Assert.assertEquals(mAscend[from], iter.next().getKey());
      from--;
    }
    from++;
    if(from != to){
      //print many tre...
    }
    org.junit.Assert.assertEquals(from, to);
  }

  @Test
  public void manyGetKey() throws IOException{
    putMany();
    TreeMap <String, Integer> m = getManyTree();
    Assert.assertEquals(m.firstKey(), tree.getKey(0));
  }

  @Test
  public void manyGetPosition() throws IOException{
    putMany();
    TreeMap <String, Integer> m = getManyTree();
    Assert.assertEquals(0, tree.getPosition(m.firstKey()).getSmaller());
  }

  /*@Test
  public void tenGetPositionWithMissing() throws IOException{
    tenPut();
    Assert.assertEquals(3,tree.getPositionWithMissing(forthWord).smaller);
  }*/



}
