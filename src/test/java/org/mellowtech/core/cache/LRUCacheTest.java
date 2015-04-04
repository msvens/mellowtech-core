/*
 * Copyright (c) 2012 mellowtech.org.
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 *
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 *
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 */
package org.mellowtech.core.cache;


import org.junit.Before;
import org.junit.Test;
import org.mellowtech.core.cache.CacheLRU;
import org.mellowtech.core.cache.CacheValue;
import org.mellowtech.core.cache.Loader;
import org.mellowtech.core.cache.NoSuchValueException;
import org.mellowtech.core.cache.Remover;

import java.util.*;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * @author Martin Svensson
 *
 */
public class LRUCacheTest {

  public HashMap <Integer, Integer> inMemoryMap;
  public int numKeys = 1000;
  public int cacheSize = 100;
  public Loader <Integer, Integer> loader;
  public Remover <Integer, Integer> remover;
  public CacheLRU <Integer, Integer> cache;
  
  @Before
  public void before(){
    Random r = new Random();
    inMemoryMap = new HashMap<>();


    for(int i = 0; i < numKeys; i++){
      inMemoryMap.put(i, r.nextInt());
    }
    loader = new Loader<Integer, Integer>() {
      @Override
      public Integer get(Integer key) throws NoSuchValueException {
        Integer i = inMemoryMap.get(key);
        if(i == null) throw new NoSuchValueException();
        return inMemoryMap.get(key);
      }
    };
    remover = new Remover<Integer, Integer>() {
      @Override
      public void remove(Integer key, CacheValue<Integer> value) {
        if(value.isDirty()) inMemoryMap.put(key, (int) value.getValue());
      }
    };

    cache = new CacheLRU<>(remover, loader, cacheSize);
   
  }
  
  @Test
  public void doTest() throws Exception{

    //First load 50 keys
    for(int i = 0; i < 50; i++){
      Integer val = cache.get(i);
      assertThat(val, equalTo(inMemoryMap.get(i)));
    }

    //Now make sure that all values are present in the cache
    for(int i = 0; i < 50; i++){
      assertThat(cache.getFromCache(i), notNullValue());
    }

    //Now set some dirty bits:
    Integer val1 = cache.get(10);
    cache.dirty(10, val1+100);
    Integer val2 = cache.get(500);
    cache.dirty(500, val2 + 100);

    //expect the map to differ from the cache
    assertThat(cache.get(10), not(equalTo(inMemoryMap.get(10))));
    assertThat(cache.get(500), not(equalTo(inMemoryMap.get(500))));

    cache.emptyCache();
    assertThat(inMemoryMap.get(10), equalTo(val1+100));
    assertThat(inMemoryMap.get(500), equalTo(val2+100));





  }

  @Test(expected = NoSuchValueException.class)
  public void expectNoSuchValue() throws NoSuchValueException{
     cache.get(numKeys+100); //should not exist
  }

  @Test(expected = ArithmeticException.class)
  public void divisionWithException() {
    int i = 1/0;
  }
  
  



}
