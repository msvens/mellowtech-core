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

import java.nio.ByteBuffer;

/**
 * Date: 2013-04-14
 * Time: 09:05
 *
 * @author Martin Svensson
 */
public class Example1 {

  public static void main(String args){
    serialize();
    list();

  }

  // START SNIPPET: ex1-serialize
  public static void serialize(){
    CBInt firstInt = new CBInt(1);
    ByteBuffer bb = firstInt.toBytes();
    CBInt secondInt = new CBInt();
    secondInt.fromBytes(bb, false);
    System.out.println(firstInt.get()+" "+secondInt.get());
  }
  //END SNIPPET: ex1-serialize

  // START SNIPPET: ex1-list
  public static void list(){
    CBMixedList list = new CBMixedList();
    list.add(1);
    list.add("a string");
    list.add(new Long(100));
    list.add(true);

    ByteBuffer bb = list.toBytes();
    list.clear();

    //don't create a new object
    list.fromBytes(bb, false);
    Integer first = (Integer) list.get(0);
    String second = (String) list.get(1);
    Long third = (Long) list.get(2);
    Boolean b = (Boolean) list.get(3);

  }
  //END SNIPPET: ex1-list




}
