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

import java.nio.ByteBuffer;

/**
 * ByteStore objects that are able to be compared on a byte level should
 * implement this interface. This allows for comparison of ByteStorables without
 * first converting them from their byte representation. This allows for, for
 * instance, disc based sorting to be done much more rapidly.
 * 
 * @author Martin Svensson
 * @version 1.0
 * @see com.asimus.bytestorable.ByteStorable
 */
public abstract class ByteComparable <T> extends ByteStorable <T>{

  /**
   * Compares two objects that are represented as bytes in a ByteBuffer. Calls:
   * byteCompare(offset1, bb, offset2, bb);
   * 
   * @param offset1
   *          the offset in the buffer for the first object
   * @param offset2
   *          the offset in the buffer for the second object
   * @param bb
   *          the buffer that holds the object
   * @return a negative integer, zero, or a positive integer as the first
   *         argument is less than, equal to, or greater than the second.
   */
  public int byteCompare(int offset1, int offset2, ByteBuffer bb) {
    return byteCompare(offset1, bb, offset2, bb);
  }

  /**
   * Compares two objects that are represented as bytes in a ByteBuffers.
   * Subclasses imp?ement this method
   * 
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
  public abstract int byteCompare(int offset1, ByteBuffer bb1, int offset2,
      ByteBuffer bb2);

  /**
   * Compares two objects that are represented as bytes in a byte array. Calls:<br/>
   * byteCompare(offset1, b, offset2, b)
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
  public int byteCompare(int offset1, int offset2, byte[] b) {
    return byteCompare(offset1, b, offset2, b);
  }

  /**
   * Compares two objects that are represented as bytes in a byte arrays.
   * Default implementation wraps the byte arrays to ByteBuffers and calls:
   * <br/> byteCompare(offset1, ByteBuffer.wrap(b1), offset2,
   * ByteBuffer.wrap(b2)
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
  public int byteCompare(int offset1, byte[] b1, int offset2, byte[] b2) {
    return byteCompare(offset1, ByteBuffer.wrap(b1), offset2, ByteBuffer
        .wrap(b2));
  }

  public int byteCompare(int offset1, byte[] b1, int offset2, ByteBuffer bb2) {
    return byteCompare(offset1, ByteBuffer.wrap(b1), offset2, bb2);
  }

  public int byteCompare(int offset1, ByteBuffer bb1, int offset2, byte[] b2) {
    return byteCompare(offset1, bb1, offset2, ByteBuffer.wrap(b2));
  }

}
