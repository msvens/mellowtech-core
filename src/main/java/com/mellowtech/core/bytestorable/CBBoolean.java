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

public class CBBoolean extends ByteComparable <Boolean> {

  /**
   * Value of this CBBoolean
   */
  private boolean value;

  /**
   * Default constructor is needed to create a new byte from a byte buffer.
   */
  public CBBoolean() {
  }

  /**
   * Initialize with a value
   *
   * @param value the value
   */
  public CBBoolean(boolean value) {
    this.value = value;
  }

  public CBBoolean(byte b){
    this.value = (b == 0) ? false : true;
  }

  // ***********GET/SET**************
  @Override
  public void set(Boolean value){
    if(value == null) throw new ByteStorableException("null value not allowed");
    if(!(value instanceof Boolean)) throw new ByteStorableException("not a Byte");
    this.value = (boolean) value;
  }

  @Override
  public Boolean get() {
    return this.value;
  }

  @Override
  public boolean isFixed() {
    return true;
  }

  @Override
  public int hashCode() {
    return value ? 1231 : 1237;
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
    bb.put(value ? (byte) 1 : 0);
  }

  @Override
  public ByteStorable <Boolean> fromBytes(ByteBuffer bb) {
    return fromBytes(bb, doNew);
  }

  @Override
  public ByteStorable <Boolean> fromBytes(ByteBuffer bb, boolean doNew) {
    if (doNew)
      return new CBBoolean(bb.get());
    value = (bb.get() == 0) ? false : true;
    return this;
  }

  @Override
  public int compareTo(ByteStorable<Boolean> other) {
    CBBoolean o = (CBBoolean) other;
    return Boolean.compare(this.value, o.value);
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof CBBoolean)
      return compareTo((CBBoolean) obj) == 0;
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

