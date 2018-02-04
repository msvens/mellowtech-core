package org.mellowtech.core.io.impl;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mellowtech.core.TestUtils;
import org.mellowtech.core.io.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * Created by msvens on 24/10/15.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class RecordFileTemplate {

  RecordFile rf;
  abstract String fname();
  abstract String fnameMoved();
  static String dir = "rftests";
  static int maxBlocks = 10;
  static int blockSize = 1024;
  static int reserve = 1024;

  static String testString = "01234567ABCDEFGH";
  static byte[] testBlock;

  static {
    StringBuilder stringBuilder = new StringBuilder();
    for(int i = 0; i < 64; i++){
      stringBuilder.append(testString);
    }
    testBlock = stringBuilder.toString().getBytes();
  }

  abstract long blocksOffset();

  abstract RecordFile init(int blockSize, int reserve, int maxBlocks, Path fname) throws Exception;

  abstract RecordFile reopen(Path fname) throws Exception;

  protected void fillFile() throws Exception {
    for(int i = 0; i < maxBlocks; i++)
      rf.insert(testBlock);
  }

  @BeforeAll
  public static void createDir(){
    TestUtils.deleteTempDir(dir);
    TestUtils.createTempDir(dir);
  }

  @AfterAll
  public static void rmDir(){
    TestUtils.createTempDir(dir);
  }

  @BeforeEach
  public void setup() throws Exception{
    rf = init(blockSize,reserve,maxBlocks, TestUtils.getAbsolutePath(dir+"/"+fname()));
  }

  @AfterEach
  public void after() throws Exception{
    rf.remove();
  }

  /**COMMON TEST CASES**/
  @Test
  public void blockSize(){
    assertEquals(blockSize, rf.getBlockSize());
  }

  @Test
  public void reserveSize() throws IOException{
    assertEquals(reserve, rf.getReserve().length);
  }

  @Test
  public void move() throws Exception {
    fillFile();
    RecordFile rf1 = rf.move(TestUtils.getAbsolutePath(dir+"/"+fname()));
    assertFalse(rf.isOpen());
    for(int i = 0; i < maxBlocks; i++) {
      assertEquals(new String(testBlock), new String(rf1.get(i)));
    }
    rf = rf1;
  }

  /****TESTS WITH ZERO ELEMENTS*********/
  @Nested
  @DisplayName("An empty recordfile ")
  class Empty {
    @Test
    @DisplayName("has zero blocks")
    void zeroSize() {
      assertEquals(0, rf.size());
    }

    @Test
    @DisplayName("has all free blocks")
    void zeroFree() {
      assertTrue(maxBlocks <= rf.getFreeBlocks());
    }

    @Test
    @DisplayName("has an empty iterator")
    void zeroIterator() {
      assertFalse(rf.iterator().hasNext());
    }


    @Test
    @DisplayName("when cleared has a file size of 0")
    void zeroClear() throws IOException {
      rf.clear();
      assertEquals(0, rf.size());
    }

    @Test
    @DisplayName("contains no blocks")
    void zeroContains() throws Exception {
      assertFalse(rf.contains(0));
    }

    @Test
    @DisplayName("has no first record")
    void zeroFirstRecord() throws Exception {
      assertEquals(-1, rf.getFirstRecord());
    }

    @Test
    @DisplayName("returns null when getting block 0")
    void zeroGet() throws Exception {
      assertNull(rf.get(0));
    }

    @Test
    @DisplayName("returns null when getting a mapped block")
    void zeroGetMapped() throws Exception {
      try {
        assertNull(rf.getMapped(0));
      } catch (UnsupportedOperationException uoe) {
        ;
      }
    }

    @Test
    @DisplayName("returns false when trying to read into a block")
    void zeroGetBoolean() throws Exception {
      byte b[] = new byte[blockSize];
      assertFalse(rf.get(0, b));
    }

    @Test
    @DisplayName("remains empty after reopen")
    void zeroReopen() throws Exception {
      rf.close();
      rf = reopen(TestUtils.getAbsolutePath(dir + "/" + fname()));
      assertEquals(0, rf.size());
      assertNull(rf.get(0));
    }

    @Test
    @DisplayName("cannot update a block")
    void zeroUpdate() throws Exception {
      byte b[] = new byte[blockSize];
      assertFalse(rf.update(0, b));
    }

    @Test
    @DisplayName("cannot delete a block")
    public void zeroDelete() throws Exception {
      assertFalse(rf.delete(0));
      assertEquals(0, rf.size());
    }
  }

  /****TESTS WITH 1 ELEMENT*************/

  @Nested
  @DisplayName("A RecordFile with only the first record used")
  class First {
    @Test
    @DisplayName("should have a size of 1")
    void oneSize() throws Exception {
      rf.insert(testBlock);
      assertEquals(1, rf.size());
    }

    @Test
    @DisplayName("should have maxBlocks - 1 free blocks")
    void oneFree() throws Exception {
      rf.insert(testBlock);
      assertTrue(maxBlocks - 1 <= rf.getFreeBlocks());
    }

    @Test
    @DisplayName("should have an iterator with only record 0")
    void oneIterator() throws Exception {
      rf.insert(testBlock);
      Iterator<Record> iter = rf.iterator();
      assertEquals(0, iter.next().record);
      assertFalse(iter.hasNext());
    }

    @Test
    @DisplayName("when cleared should have zero blocks")
    void oneClear() throws IOException {
      rf.insert(testBlock);
      rf.clear();
      assertEquals(0, rf.size());
    }

    @Test
    @DisplayName("should contain 1 block")
    void oneContains() throws Exception {
      rf.insert(testBlock);
      assertTrue(rf.contains(0));
    }

    @Test
    @DisplayName("Should returon 0 as its first record")
    void oneFirstRecord() throws Exception {
      rf.insert(testBlock);
      assertEquals(0, rf.getFirstRecord());
    }

    @Test
    @DisplayName("Contents of record 0 should be correct")
    void oneGet() throws Exception {
      rf.insert(testBlock);
      assertEquals(new String(testBlock), new String(rf.get(0)));
    }

    @Test
    @DisplayName("If mapped blocks are support should be able to map block 0")
    void oneGetMapped() throws Exception {
      try {
        rf.insert(testBlock);
        byte[] b = new byte[1024];
        ByteBuffer bb = rf.getMapped(0);
        assertEquals(blockSize, bb.limit() - bb.position());
        bb.get(b);
        assertEquals(new String(testBlock), new String(b));
      } catch (UnsupportedOperationException e) {
        ;
      }
    }

    @Test
    @DisplayName("Should return true when reading record 0")
    void oneGetBoolean() throws Exception {
      rf.insert(testBlock);
      byte b[] = new byte[blockSize];
      assertTrue(rf.get(0, b));
      assertEquals(new String(testBlock), new String(b));
    }

    @Test
    @DisplayName("When reopened should contain 1 record")
    void oneReopen() throws Exception {
      rf.insert(testBlock);
      rf.save();
      rf.close();
      rf = reopen(TestUtils.getAbsolutePath(dir + "/" + fname()));
      assertEquals(1, rf.size());
      assertEquals(new String(testBlock), new String(rf.get(0)));
    }

    @Test
    @DisplayName("Should return true when updating the first record")
    void oneUpdate() throws Exception {
      rf.insert(testBlock);
      byte b[] = new byte[blockSize];
      b[0] = 1;
      assertTrue(rf.update(0, b));
    }

    @Test
    @DisplayName("Should have size 0 after deleting the first record")
    void oneDelete() throws Exception {
      rf.insert(testBlock);
      assertTrue(rf.delete(0));
      assertEquals(0, rf.size());
    }
  }

  @Nested
  @DisplayName("A RecordFile with only the last record used")
  class Last {
    @Test
    @DisplayName("should have a size of 1")
    public void lastSize() throws Exception {
      rf.insert(maxBlocks - 1, testBlock);
      assertEquals(1, rf.size());
    }

    @Test
    @DisplayName("should have maxBlocks - 1 free blocks")
    void lastFree() throws Exception {
      rf.insert(maxBlocks - 1, testBlock);
      assertEquals(maxBlocks - 1, rf.getFreeBlocks());
    }

    @Test
    @DisplayName("should have a single record iterator")
    void lastIterator() throws Exception {
      rf.insert(maxBlocks - 1, testBlock);
      Iterator<Record> iter = rf.iterator();
      assertEquals(maxBlocks - 1, iter.next().record);
      assertFalse(iter.hasNext());
    }

    @Test
    @DisplayName("should have size 0 after clear")
    void lastClear() throws IOException {
      rf.insert(maxBlocks - 1, testBlock);
      rf.clear();
      assertEquals(0, rf.size());
    }

    @Test
    @DisplayName("should contain a record in its last position")
    void lastContains() throws Exception {
      rf.insert(maxBlocks - 1, testBlock);
      assertTrue(rf.contains(maxBlocks - 1));
    }

    @Test
    @DisplayName("its first record should be in the last position")
    void lastFirstRecord() throws Exception {
      rf.insert(maxBlocks - 1, testBlock);
      assertEquals(maxBlocks - 1, rf.getFirstRecord());
    }

    @Test
    @DisplayName("should contain the correct record")
    public void lastGet() throws Exception {
      rf.insert(maxBlocks - 1, testBlock);
      assertEquals(new String(testBlock), new String(rf.get(maxBlocks - 1)));
    }

    @Test
    @DisplayName("should be able to map the last record")
    void lastGetMapped() throws Exception {
      rf.insert(maxBlocks - 1, testBlock);
      ByteBuffer bb = null;
      try {
        bb = rf.getMapped(maxBlocks - 1);
      } catch (UnsupportedOperationException e) {
        return;
      }
      byte[] b = new byte[blockSize];
      assertEquals(blockSize, bb.limit() - bb.position());
      bb.get(b);
      assertEquals(new String(testBlock), new String(b));
    }

    @Test
    @DisplayName("should return true when reading the last record")
    void lastGetBoolean() throws Exception {
      rf.insert(maxBlocks - 1, testBlock);
      byte b[] = new byte[blockSize];
      assertTrue(rf.get(maxBlocks - 1, b));
      assertEquals(new String(testBlock), new String(b));
    }

    @Test
    @DisplayName("should be able to reopen")
    void lastReopen() throws Exception {
      rf.insert(maxBlocks - 1, testBlock);
      rf.close();
      rf = reopen(TestUtils.getAbsolutePath(dir + "/" + fname()));
      assertEquals(new String(testBlock), new String(rf.get(maxBlocks - 1)));
    }

    @Test
    @DisplayName("should return true when update the last record")
    public void lastUpdate() throws Exception {
      rf.insert(maxBlocks - 1, testBlock);
      byte b[] = new byte[blockSize];
      b[0] = 1;
      assertTrue(rf.update(maxBlocks - 1, b));
    }

    @Test
    @DisplayName("should have size 0 when deleting the last record")
    void lastDelete() throws Exception {
      rf.insert(maxBlocks - 1, testBlock);
      assertTrue(rf.delete(maxBlocks - 1));
      assertEquals(0, rf.size());
    }
  }

  @Nested
  @DisplayName("A full RecordFile ")
  class All {
    @Test
    @DisplayName("should have a size equal to max records")
    void allSize() throws Exception {
      fillFile();
      assertEquals(maxBlocks, rf.size());
    }

    @Test
    @DisplayName("should have 0 free blocks")
    void allFree() throws Exception {
      fillFile();
      assertEquals(0, rf.getFreeBlocks());
    }

    @Test
    @DisplayName("should have an iterator over all records")
    public void allIterator() throws Exception {
      fillFile();
      int i = 0;
      Iterator<org.mellowtech.core.io.Record> iter = rf.iterator();
      while (iter.hasNext()) {
        i++;
        iter.next();
      }
      assertEquals(maxBlocks, i);
    }

    @Test
    @DisplayName("when cleared should have 0 records")
    void allClear() throws Exception {
      fillFile();
      rf.clear();
      assertEquals(0, rf.size());
    }


    @Test
    @DisplayName("should contain all records")
    void allContains() throws Exception {
      fillFile();
      for (int i = 0; i < maxBlocks; i++) {
        assertTrue(rf.contains(i));
      }
    }

    @Test
    @DisplayName("first record is 0")
    void allFirstRecord() throws Exception {
      fillFile();
      assertEquals(0, rf.getFirstRecord());
    }

    @Test
    @DisplayName("should get all blocks")
    void allGet() throws Exception {
      fillFile();
      for (int i = 0; i < maxBlocks; i++) {
        assertEquals(new String(testBlock), new String(rf.get(i)));
      }
    }

    @Test
    @DisplayName("should get all records mapped")
    void allGetMapped() throws Exception {
      fillFile();
      for (int i = 0; i < maxBlocks; i++) {
        ByteBuffer bb = null;
        try {
          bb = rf.getMapped(i);
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
    @DisplayName("should be able to read all records")
    void allGetBoolean() throws Exception {
      fillFile();
      for (int i = 0; i < maxBlocks; i++) {
        byte b[] = new byte[blockSize];
        assertTrue(rf.get(i, b));
        assertEquals(new String(testBlock), new String(b));
      }
    }

    @Test
    @DisplayName("should be able to reopen")
    void allReopen() throws Exception {
      fillFile();
      rf.close();
      rf = reopen(TestUtils.getAbsolutePath(dir + "/" + fname()));
      for (int i = 0; i < maxBlocks; i++) {
        assertEquals(new String(testBlock), new String(rf.get(i)));
      }
    }


    @Test
    @DisplayName("should be able to update all records")
    void allUpdate() throws Exception {
      fillFile();
      for (int i = 0; i < maxBlocks; i++) {
        byte b[] = new byte[blockSize];
        b[0] = 1;
        assertTrue(rf.update(i, b));
      }
    }

    @Test
    @DisplayName("should be able to delete all records")
    void allDelete() throws Exception {
      fillFile();
      for (int i = 0; i < maxBlocks; i++) {
        assertTrue(rf.delete(i));
        assertEquals(maxBlocks - 1 - i, rf.size());
      }
    }
  }

  @Nested
  @DisplayName("Hanlding wrong input ")
  class ErrorPath {

    @Test
    @DisplayName("should allow to insert a record that is too big")
    void insertLargeBlock() throws Exception {
      byte b[] = new byte[blockSize + 1];
      rf.insert(b);
      assertEquals(1, rf.size());
    }

    @Test
    @DisplayName("should allow to insert a null record")
    void insertNull() throws Exception {
      rf.insert(null);
      assertEquals(1, rf.size());
    }

    @Test
    @DisplayName("Inserting into a full file throws an exception")
    void insertInFull() throws Exception {
      fillFile();
      assertThrows(Exception.class, () -> {
        rf.insert(testBlock);
      });
    }

    @Test
    @DisplayName("Inserting out of range throws an exception")
    void insertOutOfRange() throws Exception {
      assertThrows(Exception.class, () -> {
        rf.insert(maxBlocks, testBlock);
      });

    }
  }


}
