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

import java.util.ArrayList;
import java.util.List;

/**
 * @author msvens
 * @since 2017-01-29
 */
public class MixedListCodecTest extends CodecTemplate<List<Object>> {
  @Override
  public List<Object> val(int idx) {
    List<Object> l = new ArrayList<Object>(2);
    if(idx == 0){
      l.add(1);
      l.add("one");
    } else {
      l.add(2);
      l.add("two");
    }
    return l;
  }

  @Override
  public int size(int idx) {
    return 18;
  }

  @Override
  public BCodec<List<Object>> codec() {
    return new MixedListCodec();
  }
}
