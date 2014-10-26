/**
 * 
 */
package com.mellowtech.core.bytestorable;

import java.nio.ByteBuffer;

/**
 * @author msvens
 *
 */
public class StorableArrays {

  /**
   * 
   */
  public StorableArrays() {}
    /**
     * Get size of array. An array is stored with a four bytes length indicator at
     * the beginning of the byte buffer. The byte size of a short array is
     * therefore 4 + length*2.
     * <p>
     * 
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
    public static int getByteStorableArrayByteSize(ByteStorable <?> [] array) {
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
    public static void putByteStorableArray(ByteBuffer bb, ByteStorable <?> [] array) {
      // Increase position.
      int startpos = bb.position();
      bb.position(bb.position() + 4);

      bb.putInt(array == null ? 0 : array.length);
      for (int i = 0; array != null && i < array.length; i++)
        bb.put((ByteBuffer) array[i].toBytes().flip());

      // Store the size in the first four bytes
      int endpos = bb.position();
      int bytes = endpos - startpos;
      bb.putInt(startpos, bytes);
      bb.position(endpos);
    }

    /**
     * Get an array of ByteStorables from a byte buffer. Moves the byte buffer
     * pointer
     * 
     * @param bb
     *          the byte buffer containing the buffer
     * @param template
     *          the template for the specific ByteStorable
     * @param arrayTemplate
     *          the template for the array type
     * @return an array of ByteStorables
     */
    public static ByteStorable <?> [] getByteStorableArray(ByteBuffer bb,
        ByteStorable <?> [] arrayTemplate, ByteStorable <?> template) {
      int bytes = bb.getInt();
      int size = bb.getInt();
      if (size <= 0)
        return null;

      ByteStorable <?> [] array;
      Class <?> arrayClass = arrayTemplate.getClass().getComponentType();
      array = (ByteStorable[]) java.lang.reflect.Array.newInstance(arrayClass,
          size);
      for (int i = 0; i < size; i++)
        array[i] = template.fromBytes(bb);
      return array;
    }
    



}
