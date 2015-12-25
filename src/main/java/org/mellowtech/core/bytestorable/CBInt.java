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
 * Wraps an int value as a ByteStorable
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class CBInt implements BComparable<Integer,CBInt> {
  
  private final int value;

  public CBInt() {value = 0;}

  public CBInt(int value) {this.value = value;}
  
  public CBInt(Integer value) {this.value = value;}
  
  @Override
  public CBInt create(Integer value) {return new CBInt(value);}
  
  @Override
  public Integer get() {return Integer.valueOf(value);}
  
  public int value(){return value;}

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
    bb.putInt(value);
  }

  @Override
  public CBInt from(ByteBuffer bb) {
    return new CBInt(bb.getInt());
  }

  @Override
  public int compareTo(CBInt other) {
    return Integer.compare(value, other.value);
  }

  @Override
  public boolean equals(Object other) {
    if(other instanceof CBInt)
      return value == ((CBInt)other).value;
    return false;
  }
  
  @Override
  public int hashCode(){return value;}
  
  @Override
  public String toString(){return ""+value;}

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
      ByteBuffer bb2) {
    return bb1.getInt(offset1) - bb2.getInt(offset2);
  }

}
