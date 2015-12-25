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


/**
 * Date: 2013-04-17
 * Time: 17:59
 *
 * @author Martin Svensson
 */
public class CBRecordTest extends BStorableTemplate <Record.ARecord, Record>{
  
  @Before public void init(){
    type = Record.class;
    Record tmp = new Record();
    Record.ARecord r1 = tmp.newA();
    r1.f1 = 0;
    Record.ARecord r2 = tmp.newA();
    r2.f1 = 1;
    
    values = new Record.ARecord[]{r1,r2};
    
    //size: totSize + numElements (4) + index (2) + primitive value
    sizes = new int[]{14,14};
  }

  
}
