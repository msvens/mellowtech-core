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
 * Methods to format time taken for printing to stdout etc.
 */
public class PrintTime {

  static public String printTime(long pStartTime, String pSuffix) {
    return printTimeTaken(pStartTime) + " " + pSuffix;
  } // printTime

  static public String getTimeString(long pStartTime, String pSuffix) {
    return printTimeTaken(pStartTime) + " " + pSuffix;
  }
  
  static public String getTimeString(String pPrefix, long pStartTime, String pSuffix) {
    return pPrefix + " " + printTimeTaken(pStartTime) + " " + pSuffix;
  }

  /**
   * Prints time in good format hh:mm:ss
   * 
   * @param pStartTime
   *          holds when time started.
   */
  static public String printTimeTaken(long pStartTime) {
    // get current time
    return printTimeTaken(pStartTime, System.currentTimeMillis());
  } // printTimeTaken
  
  
  
  /**
   * Prints time in good format hh:mm:ss
   * 
   * @param pStartTime
   *          holds when time started.
   *          
   * @param pEndTime
   *          holds when time ended.
   */
  static public String printTimeTaken(long pStartTime, long pEndTime) {

    // get current time
    long eTime = pEndTime;
    StringBuffer sb = new StringBuffer();
    // See if we have a time stamp in fractions of a sec.
    if (eTime - pStartTime < 1000) {
      sb.append(((float) (eTime - pStartTime) / 1000) + " s.");
      return sb.toString();
    }

    // convert to secs.
    long secs = (eTime - pStartTime) / 1000;

    if (secs > 3600) {
      long hrs = secs / 3600;
      if (hrs > 1)
        sb.append(hrs + " hrs ");
      else
        sb.append(hrs + " hr ");

      secs = secs - hrs * 3600;
    }

    if (secs > 60) {
      long mins = secs / 60;
      if (mins > 1)
        sb.append(mins + " mins ");
      else
        sb.append(mins + " min ");

      secs = secs - mins * 60;
    }

    sb.append(secs + " secs");
    return sb.toString();

  } // printTimeTaken

}
