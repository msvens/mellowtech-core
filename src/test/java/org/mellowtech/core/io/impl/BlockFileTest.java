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

import java.nio.file.Path;

/**
 * Date: 2013-03-11
 * Time: 14:43
 *
 * @author Martin Svensson
 */
@DisplayName("A BlockFile")
public class BlockFileTest extends RecordFileTemplate {



  @Override
  public String fname() {return "blockFileTest.blf";}

  @Override
  public String fnameMoved() {return "blockFileTestMoved.blf";}

  @Override
  public long blocksOffset() {
    return ((BlockFile) rf).blocksOffset();
  }

  @Override
  public RecordFile reopen(Path fname) throws Exception {
    return new BlockFile(fname);
  }
  @Override
  public RecordFile init(int blockSize, int reserve, int maxBlocks, Path fname) throws Exception {
    return new BlockFile(fname, blockSize, maxBlocks, reserve);
  }


}
