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
import org.mellowtech.core.codec.CodecUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Reads BStorables from a given byte channel.
 * @param <A> Wrapped BStorable class
 * 
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 */
public class CodecReadChannel<A> {

  private BCodec<A> codec;
  private ReadableByteChannel mRbc;
  private ByteBuffer mBuffer;
  private boolean endOfChannel = false;

  /**
   * Initialize to read BStorable from a given channel using the specified
   * template.
   * 
   * @param rbc
   *          channel to read from
   * @param codec
   *          a ByteStorable template
   */
  public CodecReadChannel(ReadableByteChannel rbc, BCodec<A> codec) {
    this.codec = codec;
    mRbc = rbc;
    mBuffer = ByteBuffer.allocate(4096 * 4);
    mBuffer.flip();
  }

  /**
   * Checks if this channel is open.
   * 
   * @return true if this channel is open
   * @see ReadableByteChannel#isOpen()
   */
  public boolean isOpen() {
    return mRbc.isOpen();
  }

  /**
   * Closes this channel (and thus the supplied backing channel)
   * 
   * @exception IOException
   *              if an error occurs
   * 
   */
  public void close() throws IOException {
    mRbc.close();
  }

  /**
   * Retrieve the next BStorable in the channel.
   * 
   * @return the next object or null if there are no more to read.
   * @exception IOException
   *              if an error occurs
   */
  public A next() throws IOException {
    //int read;
    int slack = CodecUtil.slackOrSize(mBuffer, codec);
    // if(slack <= 0)
    while (slack <= 0) {
      slack = Math.abs(slack);

      // JC Note! slackOrSize returns -left if buffer.remaining() < 4.
      // This causes problems for objects that have a size as the buffersize
      // (down
      // ..to 3 bytes less). So we compensate for the 4 bytes criterion
      // ..in the check below and in the resize of the buffer.
      // ..slackOrSize requires 4 bytes so it is always certain that an int (the
      // objects
      // ..size parameter) can be stored. But this must be compensated for!
      if (slack > mBuffer.capacity() - 4) {
        // Make sure the object and the additional size of an int (although
        // maybe not used)
        // ..can fit into the buffer so sizeOrSlack() returns as it should.
        ByteBuffer tmp = ByteBuffer.allocate(slack + 4);
        tmp.put(mBuffer);
        mBuffer = tmp;
      }
      else
        CodecUtil.copyToBeginning(mBuffer, slack);

      if (mRbc.read(mBuffer) < 0) {
        endOfChannel = true;
        return null;
      }
      mBuffer.flip();

      slack = CodecUtil.slackOrSize(mBuffer, codec);
    }
    return codec.from(mBuffer);
  } // next

  /**
   * Checks if more BStorables can be read from the channel.
   * 
   * @return true if no more ByteStorables can be read.
   */
  public boolean isEndOfChannel() {
    return endOfChannel;
  }

}
