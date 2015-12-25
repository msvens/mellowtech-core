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
 * Date: 2013-04-16
 * Time: 21:25
 *
 * @author Martin Svensson
 */
public abstract class CBRecord <A extends AutoRecord,
  B extends CBRecord<A,B>> extends BStorableImp <A, B> {
  
  protected abstract A newA();
  
  /**
   * subclasses should always call this method
   */
  public CBRecord(){
    super(null);
    AutoBytes.I().parseClass(newA().getClass());
    value = newA();
  }
  
  public CBRecord(A value){
    super(value);
    AutoBytes.I().parseClass(value.getClass());
  }

  @Override
  public B from(ByteBuffer bb) {
    try{
      B toRet =  (B) getClass().newInstance();
      Class<? extends AutoRecord> rclazz = value.getClass();
      CBUtil.getSize(bb, true);
      short elements = bb.getShort();
      PrimitiveObject po = new PrimitiveObject();
      for(int i = 0; i < elements; i++){
        short index = bb.getShort();
        AutoBytes.I().setField(rclazz, index, po.from(bb), toRet.value);
      }
      return toRet;
    }
    catch(Exception e){
      throw new ByteStorableException("Could not instantiate new BSAuto", e);
    }
  }

  @Override
  public void to(ByteBuffer bb) {
    CBUtil.putSize(internalSize(), bb, true);
    Class<? extends AutoRecord> rclazz = value.getClass();
    //PrimitiveObject po = new PrimitiveObject();
    int pos = bb.position();
    bb.putShort((byte) 0); //num elements;
    int numElems = 0;
    for(Integer i : AutoBytes.I().getFieldIndexes(rclazz)){
      Object toStore = AutoBytes.I().getField(rclazz, i, this.value);
      if(toStore != null){
        bb.putShort(i.shortValue());
        new PrimitiveObject(toStore).to(bb);
        numElems++;
      }
    }
    bb.putShort(pos, (short) numElems);
  }
  
  @Override
  public int byteSize() {
    return CBUtil.byteSize(internalSize(), true);
  }

  private final int internalSize() {
    int size = 4; //num elements;
    Class<? extends AutoRecord> rclazz = value.getClass();
    //PrimitiveObject po = new PrimitiveObject();
    for(Integer i : AutoBytes.I().getFieldIndexes(rclazz)){
      Object toStore = AutoBytes.I().getField(rclazz, i, this.value);
      if(toStore != null){
        size += 2; //index;
        size += new PrimitiveObject(toStore).byteSize();
      }
    }
    return size;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CBUtil.peekSize(bb, true);
  }


}
