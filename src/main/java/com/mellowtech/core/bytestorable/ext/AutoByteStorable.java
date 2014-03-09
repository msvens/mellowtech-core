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

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteStorable;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;


/**
 * ByteStorable class that automatically reads/writes all instances of 
 * (public and not null) ByteStorable fields in the object using 
 * Java reflection. 
 * <p>
 * 
 * The intended usage is to subclass AutoByteStorable 
 * so that all (public and non-null)  fields that are instances of 
 * ByteStorable are written in toBytes() and read in fromBytes(). 
 * <p>
 * It is assumed that the ByteStorable elements are not null. Therefore, 
 * you must ensure that the fields are non-null at all times.
 * <p>
 * The following example demonstrates fictous use:
 * <p>
 * <code>
 * class MyData extends AutoByteStorable {
 *  public CBString name    = "MyName";    // OK, will be included in to/fromBytes
 *  public CBString address = "MyAddress"; // OK, will be included in to/fromBytes
 *  public CBString phone   = "MyPhone";   // OK, will be included in to/fromBytes
 *  protected int myIntValue   = 10;         // will NOT be included in to/fromBytes: not a subclass of ByteStorable
 *  private CBString myString  = "MyString"; // will NOT be included in to/fromBytes: private member
 *  protected CBString myNull  = null;       // ERROR, WILL GENERATE NullPointerException!
 * }
 * </code>
 * 
 * @author rickard.coster@asimus.se
 *
 */
@Deprecated
public class AutoByteStorable extends ByteStorable {
  
  @Override
  public int byteSize() {
    int size = 4;
    ByteStorable[] bs = getDeclaredByteStorables();
    for (int i = 0; i < bs.length; i++)
      size += bs[i].byteSize();
    return size;
  }

  
  @Override
  public int byteSize(ByteBuffer bb) {
    int position = bb.position();
    int size = bb.getInt();
    bb.position(position);
    return size;
  }
  
  @Override
  public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
    int size = bb.getInt();
    Object o = null;
    try {
      o = doNew ? getClass().newInstance() : this;
    } catch (InstantiationException e) {
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    } catch (IllegalAccessException e) {
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    }
    Field[] fields = getClass().getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      Class<?> fieldClass = fields[i].getType();
      if (ByteStorable.class.isAssignableFrom(fieldClass)) {
        ByteStorable b = null;
        try {
          b = (ByteStorable) fields[i].get(o);
          if (b == null) 
            b = (ByteStorable) fieldClass.newInstance();
          b = (ByteStorable) b.fromBytes(bb, doNew);
          fields[i].set(o, b);
        } catch (Exception e) {
          CoreLog.L().log(Level.FINE, "could not instantiate", e);
        }
      }
    }
    return (ByteStorable) o;
  }

  @Override
  public void toBytes(ByteBuffer bb) {
    ByteStorable[] bs = getDeclaredByteStorables();
    int size = 4;
    for (int i = 0; i < bs.length; i++)
      size += bs[i].byteSize();
    bb.putInt(size);
    for (int i = 0; i < bs.length; i++)
      bs[i].toBytes(bb);
  }
  
  protected ByteStorable[] getDeclaredByteStorables() {

    Field[] fields = getClass().getDeclaredFields();
    List<ByteStorable> byteStorables = new ArrayList<ByteStorable>();
    for (int i = 0; i < fields.length; i++) {
      Class<?> fieldClass = fields[i].getType();
      if (ByteStorable.class.isAssignableFrom(fieldClass)) {
        ByteStorable b = null;
        try {
          b = (ByteStorable) fields[i].get(this);
          byteStorables.add(b);
        } catch (Exception e) {
          CoreLog.L().log(Level.FINE, "illegal access", e);
        }
      }
    }
    return byteStorables.toArray(new ByteStorable[0]);
  }
}
