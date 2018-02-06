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
 *
 */
class CodecUtilTest {
  
  @Test
  void separateIntSecondSmaller() {
    int a = 2, b = 1;
    assertEquals(2, (int) CodecUtil.separate(a,b, new IntCodec()));
  }
  
  @Test
  void separateIntFirstSmaller() {
    int a = 1, b = 2;
    assertEquals(2, (int) CodecUtil.separate(a,b, new IntCodec()));
  }
  
  @Test
  void separateIntEquals() {
    int a = 1, b = 1;
    assertEquals(1, (int) CodecUtil.separate(a,b, new IntCodec()));
  }
  
  @Test
  void separateCharArraySecondSmaller() {
    char[] a = "acd".toCharArray(), b = "abc".toCharArray();
    assertEquals("ac", new String(CodecUtil.separate(a,b, new CharArrayCodec())));
  }
  
  @Test
  void separateCharArrayFirstSmaller() {
    char[] a = "abc".toCharArray(), b = "acd".toCharArray();
    assertEquals("ac", new String(CodecUtil.separate(a,b, new CharArrayCodec())));
  }
  
  @Test
  void separateCharArrayEquals() {
    char[] a = "abc".toCharArray(), b = "abc".toCharArray();
    assertEquals("abc", new String(CodecUtil.separate(a,b, new CharArrayCodec())));
  }
  
  @Test
  void separateStringSecondSmaller() {
    String a = "acd", b = "abc";
    assertEquals("ac", CodecUtil.separate(a,b, new StringCodec()));
  }
  
  @Test
  void separateStringFirstSmaller() {
    String a = "abc", b = "acd";
    assertEquals("ac", CodecUtil.separate(a,b, new StringCodec()));
  }
  
  @Test
  void separateStringsEquals() {
    String a = "abc", b = "abc";
    assertEquals("abc", CodecUtil.separate(a,b, new StringCodec()));
  }
  
  @Test
  void slackOrSizeLessThanFour() {
    ByteBuffer bb = ByteBuffer.allocate(12);
    bb.position(9);
    assertEquals(-3, CodecUtil.slackOrSize(bb, new StringCodec()));
  }
  
  @Test
  void slackOrSize(){
    StringCodec codec = new StringCodec();
    String str = "this is a test";
    ByteBuffer bb = new StringCodec().to(str);
    bb.flip();
    assertEquals(codec.byteSize(str), CodecUtil.slackOrSize(bb, codec));
  }
  
  @Test
  void slackOrSizeLessThanFully() {
    StringCodec codec = new StringCodec();
    String str = "this is a test";
    ByteBuffer bb = new StringCodec().to(str);
    bb.flip();
    bb.limit(10);
    assertEquals(-10, CodecUtil.slackOrSize(bb, codec));
  }
  
  @Test
  void copyToBeginning() {
    ByteBuffer bb = ByteBuffer.allocate(12);
    bb.putInt(1);
    bb.putInt(2);
    bb.putInt(3);
    bb.position(8);
    CodecUtil.copyToBeginning(bb, 4);
    assertEquals(4, bb.position());
    assertEquals(3, bb.getInt(0));
  }
  
  @Test
  void encodedByteSize127(){
    int i = 0xFF >> 1;
    assertEquals(i + 1, CodecUtil.byteSize(i, true));
  }
  
  @Test
  void encoddedByteSize255(){
    int i = 0xFF;
    assertEquals(i + 2, CodecUtil.byteSize(i, true));
  }
  
  @Test
  void encodedByteSizeMAX(){
    assertThrows(IllegalArgumentException.class, () -> {
      int i = Integer.MAX_VALUE;
      CodecUtil.byteSize(i, true);
    });
  }
  
  @Test
  void encodedByteSizeNegative(){
    assertThrows(IllegalArgumentException.class, () -> {
    int i = -1;
    CodecUtil.byteSize(i, true);
    });
  }
  
  @Test
  void encodeInt127(){
    int i = 0xFF >> 1;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    assertEquals(1, CodecUtil.encodeInt(i, bb));
  }
  
