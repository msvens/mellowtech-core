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
