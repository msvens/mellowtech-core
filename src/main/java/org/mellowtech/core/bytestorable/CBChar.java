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
 * BComparable wrapper for char
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 */
public class CBChar implements BComparable <Character> {

  private final char value;
  /**
   * Initialize to {@literal '\u0000'}
   *
   */
  public CBChar() {value = '\u0000';}

  /**
   * Initialize to value
   * @param value value to set
   */
  public CBChar(char value) {this.value = value;}

  /**
   * Initialize to value
   * @param value value to set
   */
  public CBChar(Character value) {this.value = value;}

  @Override
  public boolean isFixed(){
    return true;
  }
  
  @Override
  public Character get(){
    return value;
  }

  /**
   * Get the value as a the primitive type
   * @return char value
   */
  public char value(){return value;}

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
    bb.putChar(value);
  }

  @Override
  public CBChar from(ByteBuffer bb) {
    return new CBChar(bb.getChar());
  }

  @Override
  public int compareTo(BComparable<Character> other) {
    return Character.compare(value, other.get());
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof CBChar && value == ((CBChar) other).value;
  }

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
      ByteBuffer bb2) {
    return bb1.getChar(offset1) - bb2.getChar(offset2);
  }
  
  @Override
  public int hashCode(){
    return (int) value;
  }
  
  @Override
  public String toString(){
    return ""+value;
  }
  

}
