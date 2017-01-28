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

import org.mellowtech.core.bytestorable.CBUtil;
import org.mellowtech.core.util.CompiledLocale;

import java.nio.ByteBuffer;
import java.util.Locale;

/**
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 4.0.0
 */
public class ByteCodec implements BCodec<Byte> {

  @Override
  public boolean isFixed(){
    return true;
  }

  @Override
  public int fixedSize(){return 1;}

  @Override
  public int byteSize(Byte a) {
    return 1;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return 1;
  }

  @Override
  public void to(Byte value, ByteBuffer bb) {
    bb.put(value);
  }

  @Override
  public Byte from(ByteBuffer bb) {return bb.get();}

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
                         ByteBuffer bb2) {
    return Byte.compare(bb1.get(offset1),bb2.get(offset2));
  }


}
