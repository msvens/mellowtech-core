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

import org.junit.Assert;
import org.junit.Test;
import org.mellowtech.core.io.RecordFile;

import java.nio.file.Paths;

/**
 * Created by msvens on 28/10/15.
 */
public class VariableRecordFileTest extends RecordFileTemplate {

  @Override
  public String fname() {return "variableRecordFileTest.blf";}

  @Override
  public long blocksOffset() {
    return ((VariableRecordFile) rf).blocksOffset();
  }

  @Override
  public RecordFile init(int blockSize, int reserve, int maxBlocks, String fname) throws Exception {
    //return new BlockFile(Paths.get(fname), blockSize, maxBlocks, reserve);
    return new VariableRecordFile(Paths.get(fname),maxBlocks,reserve);
  }

  @Override
  public RecordFile reopen(String fname) throws Exception {
    return new VariableRecordFile(Paths.get(fname));
  }

  /**
   * A VariableRecordFile does not have blocks in that sense
   */
  @Test
  public void blockSize(){
    Assert.assertEquals(0, rf.getBlockSize());
  }

}
