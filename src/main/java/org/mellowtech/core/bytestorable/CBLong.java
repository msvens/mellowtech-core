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
 * Wraps an long value as a ByteStorable
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class CBLong extends ByteComparable <Long>{

  private long value;


  public CBLong() {}

  /**
   * Initialize with a value
   * 
   * @param value
   *          the value
   */
  public CBLong(long value) {
    this.value = value;
  }

  @Override
  public void set(Long value){
    if(value == null) throw new ByteStorableException("null not allowed");
    this.value = value;
  }

  @Override
  public Long get() {
    return value;
  }

  @Override
  public boolean isFixed(){
    return true;
  }

  /**
   * @see Long#hashCode()
   */
  @Override
  public int hashCode() {
    return (int) (value ^ (value >>> 32));
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
  public void toBytes(ByteBuffer bb) {
    bb.putLong(value);
  }

  @Override
  public ByteStorable <Long> fromBytes(ByteBuffer bb, boolean doNew) {
    if (doNew)
      return new CBLong(bb.getLong());
    value = bb.getLong();
    return this;
  }

  @Override
  public int compareTo(ByteStorable <Long> other) {
    //CBLong o = (CBLong) other;
    if (this.value > other.get())
      return 1;
    else if (this.value < other.get())
      return -1;
    return 0;

  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof CBLong)
      return compareTo((CBLong) obj) == 0;
    return false;
  }

  @Override
  public String toString() {
    return "" + value;
  }

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
