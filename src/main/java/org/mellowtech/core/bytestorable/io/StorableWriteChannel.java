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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import org.mellowtech.core.bytestorable.BStorable;

/**
 * Writes BStorables to a given output channel.
 * 
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 */
public class StorableWriteChannel {

  private WritableByteChannel mWbc;
  private ByteBuffer mBuffer;

  /**
   * Initialize to write ByteStorables to a given channel. The
   * CodecWriteChannel allocates a ByteBuffer (4096) to used for faster
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
   * Write a BStorable to the stream.
   * 
   * @param tmp
   *          a <code>ByteStorable</code> value
   * @exception IOException
   *              if an error occurs
   */
  public void write(BStorable <?> tmp) throws IOException {
    int bsz = tmp.byteSize();

    if (mBuffer.remaining() < bsz) {
      mBuffer.flip();
      if(mWbc.write(mBuffer) <= 0)
    	  throw new IOException("Channel closed");
      if (mBuffer.capacity() < bsz)
        mBuffer = ByteBuffer.allocate(bsz + 64);
      mBuffer.clear();
    }
    tmp.to(mBuffer);
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
