/*
 * Copyright (c) 2013 mellowtech.org.
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 *
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 *
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 */
package org.mellowtech.core.bytestorable.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.mellowtech.core.bytestorable.ByteStorable;

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
   * @param ByteStorable
   *          template the bytestorable template
   * @return a <code>ByteStorable</code> value
   * @exception IOException
   *              if an error occurs
   */
  public static <K extends ByteStorable> K  readFileAsByteStorable(String fileName,
      K template) throws IOException {
    ByteBuffer bb = readFileAsByteBuffer(fileName);
    if(bb == null || bb.capacity() < 1) return null;
    return (K) template.fromBytes((ByteBuffer) bb.flip());
  }

  /**
   * Converting an entire ByteStorable to a file, useful if one needs a single
   * ByteStorable in a file.
   * 
   * @param fileName
   *          the file name
   * @param ByteStorable
   *          template the bytestorable template
   * @return a <code>ByteStorable</code> value
   * @exception IOException
   *              if an error occurs
   */
  public static void writeFileAsByteStorable(String fileName,
      ByteStorable <?> object) throws IOException {
    writeFileAsByteBuffer(fileName, (ByteBuffer) object.toBytes().flip());
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
