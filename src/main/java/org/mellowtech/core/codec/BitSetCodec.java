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
import java.util.BitSet;

/**
 * @author msvens
 * @since 2017-01-28
 */
public class BitSetCodec implements BCodec<BitSet> {


  @Override
  public int byteSize(BitSet value) {
    return CodecUtil.byteSize(4 + (value.size() / 8), false);
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CodecUtil.peekSize(bb, false);
  }

  @Override
  public BitSet from(ByteBuffer bb) {
    CodecUtil.getSize(bb, false);
    int numLongs = bb.getInt();
    long[] words = new long[numLongs];
    for(int i = 0; i < words.length; i++)
      words[i] = bb.getLong();
    return BitSet.valueOf(words);
  }

  @Override
  public void to(BitSet value, ByteBuffer bb) {
    CodecUtil.putSize(4 + (value.size() / 8), bb, false);
    long[] bits = value.toLongArray();
    bb.putInt(bits.length);
    for(int i = 0; i < bits.length; i++)
      bb.putLong(bits[i]);
  }
}
