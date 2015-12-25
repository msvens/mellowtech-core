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

package org.mellowtech.core.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Data Type Utility methods
 * 
 * @author rickard.coster@asimus.se
 */
public class DataTypeUtils {
  /**
   * A <code>DecimalFormat</code> value with '.' as separator and max fraction
   * digits set to 2.
   */
  public static DecimalFormat sDecimalFormat = new DecimalFormat();

  static {
    DecimalFormatSymbols symbols = sDecimalFormat.getDecimalFormatSymbols();
    symbols.setDecimalSeparator('.');
    sDecimalFormat.setDecimalFormatSymbols(symbols);
    sDecimalFormat.setMaximumFractionDigits(2);
  }

  /**
   * Convert a 'String' to a 'Date', using
   * <em>DateFormat.getDateInstance().parse(s)</em>.
   * 
   * @param s
   *          a <code>String</code> value
   * @param defaultValue
   *          the default <code>Date</code> value to return if 's' cannot be
   *          parsed.
   * @return a <code>Date</code> value
   */
  public static Date strToDate(String s, Date defaultValue) {
    try {
      return DateFormat.getDateInstance().parse(s);
    }
    catch (Exception e) {
    }
    return defaultValue;
  }

  /**
   * Convert a 'String' to a 'double', using <em>Double.parseDouble(s)</em>.
   * 
   * @param s
   *          a <code>String</code> value
   * @param defaultValue
   *          the default <code>double</code> value to return if 's' cannot be
   *          parsed.
   * @return a <code>double</code> value
   */
  public static double strToDouble(String s, double defaultValue) {
    try {
      return Double.parseDouble(s);
    }
    catch (Exception e) {
    }
    return defaultValue;
  }

  /**
   * Convert a 'String' to a 'int', using <em>Int.parseInt(s)</em>.
   * 
   * @param s
   *          a <code>String</code> value
   * @param defaultValue
   *          the default <code>int</code> value to return if 's' cannot be
   *          parsed.
   * @return a <code>int</code> value
   */
  public static int strToInt(String s, int defaultValue) {
    try {
      return Integer.parseInt(s);
    }
    catch (Exception e) {
    }
    return defaultValue;
  }

  /**
   * Convert a 'String' to a 'long', using <em>Long.parseInt(s)</em>.
   * 
   * @param s
   *          a <code>String</code> value
   * @param defaultValue
   *          the default <code>long</code> value to return if 's' cannot be
   *          parsed.
   * @return a <code>long</code> value
   */
  public static long strToLong(String s, long defaultValue) {
    try {
      return Long.parseLong(s);
    }
    catch (Exception e) {
    }
    return defaultValue;
  }

  /**
   * Convert a <code>String</code> containing integers separated by space to
   * an <code>ArrayList</code> containing the integers as objects.
   * 
   * @param stringArray
   *          a string containing integer values separated by space.
   * @return a list of <code>Integer</code> objects, possibly empty.
   */
  public static ArrayList strArrToIntList(String stringArray) {
    ArrayList list = new ArrayList();
    StringTokenizer tk = new StringTokenizer(stringArray);

    while (tk.hasMoreTokens()) {
      Integer theInt = null;
      try {
        theInt = new Integer(tk.nextToken());
      }
      catch (NumberFormatException e) {
        theInt = null;
      }
      if (theInt != null)
        list.add(theInt);
    }
    return list;
  }

  /**
   * Convert a <code>String</code> containing integers separated by space to
   * an <code>int[]</code> containing the integers.
   * 
   * @param stringArray
   *          a string containing integer values separated by space.
   * @return an array of the integers, possibly null
   */
  public static int[] strArrToIntArr(String stringArray) {
    ArrayList list = strArrToIntList(stringArray);
    if (list.size() <= 0)
      return null;
    int[] array = new int[list.size()];
    int i = 0;
    for (Iterator iter = list.iterator(); iter.hasNext();) {
      array[i++] = ((Integer) iter.next()).intValue();
    }
    return array;
  }

  /**
   * Convert a <code>String</code> containing integers separated by space to
   * an <code>BitSet</code> containing the integers as set members
   * 
   * @param stringArray
   *          a string containing integer values separated by space.
   * @return a <code>BitSet</code> object, possibly empty.
   */
  public static BitSet strArrToBitSet(String stringArray) {
    BitSet bs = new BitSet();
    StringTokenizer tk = new StringTokenizer(stringArray);

    while (tk.hasMoreTokens()) {
      Integer theInt = null;
      try {
        theInt = new Integer(tk.nextToken());
      }
      catch (NumberFormatException e) {
        theInt = null;
      }
      if (theInt != null)
        bs.set(theInt.intValue());
    }
    return bs;
  }

