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

package com.mellowtech.core.disc.blockfile;

import java.nio.ByteBuffer;

import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.collections.mappings.BSMapping;

public class DynamicFilePointer extends ByteStorable <DynamicFilePointer> implements BSMapping<DynamicFilePointer> {
  private int blockno; // for ByteStorableBlockFile
  private int id; // for ByteStorableBlockFile
  private int recno; // for SpanningBlockFile

  // First int is either negative (- (blockno+1)) or positive (recno)
  // which means that if first int is negative, then isSpanningBlockFile is
  // false, otherwise true
  private boolean isSpanningBlockFile;

  public DynamicFilePointer() {
    blockno = 0;
    id = 0;
    recno = 0;
    isSpanningBlockFile = false;
  }

  public DynamicFilePointer(int recno) {
    this.recno = recno;
    isSpanningBlockFile = true;
  }

  public DynamicFilePointer(int blockno, int id) {
    this.blockno = blockno;
    this.id = id;
    isSpanningBlockFile = false;
  }

  public boolean isSpanningBlockFile() {
    return isSpanningBlockFile;
  }

  public int getBlockno() {
    return blockno;
  }

  public int getId() {
    return id;
  }

  public int getRecno() {
    return recno;
  }

  public String toString() {
    if (isSpanningBlockFile())
      return "sbf:" + recno;
    else
      return "blf:" + blockno + ", " + id;
  }

  @Override
  public int byteSize() {
    return isSpanningBlockFile ? 4 : 8;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    int pos = bb.position();
    int firstInt = bb.getInt();
    bb.position(pos);
    if (firstInt < 0)
      return 8;
    return 4;
  }

  @Override
  public ByteStorable <DynamicFilePointer> fromBytes(ByteBuffer bb, boolean doNew) {
    DynamicFilePointer ref = doNew ? new DynamicFilePointer() : this;
    int firstInt = bb.getInt();
    if (firstInt < 0) {
      ref.blockno = (-(firstInt + 1));
      ref.id = bb.getInt();
      ref.isSpanningBlockFile = false;
    }
    else {
      ref.recno = firstInt;
      ref.isSpanningBlockFile = true;
    }
    return ref;
  }

  @Override
  public void toBytes(ByteBuffer bb) {
    if (!isSpanningBlockFile) {
      bb.putInt(-(blockno + 1));
      bb.putInt(id);
    }
    else {
      bb.putInt(recno);
    }
  }

  @Override
  public ByteStorable <DynamicFilePointer> getTemplate() {
    return new DynamicFilePointer();
  }

  @Override
  public DynamicFilePointer getOrigTemplate() {
    return (DynamicFilePointer) getTemplate();
  }

  @Override
  public ByteStorable <DynamicFilePointer> toByteStorable(DynamicFilePointer value) {
    return value;
  }

  @Override
  public DynamicFilePointer fromByteStorable(ByteStorable bs) {
    return (DynamicFilePointer) bs;
  }

  @Override
  public int byteSize(DynamicFilePointer value) {
    return value.byteSize();
  }
}
