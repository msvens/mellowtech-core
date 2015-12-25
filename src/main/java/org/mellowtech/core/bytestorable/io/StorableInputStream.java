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
import java.io.InputStream;
import java.nio.channels.Channels;

import org.mellowtech.core.bytestorable.BStorable;

/**
 * Reads ByteStorables from a given input stream. Internally it uses a
 * StorableReadChannel
 * @param <A> Wrapped BStorable class
 * @param <B> BStorable class
 * 
 * @author Martin Svensson
 * @version 1.0
 * @see StorableReadChannel
 */
public class StorableInputStream <A, B extends BStorable <A,B>> {

  private StorableReadChannel <A,B> mChannel;

  /**
   * Initialize to read ByteStorables from a given input stream using the
   * specified template.
   * 
   * @param is
   *          stream to read from
   * @param template
   *          a ByteStorable template
   * @exception IOException
   *              if an error occurs
   */
  public StorableInputStream(InputStream is, B template)
      throws IOException {
    mChannel = new StorableReadChannel <A,B> (Channels.newChannel(is), template);
  }

  /**
   * Retrieve the next ByteStorable in the stream.
   * 
   * @return the next object or null if there are no more to read.
   * @exception IOException
   *              if an error occurs
   */
  public B next() throws IOException {
    return mChannel.next();
  }
  
  /**
   * Sets the template to use, useful when same stream is used to read different objects
   * according some rules.
   * @param template BStorable template
   */
  public void setTemplate(B template) {
	  mChannel.setTemplate(template);
  }
}
