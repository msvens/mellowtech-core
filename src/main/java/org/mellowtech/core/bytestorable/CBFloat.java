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
 * BComparable wrapper for float
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 */
public class CBFloat implements BComparable<Float> {

  private final float value;

  /**
   * Initialize to 0.0f
   */
  public CBFloat() {value = 0.0f;}

  /**
   * Initialize to float
   * @param value float to set
   */
  public CBFloat(float value) {this.value = value;}

  /**
   * Initialize to Float
   * @param value Float to set
   */
  public CBFloat(Float value) {this.value = value;}

  @Override
  public CBFloat create(Float value) {return new CBFloat(value);}
  
  @Override
  public Float get(){return value;}

  /**
   * Get the primitive float value
   * @return value
   */
  public Float value(){return value;}

  @Override
  public boolean isFixed(){
    return true;
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
  public void to(ByteBuffer bb) {
    bb.putFloat(value);
  }

  @Override
  public CBFloat from(ByteBuffer bb) {
    return new CBFloat(bb.getFloat());
  }

  @Override
  public int compareTo(BComparable<Float> other) {
    return Float.compare(value, other.get());
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof CBFloat && compareTo((CBFloat) other) == 0;
  }
  
  @Override
  public int hashCode(){
    return Float.floatToIntBits(value);
  }
  
  @Override
  public String toString(){return ""+value;}

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
      ByteBuffer bb2) {
    return Float.compare(bb1.getFloat(offset1), bb2.getFloat(offset2));
  }

}
