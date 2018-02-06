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

package org.mellowtech.core.codec;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

/**
 * @author msvens
 * @since 2017-01-27
 */
abstract  class CodecCompareTemplate<A> extends CodecTemplate<A> {

  @Test
  void testEquals(){
    A a = val(0);
    A b = codec().deepCopy(a);
    assertTrue(codec().compare(a, b) == 0);
  }

  @Test
  void testCompareToTSame(){
    A a = val(0);
    A b = codec().deepCopy(a);
    assertEquals(0, codec().compare(a,b));
  }

  @Test
  void testCompareToLess(){
    A a = val(0);
    A b = val(1);
    assertTrue(codec().compare(a,b) < 0);
  }

  @Test
  void testByteCompareSameIBIBB(){
    byte[] a = codec().to(val(0)).array();
    ByteBuffer b = codec().to(val(0));
    assertEquals(0, codec().byteCompare(0, a, 0, b));
  }

  @Test
  void testByteCompareLessIBIBB(){
    byte[] a = codec().to(val(0)).array();
    ByteBuffer b = codec().to(val(1));
    assertTrue(codec().byteCompare(0, a, 0, b) < 0);
  }

  @Test
  void testByteCompareSameIBBIB(){
    ByteBuffer a = codec().to(val(0));
    byte[] b = codec().to(val(0)).array();
    assertEquals(0, codec().byteCompare(0, a, 0, b));
  }

  @Test
  void testByteCompareLessIBBIB(){
    ByteBuffer a = codec().to(val(0));
    byte[] b = codec().to(val(1)).array();
    assertTrue(codec().byteCompare(0, a, 0, b) < 0);
  }

  @Test
  void testByteCompareSameIBBIBB(){
    ByteBuffer a = codec().to(val(0));
    ByteBuffer b = codec().to(val(0));
    assertEquals(0, codec().byteCompare(0, a, 0, b));
  }

  @Test
  void testByteCompareLessIBBIBB(){
    ByteBuffer a = codec().to(val(0));
    ByteBuffer b = codec().to(val(1));
    assertTrue(codec().byteCompare(0, a, 0, b) < 0);
  }

  @Test
  void testByteCompareSameIIB(){
    ByteBuffer tmpBuff = ByteBuffer.allocate(size(0)*2);
    codec().to(val(0), tmpBuff);
    codec().to(val(0), tmpBuff);
    assertEquals(0, codec().byteCompare(0, size(0), tmpBuff.array()));
  }

  @Test
  void testByteCompareLessIIB(){
    ByteBuffer tmpBuff = ByteBuffer.allocate(size(0)+size(1));
    codec().to(val(0), tmpBuff);
    codec().to(val(1), tmpBuff);
    assertTrue(codec().byteCompare(0, size(0), tmpBuff.array()) < 0);
  }

  @Test
  void testByteCompareSameIIBB(){
    ByteBuffer tmpBuff = ByteBuffer.allocate(size(0)*2);
    codec().to(val(0), tmpBuff);
    codec().to(val(0), tmpBuff);
    assertEquals(0, codec().byteCompare(0, size(0), tmpBuff));
  }

  @Test
  void testByteCompareLessIIBB(){
    ByteBuffer tmpBuff = ByteBuffer.allocate(size(0)+size(1));
    codec().to(val(0), tmpBuff);
    codec().to(val(1), tmpBuff);
    assertTrue(codec().byteCompare(0, size(0), tmpBuff) < 0);
  }

}
