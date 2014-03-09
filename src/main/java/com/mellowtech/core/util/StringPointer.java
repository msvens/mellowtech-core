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
package com.mellowtech.core.util;

/**
 * Class for handling substrings in a char array. The substrings are stored in
 * the array and this class is used to compare etc substrings with other
 * entities such as Strings etc. Best performance is of course when an array
 * of StringPointer is created and each object reused. The instance is also
 * useful for sliding over a buffer with tokens, only making Strings when really
 * necessary. <break>
 * 
 * There is also a field <code>fSection</code> that holds the eventual section
 * to which the sub string belongs. Default is 0 - genetal text. Its use is
 * entirely external.
 * 
 * The length of the string is limited to 32K, remember that we are doing xir,
 * longer strings make little sense as tokens.
 */
public class StringPointer implements CharSequence, Comparable {
  /**
   * Data for positioning in an array of chars. Made public for direct access,
   * if needed.
   */
  public char[] fChars = null;
  public int fOffset = -1;
  public short fLength = 0;

  /**
   * Observe that the section identifier is very optional and its use must be
   * taken care of externally.
   */
  public int fSection = 0;

  /** The word position. */
  public int fPosition = 0;

  /** Type. */
  public int fType = 1;

  /**
   * Only computed when equal to zero. Remember to set to 0 when data above
   * changes (when pointer moves).
   */
  public int fHashCode = 0;

  /**
   * Default constructor, does nothing.
   */
  public StringPointer() {
  } // default constructor

  /**
   * Sets pointing into array, the offset into the array and the length of the
   * sub sequence.
   */
  public StringPointer(char[] pChars, int pOffset, short pLength) {
    fChars = pChars;
    fOffset = pOffset;
    fLength = pLength;
  } // Constructor

  /**
   * Allocates an array of empty <code>StringPointer</code>s.
   */
  static public StringPointer[] allocateArray(int pNumElements) {
    StringPointer[] array = new StringPointer[pNumElements];
    for (int i = 0; i < pNumElements; i++)
      array[i] = new StringPointer();
    return array;
  } // allocateArray

  /**
   * For interface CharSequence.
   * 
   * @return the character at position.
   */
  public char charAt(int index) {
    if (index > fLength - 1)
      throw new ArrayIndexOutOfBoundsException(index);

    return fChars[fOffset + index];
  } // charAt

  /**
   * Compares the sub string pointed to by self to pChars. Comparison is done
   * lexicographically. Target must support the CharSequence interface.
   * 
   * @param that
   *          is the object to which compare works on.
   * @return as usual for compareTo <code> -, 0, + </code>.
   */
  public int compareTo(Object that) {
    return compareTo((CharSequence) that);
  } // compareTo

  public int compareTo(CharSequence pChars) {
    char thisChars[] = fChars;

    int i = this.fOffset;
    int j = 0;
    int n = Math.min(fLength, pChars.length());

    while (n-- != 0) {
      char c1 = thisChars[i++];
      char c2 = pChars.charAt(j++);
      if (c1 != c2) {
        return c1 - c2;
      }
    } // while
    return fLength - pChars.length();
  } // compareTo

  /**
   * Compares the object with another object that implements CharSequence.
   */
  public boolean equals(Object obj) {
    if (this == obj)
      return true;

    // A throw will be issued if obj does not implement CharSequence.
    return equals((CharSequence) obj);
  } // equals

  public boolean equals(CharSequence pSeq) {
    if (pSeq.length() != fLength)
      return false;

    int n = fLength;
    int i = 0;
    while (n-- != 0) {
      if (pSeq.charAt(i) != fChars[fOffset + i])
        return false;
      i++;
    }
    return true;
  } // equals

  /**
   * Produces a hash code for the substring, as in String. Code is OK, but I
   * consider to improve it by a F calculated in higher dimension space for next
   * gen software. The value is stored for repeated use.
   */
  public int hashCode() {
    if (fHashCode != 0)
      return fHashCode;

    int h = 0;
    int off = fOffset;
    char val[] = fChars;
    int len = fLength;

    for (int i = 0; i < len; i++) {
      h = 31 * h + val[off++];
    }
    fHashCode = h;
    return h;
  } // hashCode

  /**
   * Returns the length of this character sequence. The length is the number of
   * 16-bit Unicode characters in the sequence.
   * 
   * @return the number of characters in this sequence
   */
  public int length() {
    return fLength;
  } // length

  /**
   * Sets new values for String Pointer. Useful when re-using the object for
   * sliding over a buffer with substrings.
   */
  public void reset(char[] pChars, int pOffset, short pLength) {
    fChars = pChars;
    fOffset = pOffset;
    fLength = pLength;

    // Must reset hash code so it is recalculated when needed.
    fHashCode = 0;
  } // reset

  public void reset(int pOffset, short pLength) {
    fOffset = pOffset;
    fLength = pLength;

    // Must reset hash code so it is recalculated when needed.
    fHashCode = 0;
  } // reset

  public void reset(char[] pChars) {
    fChars = pChars;
  } // reset

  /**
   * For interface CharSequence.
   * 
   * @return a sub string according to params.
   */
  public CharSequence subSequence(int start, int end) {
    if (start > fLength - 1)
      throw new IndexOutOfBoundsException();
    if (end > fLength)
      throw new IndexOutOfBoundsException();

    return new String(fChars, fOffset + start, fOffset + end);
  } // subSequence

  /**
   * Creates a String from data pointed to.
   */
  public String toString() {
    if (fLength <= 0)
      return new String("");

    return new String(fChars, fOffset, (int) fLength);
  } // toString

} // StringPointer

