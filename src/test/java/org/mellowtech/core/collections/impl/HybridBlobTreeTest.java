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
import org.mellowtech.core.collections.BTree;
import org.mellowtech.core.collections.BTreeTemplate;
import org.mellowtech.core.io.RecordFileBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author msvens
 * @since 02/07/16
 */
public class HybridBlobTreeTest extends BTreeTemplate {


  @Override
  public String fName() {
    return "hybridblobtree";
  }

  static Path getDir(String fName){
    return Paths.get(fName).getParent();
  }

  @Override
  public BTree<String, CBString, Integer, CBInt> init(String fileName, int valueBlockSize,
                                                      int indexBlockSize, int maxValueBlocks,
                                                      int maxIndexBlocks) throws Exception {

    RecordFileBuilder builder = new RecordFileBuilder().mem().
        blockSize(valueBlockSize).maxBlocks(maxValueBlocks);

    return new HybridBlobTree<>(getDir(fileName), fName(), CBString.class, CBInt.class, builder);

  }

  @Override
  public BTree<String, CBString, Integer, CBInt> reopen(String fileName,int valueBlockSize,
                                                        int indexBlockSize, int maxValueBlocks,
                                                        int maxIndexBlocks) throws Exception {

    RecordFileBuilder builder = new RecordFileBuilder().mem().
        blockSize(valueBlockSize).maxBlocks(maxValueBlocks);

    return new HybridBlobTree<>(getDir(fileName), fName(), CBString.class, CBInt.class, builder);

  }
}
