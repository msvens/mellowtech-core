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

/**
 * @author msvens
 * @since 4.0.0
 */
public class IntCodecTest extends CodecCompareTemplate<Integer> {

  @Override
  public Integer val(int idx) {
    return idx == 0 ? 0 : 1;
  }

  @Override
  public int size(int idx) {
    return 4;
  }

  @Override
  public BCodec<Integer> codec() {
    return new IntCodec();
  }
}
