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

/**
 * Implements a byte array (byte[]) as a ByteStorable for easy storage and
 * retrieval of byte arrays.
 */

public class CBByteArray extends ByteComparable <byte[]> {
  private byte[] fByteArray = null;

  /**
   * Empty constructor.
   */
  public CBByteArray() {
    this.fByteArray = new byte[0];
  }

  /**
   * Creates a new CBByteArray which points to the supplied array.
   * 
   * @param byteArray
   *          the byte array
   */
  public CBByteArray(byte[] byteArray) {
    this.fByteArray = byteArray;
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
    setArray(byteArray, offset, length);
  }

  /**
   * Gets the array
   * 
   * @return the internal array
   */
  public byte[] getArray() {
    return fByteArray;
  } // getArray

  /**
   * Gets the array length, or 0 if it is null
   * 
   * @return the array length, or 0 if it is null
   */
  public int getArrayLength() {
    if (fByteArray == null)
      return 0;
    return fByteArray.length;
  } // getArrayLength

  /**
   * Sets the content as a pointer to the supplied array.
   * 
   * @param byteArray
   */
  @Override
  public void set(byte[] byteArray){
    if(byteArray == null) throw new ByteStorableException("null is not allowed");
    this.fByteArray = (byte[]) byteArray;
  }

  @Override
  public byte[] get(){
    return this.fByteArray;
  }


  /**
   * Sets the array contents, a new internal byte array is created if offset !=
   * 0 and length != byteArray.length, otherwise the internal byte array points
   * to the supplied byte array.
   * 
   * @param byteArray
   *          the byte array
   * @param offset
   *          the offset
   * @param length
   *          the length
   */
  public void setArray(byte[] byteArray, int offset, int length) {
    if (offset == 0 && length == byteArray.length)
      this.fByteArray = byteArray;
    else {
      this.fByteArray = new byte[length];
      System.arraycopy(byteArray, offset, fByteArray, 0, length);
    }
  }

  public ByteStorable <byte[]> fromBytes(ByteBuffer bb, boolean doNew) {
	  CBByteArray aByteArray = this;
	  if(doNew)
		  aByteArray = new CBByteArray();

    int nBytes = getSize(bb);
    aByteArray.fByteArray = new byte[nBytes];
    bb.get(aByteArray.fByteArray, 0, nBytes);

    return aByteArray;
  }

  public void toBytes(ByteBuffer bb) {
    putSize(fByteArray.length, bb);
    bb.put(fByteArray, 0, fByteArray.length);
  }

  public int byteSize() {
    return sizeBytesNeeded(fByteArray.length) + fByteArray.length;
  }

  public int byteSize(ByteBuffer bb) {
    bb.mark();
    int nBytes = getSize(bb);
    nBytes = sizeBytesNeeded(nBytes) + nBytes;
    bb.reset();
    return nBytes;
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
