/**
 * 
 */
package org.mellowtech.core.io;

import java.io.IOException;

/**
 * @author msvens
 *
 */
public class RecordFileBuilder {
  
  public enum Strategy {DISC, SPLIT, MEM_SPLIT, MEM}

  private boolean spanned = false;
  private Strategy strategy = Strategy.SPLIT;
  private boolean iterate = true;
  private Integer maxBlocks = null;
  private int blockSize = 512;
  private int reserve = 0;
  private int splitBlockSize = 512;
  private Integer splitMaxBlocks = null;
  
  /**
   * 
   */
  public RecordFileBuilder() {
  }
  
  public RecordFileBuilder span(boolean spanned) {
    this.spanned = true;
    return this;
  }
  
  public RecordFileBuilder mem() {
    strategy = Strategy.MEM;
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
    RecordFile rf = RecordFileBuilder.create(strategy, maxBlocks, fileName, blockSize, reserve, splitMaxBlocks, splitBlockSize);
    if(spanned){
      if(iterate){
        return new IteratingSpannedBlockFile(rf);
      }
      return new SpannedBlockFile(rf);
    }
    return rf;
  }
  
  private static RecordFile create(Strategy s, Integer maxBlocks, String fName, 
      int blockSize, int reserve, Integer splitMaxBlocks, Integer splitBlockSize) throws IOException {
    switch(s) {
      case DISC:
        return maxBlocks != null ? new BlockFile(fName, blockSize, maxBlocks, reserve) : new BlockFile(fName);
      case SPLIT:
        return maxBlocks != null ? new SplitBlockFile(fName, blockSize, maxBlocks, reserve, splitMaxBlocks, splitBlockSize) : new SplitBlockFile(fName);
      case MEM_SPLIT :
        return maxBlocks != null ? new MemSplitBlockFile(fName, blockSize, maxBlocks, reserve, splitMaxBlocks, splitBlockSize) : new MemSplitBlockFile(fName);
      case MEM :
        return maxBlocks != null ? new MemBlockFile(fName, blockSize, maxBlocks, reserve) : new MemBlockFile(fName);
    }
    return null;
  }
  

}
