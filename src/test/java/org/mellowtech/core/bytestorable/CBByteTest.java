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

import junit.framework.Assert;

import org.junit.Test;
import org.mellowtech.core.bytestorable.CBByte;

import java.nio.ByteBuffer;

/**
 * Date: 2013-04-17
 * Time: 17:59
 *
 * @author Martin Svensson
 */
public class CBByteTest {

  @Test
  public void test(){
    CBByte i1 = new CBByte((byte)1);
    CBByte i2 = (CBByte) i1.deepCopy();
    Assert.assertTrue(i1.equals(i2));
    Assert.assertTrue(i1.compareTo(i2) == 0);
    ByteBuffer bb = ByteBuffer.allocate(i1.byteSize()+i2.byteSize());
    i1.toBytes(bb);
    i2.toBytes(bb);
    Assert.assertTrue(i1.byteCompare(0, bb, i1.byteSize(), bb) == 0);
    CBByte i3 = new CBByte((byte)2);
    Assert.assertFalse(i1.compareTo(i3) == 0);
  }
}