  /**
   * Split a string containing strings separated by space to a
   * <code>String[]</code>
   * 
   * @param stringArray
   *          a string containing strings separated by space.
   * @return a <code>String[]</code>, possibly null
   */
  public static String[] splitStrArr(String stringArray) {
    if (stringArray == null || stringArray.length() == 0)
      return null;
    ArrayList list = new ArrayList();
    StringTokenizer tk = new StringTokenizer(stringArray);

    while (tk.hasMoreTokens())
      list.add(tk.nextToken());

    return (String[]) list.toArray(new String[0]);
  }

  /**
   * Split a string containing ints separated by space to a <code>int[]</code>
   * 
   * @param intArray
   *          a string containing int separated by space.
   * @return a <code>int[]</code>, possibly null
   */
  public static int[] splitIntArr(String intArray) {
    if (intArray == null || intArray.length() == 0)
      return null;
    ArrayList list = new ArrayList();
    StringTokenizer tk = new StringTokenizer(intArray);

    while (tk.hasMoreTokens())
      list.add(tk.nextToken());

    int[] arr = new int[list.size()];
    int i = 0;
    for (Iterator iter = list.iterator(); iter.hasNext();) {
      arr[i++] = Integer.parseInt((String) iter.next());
    }
    return arr;
  }

  /**
   * Split a string containing doubles separated by space to a
   * <code>double[]</code>
   * 
   * @param doubleArray
   *          a string containing doubles separated by space.
   * @return a <code>double[]</code>, possibly null
   */
  public static double[] splitDoubleArr(String doubleArray) {
    if (doubleArray == null || doubleArray.length() == 0)
      return null;
    ArrayList list = new ArrayList();
    StringTokenizer tk = new StringTokenizer(doubleArray);

    while (tk.hasMoreTokens())
      list.add(tk.nextToken());

    double[] arr = new double[list.size()];
    int i = 0;
    for (Iterator iter = list.iterator(); iter.hasNext();) {
      arr[i++] = Double.parseDouble((String) iter.next());
    }
    return arr;
  }

  /**
   * Convert an 'ArrayList' of 'Integer' objects to a BitSet
   * 
   * @param integerArray
   *          the Integer array
   * @return a 'BitSet'
   */
  public static BitSet intArrToBitSet(ArrayList integerArray) {
    if (integerArray == null)
      return null;

    BitSet bitSet = new BitSet();
    for (Iterator iter = integerArray.iterator(); iter.hasNext();) {
      bitSet.set(((Integer) iter.next()).intValue());
    }
    return bitSet;
  }

  /**
   * Convert an array of ints to a BitSet
   * 
   * @param integerArray
   *          the int array
   * @return a 'BitSet'
   */
  public static BitSet intArrToBitSet(int[] integerArray) {
    if (integerArray == null)
      return null;

    BitSet bitSet = new BitSet();
    for (int i = 0; i < integerArray.length; i++)
      bitSet.set(integerArray[i]);

    return bitSet;
  }

  /**
   * Convert a <code>String</code> containing integers separated by space to
   * an <code>ArrayList</code> containing the integers as objects.
   * 
   * @param stringArray
   *          a string containing integer values separated by space.
   * @return a list of <code>Integer</code> objects, possibly empty.
   */
  public static ArrayList stringArrayToIntegerList(String stringArray) {
    ArrayList list = new ArrayList();
    StringTokenizer tk = new StringTokenizer(stringArray);

    while (tk.hasMoreTokens()) {
      Integer theInt = null;
      try {
        theInt = new Integer(tk.nextToken());
      }
      catch (NumberFormatException e) {
        theInt = null;
      }
      if (theInt != null)
        list.add(theInt);
    }
    return list;
  }

  /**
   * Shuffles an integer array by the same method as in
   * java.util.Collections.shuffle
   * 
   * @param array
   *          a value of type 'int[]'
   */
  public static void shuffle(int[] array) {
    shuffle(array, new java.util.Random());
  }

