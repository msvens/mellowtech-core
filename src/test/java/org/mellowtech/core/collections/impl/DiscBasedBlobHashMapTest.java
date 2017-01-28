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

package org.mellowtech.core.collections.impl;

import org.mellowtech.core.codec.IntCodec;
import org.mellowtech.core.codec.StringCodec;
import org.mellowtech.core.collections.DiscMap;
import org.mellowtech.core.collections.DiscMapTemplate;

import java.util.Map;

/**
 * @author Martin Svensson
 *
 */
public class DiscBasedBlobHashMapTest extends DiscMapTemplate {


  private String fName = "discBasedHashMap";


  @Override
  public Map<String, Integer> init() throws Exception {
    return new DiscBasedHashMap(new StringCodec(), new IntCodec(),
        absPath(fName), true, false, VAL_BLK_SIZE, VAL_BLKS);
  }

  @Override
  public DiscMap<String, Integer> reopen() throws Exception {
    return new DiscBasedHashMap(new StringCodec(), new IntCodec(), absPath(fName), true, false);
  }




}
