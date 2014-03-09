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

import com.mellowtech.core.bytestorable.CBString;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

/**
 * Converts a string into tokens, each token is stored as a CBString in a
 * CBString[]. Not terribly efficient since it uses 2 passes (one to count the
 * tokens and one to make the array). Intended use is however for relatively
 * small buffers (like a query string) so it doesn't matter.
 */

public class CBStringArray {

  static public CBString[] toCBStringArray(String pSource) {
    int numTokens = countWords(pSource);
    if (numTokens <= 0)
      return null;

    CBString[] cbArray = new CBString[numTokens];

    try {
      int i = 0;
      Reader r = new StringReader(pSource);
      StreamTokenizer st = new StreamTokenizer(r);
      int tType = 0;
      while ((tType = st.nextToken()) != StreamTokenizer.TT_EOF) {
        if (tType == StreamTokenizer.TT_WORD) {
          if (i <= cbArray.length - 1)
            cbArray[i] = new CBString(st.sval);
          i++;
        }
      }
    }
    catch (IOException ioe) {
      numTokens = 0;
    }

    return cbArray;
  } // toCBStringArray

  static public int countWords(String pSource) {
    int numTokens = 0;
    try {
      Reader r = new StringReader(pSource);
      StreamTokenizer st = new StreamTokenizer(r);
      int tType = 0;
      while ((tType = st.nextToken()) != StreamTokenizer.TT_EOF) {
        if (tType == StreamTokenizer.TT_WORD)
          numTokens++;
      }
    }
    catch (IOException ioe) {
      numTokens = 0;
    }
    return numTokens;
  } // countWorrds

} // CBStringArray
