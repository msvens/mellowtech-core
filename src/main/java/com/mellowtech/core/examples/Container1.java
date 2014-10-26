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

package com.mellowtech.core.examples;

import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.bytestorable.CBInt;
import com.mellowtech.core.bytestorable.CBString;

import java.nio.ByteBuffer;

/**
 * Date: 2013-04-14
 * Time: 12:08
 *
 * @author Martin Svensson
 */
//START SNIPPET: c1-class
public class Container1 extends ByteStorable <Container1> {

  private CBInt f1 = new CBInt();
  private CBString f2 = new CBString();

  public Container1(){;}

  public Container1(Integer field1, String field2){
    f1.set(field1);
    f2.set(field2);
  }

  @Override
  public ByteStorable <Container1> fromBytes(ByteBuffer bb, boolean doNew) {
    Container1 toRet = doNew ? new Container1() : this;
    bb.getInt(); //read past size indicator
    toRet.f1.fromBytes(bb, false); //no need to create a new object
    toRet.f2.fromBytes(bb, false); //no need to create a new object
    return toRet;
  }

  @Override
  public void toBytes(ByteBuffer bb) {
    bb.putInt(byteSize()); //write size
    f1.toBytes(bb);
    f2.toBytes(bb);
  }

  @Override
  public int byteSize() {
    int size = 4; //size indicator
    size += f1.byteSize();
    size += f2.byteSize();
    return size;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return getSizeFour(bb); //read size indicator without moving pos in bb
  }
}
//END SNIPPET: c1-class