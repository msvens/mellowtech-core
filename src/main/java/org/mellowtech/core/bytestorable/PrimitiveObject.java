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
 * ByteStorable that can store any of our primitive type;
 * Allows for storing null values
 * Date: 2013-02-17
 * Time: 09:28
 *
 * @author Martin Svensson
 */
public class PrimitiveObject <T> extends BStorableImp <T, PrimitiveObject<T>> {

  private PrimitiveType pt;
  
  public PrimitiveObject(){super(null);}

  public PrimitiveObject(T value) throws ByteStorableException{
    super(value);
    if(value != null)
      pt = PrimitiveType.type(value);
  }

  @Override
  public PrimitiveObject <T> from(ByteBuffer bb) {
    PrimitiveObject <T> toRet; // = (doNew ? new PrimitiveObject <T> () : this);
    //toRet.pt = this.pt;
    //getSize(bb);
    CBUtil.getSize(bb, true);
    
    PrimitiveType prim = PrimitiveType.fromOrdinal(bb.get());
    BStorable <T,?> temp = PrimitiveType.fromType(prim);
    if(bb.get() != 1){
      toRet = new PrimitiveObject <> ();
    } else {
      T value = temp.from(bb).get();
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
  
  private BStorable <T,?> asStorable(){
    return PrimitiveType.fromType(pt, value);
  }
}
