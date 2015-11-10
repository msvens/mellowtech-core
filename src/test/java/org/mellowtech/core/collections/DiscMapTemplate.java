package org.mellowtech.core.collections;

import org.junit.*;
import org.mellowtech.core.CoreLog;
import org.mellowtech.core.TestUtils;
import org.mellowtech.core.bytestorable.ByteStorableException;
import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.collections.DiscMap;
import org.mellowtech.core.collections.KeyValue;
import org.mellowtech.core.util.MapEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

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

  /*
  @Override
  public void putAll(Map<? extends A,? extends C> m) {
    for (Map.Entry<? extends A, ? extends C> e : m.entrySet()) {
      this.put(e.getKey(), e.getValue());
    }
  }
  */
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

}
