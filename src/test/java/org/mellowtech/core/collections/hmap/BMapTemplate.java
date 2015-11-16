package org.mellowtech.core.collections.hmap;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mellowtech.core.TestUtils;
import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.collections.BMap;
import org.mellowtech.core.collections.KeyValue;
import org.mellowtech.core.collections.tree.BTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by msvens on 01/11/15.
 */
public abstract class BMapTemplate {

  BMap<String, CBString, Integer, CBInt> tree;
  public static String dir = "rftests";
  public static String chars = "abcdefghijklmn0123456789";
  public static int MAX_WORD_LENGTH = 20;
  public static int MAX_BYTES = 4000;

  public static CBString[] manyWords = TestUtils.randomWords(chars, MAX_WORD_LENGTH, MAX_BYTES);

  public static CBString[] words = new CBString[]{
      new CBString("hotel"), new CBString("delta"),
      new CBString("alpha"), new CBString("bravo"),
      new CBString("india"), new CBString("echo"),
      new CBString("foxtrot"), new CBString("juliet"),
      new CBString("charlie"), new CBString("golf")};

  public static CBString firstWord = new CBString("alpha");
  public static CBString forthWord = new CBString("delta");


  abstract String fName();

  abstract BMap<String,CBString,Integer,CBInt>
    init(String fileName, int bucketSize, int maxBuckets) throws Exception;

  abstract BMap<String,CBString,Integer,CBInt> reopen(String fileName) throws Exception;

  @BeforeClass
  public static void createDir(){
    TestUtils.deleteTempDir(dir);
    TestUtils.createTempDir(dir);
  }

  @Before
  public void setup() throws Exception{
    tree = init(TestUtils.getAbsolutDir(dir+"/"+fName()), 1024, 16);
  }

  @After
  public void after() throws Exception{
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
    tree = reopen(TestUtils.getAbsolutDir(dir+"/"+fName()));
    Assert.assertNull(tree.get(firstWord));
  }

  @Test
  public void emptyGetKeyValue() throws IOException{
    Assert.assertNull(tree.getKeyValue(firstWord));
  }

  @Test
  public void emptyIterator() throws IOException{
    Assert.assertFalse(tree.iterator().hasNext());
  }

  /***********one item tree tests*****************************/
  protected CBInt val(CBString key){
    return new CBInt(key.get().length());
  }

  @Test
  public void oneSize() throws IOException{
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
  public void oneRemove() throws IOException{
    tree.put(firstWord, val(firstWord));
    Assert.assertEquals(val(firstWord), tree.remove(firstWord));
  }

  @Test
  public void oneGet() throws IOException{
    tree.put(firstWord, val(firstWord));
    Assert.assertEquals(val(firstWord), tree.get(firstWord));
  }

  @Test
  public void oneReopen() throws Exception{
    tree.put(firstWord, val(firstWord));
    tree.close();
    tree = reopen(TestUtils.getAbsolutDir(dir+"/"+fName()));
    Assert.assertEquals(val(firstWord), tree.get(firstWord));
  }

  @Test
  public void oneGetKeyValue() throws IOException{
    tree.put(firstWord, val(firstWord));
    KeyValue<CBString, CBInt> kv = tree.getKeyValue(firstWord);
    Assert.assertEquals(firstWord, kv.getKey());
    Assert.assertEquals(val(firstWord), kv.getValue());
  }

  @Test
  public void oneIterator() throws IOException{
    tree.put(firstWord, val(firstWord));
    Assert.assertTrue(tree.iterator().hasNext());
  }

  /***********ten item tree tests*****************************/
  protected void fillTree() throws IOException{
    for(CBString w : words){
      tree.put(w, val(w));
    }
  }
  @Test
  public void tenSize() throws IOException{
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
    for(CBString w : words) {
      Assert.assertTrue(tree.containsKey(w));
    }
  }

  @Test
  public void tenRemove() throws IOException{
    fillTree();
    for(CBString w : words)
      Assert.assertEquals(val(w), tree.remove(w));
  }

  @Test
  public void tenGet() throws IOException{
    fillTree();
    for(CBString w : words)
      Assert.assertEquals(val(w), tree.get(w));
  }

  @Test
  public void tenReopen() throws Exception{
    fillTree();
    tree.close();
    tree = reopen(TestUtils.getAbsolutDir(dir+"/"+fName()));
    for(CBString w : words)
      Assert.assertEquals(val(w), tree.get(w));
  }

  @Test
  public void tenGetKeyValue() throws IOException{
    fillTree();
    for(CBString w : words) {
      KeyValue<CBString, CBInt> kv = tree.getKeyValue(w);
      Assert.assertEquals(w, kv.getKey());
      Assert.assertEquals(val(w), kv.getValue());
    }
  }

  @Test
  public void tenIterator() throws IOException{
    fillTree();
    Iterator iter = tree.iterator();
    int tot = 0;
    while(iter.hasNext()){
      tot++;
      iter.next();
    }
    Assert.assertEquals(10, tot);
  }

  /**********many item tree tests*****************************/
  protected void fillManyTree() throws IOException{
    for(CBString w : manyWords){
      tree.put(w, val(w));
    }
  }
  protected TreeMap <CBString,CBInt> getManyTree(){
    TreeMap <CBString,CBInt> m = new TreeMap<>();
    for(CBString w : manyWords){
      m.put(w, val(w));
    }
    return m;
  }

  @Test
  public void manySize() throws IOException{
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
    for(CBString w : manyWords) {
      Assert.assertTrue(tree.containsKey(w));
    }
  }

  @Test
  public void manyRemove() throws IOException{
    fillManyTree();
    int i = 0;
    for(CBString w : manyWords) {
      Assert.assertEquals(val(w), tree.remove(w));
    }
  }

  @Test
  public void manyGet() throws IOException{
    fillManyTree();
    for(CBString w : manyWords)
      Assert.assertEquals(val(w), tree.get(w));
  }

  @Test
  public void manyReopen() throws Exception{
    fillManyTree();
    tree.close();
    tree = reopen(TestUtils.getAbsolutDir(dir+"/"+fName()));
    for(CBString w : manyWords) {
      Assert.assertEquals(val(w), tree.get(w));
    }
  }

  @Test
  public void manyGetKeyValue() throws IOException{
    fillManyTree();
    for(CBString w : manyWords) {
      KeyValue<CBString, CBInt> kv = tree.getKeyValue(w);
      Assert.assertEquals(w, kv.getKey());
      Assert.assertEquals(val(w), kv.getValue());
    }
  }

  @Test
  public void manyIterator() throws IOException{
    fillManyTree();
    TreeMap <CBString, CBInt> m = getManyTree();
    Iterator <KeyValue<CBString,CBInt>> iter = tree.iterator();
    int items = 0;
    while(iter.hasNext()){
      items++;
      CBString w = iter.next().getKey();
      Assert.assertTrue(m.containsKey(w));
    }
    Assert.assertEquals(m.size(), items);
  }



}
