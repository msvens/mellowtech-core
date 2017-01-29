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
 * @since 2017-01-29
 */
public class StringCodec2 extends StringCodec {

  @Override
  public String from(ByteBuffer bb) {
    int length = CodecUtil.getSize2(bb);
    return UtfUtil.decode(bb, length);
  }


  @Override
  public void to(String value, ByteBuffer bb) {
    CodecUtil.putSize2(bb, buff -> {
      UtfUtil.encode(value, bb);
    });
  }


  @Override
  public int byteSize(String value) {
    return CodecUtil.byteSize2(UtfUtil.utfLength(value));
  }


  @Override
  public int byteSize(ByteBuffer bb) {
    return CodecUtil.peekSize2(bb);
  }

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
                         ByteBuffer bb2) {

    int length1 = CodecUtil.getSize2(bb1, offset1);
    int length2 = CodecUtil.getSize2(bb2, offset2);
    return UtfUtil.cmp(bb1, offset1+2, bb2, offset2+2, null, length1, length2);
  }
}
