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
import java.nio.channels.ReadableByteChannel;

import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.bytestorable.CBUtil;

/**
 * Reads ByteStorables from a given byte channel.
 * @param <A> Wrapped BStorable class
 * @param <B> BStorable class
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class StorableReadChannel <A, B extends BStorable <A,B>> {

  private B mTemplate;
  private ReadableByteChannel mRbc;
  private ByteBuffer mBuffer;
  private boolean endOfChannel = false;

  /**
   * Initialize to read ByteStorables from a given channel using the specified
   * template.
   * 
   * @param rbc
   *          channel to read from
   * @param template
   *          a ByteStorable template
   */
  public StorableReadChannel(ReadableByteChannel rbc, B template) {
    mTemplate = template;
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
   * Retrieve the next ByteStorable in the channel.
   * 
   * @return the next object or null if there are no more to read.
   * @exception IOException
   *              if an error occurs
   */
  public B next() throws IOException {
    //int read;
    int slack = CBUtil.slackOrSize(mBuffer, mTemplate);
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
        CBUtil.copyToBeginning(mBuffer, slack);

      if (mRbc.read(mBuffer) < 0) {
        endOfChannel = true;
        return null;
      }
      mBuffer.flip();

      slack = CBUtil.slackOrSize(mBuffer, mTemplate);
    }
    return mTemplate.from(mBuffer);
  } // next

  /**
   * Checks if more ByteStorables can be read from the channel.
   * 
   * @return true if no more ByteStorables can be read.
   */
  public boolean isEndOfChannel() {
    return endOfChannel;
  }

  /**
   * Install a new template, may be needed for optimized conditions.
   * @param pTemplate BStorable template
   */
  public void setTemplate(B pTemplate) {
    mTemplate = pTemplate;
  } // setTemplate
}
