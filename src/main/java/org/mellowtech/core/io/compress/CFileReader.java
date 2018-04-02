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
import java.util.Iterator;

/**
 * @author msvens
 * @since 2018-02-12
 */
public interface CFileReader extends CFile, Iterable<BlockPointer> {

  default ByteBuffer read(long offset) throws IOException {
    return read(get(offset));
  }

  default ByteBuffer read(BlockPointer ptr) throws IOException{
    ByteBuffer buffer = ByteBuffer.allocate(ptr.getOrigSize());
    int read = read(ptr, buffer, 0);
    if(read != ptr.getOrigSize())
      throw new Error("Size dont match: "+read+" "+ptr.getOrigSize());
    return buffer;
  }

  default long next(BlockPointer ptr) throws IOException {
    return BlockPointer.ByteSize + ptr.getSize();
  }

  int read(BlockPointer ptr, ByteBuffer bb, int bufferOffset) throws IOException;

  default int read(long fileOffset, ByteBuffer bb, int bufferOffset) throws IOException{
    return read(get(fileOffset), bb, bufferOffset);
  }

  default Iterator<BlockPointer> iterator(long offset){
    return Iterators.fileIterator(fc(), offset);
  }

  default Iterator<BlockPointer> iterator(){
    return iterator(0);
  }

}

