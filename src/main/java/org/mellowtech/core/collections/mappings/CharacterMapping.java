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
package org.mellowtech.core.collections.mappings;


import org.mellowtech.core.bytestorable.ByteComparable;
import org.mellowtech.core.bytestorable.ByteStorable;
import org.mellowtech.core.bytestorable.CBChar;

/**
 * @author Martin Svensson
 *
 */
public class CharacterMapping implements BCMapping <Character> {

  //private CBString template;

  public CharacterMapping(){
  }

  @Override
  public Character fromByteComparable(ByteComparable <Character> bc) {
    return bc.get();
  }

  @Override
  public ByteComparable <Character> toByteComparable(Character key) {
    return new CBChar(key);
  }

  @Override
  public Character fromByteStorable(ByteStorable <Character> bs) {
    return bs.get();
  }

  @Override
  public int byteSize(Character value) {
    return 2;
  }

  @Override
  public ByteStorable <Character> toByteStorable(Character value) {
    return new CBChar(value);
  }

  @Override
  public ByteComparable <Character> getTemplate() {
    return new CBChar();
  }

  @Override
  public Character getOrigTemplate() {
    return 'a';
  }

}
