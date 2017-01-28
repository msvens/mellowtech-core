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

package org.mellowtech.core.codec;


import java.nio.ByteBuffer;

/**
 * @author msvens
 * @since 2017-01-28
 */
public class RecordCodec<A extends BRecord> implements BCodec<A> {

  private final ObjectCodec codec = new ObjectCodec();
  private final Class<A> template;

  public RecordCodec(Class<A> aClass){
    template = aClass;
    AutoField.I().parseClass(template);
  }

  @Override
  public int byteSize(A value) {
    return CodecUtil.byteSize(internalSize(value), true);
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CodecUtil.peekSize(bb, true);
  }

  @Override
  public A from(ByteBuffer bb) {
    try{
      A toRet = template.newInstance();
      CodecUtil.getSize(bb, true);
      short elements = bb.getShort();
      for(int i = 0; i < elements; i++){
        short index = bb.getShort();
        AutoField.I().setField(template, index, codec.from(bb), toRet);
      }
      return toRet;
    }
    catch(Exception e){
      throw new CodecException("Could not instantiate new BSAuto", e);
    }
  }

  @Override
  public void to(A value, ByteBuffer bb) {
    CodecUtil.putSize(internalSize(value), bb, true);
    int pos = bb.position();
    bb.putShort((short) 0); //num elements;
    int numElems = 0;
    for(Integer i : AutoField.I().getFieldIndexes(template)){
      Object toStore = AutoField.I().getField(template, i, value);
      if(toStore != null){
        bb.putShort(i.shortValue());
        codec.to(toStore,bb);
        numElems++;
      }
    }
    bb.putShort(pos, (short) numElems);
  }

  private int internalSize(A value){
    int size = 2; //num elements;
    for(Integer i : AutoField.I().getFieldIndexes(template)){
      Object toStore = AutoField.I().getField(template, i, value);
      if(toStore != null){
        size += 2; //index;
        size += codec.byteSize(toStore);
      }
    }
    return size;
  }
}
