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
import org.junit.Test;
import org.mellowtech.core.util.MapEntry;

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

  @After
  public void after() throws Exception{
    sdm().close();
    sdm().delete();
  }

  /*****test empty map**************************/
  @Test
  public void zeroIterator() {
    org.junit.Assert.assertFalse(sdm().iterator().hasNext());
  }

  @Test
  public void zeroIteratorFrom() throws IOException {
    Assert.assertFalse(sdm().iterator(swords[0]).hasNext());
  }

  @Test
  public void zeroReopen() throws Exception{
    sdm().close();
    map = reopen();
    org.junit.Assert.assertNull(sdm().get(swords[0]));
  }


  /*****test 1 item map**************************/
  @Test
  public void oneIterator() {
    onePut();
    org.junit.Assert.assertTrue(sdm().iterator().hasNext());
  }

  @Test
  public void oneIteratorFrom() throws IOException {
    onePut();
    Assert.assertTrue(sdm().iterator(swords[0]).hasNext());
  }

  @Test
  public void oneReopen() throws Exception{
    onePut();
    sdm().close();
    map = reopen();
    org.junit.Assert.assertEquals((Integer) swords[0].length(), sdm().get(swords[0]));
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
    org.junit.Assert.assertEquals(10, tot);
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
    Assert.assertEquals(7, tot);
  }

  @Test
  public void tenReopen() throws Exception {
    tenPut();
    sdm().close();
    map = reopen();
    for (String w : words) {
      org.junit.Assert.assertEquals((Integer) w.length(), sdm().get(w));
    }
  }

  /*****test 10 item map**************************/
  @Test
  public void manyReopen() throws Exception{
    manyPut();
    sdm().close();
    map = reopen();
    for(String w : manyWords){
      org.junit.Assert.assertEquals((Integer)w.length(), sdm().get(w));
    }
  }

  @Test
  public void manyIterator() {
    manyPut();
    Iterator <Entry<String, Integer>> iter = sdm().iterator();
    int items = 0;
    while(iter.hasNext()){
      String w = iter.next().getKey();
      org.junit.Assert.assertTrue(Arrays.binarySearch(mwSort, w) >= 0);
      items++;
    }
    org.junit.Assert.assertEquals(mwSort.length, items);
  }

  @Test
  public void manyIteratorFrom() throws IOException {
    manyPut();
    int item = 10;
    String key = mwSort[item];
    Iterator <Entry<String, Integer>> iter = sdm().iterator(key);

    while(iter.hasNext()){
      Assert.assertEquals(mwSort[item], iter.next().getKey());
      item++;
    }
    Assert.assertEquals(mwSort.length, item);
  }





}
