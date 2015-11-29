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
 * Class that pre-calculates upper-case translation for chars. Useful for most
 * western languages.
 */
@Deprecated
public class UpperCaseArray {
  /**
   * Holds character to UpperCase translation precalculated to fasten tjings a
   * bit.
   */
  public char[] fUpperCaseTable;

  /**
   * Default constructor that builds the pre-calculated table for upper-case
   * translation.
   */
  public UpperCaseArray() {
    // Init upper-case conversion table. Only good for massive use of same
    // tokenizer.
    // ..We may move this to a specialized sub-class in next gen software.
    int numChars = 1024 * 64;
    fUpperCaseTable = new char[numChars];
    for (int c = 0; c < numChars; c++)
      fUpperCaseTable[c] = (char) Character.toUpperCase((char) c);
  } // default constructor

  /**
   * @param pChar
   *          holds the character to convert to upper-case.
   * @return the upper-case variant of the given char.
   */
  public char toUpperCase(char pChar) {
    return fUpperCaseTable[(int) pChar];
  } // toUpperCase

} // UpperCaseArray
