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
package org.mellowtech.core.codec.io;

import org.mellowtech.core.codec.BCodec;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Static methods for storing a single ByteStorable or ByteBuffer in a File.
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 4.0.0
 */
public class CodecFile {

  /**
   * Converting an entire file's content to a ByteStorable
   * 
   * @param fileName
   *          the file name
   * @param template
   *          BCodec template
   * @param <A> type
   * @return instance of type A
   * @exception IOException
   *              if an error occurs
   */
  public static <A> A readFileAsByteStorable(String fileName,
      BCodec<A> template) throws IOException {
    ByteBuffer bb = readFileAsByteBuffer(fileName);
    if(bb == null || bb.capacity() < 1) return null;
    return template.from((ByteBuffer) bb.flip());
  }

  /**
   * Convert a ByteStorable to a file.
   * 
   * @param fileName
   *          the file name
   * @param object instance
   * @param codec instance codec
   * @param <A> instance type
   * @exception IOException
   *              if an error occurs
   */
  public static <A> void writeFileAsByteStorable(String fileName,
      A object, BCodec<A> codec) throws IOException {
    writeFileAsByteBuffer(fileName, (ByteBuffer) codec.to(object).flip());
  }

  /**
   * Convert an entire file's content to a ByteBuffer,
   * 
   * @param fileName
   *          the file name
   * @return a <code>ByteBuffer</code> value
   * @exception IOException
   *              if an error occurs
   */
  private static ByteBuffer readFileAsByteBuffer(String fileName)
      throws IOException {
    Path p = Paths.get(fileName);
    if(!Files.exists(p)) return null;
    FileChannel fc = FileChannel.open(p, StandardOpenOption.READ);
    ByteBuffer bb = ByteBuffer.allocate((int) fc.size());
    fc.read(bb);
    fc.close();
    return bb;
  }

  /**
   * Convert an entire ByteBuffer to a file.
   * 
   * @param fileName
   *          the file name
   * @param bb
   *          the ByteBuffer
   * @exception IOException
   *              if an error occurs
   */
  private static void writeFileAsByteBuffer(String fileName, ByteBuffer bb)
      throws IOException {
    Path p = Paths.get(fileName);
    FileChannel fc = FileChannel.open(p, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    fc.write(bb);
    fc.close();
  }
}
