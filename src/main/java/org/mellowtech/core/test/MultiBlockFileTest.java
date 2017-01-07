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

import org.mellowtech.core.io.impl.MultiBlockFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author msvens
 * @since 03/04/16
 */
public class MultiBlockFileTest {

  public static void main(String[] args)throws IOException{
      int numRecords = 1024*512;
      int blockSize = 1024*4;
      Path p = Paths.get("/Users/msvens/core-tests/multiBlockFile.mbf");


    MultiBlockFile mbf = new MultiBlockFile(blockSize,p);

    Integer records[] = new Integer[numRecords];
    for(int i = 0; i < records.length; i++){
      records[i] = i;
    }
    shuffle(records);

    //Fill all blocks
    Instant start = Instant.now();
    //byte b[] = "1234567890".getBytes();
    //byte b[] = new byte[blockSize];
    byte b[] = new byte[1024*4];
    //byte b[] = null;
    for(int i = 0; i < numRecords; i++){
      mbf.insert(b);
    }
    mbf.save();
    Duration d = Duration.between(start, Instant.now());
    System.out.println("done inserting: "+d.get(ChronoUnit.MILLIS));
    //Delete all blocks
    start = Instant.now();
    for(Integer i : records){
      if(!mbf.delete(i))
        System.out.println("something wrong");
    }
    d = Duration.between(start, Instant.now());
    System.out.println("num blocks: "+mbf.size());
    System.out.println("done deleting: "+d.get(ChronoUnit.MILLIS));
    mbf.remove();

  }

  public static<T> void shuffle(T[] array){
    shuffle(ThreadLocalRandom.current(), array);
  }
  public static<T> void shuffle(Random r, T[] array){
    for(int i = array.length-1; i > 0; i--){
      int index = r.nextInt(i+1);
      T o = array[index];
      array[index] = array[i];
      array[i] = o;
    }
  }
}
