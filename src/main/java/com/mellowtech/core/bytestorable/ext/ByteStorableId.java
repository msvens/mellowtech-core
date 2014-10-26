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
 * ByteStorable that is a tuple of an integer id value and an object.
 */
@Deprecated
public class ByteStorableId extends ByteComparable <ByteStorableId> {
  int id;
  ByteStorable <?> object;
  
  public ByteStorableId() {
    this.id = -1;
    this.object = null;
  }
  
  public ByteStorableId(int id, ByteStorable <?> object) {
    this.id = id;
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

  
  @Override
  public int byteSize() {
    int dataSize = sizeBytesNeeded(id) + object.byteSize();
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
  public ByteStorable <ByteStorableId> fromBytes(ByteBuffer bb, boolean doNew) {
    int dataSize = getSize(bb);
    ByteStorableId ibs = doNew ? new ByteStorableId() : this;
    ibs.id = getSize(bb);
    ibs.object = object.fromBytes(bb, doNew);
    return ibs;
  }

  @Override
  public void toBytes(ByteBuffer bb) {
    int dataSize = sizeBytesNeeded(id) + object.byteSize();
    putSize(dataSize, bb);
    putSize(id, bb);
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
