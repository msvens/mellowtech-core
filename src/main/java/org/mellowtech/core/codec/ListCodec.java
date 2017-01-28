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
import java.util.ArrayList;
import java.util.List;

/**
 * @author msvens
 * @since 2017-01-28
 */
public class ListCodec<A> implements BCodec<List<A>> {

  private final BCodec<A> codec;

  public ListCodec(BCodec<A> codec){
    this.codec = codec;
  }

  private int internalSize(List<A> value){
    int size = 4; //number of elements;
    for(A a : value){
      size += codec.byteSize(a);
    }
    return size;
  }

  @Override
  public int byteSize(List<A> value) {
    return CodecUtil.byteSize(internalSize(value), false);
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CodecUtil.peekSize(bb, false);
  }

  @Override
  public List<A> from(ByteBuffer bb) {
    CodecUtil.getSize(bb, false); //read past size
    int elements = bb.getInt();
    List<A> list = new ArrayList<A>(elements);
    for(int i = 0; i < elements; i++)
      list.add(codec.from(bb));
    return list;
  }

  @Override
  public void to(List<A> value, ByteBuffer bb) {
    CodecUtil.putSize(internalSize(value), bb,false);
    bb.putInt(value.size());
    for(A a : value)
      codec.to(a,bb);
  }
}
