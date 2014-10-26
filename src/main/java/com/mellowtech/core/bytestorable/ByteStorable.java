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
package com.mellowtech.core.bytestorable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * The disc API rely to a large extent of the ability for Objects to represent
 * themselves as bytes to ensure fast and compact transfer to and from streams,
 * files, and channels. Objects that wish to access the functionality within the
 * disc api needs to subcass the ByteStorable.
 * 
 * A byte encoded ByteStorable object must be able to determine the total number
 * of bytes it occupies from maximum the first four bytes of the encoded object.
 * This means that if a complex dynamic object is written to bytes it will need
 * a length indicator in the first four bytes
 * <p>
 * If subclasses intend to use the methods that handle byte arrays and streams
 * they are strongly encouraged to overwrite these methods since they (in the
 * default implementation) only converts the byte arrays to ByteBuffers and
 * calls the appropriate methods.
 * </p>
 * <p>
 * <b></b>
 * </p>
 * <p>
 * The ByteStorable should not be confused withe the Serializable interface
 * found in the java.io package. However, if serializabiliy are to be used in
 * any subclass of ByteStorable it is probably a good idea to implement
 * </p>
 * 
 * <PRE>
 * 
 * private void writeObject(java.io.ObjectOutputStream out) throws IOException
 * private void readObject(java.io.ObjectInputStream in) throws IOException,
 * ClassNotFoundException;
 * 
 * </PRE>
 * 
 * <p>
 * since the conversion to and from bytes is already implemented.
 * </p>
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public abstract class ByteStorable <T> implements Comparable <ByteStorable<T>>{

  protected boolean doNew = true;
  protected T obj;

  public ByteStorable <T> deepCopy(){
    ByteBuffer bb = toBytes();
    bb.flip();
    return fromBytes(bb, true);
  }

  /**
   * @param doNew
   */
  public void setDefaultCreateType(boolean doNew) {
    this.doNew = doNew;
  }

  /**
   * @return
   */
  public boolean getDefaultCreateType() {
    return doNew;
  }

  /**
   * This default implementation just returns an exception. Subclasses has to
   * overwrite this method to be "comparable".
   * 
   * @param t
   *          an <code>Object</code> to compare with
   * @exception UnsupportedOperationException
   *              if an error occurs
   */
  public int compareTo(ByteStorable<T> t) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates a new ByteStorable object from a buffer of bytes.
   * 
   * @param bb
   *          The current buffer.
   * @param doNew
   *          if true this method should return a fresh instance
   * @return a new ByteStorable object.
   */
  public abstract ByteStorable <T> fromBytes(ByteBuffer bb, boolean doNew);

  /**
   * Creates a new ByteStorable object from a buffer of bytes. Depending on the
   * create mode this method should either return a new instance or fill the
   * current instance.
   * 
   * @param bb
   *          The current buffer.
   * @return a new ByteStorable object.
   */
  public ByteStorable <T> fromBytes(ByteBuffer bb) {
    return fromBytes(bb, doNew);
  }

  /**
   * Creates a new ByteStorable object from a buffer of bytes. The default
   * implementation converts the array into a ByteBuffer and calls fromBytes,
   * i.e<br>
   * 
   * <pre>
   * ByteBuffer bb = ByteBuffer.wrap(b);
   * bb.position(offset);
   * return fromBytes(bb);
   * </pre>
   * 
   * </p>
   * <p>
   * Subclasses are encouraged to overwrite this method if it is heavily used.
   * </p>
   * 
   * @param b
   *          a buffer to read from
   * @param offset
   *          an offset within the buffer
   * @param doNew
   *          if true this method should return a fresh instance
   * @return a new ByteStorable instance
   */
  public ByteStorable <T> fromBytes(byte b[], int offset, boolean doNew) {
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.position(offset);
    return fromBytes(bb, doNew);
  }

  public ByteStorable <T> fromBytes(byte b[], int offset) {
    return fromBytes(b, offset, doNew);
  }

  /**
   * Retrieves a byteStorable from an inputStream. The default implementation requires that
   * the length of the byteStorable is at least 4 bytes;
   * 
   * @param is stream to read from
   * @return a new ByteStorable instance
   * @exception UnsupportedOperationException
   *              if an error occurs
   */
  public ByteStorable <T> fromBytes(InputStream is)
      throws IOException {
    return fromBytes(is, doNew);
  }

  
  public ByteStorable <T> fromBytes(InputStream is, boolean doNew)
  	throws IOException{
  	ByteBuffer bb = ByteBuffer.allocate(4);
  	int b;
  	int i;
  	for(i = 0; i < 4; i++){
  		b = is.read();
  		if(b == -1)
  			throw new IOException("Unexpected end of stream: read size");
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
  	return this.fromBytes(bb1, doNew);
  }

  /**
   * Optional operation. Creates a new ByteStorable from a channel. The reason
   * this is not implemented is because it is difficult to find out how many
   * bytes should be read (and read just those) without using "push back"
   * functionality.
   * 
   * @param rbc
   *          channel to read from
   * @return a new ByteStorable instance
   * @exception UnsupportedOperationException
   *              if an error occurs
   */
  public ByteStorable <T> fromBytes(ReadableByteChannel rbc)
      throws UnsupportedOperationException {
    return fromBytes(rbc, doNew);
  }

  public ByteStorable <T> fromBytes(ReadableByteChannel rbc, boolean doNew)
      throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * Writes this ByteStorable to a ByteBuffer. Observe that calculating the
   * byteSize from a buffer should only require a maximum of four bytes of a
   * ByteStorable that has been transferred to bytes.
   * 
   * @param bb
   *          a buffer to store the byte representation
   * @see #byteSize(ByteBuffer)
   */
  public abstract void toBytes(ByteBuffer bb);

  /**
   * Writes this ByteStorable to a ByteBuffer. The ByteBuffer is allocated to
   * byteSize() bytes.
   * 
   * @return the byte buffer
   * @see #byteSize(ByteBuffer)
   */
  public ByteBuffer toBytes() {
    ByteBuffer bb = ByteBuffer.allocate(byteSize());
    toBytes(bb);
    return bb;
  }

  /**
   * Writes this ByteStorable to a ByteBuffer. If this method is to used
   * frequently subclasses are encouraged to overwrite it since the default
   * implementation only wraps the array in a ByteBuffer and calls
   * toBytes(ByteBuffer).
   * <p>
   * 
   * <pre>
   * ByteBuffer bb = ByteBuffer.wrap(b);
   * bb.position(offset);
   * toBytes(bb);
   * return bb.position() - offset;
   * </pre>
   * 
   * </p>
   * 
   * @param b
   *          a buffer
   * @param offset
   *          offset where to start write
   * @return the number of bytes written
   * @see #toBytes(ByteBuffer)
   */
  public int toBytes(byte[] b, int offset) {
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.position(offset);
    toBytes(bb);
    return bb.position() - offset;
  }

  /**
   * Writes this ByteStorable to an output stream. If this method is to used
   * frequently subclasses are encouraged to overwrite it since the default
   * implementation only wraps the array in a ByteBuffer and calls
   * toBytes(ByteBuffer).
   * <p>
   * 
   * <pre>
   * byte[] b = new byte[byteSize()];
   * toBytes(b, 0);
   * os.write(b);
   * </pre>
   * 
   * </p>
   * 
   * @param os
   *          an <code>OutputStream</code> value
   * @return the number of bytes written
   * @exception IOException
   *              if an error occurs
   * @see #toBytes(ByteBuffer)
   */
  public int toBytes(OutputStream os) throws IOException {
    int byteSize = byteSize();
    byte[] b = new byte[byteSize];
    toBytes(b, 0);
    for(int i = 0; i < b.length; i++)
    	os.write(b[i]);
    return byteSize;
  }

  /**
   * Writes this ByteStorable to an output stream. If this method is to be used
   * frequently subclasses are encouraged to overwrite it since the default
   * implementation only wraps the array in a ByteBuffer and calls
   * toBytes(ByteBuffer).
   * <p>
   * 
   * <pre>
   * ByteBuffer bb = ByteBuffer.allocate(byteSize());
   * toBytes(bb);
   * bb.flip();
   * wbc.write(bb);
   * </pre>
   * 
   * </p>
   * 
   * @param wbc
   *          a <code>WritableByteChannel</code> value
   * @return the number of bytes written
   * @exception IOException
   *              if an error occurs
   * @see #toBytes(ByteBuffer)
   */
  public int toBytes(WritableByteChannel wbc) throws IOException {
    int byteSize = byteSize();
    ByteBuffer bb = ByteBuffer.allocate(byteSize);
    toBytes(bb);
    bb.flip();
    wbc.write(bb);
    return byteSize;
  }

  /**
   * Retrieves the underlying object (if any) that this ByteStorable.
   * Default implementation returns null;
   * wraps;
   * @return
   */
  public T get(){
    return obj;
  }

  /**
   * Set the underlying object (if any) that this ByteStorable wraps.
   * Default implementation does nothing;
   * @param obj
   */
  public void set(T obj){
    this.obj = obj;
  }

  /**
   * Returns true if this byteStorable is of fixed length;
   * @return
   */
  public boolean isFixed(){
    return false;
  }

  /**
   * Calculates the current instance byte size, i.e the number of bytes it will
   * occupy (including any length indicator in the first four bytes).
   * 
   * @return the number of bytes
   */
  public abstract int byteSize();

  /**
   * From a buffer calculate how many bytes the written ByteStorable will
   * occupy. No more than four bytes can be used for this.
   * 
   * @param bb
   *          a <code>ByteBuffer</code> value
   * @return the number of bytes
   */
  public abstract int byteSize(ByteBuffer bb);

  /**
   * Same as above. Implementors are cncourage to overwrite this method if it is
   * used frequently, since the default implementation wraps the array into a
   * ByteBuffer.
   * <p>
   * 
   * <pre>
   * ByteBuffer bb = ByteBuffer.wrap(b);
   * bb.position(offset);
   * return byteSize(bb);
   * </pre>
   * 
   * </p>
   * 
   * @param b
   *          a <code>byte[]</code> value
   * @param offset
   *          an <code>int</code> value
   * @return an <code>int</code> value
   */
  public int byteSize(byte[] b, int offset) {
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.position(offset);
    return byteSize(bb);
  }

  /**
   * Returns the smallest separator between two ByteStorables. See CBString for
   * an implementation. By default separate returns a copy of the larger value.
   * 
   * @param first
   *          a <code>ByteStorable</code> value
   * @param second
   *          a <code>ByteStorable</code> value
   * @return a <code>ByteStorable</code> value
   */
  public ByteStorable <T> separate(ByteStorable <T> first, ByteStorable <T> second) {
    return first.separate(second);
  }
  
  public ByteStorable <T> separate(ByteStorable <T> second){
    ByteStorable <T> toRet = this.compareTo(second) > 0 ? this : second;
    return toRet;
  }

  /**
   * Starting at the current position copy a number bytes to the beginning of
   * the buffer and set the new postion to just after the copied bytes.
   * 
   * @param bb
   *          buffer
   * @param numBytes
   *          number of bytes to copy
   */
  public final static void copyToBeginning(ByteBuffer bb, int numBytes) {
    if (numBytes == 0) {
      bb.clear();
      return;
    }
    byte[] b = new byte[numBytes];
    bb.get(b);
    bb.clear();
    bb.put(b);
  }

  /**
   * Calculates the number of bytes that the next ByteStorable will need to be
   * fully read. If the buffer does not fully contain the next ByteStorable it
   * will return the number of bytes that are left in this buffer as a negative
   * value.
   * 
   * @param bb
   *          a buffer
   * @param template
   *          a ByteStorable template to be used for calculating the size
   * @return the next ByteStorable's size or -(bytes left in buffer)
   */
  public final static int slackOrSize(ByteBuffer bb, ByteStorable <?> template) {
    int left = bb.remaining();
    if (bb.remaining() < 4){
      return -left;
    }
    bb.mark();
    int bSize = template.byteSize(bb);
    bb.reset();
    if (bSize > left){
      return -left;
    }
    return bSize;
  }

  /**
   * Utility method for retriving the byte size when it is store as an
   * int in the first for bytes of the byte storable object
   * @param bb
   * @return the object size
   */
  public final static int getSizeFour(ByteBuffer bb){
    int size = bb.getInt();
    bb.position(bb.position() - 4);
    return size;
  }

  /**
   * Utility method for retrieving the byte size when it is stored
   * as a variable length int in the beginning of the byte storable object
   * @param bb
   * @return
   */
  public final static int getSizeVariable(ByteBuffer bb){
    int pos = bb.position();
    int length = getSize(bb);
    int sb = bb.position() - pos;
    bb.position(pos);
    return length + sb;
    //return length + sizeBytesNeeded(length);
  }

  /**
   * Calculate the number of bytes this num can be encoded in. That is, small
   * number will required less bytes with variable encoding. Useful if you want
   * to store a size indicator.
   * 
   * @param num
   *          the number to encode
   * @return the number of bytes needed to encode the number
   * @see #putSize
   * @see #getSize
   */
  public static final int sizeBytesNeeded(int num) {
    int count = 1;
    num = (num >> 7);
    while (num > 0) {
      count++;
      num = num >> 7;
    }
    return count;
  }

  public static final int sizeBytesNeeded(long num) {
    int count = 1;
    num = (num >> 7);
    while (num > 0) {
      count++;
      num = num >> 7;
    }
    return count;
  }

  /**
   * Encodes a number to a buffer. Uses less bytes for smaller numbers.
   * 
   * @param size
   *          the number to encode
   * @param bb
   *          a buffer to store the encoded value
   * @return the number of bytes written
   * @see #getSize
   */
  public static final int putSize(int size, ByteBuffer bb) {
    int c, count = 1;
    c = (size & 0x7F);
    size = (size >> 7);
    while (size > 0) {
      bb.put((byte) (c & 0xFF));
      c = (size & 0x7F);
      size = size >> 7;
      count++;
    }
    c = (c | 0x80);
    bb.put((byte) (c & 0xFF));
    return count;
  }

  /**
   * Encodes a number to a buffer. Uses less bytes for smaller numbers.
   * 
   * @param size
   *          the number to encode
   * @param bb
   *          a buffer to store the encoded value
   * @param offset
   *          offset in buffer
   * @return the number of bytes written
   * @see #getSize
   */
  public static final int putSize(int size, byte[] bb, int offset) {
    int c, count = 1;
    c = (size & 0x7F);
    size = (size >> 7);
    while (size > 0) {
      bb[offset++] = ((byte) (c & 0xFF));
      c = (size & 0x7F);
      size = size >> 7;
      count++;
    }
    c = (c | 0x80);
    bb[offset] = ((byte) (c & 0xFF));
    return count;
  }

  public static int putSize(long size, byte[] bb, int offset) {
    long c;
    int count = 1;

    c = (size & 0x7F);
    size = (size >> 7);
    while (size > 0) {
      bb[offset++] = ((byte) (c & 0xFF));
      c = (size & 0x7F);
      size = size >> 7;
      count++;
    }
    c = (c | 0x80);
    bb[offset] = ((byte) (c & 0xFF));
    return count;
  } // putBigSize

  public static final int putSize(long size, ByteBuffer bb) {
    long c;
    int count = 1;
    c = (size & 0x7F);
    size = (size >> 7);
    while (size > 0) {
      bb.put((byte) (c & 0xFF));
      c = (size & 0x7F);
      size = size >> 7;
      count++;
    }
    c = (c | 0x80);
    bb.put((byte) (c & 0xFF));
    return count;
  }

  /**
   * Decodes a previously encoded number.
   * 
   * @param bb
   *          the buffer to read from
   * @return the encoded value
   * @see #putSize
   */
  public static final int getSize(ByteBuffer bb) {
    int c, num = 0, i = 0;

    c = (bb.get() & 0xFF);
    while ((c & 0x80) == 0) {
      num |= (c << (7 * i));
      c = (bb.get() & 0xFF);
      i++;
    }
    num |= ((c & ~(0x80)) << (7 * i));
    return num;
  }

  /**
   * Decodes a previously encoded number.
   * 
   * @param bb
   *          the buffer to read from
   * @param offset
   *          offset in buffer
   * @return the encoded value
   * @see #putSize
   */
  public static final int getSize(byte[] bb, int offset) {
    int c, num = 0, i = 0;

    c = (bb[offset++] & 0xFF);
    while ((c & 0x80) == 0) {
      num |= (c << (7 * i));
      c = (bb[offset++] & 0xFF);
      i++;
    }
    num |= ((c & ~(0x80)) << (7 * i));
    return num;
  }

  public static long getSizeLong(byte[] bb, int offset) {
    long c;
    long num = 0, i = 0;

    c = (bb[offset++] & 0xFF);
    while ((c & 0x80) == 0) {
      num |= (c << (7 * i));
      c = (bb[offset++] & 0xFF);
      i++;
    }
    num |= ((c & ~(0x80)) << (7 * i));
    return num;
  } // getBigSize

  public static final long getSizeLong(ByteBuffer bb) {
    long c, i = 0;
    long num = 0;

    c = (bb.get() & 0xFF);
    while ((c & 0x80) == 0) {
      num |= (c << (7 * i));
      c = (bb.get() & 0xFF);
      i++;
    }
    num |= ((c & ~(0x80)) << (7 * i));
    return num;
  }
  
  public String toString() {
    return get() != null ? get().toString() :  super.toString();
}




}
