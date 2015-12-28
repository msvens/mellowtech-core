
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

package org.mellowtech.core.bytestorable;

import junit.framework.Assert;
import org.junit.Before;

/**
 * @author msvens
 *
 */
public class CBZByteArrayTest extends BStorableTemplate <byte[], CBZByteArray> {

  @Before public void init(){
    type = CBZByteArray.class;
    byte[] b1 = new byte[]{1,2};
    byte[] b2 = new byte[]{1,3};
    values = new byte[][]{b1,b2};
    sizes = new int[]{8,8};
  }

  @Override
  public void testGet() throws Exception {
    CBZByteArray b = newB(0);
    byte[] val = b.get();
    Assert.assertEquals(values[0][0], val[0]);
    Assert.assertEquals(values[0][1], val[1]);
  }
}
