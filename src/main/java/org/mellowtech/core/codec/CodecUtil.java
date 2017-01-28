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
import java.util.Arrays;

/**
 * Utility methods for working with ByteStorables
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 *
 */
public class CodecUtil {

  //public static Charset Utf8 = Charset.forName("UTF-8");

  /********************* ENCODING/DECODING*******************************/
  /**
   * Variable encode an int. Value has to be greater or equal to zero.
   * @param val value to encode
   * @param bb buffer to store it
   * @return number of bytes
   */
  public static int encodeInt(int val, ByteBuffer bb) {
    if(val < 0) throw new CodecException("negative value");
    int c, count = 1;
    c = (val & 0x7F);
    val = (val >> 7);
    while (val > 0) {
      bb.put((byte) (c & 0xFF));
      c = (val & 0x7F);
      val = val >> 7;
      count++;
    }
    c = (c | 0x80);
    bb.put((byte) (c & 0xFF));
    return count;
  }

  /**
   * Variable encode an int. Value has to be greater or equal to zero.
   * @param val value to encode
   * @param b buffer to store it
   * @param offset position in buffer
   * @return number of bytes
   */
  public static int encodeInt(int val, byte[] b, int offset) {
    if(val < 0) throw new IllegalArgumentException("negative value");
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.position(offset);
    return encodeInt(val, bb);
  }

  /**
   * Variable encode a long. Value has to be greater or equal to zero.
   * @param val value to encode
   * @param bb buffer to store it
   * @return number of bytes
   */
  public static final int encodeLong(long val, ByteBuffer bb) {
    if(val < 0) throw new IllegalArgumentException("negative value");
    long c;
    int count = 1;
    c = (val & 0x7F);
    val = (val >> 7);
    while (val > 0) {
      bb.put((byte) (c & 0xFF));
      c = (val & 0x7F);
      val = val >> 7;
      count++;
    }
    c = (c | 0x80);
    bb.put((byte) (c & 0xFF));
    return count;
  }

  /**
   * Variable encode a long. Value has to be greater or equal to zero.
   * @param val value to encode
   * @param b buffer to store it
   * @param offset position in buffer
   * @return number of bytes
   */
  public static final int encodeLong(long val, byte[] b, int offset) {
    if(val < 0) throw new IllegalArgumentException("negative value");
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.position(offset);
    return encodeLong(val, bb);
  }

  /**
   * Number of byte required to encode value
   * @param val value to encode
   * @return number of bytes
   */
  public static final int encodeLength(int val) {
    if(val < 0) throw new IllegalArgumentException("negative value");
    int count = 1;
    val = (val >> 7);
    while (val > 0) {
      count++;
      val = val >> 7;
    }
    return count;
  }

  /**
   * Number of byte required to encode value
   * @param val value to encode
   * @return number of bytes
   */
  public static final int encodeLength(long val) {
    if(val < 0) throw new IllegalArgumentException("negative value");
    int count = 1;
    val = (val >> 7);
    while (val > 0) {
      count++;
      val = val >> 7;
    }
    return count;
  }

  /**
   * Decodes a variable encoded int
   * @param bb bytebuffer to read from
   * @return decoded value
   */
  public static int decodeInt(ByteBuffer bb) {
    int c, num = 0, i = 0;

    c = (bb.get() & 0xFF);
    while ((c & 0x80) == 0) {
      num |= (c << (7 * i));
      c = (bb.get() & 0xFF);
      i++;
    }
    num |= ((c & ~(0x80)) << (7 * i));
    return num;
  }

  /**
   * Decodes a variable encoded int
   * @param b array to read from
   * @param offset array offset
   * @return decoded value
   */
  public static int decodeInt(byte[] b, int offset) {
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.position(offset);
    return decodeInt(bb);
  }

  /**
   * Decodes a variable encoded long
   * @param bb bytebuffer to read from
   * @return decoded value
   */
  public static long decodeLong(ByteBuffer bb) {
    long c, i = 0;
    long num = 0;

    c = (bb.get() & 0xFF);
    while ((c & 0x80) == 0) {
      num |= (c << (7 * i));
      c = (bb.get() & 0xFF);
      i++;
    }
    num |= ((c & ~(0x80)) << (7 * i));
    return num;
  }

  /**
   * Decodes a variable encoded long
   * @param b array to read from
   * @param offset array offset
   * @return decoded value
   */
  public static long decodeLong(byte[] b, int offset) {
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.position(offset);
    return decodeLong(bb);
  }

  //******************** Utility methods for ByteStorable Sizes ***************/

  /**
   * Reads an int from a byteBuffer without changing the buffer position
   * @param bb bytebuffer to read from
   * @param encoded true if the int was variable encoded
   * @return the int value
   */
  public static final int peekInt(ByteBuffer bb, boolean encoded) {
    int pos = bb.position();
    int toRet = encoded ? decodeInt(bb) : bb.getInt();
    bb.position(pos);
    return toRet;
  }

  /**
   * Reads an int from a ByteBuffer and in the process change the buffer position
   * @param bb bytebuffer to read from
   * @param encoded true if the int was variable encoded
   * @return the int value
   */
  public static final int getInt(ByteBuffer bb, boolean encoded) {
    return encoded ? decodeInt(bb) : bb.getInt();
  }

