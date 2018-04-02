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

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * @author msvens
 * @since 2018-02-06
 */
public class LZ4FileWriter extends AbstractCFile implements CFileWriter {


  private final LZ4Compressor compressor;
  private ByteBuffer compBuffer;

  public static byte[] compress(byte[] input){
    LZ4Compressor comp = LZ4Factory.unsafeInstance().fastCompressor();
    return comp.compress(input);
  }

  LZ4FileWriter(Path file, int defaultBlockSize) throws IOException {
    super(file, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    compressor = LZ4Factory.unsafeInstance().fastCompressor();
    compBuffer = ByteBuffer.allocateDirect(compressor.maxCompressedLength(defaultBlockSize));
  }

  @Override
  public long add(ByteBuffer bb) throws IOException{
    int maxLength = compressor.maxCompressedLength(bb.limit());
    if(compBuffer.capacity() < maxLength){
      compBuffer = ByteBuffer.allocateDirect(maxLength);
    }
    compBuffer.clear();
    compBuffer.position(BlockPointer.ByteSize);
    compressor.compress(bb, compBuffer);
    int length = compBuffer.position() - BlockPointer.ByteSize;

    BlockPointer ptr = new BlockPointer(-1, length, bb.limit(), false);
    ptr.write(compBuffer, 0);
    compBuffer.flip();
    long offset = fc.size();
    fc.write(compBuffer);
    return offset;

  }

  @Override
  public FileChannel fc() {
    return fc;
  }


}
