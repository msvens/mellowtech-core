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

import org.junit.jupiter.api.DisplayName;
import org.mellowtech.core.io.RecordFile;
import org.mellowtech.core.io.SplitRecordFile;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Date: 2013-03-11
 * Time: 14:43
 *
 * @author Martin Svensson
 */
@DisplayName("A MemSplitBlockFile")
public class MemSplitBlockFileTest extends SplitRecordFileTemplate{

  @Override
  public String fname() {return "memSplitBlockFileTest.blf";}

  @Override
  public String fnameMoved() {return "memSplitBlockFileTestMoved.blf";}

  @Override
  public long blocksOffset() {
    return ((MemSplitBlockFile) rf).blocksOffset();
  }

  @Override
  public SplitRecordFile init(int blockSize, int reserve, int maxBlocks, Path fname) throws Exception {
    return new MemSplitBlockFile(fname, blockSize, maxBlocks, reserve, maxBlocks, blockSize);
  }

  @Override
  public RecordFile reopen(Path fname) throws Exception {
    return new MemSplitBlockFile(fname);
  }

}
