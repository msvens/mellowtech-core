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

import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.bytestorable.CBString;

import java.nio.ByteBuffer;
import java.util.zip.Deflater;

/**
 * Trivial class encapsulating a character array. The array can be used to
 * append char array data as returned by the XmlHandlers etc. The buffer can
 * then be analyzed (e.g. tokenized) directly using the CharArrayTokenizer. All
 * this to minimize String allocations and deallocations in the text intensive
 * application and to ensure predictable memory usage.
 */
public class BufferOfChars extends ByteStorable implements CharSequence {
  // Publically accessible character array for fast manipulation.
  public char[] fBuffer = null;
  public int fLogicalLength = 0;

  /**
   * Allocates a buffer of default size - 1024 characters.
   */
  public BufferOfChars() {
    fBuffer = new char[1024];
  } // Default constructor

  /**
   * Allocates a buffer of a given size.
   * 
   * @param pSize
   *          holds the initial size of the buffer.
   */
  public BufferOfChars(int pSize) {
    fBuffer = new char[pSize];
  } // Constructor

  /**
   * Constructor taking an existing character array as its backing buffer.
   */
  public BufferOfChars(char[] pBuffer) {
    fBuffer = pBuffer;
    if (pBuffer == null)
      fLogicalLength = 0;
    else
      fLogicalLength = pBuffer.length;
  } // Constructor

  /**
   * Handy constructor for using a CharSequence.
   * 
   * @param pSequence
   *          holds the CharSequence instance.
   */
  public BufferOfChars(CharSequence pSequence) {
    this(pSequence.length());
    append(pSequence);
  }

  /**
   * Appends data to buffer, enlarging the buffeer if necessary.
   */
  public void append(char[] pChars, int pStart, int pLength) {
    // Make sure buffer is large enough to hold character data.
    while (true) {
      if (fBuffer == null)
        fBuffer = new char[1024 > pLength ? 1024 : pLength];
      if (fLogicalLength + pLength > fBuffer.length)
        enlargeBuffer();
      else
        break;
    }

    // Copy data to buffer.
    System.arraycopy(pChars, pStart, fBuffer, fLogicalLength, pLength);

    // Adjust logical length.
    fLogicalLength += pLength;
  } // append

  /**
   * Appends data to buffer, enlarging the buffeer if necessary. Internally
   * calls appendChar
   */
  public void append(CharSequence str) {
    for (int i = 0; i < str.length(); i++) {
      appendChar(str.charAt(i));
    }
  }

  /**
   * Appends len chars starting at offset from CharSequence.
   */
  public void append(CharSequence str, int offset, int len) {
    for (int i = offset; i < len && i < str.length(); i++)
      appendChar(str.charAt(i));
  }

  /**
   * Appends data to buffer, enlarging the buffeer if necessary.
   */
  public void appendChar(char pChar) {
    // Make sure buffer is large enough to hold character data.
    while (true) {
      if (fBuffer == null)
        fBuffer = new char[512];
      if (fLogicalLength + 1 > fBuffer.length)
        enlargeBuffer();
      else
        break;
    }

    // Copy data to buffer.
    fBuffer[fLogicalLength++] = pChar;
  } // append

  /**
   * Compresses the buffer using the zip utilities.
   */
  public int compress(Deflater pDeflater, byte[] pCompressedData) throws Exception{
    int resultLength = 0;

      // Encode a String into bytes, we do it dirty for now - just converting
      // chars into bytes
      // ..skipping eventual 2 byte chars - bad but necessary for now.
      byte[] input = new byte[fLogicalLength];
      for (int i = 0; i < fLogicalLength; i++)
        input[i] = (byte) fBuffer[i++];

      // Compress the bytes
      pDeflater.setInput(input);
      pDeflater.finish();
      resultLength = pDeflater.deflate(pCompressedData);

      // Try make gc discard input.
      input = null;

    return resultLength;
  } // compress

  /**
   * Enlarges the currrent buffer to 1.75 x current size. Content is copied.
   */
  public void enlargeBuffer() {
    int newBufferSize;

    newBufferSize = (int) (fBuffer.length * 1.75);

    char[] newBuffer = new char[newBufferSize];
    System.arraycopy(fBuffer, 0, newBuffer, 0,
        newBufferSize > fBuffer.length ? fBuffer.length : newBufferSize);

    fBuffer = newBuffer;
  } // enlargeBuffer

