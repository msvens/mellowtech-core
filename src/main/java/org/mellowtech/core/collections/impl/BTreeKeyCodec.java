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
package org.mellowtech.core.collections.impl;

import org.mellowtech.core.codec.CodecUtil;
import org.mellowtech.core.codec.BCodec;

import java.nio.ByteBuffer;


/**
 * A Minimal BPlusTree index consists of (in each block) a number of separators
 * each with a left a right pointer. For example:<br>
 * 0 martin 1 rickard 2<br>
 * meaning that all values less than martin is found in block 0 and values equal
 * or larger than martin but smaller than rickard is found in block 1 and values
 * equal or larger than rickard is found in block 2. It is easy to see that we
 * only need number of keys+1 pointers in each block. Therefore the BTreeKeyCodec
 * only has a left pointer (or node) and in each BTree block we store one
 * additional pointer to represent the right most pointer in a block.
 * 
 * @author Martin Svensson
 */
public class BTreeKeyCodec<A> implements BCodec<BTreeKey<A>> {

  BCodec<A> codec;

  /**
   * Creates an empty BTreeKeyCodec, needed for the CBytable methods.
   * @param codec key codec
   * 
   */
  public BTreeKeyCodec(BCodec<A> codec) {
    this.codec = codec;
  }

  @Override
  public int byteSize(BTreeKey<A> key) {
    return CodecUtil.byteSize(4 + codec.byteSize(key.key), true);
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CodecUtil.peekSize(bb,true);
  }

  @Override
  public BTreeKey<A> from(ByteBuffer bb) {
    CodecUtil.getSize(bb, true);
    A a = codec.from(bb);
    int leftNode = bb.getInt();
    return new BTreeKey<A>(a,leftNode);
  }

  @Override
  public void to(BTreeKey<A> key, ByteBuffer bb) {
    CodecUtil.putSize(4 + codec.byteSize(key.key), bb, true);
    codec.to(key.key,bb);
    bb.putInt(key.leftNode);
  }
}

class BTreeKey<A> implements Comparable<BTreeKey<A>> {

  /**
   * The Key or separator.
   */
  public A key;
  /**
   * A pointer to the block containg the keys that are smaller than this key. At
   * the lowest level in the index this points to the block number within a
   * key/value file.
   */
  public int leftNode;


  public BTreeKey(A k, int l){key = k; leftNode = l;}

  @Override
  public int compareTo(BTreeKey<A> other) {
    return ((Comparable<? super A>)key).compareTo(other.key);
  }


  public String toString(){return leftNode +": "+key;}


}