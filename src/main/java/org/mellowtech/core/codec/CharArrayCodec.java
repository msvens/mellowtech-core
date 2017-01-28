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

package org.mellowtech.core.codec;

import org.mellowtech.core.bytestorable.CBUtil;
import org.mellowtech.core.util.CompiledLocale;

import java.nio.ByteBuffer;
import java.util.Locale;

/**
 * @author msvens
 * @since 2017-01-24
 */
public class CharArrayCodec implements BCodec<char[]> {

  /**
   * Get the char hmap, creates it if it does not already exist.
   *
   * @return a <code>char[]</code> value
   */
  public static char[] getCharMap() {
    if (CharArrayCodec.charMap == null)
      setLocale();
    return CharArrayCodec.charMap;
  }
  /**
   * Get the locale, sets the locale to default if it is not already set,
   * and if so also creates the char hmap.
   *
   * @return a <code>Locale</code> value
   */
  public static Locale getLocale() {
    if (CharArrayCodec.charMap == null)
      setLocale();
    return CharArrayCodec.locale;
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
    CharArrayCodec.locale = locale;
    CharArrayCodec.charMap = new CompiledLocale().getCompiledLocale(locale);
  }

  /**
   * Set the locale and the pre compiled charmap
   *
   * @param locale
   *          a <code>Locale</code> to set
   * @param charmap
   *          charmap to set
   */
  public static void setLocale(Locale locale, char[] charmap) {
    CharArrayCodec.locale = locale;
    CharArrayCodec.charMap = charmap;
  }

  private static char[] toChars(CharSequence str){
    char[] val = new char[str.length()];
    for(int i = 0; i < val.length; i++)
      val[i] = str.charAt(i);
    return val;
  }


  private static char[] charMap = null;

  private static Locale locale = null;

  @Override
  public int compare(char[] first, char[] second) {
    int n = Math.min(first.length, second.length);
    int i = 0;
    char c1,c2;
    if (charMap == null){
      while(n-- != 0){
        c1 = first[i];
        c2 = second[i];
        if(c1 != c2) return c1 - c2;
        i++;
      }
    }
    else {
      while(n-- != 0){
        c1 = charMap[(int) first[i]];
        c2 = charMap[(int) second[i]];
        if(c1 != c2) return c1-c2;
        i++;
      }
    }
    return first.length - second.length;
  }

  @Override
  public int byteSize(char[] value) {
    return CBUtil.byteSize(UtfUtil.utfLength(value), true);
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CBUtil.peekSize(bb, true);
  }

  @Override
  public char[] from(ByteBuffer bb) {
    int length = CBUtil.getSize(bb, true);
    return UtfUtil.decodeChars(bb, length);
  }

  @Override
  public void to(char[] value, ByteBuffer bb) {
      CBUtil.putSize(UtfUtil.utfLength(value), bb, true);
      UtfUtil.encode(value, bb);
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

  /*@Override
  public boolean equals(Object o) {
    if(o instanceof CBCharArray) {
      int ret =  compareTo((CBCharArray) o);
      return ret == 0;
    }
    return false;
  }*/



}
