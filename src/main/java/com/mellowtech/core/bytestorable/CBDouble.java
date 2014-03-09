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
 * Wraps an double value as a ByteStorable
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class CBDouble extends ByteComparable <CBDouble>{


  private double value;

  public CBDouble() {}

  /**
   * Initialize with a value
   * 
   * @param value
   *          the value
   */
  public CBDouble(double value) {
    this.value = value;
  }

  // ***********GET/SET**************
  @Override
  public void set(Object value){
    if(value == null)
      throw new ByteStorableException("null values not exepted");
    else if(!(value instanceof Double))
      throw new ByteStorableException("Object is not Double");
    this.value = (Double) value;
  }

  @Override
  public Double get() {
    return value;
  }

  @Override
  public boolean isFixed(){
    return true;
  }

  public int hashCode() {
    long bits = Double.doubleToLongBits(value);
    return (int) (bits ^ (bits >>> 32));
  }

  @Override
  public int byteSize() {
    return 8;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return 8;
  }

  @Override
  public void toBytes(ByteBuffer bb) {
    bb.putDouble(value);
  }

  @Override
  public ByteStorable <CBDouble> fromBytes(ByteBuffer bb) {
    return fromBytes(bb, doNew);
  }

  @Override
  public ByteStorable <CBDouble> fromBytes(ByteBuffer bb, boolean doNew) {
    if (doNew)
      return new CBDouble(bb.getDouble());
    value = bb.getDouble();
    return this;
  }

  @Override
  public int compareTo(CBDouble other) {
    return Double.compare(this.value, other.value);
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof CBDouble)
      return compareTo((CBDouble) obj) == 0;
    return false;
  }

  @Override
  public String toString() {
    return "" + value;
  }

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
      ByteBuffer bb2) {
    
    return Double.compare(bb1.getDouble(offset1), bb2.getDouble(offset2));
    
  }
}