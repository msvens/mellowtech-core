package org.mellowtech.core.sort;

import org.mellowtech.core.codec.BCodec;
import org.mellowtech.core.codec.CodecUtil;
import org.mellowtech.core.codec.Codecs;
import org.mellowtech.core.util.Platform;

import java.nio.file.Path;

/**
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 4.0.0
 * @param <A> key type
 */
public class SortBuilder <A extends Comparable<A>> {

  public final static int DEFAULT_BLOCK_SIZE = 4096;
  public final static int DEFAULT_MEMORY_SIZE = 1024*1024*64;

  private int blockSize = DEFAULT_BLOCK_SIZE;
  private int memorySize = DEFAULT_MEMORY_SIZE;
  private boolean byteSort = false;
  private int compression = 0;
  private Path tempPath = Platform.getTempDir();
  private BCodec<A> codec;


  /**
   * Size of each read from disk. The blocksize
   * @param size
   * @return this
   */
  public SortBuilder <A> blockSize(int size){
    this.blockSize = size;
    return this;
  }

  /**
   * Max memory (approximate) used for in-memory sorting. This can vary
   * depending on implementation
   * @param size
   * @return this
   */
  public SortBuilder <A> memorySize(int size){
    this.memorySize = size;
    return this;
  }

  public SortBuilder <A> tempPath(Path path){
    this.tempPath = path;
    return this;
  }

  public SortBuilder <A> compression(int level){
    this.compression = level;
    return this;
  }

  public SortBuilder<A> codec(BCodec<A> codec) {
    this.codec = codec;
    return this;
  }

  public SortBuilder<A> codec(Class<A> type){
    this.codec = Codecs.fromClass(type);
    return this;
  }

  public SortBuilder <A> byteSort(boolean sort){
    this.byteSort = sort;
    return this;
  }

  public DiscSort<A> build(){
    if(codec == null)
      throw new NullPointerException("codec cannot be null");
    if(byteSort){
      return new EDiscBasedSort<>(codec,blockSize,memorySize,compression,tempPath);
    } else {
      return new DiscBasedSort<>(codec,blockSize,memorySize,compression,tempPath);
    }
  }




}
