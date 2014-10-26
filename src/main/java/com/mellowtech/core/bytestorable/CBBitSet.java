/*
 * Copyright (c) 2013 mellowtech.org.
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 *
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 *
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 */
package com.mellowtech.core.bytestorable;


import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * <code>CBBitSet</code> is a BitSet that is suitable for file storage since
 * it extends ByteStorable. This bitSet is wrapper for an ordinary
 * java.util.BitSet.
 * 
 * @author rickard.coster@asimus.se, msvens@gmail.com
 * @version 2.0
 * @see java.util.BitSet
 */
public class CBBitSet extends ByteStorable <BitSet> {

  private BitSet mSet;

  /**
   * Creates a new <code>CBBitSet</code> instance.
   * 
   */
  public CBBitSet() {
    mSet = new BitSet();
  }

  /**
   * Creates a new <code>CBBitSet</code> instance.
   * 
   * @param set
   *          the bitSet to use
   */
  public CBBitSet(BitSet set) {
    mSet = set;
  }

  /**
   * Set the n'th position in this bitSet.
   * 
   * @param n
   *          position to set
   * @see java.util.BitSet#set(int)
   */
  public void set(int n) {
    mSet.set(n);
  }
  
  @Override
  public void set(BitSet bs){
    this.mSet = bs;
  }
  
  @Override
  public BitSet get(){
    return this.mSet;
  }

  public int byteSize() {
    //return 4 + objectByteSize(mSet);
    return 4 + 4 + (mSet.size() / 8); //bytesize + num elements + bitSize
  }

  public int byteSize(ByteBuffer bb) {
    return ByteStorable.getSizeFour(bb);
  }

  public void toBytes(ByteBuffer bb) {
    int size = byteSize();
    bb.putInt(size);
    long[] bits = mSet.toLongArray();
    bb.putInt(bits.length);
    for(int i = 0; i < bits.length; i++)
      bb.putLong(bits[i]);
  }

  public ByteStorable <BitSet> fromBytes(ByteBuffer bb) {
    return fromBytes(bb, doNew);
  }

  public ByteStorable <BitSet> fromBytes(ByteBuffer bb, boolean doNew) {
    CBBitSet toRet = doNew ? new CBBitSet() : this;
    bb.getInt(); //byteSize
    int numLongs = bb.getInt();
    long[] words = new long[numLongs];
    for(int i = 0; i < words.length; i++)
      words[i] = bb.getLong();
    toRet.set(BitSet.valueOf(words));
    return toRet;
  }


}
