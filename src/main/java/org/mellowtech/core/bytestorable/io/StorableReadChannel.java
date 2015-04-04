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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import org.mellowtech.core.bytestorable.ByteStorable;

/**
 * Reads ByteStorables from a given byte channel.
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class StorableReadChannel <E extends ByteStorable <?>> {

  private E mTemplate;
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
  public StorableReadChannel(ReadableByteChannel rbc, E template) {
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
  public E next() throws IOException {
    int read;
    int slack = ByteStorable.slackOrSize(mBuffer, mTemplate);
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
        ByteStorable.copyToBeginning(mBuffer, slack);

      if (mRbc.read(mBuffer) < 0) {
        endOfChannel = true;
        return null;
      }
      mBuffer.flip();

      slack = ByteStorable.slackOrSize(mBuffer, mTemplate);
    }
    return (E) mTemplate.fromBytes(mBuffer);
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
   * Install a new template, may be needed for optimised conditions.
   * 
   * @olds the new template.
   */
  public void setTemplate(E pTemplate) {
    mTemplate = pTemplate;
  } // setTemplate
}
