package org.mellowtech.core.io;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * Created by msvens on 29/10/15.
 */
public class BlockFileUtils {

  //Mapped Block Utils:
  public static int calcBlocksToMap(int blockSize, int maxBlocks) {
    int region4 = 1024 * 1024 * 4;
    int region8 = region4 * 2;
    int region16 = region8 * 2;
    int region32 = region16 * 2;
    int region64 = region32 * 2; //64 MB


    long maxRecordSize = (long) blockSize * (long) maxBlocks;
    if (maxRecordSize < region64) {
      return maxBlocks;
    } else if (maxRecordSize / region64 > 10) {
      return (int) Math.ceil(region64 / (double) blockSize);
    } else if (maxRecordSize / region32 > 10) {
      return (int) Math.ceil(region32 / (double) blockSize);
    } else if (maxRecordSize / region16 > 10) {
      return (int) Math.ceil(region16 / (double) blockSize);
    } else if (maxRecordSize / region8 > 10) {
      return (int) Math.ceil(region8 / (double) blockSize);
    } else {
      return (int) Math.ceil(region4 / (double) blockSize);
    }
  }

  public static void mapBlocks(FileChannel fc, List <MappedByteBuffer> blocks, long start, long end, int blockSize, int blocksToMap) throws IOException {
    int region = blockSize * blocksToMap;
    long current = start;
    while (current < end) {
      MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_WRITE, current, region);
      current += region;
      blocks.add(bb);
    }
  }


}
