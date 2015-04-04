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

package org.mellowtech.core.bytestorable;

import org.mellowtech.core.bytestorable.AutoRecord;
import org.mellowtech.core.bytestorable.BSField;
import org.mellowtech.core.bytestorable.CBRecord;

/**
 * Date: 2013-04-17
 * Time: 19:01
 *
 * @author Martin Svensson
 */
public class Record1 extends CBRecord <Record1.ARecord>{
  
  public class ARecord implements AutoRecord {

    @BSField(1) public byte b;
    @BSField(2) public short s;
    @BSField(3) public int i;
    @BSField(4) public long l;
    @BSField(5) public float f;
    @BSField(6) public double d;
    @BSField(7) public char c;
    @BSField(8) public boolean bo;
    
    public String toString(){
      return ""+b+" "+s+" "+i+" "+l+" "+f+" "+d+" "+c+" "+bo;
    }
  }
  
  public Record1(){
    super();
  }

  public Record1(byte b, short s, int i, long l, float f, double d,
                        char c, boolean bo){
    this();
    this.record.b = b;
    this.record.s = s;
    this.record.i = i;
    this.record.l = l;
    this.record.f = f;
    this.record.d = d;
    this.record.c = c;
    this.record.bo = bo;
  }

  @Override
  protected Record1.ARecord newT() {
    return new ARecord();
  }
}
