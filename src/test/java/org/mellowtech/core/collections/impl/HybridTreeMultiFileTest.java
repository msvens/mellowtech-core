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
import org.mellowtech.core.collections.BTree;
import org.mellowtech.core.io.RecordFileBuilder;

import java.nio.file.Path;

/**
 * @author msvens
 * @since 02/07/16
 */
public class HybridTreeMultiFileTest extends BTreeTemplate {


  public static final int MultiFileSize = 1024*1024*4;
  @Override
  public String fName() {
    return "hybridtreemultifile";
  }

  static Path getDir(Path fName){
    return fName.getParent();
  }

  @Override
  public BTree<String, Integer> init(Path fileName, int valueBlockSize,
                                                      int indexBlockSize, int maxValueBlocks,
                                                      int maxIndexBlocks) throws Exception {

    RecordFileBuilder builder = new RecordFileBuilder().multi().blockSize(valueBlockSize).
        multiFileSize(MultiFileSize);

    return new HybridTree<>(getDir(fileName), fName(), new StringCodec(), new IntCodec(), builder);
  }

  @Override
  public BTree<String, Integer> reopen(Path fileName,int valueBlockSize,
                                                        int indexBlockSize, int maxValueBlocks,
                                                        int maxIndexBlocks) throws Exception {

    RecordFileBuilder builder = new RecordFileBuilder().multi().blockSize(valueBlockSize).
        multiFileSize(MultiFileSize);

    return new HybridTree<>(getDir(fileName), fName(), new StringCodec(), new IntCodec(), builder);
  }
}
