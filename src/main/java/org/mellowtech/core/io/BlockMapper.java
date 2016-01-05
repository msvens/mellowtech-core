
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

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Description...
 * Created by msvens on 29/10/15.
 */
public class BlockMapper {

  private final List<MappedByteBuffer> blocks = new ArrayList<>();
  private int blockSize;
  private int maxBlocks;
  private int blocksToMap;
  private FileChannel fc;
  private long offset;

  public BlockMapper(FileChannel fc, long offsetBlocks, int blockSize, int maxBlocks) {
    this.fc = fc;
    this.offset = offsetBlocks;
    this.blockSize = blockSize;
    this.maxBlocks = maxBlocks;
    this.blocksToMap = calcBlocksToMap();
  }

  public void expand(int toIndex) throws IOException {
    long pos = getBufferPos(toIndex);
    for (int i = blocks.size(); i <= pos; i++) {
      long filePos = offset + (pos * blockSize * blocksToMap);
      blocks.add(fc.map(FileChannel.MapMode.READ_WRITE, filePos, blockSize * blocksToMap));
    }
  }

  public MappedByteBuffer find(int record) {
    return blocks.get(getBufferPos(record));
  }

  public void force() {
    for (MappedByteBuffer mpp : blocks) {
      mpp.force();
    }
  }

  public int getBufferPos(int record) {
    return record / blocksToMap;
  }

  public void map(long end) throws IOException {
    int region = blockSize * blocksToMap;
    long current = offset;
    while (current < end) {
      MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_WRITE, current, region);
      current += region;
      blocks.add(bb);
    }
  }

  public void maybeExpand(int record) throws IOException {
    if (getBufferPos(record) >= blocks.size()) {
      expand(record);
    }
  }

  public void shrink(int lastRecord) throws IOException {
    int blockNo = getBufferPos(lastRecord);
    boolean removed = false;
    for (int i = blocks.size() - 1; i > blockNo; i--) {
      blocks.remove(i);
      removed = true;
    }
    if (removed) {
      int region = blockSize * blocksToMap;
      fc.truncate(offset + ((blockNo + 1) * region));
    }
  }

  public int truncate(int record) {
    return record - (getBufferPos(record) * blocksToMap);
  }

  private int calcBlocksToMap() {
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
}