  /**
   * Shuffles an integer array by the same method as in
   * java.util.Collections.shuffle
   * 
   * @param array
   *          a value of type 'int[]'
   * @param random
   *          a value of type 'java.util.Random'
   */
  public static void shuffle(int[] array, java.util.Random random) {
    int len = array.length;
    for (int i = len - 1; i > 0; i--) {
      int pos = random.nextInt(i);
      int tmp = array[i];
      array[i] = array[pos];
      array[pos] = tmp;
    }
  }

  /**
   * Shuffles an integer array by the same method as in
   * java.util.Collections.shuffle
   * 
   * @param array
   *          a value of type 'Object[]'
   */
  public static void shuffle(Object[] array) {
    shuffle(array, new java.util.Random());
  }

  /**
   * Shuffles an object array by the same method as in
   * java.util.Collections.shuffle
   * 
   * @param array
   *          a value of type 'Object[]'
   * @param random
   *          a value of type 'java.util.Random'
   */
  public static void shuffle(Object[] array, java.util.Random random) {
    int len = array.length;
    for (int i = len - 1; i > 0; i--) {
      int pos = random.nextInt(i);
      Object tmp = array[i];
      array[i] = array[pos];
      array[pos] = tmp;
    }
  }

  /**
   * Normalize int to a string with numDigits digits. The string is filled with
   * zeroes from the beginning.
   * 
   * @param value
   *          the value
   * @param numDigits
   *          number of digits in string
   * @return normalized value, or null if numDigits is smaller than new String(value).length()
   */
  public static String normalizeInteger(int value, int numDigits) {
    String valueString = Integer.toString(value);
    int stringLen = valueString.length();

    if (stringLen == numDigits)
      return valueString;
    else if (stringLen < numDigits) {
      char[] charArray = new char[numDigits - stringLen];
      Arrays.fill(charArray, '0');
      return new String(charArray) + valueString;
    }
    else
      return null;
  }

  /**
   * Format a double value to a String
   * 
   * @param d
   *          a <code>double</code> value
   * @return a <code>String</code> value
   */
  public static String formatDouble(double d) {
    return sDecimalFormat.format(d);
  }

  /**
   * Format a double value to a String
   * 
   * @param d
   *          a <code>double</code> value
   * @return a <code>String</code> value
   */
  public static String fmt(double d) {
    return formatDouble(d);
  }

  /**
   * Print the bits in the value with the leftmost bit being the most
   * significant bit.
   * 
   * @param value
   *          a <code>long</code> value
   * @return a <code>String</code> value
   */
  public static String printBits(long value) {
    StringBuffer sb = new StringBuffer();
    for (int shift = 63; shift >= 0; shift--)
      sb.append((((value >>> shift) & 01) != 0) ? "1" : "0");
    return sb.toString();
  }

  /**
   * Print the bits in the value with the leftmost bit being the most
   * significant bit.
   * 
   * @param value
   *          an <code>int</code> value
   * @return a <code>String</code> value
   */
  public static String printBits(int value) {
    StringBuffer sb = new StringBuffer();
    for (int shift = 31; shift >= 0; shift--)
      sb.append((((value >>> shift) & 01) != 0) ? "1" : "0");
    return sb.toString();
  }

  /**
   * Print the bits in the value with the leftmost bit being the most
   * significant bit.
   * 
   * @param value
   *          a <code>short</code> value
   * @return a <code>String</code> value
   */
  public static String printBits(short value) {
    StringBuffer sb = new StringBuffer();
    for (int shift = 15; shift >= 0; shift--)
      sb.append((((value >>> shift) & 01) != 0) ? "1" : "0");
    return sb.toString();
  }

  /**
   * Print the bits in the value with the leftmost bit being the most
   * significant bit.
   * 
   * @param value
   *          a <code>byte</code> value
   * @return a <code>String</code> value
   */
  public static String printBits(byte value) {
    StringBuffer sb = new StringBuffer();
    for (int shift = 7; shift >= 0; shift--)
      sb.append((((value >>> shift) & 01) != 0) ? "1" : "0");
    return sb.toString();
  }
  
  /**
   * Print the bits in the value with the leftmost bit being the most
   * significant bit.
   * 
   * @param values
   *          a <code>byte</code> value
   * @return a <code>String</code> value
   */
  public static String printBits(byte[] values) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < values.length; i++)
      sb.append(printBits(values[i]) +  " ");
    return sb.toString();
  }
  
}
