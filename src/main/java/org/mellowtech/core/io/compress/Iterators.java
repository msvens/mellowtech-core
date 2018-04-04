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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;

/**
 * @author msvens
 * @since 2018-02-13
 */
class Iterators {

  static Iterator<BlockPointer> bufferIterator(ByteBuffer  bb, long offset){
    if(offset >= Integer.MAX_VALUE)
      throw new IndexOutOfBoundsException("offset out of bounds");
    return new MappedCompressedFileIterator(bb, (int) offset);
  }

  static Iterator<BlockPointer> fileIterator(FileChannel fc, long offset){
    return new CompressedFileIterator(fc, offset);
  }

  static class CompressedFileIterator implements Iterator<BlockPointer> {

    FileChannel fc;
    long offset;

    public CompressedFileIterator(FileChannel fc, long startOffset) {
      this.fc = fc;
      this.offset = startOffset;
    }

    @Override
    public boolean hasNext() {
      try {
        return offset < fc.size();
      } catch (IOException e){
        throw new Error(e);
      }
    }

    @Override
    public BlockPointer next() {
      if(!hasNext()) return null;
      try{
        BlockPointer ptr = BlockPointer.read(fc, offset);
        offset += BlockPointer.ByteSize + ptr.getSize();
        return ptr;
      } catch(IOException e){
        throw new Error(e);
      }
    }
  }

  static class MappedCompressedFileIterator implements Iterator<BlockPointer> {

    ByteBuffer buffer;
    int offset = 0;
    public MappedCompressedFileIterator(ByteBuffer buffer, int startOffset){
      this.buffer = buffer;
      this.offset = startOffset;

    }

    @Override
    public boolean hasNext() {
      return offset < buffer.capacity();
    }

    @Override
    public BlockPointer next() {
      if(!hasNext()) return null;
      try {
        BlockPointer ptr = BlockPointer.read(buffer, offset, offset);
        offset += BlockPointer.ByteSize + ptr.getSize();
        return ptr;
      } catch (IOException e){
        throw new Error(e);
      }
    }
  }


}
