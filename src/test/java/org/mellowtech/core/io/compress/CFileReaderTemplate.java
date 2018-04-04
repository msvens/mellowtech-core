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

package org.mellowtech.core.io.compress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author msvens
 * @since 2018-02-25
 */
abstract class CFileReaderTemplate extends CFileTemplate {


  @FunctionalInterface
  interface DoRead{
    void read(CFileReader r) throws IOException;
  }

  CFileWriter cFileWriter(){return (CFileWriter) cFile;}
  byte[] comp;


  abstract String fName();

  CFile init(Path name) throws IOException {
    return initWriter(name);
  }

  abstract CFileWriter initWriter(Path name) throws IOException;
  abstract CFileReader initReader(Path name) throws IOException;
  abstract byte[] compressed(byte[] input);

  @BeforeEach
  void setup() throws Exception {
    super.setup();
    comp = compressed(input.array());
  }

  CFileReader getReader(){
    try {
      return initReader(absFile());
    } catch(IOException e){
      throw new Error(e);
    }
  }

  void addOne() throws IOException{
    input.clear();
    cFileWriter().add(input);
  }

  void addDbl() throws IOException{
    dblInput.clear();
    cFileWriter().add(dblInput);
  }

  void add(int times) throws IOException {
    while(times-- > 0)
      addOne();
  }

  void doReader(DoRead c) throws IOException{
    CFileReader reader = getReader();
    c.read(reader);
    reader.close();
  }


  @Nested
  @DisplayName("CFileReader")
  class CFileWriterTest {

    @Test
    @DisplayName("Get One")
    void getOne() throws IOException {
      addOne();
      doReader((reader) -> {
        BlockPointer bp = reader.get(0);
        assertEquals(comp.length, bp.getSize());
        assertEquals(input.capacity(), bp.getOrigSize());
      });
    }

    @Test
    @DisplayName("Get Next")
    void next() throws IOException {
      addOne();
      addOne();
      doReader(reader -> {
        BlockPointer bp = reader.get(0);
        BlockPointer bp1 = reader.get(reader.next(bp));
        assertEquals(bp.getSize(), bp1.getSize());
      });
    }

    @Test
    @DisplayName("Read One")
    void readOne() throws IOException {
      addOne();
      doReader(reader -> {
        ByteBuffer bb = reader.read(0);
        assertArrayEquals(input.array(),bb.array());
      });
    }

    @Test
    @DisplayName("Read Five")
    void readFive() throws IOException {
      add(5);;
      doReader(reader -> {
        long offset = 0;
        for(int i = 0; i < 5; i++){
          BlockPointer ptr = reader.get(offset);
          offset = reader.next(ptr);
          ByteBuffer bb = reader.read(ptr);
          assertArrayEquals(input.array(), bb.array());
        }
      });
    }

    @Test
    @DisplayName("Different Sized inputs")
    void readDifferent() throws IOException {
      addOne();
      addDbl();
      addOne();
      addDbl();
      doReader(reader -> {
        Iterator<BlockPointer> iter = reader.iterator();
        for(int i = 0; i < 4; i++){
          BlockPointer ptr = iter.next();
          ByteBuffer bb = reader.read(ptr);
          if(i % 2 == 0) { //normal input
            assertEquals(input.capacity(), ptr.getOrigSize());
            assertArrayEquals(input.array(), bb.array());
          } else { //double input
            assertEquals(dblInput.capacity(), ptr.getOrigSize());
            assertArrayEquals(dblInput.array(), bb.array());
          }
        }
      });
    }

    @Test
    @DisplayName("Empty Iterator")
    void emptyIterator() throws IOException {
      doReader(reader -> {
        Iterator<BlockPointer> iter = reader.iterator();
        assertFalse(iter.hasNext());
      });
    }

    @Test
    @DisplayName("10 Iterator")
    void tenIterator() throws IOException {
      add(10);
      doReader(reader -> {
        Iterator<BlockPointer> iter = reader.iterator();
        for(int i = 0; i < 10; i++){
          assertFalse(iter.next().isDeleted());
        }
        assertFalse(iter.hasNext());
      });
    }



  }


}
