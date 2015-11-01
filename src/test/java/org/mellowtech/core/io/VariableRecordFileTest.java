package org.mellowtech.core.io;

import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;

/**
 * Created by msvens on 28/10/15.
 */
public class VariableRecordFileTest extends RecordFileTemplate {

  @Override
  public String fname() {return "variableRecordFileTest.blf";}

  @Override
  public RecordFile init(int blockSize, int reserve, int maxBlocks, String fname) throws Exception {
    //return new BlockFile(Paths.get(fname), blockSize, maxBlocks, reserve);
    return new VariableRecordFile(Paths.get(fname),maxBlocks,reserve);
  }

  @Override
  public RecordFile reopen(String fname) throws Exception {
    return new VariableRecordFile(Paths.get(fname));
  }

  /**
   * A VariableRecordFile does not have blocks in that sense
   */
  @Test
  public void blockSize(){
    Assert.assertEquals(0, rf.getBlockSize());
  }

}
