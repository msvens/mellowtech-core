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
 * Date: 2013-04-16
 * Time: 21:25
 *
 * @author Martin Svensson
 */
public abstract class CBRecord <T extends AutoRecord> extends ByteStorable <T> {


  protected T record;
  
  
  
  @Override
  public T get() {
    return record;
  }

  @Override
  public void set(T obj) {
    this.record = obj;
  }
  
  protected abstract T newT();
  
  

  /**
   * subclasses should always call this method
   */
  public CBRecord(){
    AutoBytes.I().parseClass(newT().getClass());
    record = newT();
  }

  @Override
  public ByteStorable <T> fromBytes(ByteBuffer bb, boolean doNew) {
    try{
      CBRecord <T> toRet =  doNew ? getClass().newInstance() : this;
      Class<? extends AutoRecord> rclazz = record.getClass();
      bb.getInt(); //size indicator
      short elements = bb.getShort();
      PrimitiveObject po = new PrimitiveObject();
      for(int i = 0; i < elements; i++){
        short index = bb.getShort();
        po.fromBytes(bb, false);
        AutoBytes.I().setField(rclazz, index, po, toRet.record);
      }
      return toRet;
    }
    catch(Exception e){
      throw new ByteStorableException("Could not instantiate new BSAuto", e);
    }
  }

  @Override
  public void toBytes(ByteBuffer bb) {
    bb.putInt(byteSize());
    Class<? extends AutoRecord> rclazz = record.getClass();
    PrimitiveObject po = new PrimitiveObject();
    int pos = bb.position();
    bb.putShort((byte) 0); //num elements;
    int numElems = 0;
    for(Integer i : AutoBytes.I().getFieldIndexes(rclazz)){
      Object toStore = AutoBytes.I().getField(rclazz, i, this.record);

      if(toStore != null){
        bb.putShort(i.shortValue());
        po.set(toStore);
        po.toBytes(bb);
        numElems++;
      }
    }
    bb.putShort(pos, (short) numElems);
  }

  @Override
  public int byteSize() {
    int size = 8; //size + num elements;
    Class<? extends AutoRecord> rclazz = record.getClass();
    PrimitiveObject po = new PrimitiveObject();
    for(Integer i : AutoBytes.I().getFieldIndexes(rclazz)){
      Object toStore = AutoBytes.I().getField(rclazz, i, this.record);
      if(toStore != null){
        size += 2; //index;
        po.set(toStore);
        size += po.byteSize();
      }
    }
    return size;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return getSize(bb);
  }
}
