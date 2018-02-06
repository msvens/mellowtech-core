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

import org.junit.jupiter.api.DisplayName;
import org.mellowtech.core.codec.IntCodec;
import org.mellowtech.core.codec.StringCodec;
import org.mellowtech.core.collections.BMap;

import java.nio.file.Path;

/**
 * Created by msvens on 05/11/15.
 */
@DisplayName("A EHBlobTable")
class EHBlobTableImpTest extends BMapTemplate {

  @Override
  String fName() {
    return "ehtableimp";
  }

  @Override
  BMap<String, Integer> init(Path fileName, int bucketSize, int maxBuckets,
                             int indexBlockSize, int valueBlockSize,
                             int maxIndexBlocks, int maxValueBlocks) throws Exception {

    return new EHBlobTableImp<>(fileName, new StringCodec(), new IntCodec(), false, bucketSize, maxBuckets);

  }

  @Override
  BMap<String, Integer> reopen(Path fileName, int bucketSize, int maxBuckets,
                               int indexBlockSize, int valueBlockSize,
                               int maxIndexBlocks, int maxValueBlocks) throws Exception {

    return new EHBlobTableImp<>(fileName, new StringCodec(), new IntCodec(), false, -1, -1);

  }

}
