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
public class ByteArrayCodec implements BCodec<byte[]> {


  @Override
  public int byteSize(byte[] a) {
    return CodecUtil.byteSize(a.length, true);
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CodecUtil.peekSize(bb, true);
  }

  @Override
  public void to(byte[] value, ByteBuffer bb) {
    CodecUtil.putSize(value.length, bb, true);
    bb.put(value);
  }

  @Override
  public byte[] from(ByteBuffer bb) {
    int nBytes = CodecUtil.getSize(bb, true);
    byte[] tmp = new byte[nBytes];
    bb.get(tmp);
    return tmp;
  }

  @Override
  public int byteCompare(int offset1, byte[] b1, int offset2, byte[] b2) {
    int tmp, len1 = 0, len2 = 0, i = 0;

    // get Size of first array:
    tmp = b1[offset1++] & 0xFF;
    while ((tmp & 0x80) == 0) {
      len1 |= (tmp << (7 * i++));
      tmp = b1[offset1++] & 0xFF;
    }
    len1 |= ((tmp & ~(0x80)) << (7 * i));

    // get Size of second array:
    i = 0;
    tmp = b2[offset2++] & 0xFF;
    while ((tmp & 0x80) == 0) {
      len2 |= (tmp << (7 * i++));
      tmp = b2[offset2++] & 0xFF;
    }
    len2 |= ((tmp & ~(0x80)) << (7 * i));

    // now loop:
    int n = Math.min(len1, len2);
    while (n-- != 0) {
      if (b1[offset1] != b2[offset2])
        return ((int) b1[offset1] & 0xFF) - ((int) b2[offset2] & 0xFF);
      offset1++;
      offset2++;
    }
    return len1 - len2;
  }

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2, ByteBuffer bb2) {
    int tmp, tmp2, len1 = 0, len2 = 0, i = 0;

    // get Size of first array:
    tmp = bb1.get(offset1++) & 0xFF;
    while ((tmp & 0x80) == 0) {
      len1 |= (tmp << (7 * i++));
      tmp = bb1.get(offset1++) & 0xFF;
    }
    len1 |= ((tmp & ~(0x80)) << (7 * i));

    // get Size of second array:
    i = 0;
    tmp = bb2.get(offset2++) & 0xFF;
    while ((tmp & 0x80) == 0) {
      len2 |= (tmp << (7 * i++));
      tmp = bb2.get(offset2++) & 0xFF;
    }
    len2 |= ((tmp & ~(0x80)) << (7 * i));

    // now loop:
    int n = Math.min(len1, len2);
    while (n-- != 0) {
      tmp = (int) bb1.get(offset1);
      tmp2 = (int) bb2.get(offset2);
      if (tmp != tmp2)
        return (tmp & 0xFF) - (tmp2 & 0xFF);
      offset1++;
      offset2++;
    }
    return len1 - len2;
  }



}
