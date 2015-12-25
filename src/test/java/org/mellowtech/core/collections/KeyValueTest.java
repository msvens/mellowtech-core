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

import junit.framework.Assert;

import org.junit.Test;
import org.mellowtech.core.bytestorable.CBBitSet;
import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.collections.KeyValue;

import java.nio.ByteBuffer;

/**
 * @author msvens
 *
 */
public class KeyValueTest {

  @Test
  public void testEquals(){
    KeyValue <CBString, CBInt> kv1 = new KeyValue <> (new CBString("test"), new CBInt(1));
    KeyValue <CBString, CBInt> kv2 = new KeyValue <> (new CBString("test"), new CBInt(1));
    Assert.assertEquals(kv1, kv2);
  }

  @Test
  public void testEqualsDifferentValue(){
    KeyValue <CBString, CBInt> kv1 = new KeyValue <> (new CBString("test"), new CBInt(1));
    KeyValue <CBString, CBInt> kv2 = new KeyValue <> (new CBString("test"), new CBInt(2));
    Assert.assertEquals(kv1, kv2);
  }

  @Test
  public void testByteCompare(){
    KeyValue <CBString, CBInt> kv1 = new KeyValue <> (new CBString("test"), new CBInt(1));
    KeyValue <CBString, CBInt> kv2 = new KeyValue <> (new CBString("test"), new CBInt(1));
    ByteBuffer bb1 = kv1.to();
    ByteBuffer bb2 = kv2.to();
    KeyValue <CBString, CBInt> kv3 = new KeyValue();
    Assert.assertEquals(0,kv1.byteCompare(0, bb1, 0, bb2));
  }

  @Test
  public void testByteCopy(){
    KeyValue <CBString, CBInt> kv1 = new KeyValue <> (new CBString("test"), new CBInt(1));
    KeyValue <CBString, CBInt> kv2 = kv1.deepCopy();
    Assert.assertEquals(kv1.getKey(), kv2.getKey());
    Assert.assertEquals(kv1.getValue(), kv2.getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testNullKey(){
    KeyValue <CBString, CBInt> kv1 = new KeyValue <> (null, new CBInt(1));
    kv1.to();
  }
  @Test(expected = NullPointerException.class)
  public void testNullValue(){
    KeyValue <CBString, CBInt> kv1 = new KeyValue <> (new CBString("test"), null);
    kv1.to();
  }

}
