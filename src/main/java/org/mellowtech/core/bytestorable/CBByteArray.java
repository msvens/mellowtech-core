/*
 * Copyright (c) 2013 mellowtech.org.
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 *
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 *
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 */
package org.mellowtech.core.bytestorable;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Implements a byte array (byte[]) as a ByteStorable for easy storage and
 * retrieval of byte arrays.
 */

public class CBByteArray extends BStorableImp <byte[], CBByteArray> implements BComparable <byte[], CBByteArray> {

  /**
   * Empty constructor.
   */
  public CBByteArray() {super(new byte[0]);}

  /**
   * Creates a new CBByteArray which points to the supplied array.
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
   * @return the array length, or 0 if it is null
   */
  public int getArrayLength() {
    return value.length;
  } // getArrayLength

  public CBByteArray from(ByteBuffer bb) {
	  int nBytes = CBUtil.getSize(bb, true);
	  byte[] tmp = new byte[nBytes];
	  bb.get(tmp);
	  return new CBByteArray(tmp);
  }

  public void to(ByteBuffer bb) {
    CBUtil.putSize(value.length, bb, true);
    bb.put(value);
  }

  public int byteSize() {
    return CBUtil.byteSize(value.length, true);
  }

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
  public int compareTo(CBByteArray other){
    byte[] val1 = other.value;
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