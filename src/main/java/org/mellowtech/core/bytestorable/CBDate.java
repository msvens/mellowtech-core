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
import java.util.Date;

/**
 * BComparable wrapper for Date
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 */
public class CBDate extends BStorableImp <Date> implements BComparable<Date>{


  /**
   * Initialize to Now
   */
  public CBDate() {super(new Date(System.currentTimeMillis()));}

  /**
   * Initialize to date
   * @param value date to set
   */
  public CBDate(Date value) {super(value);}

  /**
   * Initialize to time millis
   * @param time date to set
   */
  public CBDate(long time){super(new Date(time));}

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
    bb.putLong(value.getTime());
  }

  @Override
  public CBDate from(ByteBuffer bb) {
    return new CBDate(bb.getLong());
  }

  @Override
  public int compareTo(BComparable<Date> other) {
    return value.compareTo(other.get());
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof CBDate)
      return compareTo((CBDate) obj) == 0;
    return false;
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
