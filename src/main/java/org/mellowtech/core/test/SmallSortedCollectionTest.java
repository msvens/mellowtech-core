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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
    TreeSet<Integer> s = Sets.newTreeSet();
    List<Integer> l = Lists.newLinkedList();
    Integer[] nums = new Integer[size];

    for(int i = 0; i < size; i++){
      s.add(i);
      l.add(i);
      nums[i] = i;
    }
    shuffle(nums);

    Random r = ThreadLocalRandom.current();
    Stopwatch sw = Stopwatch.createStarted();
    for(int i = 0; i < 1000*1000*100; i++){
      if(s.contains(i % size) != true)
        System.out.println("problem");
    }
    sw.stop();
    System.out.println("set contains:"+sw.elapsed(TimeUnit.MILLISECONDS));
    sw.reset();
    sw.start();
    for(int i = 0; i < 1000*1000*100; i++){
      if(l.contains(i % size) != true)
        System.out.println("problem");
    }
    sw.stop();
    System.out.println("list contains:"+sw.elapsed(TimeUnit.MILLISECONDS));
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
