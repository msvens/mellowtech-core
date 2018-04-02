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
import java.nio.file.Path;

/**
 * @author msvens
 * @since 2018-02-24
 */
public class CFileBuilder {

  enum Compressor{LZ4};


  private int DEFAULT_BLOCK_SIZE = 1024*64;

  private int blockSize = DEFAULT_BLOCK_SIZE;
  private boolean read = false;
  private boolean write = false;
  private boolean mapped = false;
  private Path p = null;
  private Compressor compressor = Compressor.LZ4;

  public CFileBuilder read(){
    read = true;
    return this;
  }

  public CFileBuilder write(){
    write = true;
    return this;
  }

  public CFileBuilder mapped(){
    mapped = true;
    return this;
  }

  public CFileBuilder path(Path p){
    this.p = p;
    return this;
  }

  public CFileBuilder blockSize(int size){
    blockSize = size;
    return this;
  }

  public CFileReader reader() throws IOException {
    read = true;
    write = false;
    return (CFileReader) build();
  }

  public CFileWriter writer() throws IOException {
    read = false;
    write = true;
    return (CFileWriter) build();
  }

  public CFileReaderWriter readerWriter() throws IOException {
    read = true;
    write = true;
    return (CFileReaderWriter) build();
  }

  public CFile build() throws IOException {
    if(!(read || write))
      throw new IllegalArgumentException("both read and write cannot be false");
    if(p == null)
      throw new IllegalArgumentException("no path specified");

    if(mapped)
      return buildMapped();

    if(read && write)
      throw new UnsupportedOperationException("no read writer yet");
    else if(write)
      return new LZ4FileWriter(p, blockSize);
    else
      return new LZ4CFileReader(p, blockSize);
  }

  private CFile buildMapped() throws IOException{
    if(read && write)
      throw new UnsupportedOperationException("no read writer yet");
    else if(write)
      return new LZ4FileWriter(p, blockSize);
    else
      return new LZ4MappedCFileReader(p);
  }



}
