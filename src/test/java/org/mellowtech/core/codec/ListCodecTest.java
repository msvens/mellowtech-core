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

import java.util.Arrays;
import java.util.List;

/**
 * @author msvens
 * @since 2017-01-28
 */
public class ListCodecTest extends CodecTemplate<List<Integer>> {

  @Override
  public List<Integer> val(int idx) {
    if(idx == 0)
      return Arrays.asList(1);
    else
      return Arrays.asList(1);
  }

  @Override
  public int size(int idx) {
    return 12;
  }

  @Override
  public BCodec<List<Integer>> codec() {
    return new ListCodec<Integer>(new IntCodec());
  }
}
