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
package org.mellowtech.core.util;

/**
 * A set of static methods to convert arrays to and from Strings and to
 * increase/decrease the size of arrays.
 * 
 * @author rickard.coster@asimus.se
 * @author martin.svensson@asimus.se
 */
public final class ArrayUtils {

  /**
   * Converts a string of numbers to an integer array
   * yes
   * @param str
   *          String of numbers to convert
   * @param separator
   *          String that separates each number
   * @return an array of integers
   */
  public static int[] toIntArray(String str, String separator) {
    String[] fields = str.split(separator);
    int[] tmp = new int[fields.length];
    for (int i = 0; i < tmp.length; i++)
      tmp[i] = Integer.parseInt(fields[i]);
    return tmp;
  }

  /**
   * Converts a string of numbers to a short array
   * 
   * @param str
   *          String of numbers to convert
   * @param separator
   *          String that separates each number
   * @return an array of shorts
   */
  public static short[] toShortArray(String str, String separator) {
    String[] fields = str.split(separator);
    short[] tmp = new short[fields.length];
    for (int i = 0; i < tmp.length; i++)
      tmp[i] = Short.parseShort(fields[i]);
    return tmp;
  }

  /**
   * Converts a string of numbers to a long array
   * 
   * @param str
   *          String of numbers to convert
   * @param separator
   *          String that separates each number
   * @return an array of shorts
   */
  public static long[] toLongArray(String str, String separator) {
    String[] fields = str.split(separator);
    long[] tmp = new long[fields.length];
    for (int i = 0; i < tmp.length; i++)
      tmp[i] = Long.parseLong(fields[i]);
    return tmp;
  }

  /**
   * Converts a string of numbers to a byte array. Observe that a byte ranges
   * from -127 to 127
   * 
   * @param str
   *          String of numbers to convert
   * @param separator
   *          String that separates each number
   * @return an array of bytes
   */
  public static byte[] toByteArray(String str, String separator) {
    String[] fields = str.split(separator);
    byte[] tmp = new byte[fields.length];
    for (int i = 0; i < tmp.length; i++)
      tmp[i] = Byte.parseByte(fields[i]);
    return tmp;
  }

  /**
   * Convert a string of reals to a float array.
   * 
   * @param str
   *          of numbers to convert
   * @param separator
   *          String that separates each number
   * @return an array of floats
   */
  public static float[] toFloatArray(String str, String separator) {
    String[] fields = str.split(separator);
    float[] tmp = new float[fields.length];
    for (int i = 0; i < tmp.length; i++)
      tmp[i] = Float.parseFloat(fields[i]);
    return tmp;
  }

  /**
   * Convert a string of reals to a double array.
   * 
   * @param str
   *          of numbers to convert
   * @param separator
   *          String that separates each number
   * @return an array of doubles
   */
  public static double[] toDoubleArray(String str, String separator) {
    String[] fields = str.split(separator);
    double[] tmp = new double[fields.length];
    for (int i = 0; i < tmp.length; i++)
      tmp[i] = Double.parseDouble(fields[i]);
    return tmp;
  }

  /**
   * Converts a array of numbers into a String.
   * 
   * @param a
   *          the array to convert
   * @param separator
   *          string that separates each element
   * @return a String representation of the array
   */
  public static String asString(byte[] a, String separator) {
    StringBuffer sbuff = new StringBuffer();
    for (int i = 0; i < a.length; i++)
      sbuff.append(a[i] + separator);
    sbuff.setLength(sbuff.length() - 1);
    return sbuff.toString();
  }

  /**
   * Converts a array of numbers into a String.
   * 
   * @param a
   *          the array to convert
   * @param separator
   *          string that separates each element
   * @return a String representation of the array
   */
  public static String asString(short[] a, String separator) {
    StringBuffer sbuff = new StringBuffer();
    for (int i = 0; i < a.length; i++)
      sbuff.append(a[i] + separator);
    sbuff.setLength(sbuff.length() - 1);
    return sbuff.toString();
  }

  /**
   * Converts a array of numbers into a String.
   * 
   * @param a
   *          the array to convert
   * @param separator
   *          string that separates each element
   * @return a String representation of the array
   */
  public static String asString(int[] a, String separator) {
    StringBuffer sbuff = new StringBuffer();
    for (int i = 0; i < a.length; i++)
      sbuff.append(a[i] + separator);
    sbuff.setLength(sbuff.length() - 1);
    return sbuff.toString();
  }

  /**
   * Converts a array of numbers into a String.
   * 
   * @param a
   *          the array to convert
   * @param separator
   *          string that separates each element
   * @return a String representation of the array
   */
  public static String asString(long[] a, String separator) {
    StringBuffer sbuff = new StringBuffer();
    for (int i = 0; i < a.length; i++)
      sbuff.append(a[i] + separator);
    sbuff.setLength(sbuff.length() - 1);
    return sbuff.toString();
  }

  /**
   * Converts a array of reals into a String.
   * 
   * @param a
   *          the array to convert
   * @param separator
   *          string that separates each element
   * @return a String representation of the array
   */
  public static String asString(float[] a, String separator) {
    StringBuffer sbuff = new StringBuffer();
    for (int i = 0; i < a.length; i++)
      sbuff.append(a[i] + separator);
    sbuff.setLength(sbuff.length() - 1);
    return sbuff.toString();
  }

