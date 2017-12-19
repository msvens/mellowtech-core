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

import org.junit.*;
import org.mellowtech.core.TestUtils;
import org.mellowtech.core.io.Record;
import org.mellowtech.core.io.SplitRecordFile;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * Created by msvens on 24/10/15.
 */
public abstract class SplitRecordFileTemplate extends RecordFileTemplate{


  //public SplitRecordFile rf;
  SplitRecordFile rf1;

  @Override
  public abstract SplitRecordFile init(int blockSize, int reserve, int maxBlocks, Path fname) throws Exception;


  @Before
  public void setup() throws Exception{
    rf = init(blockSize,reserve,maxBlocks, TestUtils.getAbsolutePath(dir+"/"+fname()));
    rf1 = (SplitRecordFile) rf;
  }

  private void reop() throws Exception{
    rf.close();
    rf = reopen(TestUtils.getAbsolutePath(dir+"/"+fname()));
    rf1 = (SplitRecordFile) rf;
  }

  /**COMMON TEST CASES**/
  @Test
  public void blockSizeRegion(){
    Assert.assertEquals(blockSize, rf1.getBlockSizeRegion());
  }


  /****TESTS WITH ZERO ELEMENTS*********/
  @Test
  public void zeroSizeRegion(){
    Assert.assertEquals(0, rf1.sizeRegion());
  }

  @Test
  public void zeroFreeRegion(){
    Assert.assertEquals(maxBlocks, rf1.getFreeBlocksRegion());
  }

  @Test
  public void zeroIteratorRegion(){
    Assert.assertFalse(rf1.iteratorRegion().hasNext());
  }

  @Test
  public void zeroContainsRegion() throws Exception{
    Assert.assertFalse(rf1.containsRegion(0));
  }

  @Test
  public void zeroFirstRecordRegion() throws Exception {
    Assert.assertEquals(-1, rf1.getFirstRecordRegion());
  }

  @Test
  public void zeroGetRegion() throws Exception {
    Assert.assertNull(rf1.getRegion(0));
  }

  @Test
  public void zeroGetRegionMapped() throws Exception {
    Assert.assertNull(rf1.getRegionMapped(0));
  }

  @Test
  public void zeroReopenRegion() throws Exception {
    reop();
    Assert.assertNull(rf1.getRegion(0));
  }

  @Test
  public void zeroGetRegionBoolean() throws Exception {
    byte b[] = new byte[blockSize];
    Assert.assertFalse(rf1.getRegion(0, b));
  }

  @Test
  public void zeroUpdateRegion() throws Exception {
    byte b[] = new byte[blockSize];
    Assert.assertFalse(rf1.updateRegion(0,b));
  }

  @Test
  public void zeroDeleteRegion() throws Exception {
    Assert.assertFalse(rf1.deleteRegion(0));
    Assert.assertEquals(0, rf1.sizeRegion());
  }

  /****TESTS WITH 1 ELEMENT*************/
  @Test
  public void oneSizeRegion() throws Exception{
    rf1.insertRegion(testBlock);
    Assert.assertEquals(1, rf1.sizeRegion());
  }

  @Test
  public void oneFreeRegion() throws Exception{
    rf1.insertRegion(testBlock);
    Assert.assertEquals(maxBlocks - 1, rf1.getFreeBlocksRegion());
  }

  @Test
  public void oneIteratorRegion() throws Exception{
    rf1.insertRegion(testBlock);
    Assert.assertTrue(rf1.iteratorRegion().hasNext());
  }

  @Test
  public void oneContainsRegion() throws Exception{
    rf1.insertRegion(testBlock);
    Assert.assertTrue(rf1.containsRegion(0));
  }

  @Test
  public void oneFirstRecordRegion() throws Exception {
    rf1.insertRegion(testBlock);
    Assert.assertEquals(0, rf1.getFirstRecordRegion());
  }

  @Test
  public void oneGetRegion() throws Exception {
    rf1.insertRegion(testBlock);
    Assert.assertEquals(new String(testBlock), new String(rf1.getRegion(0)));
  }

