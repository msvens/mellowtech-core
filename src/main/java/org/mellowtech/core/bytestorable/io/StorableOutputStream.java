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
import java.io.OutputStream;
import java.nio.channels.Channels;

import org.mellowtech.core.bytestorable.BStorable;

/**
 * Writes BStorables to a given output stream. Internally it uses a
 * CodecWriteChannel
 * 
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 * @see StorableWriteChannel
 */
public class StorableOutputStream {

  private StorableWriteChannel mChannel;

  /**
   * Initialize to write BStorables to a given output.
   * 
   * @param os
   *          stream to write to
   * @exception IOException
   *              if an error occurs
   */
  public StorableOutputStream(OutputStream os) throws IOException {
    mChannel = new StorableWriteChannel(Channels.newChannel(os));
  }

  /**
   * Write a BStorable to the stream.
   * 
   * @param bs
   *          the next BStorable to write
   * @exception IOException
   *              if an error occurs
   */
  public void write(BStorable <?> bs) throws IOException {
    mChannel.write(bs);
  }

  /**
   * Flushes any buffered content that has not yet been written.
   * 
   * @exception IOException
   *              if an error occurs
   */
  public void flush() throws IOException {
    mChannel.flush();
  }

}
