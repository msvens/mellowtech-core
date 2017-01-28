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
import java.util.Date;

/**
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 4.0.0
 */
public class DateCodec implements BCodec<Date> {


  @Override
  public boolean isFixed(){
    return true;
  }

  @Override
  public int fixedSize(){return 8;}

  @Override
  public int byteSize(Date a) {
    return 8;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return 8;
  }

  @Override
  public void to(Date value, ByteBuffer bb) {
    bb.putLong(value.getTime());
  }

  @Override
  public Date from(ByteBuffer bb) {return new Date(bb.getLong());}

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2, ByteBuffer bb2) {
    long diff = (bb1.getLong(offset1) - bb2.getLong(offset2));

    if (diff < 0L)
      return -1;

    if (diff > 0L)
      return 1;
    return 0;

  }

}
