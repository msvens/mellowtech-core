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

import java.nio.ByteBuffer;

/**
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 4.0.0
 */
public class BooleanCodec implements BCodec<Boolean> {

  @Override
  public boolean isFixed(){
    return true;
  }

  @Override
  public int fixedSize(){return 1;}

  @Override
  public int byteSize(Boolean a) {
    return 1;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return 1;
  }

  @Override
  public void to(Boolean value, ByteBuffer bb) {
    bb.put(value ? (byte) 1 : 0);
  }

  @Override
  public Boolean from(ByteBuffer bb) {
    return bb.get() == 0 ? false: true;
  }

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
                         ByteBuffer bb2) {
    return bb1.get(offset1) - bb2.get(offset2);
  }


}
