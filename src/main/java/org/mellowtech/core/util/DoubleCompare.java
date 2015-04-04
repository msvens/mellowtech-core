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
 * Static utility methods to compare double values allowing deviation EPSILON.
 * 
 */
public class DoubleCompare {

  public final static double EPSILON = 1e-6;

  /**
   * Tests if a is equal to b, allowing deviation EPSILON
   */
  public static boolean EQ(double a, double b) {
    return (a - b < EPSILON) && (b - a < EPSILON);
  }

  /**
   * Tests if a is not equal to b, allowing deviation EPSILON
   */
  public static boolean NOTEQ(double a, double b) {
    return !EQ(a, b);
  }

  /**
   * Tests if a is less than or equal to b, allowing deviation EPSILON
   */
  public static boolean LTE(double a, double b) {
    return (a - b < EPSILON);
  }

  /**
   * Tests if a is greater or equal to b, allowing deviation EPSILON
   */
  public static boolean GTE(double a, double b) {
    return (b - a < EPSILON);
  }

  /**
   * Tests if a is smaller than b, allowing deviation EPSILON
   */
  public static boolean LT(double a, double b) {
    return (b - a > EPSILON);
  }

  /**
   * Tests if a is smaller than b, allowing deviation EPSILON
   */
  public static boolean GT(double a, double b) {
    return (a - b > EPSILON);
  }
}
