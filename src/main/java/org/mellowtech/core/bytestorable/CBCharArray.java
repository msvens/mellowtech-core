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
import java.util.Arrays;
import java.util.Locale;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.util.CompiledLocale;

/**
 * CBChars is binary compatible with CBString, that is, it encodes its chars
 * to utf8. CBChars allows for comparison on a byte level, i.e. directly comparing the UTF-8
 * encoded characters. The main difference between CBChars and CBString is that
 * CBChars stores it's in-memory characters directly as a string which increase
 * performance in certain situations when doing heavy sorting that relies on
 * compareTo
 * <p>
 * The CBChars can be used with a CompiledLocale to get correct comparison of
 * Strings at a language specific level.
 * 
 * @author Martin Svensson, msvens@gmail.com
 * @version 1.0
 */
public class CBCharArray extends BStorableImp <char[], CBCharArray> implements BComparable<char[], CBCharArray>, CharSequence{

  /**
   * Get the char hmap, creates it if it does not already exist.
   * 
   * @return a <code>char[]</code> value
   */
  public static char[] getCharMap() {
    if (CBCharArray.charMap == null) 
      setLocale();
    return CBCharArray.charMap;
  }
  /**
   * Get the locale, sets the locale to default if it is not already set, 
   * and if so also creates the char hmap.
   * 
   * @return a <code>Locale</code> value
   */
  public static Locale getLocale() {
    if (CBCharArray.charMap == null) 
      setLocale();
    return CBCharArray.locale;
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
    CBCharArray.locale = locale;
    CBCharArray.charMap = new CompiledLocale().getCompiledLocale(locale);
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
    CoreLog.L().info("Setting locale to " + locale + ", using supplied charmap");
    CBCharArray.locale = locale;
    CBCharArray.charMap = charmap;
  }
  
  private static char[] toChars(CharSequence str){
    char[] val = new char[str.length()];
    for(int i = 0; i < val.length; i++)
      val[i] = str.charAt(i);
    return val;
  }

  
  private static char[] charMap = null;

  private static Locale locale = null;
  
  public CBCharArray(){super(new char[0]);}
  
  public CBCharArray(char[] chars){super(chars);}
  
  public CBCharArray(CharSequence str){super(toChars(str));}

  
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
    return CBUtil.byteSize(UtfUtil.utfLength(value), true);
  }


  @Override
  public int byteSize(ByteBuffer bb) {
    return CBUtil.peekSize(bb, true);
  }


  @Override
  public char charAt(int index) {
    return value[index];
  }


  @Override
  public int compareTo(CBCharArray other) {
    char[] obj1 = other.value;
    int n = Math.min(value.length, obj1.length);
    int i = 0;
    char c1,c2;
    if (charMap == null){
      while(n-- != 0){
        c1 = value[i];
        c2 = obj1[i];
        if(c1 != c2) return c1 - c2;
        i++;
      }
    }
    else {
      while(n-- != 0){
        c1 = charMap[(int) value[i]];
        c2 = charMap[(int) obj1[i]];
        if(c1 != c2) return c1-c2;
        i++;
      }
    }
    return value.length - obj1.length;
  }


  /**
   * Uses the string's equals method.
   * 
   * @param o
   *          an <code>Object</code> to compare with
   * @return true if the Strings are equal
   * @see String#equals(Object)
   */
  @Override
  public boolean equals(Object o) {
    System.out.println("executing equals");
    if(o instanceof CBCharArray) {
      int ret =  compareTo((CBCharArray) o);
      System.out.println(this+" "+(CBCharArray)o +" "+ ret);
      return ret == 0;
    }
    return false;
  }



  @Override
  public CBCharArray from(ByteBuffer bb) {
    int length = CBUtil.getSize(bb, true);
    return new CBCharArray(UtfUtil.decodeChars(bb, length));
  }

  @Override
  public int length() {
    return value.length;
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    char[] sub = Arrays.copyOfRange(value, start, end);
    return new CBCharArray(sub);
  }


  @Override
  public void to(ByteBuffer bb) {
    CBUtil.putSize(UtfUtil.utfLength(value), bb, true);
    UtfUtil.encode(value, bb);
  }
  
  @Override
  public String toString(){
    return new String(value);
  }


}
