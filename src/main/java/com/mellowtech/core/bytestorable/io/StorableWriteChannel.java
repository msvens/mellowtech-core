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
package com.mellowtech.core.bytestorable.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import com.mellowtech.core.bytestorable.ByteStorable;

/**
 * Writes ByteStorables to a given output channel.
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class StorableWriteChannel {

  private WritableByteChannel mWbc;
  private ByteBuffer mBuffer;

  /**
   * Initialize to write ByteStorables to a given channel. The
   * StorableWriteChannel allocates a ByteBuffer (4096) to used for faster
   * writing.
   * 
   * @param wbc
   *          channel to write to
   * @see ByteBuffer
   */
  public StorableWriteChannel(WritableByteChannel wbc) {
    mWbc = wbc;
    mBuffer = ByteBuffer.allocate(4096);
  }

  /**
   * Flushes and closes this channel.
   * 
   * @exception IOException
   *              if an error occurs
   */
  public void close() throws IOException {
    flush();
    mWbc.close();
  }

  /**
   * Write a ByteStorable to the stream.
   * 
   * @param tmp
   *          a <code>ByteStorable</code> value
   * @exception IOException
   *              if an error occurs
   */
  public void write(ByteStorable <?> tmp) throws IOException {
    int bsz = tmp.byteSize();

    if (mBuffer.remaining() < bsz) {
      mBuffer.flip();
      if(mWbc.write(mBuffer) <= 0)
    	  throw new IOException("Channel closed");
      if (mBuffer.capacity() < bsz)
        mBuffer = ByteBuffer.allocate(bsz + 64);
      mBuffer.clear();
    }
    tmp.toBytes(mBuffer);
  }

  /**
   * Flushes any buffered content that has not yet been written.
   * 
   * @exception IOException
   *              if an error occurs
   */
  public void flush() throws IOException {
    mBuffer.flip();
    mWbc.write(mBuffer);
    mBuffer.clear();
  }
}