  @Test
  public void oneGetRegionMapped() throws Exception {
    rf1.insertRegion(testBlock);
    ByteBuffer bb;
    try{
      bb = rf1.getRegionMapped(0);
    } catch(UnsupportedOperationException e){
      return;
    }
    Assert.assertEquals(blockSize, bb.limit()-bb.position());
    byte[] b = new byte[blockSize];
    bb.get(b);
    Assert.assertEquals(new String(testBlock), new String(b));
  }

  @Test
  public void oneReopenRegion() throws Exception {
    rf1.insertRegion(testBlock);
    reop();
    Assert.assertEquals(new String(testBlock), new String(rf1.getRegion(0)));
  }

  @Test
  public void oneGetBooleanRegion() throws Exception {
    rf1.insertRegion(testBlock);
    byte b[] = new byte[blockSize];
    Assert.assertTrue(rf1.getRegion(0, b));
    Assert.assertEquals(new String(testBlock), new String(b));
  }

  @Test
  public void oneUpdateRegion() throws Exception {
    rf1.insertRegion(testBlock);
    byte b[] = new byte[blockSize];
    b[0] = 1;
    Assert.assertTrue(rf1.updateRegion(0,b));
  }

  @Test
  public void oneDeleteRegion() throws Exception {
    rf1.insertRegion(testBlock);
    Assert.assertTrue(rf1.deleteRegion(0));
    Assert.assertEquals(0, rf1.sizeRegion());
  }

  /**Test with last record added***/
  @Test
  public void lastSizeResion() throws Exception{
    rf1.insertRegion(maxBlocks-1, testBlock);
    Assert.assertEquals(1, rf1.sizeRegion());
  }

  @Test
  public void lastFreeRegion() throws Exception{
    rf1.insertRegion(maxBlocks-1, testBlock);
    Assert.assertEquals(maxBlocks - 1, rf1.getFreeBlocksRegion());
  }

  @Test
  public void lastIteratorRegion() throws Exception{
    rf1.insertRegion(maxBlocks-1, testBlock);
    Assert.assertTrue(rf1.iteratorRegion().hasNext());
  }

  @Test
  public void lastContainsRegion() throws Exception{
    rf1.insertRegion(maxBlocks-1, testBlock);
    Assert.assertTrue(rf1.containsRegion(maxBlocks-1));
  }

  @Test
  public void lastFirstRecordRegion() throws Exception {
    rf1.insertRegion(maxBlocks-1, testBlock);
    Assert.assertEquals(maxBlocks-1, rf1.getFirstRecordRegion());
  }

  @Test
  public void lastGetRegion() throws Exception {
    rf1.insertRegion(maxBlocks-1, testBlock);
    Assert.assertEquals(new String(testBlock), new String(rf1.getRegion(maxBlocks-1)));
  }

  @Test
  public void lastGetRegionMapped() throws Exception {
    rf1.insertRegion(maxBlocks-1, testBlock);
    ByteBuffer bb;
    try{
      bb = rf1.getRegionMapped(maxBlocks-1);
    } catch(UnsupportedOperationException e){
      return;
    }
    Assert.assertEquals(blockSize, bb.limit()-bb.position());
    byte[] b = new byte[blockSize];
    bb.get(b);
    Assert.assertEquals(new String(testBlock), new String(b));
  }

  @Test
  public void lastReopenRegion() throws Exception {
    rf1.insertRegion(maxBlocks-1, testBlock);
    reop();
    Assert.assertEquals(new String(testBlock), new String(rf1.getRegion(maxBlocks-1)));
  }

  @Test
  public void lastGetBooleanRegion() throws Exception {
    rf1.insertRegion(maxBlocks-1,testBlock);
    byte b[] = new byte[blockSize];
    Assert.assertTrue(rf1.getRegion(maxBlocks-1, b));
    Assert.assertEquals(new String(testBlock), new String(b));
  }

  @Test
  public void lastUpdateRegion() throws Exception {
    rf1.insertRegion(maxBlocks-1,testBlock);
    byte b[] = new byte[blockSize];
    b[0] = 1;
    Assert.assertTrue(rf1.updateRegion(maxBlocks-1,b));
  }

