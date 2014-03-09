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

/**
 * Date: 2013-04-17
 * Time: 17:59
 *
 * @author Martin Svensson
 */
public class CBSortedMapTest {

  @Test
  public void test(){
    CBSortedMap <String, Integer> i1 = new CBSortedMap();
    i1.put("one", 1);
    i1.put("two", 2);
    CBSortedMap i2 = (CBSortedMap) i1.deepCopy();
    Assert.assertTrue(i1.get("one").equals(i2.get("one")));
    Assert.assertTrue(i2.get("two").equals(i2.get("two")));

    i1.clear();
    Assert.assertTrue(i1.size() == 0);
    i2 = (CBSortedMap <String, Integer>) i1.deepCopy();
    Assert.assertTrue(i2.size() == 0);
  }

  @Test(expected = ByteStorableException.class)
  public void insertUnsupportedType(){
    CBSortedMap <CBInt, String> i1 = new CBSortedMap<>();
    i1.put(new CBInt(1), "hej");
    i1.toBytes();
  }
}
