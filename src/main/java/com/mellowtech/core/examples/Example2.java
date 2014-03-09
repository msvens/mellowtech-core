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

import com.mellowtech.core.bytestorable.CBInt;
import com.mellowtech.core.bytestorable.CBMixedList;
import com.mellowtech.core.bytestorable.CBString;

import java.nio.ByteBuffer;

/**
 * Date: 2013-04-14
 * Time: 09:05
 *
 * @author Martin Svensson
 */
public class Example2 {

  public static void main(String args){
    compareStrings();

  }

  // START SNIPPET: ex2-compare
  public static void compareStrings(){
    CBString s1 = new CBString("string 1");
    CBString s2 = new CBString("string 1");
    //do a byte comparison
    System.out.println(s1.byteCompare(0, s1.toBytes(), 0, s2.toBytes()));

    //Compare in the same buffer
    ByteBuffer bb = ByteBuffer.allocate(s1.byteSize()+s2.byteSize());
    s1.toBytes(bb);
    s2.toBytes(bb);
    System.out.println(s1.byteCompare(0, bb, s1.byteSize(), bb));
  }
  //END SNIPPET: ex2-compare




}
