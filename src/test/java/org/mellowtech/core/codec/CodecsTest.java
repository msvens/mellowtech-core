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
 * @since 2017-01-30
 */

import org.junit.Assert;
import org.junit.Test;

public class CodecsTest {

  @Test public void getStringCodec(){
    StringCodec codec = new StringCodec();
    Assert.assertTrue(Codecs.fromClass(String.class) instanceof StringCodec);
    Assert.assertTrue(Codecs.type(new String()) instanceof StringCodec);
  }

  @Test public void getChangeStringCodec(){
    Codecs.addMapping(String.class, new StringCodec2());
    Assert.assertTrue(Codecs.fromClass(String.class) instanceof StringCodec2);
    Assert.assertTrue(Codecs.type(new String()) instanceof StringCodec2);
    Codecs.addMapping(String.class, new StringCodec());
  }
}
