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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author msvens
 * @since 08/06/16
 */
public class SmallSortedCollectionTest {

  public static void main(String[] args){
    int size = 10;
    TreeSet<Integer> s = new TreeSet<>();
    List<Integer> l = new LinkedList<>();
    Integer[] nums = new Integer[size];

    for(int i = 0; i < size; i++){
      s.add(i);
      l.add(i);
      nums[i] = i;
    }
    shuffle(nums);

    Random r = ThreadLocalRandom.current();
    Instant start = Instant.now();
    for(int i = 0; i < 1000*1000*100; i++){
      if(s.contains(i % size) != true)
        System.out.println("problem");
    }
    Duration d = Duration.between(start, Instant.now());
    System.out.println("set contains:"+d.get(ChronoUnit.MILLIS));
    start = Instant.now();
    for(int i = 0; i < 1000*1000*100; i++){
      if(l.contains(i % size) != true)
        System.out.println("problem");
    }
    d = Duration.between(start, Instant.now());
    System.out.println("list contains:"+d.get(ChronoUnit.MILLIS));
  }

  public static<T> void shuffle(Integer[] array){
    shuffle(ThreadLocalRandom.current(), array);
  }
  public static<T> void shuffle(Random r, Integer[] array){
    for(int i = array.length-1; i > 0; i--){
      Integer index = r.nextInt(i+1);
      Integer o = array[index];
      array[index] = array[i];
      array[i] = o;
    }
  }

}
