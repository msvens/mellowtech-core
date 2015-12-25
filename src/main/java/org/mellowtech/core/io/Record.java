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

package org.mellowtech.core.io;

/**
 * Date: 2013-03-11
 * Time: 12:23
 *
 * @author Martin Svensson
 */
public class Record {

  public int record;
  public byte[] data;

  public Record(){}

  public Record(int record, byte[] data){this.record = record; this.data = data;}

  public String toString(){
    return ""+record;
  }
}
