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

package org.mellowtech.core.collections.tree;

import java.nio.ByteBuffer;

import org.mellowtech.core.bytestorable.BStorableImp;

/**
 * Pointer to hold a file position and a number of bytes
 * Date: 2013-03-22
 * Time: 08:07
 *
 * @author Martin Svensson
 */
public class BlobPointer extends BStorableImp <BlobPointer.Entry, BlobPointer> {

  static class Entry {
    long fPointer;
    int bSize;
    public Entry(){}
    public Entry(long pointer, int size){fPointer = pointer; bSize = size;}
    public String toString(){return fPointer+": "+bSize;};
  }

  public BlobPointer(){
    super(new Entry());
  }

  public BlobPointer(long fPointer, int bSize){
    super(new Entry(fPointer, bSize));
  }

  public long getfPointer() {
    return value.fPointer;
  }

  public void setfPointer(long fPointer) {
    value.fPointer = fPointer;
  }

  public int getbSize() {
    return value.bSize;
  }

  public void setbSize(int bSize) {
    value.bSize = bSize;
  }

  @Override
  public BlobPointer from(ByteBuffer bb) {
    return new BlobPointer(bb.getLong(), bb.getInt());
  }

  @Override
  public void to(ByteBuffer bb) {
    bb.putLong(value.fPointer);
    bb.putInt(value.bSize);
  }

  @Override
  public int byteSize() {
    return 12;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return 12;
  }
}
