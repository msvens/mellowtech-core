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
import org.junit.Test;
import org.mellowtech.core.bytestorable.CBList;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 2013-04-17
 * Time: 17:59
 *
 * @author Martin Svensson
 */
public class CBListTest extends BStorableTemplate <List<Integer>, CBList <Integer>>{
  
  @Before public void init(){
    CBList <Integer> l1 = new CBList <> ();
    l1.add(1);
    CBList <Integer> l2 = new CBList <> ();
    l2.add(1);
    
    type = (Class<CBList<Integer>>) l1.getClass();
    values = (List<Integer>[]) new List[]{l1.get(),l2.get()};
    sizes = new int[]{13,13};
  }
  
  @Test
  @Override
  public void testAConstructor() throws Exception{
    Constructor <CBList<Integer>> c = type.getConstructor(List.class);
    c.newInstance(values[0]);
  }
}
