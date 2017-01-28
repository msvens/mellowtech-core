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

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @author msvens
 * @since 2017-01-27
 */
public abstract  class CodecCompareTemplate<A> extends CodecTemplate<A> {

  @Test
  public void testEquals() throws Exception {
    A a = val(0);
    A b = codec().deepCopy(a);
    Assert.assertTrue(codec().compare(a, b) == 0);
  }

  @Test
  public void testCompareToTSame() throws Exception {
    A a = val(0);
    A b = codec().deepCopy(a);
    Assert.assertEquals(0, codec().compare(a,b));
  }

  @Test
  public void testCompareToLess() throws Exception {
    A a = val(0);
    A b = val(1);
    Assert.assertTrue(codec().compare(a,b) < 0);
  }

  @Test
  public void testByteCompareSameIBIBB() throws Exception {
    byte[] a = codec().to(val(0)).array();
    ByteBuffer b = codec().to(val(0));
    Assert.assertEquals(0, codec().byteCompare(0, a, 0, b));
  }

  @Test
  public void testByteCompareLessIBIBB() throws Exception {
    byte[] a = codec().to(val(0)).array();
    ByteBuffer b = codec().to(val(1));
    Assert.assertTrue(codec().byteCompare(0, a, 0, b) < 0);
  }

  @Test
  public void testByteCompareSameIBBIB() throws Exception {
    ByteBuffer a = codec().to(val(0));
    byte[] b = codec().to(val(0)).array();
    Assert.assertEquals(0, codec().byteCompare(0, a, 0, b));
  }

  @Test
  public void testByteCompareLessIBBIB() throws Exception {
    ByteBuffer a = codec().to(val(0));
    byte[] b = codec().to(val(1)).array();
    Assert.assertTrue(codec().byteCompare(0, a, 0, b) < 0);
  }

  @Test
  public void testByteCompareSameIBBIBB() throws Exception {
    ByteBuffer a = codec().to(val(0));
    ByteBuffer b = codec().to(val(0));
    Assert.assertEquals(0, codec().byteCompare(0, a, 0, b));
  }

  @Test
  public void testByteCompareLessIBBIBB() throws Exception {
    ByteBuffer a = codec().to(val(0));
    ByteBuffer b = codec().to(val(1));
    Assert.assertTrue(codec().byteCompare(0, a, 0, b) < 0);
  }

  @Test
  public void testByteCompareSameIIB() throws Exception {
    ByteBuffer tmpBuff = ByteBuffer.allocate(size(0)*2);
    codec().to(val(0), tmpBuff);
    codec().to(val(0), tmpBuff);
    Assert.assertEquals(0, codec().byteCompare(0, size(0), tmpBuff.array()));
  }

  @Test
  public void testByteCompareLessIIB() throws Exception {
    ByteBuffer tmpBuff = ByteBuffer.allocate(size(0)+size(1));
    codec().to(val(0), tmpBuff);
    codec().to(val(1), tmpBuff);
    Assert.assertTrue(codec().byteCompare(0, size(0), tmpBuff.array()) < 0);
  }

  @Test
  public void testByteCompareSameIIBB() throws Exception {
    ByteBuffer tmpBuff = ByteBuffer.allocate(size(0)*2);
    codec().to(val(0), tmpBuff);
    codec().to(val(0), tmpBuff);
    Assert.assertEquals(0, codec().byteCompare(0, size(0), tmpBuff));
  }

  @Test
  public void testByteCompareLessIIBB() throws Exception {
    ByteBuffer tmpBuff = ByteBuffer.allocate(size(0)+size(1));
    codec().to(val(0), tmpBuff);
    codec().to(val(1), tmpBuff);
    Assert.assertTrue(codec().byteCompare(0, size(0), tmpBuff) < 0);
  }

}
