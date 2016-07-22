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

package org.mellowtech.core.test;

import com.google.common.base.Stopwatch;
import org.mellowtech.core.util.MappedBitSet;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author msvens
 * @since 23/04/16
 */
public class MBitSetTest {

  int max;
  int trueVals;
  int[] randVals;
  MappedBitSet bs;
  Path p = Paths.get("/Users/msvens/core-tests/mappedBuffer");

  public static void main(String[] args){
    int max =  20000000;
    MBitSetTest bst = new MBitSetTest(max, max / 2);
    bst.fillBitSet();
    bst.lookupTrue();
    bst.iterate();
    bst.reopen();
  }

  public MBitSetTest(int max, int trueVals){
    this.max = max;
    this.trueVals = trueVals;

    randVals = new int[max];
    for(int i = 0; i < max; i++){
      randVals[i] = i;
    }
    shuffle(randVals);

    bs = new MappedBitSet(p, max);
    bs.clear();

  }

  void fillBitSet(){
    Stopwatch sw = Stopwatch.createStarted();
    for(int i = 0; i < trueVals; i++){
      bs.set(randVals[i]);
    }
    sw.stop();
    System.out.format("\nSetting %d values to true in a %d sized bitset took %d millis",
        trueVals, max, sw.elapsed(TimeUnit.MILLISECONDS));
  }

  void lookupTrue(){
    Stopwatch sw = Stopwatch.createStarted();
    for(int i = 0; i < trueVals; i++){
      if(!bs.contains(randVals[i]))
        System.out.println("not set");
    }
    sw.stop();
    System.out.format("\nLookup %d values to true in a %d sized bitset took %d millis",
        trueVals, max, sw.elapsed(TimeUnit.MILLISECONDS));
  }

  void iterate(){
    int cnt = 0;
    Stopwatch sw = Stopwatch.createStarted();
    /*for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
      cnt++;
    }*/
    for(int i : bs){
      cnt++;
    }
    System.out.println("\n"+cnt);
    sw.stop();
    System.out.format("\nIterate over %d values to true in a %d sized bitset took %d millis",
        trueVals, max, sw.elapsed(TimeUnit.MILLISECONDS));
  }

  void reopen(){
    System.out.println("reopen");
    bs = null;
    bs = new MappedBitSet(p, max);
    lookupTrue();
    lookupTrue();
  }

  public static<T> void shuffle(int[] array){
    shuffle(ThreadLocalRandom.current(), array);
  }
  public static<T> void shuffle(Random r, int[] array){
    for(int i = array.length-1; i > 0; i--){
      int index = r.nextInt(i+1);
      int o = array[index];
      array[index] = array[i];
      array[i] = o;
    }
  }
}
