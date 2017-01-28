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
 * ByteStorable that can store any of our primitive type;
 * Allows for storing null values
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 * @see PrimitiveType
 * @param <A> wrapped type
 */
public class PrimitiveObject <A> extends BStorableImp <A> {

  private PrimitiveType pt;

  /**
   * Initialize this primitive object with a null value
   */
  public PrimitiveObject(){super(null);}

  /**
   * Initialize this primitive object with a specific value
   * @param value value to set
   */
  public PrimitiveObject(A value){
    super(value);
    if(value != null) {
      pt = PrimitiveType.type(value);
    }
  }

  @Override
  public PrimitiveObject <A> from(ByteBuffer bb) {
    PrimitiveObject <A> toRet; // = (doNew ? new PrimitiveObject <T> () : this);
    //toRet.pt = this.pt;
    //getSize(bb);
    CBUtil.getSize(bb, true);
    
    PrimitiveType prim = PrimitiveType.fromOrdinal(bb.get());
    @SuppressWarnings("unchecked")
    BStorable <A> temp = (BStorable <A>) PrimitiveType.fromType(prim);
    if(bb.get() != 1){
      toRet = new PrimitiveObject <> ();
    } else {
      A value = temp.from(bb).get();
      toRet = new PrimitiveObject <> (value);
    }
    toRet.pt = prim;
    return toRet;
  }

  @Override
  public void to(ByteBuffer bb) {
    CBUtil.putSize(internalSize(), bb, true);
    bb.put(pt.getByte());
    if(get() == null){
      bb.put((byte)0);
    }
    else{
      bb.put((byte)1);
      asStorable().to(bb);
    }
  }

  @Override
  public int byteSize() {
    return CBUtil.byteSize(internalSize(), true);
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CBUtil.peekSize(bb, true);
  }

  public int internalSize(){
    if(get() == null) return 2;
    return 2 + asStorable().byteSize();
  }

  @SuppressWarnings("unchecked")
  private BStorable <A> asStorable(){
    return (BStorable<A>) PrimitiveType.fromType(pt, value);
  }
}
