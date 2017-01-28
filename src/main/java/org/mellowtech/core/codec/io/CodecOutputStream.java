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

import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.codec.BCodec;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;

/**
 * Writes BStorables to a given output stream. Internally it uses a
 * CodecWriteChannel
 * 
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 * @see CodecWriteChannel
 */
public class CodecOutputStream <A> {

  private CodecWriteChannel <A> mChannel;

  /**
   * Initialize to write BStorables to a given output.
   * 
   * @param os
   *          stream to write to
   * @param codec instance codec
   * @exception IOException
   *              if an error occurs
   */
  public CodecOutputStream(OutputStream os, BCodec<A> codec) throws IOException {
    mChannel = new CodecWriteChannel(Channels.newChannel(os), codec);
  }

  /**
   * Write a BStorable to the stream.
   * 
   * @param value
   *          the next value to write
   * @exception IOException
   *              if an error occurs
   */
  public void write(A value) throws IOException {
    mChannel.write(value);
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
