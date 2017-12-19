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

import org.junit.*;
import org.mellowtech.core.TestUtils;
import org.mellowtech.core.util.MapEntry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by msvens on 09/11/15.
 */
public abstract class MapTemplate {

  public Map<String, Integer> map;

  public static String dir = "discmaptests";
  public static Path absPath(String fName){
    return TestUtils.getAbsolutePath(dir+"/"+fName);
  }
  public static String chars = "abcdefghijklmn0123456789";
  public static int MAX_WORD_LENGTH = 20;
  public static int MAX_BYTES = 4000;

  public static String[] manyWords;
  public static String[] mwSort, mwDesc;
  static {
    manyWords = TestUtils.randomWords(chars, MAX_WORD_LENGTH, MAX_BYTES);
    mwSort = Arrays.copyOf(manyWords, manyWords.length);
    mwDesc = Arrays.copyOf(manyWords, manyWords.length);
    Arrays.sort(mwSort);
    Arrays.sort(mwDesc, Collections.reverseOrder());
  }



  public static String[] words = new String[]{"hotel", "delta", "alpha", "bravo",
      "india","echo", "foxtrot", "juliet", "charlie", "golf"};

  public static String[] swords = new String[]{"alpha", "bravo", "charlie",
  "delta", "echo", "foxtrot", "golf", "hotel", "india", "juliet"};

  public static String[] rwords = new String[]{"juliet", "india", "hotel", "golf",
  "foxtrot", "echo", "delta", "charlie", "bravo", "alpha"};

  public static String treeHalfWord = "dalta";
  public static String fourHalfWord = "dilta";
  public static String manySmaller = "0";
  public static String manyLarger = "o";




  //abstract String fName();

  //abstract DiscMap<String,Integer>
  //init(String fileName, int valueBlockSize, int indexBlockSize, int maxValueBlocks, int maxIndexBlocks) throws Exception;
  public abstract Map <String, Integer> init() throws Exception;

  //abstract DiscMap<String,Integer> reopen(String fileName) throws Exception;

  @BeforeClass
  public static void createDir(){
    if(Files.exists(TestUtils.getAbsolutePath(dir)))
      TestUtils.deleteTempDir(dir);
    TestUtils.createTempDir(dir);
  }

  @Before
  public void setup() throws Exception{
    //map = init(TestUtils.getAbsolutDir(dir+"/"+fName()), 1024, 1024, 15, 5);
    map = init();
  }

  /*@After
  public void after() throws Exception{
    map.close();
    map.delete();
  }*/

  @AfterClass
  public static void deleteDir(){
    TestUtils.deleteTempDir(dir);
  }


  /****************overwritten disc hmap methods******************************/
  /*public void save() throws IOException {
    eht.save();
  }

  public void close() throws IOException{
    eht.close();
  }

  @Override
  public void compact() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete() throws IOException {
    eht.delete();
  }*/




  /*****test empty map**************************/
  /*@Test
  public void emptyIterator() {
    Assert.assertFalse(map.iterator().hasNext());
  }*/

  @Test
  public void emptySize() {
    Assert.assertEquals(0, map.size());
  }

  @Test
  public void emptyIsEmpty() {
    Assert.assertTrue(map.isEmpty());
  }

  @Test
  public void emptyContainsKey() {
    Assert.assertFalse(map.containsKey(swords[0]));
  }

  @Test
  public void emptyContainsValue() {
    Assert.assertFalse(map.containsValue(swords[0].length()));
  }

  @Test
  public void emptyGet() {
    Assert.assertNull(map.get(swords[0]));
  }

  /*@Test
  public void emptyReopen() throws Exception{
    map.close();
    map = reopen(TestUtils.getAbsolutDir(dir+"/"+fName()));
    Assert.assertNull(map.get(firstWord));
  }*/

  @Test
  public void emptyRemove(){
    Assert.assertNull(map.remove(swords[0]));
  }

  /*
  @Override
  public void putAll(Map<? extends A,? extends C> m) {
    for (Map.Entry<? extends A, ? extends C> e : m.entrySet()) {
      this.put(e.getKey(), e.getValue());
    }
  }
  */
  @Test
  public void emptyClear(){
    map.clear();
    Assert.assertTrue(map.isEmpty());
  }

  @Test
  public void emptyKeySet(){
    Set <String> keySet = map.keySet();
    Assert.assertEquals(0, keySet.size());
  }

  @Test
  public void emptyValues(){
    Collection<Integer> values = map.values();
    Assert.assertEquals(0, values.size());
  }

  @Test
  public void emptyEntrySet(){
    Set <Entry<String,Integer>> entrySet = map.entrySet();
    Assert.assertEquals(0, entrySet.size());
  }

  /*****test 1 item map**************************/
  protected void onePut(){
    map.put(swords[0], swords[0].length());
  }

  /*@Test
  public void oneIterator() {
    onePut();
    Assert.assertTrue(map.iterator().hasNext());
  }*/

  @Test
  public void oneSize() {
    onePut();
    Assert.assertEquals(1, map.size());
  }

  @Test
  public void oneIsEmpty() {
    onePut();
    Assert.assertFalse(map.isEmpty());
  }

  @Test
  public void oneContainsKey() {
    onePut();
    Assert.assertTrue(map.containsKey(swords[0]));
  }

  @Test
  public void oneContainsValue() {
    onePut();
    Assert.assertTrue(map.containsValue(swords[0].length()));
  }

  @Test
  public void oneGet() {
    onePut();
    Assert.assertEquals((Integer) swords[0].length(), map.get(swords[0]));
  }

  @Test
  public void oneRemove(){
    onePut();
    Assert.assertEquals((Integer)swords[0].length(), map.remove(swords[0]));
  }

