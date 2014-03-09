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
package com.mellowtech.core.collections;

import java.nio.ByteBuffer;

import com.mellowtech.core.bytestorable.ByteStorable;



/**
 * A Class containing a pointer to a physical location of bytes and a pointer
 * corresponding to a given class name. Objects of this type are used by
 * ObjectManager.
 * 
 * @author rickard.coster@asimus.se
 * @see ObjectManager
 */
public class PointerClassId extends ByteStorable {
  int classId = -1;
  int pointer = -1;

  /**
   * Creates a new <code>PointerClassId</code> instance.
   * 
   */
  public PointerClassId() {
  }

  /**
   * Creates a new <code>PointerClassId</code> instance.
   * 
   * @param pointer
   *          a pointer to a number of bytes
   * @param classid
   *          a pointer to an id corresponing to a class name
   */
  public PointerClassId(int pointer, int classid) {
    this.pointer = pointer;
    this.classId = classid;
  }

  /**
   * Get the unique class identifer
   * 
   * @return class identifier
   */
  public int getClassId() {
    return classId;
  }

  /**
   * Get th pointer to a location of bytes.
   * 
   * @return the pointer
   */
  public int getPointer() {
    return pointer;
  }

  /**
   * Set the class identifier
   * 
   * @param classId
   *          the class identifer
   */
  public void setClassId(int classId) {
    this.classId = classId;
  }

  public int byteSize() {
    return 8;
  }

  public int byteSize(ByteBuffer bb) {
    return 8;
  }

  public ByteStorable fromBytes(ByteBuffer bb) {
    int ptr = bb.getInt();
    int cid = bb.getInt();
    return new PointerClassId(ptr, cid);
  }

  public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
    return fromBytes(bb);
  }

  public void toBytes(ByteBuffer bb) {
    bb.putInt(pointer);
    bb.putInt(classId);
  }

  public String toString() {
    return "ptr=" + pointer + "\tclassid=" + classId;
  }
}
