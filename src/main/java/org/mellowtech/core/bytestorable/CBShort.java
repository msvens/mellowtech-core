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
 * Wraps a short value as a ByteStorable
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class CBShort implements BComparable<Short,CBShort>{

  private final short value;
  
  public CBShort() {value = (short)0;}

  public CBShort(short value) {this.value = value;}
  
  public CBShort(Short value) {this.value = value;}
  
  @Override
  public CBShort create(Short value) {return new CBShort(value);}

  @Override
  public Short get(){
    return Short.valueOf(value);
  }
  
  public short value(){return value;}
  
  @Override
  public boolean isFixed(){
    return true;
  }

  @Override
  public int byteSize() {
    return 2;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return 2;
  }

  @Override
  public void to(ByteBuffer bb) {
    bb.putShort(value);
  }

  @Override
  public CBShort from(ByteBuffer bb) {
    return new CBShort(bb.getShort());
  }

  @Override
  public int compareTo(CBShort other) {
    return Short.compare(value, other.value);
  }

  @Override
  public boolean equals(Object other) {
    if(other instanceof CBShort)
      return value == ((CBShort)other).value;
    return false;
  }
  
  @Override
  public int hashCode(){
    return (int) value;
  }
  
  @Override
  public String toString(){return ""+value;}

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
      ByteBuffer bb2) {
    return bb1.getShort(offset1) - bb2.getShort(offset2);
  }

}
