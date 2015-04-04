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
import java.io.InputStream;
import java.nio.channels.Channels;

import org.mellowtech.core.bytestorable.ByteStorable;

/**
 * Reads ByteStorables from a given input stream. Iternally it uses a
 * StorableReadChannel
 * 
 * @author Martin Svensson
 * @version 1.0
 * @see StorableReadChannel
 */
public class StorableInputStream <E extends ByteStorable <?>> {

  private StorableReadChannel <E> mChannel;

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
  public StorableInputStream(InputStream is, E template)
      throws IOException {
    mChannel = new StorableReadChannel <E> (Channels.newChannel(is), template);
  }

  /**
   * Retrieve the next ByteStorable in the stream.
   * 
   * @return the next object or null if there are no more to read.
   * @exception IOException
   *              if an error occurs
   */
  public E next() throws IOException {
    return mChannel.next();
  }
  
  /**
   * Sets the template to use, useful when same stream is used to read different objects
   * according some rules.
   */
  public void setTemplate(E template) {
	  mChannel.setTemplate(template);
  }
}
