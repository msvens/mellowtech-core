/**
 * 
 */
package com.mellowtech.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author msvens
 *
 */
public class BBInputStream extends InputStream{

  Integer reset;
  private final ByteBuffer bb;
  
  public BBInputStream(ByteBuffer bb){
    this.bb = bb;
  }
  
  public BBInputStream(int size){
    this.bb = ByteBuffer.allocate(size);
  }
  
  @Override
  public int read(byte[] b) throws IOException {
    // TODO Auto-generated method stub
    return super.read(b);
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
    // TODO Auto-generated method stub
    super.reset();
  }

  @Override
  public int read() throws IOException {
    if(bb.hasRemaining()) return -1;
      return bb.get();
  }

}
