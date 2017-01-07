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


import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author msvens
 * @since 23/04/16
 */
public class IntArrayTest {

  int max;
  int trueVals;
  int[] randVals;
  boolean[] bs;

  public static void main(String[] args){
    int max =  200000000;
    IntArrayTest bst = new IntArrayTest(max, max / 2);
    bst.fillBitSet();
    bst.countSet();
    bst.lookup();
    bst.lookupTrue();
  }

  public IntArrayTest(int max, int trueVals){
    this.max = max;
    this.trueVals = trueVals;

    randVals = new int[max];
    for(int i = 0; i < max; i++){
      randVals[i] = i;
    }
    shuffle(randVals);
    bs = new boolean[max];
    Arrays.fill(bs, false);
  }

  void fillBitSet(){
    Instant i1 = Instant.now();
    for(int i = 0; i < trueVals; i++){
      bs[randVals[i]] = true;
    }
    Duration d = Duration.between(i1, Instant.now());
    System.out.format("\nSetting %d values to true in a %d sized bitset took %d millis",
        trueVals, max, d.get(ChronoUnit.MILLIS));
  }

  void lookupTrue(){
    Instant i1 = Instant.now();
    for(int i = 0; i < trueVals; i++){
      if(!bs[randVals[i]])
        System.out.println("not set");
    }
    Duration d = Duration.between(i1, Instant.now());
    System.out.format("\nLookup %d values to true in a %d sized bitset took %d millis",
        trueVals, max, d.get(ChronoUnit.MILLIS));
  }

  void lookup(){
    Random r = ThreadLocalRandom.current();
    Instant i1 = Instant.now();
    int set = 0;
    for(int i = 0; i < trueVals; i++){
      if(bs[r.nextInt(max)])
        set++;
    }
    Duration d = Duration.between(i1, Instant.now());
    System.out.format("\nLookup %d values in a %d sized bitset took %d millis",
        trueVals, max, d.get(ChronoUnit.MILLIS));

  }

  void countSet(){
    int cnt = 0;
    Instant i1 = Instant.now();
    for(int i = 0; i < bs.length; i++){
      if(bs[i]){
        cnt++;
      }
    }
    Duration d = Duration.between(i1, Instant.now());
    System.out.println("\n"+cnt);
    System.out.format("\nIterate over %d values to true in a %d sized bitset took %d millis",
        trueVals, max, d.get(ChronoUnit.MILLIS));
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
