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
