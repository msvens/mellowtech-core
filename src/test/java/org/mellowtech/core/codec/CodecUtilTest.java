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
 *
 */
public class CodecUtilTest {
  
  @Test
  public void separateIntSecondSmaller() {
    int a = 2, b = 1;
    Assert.assertEquals(2, (int) CodecUtil.separate(a,b, new IntCodec()));
  }
  
  @Test
  public void separateIntFirstSmaller() {
    int a = 1, b = 2;
    Assert.assertEquals(2, (int) CodecUtil.separate(a,b, new IntCodec()));
  }
  
  @Test
  public void separateIntEquals() {
    int a = 1, b = 1;
    Assert.assertEquals(1, (int) CodecUtil.separate(a,b, new IntCodec()));
  }
  
  @Test
  public void separateCharArraySecondSmaller() {
    char[] a = "acd".toCharArray(), b = "abc".toCharArray();
    Assert.assertEquals("ac", new String(CodecUtil.separate(a,b, new CharArrayCodec())));
  }
  
  @Test
  public void separateCharArrayFirstSmaller() {
    char[] a = "abc".toCharArray(), b = "acd".toCharArray();
    Assert.assertEquals("ac", new String(CodecUtil.separate(a,b, new CharArrayCodec())));
  }
  
  @Test
  public void separateCharArrayEquals() {
    char[] a = "abc".toCharArray(), b = "abc".toCharArray();
    Assert.assertEquals("abc", new String(CodecUtil.separate(a,b, new CharArrayCodec())));
  }
  
  @Test
  public void separateStringSecondSmaller() {
    String a = "acd", b = "abc";
    Assert.assertEquals("ac", CodecUtil.separate(a,b, new StringCodec()));
  }
  
  @Test
  public void separateStringFirstSmaller() {
    String a = "abc", b = "acd";
    Assert.assertEquals("ac", CodecUtil.separate(a,b, new StringCodec()));
  }
  
  @Test
  public void separateStringsEquals() {
    String a = "abc", b = "abc";
    Assert.assertEquals("abc", CodecUtil.separate(a,b, new StringCodec()));
  }
  
  @Test
  public void slackOrSizeLessThanFour() {
    ByteBuffer bb = ByteBuffer.allocate(12);
    bb.position(9);
    Assert.assertEquals(-3, CodecUtil.slackOrSize(bb, new StringCodec()));
  }
  
  @Test
  public void slackOrSize(){
    StringCodec codec = new StringCodec();
    String str = "this is a test";
    ByteBuffer bb = new StringCodec().to(str);
    bb.flip();
    Assert.assertEquals(codec.byteSize(str), CodecUtil.slackOrSize(bb, codec));
  }
  
  @Test
  public void slackOrSizeLessThanFully() {
    StringCodec codec = new StringCodec();
    String str = "this is a test";
    ByteBuffer bb = new StringCodec().to(str);
    bb.flip();
    bb.limit(10);
    Assert.assertEquals(-10, CodecUtil.slackOrSize(bb, codec));
  }
  
  @Test
  public void copyToBeginning() {
    ByteBuffer bb = ByteBuffer.allocate(12);
    bb.putInt(1);
    bb.putInt(2);
    bb.putInt(3);
    bb.position(8);
    CodecUtil.copyToBeginning(bb, 4);
    Assert.assertEquals(4, bb.position());
    Assert.assertEquals(3, bb.getInt(0));
  }
  
  @Test
  public void encodedByteSize127(){
    int i = 0xFF >> 1;
    Assert.assertEquals(i + 1, CodecUtil.byteSize(i, true));
  }
  
