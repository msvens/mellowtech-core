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

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ext.CompiledLocale;

import java.nio.ByteBuffer;
import java.util.Locale;

/**
 * String that can be represented as bytes in conformance with the ByteStorable
 * definition. It represents itself as an UTF-8 encoded string. The CBString
 * also allows for comparison on a byte level, i.e. directly comparing the UTF-8
 * encoded strings.
 * <p>
 * The CBString can be used with a CompiledLocale to get correct comparison of
 * Strings at a language specific level.
 * </p>
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class CBString extends ByteComparable {

  private static char[] charMap = null;
  private static Locale locale = null;
  private String str;
  private int utfLength;

  static {
    // Do nothing here: the locale and hmap is created when it is accessed
    // and we have not already set it externally. 
    // setLocale(Locale.getDefault());
  }

  /**
   * Set the locale to the default, also creates the precompiled charmap
   */
  public static void setLocale() {
    setLocale(Locale.getDefault());
  }

  
  /**
   * Set the locale, also creates the precompiled charmap
   * 
   * @param locale
   *          a <code>Locale</code> value
   */
  public static void setLocale(Locale locale) {
    CoreLog.L().info("Setting locale to " + locale + ", creating new charmap");
    CBString.locale = locale;
    CBString.charMap = new CompiledLocale().getCompiledLocale(locale);
  }

  /**
   * Set the locale and the precompiled charmap
   * 
   * @param locale
   *          a <code>Locale</code> value
   */
  public static void setLocale(Locale locale, char[] charmap) {
    CoreLog.L().info("Setting locale to " + locale + ", using supplied charmap");
    CBString.locale = locale;
    CBString.charMap = charmap;
  }

  
  /**
   * Get the char hmap, creates it if it does not already exist.
   * 
   * @return a <code>char[]</code> value
   */
  public static char[] getCharMap() {
    if (CBString.charMap == null) 
      setLocale();
    return CBString.charMap;
  }

  /**
   * Get the locale, sets the locale to default if it is not already set, 
   * and if so also creates the char hmap.
   * 
   * @return a <code>Locale</code> value
   */
  public static Locale getLocale() {
    if (CBString.charMap == null) 
      setLocale();
    return CBString.locale;
  }

  /**
   * Creates a new <code>CBString</code> instance with an empty string
   * 
   */
  public CBString() {
    this.str = "";
    utfLength = 0;
  }

  /**
   * Creates a new <code>CBString</code> instance. If str is null a new empty
   * string will be created.
   * 
   * @param str
   *          the String that it represents.
   */
  public CBString(String str) {
    this.str = (str == null) ? "" : str;
    utfLength = getUTFLength();
  }

  /**
   * Sets the string for this CBString. If the str is null an empty string will
   * be created.
   * 
   * @param value
   *          new string
   */
  @Override
  public void set(Object value){
    if(value == null){
      this.str = "";
    }
    else if(!(value instanceof CharSequence))
      throw new ByteStorableException("not a CharSequence");
    else
      this.str = ((CharSequence) value).toString();
    utfLength = getUTFLength();
  }

  private void setStr(String str){
    this.str = (str == null) ? "" : str;
    utfLength = getUTFLength();
  }

  @Override
  public String get() {
    return str;
  }

  @Override
  public String toString() {
    return str;
  }

  /**
   * Uses the hashcode of the current string.
   * 
   * @return hashcode
   * @see String#hashCode()
   */
  public int hashCode() {
    return str.hashCode();
  }

  /**
   * Uses the string's equals method.
   * 
   * @param o
   *          an <code>Object</code> to compare with
   * @return true if the Strings are equal
   * @see String#equals(Object)
   */
  public boolean equals(Object o) {
    if (0 == compareTo(o))
      return true;
    return false;
  }

  // Overwritten ByteComparable
  public final int byteCompare(int offset1, byte b1[], int offset2, byte b2[]) {
    if (charMap == null)
      return byteCompareNoLocale(offset1, b1, offset2, b2);
    int tmp, tmp2, len1 = 0, len2 = 0, i = 0;
    // get Size of first string:
    tmp = b1[offset1++] & 0xFF;
    while ((tmp & 0x80) == 0) {
      len1 |= (tmp << (7 * i++));
      tmp = b1[offset1++] & 0xFF;
    }
    len1 |= ((tmp & ~(0x80)) << (7 * i));

    // get Size of second string:
    i = 0;
    tmp = b2[offset2++] & 0xFF;
    while ((tmp & 0x80) == 0) {
      len2 |= (tmp << (7 * i++));
      tmp = b2[offset2++] & 0xFF;
    }
    len2 |= ((tmp & ~(0x80)) << (7 * i));

    // now loop:
    int n = Math.min(len1, len2);
    char c1 = (char) 0, c2 = (char) 0;
    while (n-- != 0) {
      tmp = (int) b1[offset1] & 0xFF;
      tmp2 = (int) b2[offset2] & 0xFF;

      // string 1:
      switch (tmp >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        c1 = (char) tmp;
        offset1++;
        break;
      case 12:
      case 13:
        c1 = (char) (((tmp & 0x1F) << 6) | (b1[offset1 + 1] & 0x3F));
        n--;
        offset1 += 2;
        break;
      case 14:
        c1 = (char) (((tmp & 0x0F) << 12) | ((b1[offset1 + 1] & 0x3F) << 6) | ((b1[offset1 + 2] & 0x3F) << 0));
        n -= 2;
        offset1 += 3;
        break;
      }

      // string 2:
      switch (tmp2 >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        c2 = (char) tmp2;
        offset2++;
        break;
      case 12:
      case 13:
        c2 = (char) (((tmp2 & 0x1F) << 6) | (b2[offset2 + 1] & 0x3F));
        offset2 += 2;
        break;
      case 14:
        c2 = (char) (((tmp2 & 0x0F) << 12) | ((b2[offset2 + 1] & 0x3F) << 6) | ((b2[offset2 + 2] & 0x3F) << 0));
        offset2 += 3;
        break;
      }
      if (charMap[(int) c1] != charMap[(int) c2]) {
        return charMap[(int) c1] - charMap[(int) c2];
      }
    }
    return len1 - len2;

  }

  public final int byteCompareNoLocale(int offset1, byte b1[], int offset2,
      byte b2[]) {
    int tmp, len1 = 0, len2 = 0, i = 0;

    // get Size of first string:
    tmp = b1[offset1++] & 0xFF;
    while ((tmp & 0x80) == 0) {
      len1 |= (tmp << (7 * i++));
      tmp = b1[offset1++] & 0xFF;
    }
    len1 |= ((tmp & ~(0x80)) << (7 * i));

    // get Size of second string:
    i = 0;
    tmp = b2[offset2++] & 0xFF;
    while ((tmp & 0x80) == 0) {
      len2 |= (tmp << (7 * i++));
      tmp = b2[offset2++] & 0xFF;
    }
    len2 |= ((tmp & ~(0x80)) << (7 * i));

    // now loop:
    int n = Math.min(len1, len2);
    while (n-- != 0) {
      if (b1[offset1] != b2[offset2])
        return ((int) b1[offset1] & 0xFF) - ((int) b2[offset2] & 0xFF);
      offset1++;
      offset2++;
    }
    return len1 - len2;
  }

  public final int byteCompare(int offset1, ByteBuffer bb1, int offset2,
      ByteBuffer bb2) {
    if (charMap == null)
      return byteCompareNoLocale(offset1, bb1, offset2, bb2);

    int tmp, tmp2, len1 = 0, len2 = 0, i = 0;

    // get Size of first string:
    tmp = bb1.get(offset1++) & 0xFF;
    while ((tmp & 0x80) == 0) {
      len1 |= (tmp << (7 * i++));
      tmp = bb1.get(offset1++) & 0xFF;
    }
    len1 |= ((tmp & ~(0x80)) << (7 * i));

    // get Size of second string:
    i = 0;
    tmp = bb2.get(offset2++) & 0xFF;
    while ((tmp & 0x80) == 0) {
      len2 |= (tmp << (7 * i++));
      tmp = bb2.get(offset2++) & 0xFF;
    }
    len2 |= ((tmp & ~(0x80)) << (7 * i));

    // now loop:
    int n = Math.min(len1, len2);
    char c1 = (char) 0, c2 = (char) 0;

    while (n-- != 0) {
      tmp = (int) bb1.get(offset1) & 0xff;
      tmp2 = (int) bb2.get(offset2) & 0xff;

      // first string:
      switch (tmp >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        c1 = (char) tmp;
        offset1++;
        break;
      case 12:
      case 13:
        c1 = (char) (((tmp & 0x1F) << 6) | (bb1.get(offset1 + 1) & 0x3F));
        n--;
        offset1 += 2;
        break;
      case 14:
        c1 = (char) (((tmp & 0x0F) << 12)
            | ((bb1.get(offset1 + 1) & 0x3F) << 6) | ((bb1.get(offset1 + 2) & 0x3F) << 0));
        n -= 2;
        offset1 += 3;
        break;
      }

      // second string:
      switch (tmp2 >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        c2 = (char) tmp2;
        offset2++;
        break;
      case 12:
      case 13:
        c2 = (char) (((tmp2 & 0x1F) << 6) | (bb2.get(offset2 + 1) & 0x3F));
        offset2 += 2;
        break;
      case 14:
        c2 = (char) (((tmp2 & 0x0F) << 12)
            | ((bb2.get(offset2 + 1) & 0x3F) << 6) | ((bb2.get(offset2 + 2) & 0x3F) << 0));
        offset2 += 3;
        break;
      }
      if (charMap[(int) c1] != charMap[(int) c2])
        return charMap[(int) c1] - charMap[(int) c2];
    }
    return len1 - len2;
  }

  public final int byteCompareNoLocale(int offset1, ByteBuffer bb1,
      int offset2, ByteBuffer bb2) {
    int tmp, tmp2, len1 = 0, len2 = 0, i = 0;

    // get Size of first string:
    tmp = bb1.get(offset1++) & 0xFF;
    while ((tmp & 0x80) == 0) {
      len1 |= (tmp << (7 * i++));
      tmp = bb1.get(offset1++) & 0xFF;
    }
    len1 |= ((tmp & ~(0x80)) << (7 * i));

    // get Size of second string:
    i = 0;
    tmp = bb2.get(offset2++) & 0xFF;
    while ((tmp & 0x80) == 0) {
      len2 |= (tmp << (7 * i++));
      tmp = bb2.get(offset2++) & 0xFF;
    }
    len2 |= ((tmp & ~(0x80)) << (7 * i));

    // now loop:
    int n = Math.min(len1, len2);
    while (n-- != 0) {
      tmp = (int) bb1.get(offset1);
      tmp2 = (int) bb2.get(offset2);
      if (tmp != tmp2)
        return (tmp & 0xFF) - (tmp2 & 0xFF);
      offset1++;
      offset2++;
    }
    return len1 - len2;
  }

  // Overwritten ByteStorable
  public int compareTo(Object o) {

    if (charMap == null)
      return compareToNoLocale(o);

    String str1 = ((CBString) o).str;
    int n = Math.min(str.length(), str1.length());
    int i = 0;
    while (n-- != 0) {
      char c1 = charMap[(int) str.charAt(i)];
      char c2 = charMap[(int) str1.charAt(i)];
      if (c1 != c2)
        return c1 - c2;
      i++;
    }
    return str.length() - str1.length();

  }

  private int compareToNoLocale(Object o) {
    String str1 = ((CBString) o).str;
    int n = Math.min(str.length(), str1.length());
    int i = 0;
    while (n-- != 0) {
      if (str.charAt(i) != str1.charAt(i))
        return str.charAt(i) - str1.charAt(i);
      i++;
    }
    return str.length() - str1.length();

  }

  public int byteSize() {
    return utfLength + sizeBytesNeeded(utfLength);
  }

  public int byteSize(ByteBuffer bb) {
    int pos = bb.position();
    int length = getSize(bb);
    bb.position(pos);
    return length + sizeBytesNeeded(length);
  }

  public void toBytes(ByteBuffer bb) {
    encodeUTF(bb);
  }

  public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
    if (doNew) {
      return new CBString(decodeUTF(bb));
    }
    else {

        this.setStr(decodeUTF(bb));
    }
    return this;
  }

  /*
   * public ByteStorable fromBytes(ByteBuffer bb){ return new
   * CBString(decodeUTF(bb)); }
   */

  /**
   * Finds the shortest string that separates two strings:<br>
   * example 1: martin and rickard would be "r"<br>
   * example 2: martin and mary would be "mary"
   * 
   * @param str1
   *          a <code>ByteStorable</code> value
   * @param str2
   *          a <code>ByteStorable</code> value
   * @return the smallest separator
   */
  public ByteStorable separate(ByteStorable str1, ByteStorable str2) {
    String small, large;

    if (str1.compareTo(str2) < 0) {
      small = ((CBString) str1).str;
      large = ((CBString) str2).str;
    }
    else {
      small = ((CBString) str2).str;
      large = ((CBString) str1).str;
    }
    int i;
    if (charMap == null) {
      for (i = 0; i < small.length(); i++) {
        if (small.charAt(i) != large.charAt(i))
          break;
      }
    }
    else {
      for (i = 0; i < small.length(); i++) {
        char c1 = charMap[(int) small.charAt(i)];
        char c2 = charMap[(int) large.charAt(i)];
        if (c1 != c2)
          break;
      }
    }

    CBString newStr = new CBString();
    if (small.length() == large.length() && i == large.length()) {
      newStr.setStr(large);
      return newStr;
    }
    newStr.setStr(new String(large.substring(0, i + 1)));

    return newStr;

  }

  /** *****************COPIED FROM OLD XCBString***************** */
  /** *****************METHOD NAMES ETC. WILL BE SYNCHED WITH***** */
  /** *****************DECODE UTF ETC***************************** */
  static public char[] fromUTF(ByteBuffer bb, int pLength) {
    if (pLength == 0)
      return new char[0];

    char[] str = new char[pLength];
    int index = 0;

    int c, char2, char3;
    while (pLength > 0) {
      c = (int) bb.get() & 0xff;
      switch (c >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        pLength--;
        str[index++] = (char) c;
        break;

      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        pLength -= 2;
        char2 = (int) bb.get();
        str[index++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        pLength -= 3;
        char2 = (int) bb.get();
        char3 = (int) bb.get();
        str[index++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        ;
      } // switch
    } // while

    return str;
  } // fromUTF

  /**
   * Given a character sequence this method calculates the number of bytes that
   * are necessary to code the string in the utf8 format.
   * 
   * @param pStr
   *          holds the character sequence.
   * @return the number of bytes needed.
   */
  public static int getUTFLength(CharSequence pStr) {
    if (pStr == null)
      return 0;
    int strlen = pStr.length();
    int c;
    int utflength = 0;
    for (int i = 0; i < strlen; i++) {
      c = pStr.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007F))
        utflength++;
      else if (c > 0x07FF)
        utflength += 3;
      else
        utflength += 2;
    }
    return utflength;
  } // getUTFLength

  /**
   * Takes a CharSequence and turns it into a byte array of utf encoded data.
   * 
   * @param pStr
   *          holds the character data to convert.
   * @return a byte array with utf data.
   */
  static public byte[] toUTF(CharSequence pStr) {
    int utflen = getUTFLength(pStr);
    if (utflen == 0)
      return new byte[0];

    int strlen = pStr.length();
    int c;

    // Allocate byte arrray to hold length + utf bytes.
    byte[] utfData = new byte[utflen];

    int offset = 0;
    for (int i = 0; i < strlen; i++) {
      c = pStr.charAt(i);

      if ((c >= 0x0001) && (c <= 0x007F))
        utfData[offset++] = ((byte) c);
      else if (c > 0x07FF) {
        utfData[offset++] = ((byte) (0xE0 | ((c >> 12) & 0x0F)));
        utfData[offset++] = ((byte) (0x80 | ((c >> 6) & 0x3F)));
        utfData[offset++] = ((byte) (0x80 | ((c >> 0) & 0x3F)));
      }
      else {
        utfData[offset++] = ((byte) (0xC0 | ((c >> 6) & 0x1F)));
        utfData[offset++] = ((byte) (0x80 | ((c >> 0) & 0x3F)));
      }
    }
    ; // for all chars in input

    return utfData;
  } // toUFT

  /** ********************END COPY FROM OLD*************************** */

  /** ***********PRIVATE METHODS GOES HERE**************************** */
  private String decodeUTF(ByteBuffer bb) {
    int slength = getSize(bb);

    if (slength == 0)
      return null;

    StringBuffer tempstr = new StringBuffer(slength);

    int c, char2, char3;

    while (slength > 0) {
      c = (int) bb.get() & 0xff;
      switch (c >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        slength--;
        tempstr.append((char) c);
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        slength -= 2;
        char2 = (int) bb.get();
        tempstr.append((char) (((c & 0x1F) << 6) | (char2 & 0x3F)));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        slength -= 3;
        char2 = (int) bb.get();
        char3 = (int) bb.get();
        tempstr
            .append((char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0)));
        break;
      default:
        ;
      }
    }
    return tempstr.toString();
  }

  private void encodeUTF(ByteBuffer bb) {
    int strlen = (utfLength == 0) ? 0 : str.length();
    int c;

    // code length;
    putSize(utfLength, bb);

    for (int i = 0; i < strlen; i++) {
      c = str.charAt(i);

      if ((c >= 0x0001) && (c <= 0x007F))
        bb.put((byte) c);
      else if (c > 0x07FF) {
        bb.put((byte) (0xE0 | ((c >> 12) & 0x0F)));
        bb.put((byte) (0x80 | ((c >> 6) & 0x3F)));
        bb.put((byte) (0x80 | ((c >> 0) & 0x3F)));
      }
      else {
        bb.put((byte) (0xC0 | ((c >> 6) & 0x1F)));
        bb.put((byte) (0x80 | ((c >> 0) & 0x3F)));
      }
    }
    ;
  }

  private int getUTFLength() {
    if (str == null)
      return 0;
    int strlen = str.length();
    int c;
    int utflength = 0;
    for (int i = 0; i < strlen; i++) {
      c = str.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007F))
        utflength++;
      else if (c > 0x07FF)
        utflength += 3;
      else
        utflength += 2;
    }
    return utflength;
  }
}
