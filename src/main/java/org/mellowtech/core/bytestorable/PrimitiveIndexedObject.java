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

//TODO: Add Type informaiton
/**
 * ByteStorable that can store any of our primitive type. Different from PrimitiveObject
 * this class also stores an index value. Allows for storing null values
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 * @see PrimitiveType
 *
 */
public class PrimitiveIndexedObject extends BStorableImp <PrimitiveIndexedObject.Entry>{

  /**
   * Entry containing index, object and type
   */
  public static class Entry {
    public int index = -1;
    @SuppressWarnings("rawtypes")
    public BStorable primObject = null;
    public PrimitiveType objectType;
  }

  /**
   * Initialize with an empty entry. Useful when using
   * as a template instance
   */
  public PrimitiveIndexedObject(){super(new Entry());}

  /**
   * Initialize with a value and index
   * @param obj the value
   * @param index the indx
   */
  public PrimitiveIndexedObject(Object obj, int index){
    this();
    set(obj, index);
  }

  /**
   * Get the object this instance holds. Effectively calls
   * {@code return value.primObject != null ? value.primObject.get() : null}
   * @return value or null
   */
  public Object getPrimitiveObject(){
    return value.primObject != null ? value.primObject.get() : null;
  }

  /**
   * Get the index this object holds
   * @return index or -1 if not set
   */
  public int getIndex(){
    return value.index;
  }

  /**
   * Sets the value of this instance to null
   */
  public void setNull(){
    value.primObject = null;
  }

  /**
   * Set the value and index of this instance
   * @param o value
   * @param index index
   */
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
