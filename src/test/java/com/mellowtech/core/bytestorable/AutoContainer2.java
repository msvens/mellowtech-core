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

package com.mellowtech.core.bytestorable;

/**
 * Date: 2013-04-17
 * Time: 19:01
 *
 * @author Martin Svensson
 */
public class AutoContainer2 extends CBAuto{

  @BSField(index = 1) public byte b;
  @BSField(index = 2) public short s;
  @BSField(index = 3) public int i;
  @BSField(index = 4) public long l;
  @BSField(index = 5) public float f;
  @BSField(index = 6) public double d;
  @BSField(index = 7) public char c;
  @BSField(index = 8) public boolean bo;

  public AutoContainer2(){
    super();
  }

  public AutoContainer2(byte b, short s, int i, long l, float f, double d,
                        char c, boolean bo){
    this();
    this.b = b;
    this.s = s;
    this.i = i;
    this.l = l;
    this.f = f;
    this.d = d;
    this.c = c;
    this.bo = bo;
  }

  public String toString(){
    return ""+b+" "+s+" "+i+" "+l+" "+f+" "+d+" "+c+" "+bo;
  }
}