  @Test
  public void lastDeleteRegion() throws Exception {
    rf1.insertRegion(maxBlocks-1,testBlock);
    Assert.assertTrue(rf1.deleteRegion(maxBlocks-1));
    Assert.assertEquals(0, rf1.sizeRegion());
  }

  private void fillRegionFile() throws Exception {
    for(int i = 0; i < maxBlocks; i++)
      rf1.insertRegion(testBlock);
  }
  /**Test with all records added***/
  @Test
  public void allSizeRegion() throws Exception{
    fillRegionFile();
    Assert.assertEquals(maxBlocks, rf1.sizeRegion());
  }

  @Test
  public void allFreeRegion() throws Exception{
    fillRegionFile();
    Assert.assertEquals(0, rf1.getFreeBlocksRegion());
  }

  @Test
  public void allIteratorRegion() throws Exception{
    fillRegionFile();
    int i = 0;
    Iterator<Record> iter = rf1.iteratorRegion();
    while(iter.hasNext()){
      i++;
      iter.next();
    }
    Assert.assertEquals(maxBlocks, i);
  }

  @Test
  public void allContainsRegion() throws Exception{
    fillRegionFile();
    for(int i = 0; i < maxBlocks; i++){
      Assert.assertTrue(rf1.containsRegion(i));
    }
  }

  @Test
  public void allFirstRecordRegion() throws Exception {
    fillRegionFile();
    Assert.assertEquals(0, rf1.getFirstRecordRegion());
  }

  @Test
  public void allGetRegion() throws Exception {
    fillRegionFile();
    for(int i = 0; i < maxBlocks; i++) {
      Assert.assertEquals(new String(testBlock), new String(rf1.getRegion(i)));
    }
  }

  @Test
  public void allGetRegionMapped() throws Exception{
    fillRegionFile();
    for(int i = 0; i < maxBlocks; i++) {
      ByteBuffer bb = null;
      try{
        bb = rf1.getRegionMapped(i);
      } catch(UnsupportedOperationException e){return;}
      byte[] b = new byte[blockSize];
      Assert.assertEquals(blockSize, bb.limit()-bb.position());
      bb.get(b);
      Assert.assertEquals(new String(testBlock), new String(b));
    }
  }

  @Test
  public void allReopenRegion() throws Exception {
    fillRegionFile();
    reop();
    for(int i = 0; i < maxBlocks; i++) {
      Assert.assertEquals(new String(testBlock), new String(rf1.getRegion(i)));
    }
  }

  @Test
  public void allGetBooleanRegion() throws Exception {
    fillRegionFile();
    for(int i = 0; i < maxBlocks; i++){
      byte b[] = new byte[blockSize];
      Assert.assertTrue(rf1.getRegion(i, b));
      Assert.assertEquals(new String(testBlock), new String(b));
    }
  }

  @Test
  public void allUpdateRegion() throws Exception {
    fillRegionFile();
    for(int i = 0; i < maxBlocks; i++) {
      byte b[] = new byte[blockSize];
      b[0] = 1;
      Assert.assertTrue(rf1.updateRegion(i, b));
    }
  }

  @Test
  public void allDeleteRegion() throws Exception {
    fillRegionFile();
    for(int i = 0; i < maxBlocks; i++) {
      Assert.assertTrue(rf1.deleteRegion(i));
      Assert.assertEquals(maxBlocks - 1 - i, rf1.sizeRegion());
    }
  }

  /****Test Error paths**********************/

  @Test
  public void insertLargeBlockRegion() throws Exception {
    byte b[] = new byte[blockSize+1];
    rf1.insertRegion(b);
    Assert.assertEquals(1, rf1.sizeRegion());
  }

  @Test
  public void insertNullRegion() throws Exception {
    rf1.insertRegion(null);
    Assert.assertEquals(1, rf1.sizeRegion());
  }

  @Test(expected = Exception.class)
  public void insertInFullRegion() throws Exception {
    fillRegionFile();
    rf1.insertRegion(testBlock);
  }

  @Test(expected = Exception.class)
  public void insertOutOfRangeRegion() throws Exception {
    rf1.insertRegion(maxBlocks, testBlock);
  }


}
