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
import java.util.BitSet;

/**
 * BStorable wrapper for BitSet
 * 
 * @author Rickard Cöster {@literal <rickard.coster@asimus.se>}, Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 * @see java.util.BitSet
 */
public class CBBitSet extends BStorableImp <BitSet, CBBitSet> {

  /**
   * Initialize this CBBitSet with an empty BitSet
   */
  public CBBitSet() { super(new BitSet());}

  /**
   * Initialize this CBBitSet with an existing BitSet
   * @param set BitSet to initialize with
   */
  public CBBitSet(BitSet set) {super(set);}

  /**
   * Set the n'th position in this bitSet.
   * 
   * @param n
   *          position to set
   * @see java.util.BitSet#set(int)
   */
  public void set(int n) {
    value.set(n);
  }

  @Override
  public int byteSize() {
    return CBUtil.byteSize(4 + (value.size() / 8), false);
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CBUtil.peekSize(bb, false);
  }

  @Override
  public void to(ByteBuffer bb) {
    CBUtil.putSize(4 + (value.size() / 8), bb, false);
    long[] bits = value.toLongArray();
    bb.putInt(bits.length);
    for(int i = 0; i < bits.length; i++)
      bb.putLong(bits[i]);
  }

  @Override
  public CBBitSet from(ByteBuffer bb) {
    CBUtil.getSize(bb, false);
    int numLongs = bb.getInt();
    long[] words = new long[numLongs];
    for(int i = 0; i < words.length; i++)
      words[i] = bb.getLong();
    return new CBBitSet((BitSet.valueOf(words)));
  }


}
