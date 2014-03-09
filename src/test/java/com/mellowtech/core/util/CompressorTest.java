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

package com.mellowtech.core.util;

import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

/**
 * Date: 2013-01-26
 * Time: 13:24
 *
 * @author Martin Svensson
 */
public class CompressorTest {

  @Test
  public void test() throws IOException {
    Map.Entry entry;
    String s = "this that hej asklhh askh  asdkj  asdkj akjd alkjkj lkajslj laskj";
    byte[] toCompress = s.getBytes();
    Compressor c = new Compressor();
    byte[] compressed = c.compress(toCompress, 0, toCompress.length);
    CompResult res = c.decompress(compressed, 0, compressed.length);
    String s1 = new String(res.getBuffer(), 0, res.getLength());
    Assert.assertEquals(s, s1);
  }
}
