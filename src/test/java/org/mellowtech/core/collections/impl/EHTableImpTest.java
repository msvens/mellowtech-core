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

import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.codec.IntCodec;
import org.mellowtech.core.codec.StringCodec;
import org.mellowtech.core.collections.BMap;
import org.mellowtech.core.collections.BMapTemplate;
import org.mellowtech.core.collections.impl.EHTableImp;
import org.omg.PortableInterceptor.INACTIVE;

import java.nio.file.Paths;

/**
 * Created by msvens on 05/11/15.
 */
public class EHTableImpTest extends BMapTemplate {

  @Override
  public String fName() {
    return "ehtableimp";
  }

  @Override
  public BMap<String, Integer> init(String fileName, int bucketSize, int maxBuckets) throws Exception{
    return new EHTableImp<>(Paths.get(fileName), new StringCodec(), new IntCodec(), false, bucketSize, maxBuckets);
  }

  @Override
  public BMap<String, Integer> reopen(String fileName) throws Exception{
    return new EHTableImp<>(Paths.get(fileName), new StringCodec(), new IntCodec(), false);
  }
}
