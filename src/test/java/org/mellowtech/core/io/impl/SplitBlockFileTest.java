package org.mellowtech.core.io.impl;

import org.junit.jupiter.api.DisplayName;
import org.mellowtech.core.io.RecordFile;
import org.mellowtech.core.io.SplitRecordFile;

import java.nio.file.Path;

/**
 * Date: 2013-03-11
 * Time: 14:43
 *
 * @author Martin Svensson
 */
@DisplayName("A SplitBlockFile")
public class SplitBlockFileTest extends SplitRecordFileTemplate{

  @Override
  public String fname() {return "splitBlockFileTest.blf";}

  @Override
  public String fnameMoved() {return "splitBlockFileTestMoved.blf";}

  @Override
  public long blocksOffset() {
    return ((SplitBlockFile)rf).blocksOffset();
  }

  @Override
  public SplitRecordFile init(int blockSize, int reserve, int maxBlocks, Path fname) throws Exception {
    return new SplitBlockFile(fname, blockSize, maxBlocks, reserve, maxBlocks, blockSize);
  }

  @Override
  public RecordFile reopen(Path fname) throws Exception {
    return new SplitBlockFile(fname);
  }

}
