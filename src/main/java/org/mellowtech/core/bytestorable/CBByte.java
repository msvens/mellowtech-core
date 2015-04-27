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
 * @author Martin Svensson
 */

public class CBByte implements BComparable <Byte, CBByte> {

  private final byte value;
  /**
   * Default constructor is needed to create a new byte from a byte buffer.
   */
  public CBByte() {value = (byte) 0;}

  public CBByte(byte value) {this.value = value;}
  
  public CBByte(Byte value) {this.value = value;}
  
  @Override
  public Byte get() {return Byte.valueOf(value);}

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
  public int compareTo(CBByte other) {
    return this.value - other.value;
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

