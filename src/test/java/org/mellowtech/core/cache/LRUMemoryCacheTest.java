
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

package org.mellowtech.core.cache;

import org.junit.Before;
import org.junit.Test;
import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.cache.CacheLRUMemory;
import org.mellowtech.core.cache.CacheValue;
import org.mellowtech.core.cache.Loader;
import org.mellowtech.core.cache.NoSuchValueException;
import org.mellowtech.core.cache.Remover;

import java.util.HashMap;
import java.util.Random;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Martin Svensson
 *
 */
public class LRUMemoryCacheTest {

  public HashMap <CBInt, CBString> inMemoryMap;
  public int numKeys = 1000;
  public int numWords = 1000;
  public long cacheSize = 1024;
  public Loader <CBInt, CBString> loader;
  public Remover <CBInt, CBString> remover;
  public CacheLRUMemory <Integer, CBInt, String,CBString> cache;
  
  @Before
  public void before(){

    Random r = new Random();
    inMemoryMap = new HashMap<>();


    for(int i = 0; i < numKeys; i++){
      inMemoryMap.put(new CBInt(i),
              new CBString("" + (int) (r.nextGaussian() * numWords)));
    }
    loader = new Loader<CBInt, CBString>() {
      @Override
      public CBString get(CBInt key) throws NoSuchValueException {
        CBString i = inMemoryMap.get(key);
        if(i == null) throw new NoSuchValueException();
        return new CBString(i.get());
      }
    };
    remover = new Remover<CBInt, CBString>() {
      @Override
      public void remove(CBInt key, CacheValue<CBString> value) {
        if(value.isDirty()){
          inMemoryMap.put(key, new CBString(value.getValue().get()));
        }
      }
    };

    cache = new CacheLRUMemory<>(remover, loader, cacheSize, true);
   
  }
  
  @Test
  public void doTest() throws Exception{

    //First load 50 keys
    for(int i = 0; i < 50; i++){
      CBString val = cache.get(new CBInt(i));
      assertThat(val, equalTo(inMemoryMap.get(new CBInt(i))));
    }

    //Now make sure that all values are present in the cache
    for(int i = 0; i < 50; i++){
      assertThat(cache.getFromCache(new CBInt(i)), notNullValue());
    }


    //Now set some dirty bits:
    CBString val1 = cache.get(new CBInt(10));
    cache.dirty(new CBInt(10), new CBString(val1.get()+"new"));
    CBString val2 = cache.get(new CBInt(500));
    cache.dirty(new CBInt(500), new CBString(val2.get() + "new"));


    //expect the map to differ from the cache
    assertThat(cache.get(new CBInt(10)), not(equalTo(inMemoryMap.get(new CBInt(10)))));
    assertThat(cache.get(new CBInt(500)), not(equalTo(inMemoryMap.get(new CBInt(500)))));

    //Add some more to cache...should flush...

    for(int i = 50; i < 400; i++){
      CBString val = cache.get(new CBInt(i));
      assertThat(val, equalTo(inMemoryMap.get(new CBInt(i))));
    }

    cache.emptyCache();
    assertThat(inMemoryMap.get(new CBInt(10)), equalTo(new CBString(val1.get() + "new")));
    assertThat(inMemoryMap.get(new CBInt(500)), equalTo(new CBString(val2.get()+ "new")));





  }

  @Test(expected = NoSuchValueException.class)
  public void expectNoSuchValue() throws NoSuchValueException{
    cache.get(new CBInt(numKeys + 100)); //should not exist
  }

  
  



}
