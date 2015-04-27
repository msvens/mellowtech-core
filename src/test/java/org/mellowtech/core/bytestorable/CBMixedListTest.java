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

import java.lang.reflect.Constructor;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mellowtech.core.bytestorable.CBMixedList;

/**
 * Date: 2013-04-17
 * Time: 17:59
 *
 * @author Martin Svensson
 */
public class CBMixedListTest extends BStorableTemplate <List<Object>, CBMixedList> {

  @Before public void init(){
    CBMixedList l1 = new CBMixedList ();
    l1.add(1);
    CBMixedList l2 = new CBMixedList ();
    l2.add(1);
    
    type = CBMixedList.class;
    values = (List <Object>[]) new List[]{l1.get(),l2.get()};
    sizes = new int[]{12,12};
  }
  
  @Test
  @Override
  public void testAConstructor() throws Exception{
    Constructor <CBMixedList> c = type.getConstructor(List.class);
    c.newInstance(values[0]);
  }
}
