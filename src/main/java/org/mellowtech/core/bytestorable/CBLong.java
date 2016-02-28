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
 * BComparable wrapper for long
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 */
public class CBLong implements BComparable <Long,CBLong>{
  
  private final long value;

  /**
   * Initialize to 0
   */
  public CBLong() {value = 0L;}

  /**
   * Initialize to long
   * @param value long to set
   */
  public CBLong(long value) {this.value = value;}

  /**
   * Initialize to long
   * @param value Long to set
   */
  public CBLong(Long value) {this.value = value;}
  
  @Override
  public CBLong create(Long value) {return new CBLong(value);}

  @Override
  public Long get(){return value;}

  /**
   * Get the primitive long value
   * @return value
   */
  public long value(){return value;}
  
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
    bb.putLong(value);
  }

  @Override
  public CBLong from(ByteBuffer bb) {
    return new CBLong(bb.getLong());
  }

  @Override
  public int compareTo(CBLong other) {
    return Long.compare(value, other.value);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof CBLong && compareTo((CBLong) other) == 0;
  }
  
  @Override
  public int hashCode(){
    return (int)(value ^ (value >>> 32));
  }
  
  @Override
  public String toString(){return ""+value;}

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
      ByteBuffer bb2) {
    
    long diff = (bb1.getLong(offset1) - bb2.getLong(offset2));
    
    if (diff < 0L) 
      return -1;
    
    if (diff > 0L) 
      return 1;
    return 0;
  }
}
