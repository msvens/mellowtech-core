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
public abstract class CBAuto <T extends CBAuto<T>> implements BStorable <T, T>{

  /**
   * subclasses should always call this method
   */
  public CBAuto(){
    AutoBytes.I().parseClass(getClass());
  }


  @Override
  public T from(ByteBuffer bb) {
    try{
      Class clazz = getClass();
      T toRet =  (T) clazz.newInstance();
      CBUtil.getSize(bb, true);
      short elements = bb.getShort();
      PrimitiveObject po = new PrimitiveObject();
      for(int i = 0; i < elements; i++){
        short index = bb.getShort();
        AutoBytes.I().setField(clazz, index, po.from(bb), toRet);
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
    Class clazz = getClass();
    //PrimitiveObject po = new PrimitiveObject();
    //PrimitiveObject po;
    int pos = bb.position();
    bb.putShort((byte) 0); //num elements;
    int numElems = 0;
    for(Integer i : AutoBytes.I().getFieldIndexes(clazz)){
      Object toStore = AutoBytes.I().getField(clazz, i, this);
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
  
  private int internalSize() {
    int size = 4; //size + num elements;
    Class <T> clazz = (Class<T>) getClass();
    for(Integer i : AutoBytes.I().getFieldIndexes(clazz)){
      Object toStore = AutoBytes.I().getField(clazz, i, this);
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

  @Override
  public T get(){return (T) this;}
}
