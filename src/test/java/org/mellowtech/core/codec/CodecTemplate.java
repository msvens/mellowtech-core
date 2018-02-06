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

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * @author msvens
 * @since 2017-01-27
 */
public abstract class CodecTemplate<A> {


  abstract A val(int idx);
  abstract int size(int idx);
  abstract BCodec<A> codec();


  @Test
  void testByteSize() throws Exception{
    //B b = newB(0);
    assertEquals(size(0),codec().byteSize(val(0)));
  }

  @Test
  void testByteSizeBuffer() throws Exception{;
    ByteBuffer bb = codec().to(val(0));
    bb.flip();
    assertEquals(size(0), codec().byteSize(bb));
  }

  @Test
  void testDeepCopy() throws Exception {
    A a = codec().deepCopy(val(0));
    assertNotNull(a);
  }

  @Test
  void testFromArray() throws Exception {
    A a = val(0);
    byte array[] = new byte[codec().byteSize(a)];
    codec().to(a, array, 0);
    codec().from(array, 0);
  }

  @Test
  void testFromBuffer() throws Exception {
    A a = val(0);
    ByteBuffer bb = codec().to(a);
    bb.flip();
    codec().from(bb);
    //assertEquals(values[0], b1.get());
  }

  @Test
  void testFromStream() throws Exception {
    A a = val(0);
    byte array[] = new byte[codec().byteSize(a)];
    codec().to(a, array, 0);
    ByteArrayInputStream stream = new ByteArrayInputStream(array);
    codec().from(stream);
    //assertEquals(values[0], b1.get());
  }

  @Test
  void testFromChannel() throws Exception {
    A a = val(0);
    byte array[] = new byte[codec().byteSize(a)];
    codec().to(a, array, 0);
    ByteArrayInputStream stream = new ByteArrayInputStream(array);
    ReadableByteChannel channel = Channels.newChannel(stream);
    codec().from(channel);
  }

  @Test
  void testTo() throws Exception {
    A a = val(0);
    ByteBuffer bb = codec().to(a);
    assertEquals(size(0), bb.capacity());
  }

  @Test
  void testToArray() throws Exception {
    A a = val(0);
    byte array[] = new byte[codec().byteSize(a)];
    codec().to(a, array, 0);
  }

  @Test
  void testToStream() throws Exception {
    A a = val(0);
    ByteArrayOutputStream bos = new ByteArrayOutputStream(codec().byteSize(a));
    codec().to(a, bos);
  }

  @Test
  void testToChannel() throws Exception {
    A a = val(0);
    ByteArrayOutputStream bos = new ByteArrayOutputStream(codec().byteSize(a));
    WritableByteChannel channel = Channels.newChannel(bos);
    codec().to(a, channel);
  }


}
