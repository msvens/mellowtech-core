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

package com.mellowtech.core.bytestorable;

import java.nio.ByteBuffer;

/**
 * ByteStorable that can store any of our primitive type;
 * Allows for storing null values
 * Date: 2013-02-17
 * Time: 09:28
 *
 * @author Martin Svensson
 */
public class PrimitiveObject <T> extends ByteStorable <T> {

  private PrimitiveType pt;
  
  public PrimitiveObject(){
    //this(null);
  }

  public PrimitiveObject(T obj) throws ByteStorableException{
    set(obj);
  }
  
  @Override
  public void set(T obj) {
    super.set(obj);
    if(obj != null)
      pt = PrimitiveType.type(obj);
  }

  @Override
  public ByteStorable <T> fromBytes(ByteBuffer bb, boolean doNew) {
    PrimitiveObject <T> toRet = (doNew ? new PrimitiveObject <T> () : this);
    //toRet.pt = this.pt;
    getSize(bb);
    PrimitiveType prim = PrimitiveType.fromOrdinal(bb.get());
    toRet.pt = prim;
    if(bb.get() != 1){
      toRet.set(null);
      return toRet;
    }
    ByteStorable <T> obj = PrimitiveType.fromType(toRet.pt);
    obj.fromBytes(bb, false);
    toRet.set(obj.get());
    return toRet;
  }

  @Override
  public void toBytes(ByteBuffer bb) {
    putSize(internalSize(), bb);
    bb.put(pt.getByte());
    if(get() == null){
      bb.put((byte)0);
    }
    else{
      bb.put((byte)1);
      asStorable().toBytes(bb);
    }
  }

  @Override
  public int byteSize() {
    return internalSize() + sizeBytesNeeded(internalSize());
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return getSizeVariable(bb);
  }

  public int internalSize(){
    if(get() == null) return 2;
    return 2 + asStorable().byteSize();
  }
  
  private ByteStorable <T> asStorable(){
    ByteStorable <T> bs = PrimitiveType.fromType(pt);
    bs.set(get());
    return bs;
  }
}
