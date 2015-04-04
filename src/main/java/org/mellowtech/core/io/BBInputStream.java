/**
 * 
 */
package org.mellowtech.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
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
