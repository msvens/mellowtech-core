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
package com.mellowtech.core.collections.tree;

import java.nio.ByteBuffer;

import com.mellowtech.core.bytestorable.ByteStorable;



/**
 * A Minimal BPlusTree index consists of (in each block) a number of separators
 * each with a left a right pointer. For example:<br>
 * 0 martin 1 rickard 2<br>
 * meaning that all values less than martin is found in block 0 and values equal
 * or larger than martin but smaller than rickard is found in block 1 and values
 * equal or larger than rickard is found in block 2. It is easy to see that we
 * only need number of keys+1 pointers in each block. Therefore the BTreeKey
 * only has a left pointer (or node) and in each BTree block we store one
 * additional pointer to represent the right most pointer in a block.
 * 
 * @author Martin Svensson
 */
public class BTreeKey extends ByteStorable {
  /**
   * The Key or separator.
   */
  public ByteStorable key;
  /**
   * A pointer to the block containg the keys that are smaller than this key. At
   * the lowest level in the index this points to the block number within a
   * key/value file.
   */
  public int leftNode;

  /**
   * Creates an empty BTreeKey, needed for the CBytable methods.
   * 
   */
  public BTreeKey() {
    this(null, -1);
  }

  /**
   * Creates a new <code>BTreeKey</code> instance.
   * 
   * @param key
   *          the separator
   * @param left
   *          the blocknumber to the left child
   */
  public BTreeKey(ByteStorable key, int left) {
    this.key = key;
    this.leftNode = left;
  }

  public int byteSize() {
    return key.byteSize() + 4;
  }

  public int byteSize(ByteBuffer bb) {
    return key.byteSize(bb) + 4;
  }

  public void toBytes(ByteBuffer bb) {
    key.toBytes(bb);
    bb.putInt(leftNode);
  }

  public ByteStorable fromBytes(ByteBuffer bb) {
    BTreeKey tmp = new BTreeKey();
    tmp.key = key.fromBytes(bb);
    tmp.leftNode = bb.getInt();
    return tmp;
  }

  public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
    return fromBytes(bb);
  }

  public int compareTo(Object o) {
    BTreeKey toCompare = (BTreeKey) o;
    return key.compareTo(toCompare.key);
  }

  public String toString() {
    return leftNode + ":" + key;
  }
}
