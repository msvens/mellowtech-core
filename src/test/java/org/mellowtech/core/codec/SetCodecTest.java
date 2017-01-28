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

package org.mellowtech.core.codec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author msvens
 * @since 2017-01-28
 */
public class SetCodecTest extends CodecTemplate<Set<String>> {


  @Override
  public Set<String> val(int idx) {
    Set<String> s = new HashSet<>();
    if(idx == 0){
      s.add("one");
    } else {
      s.add("two");
    }
    return s;
  }

  @Override
  public int size(int idx) {
    //4 + 4 num values + 4 byte size
    return 12;
  }

  @Override
  public BCodec<Set<String>> codec() {
    return new SetCodec<String>(new StringCodec());
  }
}
