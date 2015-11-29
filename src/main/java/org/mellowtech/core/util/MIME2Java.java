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

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class MIME2Java {

  static private Map <String, String> s_enchash;
  static private Map <String, String> s_revhash;

  static {
    s_enchash = new HashMap <> ();
    // <preferred MIME name (uppercase)>, <Java encoding name>
    s_enchash.put("UTF-8", "UTF8");
    s_enchash.put("US-ASCII", "ASCII");
    s_enchash.put("ISO-8859-1", "8859_1");
    s_enchash.put("ISO-8859-2", "8859_2");
    s_enchash.put("ISO-8859-3", "8859_3");
    s_enchash.put("ISO-8859-4", "8859_4");
    s_enchash.put("ISO-8859-5", "8859_5");
    s_enchash.put("ISO-8859-6", "8859_6");
    s_enchash.put("ISO-8859-7", "8859_7");
    s_enchash.put("ISO-8859-8", "8859_8");
    s_enchash.put("ISO-8859-9", "8859_9");
    s_enchash.put("ISO-2022-JP", "JIS");
    s_enchash.put("SHIFT_JIS", "SJIS");
    /**
     * MS932 is suitable for Windows-31J, but JDK 1.1.x does not support MS932.
     */
    String version = System.getProperty("java.version");
    if (version.equals("1.1") || version.startsWith("1.1.")) {
      s_enchash.put("WINDOWS-31J", "SJIS");
    }
    else {
      s_enchash.put("WINDOWS-31J", "MS932");
    }
    s_enchash.put("EUC-JP", "EUCJIS");
    s_enchash.put("GB2312", "GB2312");
    s_enchash.put("BIG5", "Big5");
    s_enchash.put("EUC-KR", "KSC5601");
    s_enchash.put("ISO-2022-KR", "ISO2022KR");
    s_enchash.put("KOI8-R", "KOI8_R");

    s_enchash.put("EBCDIC-CP-US", "CP037");
    s_enchash.put("EBCDIC-CP-CA", "CP037");
    s_enchash.put("EBCDIC-CP-NL", "CP037");
    s_enchash.put("EBCDIC-CP-DK", "CP277");
    s_enchash.put("EBCDIC-CP-NO", "CP277");
    s_enchash.put("EBCDIC-CP-FI", "CP278");
    s_enchash.put("EBCDIC-CP-SE", "CP278");
    s_enchash.put("EBCDIC-CP-IT", "CP280");
    s_enchash.put("EBCDIC-CP-ES", "CP284");
    s_enchash.put("EBCDIC-CP-GB", "CP285");
    s_enchash.put("EBCDIC-CP-FR", "CP297");
    s_enchash.put("EBCDIC-CP-AR1", "CP420");
    s_enchash.put("EBCDIC-CP-HE", "CP424");
    s_enchash.put("EBCDIC-CP-CH", "CP500");
    s_enchash.put("EBCDIC-CP-ROECE", "CP870");
    s_enchash.put("EBCDIC-CP-YU", "CP870");
    s_enchash.put("EBCDIC-CP-IS", "CP871");
    s_enchash.put("EBCDIC-CP-AR2", "CP918");

    // j:CNS11643 -> EUC-TW?
    // ISO-2022-CN? ISO-2022-CN-EXT?

    s_revhash = new HashMap <> ();
    // <Java encoding name (uppercase)>, <preferred MIME name>
    s_revhash.put("UTF8", "UTF-8");
    s_revhash.put("ASCII", "US-ASCII");
    s_revhash.put("8859_1", "ISO-8859-1");
    s_revhash.put("8859_2", "ISO-8859-2");
    s_revhash.put("8859_3", "ISO-8859-3");
    s_revhash.put("8859_4", "ISO-8859-4");
    s_revhash.put("8859_5", "ISO-8859-5");
    s_revhash.put("8859_6", "ISO-8859-6");
    s_revhash.put("8859_7", "ISO-8859-7");
    s_revhash.put("8859_8", "ISO-8859-8");
    s_revhash.put("8859_9", "ISO-8859-9");
    s_revhash.put("JIS", "ISO-2022-JP");
    s_revhash.put("SJIS", "Shift_JIS");
    s_revhash.put("MS932", "WINDOWS-31J");
    s_revhash.put("EUCJIS", "EUC-JP");
    s_revhash.put("GB2312", "GB2312");
    s_revhash.put("BIG5", "Big5");
    s_revhash.put("KSC5601", "EUC-KR");
    s_revhash.put("ISO2022KR", "ISO-2022-KR");
    s_revhash.put("KOI8_R", "KOI8-R");

    s_revhash.put("CP037", "EBCDIC-CP-US");
    s_revhash.put("CP037", "EBCDIC-CP-CA");
    s_revhash.put("CP037", "EBCDIC-CP-NL");
    s_revhash.put("CP277", "EBCDIC-CP-DK");
    s_revhash.put("CP277", "EBCDIC-CP-NO");
    s_revhash.put("CP278", "EBCDIC-CP-FI");
    s_revhash.put("CP278", "EBCDIC-CP-SE");
    s_revhash.put("CP280", "EBCDIC-CP-IT");
    s_revhash.put("CP284", "EBCDIC-CP-ES");
    s_revhash.put("CP285", "EBCDIC-CP-GB");
    s_revhash.put("CP297", "EBCDIC-CP-FR");
    s_revhash.put("CP420", "EBCDIC-CP-AR1");
    s_revhash.put("CP424", "EBCDIC-CP-HE");
    s_revhash.put("CP500", "EBCDIC-CP-CH");
    s_revhash.put("CP870", "EBCDIC-CP-ROECE");
    s_revhash.put("CP870", "EBCDIC-CP-YU");
    s_revhash.put("CP871", "EBCDIC-CP-IS");
    s_revhash.put("CP918", "EBCDIC-CP-AR2");
  }

  private MIME2Java() {
  }

  /**
   * Convert a MIME charset name, also known as an XML encoding name, to a Java
   * encoding name.
   * 
   * @param mimeCharsetName
   *          Case insensitive MIME charset name:
   *          <code>UTF-8, US-ASCII, ISO-8859-1,
   *                          ISO-8859-2, ISO-8859-3, ISO-8859-4, ISO-8859-5, ISO-8859-6,
   *                          ISO-8859-7, ISO-8859-8, ISO-8859-9, ISO-2022-JP, Shift_JIS, Windows-31J
   *                          EUC-JP, GB2312, Big5, EUC-KR, ISO-2022-KR, KOI8-R,
   *                          EBCDIC-CP-US, EBCDIC-CP-CA, EBCDIC-CP-NL, EBCDIC-CP-DK,
   *                          EBCDIC-CP-NO, EBCDIC-CP-FI, EBCDIC-CP-SE, EBCDIC-CP-IT,
   *                          EBCDIC-CP-ES, EBCDIC-CP-GB, EBCDIC-CP-FR, EBCDIC-CP-AR1,
   *                          EBCDIC-CP-HE, EBCDIC-CP-CH, EBCDIC-CP-ROECE, EBCDIC-CP-YU,
   *                          EBCDIC-CP-IS and EBCDIC-CP-AR2</code>.
   * @return Java encoding name, or <var>null</var> if <var>mimeCharsetName</var>
   *         is unknown.
   * @see #reverse
   */
  public static String convert(String mimeCharsetName) {
    String tmp = (String) s_enchash.get(mimeCharsetName.toUpperCase());
    return tmp != null ? tmp : mimeCharsetName;
  }

  /**
   * Convert a Java encoding name to MIME charset name. Available values of
   * <i>encoding</i> are "UTF8", "8859_1", "8859_2", "8859_3", "8859_4",
   * "8859_5", "8859_6", "8859_7", "8859_8", "8859_9", "JIS", "SJIS", "MS932",
   * "EUCJIS", "GB2312", "BIG5", "KSC5601", "ISO2022KR", "KOI8_R", "CP037",
   * "CP277", "CP278", "CP280", "CP284", "CP285", "CP297", "CP420", "CP424",
   * "CP500", "CP870", "CP871" and "CP918".
   * 
   * @param encoding
   *          Case insensitive Java encoding name:
   *          <code>UTF8, 8859_1, 8859_2, 8859_3,
   *                      8859_4, 8859_5, 8859_6, 8859_7, 8859_8, 8859_9, JIS, SJIS, MS932, EUCJIS,
   *                      GB2312, BIG5, KSC5601, ISO2022KR, KOI8_R, CP037, CP277, CP278,
   *                      CP280, CP284, CP285, CP297, CP420, CP424, CP500, CP870, CP871 
   *                      and CP918</code>.
   * @return MIME charset name, or <var>null</var> if <var>encoding</var> is
   *         unknown.
   * @see #convert
   */
  public static String reverse(String encoding) {
    String tmp = (String) s_revhash.get(encoding.toUpperCase());
    return (tmp != null) ? tmp : encoding;
  }
}
