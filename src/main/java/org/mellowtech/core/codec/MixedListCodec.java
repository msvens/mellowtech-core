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

import com.sun.org.apache.bcel.internal.classfile.Code;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 4.0.0
 */
public class MixedListCodec implements BCodec<List<Object>>{

  private int internalSize(List<Object> value){
    int size = 4; //num elements
    for(Object o : value){
      BCodec codec = Codecs.type(o);
      size += 1; //store object type
      size += codec.byteSize(o);
    }
    return size;
  }

  @Override
  public int byteSize(List<Object> value) {
    return CodecUtil.byteSize4(internalSize(value));
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CodecUtil.peekSize4(bb);
  }

  @Override
  public List<Object> from(ByteBuffer bb) {
    CodecUtil.getSize4(bb);
    int elems = bb.getInt();
    List<Object> toRet = new ArrayList(elems);
    for(int i = 0; i < elems; i++){
      BCodec c = Codecs.fromByte(bb.get());
      toRet.add(c.from(bb));
    }
    return toRet;
  }

  @Override
  public void to(List<Object> value, ByteBuffer bb) {
    CodecUtil.putSize4(bb, (buff) -> {
      buff.putInt(value.size());
      for(Object o : value) {
        BCodec c = Codecs.type(o);
        bb.put(Codecs.toByte(c));
        c.to(o, buff);
      }
    });
  }

}
