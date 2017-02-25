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

package org.mellowtech.core.collections.impl;

import java.nio.ByteBuffer;

import org.mellowtech.core.codec.BCodec;

/**
 * Pointer to hold a file position and a number of bytes
 * Date: 2013-03-22
 * Time: 08:07
 *
 * @author Martin Svensson
 * @since 4.0.0
 */
public class BlobPointerCodec implements BCodec<BlobPointer> {

  @Override
  public int byteSize(BlobPointer blobPointer) {
    return 12;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return 12;
  }

  @Override
  public BlobPointer from(ByteBuffer bb) {
    return new BlobPointer(bb.getLong(), bb.getInt());
  }

  @Override
  public void to(BlobPointer value, ByteBuffer bb) {
    bb.putLong(value.fPointer);
    bb.putInt(value.bSize);
  }

}

class BlobPointer {
  public long fPointer;
  public int bSize;
  public BlobPointer(){}
  public BlobPointer(long pointer, int size){fPointer = pointer; bSize = size;}
  public String toString(){return fPointer+": "+bSize;};
}