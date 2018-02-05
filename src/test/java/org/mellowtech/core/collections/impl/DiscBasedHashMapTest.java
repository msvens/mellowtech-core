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
import org.mellowtech.core.collections.DiscMap;
import org.mellowtech.core.collections.EHTableBuilder;

/**
 * @author Martin Svensson
 *
 */
@DisplayName("A DiscBasedHashMap")
class DiscBasedHashMapTest extends DiscMapTemplate {


  @Override
  DiscMap<String, Integer> init() throws Exception {
    String fName = "discBasedHashMap";
    EHTableBuilder<String,Integer> builder = new EHTableBuilder<>();
    builder.keyCodec(new StringCodec()).valueCodec(new IntCodec()).filePath(absPath(fName));
    builder.bucketSize(VAL_BLK_SIZE).maxBuckets(VAL_BLKS).blobValues(false).inMemory(false);
    return new DiscBasedHashMap<>(builder);
  }

  @Override
  DiscMap<String, Integer> reopen() throws Exception {
    return init();
    /*EHTableBuilder<String,Integer> builder = new EHTableBuilder<>();
    builder.keyCodec(new StringCodec()).valueCodec(new IntCodec()).filePath(absPath(fName));
    builder.bucketSize(VAL_BLK_SIZE).maxBuckets(VAL_BLKS).blobValues(false).inMemory(false);
    return new DiscBasedHashMap(builder);*/
  }




}
