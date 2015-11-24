package org.mellowtech.core.collections;

import org.junit.*;
import org.junit.Assert;

import org.mellowtech.core.TestUtils;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by msvens on 09/11/15.
 */
public abstract class DiscMapTemplate extends MapTemplate {

  static int IDX_BLK_SIZE = 1024;
  static int VAL_BLK_SIZE = 1024;
  static int IDX_BLKS = 5;
  static int VAL_BLKS = 15;

  DiscMap <String, Integer> dm() {
    return (DiscMap <String, Integer>) map;
  };

  abstract DiscMap <String, Integer> reopen() throws Exception;

  @After
  public void after() throws Exception{
    dm().close();
    dm().delete();
  }

  /*****test empty map**************************/
  @Test
  public void emptyIterator() {
    Assert.assertFalse(dm().iterator().hasNext());
  }

  @Test
  public void emptyReopen() throws Exception{
    dm().close();
    map = reopen();
    Assert.assertNull(dm().get(swords[0]));
  }


  /*****test 1 item map**************************/
  @Test
  public void oneIterator() {
    onePut();
    Assert.assertTrue(dm().iterator().hasNext());
  }

  @Test
  public void oneReopen() throws Exception{
    onePut();
    dm().close();
    map = reopen();
    Assert.assertEquals((Integer) swords[0].length(), dm().get(swords[0]));
  }

  /*****test 10 item map**************************/
  @Test
  public void tenIterator() {
    tenPut();
    int tot = 0;
    Iterator<Map.Entry<String,Integer>>  iter = dm().iterator();
    while(iter.hasNext()){
      tot++;
      iter.next();
    }
    Assert.assertEquals(10, tot);
  }

  @Test
  public void tenReopen() throws Exception {
    tenPut();
    dm().close();
    map = reopen();
    for (String w : words) {
      Assert.assertEquals((Integer) w.length(), dm().get(w));
    }
  }

  /*****test 10 item map**************************/
  @Test
  public void manyReopen() throws Exception{
    manyPut();
    dm().close();
    map = reopen();
    for(String w : manyWords){
      Assert.assertEquals((Integer)w.length(), dm().get(w));
    }
  }

  @Test
  public void manyIterator() {
    manyPut();
    Iterator <Entry<String, Integer>> iter = dm().iterator();
    int items = 0;
    while(iter.hasNext()){
      String w = iter.next().getKey();
      Assert.assertTrue(Arrays.binarySearch(mwSort, w) >= 0);
      items++;
    }
    Assert.assertEquals(mwSort.length, items);
  }

}
