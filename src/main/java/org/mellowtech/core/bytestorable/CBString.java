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
import java.util.Locale;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.ext.CompiledLocale;

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
 * @author Martin Svensson <msvens@gmail.com>
 * @version 2.0 (total rewrite)
 */
public class CBString extends ByteComparable <String> {

  private static char[] charMap = null;
  private static Locale locale = null;
  

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
  
  public CBString(String str){
    set(str);
  }
  
  public CBString(){
    set("");
  }

  
  @Override
  public int compareTo(ByteStorable <String> o) {
    if (charMap == null)
      return obj.compareTo(o.obj);

    String str1 = o.obj;
    int n = Math.min(obj.length(), str1.length());
    int i = 0;
    while (n-- != 0) {
      char c1 = charMap[(int) obj.charAt(i)];
      char c2 = charMap[(int) str1.charAt(i)];
      if (c1 != c2)
        return c1 - c2;
      i++;
    }
    return obj.length() - str1.length();
  }
  
  

  @Override
  public int byteCompare(int offset1, int offset2, byte[] b) {
    return charMap == null ? UtfUtil.compare(b, offset1, b, offset2) :
      UtfUtil.compare(b, offset1, b, offset2, charMap);
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
  public ByteStorable<String> fromBytes(ByteBuffer bb, boolean doNew) {
    CBString ret = doNew ? new CBString() : this;
    //System.out.println("decoding: "+bb.position());
    int length = CBUtil.decodeInt(bb);
    ret.set(UtfUtil.decode(bb, length));
    return ret;
  }


  @Override
  public void toBytes(ByteBuffer bb) {
    CBUtil.putSizeInt(UtfUtil.utfLength(obj), bb, true);
    UtfUtil.encode(obj, bb);
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
  public ByteStorable <String> separate(ByteStorable <String> str1, 
      ByteStorable <String> str2) {
    String small, large;

    if (str1.compareTo(str2) < 0) {
      small = ((CBString) str1).get();
      large = ((CBString) str2).get();
    }
    else {
      small = ((CBString) str2).get();
      large = ((CBString) str1).get();
    }
    int i;
    for(i = 0; i < small.length(); i++){
      if(small.charAt(i) != large.charAt(i))
        break;
    }
    /*if (charMap == null) {
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
    }*/

    CBString newStr = new CBString();
    if (small.length() == large.length() && i == large.length()) {
      newStr.set(large);
      return newStr;
    }
    newStr.set(new String(large.substring(0, i + 1)));

    return newStr;
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

  /**
   * Uses the string's equals method.
   * 
   * @param o
   *          an <code>Object</code> to compare with
   * @return true if the Strings are equal
   * @see String#equals(Object)
   */
  public boolean equals(Object o) {
    if(o instanceof CBString)
      return compareTo((CBString)o) == 0 ? true : false;
    return false;
  }


}
