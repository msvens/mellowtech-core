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
 * @author joacos
 * 
 * Implements a re-usable object that converts integers to a comparable
 * StringPointer. Useful if many int-to- string conversions are needed.
 */
public class FastIntToStringPointer {
  public char[] fIntAsChars = new char[12];
  public short fLength = 0;
  public StringPointer fStringPointer = new StringPointer();

  public FastIntToStringPointer() {
    // Set default value to zero so everything
    // ..is initialised.
    toStringPointer(0);
  } // Constructor

  public StringPointer toStringPointer(int i) {
    short len = 0;
    char[] buf = fIntAsChars;

    switch (i) {
    case Integer.MIN_VALUE:
      // "-2147483648";
      buf[len++] = '-';
      buf[len++] = '2';
      buf[len++] = '1';
      buf[len++] = '4';
      buf[len++] = '7';
      buf[len++] = '4';
      buf[len++] = '8';
      buf[len++] = '3';
      buf[len++] = '6';
      buf[len++] = '4';
      buf[len++] = '8';
      break;

    case -3:
      buf[len++] = '-';
      buf[len++] = '3';
      break;

    case -2:
      buf[len++] = '-';
      buf[len++] = '2';
      break;

    case -1:
      buf[len++] = '-';
      buf[len++] = '1';
      break;

    case 0:
      buf[len++] = '0';
      break;

    case 1:
      buf[len++] = '1';
      break;

    case 2:
      buf[len++] = '2';
      break;

    case 3:
      buf[len++] = '3';
      break;

    case 4:
      buf[len++] = '4';
      break;

    case 5:
      buf[len++] = '5';
      break;

    case 6:
      buf[len++] = '6';
      break;

    case 7:
      buf[len++] = '7';
      break;

    case 8:
      buf[len++] = '8';
      break;

    case 9:
      buf[len++] = '9';
      break;

    case 10:
      buf[len++] = '1';
      buf[len++] = '0';
      break;
    } // switch

    // See if trivial conversion done.
    if (len > 0) {
      fLength = len;
      fStringPointer.reset(fIntAsChars, 0, fLength);
      return fStringPointer;
    }

    short charPos = getChars(i, fIntAsChars);

    fLength = (short) (12 - charPos);
    fStringPointer.reset(fIntAsChars, charPos, fLength);

    return fStringPointer;
  } // toStringPointer

  /**
   * All possible chars for representing a number as a String
   */
  final static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
      '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
      'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

  final static char[] DigitTens = { '0', '0', '0', '0', '0', '0', '0', '0',
      '0', '0', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '2', '2',
      '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3', '3', '3',
      '3', '3', '3', '3', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
      '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '6', '6', '6', '6',
      '6', '6', '6', '6', '6', '6', '7', '7', '7', '7', '7', '7', '7', '7',
      '7', '7', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '9', '9',
      '9', '9', '9', '9', '9', '9', '9', '9', };

  final static char[] DigitOnes = { '0', '1', '2', '3', '4', '5', '6', '7',
      '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1',
      '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5',
      '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3',
      '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7',
      '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1',
      '2', '3', '4', '5', '6', '7', '8', '9', };

  private static short getChars(int i, char[] buf) {
    int q, r;
    short charPos = 12;
    char sign = 0;

    if (i < 0) {
      sign = '-';
      i = -i;
    }

    // Generate two digits per iteration
    while (i >= 65536) {
      q = i / 100;
      // really: r = i - (q * 100);
      r = i - ((q << 6) + (q << 5) + (q << 2));
      i = q;
      buf[--charPos] = DigitOnes[r];
      buf[--charPos] = DigitTens[r];
    }

    // Fall thru to fast mode for smaller numbers
    // assert(i <= 65536, i);
    for (;;) {
      q = (i * 52429) >>> (16 + 3);
      r = i - ((q << 3) + (q << 1)); // r = i-(q*10) ...
      buf[--charPos] = digits[r];
      i = q;
      if (i == 0)
        break;
    }
    if (sign != 0) {
      buf[--charPos] = sign;
    }
    return charPos;
  }

} // FastIntToStringPointer
