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
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author msvens
 * @since 2018-02-25
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class CFileTemplate {

  static int DEFAULT_BLOCK_SIZE = 1024*64;

  final URL url = this.getClass().getResource("/com/mellowtech/core/sort/longText.txt");
  final String dir = "compresstests";
  CFile cFile;
  ByteBuffer input;
  ByteBuffer dblInput;

  abstract String fName();
  abstract CFile init(Path name) throws IOException;

  Path absDir() {
    return TestUtils.getAbsolutePath(dir);
  }

  Path absFile() {
    return TestUtils.getAbsolutePath(dir + "/" + fName());
  }

  void createFile() throws IOException{
    File f = absFile().toFile();
    if(!f.exists()) f.createNewFile();
  }



  void loadInputData(){
    try {
      FileInputStream is = new FileInputStream(url.getFile());
      byte[] bytes = is.readAllBytes();
      input = ByteBuffer.wrap(bytes);
      dblInput = ByteBuffer.allocate(bytes.length*2);
      dblInput.put(bytes);
      dblInput.put(bytes);
    } catch(IOException e){
      throw new Error("could not load input data");
    }
  }


  @BeforeAll
  void createDir(){
    if(Files.exists(absDir()))
      TestUtils.deleteTempDir(dir);
    TestUtils.createTempDir(dir);
  }

  @AfterAll
  void deleteDir(){
    TestUtils.deleteTempDir(dir);
  }

  @BeforeEach
  void setup() throws Exception {
    createFile();
    cFile = init(absFile());
    loadInputData();
  }

  @AfterEach
  void after() throws Exception {
    cFile.close();
    cFile.delete();
  }

  @Nested
  @DisplayName("Empty CFile")
  class EmptyCFile {
    @Test
    @DisplayName("Opened")
    void opened() {
      assertTrue(cFile.isOpen());
    }

    @Test
    @DisplayName("Closed")
    void closed() throws IOException {
      cFile.close();
      assertFalse(cFile.isOpen());
    }

    @Test
    @DisplayName("Multiple Closed")
    void closed2() throws IOException {
      cFile.close();
      cFile.close();
      assertFalse(cFile.isOpen());
    }

    @Test
    @DisplayName("Throw exception when reading a block")
    void getBlock(){
      assertThrows(Exception.class, () -> {
        cFile.get(0);
      });
    }
  }




}
