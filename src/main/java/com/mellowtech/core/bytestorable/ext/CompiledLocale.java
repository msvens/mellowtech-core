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
package com.mellowtech.core.bytestorable.ext;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;


public class CompiledLocale {

  public String[] arr;

  public CompiledLocale() {
    arr = new String[Character.MAX_VALUE];
    buildCharArray();
  }

  public char[] getCompiledLocale(Locale l) {
    Collator c = Collator.getInstance(l);
    String[] toSort = new String[Character.MAX_VALUE];
    System.arraycopy(arr, 0, toSort, 0, Character.MAX_VALUE);
    Arrays.sort(toSort, new LocaleComparator(c));
    char[] toReturn = new char[Character.MAX_VALUE];
    Arrays.fill(toReturn, Character.MAX_VALUE);
    for (int i = 0; i < toSort.length; i++) {
      if (toSort[i] == null)
        break;
      char c1 = toSort[i].charAt(0);
      toReturn[(int) c1] = (char) i;
    }
    return toReturn;

  }

  /**
   * Reads a stored CompiledLocale array from file. 
   * 
   * @param file the file to read from
   * @return the char array
   * @throws IOException if an IO read error occurs
   */
  public char[] readLocale(File file) throws IOException {
    RandomAccessFile f = new RandomAccessFile(file, "r");
    
    // Read entire file into a ByteBuffer
    byte[] buffer = new byte[(int)f.length()];
    f.readFully(buffer);
    ByteBuffer bb = ByteBuffer.wrap(buffer);
    CharBuffer cb = bb.asCharBuffer();
    char[] chars;
    if (cb.hasArray())
      chars = cb.array();
    else {
      chars = new char[Character.MAX_VALUE];
      for (int i = 0; i < chars.length; i++)
        chars[i] = cb.charAt(i);
    }
    
    // Close up and return 
    f.close();
    return chars;
  }

  /**
   * Writes a CompiledLocale array to a file.
   * 
   * @param lc the CompiledLocale array.
   * @param file the file to write to 
   * @throws IOException if an IO write error occurs
   */
  public void writeLocale(char[] lc, File file) throws IOException {
    RandomAccessFile f = new RandomAccessFile(file, "rw");
    // Write char:s to file
    f.writeChars(new String(lc));
    f.close();
  }

  public void buildCharArray() {
    for (int i = 0; i < (int) Character.MAX_VALUE; i++) {
      char c = (char) i;
      if (Character.isDefined(c)) {
        arr[i] = "" + c;
      }
      else
        arr[i] = null;
    }
  }
}

class LocaleComparator implements Comparator<String> {
  Collator c;

  public LocaleComparator(Collator c) {
    this.c = c;
  }

  public int compare(String obj1, String obj2) {
    if (obj1 == null && obj2 == null)
      return 0;
    else if (obj1 == null)
      return 1;
    else if (obj2 == null)
      return -1;
    return c.compare(obj1, obj2);
  }

}
