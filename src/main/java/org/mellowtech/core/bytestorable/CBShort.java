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
