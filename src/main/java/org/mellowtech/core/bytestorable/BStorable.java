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
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 *
 * @param <A> type of value
 */
public interface BStorable <A> {

  /**
   * Size of serialized instance (including any size indicators)
   * @return size in bytes
   */
  int byteSize();

  /**
   * Size of serialized instance stored in buffer. A maixmum of
   * 4 bytes should be needed to determine the size.
   * <p>
   *   <b>Note: </b>any buffer position needs to be reset before
   *   returning
   * </p>
   * @param bb buffer to read from
   * @return size in bytes
   */
  int byteSize(ByteBuffer bb);

  /**
   * Create a new instance of this type
   * @param a valut to instantiate with
   * @return new instance
   */
  default BStorable<A> create(A a){
    try {
      @SuppressWarnings("unchecked")
      Constructor c = this.getClass().getConstructor(a.getClass());
      return (BStorable<A>) c.newInstance(a);
    }
    catch(Exception e){
      throw new ByteStorableException("no such constructor method");
    }
  }

  /**
   * Deep copy of this instance
   * @return new instance
   */
  default BStorable<A> deepCopy(){
    ByteBuffer bb = to();
    bb.flip();
    return from(bb);
  }

  /**
   * Deserialize and return a new instance of
   * this type
   * @param bb buffer to read from
   * @return new instance
   */
  BStorable<A> from(ByteBuffer bb);

  /**
   * Deserialize and return a new instance of
   * this type
   * @param b byte array to read from
   * @param offset offset in the array
   * @return new instance
   */
  default BStorable<A> from(byte[] b, int offset) {
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.position(offset);
    return from(bb);
  }

  /**
   * Deserialize and return a new instance of this type
   * @param is stream to read from
   * @return new instance
   * @throws IOException if unexpected end of stream
   */
  default BStorable<A> from(InputStream is) throws IOException{
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

  /**
   * Deserialize and return a new instance of this type
   * @param rbc channel to read from
   * @return new instance
   * @throws IOException if underlying channel throws exception
   */
  default BStorable<A> from(ReadableByteChannel rbc) throws IOException{
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
    bb1.flip();
    return this.from(bb1);
  }

  /**
   * Get the value that this BStorable holds
   * @return the value
   */
  A get();

  /**
   * Indicate if the byte size of this type (e.g. integers) is fixed, defaults
   * to false
   * @return true if the byte size is fixed
   */
  default boolean isFixed() {return false;}

  /**
   * Serialize this instance to buffer starting at
   * current position
   * @param bb buffer to write to
   */
  void to(ByteBuffer bb);

  /**
   * Serialize this instance to a new buffer
   * @return new ByteBuffer
   */
  default ByteBuffer to() {
    ByteBuffer bb = ByteBuffer.allocate(byteSize());
    to(bb);
    return bb;
  }

  /**
   * Serialize this instance to byte array
   * @param b array to write to
   * @param offset start offset
   * @return bytes written
   */
  default int to(byte[] b, int offset) {
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.position(offset);
    to(bb);
    return bb.position() - offset;
  }

  /**
   * Serialize this instance to stream
   * @param os stream to write to
   * @return bytes written
   * @throws IOException if underlying stream throws an exception
   */
  default int to(OutputStream os) throws IOException{
    int byteSize = byteSize();
    byte[] b = new byte[byteSize];
    to(b, 0);
    for (byte aB : b) os.write(aB);
    return byteSize;
  }

  /**
   * Serialize this instance to channel
   * @param wbc channel to write to
   * @return bytes written
   * @throws IOException if the underlying channel throws an exception
   */
  default int to(WritableByteChannel wbc) throws IOException {
    int byteSize = byteSize();
    ByteBuffer bb = ByteBuffer.allocate(byteSize);
    to(bb);
    bb.flip();
    wbc.write(bb);
    return byteSize;
  }

}
