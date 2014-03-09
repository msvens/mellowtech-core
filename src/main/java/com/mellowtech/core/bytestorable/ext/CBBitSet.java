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
package com.mellowtech.core.bytestorable.ext;

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteStorable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.logging.Level;

/**
 * <code>CBBitSet</code> is a BitSet that is suitable for file storage since
 * it extends ByteStorable. This bitSet is wrapper for an ordinary
 * java.util.BitSet.
 * 
 * @author rickard.coster@asimus.se
 * @version 1.0
 * @see java.util.BitSet
 */
public class CBBitSet extends ByteStorable {

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
   * Get the bitset this CBBitSet contains
   * 
   * @return a <code>BitSet</code> value
   */
  public BitSet getBitSet() {
    return mSet;
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

  /**
   * Calculate a Serializable object's byte size
   * 
   * @param o
   *          the object to calculate
   * @return the byte size
   */
  protected int objectByteSize(Serializable o) {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(o);
      return bos.size();
    }
    catch (Exception e) {
      return 0;
    }
  }

  /**
   * Convert a Serializable object to a ByteBuffer
   * 
   * @param o
   *          Object to convert
   * @return a new ByteBuffer containg the serialized object
   */
  protected ByteBuffer objectToByteBuffer(Serializable o) {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(o);
      ByteBuffer bb = ByteBuffer.allocate(bos.size());
      bb.put(bos.toByteArray());
      bb.rewind();
      return bb;
    }
    catch (Exception e) {
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    }
  }

  /**
   * Convert a byte buffer to a Serializable object
   * 
   * @param bb
   *          the byte buffer to convert
   * @return a the serializable object
   */
  protected Serializable objectFromByteBuffer(ByteBuffer bb) {
    try {
      ByteArrayInputStream bis = new ByteArrayInputStream(bb.array(), bb
          .arrayOffset()
          + bb.position(), bb.limit() - bb.position());
      ObjectInputStream ois = new ObjectInputStream(bis);
      Serializable o = (Serializable) ois.readObject();
      return o;
    }
    catch (Exception e) {
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    }
  }

  public int byteSize() {
    return 4 + objectByteSize(mSet);
  }

  public int byteSize(ByteBuffer bb) {
    int pos = bb.position();
    int size = bb.getInt();
    bb.position(pos);
    return size;
  }

  public void toBytes(ByteBuffer bb) {
    int size = byteSize();
    bb.putInt(size);
    if (size != 0)
      bb.put(objectToByteBuffer(mSet));
  }

  public ByteStorable fromBytes(ByteBuffer bb) {
    return fromBytes(bb, doNew);
  }

  public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
    int size = bb.getInt();
    if (size == 0)
      return new CBBitSet();

    if (doNew) {
      CBBitSet bitset = new CBBitSet();
      bitset.mSet = (BitSet) objectFromByteBuffer(bb);
      bb.position(bb.position() + size - 4);
      return bitset;
    }
    this.mSet = (BitSet) objectFromByteBuffer(bb);
    bb.position(bb.position() + size - 4);
    return this;
  }

  public int compareTo(Object obj1) {
    return 0;
  }

  public String toString() {
    return getBitSet().toString();
  }

}
