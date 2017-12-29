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


import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Utility methods for UTF-8 based strings. These methods are generally implemented
 * for both Strings and char[]
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 *
 */
public class UtfUtil {

  /**
   * Convert an array of chars to an utf-8 encoded byte array
   * @param chars array to convert
   * @return utf8 encoded byte array
   */
  /*public static byte[] toBytes(char[] chars){
    ByteBuffer bb = Charset.forName("UTF-8").encode(CharBuffer.wrap(chars));
    byte[] b = new byte[bb.remaining()];
    bb.get(b);
    return b;
  }*/

  /**
   * Calculate the size in bytes of an utf8 encoded string
   * @param str String to encode
   * @return size
   */
  public static int utfLength(final String str) {
    int len = str.length();
    int c;
    int utflen = 0;
    for (int i = 0; i < len; i++) {
      c = str.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007F)) {
        utflen++;
      } else if (c > 0x07FF) {
        utflen += 3;
      } else {
        utflen += 2;
      }
    }
    return utflen;
  }

  /**
   * Calculate the size in bytes of an utf8 encoded String
   * @param str char array to encode
   * @return size
   */
  public static int utfLength(final char[] str){
    return utfLength(str, 0, str.length);
  }

  /**
   * Calculate the size in bytes of an utf8 encoded String
   * @param str char array to encode
   * @param offset offset in array
   * @param length number of chars to read
   * @return size
   */
  public static int utfLength(final char[] str, int offset, int length) {
    int c;
    int utflen = 0;
    for (int i = 0; i < length; i++) {
      c = str[offset++];
      if ((c >= 0x0001) && (c <= 0x007F)) {
        utflen++;
      } else if (c > 0x07FF) {
        utflen += 3;
      } else {
        utflen += 2;
      }
    }
    return utflen;
  }

  /**
   * Utf8 encode a String
   * @param str string to encode
   * @return byte array
   */
  public static byte[] encode(final String str) {
    byte b[] = new byte[utfLength(str)];
    encode(str, b, 0);
    return b;
  }

  /**
   * Utf8 encode a String
   * @param str char array to encode
   * @return byte array
   */
  public static byte[] encode(final char[] str) {
    byte b[] = new byte[utfLength(str)];
    encode(str, b, 0);
    return b;
  }

  /**
   * Utf8 encode a String
   * @param str string to encode
   * @param b destination array
   * @param offset offset in array
   * @return bytes written
   */
  public static int encode(final String str, final byte[] b,
      final int offset) {
    int len = str.length();
    int c, count = offset;

    int i = 0;
    for (i = 0; i < len; i++) {
      c = str.charAt(i);
      if (!((c >= 0x0001) && (c <= 0x007F)))
        break;
      b[count++] = (byte) c;
    }

    // difficult case:
    for (; i < len; i++) {
      c = str.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007F)) {
        b[count++] = (byte) c;

      } else if (c > 0x07FF) {
        b[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
        b[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
        b[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
      } else {
        b[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
        b[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
      }
    }
    return count - offset;
  }

  /**
   * Utf8 encode a string
   * @param str array to encode
   * @param b destination array
   * @param offset offset in destination array
   * @return number of bytes written
   */
  public static int encode(final char[] str, final byte[] b,
      final int offset) {
    int len = str.length;
    int c, count = offset;

    int i = 0;
    for (i = 0; i < len; i++) {
      c = str[i];
      if (!((c >= 0x0001) && (c <= 0x007F)))
        break;
      b[count++] = (byte) c;
    }

    // difficult case:
    for (; i < len; i++) {
      c = str[i];
      if ((c >= 0x0001) && (c <= 0x007F)) {
        b[count++] = (byte) c;

      } else if (c > 0x07FF) {
        b[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
        b[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
        b[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
      } else {
        b[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
        b[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
      }
    }
    return count - offset;
  }

  /**
   * Utf8 encode a string
   * @param str string to encode
   * @param b destination buffer
   */
  public static void encode(final String str, final ByteBuffer b) {
    /*if(b.hasArray()){
      b.position(b.position()+encode(str, b.array(), b.position()));
      return;
    }*/
    int len = str.length();
    int c;

    int i = 0;
    for (i = 0; i < len; i++) {
      c = str.charAt(i);
      if (!((c >= 0x0001) && (c <= 0x007F)))
        break;
      b.put((byte) c);
    }

    // difficult case:
    for (; i < len; i++) {
      c = str.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007F)) {
        b.put((byte) c);

      } else if (c > 0x07FF) {
        b.put((byte) (0xE0 | ((c >> 12) & 0x0F)));
        b.put((byte) (0x80 | ((c >> 6) & 0x3F)));
        b.put((byte) (0x80 | ((c >> 0) & 0x3F)));
      } else {
        b.put((byte) (0xC0 | ((c >> 6) & 0x1F)));
        b.put((byte) (0x80 | ((c >> 0) & 0x3F)));
      }
    }
  }

  /**
   * Utf8 encode a string
   * @param str char array to encode
   * @param b destination buffer
   */
  public static void encode(final char[] str, final ByteBuffer b) {

    int len = str.length;
    int c;

    int i = 0;
    for (i = 0; i < len; i++) {
      c = str[i];
      if (!((c >= 0x0001) && (c <= 0x007F)))
        break;
      b.put((byte) c);
    }

    // difficult case:
    for (; i < len; i++) {
      c = str[i];
      if ((c >= 0x0001) && (c <= 0x007F)) {
        b.put((byte) c);

      } else if (c > 0x07FF) {
        b.put((byte) (0xE0 | ((c >> 12) & 0x0F)));
        b.put((byte) (0x80 | ((c >> 6) & 0x3F)));
        b.put((byte) (0x80 | ((c >> 0) & 0x3F)));
      } else {
        b.put((byte) (0xC0 | ((c >> 6) & 0x1F)));
        b.put((byte) (0x80 | ((c >> 0) & 0x3F)));
      }
    }

  }

  /**
   * Decode an utf8 encoded string
   * @param b source array
   * @return string value
   */
  public static String decode(final byte[] b){
    return decode(b, 0, b.length);
  }

  /**
   * Decode an utf8 encoded string
   * @param b source array
   * @return char array value
   */
  public static char[] decodeChars(final byte[] b){
    return decodeChars(b, 0, b.length);
  }

  /**
   * Decode an utf8 encoded string
   * @param b source array
   * @param offset offset in array
   * @param length number of bytes to read
   * @return string value
   */
  public static String decode(final byte[] b, int offset, int length){
    int count = offset, c_count = 0;
    int c, char2, char3;
    char arr[] = new char[length];
    int to = offset + length;
    while (count < to) {
      c = (int) b[count] & 0xff;
      if (c > 127)
        break;
      count++;
      arr[c_count++] = (char) c;
    }

    // difficult case:
    while (count < to) {
      c = (int) b[count] & 0xff;
      switch (c >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        count++;
        arr[c_count++] = (char) c;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        count += 2;
        if (count > to)
          throw new Error(
              "malformed input: partial character at end");
        char2 = (int) b[count - 1];
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + count);
        arr[c_count++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        count += 3;
        if (count > to)
          throw new Error(
              "malformed input: partial character at end");
        char2 = (int) b[count - 2];
        char3 = (int) b[count - 1];
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (count - 1));
        arr[c_count++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + count+" "+length+" "+to+" "+offset+" "+(c >> 4)+" "+ CodecUtil.decodeInt(b, count)+" "+b[count-1]+" "+b[count-2]+" "+b[count-3]);
      }
    }
    // The number of chars produced may be less than length
    return new String(arr, 0, c_count);
  }

  /**
   * Decode an utf8 encoded string
   * @param b source array
   * @param offset offset in array
   * @param length number of bytes to read
   * @return char array value
   */
  public static char[] decodeChars(final byte[] b, int offset, int length){
    int count = offset, c_count = 0;
    int c, char2, char3;
    char arr[] = new char[length];
    int to = offset + length;
    while (count < to) {
      c = (int) b[count] & 0xff;
      if (c > 127)
        break;
      count++;
      arr[c_count++] = (char) c;
    }

    // difficult case:
    while (count < length) {
      c = (int) b[count] & 0xff;
      switch (c >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        count++;
        arr[c_count++] = (char) c;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        count += 2;
        if (count > length)
          throw new Error(
              "malformed input: partial character at end");
        char2 = (int) b[count - 1];
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + count);
        arr[c_count++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        count += 3;
        if (count > length)
          throw new Error(
              "malformed input: partial character at end");
        char2 = (int) b[count - 2];
        char3 = (int) b[count - 1];
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (count - 1));
        arr[c_count++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + count);
      }
    }
    // The number of chars produced may be less than length
    return Arrays.copyOfRange(arr, 0, c_count);
  }


  /**
   * Decode an utf8 encoded string
   * @param b source buffer
   * @param length bytes to read
   * @return string value
   */
  public static String decode(final ByteBuffer b, int length){
    /*if(b.hasArray()){
      String toRet = decode(b.array(), b.position(), length);
      b.position(b.position()+length);
      System.out.println(toRet);
      return toRet;
    }*/
    int count = 0, c_count = 0;
    int c, char2, char3;
    char arr[] = new char[length];
    
    while (count < length) {
      c = (int) b.get(b.position()) & 0xff;
      if (c > 127)
        break;
      b.get();
      count++;
      arr[c_count++] = (char) c;
    }

    // difficult case:
    while (count < length) {
      c = (int) b.get() & 0xff;
      switch (c >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        count++;
        arr[c_count++] = (char) c;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        count += 2;
        if (count > length)
          throw new Error(
              "malformed input: partial character at end");
        char2 = (int) b.get();
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + count);
        arr[c_count++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        count += 3;
        if (count > length)
          throw new Error(
              "malformed input: partial character at end");
        char2 = (int) b.get();
        char3 = (int) b.get();
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (count - 1));
        arr[c_count++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + count);
      }
    }
    // The number of chars produced may be less than length
    return new String(arr, 0, c_count);
  }

  /**
   * Decode an utf8 encoded string
   * @param b source buffer
   * @param length bytes to read
   * @return char array value
   */
  public static char[] decodeChars(final ByteBuffer b, int length){
    /*if(b.hasArray()){
      return decodeChars(b.array(), b.position(), length);
    }*/
    int count = 0, c_count = 0;
    int c, char2, char3;
    char arr[] = new char[length];
    
    while (count < length) {
      c = (int) b.get(b.position()) & 0xff;
      if (c > 127)
        break;
      b.get();
      count++;
      arr[c_count++] = (char) c;
    }

    // difficult case:
    while (count < length) {
      c = (int) b.get() & 0xff;
      switch (c >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        count++;
        arr[c_count++] = (char) c;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        count += 2;
        if (count > length)
          throw new Error(
              "malformed input: partial character at end");
        char2 = (int) b.get();
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + count);
        arr[c_count++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        count += 3;
        if (count > length)
          throw new Error(
              "malformed input: partial character at end");
        char2 = (int) b.get();
        char3 = (int) b.get();
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (count - 1));
        arr[c_count++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + count);
      }
    }
    // The number of chars produced may be less than length
    return Arrays.copyOfRange(arr, 0, c_count);
  }



  /**
   * Compare two utf8 encoded strings. This method assumes that a size indicator
   * is stored first in each array as defined in CBUtil
   * @param b1 first array
   * @param o1 first array offset
   * @param b2 second array
   * @param o2 second array offset
   * @return a negative integer, zero, or a positive integer as b1 is less than, equal to, or greater than b2.
   * @see CodecUtil#encodeInt(int, byte[], int)
   */
  public static int compare(byte[] b1, int o1, byte[] b2, int o2){


    int length1, length2, c1,c2, num = 0, i = 0;
    
    // length1
    c1 = (b1[o1++] & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b1[o1++] & 0xFF);
      i++;
    }
    length1 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    // length2
    num = 0;
    i = 0;
    c1 = (b2[o2++] & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b2[o2++] & 0xFF);
      i++;
    }
    length2 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    //System.out.println(length1+" "+length2);
    int min = Math.min(length1, length2);
    int count = 0;
    while(count < min){
      c1 = b1[o1] & 0xFF;
      c2 = b2[o2] & 0xFF;
      if(c1 > 127 || c2 > 127)
        break;
      if(c1 != c2)
        return c1 - c2;
      o1++;
      o2++;
      count++;
    }
    //difficult case
    //you only have to update count for the char from the first string
    //since it should be exactly the same as long as the chars are the same
    char cmp1, cmp2;
    int char2, char3;
    while(count < min){
      //first char
      c1 = (int) b1[o1] & 0xff;
      switch (c1 >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        //0xxxxxxx
        count++; o1++;
        cmp1 = (char) c1;
        break;
      case 12:
      case 13:
        //110x xxxx 10xx xxxx
        count += 2;
        o1 += 2;
        char2 = (int) b1[o1 - 1];
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + o1);
        cmp1 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        //1110 xxxx 10xx xxxx 10xx xxxx
        count += 3;
        o1 += 3;
        char2 = (int) b1[o1 - 2];
        char3 = (int) b1[o1 - 1];
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (o1 - 1));
        cmp1 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        //10xx xxxx, 1111 xxxx
        throw new Error("malformed input around byte " + o1);
      }
      
      //second char
      c1 = (int) b2[o2] & 0xff;
      switch (c1 >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        //0xxxxxxx
        o2++;
        cmp2 = (char) c1;
        break;
      case 12:
      case 13:
        //110x xxxx 10xx xxxx
        o2 += 2;
        char2 = (int) b2[o2 - 1];
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + o2);
        cmp2 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        //1110 xxxx 10xx xxxx 10xx xxxx
        o2 += 3;
        char2 = (int) b2[o2 - 2];
        char3 = (int) b2[o2 - 1];
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (count - 1));
        cmp2 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        //10xx xxxx, 1111 xxxx
        throw new Error("malformed input around byte " + count);
      }
      if(cmp1 != cmp2)
        return cmp1 - cmp2;
    }
    //the string starts the same (or are actually the same)
    return length1 - length2;
  }

  /**
   * Compare two utf8 encoded strings. This method assumes that a size indicator
   * is stored first in each array as defined in CBUtil
   * @param b1 first buffer
   * @param o1 first buffer offset
   * @param b2 second buffer
   * @param o2 second buffer offset
   * @return a negative integer, zero, or a positive integer as b1 is less than, equal to, or greater than b2.
   * @see CodecUtil#encodeInt(int, byte[], int)
   */
  public static int compare(ByteBuffer b1, int o1, ByteBuffer b2, int o2){
    
    int length1, length2, c1,c2, num = 0, i = 0;
    
    // length1
    c1 = (b1.get(o1++) & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b1.get(o1++) & 0xFF);
      i++;
    }
    length1 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    // length2
    num = 0;
    i = 0;
    c1 = (b2.get(o2++) & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b2.get(o2++) & 0xFF);
      i++;
    }
    length2 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    
    int min = Math.min(length1, length2);
    int count = 0;
    while(count < min){
      c1 = b1.get(o1) & 0xFF;
      c2 = b2.get(o2) & 0xFF;
      if(c1 > 127 || c2 > 127)
        break;
      if(c1 != c2)
        return c1 - c2;
      o1++;
      o2++;
      count++;
    }
    //difficult case
    //you only have to update count for the char from the first string
    //since it should be exactly the same as long as the chars are the same
    char cmp1, cmp2;
    int char2, char3;
    while(count < min){
      //first char
      c1 = (int) b1.get(o1) & 0xff;
      switch (c1 >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        //0xxxxxxx
        count++; o1++;
        cmp1 = (char) c1;
        break;
      case 12:
      case 13:
        //110x xxxx 10xx xxxx
        count += 2;
        o1 += 2;
        char2 = (int) b1.get(o1 - 1);
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + o1);
        cmp1 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        //1110 xxxx 10xx xxxx 10xx xxxx
        count += 3;
        o1 += 3;
        char2 = (int) b1.get(o1 - 2);
        char3 = (int) b1.get(o1 - 1);
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
          throw new Error("malformed input around byte "
              + (o1 - 1));
        }
        cmp1 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        //10xx xxxx, 1111 xxxx
        throw new Error("malformed input around byte " + o1);
      }
      
      //second char
      c1 = (int) b2.get(o2) & 0xff;
      switch (c1 >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        o2++;
        cmp2 = (char) c1;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        o2 += 2;
        char2 = (int) b2.get(o2 - 1);
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + o2);
        cmp2 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        o2 += 3;
        char2 = (int) b2.get(o2 - 2);
        char3 = (int) b2.get(o2 - 1);
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (count - 1));
        cmp2 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + count);
      }
      if(cmp1 != cmp2)
        return cmp1 - cmp2;
    }
    //the string starts the same (or are actually the same)
    return length1 - length2;
  }

  /**
   * Compare two utf8 encoded strings. This method assumes that a size indicator
   * is stored first in each array as defined in CBUtil
   * @param b1 first array
   * @param o1 first array offset
   * @param b2 second array
   * @param o2 second array offset
   * @param map char map for localized comapre
   * @return a negative integer, zero, or a positive integer as b1 is less than, equal to, or greater than b2.
   * @see CodecUtil#encodeInt(int, byte[], int)
   * @see org.mellowtech.core.util.CompiledLocale
   */
  public static int compare(byte[] b1, int o1, byte[] b2, int o2, char[] map){
    int length1, length2, c1,c2, num = 0, i = 0;
    
    // length1
    c1 = (b1[o1++] & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b1[o1++] & 0xFF);
      i++;
    }
    length1 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    // length2
    num = 0;
    i = 0;
    c1 = (b2[o2++] & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b2[o2++] & 0xFF);
      i++;
    }
    length2 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    //System.out.println(length1+" "+length2);
    int min = Math.min(length1, length2);
    int count = 0;
    while(count < min){
      c1 = b1[o1] & 0xFF;
      c2 = b2[o2] & 0xFF;
      if(c1 > 127 || c2 > 127)
        break;
      if(c1 != c2)
        return map[c1] - map[c2];
      o1++;
      o2++;
      count++;
    }
    //difficult case
    //you only have to update count for the char from the first string
    //since it should be exactly the same as long as the chars are the same
    char cmp1, cmp2;
    int char2, char3;
    while(count < min){
      //first char
      c1 = (int) b1[o1] & 0xff;
      switch (c1 >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        count++; o1++;
        cmp1 = (char) c1;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        count += 2;
        o1 += 2;
        char2 = (int) b1[o1 - 1];
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + o1);
        cmp1 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        count += 3;
        o1 += 3;
        char2 = (int) b1[o1 - 2];
        char3 = (int) b1[o1 - 1];
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (o1 - 1));
        cmp1 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + o1);
      }
      
      //second char
      c1 = (int) b2[o2] & 0xff;
      switch (c1 >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        o2++;
        cmp2 = (char) c1;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        o2 += 2;
        char2 = (int) b2[o2 - 1];
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + o2);
        cmp2 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        o2 += 3;
        char2 = (int) b2[o2 - 2];
        char3 = (int) b2[o2 - 1];
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (count - 1));
        cmp2 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + count);
      }
      if(cmp1 != cmp2)
        return map[cmp1] - map[cmp2];
    }
    //the string starts the same (or are actually the same)
    return length1 - length2;
  }

  /**
   * Compare two utf8 encoded strings. This method assumes that a size indicator
   * is stored first in each array as defined in CBUtil
   * @param b1 first buffer
   * @param o1 first buffer offset
   * @param b2 second buffer
   * @param o2 second buffer offset
   * @param map char map for localized comapre
   * @return a negative integer, zero, or a positive integer as b1 is less than, equal to, or greater than b2.
   * @see CodecUtil#encodeInt(int, byte[], int)
   * @see org.mellowtech.core.util.CompiledLocale
   */
  public static int compare(ByteBuffer b1, int o1, ByteBuffer b2, int o2, char[] map){
    
    int length1, length2, c1,c2, num = 0, i = 0;
    
    // length1
    c1 = (b1.get(o1++) & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b1.get(o1++) & 0xFF);
      i++;
    }
    length1 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    // length2
    num = 0;
    i = 0;
    c1 = (b2.get(o2++) & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b2.get(o2++) & 0xFF);
      i++;
    }
    length2 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    
    int min = Math.min(length1, length2);
    int count = 0;
    while(count < min){
      c1 = b1.get(o1) & 0xFF;
      c2 = b2.get(o2) & 0xFF;
      if(c1 > 127 || c2 > 127)
        break;
      if(c1 != c2)
        return map[c1] - map[c2];
      o1++;
      o2++;
      count++;
    }
    //difficult case
    //you only have to update count for the char from the first string
    //since it should be exactly the same as long as the chars are the same
    char cmp1, cmp2;
    int char2, char3;
    while(count < min){
      //first char
      c1 = (int) b1.get(o1) & 0xff;
      switch (c1 >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        count++; o1++;
        cmp1 = (char) c1;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        count += 2;
        o1 += 2;
        char2 = (int) b1.get(o1 - 1);
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + o1);
        cmp1 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        count += 3;
        o1 += 3;
        char2 = (int) b1.get(o1 - 2);
        char3 = (int) b1.get(o2 - 1);
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (o1 - 1));
        cmp1 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + o1);
      }
      
      //second char
      c1 = (int) b2.get(o2) & 0xff;
      switch (c1 >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        o2++;
        cmp2 = (char) c1;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        o2 += 2;
        char2 = (int) b2.get(o2 - 1);
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + o2);
        cmp2 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        o2 += 3;
        char2 = (int) b2.get(o2 - 2);
        char3 = (int) b2.get(o2 - 1);
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (count - 1));
        cmp2 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + count);
      }
      if(cmp1 != cmp2)
        return map[cmp1] - map[cmp2];
    }
    //the string starts the same (or are actually the same)
    return length1 - length2;
  }

  //normal string comaarisons:
  public static int compare(char[] str1, char[] str2, char[] charMap) {
    //char[] obj1 = other.get();
    int n = Math.min(str1.length, str2.length);
    int i = 0;
    char c1,c2;
    if (charMap == null){
      while(n-- != 0){
        c1 = str1[i];
        c2 = str2[i];
        if(c1 != c2) return c1 - c2;
        i++;
      }
    }
    else {
      while(n-- != 0){
        c1 = charMap[(int) str1[i]];
        c2 = charMap[(int) str2[i]];
        if(c1 != c2) return c1-c2;
        i++;
      }
    }
    return str1.length - str2.length;
  }

  //Correct ones
  /**
   * Compare two utf8 encoded strings. This method assumes that a size indicator
   * is stored first in each array as defined in CBUtil
   * @param b1 first buffer
   * @param o1 first buffer offset
   * @param b2 second buffer
   * @param o2 second buffer offset
   * @param length1 number of bytes to read in first buffer
   * @param length2 number of bytes to read in the second buffer
   * @return a negative integer, zero, or a positive integer as b1 is less than, equal to, or greater than b2.
   * @see CodecUtil#encodeInt(int, byte[], int)
   */
  public static int cmp(ByteBuffer b1, int o1, ByteBuffer b2, int o2, int length1, int length2){

    int c1,c2, num = 0, i = 0;

    /*
    // length1
    c1 = (b1.get(o1++) & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b1.get(o1++) & 0xFF);
      i++;
    }
    length1 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    // length2
    num = 0;
    i = 0;
    c1 = (b2.get(o2++) & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b2.get(o2++) & 0xFF);
      i++;
    }
    length2 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    */


    int min = Math.min(length1, length2);
    int count = 0;
    while(count < min){
      c1 = b1.get(o1) & 0xFF;
      c2 = b2.get(o2) & 0xFF;
      if(c1 > 127 || c2 > 127)
        break;
      if(c1 != c2)
        return c1 - c2;
      o1++;
      o2++;
      count++;
    }
    //difficult case
    //you only have to update count for the char from the first string
    //since it should be exactly the same as long as the chars are the same
    char cmp1, cmp2;
    int char2, char3;
    while(count < min){
      //first char
      c1 = (int) b1.get(o1) & 0xff;
      switch (c1 >> 4) {
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
          //0xxxxxxx
          count++; o1++;
          cmp1 = (char) c1;
          break;
        case 12:
        case 13:
          //110x xxxx 10xx xxxx
          count += 2;
          o1 += 2;
          char2 = (int) b1.get(o1 - 1);
          if ((char2 & 0xC0) != 0x80)
            throw new Error("malformed input around byte "
                + o1);
          cmp1 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
          break;
        case 14:
          //1110 xxxx 10xx xxxx 10xx xxxx
          count += 3;
          o1 += 3;
          char2 = (int) b1.get(o1 - 2);
          char3 = (int) b1.get(o2 - 1);
          if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
            throw new Error("malformed input around byte "
                + (o1 - 1));
          cmp1 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
          break;
        default:
          //10xx xxxx, 1111 xxxx
          throw new Error("malformed input around byte " + o1);
      }

      //second char
      c1 = (int) b2.get(o2) & 0xff;
      switch (c1 >> 4) {
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
        /* 0xxxxxxx */
          o2++;
          cmp2 = (char) c1;
          break;
        case 12:
        case 13:
        /* 110x xxxx 10xx xxxx */
          o2 += 2;
          char2 = (int) b2.get(o2 - 1);
          if ((char2 & 0xC0) != 0x80)
            throw new Error("malformed input around byte "
                + o2);
          cmp2 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
          break;
        case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
          o2 += 3;
          char2 = (int) b2.get(o2 - 2);
          char3 = (int) b2.get(o2 - 1);
          if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
            throw new Error("malformed input around byte "
                + (count - 1));
          cmp2 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
          break;
        default:
        /* 10xx xxxx, 1111 xxxx */
          throw new Error("malformed input around byte " + count);
      }
      if(cmp1 != cmp2)
        return cmp1 - cmp2;
    }
    //the string starts the same (or are actually the same)
    return length1 - length2;
  }


  /**
   * Compare two utf8 encoded strings. This method assumes that a size indicator
   * is stored first in each array as defined in CBUtil
   * @param b1 first buffer
   * @param o1 first buffer offset
   * @param b2 second buffer
   * @param o2 second buffer offset
   * @param map char map for localized comapre
   * @param length1 number of bytes to read in the first buffer
   * @param length2 number of bytes to read in the second buffer
   * @return a negative integer, zero, or a positive integer as b1 is less than, equal to, or greater than b2.
   * @see CodecUtil#encodeInt(int, byte[], int)
   * @see org.mellowtech.core.util.CompiledLocale
   */
  public static int cmp(ByteBuffer b1, int o1, ByteBuffer b2, int o2, char[] map, int length1, int length2){

    if(map == null) return cmp(b1, o1, b2, o2, length1, length2);

    int c1,c2, num = 0, i = 0;

    /*
    // length1
    c1 = (b1.get(o1++) & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b1.get(o1++) & 0xFF);
      i++;
    }
    length1 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    // length2
    num = 0;
    i = 0;
    c1 = (b2.get(o2++) & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b2.get(o2++) & 0xFF);
      i++;
    }
    length2 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    */

    int min = Math.min(length1, length2);
    int count = 0;
    while(count < min){
      c1 = b1.get(o1) & 0xFF;
      c2 = b2.get(o2) & 0xFF;
      if(c1 > 127 || c2 > 127)
        break;
      if(c1 != c2)
        return map[c1] - map[c2];
      o1++;
      o2++;
      count++;
    }
    //difficult case
    //you only have to update count for the char from the first string
    //since it should be exactly the same as long as the chars are the same
    char cmp1, cmp2;
    int char2, char3;
    while(count < min){
      //first char
      c1 = (int) b1.get(o1) & 0xff;
      switch (c1 >> 4) {
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
        /* 0xxxxxxx */
          count++; o1++;
          cmp1 = (char) c1;
          break;
        case 12:
        case 13:
        /* 110x xxxx 10xx xxxx */
          count += 2;
          o1 += 2;
          char2 = (int) b1.get(o1 - 1);
          if ((char2 & 0xC0) != 0x80)
            throw new Error("malformed input around byte "
                + o1);
          cmp1 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
          break;
        case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
          count += 3;
          o1 += 3;
          char2 = (int) b1.get(o1 - 2);
          char3 = (int) b1.get(o2 - 1);
          if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
            throw new Error("malformed input around byte "
                + (o1 - 1));
          cmp1 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
          break;
        default:
        /* 10xx xxxx, 1111 xxxx */
          throw new Error("malformed input around byte " + o1);
      }

      //second char
      c1 = (int) b2.get(o2) & 0xff;
      switch (c1 >> 4) {
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
        /* 0xxxxxxx */
          o2++;
          cmp2 = (char) c1;
          break;
        case 12:
        case 13:
        /* 110x xxxx 10xx xxxx */
          o2 += 2;
          char2 = (int) b2.get(o2 - 1);
          if ((char2 & 0xC0) != 0x80)
            throw new Error("malformed input around byte "
                + o2);
          cmp2 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
          break;
        case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
          o2 += 3;
          char2 = (int) b2.get(o2 - 2);
          char3 = (int) b2.get(o2 - 1);
          if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
            throw new Error("malformed input around byte "
                + (count - 1));
          cmp2 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
          break;
        default:
        /* 10xx xxxx, 1111 xxxx */
          throw new Error("malformed input around byte " + count);
      }
      if(cmp1 != cmp2)
        return map[cmp1] - map[cmp2];
    }
    //the string starts the same (or are actually the same)
    return length1 - length2;
  }

}
