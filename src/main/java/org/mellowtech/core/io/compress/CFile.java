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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * @author msvens
 * @since 2018-02-10
 */
public interface CFile {

  static int BLOCK = 787997538;
  static int DELETED_BLOCK = 687567531;
  //static int DEFAULT_BLOCK_SIZE = 64*1024;

  FileChannel fc();
  Path p();

  default BlockPointer get(long offset) throws IOException{
    return BlockPointer.read(fc(), offset);
  }

  default void close() throws IOException {
    fc().close();
  }

  default void delete() throws IOException {
    if(isOpen())
      close();
    Files.delete(p());
  }

  default boolean isOpen(){
    return fc().isOpen();
  }
}

abstract class AbstractCFile implements CFile{

  final Path p;
  final FileChannel fc;

  AbstractCFile(Path p, OpenOption... options) throws IOException{
    this.p = p;
    fc = FileChannel.open(p, options);
  }

  public Path p(){
    return p;
  }

  public FileChannel fc(){
    return fc;
  }
}




