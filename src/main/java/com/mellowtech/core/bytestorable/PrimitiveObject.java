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
public class PrimitiveObject extends ByteStorable {


  private ByteStorable primObject;
  private PrimitiveType objectType;

  public PrimitiveObject(){
    primObject = null;
  }

  public PrimitiveObject(Object obj) throws ByteStorableException{
    set(obj);
  }

  public Object get(){
    return primObject != null ? primObject.get() : null;
  }

  public void setNull(){
    this.primObject = null;
  }

  public void set(Object obj){
    this.objectType = PrimitiveType.type(obj);
    if(objectType == null) throw new ByteStorableException("Unknown Object Type");
    if(obj != null && !(obj instanceof Class)){
      primObject = PrimitiveType.fromType(objectType);
      primObject.set(obj);
    }
    else
      primObject = null;
  }


  @Override
  public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
    PrimitiveObject toRet = doNew ? new PrimitiveObject() : this;
    getSize(bb);
    toRet.objectType = PrimitiveType.fromOrdinal(bb.get());
    if(bb.get() != 1){
      toRet.primObject = null;
      return toRet;
    }
    toRet.primObject = PrimitiveType.fromType(toRet.objectType);
    toRet.primObject.fromBytes(bb, false);
    return toRet;
  }

  @Override
  public void toBytes(ByteBuffer bb) {
    putSize(internalSize(), bb);
    bb.put(objectType.getByte());
    if(primObject == null){
      bb.put((byte)0);
    }
    else{
      bb.put((byte)1);
      primObject.toBytes(bb);
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
    if(primObject == null) return 2;
    return 2 + primObject.byteSize();
  }
}
