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

import org.junit.Before;
import org.mellowtech.core.bytestorable.CBBoolean;


/**
 * Date: 2013-04-17
 * Time: 17:59
 *
 * @author Martin Svensson
 */
public class CBBooleanTest extends BComparableTemplate <Boolean, CBBoolean> {

  @Before public void init(){
    type = CBBoolean.class;
    values = new Boolean[]{false,true};
    sizes = new int[]{1,1};
  }
  
  /*@Test
  public void test(){
    CBBoolean i1 = new CBBoolean(true);
    CBBoolean i2 = (CBBoolean) i1.deepCopy();
    Assert.assertTrue(i1.equals(i2));
    Assert.assertTrue(i1.compareTo(i2) == 0);
    ByteBuffer bb = ByteBuffer.allocate(i1.byteSize()+i2.byteSize());
    i1.to(bb);
    i2.to(bb);
    Assert.assertTrue(i1.byteCompare(0, bb, i1.byteSize(), bb) == 0);
    CBBoolean i3 = new CBBoolean(false);
    Assert.assertFalse(i1.compareTo(i3) == 0);
  }*/
}
