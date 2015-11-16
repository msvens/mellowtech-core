package org.mellowtech.core.collections;

import org.junit.*;
import org.junit.Assert;

import org.mellowtech.core.TestUtils;

import org.mellowtech.core.bytestorable.CBString;

import org.mellowtech.core.util.MapEntry;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by msvens on 09/11/15.
 */
public abstract class DiscMapTemplate {

  DiscMap<String, Integer> map;
  public static String dir = "discmaptests";
  public static String chars = "abcdefghijklmn0123456789";
  public static int MAX_WORD_LENGTH = 20;
  public static int MAX_BYTES = 4000;

  public static String[] manyWords = TestUtils.randomStrings(chars, MAX_WORD_LENGTH, MAX_BYTES);

  public static String[] words = new String[]{"hotel", "delta", "alpha", "bravo",
      "india","echo", "foxtrot", "juliet", "charlie", "golf"};

  public static String firstWord = "alpha";
  public static String forthWord = "delta";



  abstract String fName();

  abstract DiscMap<String,Integer>
  init(String fileName, int valueBlockSize, int indexBlockSize, int maxValueBlocks, int maxIndexBlocks) throws Exception;

  abstract DiscMap<String,Integer> reopen(String fileName) throws Exception;

  @BeforeClass
  public static void createDir(){
    if(Files.exists(Paths.get(TestUtils.getAbsolutDir(dir))))
      TestUtils.deleteTempDir(dir);
    TestUtils.createTempDir(dir);
  }

  @Before
  public void setup() throws Exception{
    map = init(TestUtils.getAbsolutDir(dir+"/"+fName()), 1024, 1024, 15, 5);
  }

  @After
  public void after() throws Exception{
    map.close();
    map.delete();
  }

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
  @Test
  public void emptyIterator() {
    Assert.assertFalse(map.iterator().hasNext());
  }

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
    Assert.assertFalse(map.containsKey(firstWord));
  }

  @Test
  public void emptyContainsValue() {
    Assert.assertFalse(map.containsValue(firstWord.length()));
  }

  @Test
  public void emptyGet() {
    Assert.assertNull(map.get(firstWord));
  }

  @Test
  public void emptyReopen() throws Exception{
    map.close();
    map = reopen(TestUtils.getAbsolutDir(dir+"/"+fName()));
    Assert.assertNull(map.get(firstWord));
  }

  @Test
  public void emptyRemove(){
    Assert.assertNull(map.remove(firstWord));
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
    Set <Map.Entry<String,Integer>> entrySet = map.entrySet();
    Assert.assertEquals(0, entrySet.size());
  }

  /*****test 1 item map**************************/
  protected void onePut(){
    map.put(firstWord, firstWord.length());
  }

  @Test
  public void oneIterator() {
    onePut();
    Assert.assertTrue(map.iterator().hasNext());
  }

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
    Assert.assertTrue(map.containsKey(firstWord));
  }

  @Test
  public void oneContainsValue() {
    onePut();
    Assert.assertTrue(map.containsValue(firstWord.length()));
  }

  @Test
  public void oneGet() {
    onePut();
    Assert.assertEquals((Integer) firstWord.length(), map.get(firstWord));
  }

  @Test
  public void oneReopen() throws Exception{
    onePut();
    map.close();
    map = reopen(TestUtils.getAbsolutDir(dir+"/"+fName()));
    Assert.assertEquals((Integer) firstWord.length(), map.get(firstWord));
  }

  @Test
  public void oneRemove(){
    onePut();
    Assert.assertEquals((Integer)firstWord.length(), map.remove(firstWord));
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
    Assert.assertTrue(keySet.contains(firstWord));
  }

  @Test
  public void oneValues(){
    onePut();
    Collection<Integer> values = map.values();
    Assert.assertTrue(values.contains(firstWord.length()));
  }

  @Test
  public void oneEntrySet(){
    onePut();
    Set <Map.Entry<String,Integer>> entrySet = map.entrySet();
    List <Map.Entry<String,Integer>> list = new ArrayList<>();
    list.addAll(entrySet);
    Assert.assertEquals(firstWord, list.get(0).getKey());
    Assert.assertEquals((Integer)firstWord.length(), list.get(0).getValue());
  }

  /*****test 10 item map**************************/
  protected void tenPut(){
    for(String w : words) {
      map.put(w, w.length());
    }
  }

  @Test
  public void tenIterator() {
    tenPut();
    int tot = 0;
    Iterator<Map.Entry<String,Integer>>  iter = map.iterator();
    while(iter.hasNext()){
      tot++;
      iter.next();
    }
    Assert.assertEquals(10, tot);
  }

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
  public void tenReopen() throws Exception{
    tenPut();
    map.close();
    map = reopen(TestUtils.getAbsolutDir(dir+"/"+fName()));
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
    Set <Map.Entry<String,Integer>> entrySet = map.entrySet();
    for(String w : words) {
      Map.Entry<String, Integer> e = new MapEntry<>(w, w.length());
      Assert.assertTrue(entrySet.contains(e));
    }
    Assert.assertEquals(10, entrySet.size());
  }

  protected void manyPut(){
    for(String w : manyWords){
      map.put(w, w.length());
    }
  }

  protected TreeMap <String, Integer> getManyTree(){
    TreeMap <String, Integer> m = new TreeMap<>();
    for(String w : manyWords){
      m.put(w, w.length());
    }
    return m;
  }

  /*****test 10 item map**************************/
  @Test
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
  }

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
  public void manyReopen() throws Exception{
    manyPut();
    map.close();
    map = reopen(TestUtils.getAbsolutDir(dir+"/"+fName()));
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
    Set <Map.Entry<String,Integer>> entrySet = map.entrySet();
    for(String w : manyWords) {
      Map.Entry<String, Integer> e = new MapEntry<>(w, w.length());
      Assert.assertTrue(entrySet.contains(e));
    }
    Assert.assertEquals(manyWords.length, entrySet.size());
  }



}
