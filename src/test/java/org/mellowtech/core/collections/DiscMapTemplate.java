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

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.Map.Entry;

/**
 * Created by msvens on 09/11/15.
 */
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class DiscMapTemplate extends MapTemplate {

  public static int IDX_BLK_SIZE = 1024;
  public static int VAL_BLK_SIZE = 1024;
  public static int IDX_BLKS = 5;
  public static int VAL_BLKS = 15;

  public DiscMap <String, Integer> dm() {
    return (DiscMap <String, Integer>) map;
  };

  public abstract DiscMap <String, Integer> reopen() throws Exception;

  @AfterEach
  public void after() throws Exception{
    dm().close();
    dm().delete();
  }

  /*****test empty map**************************/
  @Test
  public void emptyIterator() {
    assertFalse(dm().iterator().hasNext());
  }

  @Test
  public void emptyReopen() throws Exception{
    dm().close();
    map = reopen();
    assertNull(dm().get(swords[0]));
  }


  /*****test 1 item map**************************/
  @Test
  public void oneIterator() {
    onePut();
    assertTrue(dm().iterator().hasNext());
  }

  @Test
  public void oneReopen() throws Exception{
    onePut();
    dm().close();
    map = reopen();
    assertEquals((Integer) swords[0].length(), dm().get(swords[0]));
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
    assertEquals(10, tot);
  }

  @Test
  public void tenReopen() throws Exception {
    tenPut();
    dm().close();
    map = reopen();
    for (String w : words) {
      assertEquals((Integer) w.length(), dm().get(w));
    }
  }

  /*****test 10 item map**************************/
  @Test
  public void manyReopen() throws Exception{
    manyPut();
    dm().close();
    map = reopen();
    for(String w : manyWords){
      assertEquals((Integer)w.length(), dm().get(w));
    }
  }

  @Test
  public void manyIterator() {
    manyPut();
    Iterator <Entry<String, Integer>> iter = dm().iterator();
    int items = 0;
    while(iter.hasNext()){
      String w = iter.next().getKey();
      assertTrue(Arrays.binarySearch(mwSort, w) >= 0);
      items++;
    }
    assertEquals(mwSort.length, items);
  }

}
