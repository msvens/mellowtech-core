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
import java.util.UUID;

/**
 * BComparable wrapper for Date
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 */
public class CBUUID extends BStorableImp <UUID, CBUUID> implements BComparable<UUID, CBUUID>{


  /**
   * Initialize to Null
   */
  public CBUUID() {super(null);}

  /**
   * Initialize to UUID
   * @param value date to set
   */
  public CBUUID(UUID value) {super(value);}

  /**
   * Initialize with longs
   * @param mostSig The most significant bits of the UUID
   * @param leastSig - The least significant bits of the UUID
   */
  public CBUUID(long mostSig, long leastSig){super(new UUID(mostSig,leastSig));}

  @Override
  public boolean isFixed(){
    return true;
  }

  @Override
  public int byteSize() {
    return 16;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return 16;
  }

  @Override
  public void to(ByteBuffer bb) {
    bb.putLong(value.getMostSignificantBits());
    bb.putLong(value.getLeastSignificantBits());
  }

  @Override
  public CBUUID from(ByteBuffer bb) {
    return new CBUUID(bb.getLong(), bb.getLong());
  }

  @Override
  public int compareTo(CBUUID other) {
    return value.compareTo(other.value);
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof CBUUID)
      return compareTo((CBUUID) obj) == 0;
    return false;
  }

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
      ByteBuffer bb2) {
    int cmp = Long.compare(bb1.getLong(offset1), bb2.getLong(offset2));
    return cmp != 0 ? cmp : Long.compare(bb1.getLong(offset1+8), bb2.getLong(offset2+8));
  }
}
