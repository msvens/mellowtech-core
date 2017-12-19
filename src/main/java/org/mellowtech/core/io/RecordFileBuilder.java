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

package org.mellowtech.core.io;

import org.mellowtech.core.io.impl.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Builder for record based files. Always use this class when creating and opening
 * your record file
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 */
public class RecordFileBuilder {
  
  public enum Strategy {DISC, SPLIT, MEM_SPLIT, DISC_MEM, MULTI}

  private boolean spanned = false;
  private Strategy strategy = Strategy.DISC;
  private boolean iterate = true;
  private Integer maxBlocks = null;
  private int blockSize = 512;
  private int reserve = 0;
  private int splitBlockSize = 512;
  private Integer splitMaxBlocks = null;
  private int multiFileSize = 1024*1024*64;
  
  /**
   * Create a new RecordFileBuilder
   */
  public RecordFileBuilder() {
  }

  public RecordFileBuilder strategy(Strategy s){
    this.strategy = s;
    return this;
  }
  
  public RecordFileBuilder span(boolean spanned) {
    this.spanned = true;
    return this;
  }
  
  public RecordFileBuilder mem() {
    strategy = Strategy.DISC_MEM;
    return this;
  }
  
  public RecordFileBuilder split() {
    strategy = Strategy.SPLIT;
    return this;
  }
  
  public RecordFileBuilder memSplit() {
    strategy = Strategy.MEM_SPLIT;
    return this;
  }
  
  public RecordFileBuilder disc() {
    strategy = Strategy.DISC;
    return this;
  }

  public RecordFileBuilder multi() {
    strategy = Strategy.MULTI;
    return this;
  }

  public boolean isMapped(){
    return strategy == Strategy.MEM_SPLIT || strategy == Strategy.DISC_MEM || strategy == Strategy.MULTI;
  }

  public RecordFileBuilder multiFileSize(int fileSize){
    this.multiFileSize = fileSize;
    return this;
  }
  
  public RecordFileBuilder iterate(boolean iter) {
    this.iterate = iter;
    return this;
  }
  
  public RecordFileBuilder maxBlocks(Integer max) {
    this.maxBlocks = max;
    return this;
  }
  
  public RecordFileBuilder blockSize(int size) {
    this.blockSize = size;
    return this;
  }
  
  public RecordFileBuilder splitBlockSize(int size) {
    this.splitBlockSize = size;
    return this;
  }
  
  public RecordFileBuilder splitMaxBlocks(int size) {
    this.splitMaxBlocks = size;
    return this;
  }

  
  public RecordFileBuilder reserve(int bytes) {
    this.reserve = bytes;
    return this;
  }

  public RecordFile build(String fileName) throws IOException{
    return build(Paths.get(fileName));
  }

  public RecordFile build(Path path) throws IOException{
    if(spanned)
      return new VariableRecordFile(path,maxBlocks == null ? 0 : maxBlocks,reserve);
    else
      return RecordFileBuilder.create(strategy, maxBlocks, path, blockSize, reserve, splitMaxBlocks, splitBlockSize, multiFileSize);
  }
  
  private static RecordFile create(Strategy s, Integer maxBlocks, Path path,
      int blockSize, int reserve, Integer splitMaxBlocks, Integer splitBlockSize, int multiFileSize) throws IOException {
    switch(s) {
      case DISC:
        return maxBlocks != null ? new BlockFile(path, blockSize, maxBlocks, reserve) : new BlockFile(path);
      case SPLIT:
        return maxBlocks != null ? new SplitBlockFile(path, blockSize, maxBlocks, reserve, splitMaxBlocks, splitBlockSize) : new SplitBlockFile(path);
      case MEM_SPLIT :
        return maxBlocks != null ? new MemSplitBlockFile(path, blockSize, maxBlocks, reserve, splitMaxBlocks, splitBlockSize) : new MemSplitBlockFile(path);
      case DISC_MEM :
        return maxBlocks != null ? new MemBlockFile(path, blockSize, maxBlocks, reserve) : new MemBlockFile(path);
      case MULTI:
        return new MultiBlockFile(multiFileSize, blockSize, reserve, path);
    }
    return null;
  }
  

}