  @Test
  public void oneClear(){
    onePut();
    map.clear();
    Assert.assertTrue(map.isEmpty());
  }

  @Test
  public void oneKeySet(){
    onePut();
    Set <String> keySet = map.keySet();
    Assert.assertTrue(keySet.contains(swords[0]));
  }

  @Test
  public void oneValues(){
    onePut();
    Collection<Integer> values = map.values();
    Assert.assertTrue(values.contains(swords[0].length()));
  }

  @Test
  public void oneEntrySet(){
    onePut();
    Set <Entry<String,Integer>> entrySet = map.entrySet();
    List <Entry<String,Integer>> list = new ArrayList<>();
    list.addAll(entrySet);
    Assert.assertEquals(swords[0], list.get(0).getKey());
    Assert.assertEquals((Integer)swords[0].length(), list.get(0).getValue());
  }

  /*****test 10 item map**************************/
  protected void tenPut(){
    for(String w : words) {
      map.put(w, w.length());
    }
  }

  /*@Test
  public void tenIterator() {
    tenPut();
    int tot = 0;
    Iterator<Entry<String,Integer>>  iter = map.iterator();
    while(iter.hasNext()){
      tot++;
      iter.next();
    }
    Assert.assertEquals(10, tot);
  }*/

  @Test
  public void tenSize() {
    tenPut();
    Assert.assertEquals(10, map.size());
  }

  @Test
  public void tenIsEmpty() {
    tenPut();
    Assert.assertFalse(map.isEmpty());
  }

  @Test
  public void tenContainsKey() {
    tenPut();
    for(String w : words) {
      Assert.assertTrue(map.containsKey(w));
    }
  }

  @Test
  public void tenContainsValue() {
    tenPut();
    for(String w : words) {
      Assert.assertTrue(map.containsValue(w.length()));
    }

  }

  @Test
  public void tenGet() {
    tenPut();
    for(String w : words){
      Assert.assertEquals((Integer)w.length(), map.get(w));
    }
  }

  @Test
  public void tenRemove(){
    tenPut();
    for(String w : words){
      Assert.assertEquals((Integer)w.length(), map.remove(w));
    }
    Assert.assertTrue(map.isEmpty());
  }

  @Test
  public void tenClear(){
    tenPut();
    map.clear();
    Assert.assertTrue(map.isEmpty());
  }

  @Test
  public void tenKeySet(){
    tenPut();
    Set <String> keySet = map.keySet();
    for(String w : words){
      Assert.assertTrue(keySet.contains(w));
    }
    Assert.assertEquals(10, keySet.size());
  }

  @Test
  public void tenValues(){
    tenPut();
    Collection<Integer> values = map.values();
    for(String w : words){
      Assert.assertTrue(values.contains(w.length()));
    }
    Assert.assertEquals(10, values.size());
  }

  @Test
  public void tenEntrySet(){
    tenPut();
    Set <Entry<String,Integer>> entrySet = map.entrySet();
    for(String w : words) {
      Entry<String, Integer> e = new MapEntry<>(w, w.length());
      Assert.assertTrue(entrySet.contains(e));
    }
    Assert.assertEquals(10, entrySet.size());
  }

  protected void manyPut(){
    for(String w : manyWords){
      map.put(w, w.length());
    }
  }

  /*****test 10 item map**************************/
  /*@Test
  public void manyIterator() {
    manyPut();
    TreeMap <String, Integer> m = getManyTree();
    Iterator <Entry<String, Integer>> iter = map.iterator();
    int items = 0;
    while(iter.hasNext()){
      items++;
      String w = iter.next().getKey();
      Assert.assertTrue(m.containsKey(w));
    }
    Assert.assertEquals(m.size(), items);
  }*/

  @Test
  public void manySize() {
    manyPut();
    Assert.assertEquals(manyWords.length, map.size());
  }

  @Test
  public void manyIsEmpty() {
    manyPut();
    Assert.assertFalse(map.isEmpty());
  }

  @Test
  public void manyContainsKey() {
    manyPut();
    for(String w : manyWords) {
      Assert.assertTrue(map.containsKey(w));
    }
  }

  @Test
  public void manyContainsValue() {
    manyPut();
    for(String w : manyWords) {
      Assert.assertTrue(map.containsValue(w.length()));
    }

  }

  @Test
  public void manyGet() {
    manyPut();
    for(String w : manyWords){
      Assert.assertEquals((Integer)w.length(), map.get(w));
    }
  }

  @Test
  public void manyRemove(){
    manyPut();
    for(String w : manyWords){
      Assert.assertEquals((Integer)w.length(), map.remove(w));
    }
    Assert.assertTrue(map.isEmpty());
  }

  @Test
  public void manyClear(){
    manyPut();
    map.clear();
    Assert.assertTrue(map.isEmpty());
  }

  @Test
  public void manyKeySet(){
    manyPut();
    Set <String> keySet = map.keySet();
    for(String w : manyWords){
      Assert.assertTrue(keySet.contains(w));
    }
    Assert.assertEquals(manyWords.length, keySet.size());
  }

  @Test
  public void manyValues(){
    manyPut();
    Collection<Integer> values = map.values();
    for(String w : manyWords){
      Assert.assertTrue(values.contains(w.length()));
    }
    Assert.assertEquals(manyWords.length, values.size());
  }

  @Test
  public void manyEntrySet(){
    manyPut();
    Set <Entry<String,Integer>> entrySet = map.entrySet();
    for(String w : manyWords) {
      Entry<String, Integer> e = new MapEntry<>(w, w.length());
      Assert.assertTrue(entrySet.contains(e));
    }
    Assert.assertEquals(manyWords.length, entrySet.size());
  }



}
