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

import org.mellowtech.core.codec.BRecord;

import java.nio.ByteBuffer;


/**
 * Template class for creating an automatically generated
 * BStorable that wraps and BRecord. Useful when you
 * need to serialize/deserialize a record rather than a
 * single value
 * <p>Example code:</p>
 * <pre style="code">
 *   public class MyContainer extends CBRecord {@literal <}MyContainer.Record, MyContainer{@literal >} {
 *     static class Record implements BRecord {
 *       {@literal @}BSField(2) public Integer f1;
 *       {@literal @}BSField(1) public String f2;
 *     }
 *
 *     {@literal @}Override
 *     protected Record newA() {
 *       return new Record();
 *     }
 *   }
 * </pre>
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.4
 */
public abstract class CBRecord <A extends BRecord> extends BStorableImp <A> {

  /**
   * Create a new value that this CBRecord holds. This method
   * is called from the default constructor and should be implemented
   * by subclasses
   * @return a new value
   */
  protected abstract A newA();
  
  /**
   * Instantiate this CBRecord. Will call newA. Subclasses
   * that override the default constructor should always call this
   */
  public CBRecord(){
    super(null);
    AutoBytes.I().parseClass(newA().getClass());
    value = newA();
  }

  /**
   * Instantiate this CBRecord with a value. If value is null a new
   * one will be created. Subclasses that override this method should
   * always call this
   * @param value record to set
   */
  public CBRecord(A value){
    super(value);
    AutoBytes.I().parseClass(value.getClass());
    if(value == null){
      this.value = newA();
    }
  }

  @Override
  public CBRecord<A> from(ByteBuffer bb) {
    try{
      CBRecord<A> toRet =  getClass().newInstance();
      Class<? extends BRecord> rclazz = value.getClass();
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
    Class<? extends BRecord> rclazz = value.getClass();
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
    Class<? extends BRecord> rclazz = value.getClass();
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
