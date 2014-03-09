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
package com.mellowtech.core.disc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.util.ArrayUtils;

/**
 * A first version of PackedByteStorableFile. Lots of room for optimization:<br/>
 * 1. Now uses a SpanningBlockFile as a fileStore (maybe unnesseary) 2. Used
 * ByteArraOutput and Input Stream which is far from optimal. We should really
 * create our own input/output streams that does not need to be reallocated. If
 * this is done we will see major speed improvements.
 * 
 * @author Martin Svensson
 * 
 */
public class PackedByteStorableFile {

  private SpanningBlockFile fileStore;
  private ByteStorable template;
  private byte[] buffer;

  public PackedByteStorableFile(String fileName, ByteStorable template)
      throws IOException {
    fileStore = new SpanningBlockFile(fileName);
    this.template = template;
    this.buffer = new byte[1024];
  }

  public int insertByteStorable(ByteStorable toInsert) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    GZIPOutputStream gOut = new GZIPOutputStream(bos);
    if (buffer.length < toInsert.byteSize())
      buffer = new byte[(int) (toInsert.byteSize() * 1.75)];
    int length = toInsert.toBytes(buffer, 0);
    gOut.write(buffer, 0, length);
    return fileStore.insert(bos.toByteArray(), bos.size());
    // return null;
  }

  public ByteStorable getByteStorable(int record) throws IOException {
    byte bytes[] = fileStore.get(record);
    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
    GZIPInputStream gIn = new GZIPInputStream(bis);
    int b;
    int offset = 0;
    while ((b = gIn.read()) != -1) {
      if (offset == buffer.length)
        ArrayUtils.setSize(buffer, (int) (buffer.length * 1.75));
      buffer[offset++] = (byte) b;
    }
    return template.fromBytes(buffer, 0);
  }

  public ByteStorable getByteStorable(int record, ByteStorable type)
      throws IOException {
    byte bytes[] = fileStore.get(record);
    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
    GZIPInputStream gIn = new GZIPInputStream(bis);
    int b;
    int offset = 0;
    while ((b = gIn.read()) != -1) {
      if (offset == buffer.length)
        ArrayUtils.setSize(buffer, (int) (buffer.length * 1.75));
      buffer[offset++] = (byte) b;
    }
    return type.fromBytes(buffer, 0);
  }

  /**
   * Close an open file. Header and pointer data will be saved. Always call this
   * method when closing a PackedStorableFile file
   * 
   * @throws IOException
   *           if the file could not be written to disc.
   */
  public void close() throws IOException {
    fileStore.closeFile();
  }

}
