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
 * BComparable wrapper for booleans
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 */

public class CBBoolean implements BComparable <Boolean> {

  private final boolean value;
  
  /**
   * Initialize this CBBoolean to false
   */
  public CBBoolean() {value = false;}

  /**
   * Initialize to true or false
   *
   * @param value the value
   */
  public CBBoolean(boolean value) {this.value = value;}

  /**
   * Initialize to true or false
   * @param value the value
   */
  public CBBoolean(Boolean value) {this.value = value;}

  /**
   * Initialize to true or false
   * @param b if b is zero initialize to false otherwise true
   */
  public CBBoolean(byte b){this(b == 0 ? false : true);}
  
  @Override
  public CBBoolean create(Boolean value) {return new CBBoolean(value);}

  // ***********GET/SET**************
  @Override
  public Boolean get(){
    return Boolean.valueOf(value);
  }
  
  public boolean value(){
    return value;
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
  public void to(ByteBuffer bb) {
    bb.put(value ? (byte) 1 : 0);
  }

  @Override
  public CBBoolean from(ByteBuffer bb) {
    return new CBBoolean(bb.get());
  }

  @Override
  public int compareTo(BComparable<Boolean> other) {
    return Boolean.compare(value, other.get());
  }

  @Override
  public boolean equals(Object other) {
    if(other instanceof CBBoolean){
      return compareTo((CBBoolean)other) == 0;
    }
    return false;
  }

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
                         ByteBuffer bb2) {
    return bb1.get(offset1) - bb2.get(offset2);
  }
  
  @Override
  public String toString(){
    return ""+value;
  }

}

