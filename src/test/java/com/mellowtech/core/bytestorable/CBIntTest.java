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

import junit.framework.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Date: 2013-04-17
 * Time: 17:59
 *
 * @author Martin Svensson
 */
public class CBIntTest {

  @Test
  public void test(){
    CBInt int1 = new CBInt(1);
    CBInt int2 = (CBInt) int1.deepCopy();
    Assert.assertTrue(int1.equals(int2));
    Assert.assertTrue(int1.compareTo(int2) == 0);
    ByteBuffer bb = ByteBuffer.allocate(int1.byteSize()+int2.byteSize());
    int1.toBytes(bb);
    int2.toBytes(bb);
    Assert.assertTrue(int1.byteCompare(0, bb, int1.byteSize(), bb) == 0);
    CBInt int3 = new CBInt(2);
    Assert.assertFalse(int1.compareTo(int3) == 0);
  }
}