  @Test
  public void encoddedByteSize255(){
    int i = 0xFF;
    Assert.assertEquals(i + 2, CodecUtil.byteSize(i, true));
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void encodedByteSizeMAX(){
    int i = Integer.MAX_VALUE;
    CodecUtil.byteSize(i, true);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void encodedByteSizeNegative(){
    int i = -1;
    CodecUtil.byteSize(i, true);
  }
  
  @Test
  public void encodeInt127(){
    int i = 0xFF >> 1;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    Assert.assertEquals(1, CodecUtil.encodeInt(i, bb));
  }
  
  @Test
  public void encodeInt255(){
    int i = 0xFF;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    Assert.assertEquals(2, CodecUtil.encodeInt(i, bb));
  }
  
  @Test
  public void encodeIntMax(){
    int i = Integer.MAX_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    Assert.assertEquals(CodecUtil.encodeLength(i), CodecUtil.encodeInt(i, bb));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void encodeIntMin(){
    int i = Integer.MIN_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    Assert.assertEquals(CodecUtil.encodeLength(i), CodecUtil.encodeInt(i, bb));
  }
  
  @Test
  public void encodeIntZero(){
    int i = 0;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    Assert.assertEquals(CodecUtil.encodeLength(i), CodecUtil.encodeInt(i, bb));
  }
  
  @Test
  public void encodeLong127(){
    long i = 0xFF >> 1;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    Assert.assertEquals(1, CodecUtil.encodeLong(i, bb));
  }
  
  @Test
  public void encodeLong255(){
    long i = 0xFF;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    Assert.assertEquals(2, CodecUtil.encodeLong(i, bb));
  }
  
  @Test
  public void encodeLongMax(){
    long i = Long.MAX_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    Assert.assertEquals(CodecUtil.encodeLength(i), CodecUtil.encodeLong(i, bb));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void encodeLongMin(){
    long i = Long.MIN_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    Assert.assertEquals(CodecUtil.encodeLength(i), CodecUtil.encodeLong(i, bb));
  }
  
  @Test
  public void encodeLongZero(){
    long i = 0;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    Assert.assertEquals(CodecUtil.encodeLength(i), CodecUtil.encodeLong(i, bb));
  }
  
  @Test
  public void decodeInt127(){
    int i = 0xFF >> 1;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    CodecUtil.encodeInt(i, bb);
    bb.flip();
    Assert.assertEquals(i, CodecUtil.decodeInt(bb));
  }
  
  @Test
  public void decodeInt255(){
    int i = 0xFF;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    CodecUtil.encodeInt(i, bb);
    bb.flip();
    Assert.assertEquals(i, CodecUtil.decodeInt(bb));
  }
  
  @Test
  public void decodeIntMax(){
    int i = Integer.MAX_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    CodecUtil.encodeInt(i, bb);
    bb.flip();
    Assert.assertEquals(i, CodecUtil.decodeInt(bb));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void decodeIntMin(){
    int i = Integer.MIN_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    CodecUtil.encodeInt(i, bb);
  }
  
  @Test
  public void decodeIntZero(){
    int i = 0;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    CodecUtil.encodeInt(i, bb);
    bb.flip();
    Assert.assertEquals(i, CodecUtil.decodeInt(bb));
  }
  
  @Test
  public void decodeLong127(){
    long i = 0xFF >> 1;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    CodecUtil.encodeLong(i, bb);
    bb.flip();
    Assert.assertEquals(i, CodecUtil.decodeLong(bb));
  }
  
  @Test
  public void decodeLong255(){
    long i = 0xFF;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    CodecUtil.encodeLong(i, bb);
    bb.flip();
    Assert.assertEquals(i, CodecUtil.decodeLong(bb));
  }
  
  @Test
  public void decodeLongMax(){
    long i = Long.MAX_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    CodecUtil.encodeLong(i, bb);
    bb.flip();
    Assert.assertEquals(i, CodecUtil.decodeLong(bb));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void decodeLongMin(){
    long i = Long.MIN_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    CodecUtil.encodeLong(i, bb);
  }
  
  @Test
  public void decodeLongZero(){
    long i = 0;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    CodecUtil.encodeLong(i, bb);
    bb.flip();
    Assert.assertEquals(i, CodecUtil.decodeLong(bb));
  }

}
