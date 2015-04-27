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
 * Date: 2013-02-17
 * Time: 18:04
 *
 * @author Martin Svensson
 */
public class PrimitiveIndexedObject extends BStorableImp <PrimitiveIndexedObject.Entry, PrimitiveIndexedObject>{

  public static class Entry {
    public int index = -1;
    @SuppressWarnings("rawtypes")
    public BStorable primObject = null;
    public PrimitiveType objectType;
  }
  
  
  
  public PrimitiveIndexedObject(){super(new Entry());}

  public PrimitiveIndexedObject(Object obj, int index) throws ByteStorableException {
    this();
    set(obj, index);
  }

  public Object getPrimitiveObject(){
    return value.primObject != null ? value.primObject.get() : null;
  }

  public int getIndex(){
    return value.index;
  }

  public void setNull(){
    value.primObject = null;
  }

  public void set(Object o, int index){
    value.objectType = PrimitiveType.type(o);
    value.index = index;
    if(value.objectType == null) 
      throw new ByteStorableException("Unknown Type "+o.getClass().getName());
    if(o != null){
      value.primObject = PrimitiveType.fromType(value.objectType,o);
    }
    else
      value.primObject = null;
  }


  @Override
  public PrimitiveIndexedObject from(ByteBuffer bb) {
    PrimitiveIndexedObject toRet = new PrimitiveIndexedObject();
    CBUtil.getSize(bb, true);
    toRet.value.index = bb.getInt();
    toRet.value.objectType = PrimitiveType.fromOrdinal(bb.get());
    if(bb.get() != 1){
      toRet.value.primObject = null;
      return toRet;
    }
    toRet.value.primObject = PrimitiveType.fromType(toRet.value.objectType).from(bb);
    return toRet;
  }

  @Override
  public void to(ByteBuffer bb) {
    CBUtil.putSize(internalSize(), bb, true);
    bb.putInt(value.index);
    bb.put(value.objectType.getByte());
    if(value.primObject == null){
      bb.put((byte)0);
    }
    else{
      bb.put((byte)1);
      value.primObject.to(bb);
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
    if(value.primObject == null) return 2;
    return 6 + value.primObject.byteSize();
  }
}