  @Test
  void encodeInt255(){
    int i = 0xFF;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    assertEquals(2, CodecUtil.encodeInt(i, bb));
  }
  
  @Test
  void encodeIntMax(){
    int i = Integer.MAX_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    assertEquals(CodecUtil.encodeLength(i), CodecUtil.encodeInt(i, bb));
  }
  
  @Test
  void encodeIntMin(){
    assertThrows(IllegalArgumentException.class, () -> {
      int i = Integer.MIN_VALUE;
      ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
      assertEquals(CodecUtil.encodeLength(i), CodecUtil.encodeInt(i, bb));
    });
  }
  
  @Test
  void encodeIntZero(){
    int i = 0;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    assertEquals(CodecUtil.encodeLength(i), CodecUtil.encodeInt(i, bb));
  }
  
  @Test
  void encodeLong127(){
    long i = 0xFF >> 1;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    assertEquals(1, CodecUtil.encodeLong(i, bb));
  }
  
  @Test
  void encodeLong255(){
    long i = 0xFF;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    assertEquals(2, CodecUtil.encodeLong(i, bb));
  }
  
  @Test
  void encodeLongMax(){
    long i = Long.MAX_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    assertEquals(CodecUtil.encodeLength(i), CodecUtil.encodeLong(i, bb));
  }
  
  @Test
  void encodeLongMin(){
    assertThrows(IllegalArgumentException.class, () -> {
      long i = Long.MIN_VALUE;
      ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
      assertEquals(CodecUtil.encodeLength(i), CodecUtil.encodeLong(i, bb));
    });
  }
  
  @Test
  void encodeLongZero(){
    long i = 0;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    assertEquals(CodecUtil.encodeLength(i), CodecUtil.encodeLong(i, bb));
  }
  
  @Test
  void decodeInt127(){
    int i = 0xFF >> 1;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    CodecUtil.encodeInt(i, bb);
    bb.flip();
    assertEquals(i, CodecUtil.decodeInt(bb));
  }
  
  @Test
  void decodeInt255(){
    int i = 0xFF;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    CodecUtil.encodeInt(i, bb);
    bb.flip();
    assertEquals(i, CodecUtil.decodeInt(bb));
  }
  
  @Test
  void decodeIntMax(){
    int i = Integer.MAX_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    CodecUtil.encodeInt(i, bb);
    bb.flip();
    assertEquals(i, CodecUtil.decodeInt(bb));
  }
  
  @Test
  void decodeIntMin(){
    assertThrows(IllegalArgumentException.class, () -> {
      int i = Integer.MIN_VALUE;
      ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
      CodecUtil.encodeInt(i, bb);
    });
  }
  
  @Test
  void decodeIntZero(){
    int i = 0;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    CodecUtil.encodeInt(i, bb);
    bb.flip();
    assertEquals(i, CodecUtil.decodeInt(bb));
  }
  
  @Test
  void decodeLong127(){
    long i = 0xFF >> 1;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    CodecUtil.encodeLong(i, bb);
    bb.flip();
    assertEquals(i, CodecUtil.decodeLong(bb));
  }
  
  @Test
  void decodeLong255(){
    long i = 0xFF;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    CodecUtil.encodeLong(i, bb);
    bb.flip();
    assertEquals(i, CodecUtil.decodeLong(bb));
  }
  
  @Test
  void decodeLongMax(){
    long i = Long.MAX_VALUE;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    CodecUtil.encodeLong(i, bb);
    bb.flip();
    assertEquals(i, CodecUtil.decodeLong(bb));
  }
  
  @Test
  void decodeLongMin(){
    assertThrows(IllegalArgumentException.class, () -> {
      long i = Long.MIN_VALUE;
      ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
      CodecUtil.encodeLong(i, bb);
    });
  }
  
  @Test
  void decodeLongZero(){
    long i = 0;
    ByteBuffer bb = ByteBuffer.allocate(CodecUtil.encodeLength(i));
    CodecUtil.encodeLong(i, bb);
    bb.flip();
    assertEquals(i, CodecUtil.decodeLong(bb));
  }

}
