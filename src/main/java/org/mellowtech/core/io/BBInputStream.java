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
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Byte Buffer backed input stream
 *
 * @author msvens
 *
 */
public class BBInputStream extends InputStream{

  private final ByteBuffer bb;
  
  public BBInputStream(ByteBuffer bb){
    this.bb = bb;
  }
  
  public BBInputStream(int size){
    this.bb = ByteBuffer.allocate(size);
  }
  
  @Override
  public int read(byte[] b) throws IOException {
    return this.read(b, 0, b.length);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    int toRead = Math.min(len, available());
    if(toRead == 0) return -1;
    bb.get(b, off, toRead);
    return toRead;
  }

  @Override
  public long skip(long n) throws IOException {
    int toSkip = Math.min((int)n, available());
    bb.position(bb.position()+toSkip);
    return toSkip;
  }

  @Override
  public int available() throws IOException {
    return bb.remaining();
  }

  @Override
  public synchronized void reset() throws IOException {
    super.reset();
  }

  @Override
  public int read() throws IOException {
    if(bb.hasRemaining()) bb.get();
    return -1;
  }

}
