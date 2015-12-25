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