  /**
   * @return the character arrray.
   */
  public char[] getBuffer() {
    return fBuffer;
  } // getBuffer

  /**
   * @return the logical length of the buffer.
   */
  public int getLogicalLength() {
    return fLogicalLength;
  } // getLogicalLength

  /**
   * Resets the logical length of the buffer.
   */
  public void reset() {
    fLogicalLength = 0;
  } // reset

  /**
   * resets the instance and sets the buffer to a minimum size.
   */
  public void reset(int pToSize) {
    reset();
    if (fBuffer.length > 1024)
      fBuffer = new char[1024];
  } // reset

  /**
   * Sets a new character array as backing array.
   * 
   * @param pBuffer
   *          is the new backing char array.
   */
  public void setBuffer(char[] pBuffer) {
    fBuffer = pBuffer;
    fLogicalLength = pBuffer.length;
  } // setBuffer

  /**
   * Sets the logical length of the buffer. Note that this must be less than the
   * physical length.
   */
  public void setLogicalLength(int pLogicalLength) {
    if (pLogicalLength > fBuffer.length)
      throw new IllegalArgumentException(
          "Logical size of buffer cannot exceed physical size.");
    fLogicalLength = pLogicalLength;
  } // setLogicalLength

  public String toString() {
    if (fLogicalLength > 0)
      return new String(fBuffer, 0, fLogicalLength);
    return new String("");
  }

  /** ***********IMPLEMENTED CharSequence METHODS*************** */

  /**
   * For interface CharSequence.
   * 
   * @return the character at position.
   */
  public char charAt(int index) {
    if (index > fLogicalLength - 1)
      throw new ArrayIndexOutOfBoundsException(index);

    return fBuffer[index];
  } // charAt

  /**
   * Returns the length of this character sequence. The length is the number of
   * 16-bit Unicode characters in the sequence.
   * 
   * @return the number of characters in this sequence
   */
  public int length() {
    return fLogicalLength;
  } // length

  /**
   * For interface CharSequence.
   * 
   * @return a sub string according to params.
   */
  public CharSequence subSequence(int start, int end) {
    if (start > fLogicalLength - 1) {
      if (start == 0 && end == 0 && fLogicalLength == 0)
        return new String("");
      throw new IndexOutOfBoundsException();
    }
    if (end > fLogicalLength) {
      if (start == 0 && end == 0 && fLogicalLength == 0)
        return new String("");
      throw new IndexOutOfBoundsException();
    }

    String str = new String(fBuffer, start, end - start);
    return str;
  } // subSequence

  /** ***********IMPLEMENTED ByteStorable.java METHODS*************** */
  public int byteSize() {
    int len = CBString.getUTFLength(this);
    return 4 + len;// sizeBytesNeeded(len) + len;
  } // byteSize

  public int byteSize(ByteBuffer bb) {
    int position = bb.position();
    int len = bb.getInt();// getSize(bb);
    bb.position(position);

    return 4 + len; // size;
  } // byteSize

  public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
    int len = bb.getInt();// getSize(bb);
    if (doNew) {
      BufferOfChars boc = new BufferOfChars();
      boc.fBuffer = CBString.fromUTF(bb, len);
      boc.fLogicalLength = boc.fBuffer.length;

      return boc;
    }
    else {
      this.fBuffer = CBString.fromUTF(bb, len);
      this.fLogicalLength = this.fBuffer.length;
      return this;
    }

  } // fromBytes

  public void toBytes(ByteBuffer bb) {
    // int bsz = byteSize();

    // Put size of struct.
    int byteLen = CBString.getUTFLength(this);
    // putSize(byteLen, bb);
    bb.putInt(byteLen);

    // Convert char[] to utf8 coded byte[].
    byte[] byteBuf = CBString.toUTF(this);

    if (byteLen > 0)
      bb.put(byteBuf, 0, byteLen);
  } // toBytes

} // BufferOfChars
