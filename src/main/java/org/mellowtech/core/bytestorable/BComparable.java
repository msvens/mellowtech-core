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

import java.nio.ByteBuffer;

/**
 * An extension to BStorable that adds ordering to BStorables. Notably implementing
 * classes can implement methods for comparing serialized objects.
 *
 * <p>
 * Typically a user of this library would use any of the predefined BComparables and
 * not implement this interface directly
 * </p>
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 *
 * @param <A> type of value
 * @param <B> self type
 * @see BStorable
 *
 */
public interface BComparable <A,B extends BComparable<A,B>> extends BStorable <A,B>, Comparable <B> {

  /**
   * Default implementation of compareTo casts the value to a Comparable
   * and compares them
   * <pre>
   * {@code
   *  Comparable <? super A> cmp = (Comparable <? super A>) this.get();
   *  return cmp.compareTo(other.get)
   * }
   * </pre>
   *
   * @param other - object to compare to
   * @return see Comparable
   */
  @Override
  default int compareTo(B other){
    @SuppressWarnings("unchecked")
    Comparable <? super A> cmp = (Comparable <? super A>) this.get();
    return cmp.compareTo(other.get());
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
    bb1.position(offset1);
    B b1 = this.from(bb1);
    bb2.position(offset2);
    B b2 = this.from(bb2);
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

}
