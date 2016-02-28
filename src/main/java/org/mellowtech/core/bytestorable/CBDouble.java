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

/**
 * BComparable wrapper for double
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 */
public class CBDouble implements BComparable<Double, CBDouble>{

  private final double value;

  /**
   * Initialize to 0.0
   */
  public CBDouble() {value = 0.0;}

  /**
   * Initialize to double
   * @param value double to set
   */
  public CBDouble(double value) {this.value = value;}

  /**
   * Initialize to Double
   * @param value Double to set
   */
  public CBDouble(Double value) {this.value = value;}

  @Override
  public Double get(){return value;}

  /**
   * Get the primitive double value
   * @return value
   */
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
    return other instanceof CBDouble && compareTo((CBDouble) other) == 0;
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
