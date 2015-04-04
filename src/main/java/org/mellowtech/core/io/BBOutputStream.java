/**
 * 
 */
package org.mellowtech.core.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
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
