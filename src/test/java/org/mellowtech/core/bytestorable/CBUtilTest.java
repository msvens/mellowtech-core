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

package org.mellowtech.core.bytestorable;

import java.nio.ByteBuffer;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author msvens
 *
 */
public class CBUtilTest {
  
  @Test
  public void separateIntSecondSmaller() {
    CBInt str1 = new CBInt(2);
    CBInt str2 = new CBInt(1);
    Assert.assertEquals(1, CBUtil.separate(str1, str2).value());
  }
  
  @Test
  public void separateIntFirstSmaller() {
    CBInt str1 = new CBInt(1);
    CBInt str2 = new CBInt(2);
    Assert.assertEquals(1, CBUtil.separate(str1, str2).value());
  }
  
  @Test
  public void separateIntEquals() {
    CBInt str1 = new CBInt(1);
    CBInt str2 = new CBInt(1);
    Assert.assertEquals(1, CBUtil.separate(str1, str2).value());
  }
  
  @Test
  public void separateCharArraySecondSmaller() {
    CBCharArray str1 = new CBCharArray("acd");
    CBCharArray str2 = new CBCharArray("abc");
    Assert.assertEquals("ac", new String(CBUtil.separate(str1, str2).get()));
  }
  
  @Test
  public void separateCharArrayFirstSmaller() {
    CBCharArray str1 = new CBCharArray("abc");
    CBCharArray str2 = new CBCharArray("acd");
    Assert.assertEquals("ac", new String(CBUtil.separate(str1, str2).get()));
  }
  
  @Test
  public void separateCharArrayEquals() {
    CBCharArray str1 = new CBCharArray("abc");
    CBCharArray str2 = new CBCharArray("abc");
    Assert.assertEquals("abc", new String(CBUtil.separate(str1, str2).get()));
  }
  
  @Test
  public void separateStringSecondSmaller() {
    CBString str1 = new CBString("acd");
    CBString str2 = new CBString("abc");
    Assert.assertEquals("ac", CBUtil.separate(str1, str2).get());
  }
  
  @Test
  public void separateStringFirstSmaller() {
    CBString str1 = new CBString("abc");
    CBString str2 = new CBString("acd");
    Assert.assertEquals("ac", CBUtil.separate(str1, str2).get());
  }
  
  @Test
  public void separateStringsEquals() {
    CBString str1 = new CBString("abc");
    CBString str2 = new CBString("abc");
    Assert.assertEquals("abc", CBUtil.separate(str1, str2).get());
  }
  
  @Test
  public void slackOrSizeLessThanFour() {
    ByteBuffer bb = ByteBuffer.allocate(12);
    bb.position(9);
    Assert.assertEquals(-3, CBUtil.slackOrSize(bb, new CBString()));
  }
  
  @Test
  public void slackOrSize(){
    CBString str = new CBString("this is a test");
    ByteBuffer bb = str.to();
    bb.flip();
    Assert.assertEquals(str.byteSize(), CBUtil.slackOrSize(bb, new CBString()));
  }
  
  @Test
  public void slackOrSizeLessThanFully() {
    CBString str = new CBString("this is a test");
    ByteBuffer bb = str.to();
    bb.flip();
    bb.limit(10);
    Assert.assertEquals(-10, CBUtil.slackOrSize(bb, new CBString()));
  }
  
  @Test
  public void copyToBeginning() {
    ByteBuffer bb = ByteBuffer.allocate(12);
    bb.putInt(1);
    bb.putInt(2);
    bb.putInt(3);
    bb.position(8);
    CBUtil.copyToBeginning(bb, 4);
    Assert.assertEquals(4, bb.position());
    Assert.assertEquals(3, bb.getInt(0));
  }
  
  @Test
  public void encodedByteSize127(){
    int i = 0xFF >> 1;
    Assert.assertEquals(i + 1, CBUtil.byteSize(i, true));
  }
  
  @Test
  public void encoddedByteSize255(){
    int i = 0xFF;
    Assert.assertEquals(i + 2, CBUtil.byteSize(i, true));
  }
  
  @Test(expected=ByteStorableException.class)
  public void encodedByteSizeMAX(){
    int i = Integer.MAX_VALUE;
    CBUtil.byteSize(i, true);
  }
  
