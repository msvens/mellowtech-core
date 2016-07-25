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

import java.nio.ByteBuffer;

/**
 * BStorable wrapper for Class
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 */
@SuppressWarnings("rawtypes")
public class CBClass extends BStorableImp <Class, CBClass> implements Comparable <CBClass> {

  /**
   * Initalize to a value of null
   */
  public CBClass() {super(null);}

  /**
   * Initialize to class
   * @param c class to set
   */
  public CBClass(Class c){super(c);}

  @Override
  public int compareTo(CBClass other) {
    return value.getName().compareTo(other.value.getName());
  }

  @Override
  public CBClass from(ByteBuffer bb) {
    String tmp1 = new CBString().from(bb).get();
    try{
      return new CBClass(Class.forName(tmp1));
    }
    catch(Exception e){
      throw new RuntimeException("could not find class: "+tmp1, e);
    }
  }

  @Override
  public void to(ByteBuffer bb) {
    CBString store = new CBString(get().getName());
    store.to(bb);
  }

  @Override
  public int byteSize() {
    CBString store = new CBString(get().getName());
    return store.byteSize();
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return new CBString().byteSize(bb);
  }

}
