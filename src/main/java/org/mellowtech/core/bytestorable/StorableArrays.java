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

//TODO: Simpliy or deprecate
/**
 * Reading and Writing various arrays to and from ByteBuffers.
 *
 * @author Rickard Cöster {@literal <rickard.coster@asimus.se>}, Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 * @see java.util.BitSet
 *
 */
public class StorableArrays {

  public StorableArrays() {}
  
    /**
     * Get size of array. An array is stored with a four bytes length indicator at
     * the beginning of the byte buffer. The byte size of a short array is
     * therefore 4 + length*2.
     * <p>
     * This method does not change the ByteBuffer position.
     * 
     * @param bb
     *          a <code>ByteBuffer</code> value
     * @return the size (in bytes) of the array, including the four bytes for the
     *         size indicator
     */
    public static int getShortArrayByteSize(ByteBuffer bb) {
      int size = bb.getInt();
      bb.position(bb.position() - 4);
      return 4 + size * 2;
    }

    /**
     * Get size of array. An array is stored with a four bytes length indicator at
     * the beginning of the byte buffer. The byte size of a short array is
     * therefore 4 + length*2.
     * <p>
     * 
     * @param array
     *          the array
     * @return the size (in bytes) of the array, including the four bytes for the
     *         size indicator
     */
    public static int getShortArrayByteSize(short[] array) {
      return 4 + (array == null ? 0 : array.length * 2);
    }

    /**
     * Store an array of shorts in a byte buffer. Moves the byte buffer pointer.
     * 
     * @param bb
     *          the buffer used for storage
     * @param array
     *          the array to store
     */
    public static void putShortArray(ByteBuffer bb, short[] array) {
      bb.putInt(array == null ? 0 : array.length);
      for (int i = 0; array != null && i < array.length; i++)
        bb.putShort(array[i]);
    }

    /**
     * Get an array of shorts from a byte buffer. Moves the byte buffer pointer
     * 
     * @param bb
     *          the byte buffer containing the buffer
     * @return an array of shorts
     */
    public static short[] getShortArray(ByteBuffer bb) {
      int size = bb.getInt();
      if (size <= 0)
        return null;
      short[] array = new short[size];
      for (int i = 0; i < size; i++)
        array[i] = bb.getShort();
      return array;
    }

    /**
     * Get size of array. An array is stored with a four bytes length indicator at
     * the beginning of the byte buffer. The byte size of an integer array is
     * therefore 4 + length*4.
     * <p>
     * 
     * This method does not change the ByteBuffer position.
     * 
     * @param bb
     *          a <code>ByteBuffer</code> value
     * @return the size (in bytes) of the array, including the four bytes for the
     *         size indicator
     */
    public static int getIntArrayByteSize(ByteBuffer bb) {
      int size = bb.getInt();
      bb.position(bb.position() - 4);
      return 4 + size * 4;
    }

    /**
     * Get size of array. An array is stored with a four bytes length indicator at
     * the beginning of the byte buffer. The byte size of a int array is therefore
     * 4 + length*4.
     * <p>
     * 
     * @param array
     *          the array
     * @return the size (in bytes) of the array, including the four bytes for the
     *         size indicator
     */
    public static int getIntArrayByteSize(int[] array) {
      return 4 + (array == null ? 0 : array.length * 4);
    }

    /**
     * Store an array of ints in a byte buffer. Moves the byte buffer pointer.
     * 
     * @param bb
     *          the buffer used for storage
     * @param array
     *          the array to store
     */
    public static void putIntArray(ByteBuffer bb, int[] array) {
      bb.putInt(array == null ? 0 : array.length);
      for (int i = 0; array != null && i < array.length; i++)
        bb.putInt(array[i]);
    }

    /**
     * Get an array of ints from a byte buffer. Moves the byte buffer pointer
     * 
     * @param bb
     *          the byte buffer containing the buffer
     * @return an array of ints
     */
    public static int[] getIntArray(ByteBuffer bb) {
      int size = bb.getInt();
      if (size <= 0)
        return null;
      int[] array = new int[size];
      for (int i = 0; i < size; i++)
        array[i] = bb.getInt();
      return array;
    }

    /**
     * Get size of array. An array is stored with a four bytes length indicator at
     * the beginning of the byte buffer. The byte size of a float array is
     * therefore 4 + length*4.
     * <p>
     * 
     * This method does not change the ByteBuffer position.
     * 
     * @param bb
     *          a <code>ByteBuffer</code> value
     * @return the size (in bytes) of the array, including the four bytes for the
     *         size indicator
     */
    public static int getFloatArrayByteSize(ByteBuffer bb) {
      int size = bb.getInt();
      bb.position(bb.position() - 4);
      return 4 + size * 4;
    }

    /**
     * Get size of array. An array is stored with a four bytes length indicator at
     * the beginning of the byte buffer. The byte size of a float array is
     * therefore 4 + length*4.
     * <p>
     * 
     * @param array
     *          the array
     * @return the size (in bytes) of the array, including the four bytes for the
     *         size indicator
     */
    public static int getFloatArrayByteSize(float[] array) {
      return 4 + (array == null ? 0 : array.length * 4);
    }

    /**
     * Store an array of floats in a byte buffer. Moves the byte buffer pointer.
     * 
     * @param bb
     *          the buffer used for storage
     * @param array
     *          the array to store
     */
    public static void putFloatArray(ByteBuffer bb, float[] array) {
      bb.putInt(array == null ? 0 : array.length);
      for (int i = 0; array != null && i < array.length; i++)
        bb.putFloat(array[i]);
    }

