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
import java.util.Map;

/**
 * @author msvens
 * @since 2017-01-28
 */
public class MapCodecTest extends CodecTemplate<Map<String,String>> {


  @Override
  public Map<String, String> val(int idx) {
    Map<String,String> m = new HashMap<>();
    if(idx == 0){
      m.put("one", "one");
      return m;
    } else {
      m.put("two", "two");
    }
    return m;
  }

  @Override
  public int size(int idx) {
    //4 + 4 (values) + 4 num values + 4 byte size
    return 16;
  }

  @Override
  public BCodec<Map<String, String>> codec() {
    return new MapCodec<String,String>(new StringCodec(),new StringCodec());
  }
}
