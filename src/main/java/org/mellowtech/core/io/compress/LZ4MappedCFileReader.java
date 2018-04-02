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

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

/**
 * @author msvens
 * @since 2018-02-13
 */
public class LZ4MappedCFileReader extends AbstractCFile implements CFileReader {

  private final MappedByteBuffer mbb;
  private final LZ4FastDecompressor decomp;

  LZ4MappedCFileReader(Path file) throws IOException{
    super(file, StandardOpenOption.READ);
    if(fc.size() > Integer.MAX_VALUE)
      throw new IOException("file to large to map in one buffer");
    mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, (int) fc.size());
    decomp = LZ4Factory.unsafeInstance().fastDecompressor();
  }

  @Override
  public FileChannel fc() {
    return fc;
  }

  @Override
  public BlockPointer get(long offset) throws IOException{
    return BlockPointer.read(mbb, (int) offset, offset);
  }

  @Override
  public Iterator<BlockPointer> iterator(long offset) {
    return Iterators.bufferIterator(mbb, offset);
  }

  @Override
  public int read(BlockPointer ptr, ByteBuffer bb, int bufferOffset) throws IOException{
    if(bufferOffset + ptr.getOrigSize() > bb.limit())
      throw new IOException("decompressed data can not fit in the buffer");
    decomp.decompress(mbb, (int) ptr.getOffset()+BlockPointer.ByteSize,
        bb, bufferOffset, ptr.getOrigSize());
    return ptr.getOrigSize();
  }

  public String toString(){
    StringBuilder stringBuilder = new StringBuilder();
    this.forEach((b) -> {
      stringBuilder.append(b.toString()); stringBuilder.append('\n');
    });
    return stringBuilder.toString();
  }
}
