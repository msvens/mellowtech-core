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

import org.mellowtech.core.bytestorable.BStorableImp;
import org.mellowtech.core.bytestorable.CBUtil;


/**
 * A Class containing a pointer to a physical location of bytes and a pointer
 * corresponding to a given class name. Objects of this type are used by
 * ObjectManager.
 * 
 * @author rickard.coster@asimus.se
 * @see ObjectManager
 */
public class PointerClassId extends BStorableImp <PointerClassId.Pointer, PointerClassId>{

  public static class Pointer {
    short classId = -1;
    byte[] bytes;
    
    public Pointer(){}
    
    public Pointer(short clsId, byte[] b){
      classId = clsId;
      bytes = b;
    }
    public String toString() {
      return "classid=" + classId;
    }
  }

  /**
   * Creates a new <code>PointerClassId</code> instance.
   * 
   */
  public PointerClassId() { super(new Pointer());}

  /**
   * Creates a new <code>PointerClassId</code> instance.
   * 
   * @param classid
   *          a pointer to an id corresponing to a class name
   * @param b
   *          bytes
   */
  public PointerClassId(short classid, byte[] b) {
    super(new Pointer(classid, b));
  }

  /**
   * Get the unique class identifer
   * 
   * @return class identifier
   */
  public short getClassId() {
    return value.classId;
  }

  /**
   * Set the class identifier
   * 
   * @param classId
   *          the class identifer
   */
  public void setClassId(short classId) {
    value.classId = classId;
  }
  
  @Override
  public int byteSize() {
    return CBUtil.byteSize(internalSize(), false);
  }
  
  private int internalSize() {
    return 2 + value.bytes.length;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CBUtil.peekSize(bb, false);
  }

  @Override
  public void to(ByteBuffer bb) {
    CBUtil.putSize(internalSize(), bb, false);
    bb.putShort(value.classId);
    bb.put(value.bytes);
  }

  @Override
  public PointerClassId from(ByteBuffer bb) {
    //PointerClassId  toRet = doNew ? new PointerClassId() : this;
    int size = CBUtil.getSize(bb, false);
    short classId = bb.getShort();
    byte b[] = new byte[size - 2];
    bb.get(b);
    return new PointerClassId(classId, b);
  }
}
