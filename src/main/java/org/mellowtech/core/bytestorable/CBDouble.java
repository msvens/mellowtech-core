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
 * Wraps a double value as a ByteStorable
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class CBDouble implements BComparable<Double, CBDouble>{

  private final double value;
  
  public CBDouble() {value = 0.0;}

  public CBDouble(double value) {this.value = value;}
  
  public CBDouble(Double value) {this.value = value;}

  @Override
  public Double get(){return Double.valueOf(value);}
  
  public double value() {return value;}
  
  @Override
  public boolean isFixed(){
    return true;
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
  public void to(ByteBuffer bb) {
    bb.putDouble(value);
  }

  @Override
  public CBDouble from(ByteBuffer bb) {
    return new CBDouble(bb.getDouble());
  }

  @Override
  public int compareTo(CBDouble other) {
    return Double.compare(value, other.value);
  }

  @Override
  public boolean equals(Object other) {
    if(other instanceof CBDouble)
      return compareTo((CBDouble) other) == 0;
    return false;
  }
  
  @Override
  public int hashCode(){
    long bits = Double.doubleToLongBits(value);
    return (int)(bits ^ (bits >>> 32));
  }
  
  @Override
  public String toString(){
    return ""+value;
  }

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
      ByteBuffer bb2) {
    return Double.compare(bb1.getDouble(offset1), bb2.getDouble(offset2));
  }
  
}
