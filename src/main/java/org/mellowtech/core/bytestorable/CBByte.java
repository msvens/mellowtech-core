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
 * BComparable wrapper for bytes
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 */

public class CBByte implements BComparable <Byte> {

  private final byte value;

  /**
   * Initialize to 0
   */
  public CBByte() {value = (byte) 0;}

  /**
   * Initialize to value
   * @param value value to set
   */
  public CBByte(byte value) {this.value = value;}

  /**
   * Initialize to value
   * @param value value to set
   */
  public CBByte(Byte value) {this.value = value;}
  
  @Override
  public Byte get() {return Byte.valueOf(value);}

  /**
   * Get the value as the primitive type
   * @return the value
   */
  public byte value() {return value;}
  
  @Override
  public boolean isFixed() {
    return true;
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
  public void to(ByteBuffer bb) {
    bb.put(value);
  }

  @Override
  public CBByte from(ByteBuffer bb) {
    return new CBByte(bb.get());
  }

  @Override
  public int compareTo(BComparable<Byte> other) {
    return this.value - other.get();
  }

  @Override
  public boolean equals(Object other) {
    if(other instanceof CBByte)
      return this.value == ((CBByte)other).value;
    return false;
  }

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
                         ByteBuffer bb2) {
    return bb1.get(offset1) - bb2.get(offset2);
  }
  
  @Override
  public int hashCode(){return (int) value;}
  
  @Override
  public String toString(){
    return ""+value;
  }

}

