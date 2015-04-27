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
import java.io.OutputStream;
import java.nio.channels.Channels;

import org.mellowtech.core.bytestorable.BStorable;

/**
 * Writes ByteStorables to a given output stream. Internally it uses a
 * StorableWriteChannel
 * 
 * @author Martin Svensson
 * @version 1.0
 * @see StorableWriteChannel
 */
public class StorableOutputStream {

  private StorableWriteChannel mChannel;

  /**
   * Initialize to write ByteStorables to a given output.
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
   * Write a ByteStorable to the stream.
   * 
   * @param bs
   *          the next ByteStorable to write
   * @exception IOException
   *              if an error occurs
   */
  public void write(BStorable <?,?> bs) throws IOException {
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
