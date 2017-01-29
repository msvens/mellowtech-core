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
import java.util.HashMap;
import java.util.Map;

/**
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 4.0.0
 */
public class MapCodec<A,B> implements BCodec<Map<A,B>> {

  private final BCodec<A> keyCodec;
  private final BCodec<B> valueCodec;

  public MapCodec(BCodec<A> keyCodec, BCodec<B> valueCodec){
    this.keyCodec = keyCodec;
    this.valueCodec = valueCodec;
  }

  private int internalSize(Map<A,B> value){
    int size = 4; //number of elements;
    for(Map.Entry<A,B> entry : value.entrySet()){
      size += keyCodec.byteSize(entry.getKey());
      size += valueCodec.byteSize(entry.getValue());
    }
    return size;
  }

  @Override
  public int byteSize(Map<A,B> value) {
    return CodecUtil.byteSize(internalSize(value), false);
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CodecUtil.peekSize(bb, false);
  }

  @Override
  public Map<A,B> from(ByteBuffer bb) {
    CodecUtil.getSize(bb, false); //read past size
    int elements = bb.getInt();
    Map<A,B> map = new HashMap<A, B>(elements);
    for(int i = 0; i < elements; i++)
      map.put(keyCodec.from(bb), valueCodec.from(bb));
    return map;
  }

  @Override
  public void to(Map<A,B> value, ByteBuffer bb) {
    CodecUtil.putSize(internalSize(value), bb,false);
    bb.putInt(value.size());
    for(Map.Entry<A,B> entry: value.entrySet()) {
      keyCodec.to(entry.getKey(), bb);
      valueCodec.to(entry.getValue(),bb);
    }
  }
}
