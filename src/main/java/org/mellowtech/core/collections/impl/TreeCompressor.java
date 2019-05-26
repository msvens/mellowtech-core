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

import org.mellowtech.core.codec.BBuffer;
import org.mellowtech.core.codec.BCodec;
import org.mellowtech.core.collections.BTree;
import org.mellowtech.core.collections.KeyValue;
import org.mellowtech.core.io.compress.CFileBuilder;
import org.mellowtech.core.io.compress.CFileWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * @author msvens
 * @since 2018-05-06
 */
public class TreeCompressor<A,B> {

  /**
   *
   * @param tree
   * @param dir
   * @param name
   * @param blockSize size of the uncompressed block
   */
  public void compress(BTree<A,B> tree, Path dir, String name, BCodec<A> keyCodec,
                       BCodec<B> valueCodec, int blockSize) throws IOException {

    Iterator<KeyValue<A,B>> iter = tree.iterator();
    BBuffer<KeyValue<A,B>> buffer;
    KeyValueCodec<A,B> codec = new KeyValueCodec<>(keyCodec,valueCodec);
    buffer = new BBuffer<>(ByteBuffer.allocateDirect(blockSize), codec, BBuffer.PtrType.BIG);
    Path file = dir.resolve(name+".cval");
    CFileBuilder cFileBuilder = new CFileBuilder();
    cFileBuilder.blockSize(blockSize).path(file);
    CFileWriter writer = cFileBuilder.writer();

    while(iter.hasNext()){
      KeyValue<A,B> kv = iter.next();
      if(!buffer.fits(kv)){ //flush current buffer
        System.out.println(buffer.getNumberOfElements());
        writer.add(buffer.getBlock(), 0, blockSize);
        buffer.clear();
      }
      buffer.insertUnsorted(kv); //since it is already sorted
    }
    //write out the last
    if(buffer.getNumberOfElements() > 0)
      writer.add(buffer.getBlock(), 0, blockSize);
    writer.close();
  }


}
