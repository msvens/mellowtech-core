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

package org.mellowtech.core.collections;


import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mellowtech.core.codec.IntCodec;
import org.mellowtech.core.codec.StringCodec;
import org.mellowtech.core.collections.impl.KeyValueCodec;

import java.nio.ByteBuffer;

/**
 * @author msvens
 *
 */
@DisplayName("KeyValue should")
class KeyValueTest {

  @Test
  void testEquals(){
    KeyValue <String, Integer> kv1 = new KeyValue <> ("test", 1);
    KeyValue <String, Integer> kv2 = new KeyValue <> ("test", 1);
    assertEquals(kv1, kv2);
  }

  @Test
  void testEqualsDifferentValue(){
    KeyValue <String, Integer> kv1 = new KeyValue <> ("test", 1);
    KeyValue <String, Integer> kv2 = new KeyValue <> ("test", 2);
    assertEquals(kv1, kv2);
  }

  @Test
  void testByteCompare(){
    KeyValueCodec<String,Integer> kvCodec = new KeyValueCodec<>(new StringCodec(), new IntCodec());
    KeyValue <String, Integer> kv1 = new KeyValue <> ("test", 1);
    KeyValue <String, Integer> kv2 = new KeyValue <> ("test", 1);

    ByteBuffer bb1 = kvCodec.to(kv1);
    ByteBuffer bb2 = kvCodec.to(kv2);
    assertEquals(0,kvCodec.byteCompare(0, bb1, 0, bb2));
  }

  @Test
  void testByteCopy(){
    KeyValueCodec<String,Integer> kvCodec = new KeyValueCodec<>(new StringCodec(), new IntCodec());
    KeyValue <String, Integer> kv1 = new KeyValue <> ("test", 1);
    KeyValue <String, Integer> kv2 = kvCodec.deepCopy(kv1);
    assertEquals(kv1.getKey(), kv2.getKey());
    assertEquals(kv1.getValue(), kv2.getValue());
  }

  @Test
  void testNullKey(){
    assertThrows(NullPointerException.class, () -> {
      KeyValueCodec<String, Integer> kvCodec = new KeyValueCodec<>(new StringCodec(), new IntCodec());
      KeyValue<String, Integer> kv1 = new KeyValue<>(null, 1);
      kvCodec.to(kv1);
    });
  }

  @Test
  void testNullValue(){
    assertThrows(NullPointerException.class, () -> {
      KeyValueCodec<String, Integer> kvCodec = new KeyValueCodec<>(new StringCodec(), new IntCodec());
      KeyValue<String, Integer> kv1 = new KeyValue<>("test", null);
      kvCodec.to(kv1);
    });
  }

}
