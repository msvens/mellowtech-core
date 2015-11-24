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
package org.mellowtech.core.collections.impl;

import java.nio.ByteBuffer;

import org.mellowtech.core.bytestorable.BComparableImp;
import org.mellowtech.core.bytestorable.BComparable;



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
public class BTreeKey <K extends BComparable <?,K>> extends BComparableImp <BTreeKey.Entry<K>, BTreeKey<K>> {
  
  static class Entry<K extends BComparable<?,K>> {
    
    public Entry(K k, int l){key = k; leftNode = l;} 
    /**
     * The Key or separator.
     */
    public K key;
    /**
     * A pointer to the block containg the keys that are smaller than this key. At
     * the lowest level in the index this points to the block number within a
     * key/value file.
     */
    public int leftNode;
    
    public String toString(){return leftNode +": "+key;}
  }

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
  public BTreeKey(K key, int left) {
    super(new Entry<K>(key, left));
  }

  public int byteSize() {
    return value.key.byteSize() + 4;
    //return CBUtil.byteSize(value.key.byteSize(), true);
  }

  public int byteSize(ByteBuffer bb) {
    return value.key.byteSize(bb) + 4;
    //return CBUtil.peekSize(bb, true);
  }

  public void to(ByteBuffer bb) {
    value.key.to(bb);
    bb.putInt(value.leftNode);
  }

  public BTreeKey<K> from(ByteBuffer bb) {
    BTreeKey <K> tmp = new BTreeKey <>();
    tmp.value.key = (K) value.key.from(bb);
    tmp.value.leftNode = bb.getInt();
    return tmp;
  }

  @Override
  public int compareTo(BTreeKey<K> t){
    return value.key.compareTo(t.get().key);
  }

}
