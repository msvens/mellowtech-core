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
 * Date: 2013-02-17
 * Time: 18:04
 *
 * @author Martin Svensson
 */
public class PrimitiveIndexedObject extends ByteStorable <PrimitiveIndexedObject.Entry>{

  public class Entry {
    public int index = -1;
    public ByteStorable primObject = null;
    public PrimitiveType objectType;
  }
  
  
  
  public PrimitiveIndexedObject(){
    this.obj = new Entry();
  }

  public PrimitiveIndexedObject(Object obj, int index) throws ByteStorableException {
    set(obj, index);
  }

  public Object getPrimitiveObject(){
    return obj.primObject != null ? obj.primObject.get() : null;
  }

  public int getIndex(){
    return obj.index;
  }

  public void setNull(){
    obj.primObject = null;
  }

  public void set(Object o, int index){
    obj.objectType = PrimitiveType.type(o);
    obj.index = index;
    if(obj.primObject == null) 
      throw new ByteStorableException("Unknown Object Type "+obj.getClass().getName());
    if(o != null){
      obj.primObject = PrimitiveType.fromType(obj.objectType);
      obj.primObject.set(o);
    }
    else
      obj.primObject = null;
  }


  @Override
  public ByteStorable <Entry> fromBytes(ByteBuffer bb, boolean doNew) {
    PrimitiveIndexedObject toRet = doNew ? new PrimitiveIndexedObject() : this;
    getSize(bb); //read past length
    toRet.obj.index = bb.getInt();
    toRet.obj.objectType = PrimitiveType.fromOrdinal(bb.get());
    if(bb.get() != 1){
      toRet.obj.primObject = null;
      return toRet;
    }
    toRet.obj.primObject = PrimitiveType.fromType(toRet.obj.objectType);
    toRet.obj.primObject.fromBytes(bb, false);
    return toRet;
  }

  @Override
  public void toBytes(ByteBuffer bb) {
    putSize(internalSize(), bb);
    bb.putInt(obj.index);
    bb.put(obj.objectType.getByte());
    if(obj.primObject == null){
      bb.put((byte)0);
    }
    else{
      bb.put((byte)1);
      obj.primObject.toBytes(bb);
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
    if(obj.primObject == null) return 2;
    return 6 + obj.primObject.byteSize();
  }
}
