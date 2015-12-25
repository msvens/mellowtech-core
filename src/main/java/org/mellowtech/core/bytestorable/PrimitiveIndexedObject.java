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
