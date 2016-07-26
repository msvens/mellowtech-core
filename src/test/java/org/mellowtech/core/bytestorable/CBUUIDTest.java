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

import java.util.Date;
import java.util.UUID;

/**
 * Date: 2013-04-17
 * Time: 17:59
 *
 * @author Martin Svensson
 */
public class CBUUIDTest extends BComparableTemplate <UUID, CBUUID> {

  @Before public void init(){
    type = CBUUID.class;
    UUID u1 = UUID.randomUUID();
    UUID u2 = UUID.randomUUID();
    sizes = new int[]{16,16};
    values = new UUID[2];
    if(u1.compareTo(u2) < 0){
      values[0] = u1;
      values[1] = u2;
    } else {
      values[0] = u2;
      values[1] = u1;
    }
  }
  
}
