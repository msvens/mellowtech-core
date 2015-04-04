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
package org.mellowtech.core.bytestorable.ext;

import java.nio.ByteBuffer;

import org.mellowtech.core.bytestorable.ByteComparable;
import org.mellowtech.core.bytestorable.ByteStorable;


/**
 * ByteStorable that is a tuple of an integer ptr value and a object.
 * 
 * The object that can be inserted in a ByteStorablePtr is a ByteComparable, 
 * as opposed to a ByteStorable in ByteStorableId and ByteStorableIdPtr, since
 * this class will require that the comparison is done by means of comparing
 * the actual contents of the object. 
 */
@Deprecated
public class ByteStorablePtr extends ByteComparable {
  int ptr;
  ByteComparable object;
  
  public ByteStorablePtr() {
    this.ptr = -1;
    this.object = null;
  }
  
  public ByteStorablePtr(int id, ByteComparable object) {
    this.ptr = id;
    this.object = object;
  }

  public ByteStorable getObject() {
    return object;
  }

  public void setObject(ByteComparable object) {
    this.object = object;
  }

  public int getPtr() {
    return ptr;
  }

  public void setPtr(int ptr) {
    this.ptr = ptr;
  }

  @Override
  public int byteSize() {
    int dataSize = sizeBytesNeeded(ptr) + object.byteSize();
    return sizeBytesNeeded(dataSize) + dataSize; 
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    int position = bb.position();
    int dataSize = getSize(bb);
    bb.position(position);
    return sizeBytesNeeded(dataSize) + dataSize;
  }

  @Override
  public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
    int dataSize = getSize(bb);
    ByteStorablePtr ibs = doNew ? new ByteStorablePtr() : this;
    ibs.ptr = getSize(bb);
    ibs.object = (ByteComparable) object.fromBytes(bb, doNew);
    return ibs;
  }

  @Override
  public void toBytes(ByteBuffer bb) {
    int dataSize = sizeBytesNeeded(ptr) + object.byteSize();
    putSize(dataSize, bb);
    putSize(ptr, bb);
    object.toBytes(bb);
  }

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2, ByteBuffer bb2) {
    // set positions
    bb1.position(offset1);
    bb2.position(offset2);
    
    // get byte sizes first
    int dataSize1 = getSize(bb1);
    int dataSize2 = getSize(bb2);
    
    //  get pointers 
    int ptr1 = getSize(bb1);
    int ptr2 = getSize(bb2);
    
    // Compare using the ByteComparable's byteCompare method.
    return object.byteCompare(offset1, bb1, offset2, bb2);
  }

}
