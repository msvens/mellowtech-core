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

package org.mellowtech.core.util;

/**
 * Date: 2013-01-26
 * Time: 13:49
 *
 * @author Martin Svensson
 */
public class CompResult {

  public final int length;
  public final byte[] buffer;

  public CompResult(){
    this(0, null);
  }

  public CompResult(int length, byte[] buffer){
    this.length = length;
    this.buffer = buffer;
  }

  public int getLength() {
    return length;
  }

  public byte[] getBuffer(){
    return buffer;
  }
}
