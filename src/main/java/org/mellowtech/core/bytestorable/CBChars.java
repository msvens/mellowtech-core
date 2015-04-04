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
package org.mellowtech.core.bytestorable;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Locale;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.ext.CompiledLocale;

/**
 * CBChars is binary compatiable with CBString, that is, it encodes its chars
 * to utf8. CBChars allows for comparison on a byte level, i.e. directly comparing the UTF-8
 * encoded characters. The main difference between CBChars and CBString is that
 * CBChars stores it's in-memory characters directly as a string which increase
 * performance in certain situations when doing heavy sorting that relies on
 * compareTo
 * <p>
 * The CBChars can be used with a CompiledLocale to get correct comparison of
 * Strings at a language specific level.
 * </p>
 * 
 * @author Martin Svensson <msvens@gmail.com>
 * @version 1.0
 */
public class CBChars extends ByteComparable <char[]> implements CharSequence{

  /**
   * Get the char hmap, creates it if it does not already exist.
   * 
   * @return a <code>char[]</code> value
   */
  public static char[] getCharMap() {
    if (CBChars.charMap == null) 
      setLocale();
    return CBChars.charMap;
  }
  /**
   * Get the locale, sets the locale to default if it is not already set, 
   * and if so also creates the char hmap.
   * 
   * @return a <code>Locale</code> value
   */
  public static Locale getLocale() {
    if (CBChars.charMap == null) 
      setLocale();
    return CBChars.locale;
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
    CBChars.locale = locale;
    CBChars.charMap = new CompiledLocale().getCompiledLocale(locale);
  }

  /**
   * Set the locale and the precompiled charmap
   * 
   * @param locale
   *          a <code>Locale</code> value
   */
  public static void setLocale(Locale locale, char[] charmap) {
    CoreLog.L().info("Setting locale to " + locale + ", using supplied charmap");
    CBChars.locale = locale;
    CBChars.charMap = charmap;
  }

  
  private static char[] charMap = null;

  private static Locale locale = null;
  
  private char[] value;
  
  public CBChars(){
    set(new char[0]);
  }
  
  public CBChars(char[] chars){
    set(chars);
  }
  
  public CBChars(CharSequence str){
    char[] val = new char[str.length()];
    for(int i = 0; i < val.length; i++)
      val[i] = str.charAt(i);
    set(val);
  }

  
  @Override
  public int byteCompare(int offset1, byte[] b1, int offset2, byte[] b2) {
    return charMap == null ? UtfUtil.compare(b1, offset1, b2, offset2) :
      UtfUtil.compare(b1, offset1, b2, offset2, charMap);
  }
  
  

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
      ByteBuffer bb2) {
    return charMap == null ? UtfUtil.compare(bb1, offset1, bb2, offset2) :
      UtfUtil.compare(bb1, offset1, bb2, offset2, charMap);
  }


  @Override
  public int byteCompare(int offset1, int offset2, byte[] b) {
    return charMap == null ? UtfUtil.compare(b, offset1, b, offset2) :
      UtfUtil.compare(b, offset1, b, offset2, charMap);
  }


  @Override
  public int byteSize() {
    int utfLength = UtfUtil.utfLength(obj);
    return CBUtil.encodeLength(utfLength) + utfLength;
  }


  @Override
  public int byteSize(ByteBuffer bb) {
    return CBUtil.peekSizeInt(bb, true);
  }


  @Override
  public char charAt(int index) {
    return obj[index];
  }


  @Override
  public int compareTo(ByteStorable <char[]> o) {
    char[] obj1 = o.obj;
    int n = Math.min(obj.length, obj1.length);
    int i = 0;
    char c1,c2;
    if (charMap == null){
      while(n-- != 0){
        c1 = obj[i];
        c2 = obj1[i];
        if(c1 != c2) return c1 - c2;
        i++;
      }
    }
    else {
      while(n-- != 0){
        c1 = charMap[(int) obj[i]];
        c2 = charMap[(int) obj1[i]];
        if(c1 != c2) return c1-c2;
        i++;
      }
    }
    return obj.length - obj1.length;
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
    if(o instanceof CBChars)
      return compareTo((CBChars)o) == 0 ? true : false;
    return false;
  }



  @Override
  public ByteStorable<char[]> fromBytes(ByteBuffer bb, boolean doNew) {
    CBChars ret = doNew ? new CBChars() : this;
    int length = CBUtil.decodeInt(bb);
    ret.obj = UtfUtil.decodeChars(bb, length);
    return ret;
  }

  /**
   * Uses the hashcode of the current string.
   * 
   * @return hashcode
   * @see String#hashCode()
   */
  public int hashCode() {
    return obj.hashCode();
  }

  @Override
  public int length() {
    return obj.length;
  }


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
  public ByteStorable <char[]> separate(ByteStorable <char[]> str1, 
      ByteStorable <char[]> str2) {
    char[] small, large;

    if (str1.compareTo(str2) < 0) {
      small = str1.get();
      large = str2.get();
    }
    else {
      small = str2.get();
      large = str1.get();
    }
    int i;
    for(i = 0; i < small.length; i++){
      if(small[i] != large[i])
        break;
    }

    CBChars newStr = new CBChars();
    if (small.length == large.length && i == large.length) {
      newStr.set(large);
      return newStr;
    }
    //newStr.set(new String(large.substring(0, i + 1)));
    newStr.set(Arrays.copyOf(large, i+1));
    return newStr;
  }


  @Override
  public CharSequence subSequence(int start, int end) {
    char[] sub = Arrays.copyOfRange(obj, start, end);
    return new CBChars(sub);
  }


  @Override
  public void toBytes(ByteBuffer bb) {
    CBUtil.putSizeInt(UtfUtil.utfLength(obj), bb, true);
    UtfUtil.encode(obj, bb);
  }
  
  @Override
  public String toString(){
    return new String(obj);
  }


}