    /**
     * Get an array of floats from a byte buffer. Moves the byte buffer pointer
     * 
     * @param bb
     *          the byte buffer containing the buffer
     * @return an array of floats
     */
    public static float[] getFloatArray(ByteBuffer bb) {
      int size = bb.getInt();
      if (size <= 0)
        return null;
      float[] array = new float[size];
      for (int i = 0; i < size; i++)
        array[i] = bb.getFloat();
      return array;
    }

    /**
     * Get size of array. An array is stored with a four bytes length indicator at
     * the beginning of the byte buffer. The byte size of a double array is
     * therefore 4 + length*8.
     * <p>
     * 
     * This method does not change the ByteBuffer position.
     * 
     * @param bb
     *          a <code>ByteBuffer</code> value
     * @return the size (in bytes) of the array, including the four bytes for the
     *         size indicator
     */
    public static int getDoubleArrayByteSize(ByteBuffer bb) {
      int size = bb.getInt();
      bb.position(bb.position() - 4);
      return 4 + size * 8;
    }

    /**
     * Get size of array. An array is stored with a four bytes length indicator at
     * the beginning of the byte buffer. The byte size of a double array is
     * therefore 4 + length*8.
     * <p>
     * 
     * @param array
     *          the array
     * @return the size (in bytes) of the array, including the four bytes for the
     *         size indicator
     */
    public static int getDoubleArrayByteSize(double[] array) {
      return 4 + (array == null ? 0 : array.length * 8);
    }

    /**
     * Store an array of doubles in a byte buffer. Moves the byte buffer pointer.
     * 
     * @param bb
     *          the buffer used for storage
     * @param array
     *          the array to store
     */
    public static void putDoubleArray(ByteBuffer bb, double[] array) {
      bb.putInt(array == null ? 0 : array.length);
      for (int i = 0; array != null && i < array.length; i++)
        bb.putDouble(array[i]);
    }

    /**
     * Get an array of doubles from a byte buffer. Moves the byte buffer pointer
     * 
     * @param bb
     *          the byte buffer containing the buffer
     * @return an array of doubles
     */
    public static double[] getDoubleArray(ByteBuffer bb) {
      int size = bb.getInt();
      if (size <= 0)
        return null;
      double[] array = new double[size];
      for (int i = 0; i < size; i++)
        array[i] = bb.getDouble();
      return array;
    }
    
    /**
     * Get size of ByteStorable array. An array is stored with the number of bytes
     * total in first four bytes. Then comes a four bytes length indicator, then
     * the array itself.
     * 
     * This method does not change the ByteBuffer position.
     * 
     * @param bb
     *          a <code>ByteBuffer</code> value
     * @return the size (in bytes) of the array, including the four bytes for the
     *         size indicator
     */
    public static int getByteStorableArrayByteSize(ByteBuffer bb) {
      int size = bb.getInt();
      bb.position(bb.position() - 4);
      return size;
    }

    /**
     * Get size of ByteStorable array. An array is stored with the number of bytes
     * total in first four bytes. Then comes a four bytes length indicator, then
     * the array itself.
     * 
     * @param array
     *          the array
     * @return the size (in bytes) of the array, including the four bytes for the
     *         size indicator
     */
    public static int getByteStorableArrayByteSize(BStorable <?,?> [] array) {
      int size = 4 + 4;
      for (int i = 0; array != null && i < array.length; i++)
        size += array[i].byteSize();
      return size;
    }

    /**
     * Store an array of ByteStorables in a byte buffer. Moves the byte buffer
     * pointer.
     * 
     * @param bb
     *          the buffer used for storage
     * @param array
     *          the array to store
     */
    public static void putByteStorableArray(ByteBuffer bb, BStorable <?,?> [] array) {
      // Increase position.
      int startpos = bb.position();
      bb.position(bb.position() + 4);

      bb.putInt(array == null ? 0 : array.length);
      for (int i = 0; array != null && i < array.length; i++)
        bb.put((ByteBuffer) array[i].to().flip());

      // Store the size in the first four bytes
      int endpos = bb.position();
      int bytes = endpos - startpos;
      bb.putInt(startpos, bytes);
      bb.position(endpos);
    }
    
    /**
     * Get an array of ByteStorables from a byte buffer. Moves the byte buffer
     * pointer
     * @param bb the byte buffer containing the buffer
     * @param arrayTemplate the template for the specific ByteStorable
     * @param template the template for the specific ByteStorable
     * @param <A> wrapped BComparable class
     * @param <B> BComparabble class
     * @return an array of ByteStorables
     */
    public static <A,B extends BStorable<A,B>> BStorable <A,B> [] getByteStorableArray(ByteBuffer bb,
        BStorable <A,B> [] arrayTemplate, BStorable <A,B> template) {
      bb.getInt();
      int size = bb.getInt();
      if (size <= 0)
        return null;

      BStorable <A,B> [] array;
      Class <?> arrayClass = arrayTemplate.getClass().getComponentType();
      array = (BStorable<A,B>[]) java.lang.reflect.Array.newInstance(arrayClass,
          size);
      for (int i = 0; i < size; i++)
        array[i] = template.from(bb);
      return array;
    }
    



}
