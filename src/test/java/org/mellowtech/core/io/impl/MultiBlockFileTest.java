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

package org.mellowtech.core.io.impl;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.mellowtech.core.io.RecordFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Date: 2013-03-11
 * Time: 14:43
 *
 * @author Martin Svensson
 */
@DisplayName("A Multiblockfile ")
public class MultiBlockFileTest extends RecordFileTemplate {


  @Override
  public String fname() {return "multiBlockFileTest1.mbf";}

  @Override
  public String fnameMoved() {return "multiBlockFileTest1Moved.mbf";}

  @Override
  public long blocksOffset() {
    return 0;
  }

  @Override
  public RecordFile init(int blockSize, int reserve, int maxBlocks, Path fname) throws Exception {
    return new MultiBlockFile(blockSize*2, blockSize, reserve, fname);
    //return new MemBlockFile(Paths.get(fname), blockSize, maxBlocks, reserve);
  }

  @Override
  public RecordFile reopen(Path fname) throws Exception {
    return new MultiBlockFile(blockSize*2, blockSize, reserve, fname);
  }


  @Test
  @Override
  public void reserveSize() throws IOException{
    assertEquals(reserve, rf.getReserve().length);
  }

  @Nested
  @DisplayName("Full file ")
  class All extends RecordFileTemplate.All {

    @Override
    @Test
    public void allFree() throws Exception{
      fillFile();
      assertEquals(Integer.MAX_VALUE-maxBlocks, rf.getFreeBlocks());
    }
  }

  @Nested
  @DisplayName("Record file wit only last record used ")
  class Last extends RecordFileTemplate.Last {
    @Override
    @Test
    public void lastFree() throws Exception{
      rf.insert(maxBlocks-1, testBlock);
      assertEquals(Integer.MAX_VALUE - 1, rf.getFreeBlocks());
    }
  }

 @Nested
 @DisplayName("Hanlding wrong input ")
 class ErrorPath extends RecordFileTemplate.ErrorPath {

    @Override
    @Test
    void insertInFull() throws Exception {
      fillFile();
      rf.insert(testBlock);
    }

    @Override
    @Test
    void insertOutOfRange() throws Exception {
      rf.insert(maxBlocks, testBlock);
    }
  }

}
