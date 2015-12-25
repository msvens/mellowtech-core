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
import java.util.Locale;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.util.CompiledLocale;

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
 * @author Martin Svensson, msvens@gmail.com
 * @version 2.0 (total rewrite)
 */
public class CBString extends BComparableImp<String, CBString>{

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
   * @param charmap
   *          a charmap to set
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
  
  public CBString(String str){super(str);}
  
  public CBString(){super("");}

  @Override
  public CBString create(String str) {return new CBString(str);}
  
  @Override
  public int compareTo(CBString other) {
    if (charMap == null)
      return value.compareTo(other.value);

    String str1 = other.value;
    int n = Math.min(value.length(), str1.length());
    int i = 0;
    while (n-- != 0) {
      char c1 = charMap[(int) value.charAt(i)];
      char c2 = charMap[(int) str1.charAt(i)];
      if (c1 != c2)
        return c1 - c2;
      i++;
    }
    return value.length() - str1.length();
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
  public CBString from(ByteBuffer bb) {
    int length = CBUtil.decodeInt(bb);
    return new CBString((UtfUtil.decode(bb, length)));
  }


  @Override
  public void to(ByteBuffer bb) {
    CBUtil.putSize(UtfUtil.utfLength(value), bb, true);
    UtfUtil.encode(value, bb);
  }


  @Override
  public int byteSize() {
    return CBUtil.byteSize(UtfUtil.utfLength(value), true);
  }


  @Override
  public int byteSize(ByteBuffer bb) {
    return CBUtil.peekSize(bb, true);
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