  /**
   * Puts an int to a ByteBuffer
   * @param size the value to put
   * @param bb the byteBuffer to use
   * @param encoded true if the value should be variable encoded
   * @return number of bytes written
   */
  public static final int putSize(int size, ByteBuffer bb, boolean encoded) {
    if (encoded) {
      return encodeInt(size, bb);
    }
    bb.putInt(size);
    return 4;
  }

  /**
   * Reads a size indicator from a buffer. Effectively calls getInt(bb, encoded)
   * @param bb buffer to read from
   * @param encoded true if the value is variable encoded
   * @return the value
   */
  public static final int getSize(ByteBuffer bb, boolean encoded) {
    return getInt(bb, encoded);
  }

  /**
   * Returns the effective byte size of something of size val, including
   * any bytes to store a size indicator
   * @param val byte size
   * @param encoded true if the size indicator should be variable encoded
   * @return byte size with size indicator
   */
  public static final int byteSize(int val, boolean encoded) {
    if(val < 0 || val >= Integer.MAX_VALUE - 5)
      throw new IllegalArgumentException("valute out of range");
    return encoded ? encodeLength(val) + val : 4 + val;
  }

  /**
   * Reads the byte size from a buffer without moving the buffer position
   * @param bb buffer to read from
   * @param encoded true if the size indicator was variable encoded
   * @return byte size
   */
  public static final int peekSize(ByteBuffer bb, boolean encoded) {
    int val = peekInt(bb, encoded);
    return encoded ? encodeLength(val) + val : 4 + val;
  }

  //******************* END methods for ByteStorable Sizes ********************

  /**
   * Separates to BComparables. If the BComparables are CBstrings or CBCharArrays
   * the corresponding separate methods will be called. Else the largest value
   * will be returned
   * @param first first value
   * @param second second value
   * @param <A> BComparable
   * @return A new value of B
   * @see CodecUtil#separate(String, String, StringCodec)
   * @see CodecUtil#separate(char[], char[], CharArrayCodec)
   */
  public static <A> A separate(A first, A second, BCodec<A> codec){
    if(first instanceof String){
      return (A) separate((String) first, (String) second, (StringCodec) codec);
    }
    else if(first instanceof char[]){
      return (A) separate((char[])first, (char[]) second, (CharArrayCodec) codec);
    }
    return codec.compare(first,second) > 0 ? codec.deepCopy(first) : codec.deepCopy(second);
  }

  /**
   * Returns the smallest separator of 2 strings. If the first string is
   * "ABCD" and the second string is "ACA" the returned separator will be "AC"
   * @param first first string
   * @param second second string
   * @return a new CBString
   */
  public static String separate(String first, String second, StringCodec codec){
    String small, large;

    if (codec.compare(first,second) < 0) {
      small = first;
      large = second;
    }
    else {
      large = first;
      small = second;
    }
    int i;
    for(i = 0; i < small.length(); i++){
      if(small.charAt(i) != large.charAt(i))
        break;
    }
    if (small.length() == large.length() && i == large.length()) {
      return new String(large);
    }

    return new String((new String(large.substring(0, i + 1))));
  }

  /**
   * Returns the smallest separator of 2 char arrays. If the first array is
   * "ABCD" and the second array is "ACA" the returned separator will be "AC"
   * @param first first string
   * @param second second string
   * @return a new CBCharArray
   */
  public final static char[] separate(char[] first, char[] second, CharArrayCodec codec){
      char[] small, large;

      if (codec.compare(first,second) < 0) {
        small = first;
        large = second;
      }
      else {
        large = first;
        small = second;
      }
      int i;
      for(i = 0; i < small.length; i++){
        if(small[i] != large[i])
          break;
      }


      if (small.length == large.length && i == large.length) {
        return large;
      }
      return Arrays.copyOf(large, i+1);
  }



  /**
   * Starting at the current position copy a number bytes to the beginning of
   * the buffer and set the new position to just after the copied bytes.
   *
   * @param bb
   *          buffer
   * @param numBytes
   *          number of bytes to copy
   */
  public final static void copyToBeginning(ByteBuffer bb, int numBytes) {
    if (numBytes == 0) {
      bb.clear();
      return;
    }
    byte[] b = new byte[numBytes];
    bb.get(b);
    bb.clear();
    bb.put(b);
  }

  /**
   * Calculates the number of bytes that the next BStorable will need to be
   * fully read. If the buffer does not fully contain the next BStorable it
   * will return the number of bytes that are left in this buffer as a negative
   * value.
   *
   * @param bb
   *          a buffer
   * @param codec
   *          a BCodec to use for calculating the size
   * @return the next ByteStorable's size or -(bytes left in buffer)
   */
  public final static int slackOrSize(ByteBuffer bb, BCodec<?> codec) {
    int left = bb.remaining();
    if (bb.remaining() < 4){
      return -left;
    }
    bb.mark();
    int bSize = codec.byteSize(bb);
    bb.reset();
    if (bSize > left){
      return -left;
    }
    return bSize;
  }

}
