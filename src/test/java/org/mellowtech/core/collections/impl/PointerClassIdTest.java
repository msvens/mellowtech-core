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

package org.mellowtech.core.collections.impl;

import org.junit.Assert;
import org.junit.Test;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.collections.impl.PointerClassId;

/**
 * @author msvens
 *
 */
public class PointerClassIdTest {

  /**
   * 
   */
  @Test
  public void testByteCopy() {
    PointerClassId cid = new PointerClassId((short)1, new CBString("hello").to().array());
    PointerClassId cid2 = cid.deepCopy();
    Assert.assertEquals((short)1, cid2.getClassId());
    Assert.assertEquals("hello", new CBString().from(cid2.get().bytes, 0).get());

  }

  @Test(expected = NullPointerException.class)
  public void testNullBytes() {
    PointerClassId cid = new PointerClassId((short)1, null);
    cid.to();
  }

}
