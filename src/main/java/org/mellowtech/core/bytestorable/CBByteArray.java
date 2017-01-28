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
import java.util.Arrays;

/**
 * BComparable wrapper for byte[]
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 *
 */

public class CBByteArray extends BStorableImp <byte[]> implements BComparable <byte[]> {

  /**
   * Initialize to zero sized byte[]
   */
  public CBByteArray() {super(new byte[0]);}

  /**
   * Initialize to given byte[]
   * 
   * @param byteArray
   *          the byte array
   */
  public CBByteArray(byte[] byteArray) {
    super(byteArray);
  }

  /**
   * Creates a new CBByteArray with initial contents, a new internal byte array
   * is created if offset != 0 and length != byteArray.length, otherwise the
   * internal byte array points to the supplied byte array.
   * 
   * @param byteArray
   *          the byte array
   * @param offset
   *          the offset
   * @param length
   *          the length
   */
  public CBByteArray(byte[] byteArray, int offset, int length) {
    super(Arrays.copyOfRange(byteArray, offset, offset+length));
  }

  /**
   * Gets the array length, or 0 if it is null
   * 
   * @return array length, or 0 if it is null
   */
  public int getArrayLength() {
    return value.length;
  } // getArrayLength

  @Override
  public CBByteArray from(ByteBuffer bb) {
	  int nBytes = CBUtil.getSize(bb, true);
	  byte[] tmp = new byte[nBytes];
	  bb.get(tmp);
	  return new CBByteArray(tmp);
  }

  @Override
  public void to(ByteBuffer bb) {
    CBUtil.putSize(value.length, bb, true);
    bb.put(value);
  }

  @Override
  public int byteSize() {
    return CBUtil.byteSize(value.length, true);
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CBUtil.peekSize(bb, true);
  }
  
  @Override
  public boolean equals(Object o){
    if(o instanceof CBByteArray){
      return this.compareTo((CBByteArray)o) == 0;
    }
    return false;
  }
  
  @Override
  public int compareTo(BComparable<byte[]> other){
    byte[] val1 = other.get();
    int n = Math.min(value.length, val1.length);
    int i = 0;
    byte c1,c2;
    while(n-- != 0){
      c1 = value[i];
      c2 = val1[i];
      if(c1 != c2) return c1 - c2;
      i++;
    }
    return value.length - val1.length;
  }

  @Override
  public int byteCompare(int offset1, byte[] b1, int offset2, byte[] b2) {
    int tmp, len1 = 0, len2 = 0, i = 0;

    // get Size of first array:
    tmp = b1[offset1++] & 0xFF;
    while ((tmp & 0x80) == 0) {
      len1 |= (tmp << (7 * i++));
      tmp = b1[offset1++] & 0xFF;
    }
    len1 |= ((tmp & ~(0x80)) << (7 * i));

    // get Size of second array:
    i = 0;
    tmp = b2[offset2++] & 0xFF;
    while ((tmp & 0x80) == 0) {
      len2 |= (tmp << (7 * i++));
      tmp = b2[offset2++] & 0xFF;
    }
    len2 |= ((tmp & ~(0x80)) << (7 * i));

    // now loop:
    int n = Math.min(len1, len2);
    while (n-- != 0) {
      if (b1[offset1] != b2[offset2])
        return ((int) b1[offset1] & 0xFF) - ((int) b2[offset2] & 0xFF);
      offset1++;
      offset2++;
    }
    return len1 - len2;
  }

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2, ByteBuffer bb2) {
    int tmp, tmp2, len1 = 0, len2 = 0, i = 0;

    // get Size of first array:
    tmp = bb1.get(offset1++) & 0xFF;
    while ((tmp & 0x80) == 0) {
      len1 |= (tmp << (7 * i++));
      tmp = bb1.get(offset1++) & 0xFF;
    }
    len1 |= ((tmp & ~(0x80)) << (7 * i));

    // get Size of second array:
    i = 0;
    tmp = bb2.get(offset2++) & 0xFF;
    while ((tmp & 0x80) == 0) {
      len2 |= (tmp << (7 * i++));
      tmp = bb2.get(offset2++) & 0xFF;
    }
    len2 |= ((tmp & ~(0x80)) << (7 * i));

    // now loop:
    int n = Math.min(len1, len2);
    while (n-- != 0) {
      tmp = (int) bb1.get(offset1);
      tmp2 = (int) bb2.get(offset2);
      if (tmp != tmp2)
        return (tmp & 0xFF) - (tmp2 & 0xFF);
      offset1++;
      offset2++;
    }
    return len1 - len2;
  }
} // CBByteArray
