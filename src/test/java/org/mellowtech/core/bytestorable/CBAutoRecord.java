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

/**
 * Date: 2013-04-17
 * Time: 19:01
 *
 * @author Martin Svensson
 */
public class CBAutoRecord extends  CBAuto <CBAutoRecord>{

  @BSField(1) public Integer f1;


  public CBAutoRecord(){
    super();
  }

  public CBAutoRecord(CBAutoRecord record){
    super();
    f1 = record.f1;
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null || !(obj instanceof CBAutoRecord)) return false;
    CBAutoRecord o = (CBAutoRecord) obj;
    if(o.f1 == null && this.f1 == null) return true;
    if(o.f1 == null || this.f1 == null) return false;
    return o.f1.equals(this.f1);
  }
}
