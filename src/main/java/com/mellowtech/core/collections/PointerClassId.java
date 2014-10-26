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
import com.mellowtech.core.collections.PointerClassId.Pointer;



/**
 * A Class containing a pointer to a physical location of bytes and a pointer
 * corresponding to a given class name. Objects of this type are used by
 * ObjectManager.
 * 
 * @author rickard.coster@asimus.se
 * @see ObjectManager
 */
public class PointerClassId extends ByteStorable <PointerClassId.Pointer>{

  public class Pointer {
    short classId = -1;
    byte[] bytes;
    
    public String toString() {
      return "classid=" + classId;
    }
  }
  
  private Pointer p;

  /**
   * Creates a new <code>PointerClassId</code> instance.
   * 
   */
  public PointerClassId() {
    p = new Pointer();
  }

  /**
   * Creates a new <code>PointerClassId</code> instance.
   * 
   * @param pointer
   *          a pointer to a number of bytes
   * @param classid
   *          a pointer to an id corresponing to a class name
   */
  public PointerClassId(short classid, byte[] b) {
    this();
    this.p.bytes = b;
    this.p.classId = classid;
  }

  /**
   * Get the unique class identifer
   * 
   * @return class identifier
   */
  public short getClassId() {
    return p.classId;
  }

  /**
   * Set the class identifier
   * 
   * @param classId
   *          the class identifer
   */
  public void setClassId(short classId) {
    p.classId = classId;
  }

  public String toString() {
    return p.toString();
  }
  
  @Override
  public Pointer get() {
    return p;
  }

  @Override
  public void set(Pointer obj) {
    p = obj;
  }
  
  @Override
  public int byteSize() {
    return 4 + 2 + p.bytes.length;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return ByteStorable.getSizeFour(bb);
  }

  @Override
  public void toBytes(ByteBuffer bb) {
    bb.putInt(byteSize());
    bb.putShort(p.classId);
    bb.put(p.bytes);
  }

  @Override
  public ByteStorable <Pointer> fromBytes(ByteBuffer bb, boolean doNew) {
    PointerClassId  toRet = doNew ? new PointerClassId() : this;
    int size = bb.getInt(); //size
    toRet.p.classId = bb.getShort();
    toRet.p.bytes = new byte[size - 6];
    bb.get(toRet.p.bytes);
    return toRet;
  }
}
