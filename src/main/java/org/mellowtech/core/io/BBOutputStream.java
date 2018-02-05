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

package org.mellowtech.core.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * ByteBuffer backed output stream
 * @author msvens
 *
 */
public class BBOutputStream extends OutputStream {
  
  private final ByteBuffer bb;

  public BBOutputStream(ByteBuffer bb) {
    this.bb = bb;
  }
  
  public BBOutputStream(int size) {
    this.bb = ByteBuffer.allocate(size);
  }

  @Override
  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    if(bb.remaining() < len) throw new IOException("underlying will overflow");
    bb.put(b, off, len);
  }

  @Override
  public void write(int b) throws IOException {
    if(bb.hasRemaining())
      bb.put((byte)b);
    else
      throw new IOException("underlying will overflow");
  }
  
  public void reset(){
    bb.clear();
  }
  
  public int available(){
    return bb.remaining();
  }

}
