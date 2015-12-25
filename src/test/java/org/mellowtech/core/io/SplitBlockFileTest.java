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

package org.mellowtech.core.io;

import junit.framework.Assert;

import org.junit.Test;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.io.BlockFile;
import org.mellowtech.core.io.Record;
import org.mellowtech.core.io.SplitBlockFile;
import org.mellowtech.core.util.Platform;

import java.io.File;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Date: 2013-03-11
 * Time: 14:43
 *
 * @author Martin Svensson
 */
public class SplitBlockFileTest extends SplitRecordFileTemplate{

  @Override
  public String fname() {return "splitBlockFileTest.blf";}

  @Override
  public long blocksOffset() {
    return ((SplitBlockFile)rf).blocksOffset();
  }

  @Override
  public SplitRecordFile init(int blockSize, int reserve, int maxBlocks, String fname) throws Exception {
    return new SplitBlockFile(Paths.get(fname), blockSize, maxBlocks, reserve, maxBlocks, blockSize);
  }

  @Override
  public RecordFile reopen(String fname) throws Exception {
    return new SplitBlockFile(Paths.get(fname));
  }

}