  /**
   * Converts a array of reals into a String.
   * 
   * @param a
   *          the array to convert
   * @param separator
   *          string that separates each element
   * @return a String representation of the array
   */
  public static String asString(double[] a, String separator) {
    StringBuffer sbuff = new StringBuffer();
    for (int i = 0; i < a.length; i++)
      sbuff.append(a[i] + separator);
    sbuff.setLength(sbuff.length() - 1);
    return sbuff.toString();
  }

  /**
   * Converts an array of Objects into a String.
   * 
   * @param a
   *          the array to convert
   * @param separator
   *          string that separates each element
   * @return a String representation of the array
   */
  public static String asString(Object[] a, String separator) {
    StringBuffer sbuff = new StringBuffer();
    for (int i = 0; i < a.length; i++)
      sbuff.append(a[i] + separator);
    sbuff.setLength(sbuff.length() - 1);
    return sbuff.toString();
  }

  /**
   * Increases or decreases the size of an array of bytes. If the new size is
   * smaller only newSize elements will be copied.
   * 
   * @param a
   *          the original array
   * @param newSize
   *          the size of the new array
   * @return a new array with the original elements in a copied to it
   */
  public static byte[] setSize(byte[] a, int newSize) {
    if (newSize < 0)
      throw new IllegalArgumentException("Negative array size");
    byte[] v = new byte[newSize];
    System.arraycopy(a, 0, v, 0, newSize > a.length ? a.length : newSize);
    return v;
  }

  /**
   * Increases or decreases the size of an array of chars. If the new size is
   * smaller only newSize elements will be copied.
   * 
   * @param a
   *          the original array
   * @param newSize
   *          the size of the new array
   * @return a new array with the original elements in a copied to it
   */
  public static char[] setSize(char[] a, int newSize) {
    if (newSize < 0)
      throw new IllegalArgumentException("Negative array size");
    char[] v = new char[newSize];
    System.arraycopy(a, 0, v, 0, newSize > a.length ? a.length : newSize);
    return v;
  }

  /**
   * Increases or decreases the size of an array of shors. If the new size is
   * smaller only newSize elements will be copied.
   * 
   * @param a
   *          the original array
   * @param newSize
   *          the size of the new array
   * @return a new array with the original elements in a copied to it
   */
  public static short[] setSize(short[] a, int newSize) {
    if (newSize < 0)
      throw new IllegalArgumentException("Negative array size");
    short[] v = new short[newSize];
    System.arraycopy(a, 0, v, 0, newSize > a.length ? a.length : newSize);
    return v;
  }

  /**
   * Increases or decreases the size of an array of ints. If the new size is
   * smaller only newSize elements will be copied.
   * 
   * @param a
   *          the original array
   * @param newSize
   *          the size of the new array
   * @return a new array with the original elements in a copied to it
   */
  public static int[] setSize(int[] a, int newSize) {
    if (newSize < 0)
      throw new IllegalArgumentException("Negative array size");
    int[] v = new int[newSize];
    System.arraycopy(a, 0, v, 0, newSize > a.length ? a.length : newSize);
    return v;
  }

  /**
   * Increases or decreases the size of an array of longs. If the new size is
   * smaller only newSize elements will be copied.
   * 
   * @param a
   *          the original array
   * @param newSize
   *          the size of the new array
   * @return a new array with the original elements in a copied to it
   */
  public static long[] setSize(long[] a, int newSize) {
    if (newSize < 0)
      throw new IllegalArgumentException("Negative array size");
    long[] v = new long[newSize];
    System.arraycopy(a, 0, v, 0, newSize > a.length ? a.length : newSize);
    return v;
  }

  /**
   * Increases or decreases the size of an array of floats. If the new size is
   * smaller only newSize elements will be copied.
   * 
   * @param a
   *          the original array
   * @param newSize
   *          the size of the new array
   * @return a new array with the original elements in a copied to it
   */
  public static float[] setSize(float[] a, int newSize) {
    if (newSize < 0)
      throw new IllegalArgumentException("Negative array size");
    float[] v = new float[newSize];
    System.arraycopy(a, 0, v, 0, newSize > a.length ? a.length : newSize);
    return v;
  }

  /**
   * Increases or decreases the size of an array of doubles. If the new size is
   * smaller only newSize elements will be copied.
   * 
   * @param a
   *          the original array
   * @param newSize
   *          the size of the new array
   * @return a new array with the original elements in a copied to it
   */
  public static double[] setSize(double[] a, int newSize) {
    if (newSize < 0)
      throw new IllegalArgumentException("Negative array size");
    double[] v = new double[newSize];
    System.arraycopy(a, 0, v, 0, newSize > a.length ? a.length : newSize);
    return v;
  }

  /**
   * Increases or decreases the size of an array of Objects. If the new size is
   * smaller only newSize elements will be copied.
   * 
   * @param a
   *          the original array
   * @param newSize
   *          the size of the new array
   * @return a new array with the original elements in a copied to it
   */
  public static Object[] setSize(Object[] a, int newSize) {
    if (newSize < 0)
      throw new IllegalArgumentException("Negative array size");
    Class arrayClass = a.getClass().getComponentType();
    Object[] v = (Object[]) java.lang.reflect.Array.newInstance(arrayClass,
        newSize);
    System.arraycopy(a, 0, v, 0, newSize > a.length ? a.length : newSize);
    return v;
  }
}
