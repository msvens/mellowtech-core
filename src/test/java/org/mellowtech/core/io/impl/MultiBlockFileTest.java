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

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Date: 2013-03-11
 * Time: 14:43
 *
 * @author Martin Svensson
 */
public class MultiBlockFileTest extends RecordFileTemplate {


  @Override
  public String fname() {return "multiBlockFileTest1.mbf";}

  @Override
  public long blocksOffset() {
    return 0;
  }

  @Override
  public RecordFile init(int blockSize, int reserve, int maxBlocks, String fname) throws Exception {
    return new MultiBlockFile(blockSize*2, blockSize, Paths.get(fname));
    //return new MemBlockFile(Paths.get(fname), blockSize, maxBlocks, reserve);
  }

  @Override
  public RecordFile reopen(String fname) throws Exception {
    return new MultiBlockFile(blockSize*2, blockSize, Paths.get(fname));
  }

  /****overwritten tests****/
  @Test
  @Override
  public void oneClear() throws IOException {
    rf.insert(testBlock);
    rf.clear();
    Assert.assertTrue(rf.fileSize() == blockSize*2);
    Assert.assertEquals(0, rf.size());
  }

  @Test
  @Override
  public void zeroClear() throws IOException{
    rf.clear();
    Assert.assertTrue(rf.fileSize() == blockSize*2);
    Assert.assertEquals(0, rf.size());
  }

  @Test
  @Override
  public void allClear() throws Exception{
    fillFile();
    rf.clear();
    Assert.assertTrue(rf.fileSize() == blockSize*2);
    Assert.assertEquals(0, rf.size());
  }

  @Test
  @Override
  public void lastClear() throws IOException{
    rf.insert(maxBlocks-1, testBlock);
    rf.clear();
    Assert.assertTrue(rf.fileSize() == blockSize*2);
    Assert.assertEquals(0, rf.size());
  }

  @Test
  @Override
  public void lastFree() throws Exception{
    rf.insert(maxBlocks-1, testBlock);
    Assert.assertEquals(Integer.MAX_VALUE - 1, rf.getFreeBlocks());
  }

  @Test
  @Override
  public void allFree() throws Exception{
    fillFile();
    Assert.assertEquals(Integer.MAX_VALUE-maxBlocks, rf.getFreeBlocks());
  }

  @Override
  @Test(expected = UnsupportedOperationException.class)
  public void reserveSize() throws IOException{
    Assert.assertEquals(reserve, rf.getReserve().length);
  }

  //dont expect any exceptions
  @Override
  @Test
  public void insertInFull() throws Exception {
    fillFile();
    rf.insert(testBlock);
  }

  @Override
  @Test
  public void insertOutOfRange() throws Exception {
    rf.insert(maxBlocks, testBlock);
  }


}
