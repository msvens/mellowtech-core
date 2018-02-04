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


  @BeforeEach
  public void setup() throws Exception{
    rf = init(blockSize,reserve,maxBlocks, TestUtils.getAbsolutePath(dir+"/"+fname()));
    rf1 = (SplitRecordFile) rf;
  }

  private void reop() throws Exception{
    rf.close();
    rf = reopen(TestUtils.getAbsolutePath(dir+"/"+fname()));
    rf1 = (SplitRecordFile) rf;
  }

  private void fillRegionFile() throws Exception {
    for(int i = 0; i < maxBlocks; i++)
      rf1.insertRegion(testBlock);
  }

  /**COMMON TEST CASES**/
  @Test
  public void blockSizeRegion(){
    assertEquals(blockSize, rf1.getBlockSizeRegion());
  }


  /****TESTS WITH ZERO ELEMENTS*********/
  @Nested
  @DisplayName("A file with no region records")
  class EmmptyRegion{
    @Test
    void zeroSizeRegion() {
      assertEquals(0, rf1.sizeRegion());
    }

    @Test
    void zeroFreeRegion() {
      assertEquals(maxBlocks, rf1.getFreeBlocksRegion());
    }

    @Test
    void zeroIteratorRegion() {
      assertFalse(rf1.iteratorRegion().hasNext());
    }

    @Test
    void zeroContainsRegion() throws Exception {
      assertFalse(rf1.containsRegion(0));
    }

    @Test
    void zeroFirstRecordRegion() throws Exception {
      assertEquals(-1, rf1.getFirstRecordRegion());
    }

    @Test
    void zeroGetRegion() throws Exception {
      assertNull(rf1.getRegion(0));
    }

    @Test
    void zeroGetRegionMapped() throws Exception {
      assertNull(rf1.getRegionMapped(0));
    }

    @Test
    void zeroReopenRegion() throws Exception {
      reop();
      assertNull(rf1.getRegion(0));
    }

    @Test
    void zeroGetRegionBoolean() throws Exception {
      byte b[] = new byte[blockSize];
      assertFalse(rf1.getRegion(0, b));
    }

    @Test
    void zeroUpdateRegion() throws Exception {
      byte b[] = new byte[blockSize];
      assertFalse(rf1.updateRegion(0, b));
    }

    @Test
    void zeroDeleteRegion() throws Exception {
      assertFalse(rf1.deleteRegion(0));
      assertEquals(0, rf1.sizeRegion());
    }
  }

  /****TESTS WITH 1 ELEMENT*************/
  @Nested
  @DisplayName("A file with only the first region record used")
  class FirstRegion {
    @Test
    void oneSizeRegion() throws Exception {
      rf1.insertRegion(testBlock);
      assertEquals(1, rf1.sizeRegion());
    }

    @Test
    void oneFreeRegion() throws Exception {
      rf1.insertRegion(testBlock);
      assertEquals(maxBlocks - 1, rf1.getFreeBlocksRegion());
    }

    @Test
    void oneIteratorRegion() throws Exception {
      rf1.insertRegion(testBlock);
      assertTrue(rf1.iteratorRegion().hasNext());
    }

    @Test
    void oneContainsRegion() throws Exception {
      rf1.insertRegion(testBlock);
      assertTrue(rf1.containsRegion(0));
    }

    @Test
    void oneFirstRecordRegion() throws Exception {
      rf1.insertRegion(testBlock);
      assertEquals(0, rf1.getFirstRecordRegion());
    }

    @Test
    void oneGetRegion() throws Exception {
      rf1.insertRegion(testBlock);
      assertEquals(new String(testBlock), new String(rf1.getRegion(0)));
    }

    @Test
    void oneGetRegionMapped() throws Exception {
      rf1.insertRegion(testBlock);
      ByteBuffer bb;
      try {
        bb = rf1.getRegionMapped(0);
      } catch (UnsupportedOperationException e) {
        return;
      }
      assertEquals(blockSize, bb.limit() - bb.position());
      byte[] b = new byte[blockSize];
      bb.get(b);
      assertEquals(new String(testBlock), new String(b));
    }

    @Test
    void oneReopenRegion() throws Exception {
      rf1.insertRegion(testBlock);
      reop();
      assertEquals(new String(testBlock), new String(rf1.getRegion(0)));
    }

    @Test
    void oneGetBooleanRegion() throws Exception {
      rf1.insertRegion(testBlock);
      byte b[] = new byte[blockSize];
      assertTrue(rf1.getRegion(0, b));
      assertEquals(new String(testBlock), new String(b));
    }

    @Test
    void oneUpdateRegion() throws Exception {
      rf1.insertRegion(testBlock);
      byte b[] = new byte[blockSize];
      b[0] = 1;
      assertTrue(rf1.updateRegion(0, b));
    }

    @Test
    void oneDeleteRegion() throws Exception {
      rf1.insertRegion(testBlock);
      assertTrue(rf1.deleteRegion(0));
      assertEquals(0, rf1.sizeRegion());
    }
  }

  @Nested
  @DisplayName("A file with only the last region record used")
  class LastRegion {

    @Test
    void lastSizeResion() throws Exception {
      rf1.insertRegion(maxBlocks - 1, testBlock);
      assertEquals(1, rf1.sizeRegion());
    }

    @Test
    void lastFreeRegion() throws Exception {
      rf1.insertRegion(maxBlocks - 1, testBlock);
      assertEquals(maxBlocks - 1, rf1.getFreeBlocksRegion());
    }

    @Test
    void lastIteratorRegion() throws Exception {
      rf1.insertRegion(maxBlocks - 1, testBlock);
      assertTrue(rf1.iteratorRegion().hasNext());
    }

    @Test
    void lastContainsRegion() throws Exception {
      rf1.insertRegion(maxBlocks - 1, testBlock);
      assertTrue(rf1.containsRegion(maxBlocks - 1));
    }

    @Test
    void lastFirstRecordRegion() throws Exception {
      rf1.insertRegion(maxBlocks - 1, testBlock);
      assertEquals(maxBlocks - 1, rf1.getFirstRecordRegion());
    }

    @Test
    void lastGetRegion() throws Exception {
      rf1.insertRegion(maxBlocks - 1, testBlock);
      assertEquals(new String(testBlock), new String(rf1.getRegion(maxBlocks - 1)));
    }

    @Test
    void lastGetRegionMapped() throws Exception {
      rf1.insertRegion(maxBlocks - 1, testBlock);
      ByteBuffer bb;
      try {
        bb = rf1.getRegionMapped(maxBlocks - 1);
      } catch (UnsupportedOperationException e) {
        return;
      }
      assertEquals(blockSize, bb.limit() - bb.position());
      byte[] b = new byte[blockSize];
      bb.get(b);
      assertEquals(new String(testBlock), new String(b));
    }

    @Test
    void lastReopenRegion() throws Exception {
      rf1.insertRegion(maxBlocks - 1, testBlock);
      reop();
      assertEquals(new String(testBlock), new String(rf1.getRegion(maxBlocks - 1)));
    }

    @Test
    void lastGetBooleanRegion() throws Exception {
      rf1.insertRegion(maxBlocks - 1, testBlock);
      byte b[] = new byte[blockSize];
      assertTrue(rf1.getRegion(maxBlocks - 1, b));
      assertEquals(new String(testBlock), new String(b));
    }

    @Test
    void lastUpdateRegion() throws Exception {
      rf1.insertRegion(maxBlocks - 1, testBlock);
      byte b[] = new byte[blockSize];
      b[0] = 1;
      assertTrue(rf1.updateRegion(maxBlocks - 1, b));
    }

    @Test
    void lastDeleteRegion() throws Exception {
      rf1.insertRegion(maxBlocks - 1, testBlock);
      assertTrue(rf1.deleteRegion(maxBlocks - 1));
      assertEquals(0, rf1.sizeRegion());
    }
  }


  @Nested
  @DisplayName("a File with all region records used")
  class AllRegion {
    @Test
    void allSizeRegion() throws Exception {
      fillRegionFile();
      assertEquals(maxBlocks, rf1.sizeRegion());
    }

    @Test
    void allFreeRegion() throws Exception {
      fillRegionFile();
      assertEquals(0, rf1.getFreeBlocksRegion());
    }

    @Test
    void allIteratorRegion() throws Exception {
      fillRegionFile();
      int i = 0;
      Iterator<Record> iter = rf1.iteratorRegion();
      while (iter.hasNext()) {
        i++;
        iter.next();
      }
      assertEquals(maxBlocks, i);
    }

    @Test
    void allContainsRegion() throws Exception {
      fillRegionFile();
      for (int i = 0; i < maxBlocks; i++) {
        assertTrue(rf1.containsRegion(i));
      }
    }

    @Test
    void allFirstRecordRegion() throws Exception {
      fillRegionFile();
      assertEquals(0, rf1.getFirstRecordRegion());
    }

    @Test
    void allGetRegion() throws Exception {
      fillRegionFile();
      for (int i = 0; i < maxBlocks; i++) {
        assertEquals(new String(testBlock), new String(rf1.getRegion(i)));
      }
    }

    @Test
    void allGetRegionMapped() throws Exception {
      fillRegionFile();
      for (int i = 0; i < maxBlocks; i++) {
        ByteBuffer bb = null;
        try {
          bb = rf1.getRegionMapped(i);
        } catch (UnsupportedOperationException e) {
          return;
        }
        byte[] b = new byte[blockSize];
        assertEquals(blockSize, bb.limit() - bb.position());
        bb.get(b);
        assertEquals(new String(testBlock), new String(b));
      }
    }

    @Test
    void allReopenRegion() throws Exception {
      fillRegionFile();
      reop();
      for (int i = 0; i < maxBlocks; i++) {
        assertEquals(new String(testBlock), new String(rf1.getRegion(i)));
      }
    }

    @Test
    void allGetBooleanRegion() throws Exception {
      fillRegionFile();
      for (int i = 0; i < maxBlocks; i++) {
        byte b[] = new byte[blockSize];
        assertTrue(rf1.getRegion(i, b));
        assertEquals(new String(testBlock), new String(b));
      }
    }

    @Test
    void allUpdateRegion() throws Exception {
      fillRegionFile();
      for (int i = 0; i < maxBlocks; i++) {
        byte b[] = new byte[blockSize];
        b[0] = 1;
        assertTrue(rf1.updateRegion(i, b));
      }
    }

    @Test
    public void allDeleteRegion() throws Exception {
      fillRegionFile();
      for (int i = 0; i < maxBlocks; i++) {
        assertTrue(rf1.deleteRegion(i));
        assertEquals(maxBlocks - 1 - i, rf1.sizeRegion());
      }
    }
  }

  /****Test Error paths**********************/

  @Nested
  @DisplayName("Hanling wrong input in region records")
  class RegionErrorPath {
    @Test
    void insertLargeBlockRegion() throws Exception {
      byte b[] = new byte[blockSize + 1];
      rf1.insertRegion(b);
      assertEquals(1, rf1.sizeRegion());
    }

    @Test
    void insertNullRegion() throws Exception {
      rf1.insertRegion(null);
      assertEquals(1, rf1.sizeRegion());
    }

    @Test
    public void insertInFullRegion() throws Exception {
      fillRegionFile();
      assertThrows(Exception.class, () -> {
        rf1.insertRegion(testBlock);
      });
    }

    @Test
    public void insertOutOfRangeRegion() throws Exception {
      assertThrows(Exception.class, () -> {
        rf1.insertRegion(maxBlocks, testBlock);
      });

    }
  }


}
