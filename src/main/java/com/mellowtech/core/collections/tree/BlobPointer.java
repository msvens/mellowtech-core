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

import com.mellowtech.core.bytestorable.ByteStorable;

import java.nio.ByteBuffer;

/**
 * Pointer to hold a file position and a number of bytes
 * Date: 2013-03-22
 * Time: 08:07
 *
 * @author Martin Svensson
 */
public class BlobPointer extends ByteStorable {

  long fPointer;
  int bSize;

  public BlobPointer(){}

  public BlobPointer(long fPointer, int bSize){
    this.fPointer = fPointer;
    this.bSize = bSize;
  }

  public long getfPointer() {
    return fPointer;
  }

  public void setfPointer(long fPointer) {
    this.fPointer = fPointer;
  }

  public int getbSize() {
    return bSize;
  }

  public void setbSize(int bSize) {
    this.bSize = bSize;
  }

  @Override
  public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
    BlobPointer toRet = doNew ? new BlobPointer(): this;
    toRet.fPointer = bb.getLong();
    toRet.bSize = bb.getInt();
    return toRet;
  }

  @Override
  public void toBytes(ByteBuffer bb) {
    bb.putLong(fPointer);
    bb.putInt(bSize);
  }

  @Override
  public int byteSize() {
    return 12;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return 12;
  }

  public String toString(){
    return fPointer+":"+bSize;
  }
}
