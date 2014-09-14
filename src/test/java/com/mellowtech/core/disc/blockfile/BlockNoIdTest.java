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

package com.mellowtech.core.disc.blockfile;

import com.mellowtech.core.bytestorable.CBString;
import junit.framework.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Date: 2013-01-23
 * Time: 21:55
 *
 * @author Martin Svensson
 */
@Deprecated
public class BlockNoIdTest {

  //@Test
  public void test() {

    BlockNoId block = new BlockNoId(64, new CBString());
    CBString cs[] = new CBString[10];
    cs[0] = new CBString("h* 0");
    cs[1] = new CBString("h* 7");
    cs[2] = new CBString("h* 2");
    cs[3] = new CBString("h* 3");
    cs[4] = new CBString("h* 4");
    cs[5] = new CBString("h* 9");
    cs[6] = new CBString("h* 6");
    cs[7] = new CBString("h* 1");
    cs[8] = new CBString("h* 8");
    cs[9] = new CBString("h* 5");


    for (int i = 0; i < cs.length; i++)
      block.insert(cs[i]);

    Assert.assertEquals(cs.length, block.getCount());

    for (int i = 0; i < cs.length; i++)
      Assert.assertNotNull(block.get(i));;

    Assert.assertNotNull(block.remove(cs[2]));

    Assert.assertEquals(cs.length-1, block.getCount());


    ByteBuffer bb = ByteBuffer.allocate(block.byteSize());
    block.toBytes(bb);

    bb.flip();
    BlockNoId block2 = (BlockNoId) block.fromBytes(bb);

    Assert.assertEquals(block.getCount(), block2.getCount());
  }
}
