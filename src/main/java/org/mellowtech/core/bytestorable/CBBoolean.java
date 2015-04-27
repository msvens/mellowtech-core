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

public class CBBoolean implements BComparable <Boolean, CBBoolean> {

  private final boolean value;
  
  /**
   * Default constructor is needed to create a new byte from a byte buffer.
   */
  public CBBoolean() {value = false;}

  /**
   * Initialize with a value
   *
   * @param value the value
   */
  public CBBoolean(boolean value) {this.value = value;}
  
  public CBBoolean(Boolean value) {this.value = value;}
  

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
  public int compareTo(CBBoolean other) {
    return Boolean.compare(value, other.value);
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

