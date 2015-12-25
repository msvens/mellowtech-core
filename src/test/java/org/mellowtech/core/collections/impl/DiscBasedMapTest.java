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

import java.util.*;

import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.collections.BTreeBuilder;
import org.mellowtech.core.collections.DiscMap;
import org.mellowtech.core.collections.SortedDiscMapTemplate;
import org.mellowtech.core.collections.impl.DiscBasedMap;


/**
 * @author Martin Svensson
 */
public class DiscBasedMapTest extends SortedDiscMapTemplate {

  String fName = "discBasedMap";


  @Override
  public DiscMap<String, Integer> reopen() throws Exception {
    return new DiscBasedMap<>(CBString.class, CBInt.class, absPath(fName), false, false);
  }

  @Override
  public Map<String, Integer> init() throws Exception {
    BTreeBuilder builder = new BTreeBuilder();
    builder.maxBlocks(VAL_BLKS).maxIndexBlocks(IDX_BLKS).valueBlockSize(VAL_BLK_SIZE).indexBlockSize(IDX_BLK_SIZE);
    builder.blobValues(false).indexInMemory(true).valuesInMemory(false);
    return new DiscBasedMap<>(CBString.class, CBInt.class, absPath(fName), builder);
  }
}
