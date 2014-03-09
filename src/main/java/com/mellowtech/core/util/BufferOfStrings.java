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
 * Class for handling a buffer containing multiple strings. Sole purpose is to
 * reduce dynamic memory allocation (String objects) in a text intensive
 * application.ll
 * 
 * BufferOfStrings can use an existing StringBuffer as backing buffer.
 */
public class BufferOfStrings {

  // Holds the number of strings (character sequences) in the buffer.
  private int fNumOfStrings = 0;

  // Buffer holding the various strings.
  private StringBuffer fBuffer = null;

  // Array of offsets pointing into fBuffer. Each offset points to a string
  // start.
  private int[] fOffsets = null;

  // Array of string lengths, each length corresponds to the offset for the item
  // given in fOffsets.
  private short[] fLengths = null;

  // Array of token types, a one-to-one correspondence to the other arrays.
  private byte[] fTTypes = null;

  // Default size of arrays. Expands to 1,75 x size wheen needed.
  private int fArraySize = 2000;

  // List of token types - note that T_TEXT is 0 (zero) so no need to set that
  // in fTTypes.
  public final byte T_TEXT = 0;
  public final byte T_STOPWORD = 1;
  public final byte T_NUMBER = 2;

  public final byte T_SENTENCE = 4;
  public final byte T_PARAGRAPH = 8;

  /**
   * Nested class for use in comparing a substring (token) in the buffer to a
   * hmap such as the <code>HashMap</code> without need to create an object to
   * do the stuff.
   */
  public class ItemCompare {
    public int fTokenId = 0;

  } // class ItemCompare

  /**
   * default constructor initializing size to zero and sets up buffers.
   */
  public BufferOfStrings() {
    fOffsets = new int[fArraySize];

    fLengths = new short[fArraySize];
    fBuffer = new StringBuffer();
    fNumOfStrings = 0;
  } // Default constructor

  /**
   * Constructor taking a StringBuffer as a backing buffer and tokenizes it.
   * 
   * @param pStringBuffer
   *          references the StringBuffer to use.
   */
  public BufferOfStrings(StringBuffer pStringBuffer) {
    fBuffer = pStringBuffer;
    fOffsets = new int[fArraySize];
    fLengths = new short[fArraySize];
    fNumOfStrings = 0;

  } // Constructor

  /**
   * Adds a string to our buffer of many strings.
   * 
   * @param pString
   *          holds the string to be inserted.
   */
  public void append(String pString) {
    fOffsets[fNumOfStrings] = fBuffer.length();
    fBuffer.append(pString);

    fLengths[fNumOfStrings] = (short) pString.length();
    fNumOfStrings++;
  } // append

  /**
   * Adds a string defined in an implementation of CharSequence.
   */
  public void append(CharSequence pCharSeq) {
    fOffsets[fNumOfStrings] = fBuffer.length();

    int len = pCharSeq.length();
    for (int i = 0; i < len; i++)
      fBuffer.append(pCharSeq.charAt(i));

    fLengths[fNumOfStrings] = (short) len;
    fNumOfStrings++;
  } // append

  /**
   * @return the number of strings in buffer.
   */
  public int getNumOfStrings() {
    return fNumOfStrings;
  } // getNumOfStrings

  /**
   * Tokenizes the content of the buffer and builds a table of token types.
   */
  public void tokenize() {

  } // tokeniize

} // BufferOfStrings
