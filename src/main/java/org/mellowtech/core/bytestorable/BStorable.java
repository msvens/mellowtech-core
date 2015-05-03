/**
 * 
 */
package org.mellowtech.core.bytestorable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Interface that defines base functionality for objects that can be
 * transformed to and from bytes.
 * 
 * @author Martin Svensson
 *
 * @param <A> type of the wrapped object
 * @param <B> self type
 */
public interface BStorable <A, B extends BStorable<A,B>> {
  
  
  public A get();
  
  public B from(ByteBuffer bb);
  
  public default B create(A t){
    try {
      Constructor <B> c = (Constructor<B>) this.getClass().getConstructor(t.getClass());
      return c.newInstance(t);
    }
    catch(Exception e){
      throw new ByteStorableException("no such constructor method");
    }
  }
  
  public default B from(byte[] b, int offset) {
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.position(offset);
    return from(bb);
  }
  
  public default B from(InputStream is) throws IOException{
      ByteBuffer bb = ByteBuffer.allocate(4);
      int b;
      int i;
      for(i = 0; i < 4; i++){
        b = is.read();
        if(b == -1)
          break;
        bb.put((byte)b);
      }
      bb.flip();
      int byteSize = this.byteSize(bb);
      ByteBuffer bb1 = ByteBuffer.allocate(byteSize);
      bb1.put(bb);
      for(; i < byteSize; i++){
        b = is.read();
        if(b == -1)
          throw new IOException("Unexpected end of stream: read object");
        bb1.put((byte)b);
      }
      bb1.flip();
      return this.from(bb1);
  }
  
  public default B from(ReadableByteChannel rbc) throws IOException{
    ByteBuffer bb = ByteBuffer.allocate(4);
    ByteBuffer one = ByteBuffer.allocate(1);
    //int b;
    int i;
    for(i = 0; i < 4; i++){
      int read = rbc.read(one);
      if(read == -1)
        break;
      bb.put(one.get(0));
      one.clear();
    }
    bb.flip();
    int byteSize = this.byteSize(bb);
    ByteBuffer bb1 = ByteBuffer.allocate(byteSize);
    bb1.put(bb);
    rbc.read(bb1);
    /*for(; i < byteSize; i++){
      b = is.read();
      if(b == -1)
        throw new IOException("Unexpected end of stream: read object");
      bb1.put((byte)b);
    }*/
    bb1.flip();
    return this.from(bb1);
  }
  
  public void to(ByteBuffer bb);
  
  public default ByteBuffer to() {
    ByteBuffer bb = ByteBuffer.allocate(byteSize());
    to(bb);
    return bb;
  }
  
  public default int to(byte[] b, int offset) {
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.position(offset);
    to(bb);
    return bb.position() - offset;
  }
  
  public default int to(OutputStream os) throws IOException{
    int byteSize = byteSize();
    byte[] b = new byte[byteSize];
    to(b, 0);
    for(int i = 0; i < b.length; i++)
      os.write(b[i]);
    return byteSize;
  }
  
  public default int to(WritableByteChannel wbc) throws IOException {
    int byteSize = byteSize();
    ByteBuffer bb = ByteBuffer.allocate(byteSize);
    to(bb);
    bb.flip();
    wbc.write(bb);
    return byteSize;
  }
  
  public int byteSize();
  public int byteSize(ByteBuffer bb);
  
  public default B deepCopy(){
    ByteBuffer bb = to();
    bb.flip();
    return from(bb);
  }
  
  default public boolean isFixed() {return false;}

}
