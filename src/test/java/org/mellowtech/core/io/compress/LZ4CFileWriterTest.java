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

package org.mellowtech.core.io.compress;

import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author msvens
 * @since 2018-03-24
 */
@DisplayName("LZ4 CFileReader")
public class LZ4CFileWriterTest extends CFileWriterTemplate {


  @Override
  String fName() {
    return "lz4CFileWriter.lzc";
  }

  @Override
  CFile init(Path name) throws IOException {
    return new LZ4FileWriter(name, DEFAULT_BLOCK_SIZE);
  }


}