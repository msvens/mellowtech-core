/*
 * Copyright 2015 mellowtech.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mellowtech.core.util;

import junit.framework.Assert;

import org.junit.Test;
import org.mellowtech.core.util.CompResult;
import org.mellowtech.core.util.Compressor;

import java.io.IOException;
import java.util.Map;

/**
 * Date: 2013-01-26
 * Time: 13:24
 *
 * @author Martin Svensson
 */
@Deprecated
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
