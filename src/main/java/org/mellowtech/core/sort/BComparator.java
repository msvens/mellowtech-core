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

package org.mellowtech.core.sort;

import org.mellowtech.core.codec.BCodec;

import java.nio.ByteBuffer;
import java.util.Comparator;


/**
 * @author msvens
 *
 */
public class BComparator<A> implements Comparator<Integer>{

  private final BCodec<A> template;
  private final ByteBuffer buffer;
  public BComparator(BCodec<A> template, ByteBuffer buffer){
    this.template = template;
    this.buffer = buffer;
  }
  
  @Override
  public int compare(Integer o1, Integer o2) {
    return template.byteCompare(o1, o2, buffer);
  }

}
