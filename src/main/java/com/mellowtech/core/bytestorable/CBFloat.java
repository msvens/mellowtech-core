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
 * Wraps a flot value as a ByteStorable
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class CBFloat extends ByteComparable <Float> {


  private float value;


  public CBFloat() {}

  /**
   * Initalize with a value
   * 
   * @param value
   *          the value
   */
  public CBFloat(float value) {
    this.value = value;
  }

  // ***********GET/SET**************
  @Override
  public void set(Float value){
    if(value == null) throw new ByteStorableException("null value not allowed");
    this.value = (Float) value;
  }

  @Override
  public Float get() {
    return value;
  }

  @Override
  public boolean isFixed(){
    return true;
  }

  /**
   * @see Float#floatToIntBits(float)
   */
  public int hashCode() {
    return Float.floatToIntBits(value);
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
    bb.putFloat(value);
  }

  @Override
  public ByteStorable <Float> fromBytes(ByteBuffer bb, boolean doNew) {
    if (doNew)
      return new CBFloat(bb.getFloat());
    value = bb.getFloat();
    return this;
  }

  @Override
  public int compareTo(ByteStorable <Float> other) {
    return Float.compare(this.value, other.get());
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof CBFloat)
      return compareTo((CBFloat) obj) == 0;
    return false;
  }

  public String toString() {
    return "" + value;
  }

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
      ByteBuffer bb2) {
    return Float.compare(bb1.getFloat(offset1), bb2.getFloat(offset2));
  }

}
