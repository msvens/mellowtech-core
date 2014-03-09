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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * @author martins
 * 
 */
public class ByteBufferOutputStream extends OutputStream {

  ByteBuffer bb;
  boolean extendable;
  private static int DEFAULT_SIZE = 1024;

  public ByteBufferOutputStream(ByteBuffer buffer) throws IOException {
    if (buffer.isReadOnly())
      throw new IOException("ByteBuffer is read only");

    this.bb = buffer;
    extendable = false;
  }

  public ByteBufferOutputStream() {
    bb = ByteBuffer.allocate(DEFAULT_SIZE);
    extendable = true;
  }

  public void reset() {
    bb.clear();
  }

  public ByteBuffer getByteBuffer() {
    return bb;
  }

  public void write(int b) throws IOException {
    if (bb.position() >= bb.capacity() && extendable) {
      expandBuffer();
    }
    else if (bb.position() >= bb.limit())
      throw new IOException("Underlying buffer has reached its limit");
    bb.put((byte) b);
  }

  public synchronized void write(byte[] bytes, int off, int len)
      throws IOException {
    if (!this.extendable) {
      len = Math.min(len, bb.remaining());
      bb.put(bytes, off, len);
      return;
    }
    if (len > bb.remaining())
      expandBuffer(len - bb.remaining());
    bb.put(bytes, off, len);
  }

  private void expandBuffer(int min) {
    int toExpand = bb.capacity() < min ? min : bb.capacity();
    ByteBuffer tmpBuffer = ByteBuffer.allocate(bb.capacity() + toExpand);
    bb.flip();
    tmpBuffer.put(bb);
    bb = tmpBuffer;
  }

  private void expandBuffer() {
    ByteBuffer tmpBuffer = ByteBuffer.allocate(bb.capacity() * 2);
    bb.clear();
    tmpBuffer.put(bb);
    bb = tmpBuffer;
  }

}