  @Test(expected=ByteStorableException.class)
  public void encodedByteSizeNegative(){
    int i = -1;
    CBUtil.byteSize(i, true);
  }
  
  @Test
  public void encodeInt127(){
    int i = 0xFF >> 1;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    Assert.assertEquals(1, CBUtil.encodeInt(i, bb));
  }
  
  @Test
  public void encodeInt255(){
    int i = 0xFF;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    Assert.assertEquals(2, CBUtil.encodeInt(i, bb));
  }
  
  @Test
  public void encodeIntMax(){
    int i = Integer.MAX_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    Assert.assertEquals(CBUtil.encodeLength(i), CBUtil.encodeInt(i, bb));
  }
  
  @Test(expected = ByteStorableException.class)
  public void encodeIntMin(){
    int i = Integer.MIN_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    Assert.assertEquals(CBUtil.encodeLength(i), CBUtil.encodeInt(i, bb));
  }
  
  @Test
  public void encodeIntZero(){
    int i = 0;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    Assert.assertEquals(CBUtil.encodeLength(i), CBUtil.encodeInt(i, bb));
  }
  
  @Test
  public void encodeLong127(){
    long i = 0xFF >> 1;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    Assert.assertEquals(1, CBUtil.encodeLong(i, bb));
  }
  
  @Test
  public void encodeLong255(){
    long i = 0xFF;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    Assert.assertEquals(2, CBUtil.encodeLong(i, bb));
  }
  
  @Test
  public void encodeLongMax(){
    long i = Long.MAX_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    Assert.assertEquals(CBUtil.encodeLength(i), CBUtil.encodeLong(i, bb));
  }
  
  @Test(expected = ByteStorableException.class)
  public void encodeLongMin(){
    long i = Long.MIN_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    Assert.assertEquals(CBUtil.encodeLength(i), CBUtil.encodeLong(i, bb));
  }
  
  @Test
  public void encodeLongZero(){
    long i = 0;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    Assert.assertEquals(CBUtil.encodeLength(i), CBUtil.encodeLong(i, bb));
  }
  
  @Test
  public void decodeInt127(){
    int i = 0xFF >> 1;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    CBUtil.encodeInt(i, bb);
    bb.flip();
    Assert.assertEquals(i, CBUtil.decodeInt(bb));
  }
  
  @Test
  public void decodeInt255(){
    int i = 0xFF;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    CBUtil.encodeInt(i, bb);
    bb.flip();
    Assert.assertEquals(i, CBUtil.decodeInt(bb));
  }
  
  @Test
  public void decodeIntMax(){
    int i = Integer.MAX_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    CBUtil.encodeInt(i, bb);
    bb.flip();
    Assert.assertEquals(i, CBUtil.decodeInt(bb));
  }
  
  @Test(expected = ByteStorableException.class)
  public void decodeIntMin(){
    int i = Integer.MIN_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    CBUtil.encodeInt(i, bb);
  }
  
  @Test
  public void decodeIntZero(){
    int i = 0;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    CBUtil.encodeInt(i, bb);
    bb.flip();
    Assert.assertEquals(i, CBUtil.decodeInt(bb));
  }
  
  @Test
  public void decodeLong127(){
    long i = 0xFF >> 1;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    CBUtil.encodeLong(i, bb);
    bb.flip();
    Assert.assertEquals(i, CBUtil.decodeLong(bb));
  }
  
  @Test
  public void decodeLong255(){
    long i = 0xFF;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    CBUtil.encodeLong(i, bb);
    bb.flip();
    Assert.assertEquals(i, CBUtil.decodeLong(bb));
  }
  
  @Test
  public void decodeLongMax(){
    long i = Long.MAX_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    CBUtil.encodeLong(i, bb);
    bb.flip();
    Assert.assertEquals(i, CBUtil.decodeLong(bb));
  }
  
  @Test(expected = ByteStorableException.class)
  public void decodeLongMin(){
    long i = Long.MIN_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    CBUtil.encodeLong(i, bb);
  }
  
  @Test
  public void decodeLongZero(){
    long i = 0;
    ByteBuffer bb = ByteBuffer.allocate(CBUtil.encodeLength(i));
    CBUtil.encodeLong(i, bb);
    bb.flip();
    Assert.assertEquals(i, CBUtil.decodeLong(bb));
  }

}
