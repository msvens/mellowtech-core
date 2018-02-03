package org.mellowtech.core.collections;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by msvens on 11/11/15.
 */
public abstract class SortedDiscMapTemplate extends NavigableMapTemplate {

  public static int IDX_BLK_SIZE = 1024;
  public static int VAL_BLK_SIZE = 1024;
  public static int IDX_BLKS = 5;
  public static int VAL_BLKS = 15;

  public SortedDiscMap <String, Integer> sdm() {
    return (SortedDiscMap <String, Integer>) map;
  };

  public abstract DiscMap <String, Integer> reopen() throws Exception;

  @AfterEach
  public void after() throws Exception{
    sdm().close();
    sdm().delete();
  }

  /*****test empty map**************************/
  @Test
  public void zeroIterator() {
    assertFalse(sdm().iterator().hasNext());
  }

  @Test
  public void zeroIteratorFrom() throws IOException {
    assertFalse(sdm().iterator(swords[0]).hasNext());
  }

  @Test
  public void zeroReopen() throws Exception{
    sdm().close();
    map = reopen();
    assertNull(sdm().get(swords[0]));
  }


  /*****test 1 item map**************************/
  @Test
  public void oneIterator() {
    onePut();
    assertTrue(sdm().iterator().hasNext());
  }

  @Test
  public void oneIteratorFrom() throws IOException {
    onePut();
    assertTrue(sdm().iterator(swords[0]).hasNext());
  }

  @Test
  public void oneReopen() throws Exception{
    onePut();
    sdm().close();
    map = reopen();
    assertEquals((Integer) swords[0].length(), sdm().get(swords[0]));
  }

  /*****test 10 item map**************************/

  @Test
  public void tenIterator() {
    tenPut();
    int tot = 0;
    Iterator<Map.Entry<String,Integer>>  iter = sdm().iterator();
    while(iter.hasNext()){
      tot++;
      iter.next();
    }
    assertEquals(10, tot);
  }

  @Test
  public void tenIteratorFrom() throws IOException {
    tenPut();
    int tot = 0;
    Iterator iter = sdm().iterator(swords[3]);
    while(iter.hasNext()){
      tot++;
      iter.next();
    }
    assertEquals(7, tot);
  }

  @Test
  public void tenReopen() throws Exception {
    tenPut();
    sdm().close();
    map = reopen();
    for (String w : words) {
      assertEquals((Integer) w.length(), sdm().get(w));
    }
  }

  /*****test 10 item map**************************/
  @Test
  public void manyReopen() throws Exception{
    manyPut();
    sdm().close();
    map = reopen();
    for(String w : manyWords){
      assertEquals((Integer)w.length(), sdm().get(w));
    }
  }

  @Test
  public void manyIterator() {
    manyPut();
    Iterator <Entry<String, Integer>> iter = sdm().iterator();
    int items = 0;
    while(iter.hasNext()){
      String w = iter.next().getKey();
      assertTrue(Arrays.binarySearch(mwSort, w) >= 0);
      items++;
    }
    assertEquals(mwSort.length, items);
  }

  @Test
  public void manyIteratorFrom() throws IOException {
    manyPut();
    int item = 10;
    String key = mwSort[item];
    Iterator <Entry<String, Integer>> iter = sdm().iterator(key);

    while(iter.hasNext()){
      assertEquals(mwSort[item], iter.next().getKey());
      item++;
    }
    assertEquals(mwSort.length, item);
  }





}
