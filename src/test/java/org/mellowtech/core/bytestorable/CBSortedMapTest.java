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
import java.util.Map;
import java.util.SortedMap;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mellowtech.core.bytestorable.ByteStorableException;
import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBSortedMap;

/**
 * Date: 2013-04-17
 * Time: 17:59
 *
 * @author Martin Svensson
 */
public class CBSortedMapTest extends BStorableTemplate<SortedMap<String,String>, CBSortedMap <String,String>>{

  @Before public void init(){
    CBSortedMap <String,String> m1 = new CBSortedMap <> ();
    m1.put("one", "one");
    CBSortedMap <String,String> m2 = new CBSortedMap <> ();
    m2.put("two", "two");
    
    type = (Class<CBSortedMap<String, String>>) m1.getClass();
    values = (SortedMap<String, String>[]) new SortedMap[]{m1.get(),m2.get()};
    
    //Size is 4 (byte size) + 4 (num elems) + 2 (types) + size of key + 1 (value exist) + size of value
    //size of key = 4
    //size of value = 4
    
    sizes = new int[]{19,19};
  }
  
  @Test
  @Override
  public void testAConstructor() throws Exception{
    Constructor <CBSortedMap<String,String>> c = type.getConstructor(SortedMap.class);
    c.newInstance(values[0]);
  }
}
