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

import org.junit.jupiter.api.*;
import org.mellowtech.core.TestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author msvens
 * @since 2018-02-25
 */
abstract class CFileWriterTemplate extends CFileTemplate {

  CFileWriter cFileWriter(){return (CFileWriter) cFile;}


  abstract String fName();
  abstract CFile init(Path name) throws IOException;


  @Nested
  @DisplayName("CFileWriter")
  class CFileWriterTest {
    @Test
    @DisplayName("Add One")
    void addOne() throws IOException{
      long pos = cFileWriter().add(input);
      assertEquals(0, pos);
    }
    @Test
    @DisplayName("Add Two")
    void addTwo() throws IOException{
      long pos = cFileWriter().add(input);
      input.clear();
      assertEquals(0, pos);
      pos = cFileWriter().add(input);
      assertTrue(pos > 0);
    }
    @Test
    @DisplayName("Add 10")
    void addTen() throws IOException{
      //input.clear();
      long pos = cFileWriter().add(input);
      input.clear();
      assertEquals(0, pos);
      long length = cFileWriter().add(input);
      pos = length;
      for(int i = 0; i < 8; i++){
        input.clear();
        long p1 = cFileWriter().add(input);
        assertEquals(length,p1 - pos);
        pos = p1;
      }
    }




  }




}
