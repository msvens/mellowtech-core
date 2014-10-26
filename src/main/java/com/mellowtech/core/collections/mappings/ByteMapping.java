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
package com.mellowtech.core.collections.mappings;


import com.mellowtech.core.bytestorable.ByteComparable;
import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.bytestorable.CBByte;

/**
 * @author Martin Svensson
 *
 */
public class ByteMapping implements BCMapping <Byte> {

  //private CBString template;

  public ByteMapping(){
  }

  @Override
  public Byte fromByteComparable(ByteComparable <Byte> bc) {
    return bc.get();
  }

  @Override
  public ByteComparable <Byte> toByteComparable(Byte key) {
    return new CBByte(key);
  }

  @Override
  public Byte fromByteStorable(ByteStorable <Byte> bs) {
    return bs.get();
  }

  @Override
  public int byteSize(Byte value) {
    return 1;
  }

  @Override
  public ByteStorable <Byte> toByteStorable(Byte value) {
    return new CBByte(value);
  }

  @Override
  public ByteComparable <Byte> getTemplate() {
    return new CBByte();
  }

  @Override
  public Byte getOrigTemplate() {
    return new Byte((byte)0);
  }

}
