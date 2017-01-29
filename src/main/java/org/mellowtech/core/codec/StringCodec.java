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

import org.mellowtech.core.util.CompiledLocale;

import java.nio.ByteBuffer;
import java.util.Locale;

/**
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 4.0.0
 */
public class StringCodec implements BCodec<String> {

  protected static char[] charMap = null;
  protected static Locale locale = null;

  @Override
  public int compare(String first, String second) {
    if (charMap == null)
      return first.compareTo(second);

    //String str1 = other.get();
    int n = Math.min(first.length(), second.length());
    int i = 0;
    while (n-- != 0) {
      char c1 = charMap[(int) first.charAt(i)];
      char c2 = charMap[(int) second.charAt(i)];
      if (c1 != c2)
        return c1 - c2;
      i++;
    }
    return first.length() - second.length();
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
    StringCodec.locale = locale;
    StringCodec.charMap = new CompiledLocale().getCompiledLocale(locale);
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
    StringCodec.locale = locale;
    StringCodec.charMap = charmap;
  }


  /**
   * Get the char hmap, creates it if it does not already exist.
   *
   * @return a <code>char[]</code> value
   */
  public static char[] getCharMap() {
    if (StringCodec.charMap == null)
      setLocale();
    return StringCodec.charMap;
  }

  /**
   * Get the locale, sets the locale to default if it is not already set,
   * and if so also creates the char hmap.
   *
   * @return a <code>Locale</code> value
   */
  public static Locale getLocale() {
    if (StringCodec.charMap == null)
      setLocale();
    return StringCodec.locale;
  }


  /*
  @Override
  public int byteCompare(int offset1, int offset2, byte[] b) {
    return charMap == null ? UtfUtil.compare(b, offset1, b, offset2) :
        UtfUtil.compare(b, offset1, b, offset2, charMap);
  }


  @Override
  public int byteCompare(int offset1, byte[] b1, int offset2, byte[] b2) {
    return charMap == null ? UtfUtil.compare(b1, offset1, b2, offset2) :
        UtfUtil.compare(b1, offset1, b2, offset2, charMap);
  }*/


  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
                         ByteBuffer bb2) {

    return charMap == null ? UtfUtil.compare(bb1, offset1, bb2, offset2) :
        UtfUtil.compare(bb1, offset1, bb2, offset2, charMap);

  }


  @Override
  public String from(ByteBuffer bb) {
    //int length = CBUtil.decodeInt(bb);
    return UtfUtil.decode(bb, CodecUtil.getSize(bb, true));
  }


  @Override
  public void to(String value, ByteBuffer bb) {
    CodecUtil.putSize(UtfUtil.utfLength(value), bb, true);
    UtfUtil.encode(value, bb);
  }


  @Override
  public int byteSize(String value) {
    return CodecUtil.byteSize(UtfUtil.utfLength(value), true);
  }


  @Override
  public int byteSize(ByteBuffer bb) {
    return CodecUtil.peekSize(bb, true);
  }


}
