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
package com.mellowtech.core.bytestorable;

import java.nio.ByteBuffer;


/**
 * @author Martin Svensson
 */

public class CBByte extends ByteComparable <Byte> {

  /**
   * Value of this CBByte
   */
  private byte value;

  /**
   * Default constructor is needed to create a new byte from a byte buffer.
   */
  public CBByte() {
  }

  /**
   * Initialize with a value
   *
   * @param value the value
   */
  public CBByte(byte value) {
    this.value = value;
  }

  // ***********GET/SET**************
  @Override
  public void set(Byte value){
    if(value == null) throw new ByteStorableException("null value not allowed");
    if(!(value instanceof Byte)) throw new ByteStorableException("not a Byte");
    this.value = (byte) value;
  }

  @Override
  public Byte get() {
    return this.value;
  }

  @Override
  public boolean isFixed() {
    return true;
  }

  @Override
  public int hashCode() {
    return value;
  }

  @Override
  public int byteSize() {
    return 1;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return 1;
  }

  @Override
  public void toBytes(ByteBuffer bb) {
    bb.put(value);
  }

  @Override
  public ByteStorable <Byte> fromBytes(ByteBuffer bb) {
    return fromBytes(bb, doNew);
  }

  @Override
  public ByteStorable <Byte> fromBytes(ByteBuffer bb, boolean doNew) {
    if (doNew)
      return new CBByte(bb.get());
    value = bb.get();
    return this;
  }

  @Override
  public int compareTo(ByteStorable <Byte> other) {
    return this.value - other.get();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof CBByte)
      return this.value == ((CBByte)obj).value;
    return false;
  }

  @Override
  public String toString() {
    return "" + value;
  }

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
                         ByteBuffer bb2) {
    return bb1.get(offset1) - bb2.get(offset2);
  }

}

