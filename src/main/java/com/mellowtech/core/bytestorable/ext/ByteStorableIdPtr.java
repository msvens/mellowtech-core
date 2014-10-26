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

import com.mellowtech.core.bytestorable.ByteComparable;
import com.mellowtech.core.bytestorable.ByteStorable;

import java.nio.ByteBuffer;


/**
 * ByteStorable that is a triplet of an integer id value and an integer ptr 
 * and a ByteComparable object.
 * 
 */
@Deprecated
public class ByteStorableIdPtr extends ByteComparable <ByteStorableIdPtr> {
  int id;
  int ptr;
  ByteStorable <?> object;
  
  public ByteStorableIdPtr() {
    this.id = -1;
    this.ptr = -1;
    this.object = null;
  }
  
  public ByteStorableIdPtr(int id, int ptr, ByteStorable <?> object) {
    this.id = id;
    this.ptr = ptr;
    this.object = object;
  }
  
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public ByteStorable <?> getObject() {
    return object;
  }

  public void setObject(ByteStorable <?> object) {
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
    int dataSize = sizeBytesNeeded(id) + sizeBytesNeeded(ptr) + object.byteSize();
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
  public ByteStorable <ByteStorableIdPtr> fromBytes(ByteBuffer bb, boolean doNew) {
    int dataSize = getSize(bb);
    ByteStorableIdPtr ibs = doNew ? new ByteStorableIdPtr() : this;
    ibs.id = getSize(bb);
    ibs.ptr = getSize(bb);
    ibs.object = object.fromBytes(bb, doNew);
    return ibs;
  }

  @Override
  public void toBytes(ByteBuffer bb) {
    int dataSize = sizeBytesNeeded(id) + sizeBytesNeeded(ptr) + object.byteSize();
    putSize(dataSize, bb);
    putSize(id, bb);
    putSize(ptr, bb);
    object.toBytes(bb);
  }

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2, ByteBuffer bb2) {
    // set positions
    bb1.position(offset1);
    bb2.position(offset2);
    
    // get byte sizes first
    int byteSize1 = getSize(bb1);
    int byteSize2 = getSize(bb2);
    
    // get ids and compare by id
    int id1 = getSize(bb1);
    int id2 = getSize(bb2);
    
    if (id1 < id2) 
      return -1;
    else if (id1 > id2) 
      return 1;
    return 0;
  }

}
