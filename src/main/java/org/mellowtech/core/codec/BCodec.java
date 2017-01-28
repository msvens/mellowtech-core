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

package org.mellowtech.core.codec;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Interface that defines base functionality for objects that can be
 * transformed to and from bytes.
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 4.0.0
 *
 * @param <A> type of value
 */
public interface BCodec<A> {


  default int compare(A first, A second){
    return ((Comparable <? super A>)first).compareTo(second);
  }
  /**
   * Compares two objects that are represented as bytes in a ByteBuffer. calls
   * <pre>
   * {@code
   *  return byteCompare(offset1, bb, offset2, bb);
   * }
   * </pre>
   * @param offset1
   *          the offset in the buffer for the first object
   * @param offset2
   *          the offset in the buffer for the second object
   * @param bb
   *          the buffer that holds the object
   * @return a negative integer, zero, or a positive integer as the first
   *         argument is less than, equal to, or greater than the second.
   */
  default int byteCompare(int offset1, int offset2, ByteBuffer bb) {
    return byteCompare(offset1, bb, offset2, bb);
  }

  /**
   * Compares two objects that are represented as bytes in a ByteBuffers.
   * Default implementation reads the objects and call their compareTo method.
   * <p>
   * <b>Subclasses should override this</b>
   * </p>
   * @param offset1
   *          the offset in the buffer for the first object
   * @param bb1
   *          buffer for the first object
   * @param offset2
   *          the offset in the buffer for the second object
   * @param bb2
   *          buffer for the second object
   * @return a negative integer, zero, or a positive integer as the first
   *         argument is less than, equal to, or greater than the second.
   */
  default int byteCompare(int offset1, ByteBuffer bb1, int offset2,
                          ByteBuffer bb2){
    //throw new UnsupportedOperationException("unsupported operation");
    bb1.position(offset1);
    @SuppressWarnings("unchecked")
    Comparable <? super A> b1 = (Comparable <? super A>) from(bb1);
    bb2.position(offset2);
    A b2 = from(bb2);
    return b1.compareTo(b2);
  }

  /**
   * Compares two objects that are represented as bytes in a byte array. Default implementation
   * calls
   * <pre>
   * {@code return byteCompare(offset1, b, offset2, b);}
   * </pre>
   *
   * @param offset1
   *          the offset in the buffer for the first object
   * @param offset2
   *          the offset in the buffer for the second object
   * @param b
   *          the buffer that holds the object
   * @return a negative integer, zero, or a positive integer as the first
   *         argument is less than, equal to, or greater than the second.
   */
  default int byteCompare(int offset1, int offset2, byte[] b) {
    return byteCompare(offset1, b, offset2, b);
  }

  /**
   * Compares two objects that are represented as bytes in a byte arrays.
   * Default implementation wraps the byte arrays to ByteBuffers and calls
   * <pre>
   *   {@code return byteCompare(offset1, ByteBuffer.wrap(b1), offset2, ByteBuffer.wrap(b2)}
   * </pre>
   *
   * @param offset1
   *          the offset in the buffer for the first object
   * @param b1
   *          array that holds the first object
   * @param offset2
   *          the offset in the buffer for the second object
   * @param b2
   *          array that holds the second object
   * @return a negative integer, zero, or a positive integer as the first
   *         argument is less than, equal to, or greater than the second.
   */
  default int byteCompare(int offset1, byte[] b1, int offset2, byte[] b2) {
    return byteCompare(offset1, ByteBuffer.wrap(b1), offset2, ByteBuffer
        .wrap(b2));
  }

  /**
   * Compares two object thar are represented as bytes
   * Default implementation calls
   * <pre>
   *   {@code return byteCompare(offset1, ByteBuffer.wrap(b1), offset2, bb2);}
   * </pre>
   * @param offset1 offset in the array for the first object
   * @param b1 array that holds the first object
   * @param offset2 offset int the buffer for the second object
   * @param bb2 buffer that holds the second object
   * @return a negative integer, zero, ora a positive integer
   */
  default int byteCompare(int offset1, byte[] b1, int offset2, ByteBuffer bb2) {
    return byteCompare(offset1, ByteBuffer.wrap(b1), offset2, bb2);
  }

  /**
   * Compares two object thar are represented as bytes
   * Default implementation calls
   * <pre>
   *   {@code return byteCompare(offset1, bb1, offset2, ByteBuffer.wrap(b2));}
   * </pre>
   * @param offset1 offset in the buffer for the first object
   * @param bb1 buffer that holds the first object
   * @param offset2 offset int the array for the second object
   * @param b2 array that holds the second object
   * @return a negative integer, zero, ora a positive integer
   */
  default int byteCompare(int offset1, ByteBuffer bb1, int offset2, byte[] b2) {
    return byteCompare(offset1, bb1, offset2, ByteBuffer.wrap(b2));
  }

  /**
   * Size of serialized instance (including any size indicators)
   * @param a instance to serialize
   * @return size in bytes
   */
  int byteSize(A a);

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
   * Deep copy of this instance
   * @return new instance
   */
  default A deepCopy(A value){
    ByteBuffer bb = to(value);
    bb.flip();
    return from(bb);
  }

  /**
   * Deserialize and return a new instance of
   * this type
   * @param bb buffer to read from
   * @return new instance
   */
  A from(ByteBuffer bb);

  /**
   * Deserialize and return a new instance of
   * this type
   * @param b byte array to read from
   * @param offset offset in the array
   * @return new instance
   */
  default A from(byte[] b, int offset) {
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
  default A from(InputStream is) throws IOException{
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
  default A from(ReadableByteChannel rbc) throws IOException{
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
   * Indicate if the byte size of this type (e.g. integers) is fixed, defaults
   * to false
   * @return true if the byte size is fixed
   */
  default boolean isFixed() {return false;}

  default int fixedSize(){
    return 0;
  }

  /**
   * Serialize an object to buffer starting at
   * current position
   * @param a instance to serialize
   * @param bb buffer to write to
   */
  void to(A a, ByteBuffer bb);

  /**
   * Serialize instance to a new buffer
   * @param value object to serialize
   * @return new ByteBuffer
   */
  default ByteBuffer to(A value) {
    ByteBuffer bb = ByteBuffer.allocate(byteSize(value));
    to(value, bb);
    return bb;
  }

  /**
   * Serialize instance to byte array
   * @param value object to serialize
   * @param b array to write to
   * @param offset start offset
   * @return bytes written
   */
  default int to(A value, byte[] b, int offset) {
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.position(offset);
    to(value, bb);
    return bb.position() - offset;
  }

  /**
   * Serialize instance to stream
   * @param value object to serialize
   * @param os stream to write to
   * @return bytes written
   * @throws IOException if underlying stream throws an exception
   */
  default int to(A value, OutputStream os) throws IOException{
    int byteSize = byteSize(value);
    byte[] b = new byte[byteSize];
    to(value, b, 0);
    for (byte aB : b) os.write(aB);
    return byteSize;
  }

  /**
   * Serialize instance to channel
   * @param value object to serialize
   * @param wbc channel to write to
   * @return bytes written
   * @throws IOException if the underlying channel throws an exception
   */
  default int to(A value, WritableByteChannel wbc) throws IOException {
    int byteSize = byteSize(value);
    ByteBuffer bb = ByteBuffer.allocate(byteSize);
    to(value, bb);
    bb.flip();
    wbc.write(bb);
    return byteSize;
  }
}
