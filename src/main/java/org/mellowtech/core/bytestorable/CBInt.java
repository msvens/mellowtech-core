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
 * Wraps an int value as a ByteStorable
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class CBInt extends ByteComparable <Integer> {

  /**
   * Value of this CBInt
   * 
   */
  protected int value;

  /**
   * Default constructor is needed to create a new inte from a byte buffer.
   * 
   */
  public CBInt() {}

  /**
   * Initialize with an int value
   * 
   * @param value
   *          the value
   */
  public CBInt(int value) {
    this.value = value;
  }

  // ***********GET/SET**************
  @Override
  public void set(Integer value){
    if(value == null) throw new ByteStorableException("null value not allowed");
    this.value = value;
  }

  @Override
  public Integer get() {
    return value;
  }

  @Override
  public boolean isFixed(){
    return true;
  }

  /**
   * @return hashcode
   * @see Integer#hashCode()
   */
  @Override
  public int hashCode() {
    return value;
  }

  @Override
  public int byteSize() {
    return 4;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return 4;
  }

  @Override
  public void toBytes(ByteBuffer bb) {
    bb.putInt(value);
  }

  @Override
  public ByteStorable <Integer> fromBytes(ByteBuffer bb, boolean doNew) {
    if (doNew)
      return new CBInt(bb.getInt());
    value = bb.getInt();
    return this;
  }

  @Override
  public int compareTo(ByteStorable <Integer> other) {
    return this.value - other.get();
  }

  @Override
  public String toString() {
    return "" + value;
  }

  /** **************UTILITY METHODS****************************** */
  /*public static int readInt(byte[] b, int offset) {
    int char1 = (((char) b[offset]) & 0xFF);
    int char2 = (((char) b[offset + 1]) & 0xFF);
    int char3 = (((char) b[offset + 2]) & 0xFF);
    int char4 = (((char) b[offset + 3]) & 0xFF);
    return ((char1 << 24) + (char2 << 16) + (char3 << 8) + (char4 << 0));
  }

  public static int writeInt(byte[] b, int offset, int value) {
    b[offset] = (byte) ((value >>> 24) & 0xFF);
    b[offset + 1] = (byte) ((value >>> 16) & 0xFF);
    b[offset + 2] = (byte) ((value >>> 8) & 0xFF);
    b[offset + 3] = (byte) ((value >>> 0) & 0xFF);
    return 4;
  }*/

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof CBInt)
      return this.value == ((CBInt)obj).value;
    return false;
  }

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
      ByteBuffer bb2) {
    return bb1.getInt(offset1) - bb2.getInt(offset2);
  }

}
