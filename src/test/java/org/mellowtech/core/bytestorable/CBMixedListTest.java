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
import org.mellowtech.core.bytestorable.ByteStorableException;
import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBMixedList;

/**
 * Date: 2013-04-17
 * Time: 17:59
 *
 * @author Martin Svensson
 */
public class CBMixedListTest {

  @Test
  public void test(){
    CBMixedList i1 = new CBMixedList();
    i1.add("one string");
    i1.add(new Integer(2));
    CBMixedList i2 = (CBMixedList) i1.deepCopy();
    Assert.assertTrue(i1.get(0).equals(i2.get(0)));
    Assert.assertTrue(i2.get(1).equals(i2.get(1)));

    i1.clear();
    Assert.assertTrue(i1.size() == 0);
    i2 = (CBMixedList) i1.deepCopy();
    Assert.assertTrue(i2.size() == 0);
  }

  @Test(expected = ByteStorableException.class)
  public void insertUnsupportedType(){
    CBMixedList i1 = new CBMixedList();
    i1.add(new CBInt(1));
  }
}
