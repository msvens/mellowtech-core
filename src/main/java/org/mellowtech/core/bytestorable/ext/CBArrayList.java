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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.ByteStorable;
import org.mellowtech.core.bytestorable.CBString;

/**
 * A ByteStorable wrapper for an ArrayList
 * 
 * @author Rickard Coster
 * @version 1.0
 * @see java.util.ArrayList
 */
@Deprecated
public class CBArrayList extends ByteStorable {

  /**
   * The internal ArrayList.
   * 
   */
  protected ArrayList<ByteStorable> list = new ArrayList<ByteStorable>();

  /**
   * The name of the class type this array contains.
   * 
   */
  protected CBString template = null;

  /**
   * Creates a new <code>CBArrayList</code> instance.
   * 
   */
  public CBArrayList() {
  }

  /**
   * Creates a new <code>CBArrayList</code> instance.
   * 
   * @param template
   *          that indicates what type of objects that is stored
   */
  public CBArrayList(ByteStorable template) {
    this.template = new CBString(template.getClass().getName());
  }

  /**
   * Set the template
   * 
   * @param template
   *          that indicates what type of objects that is stored
   */
  public void setTemplate(ByteStorable template) {
    this.template = new CBString(template.getClass().getName());
  }

  /**
   * Get the internal ArrayList
   * 
   * @return an <code>ArrayList</code> value
   */
  public ArrayList<ByteStorable> getArrayList() {
    return list;
  }

  /** ***************MORE GETTERS/SETTERS THAT EASES ACCESS********************* */
  public ByteStorable remove(int index){
  	return list.remove(index);
  }
  
  public ByteStorable get(int index) {
    return list.get(index);
  }

  public void add(int index, ByteStorable elem) {
    list.add(index, elem);
  }

  public void add(ByteStorable elem) {
    list.add(elem);
  }

  public void clear() {
    list.clear();
  }

  public int size() {
    return list.size();
  }

  public int byteSize() {
    int size = 8 + template.byteSize();
    for (Iterator<ByteStorable> iter = list.iterator(); iter.hasNext();)
      size += iter.next().byteSize();
    return size;
  }

  public int byteSize(ByteBuffer bb) {
    int size = bb.getInt();
    bb.position(bb.position() - 4);
    return size;
  }

  public void toBytes(ByteBuffer bb) {
    bb.putInt(byteSize());
    bb.putInt(list.size());
    template.toBytes(bb);
    for (Iterator<ByteStorable> iter = list.iterator(); iter.hasNext();) {
      ByteStorable bs = iter.next();
      bs.toBytes(bb);
    }
  }

  public ByteStorable fromBytes(ByteBuffer bb) {
    return fromBytes(bb, doNew);
  }

  public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
    int size = bb.getInt();
    int count = bb.getInt();
    if (doNew) {
      CBArrayList cblist = new CBArrayList();
      cblist.template = new CBString();
      cblist.template = (CBString) cblist.template.fromBytes(bb);

      try {
        ByteStorable bs = (ByteStorable) Class.forName(
            cblist.template.toString()).newInstance();
        for (int i = 0; i < count; i++) {
          cblist.list.add(bs.fromBytes(bb, true));
        }
      }
      catch (Exception e) {
        CoreLog.L().log(Level.SEVERE, "", e);
        return null;
      }
      return cblist;
    }
    template = (CBString) template.fromBytes(bb, false);
    list.clear();
    try {
      ByteStorable bs = (ByteStorable) Class.forName(template.toString())
          .newInstance();
      for (int i = 0; i < count; i++) {
        list.add(bs.fromBytes(bb, true));
      }
    }
    catch (Exception e) {
      CoreLog.L().log(Level.SEVERE, "", e);
      return null;
    }
    return this;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    int count = 0;
    for (Iterator<ByteStorable> iter = list.iterator(); iter.hasNext();) {
      sb.append(" " + iter.next());
      count++;
    }
    return byteSize() + "\t" + count + "\t[" + sb.toString() + "]";
  }
}
