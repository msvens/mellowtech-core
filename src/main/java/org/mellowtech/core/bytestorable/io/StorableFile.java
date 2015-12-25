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
package org.mellowtech.core.bytestorable.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.mellowtech.core.bytestorable.BStorable;

/**
 * Static methods for storing a single ByteStorable or ByteBuffer in a File.
 */
public class StorableFile {

  /**
   * Converting an entire file's content to a ByteStorable, useful if one has a
   * single ByteStorable in a file.
   * 
   * @param fileName
   *          the file name
   * @param template
   *          the BComparable template
   * @param <A> Wrapped BComparable class
   * @param <B> BComparable class 
   * @return BComparable of type B
   * @exception IOException
   *              if an error occurs
   */
  public static <A, B extends BStorable<A,B>> B readFileAsByteStorable(String fileName,
      B template) throws IOException {
    ByteBuffer bb = readFileAsByteBuffer(fileName);
    if(bb == null || bb.capacity() < 1) return null;
    return template.from((ByteBuffer) bb.flip());
  }

  /**
   * Converting an entire ByteStorable to a file, useful if one needs a single
   * ByteStorable in a file.
   * 
   * @param fileName
   *          the file name
   * @param object BComparable template
   * @exception IOException
   *              if an error occurs
   */
  public static void writeFileAsByteStorable(String fileName,
      BStorable <?,?> object) throws IOException {
    writeFileAsByteBuffer(fileName, (ByteBuffer) object.to().flip());
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
    File f = new File(fileName);
    if(!f.exists()) return null;
    RandomAccessFile raf = new RandomAccessFile(new File(fileName), "r");
    if(raf.length() < 1){
      raf.close();
      return null;
    }
    FileChannel fc = raf.getChannel();
    ByteBuffer bb = ByteBuffer.allocate((int) raf.length());
    fc.read(bb);
    fc.close();
    raf.close();
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
    RandomAccessFile raf = new RandomAccessFile(new File(fileName), "rwd");
    raf.setLength(0);
    FileChannel fc = raf.getChannel();
    fc.write(bb);
    fc.force(true);
    fc.close();
    raf.close();
  }
}
