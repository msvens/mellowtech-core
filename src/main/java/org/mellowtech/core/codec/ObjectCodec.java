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
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 4.0.0
 */
public class ObjectCodec implements BCodec<Object> {

  @Override
  public int byteSize(Object o) {
    BCodec codec = Codecs.type(o);
    return CodecUtil.byteSize(1 + codec.byteSize(o), true);
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    CodecUtil.peekSize(bb, true);
    return 0;
  }

  @Override
  public Object from(ByteBuffer bb) {
    CodecUtil.getSize(bb, true);
    byte b = bb.get();
    BCodec codec = Codecs.fromByte(b);
    return codec.from(bb);
  }

  @Override
  public void to(Object o, ByteBuffer bb) {
    BCodec codec = Codecs.type(o);
    CodecUtil.putSize(1 + codec.byteSize(o), bb, true);
    bb.put(Codecs.toByte(codec));
    codec.to(o, bb);

  }
}
