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
/*
 * Refactored on Jan 26, 2005
 *
 * XIR - eXtreme Information retrieval
 * 
 * This module was kindly provided by Virtual Genetics Laboratory AB.
 * Originally written by Peter Franz�n.
 * 
 * Modified and extended By Joakim C�ster, Asimus AB.
 * 
 * Both Virtual Genetics and Asimus can treat this module as their own.
 */
package com.mellowtech.core.bytestorable.ext;

import com.mellowtech.core.bytestorable.ByteStorable;

import java.nio.ByteBuffer;

/**
 * An IntegerSet is an unordered collection of unique, non-negative 32-bit
 * integers. IntegerSets can be combined using the standard set operations union
 * (OR), intersection (AND), and difference (NOT).
 * <p>
 * Note that this class is not synchronized. External synchronization must be
 * provided byt its clients if it is to be used safely in a multithreaded
 * environment.
 */
@Deprecated
public class IntegerSet extends ByteStorable {
  // Implementation notes.

  // An IntegerSet is implemented as a logical array of bits, where each bit
  // number represents the corresponding integer, starting with 0. If a bit
  // in the logical array is set, it means that the corresponding integer is
  // a member of the set.

  // Physically, the bit array is an array of units, where a unit is an
  // integral data type, e.g. a byte, an int or a long. The units contain the
  // individual bits in the logical bit array, and it is at the unit level
  // that the bits are manipulated.

  // To locate the unit where an integer is encoded, the integer's value is
  // divided by the number of bits in each unit. Since the number of bits
  // in a unit always is a power of 2, this division can be performed with
  // right shifting with the 2-logarithm for this number.

  // To locate the bit within a unit where an integer is encoded, the
  // integer's value is taken modulo the number of bits per unit. This
  // operation can be performed by masking out all bits except those used to
  // encode the number of bits per unit - 1.

  // In some comments and notes there are performance considerations given for
  // certain implementation choices. Actual tests on several platforms have
  // been done to back these claims. The platforms used in these tests are
  // Solaris 8 with Java 1.4, Tru64 5.1 with Java 1.3.1, Linux Redhat 7.0 with
  // Java 1.3.1, MacOS X with Java 1.3.1, and Windows NT 4.0 WS with Java 1.4.

  // A unit is an integer, i.e a byte, a short an int or a long, containing a
  // number of bits. The number of bits in a unit will thereby always be a
  // power of 2 (byte -> 8, short -> 16, int -> 32, long -> 64).
  // Tests show that using int as the unit type is faster than using long on
  // most platforms, and only slightly slower on some 64-bit platforms. Using
  // int is always faster than using byte as the unit type.
  private static final int kNumBitsPerUnit = 32;

  // To get the segment number an integer's bit is located in, the integer
  // should be right shifted with the 2-logarithm for kNumBitsPerSegment,
  // which is equal to ln(x) / ln(2) (since n-log(x) == ln(x) / ln(n)).
  private static final int kUnitIndexShift = (int) Math.floor(Math
      .log(kNumBitsPerUnit)
      / Math.log(2));

  // The mask to apply to an integer to get its bit index within its unit.
  private static final int kBitIndexMask = kNumBitsPerUnit - 1;

  // The default size of an IntegerSet is to use one segment.
  private static final int kDefaultInitialSize = 512 * kNumBitsPerUnit;

  // A segment containing units with the value 0. Is used to mimic the
  // behaviour of memset() when clearing all bits/units in a segment. Copying
  // the contents of an array with System.arraycopy() is much faster than
  // looping over all elements and setting them to 0 individually. Exactly how
  // much faster depends on the length of the array.
  private static final int[] kZeroSegment = new int[kDefaultInitialSize];

  // An array of segments, where each segment is an array of units, i.e. the
  // integer type indicated by kNumBitsPerUnit. If an individual segment array
  // is null, it means that no integer encoded within that segement is a
  // member of the set.
  private int[] fUnits;

  // The currently largest value that can be a member of the set, which
  // depends on how large the array of units is.
  private int fCurMaxValue;

  // The cardinality of the set, i.e. the number of integers that are members
  // of the set. The value -1 indicates the cardinality must be calculated.
  private int fCardinality;

  /**
   * Create a new IntegerSet with a specific initial maximum value.
   * 
   * @param pMaxValue
   *          The maximum integer value the set is intended to hold. Should an
   *          integer with a value larger than this maximum be added, the set
   *          will be resized.
   * 
   * @throws IllegalArgumentException
   *           If pMaxValue is negative.
   */
  public IntegerSet(int pMaxValue) {
    if (pMaxValue < 0)
      throw new IllegalArgumentException("Negative IntegerSet max value: "
          + pMaxValue);

    // Calculate how many units we need to encode pMaxValue. Since the
    // set is 0-based, we need room for one more integer than this value.
    pMaxValue++;
    int aNumUnits = pMaxValue / kNumBitsPerUnit;
    if (pMaxValue % kNumBitsPerUnit != 0)
      aNumUnits++;

    // Allocate the unit array. The real maximum value is the number of
    // integers that can be stored in all units - 1.
    fUnits = new int[aNumUnits];
    fCurMaxValue = aNumUnits * kNumBitsPerUnit - 1;
  }

  /**
   * Create a new IntegerSet with a default initial size.
   */
  public IntegerSet() {
    this(kDefaultInitialSize);
  }

  /**
   * Check if an integer is a member of the set.
   * 
   * @param pValue
   *          The integer to check for membership.
   * 
   * @return True if pValue is a member of the set, false if not.
   * 
   * @throws IllegalArgumentException
   *           If pValue is negative.
   */
  public boolean contains(int pValue) {
    // if (pValue < 0)
    // throw new IllegalArgumentException("Negative IntegerSet value: " +
    // pValue);

    // If the value currently cannot be encoded in the set it cannot be
    // a member of the set either.
    if (pValue > fCurMaxValue)
      return false;

    // Return whether the bit corresponding to the value is set or not.
    return (fUnits[pValue >>> kUnitIndexShift] & (1 << (pValue & kBitIndexMask))) != 0;
  }

  /**
   * Add an integer to the set. After this call has completed, the method
   * <code>contains(pValue)</code> will return true.
   * 
   * @param pValue
   *          The integer to add to the set.
   * 
   * @throws IllegalArgumentException
   *           If pValue is negative.
   */
  public void add(int pValue) {
    // if (pValue < 0)
    // throw new IllegalArgumentException("Negative IntegerSet value: " +
    // pValue);

    // Make sure the IntegerSet is large enough to hold the specified value.
    if (pValue > fCurMaxValue)
      expand(pValue);

    // Set the bit corresponding to the value.
    fUnits[pValue >>> kUnitIndexShift] |= 1 << (pValue & kBitIndexMask);

    // Cached cardinality is no longer valid.
    fCardinality = -1;
  }

  /**
   * Remove an integer from the set. After this call has completed, the method
   * <code>contains(pValue)</code> will return false.
   * 
   * @param pValue
   *          The integer to remove from the set.
   * 
   * @throws IllegalArgumentException
   *           If pValue is negative.
   */
  public void remove(int pValue) {
    // if (pValue < 0)
    // throw new IllegalArgumentException("Negative IntegerSet value: " +
    // pValue);

    // If the value currently cannot be encoded in the set it cannot be
    // a member of the set either, and need noy be removed.
    if (pValue > fCurMaxValue)
      return;

    // Clear the bit corresponding to the value.
    fUnits[pValue >>> kUnitIndexShift] &= ~(1 << (pValue & kBitIndexMask));

    // Cached cardinality is no longer valid.
    fCardinality = -1;
  }

  /**
   * Remove all integers from a set. After this call has completed, the method
   * <code>contains(pValue)</code> will return false for all legal values.
   */
  public void clear() {
    for (int i = 0; i < fUnits.length; i++)
      fUnits[i] = 0;

    fCardinality = 0;
  }

  /**
   * Get the number of integers in the set.
   * 
   * @return The number of integers that are members of this set.
   */
  public int cardinality() {
    if (fCardinality == -1) {
      // No cached value, loop over all units and
      // sum the number of set bits.
      fCardinality = 0;
      for (int i = 0; i < fUnits.length; i++)
        fCardinality += getNumBitsSet(fUnits[i]);
    }

    return fCardinality;
  }

  /**
   * Get the next integer that is a member of this set.
   * 
   * @param pStartValue
   *          The first value to check for membership.
   * 
   * @return The smallest integer value that is >= pStartValue and is a member
   *         of the set. If no such value exists, -1 will be returned.
   * 
   * @throws IllegalArgumentException
   *           If pValue is negative.
   */
  public int getNextMember(int pStartValue) {
    // if (pStartValue < 0)
    // throw new IllegalArgumentException("Negative IntegerSet value: " +
    // pStartValue);

    // If the start value currently cannot be encoded in the set, there can
    // be no members with value >= pStartValue.
    if (pStartValue > fCurMaxValue)
      return -1;

    // Get the value of the unit where the start candidate is encoded and
    // mask off all bits lower than the start candidate's bit, since they
    // encode integers with lower values than the start candidate.
    int aUnit = pStartValue >>> kUnitIndexShift;
    int aUnitValue = fUnits[aUnit]
        & ~((1 << (pStartValue & kBitIndexMask)) - 1);

    // Loop until we find a unit whose value is non-zero.
    while (aUnitValue == 0 && aUnit < fUnits.length - 1)
      aUnitValue = fUnits[++aUnit];

    if (aUnitValue != 0)
      // We have found a unit with a set bit. Return the absolute position
      // of this bit (which is the first set bit in aUnitValue) in the
      // logical bit array.
      return aUnit * kNumBitsPerUnit + getFirstBit(aUnitValue);
    else
      // No unit with a bit set found, no member >= pStartValue exists.
      return -1;
  }

  /**
   * Calculate the intersection of this set and another set. The result will be
   * stored in this set. This is equivalent to the logical AND of this set with
   * the argument set, i.e. <code>this = this & pArgument</code>.
   * <p>
   * When the operation has finished, this set will have been modified to
   * contain those integers present only in both sets.
   * 
   * @param pArgument
   *          The IntegerSet to do the intersection with. This set will be left
   *          unmodified by the call.
   */
  public void intersection(IntegerSet pArgument) {
    // Doing an intersection with oneself is a no-op.
    if (this == pArgument)
      return;

    // Loop over all units that are common for both sets and calculate
    // the logical AND of their individual bits.
    int aNumUnits = Math.min(fUnits.length, pArgument.fUnits.length);
    for (int i = 0; i < aNumUnits; i++)
      fUnits[i] &= pArgument.fUnits[i];

    // Clear any units not present in the argument set.
    for (int i = aNumUnits; i < fUnits.length; i++)
      fUnits[i] = 0;

    // Cached cardinality is no longer valid.
    fCardinality = -1;
  }

  /**
   * Sets all bits in hmap.
   */
  public void setAllBits() {
    for (int i = 0; i <= fCurMaxValue; i++)
      add(i);
  } // setAllBits

  /**
   * Calculate the union of this set and another set. The result will be stored
   * in this set. This is equivalent to the logical OR of this set with the
   * argument set, i.e. <code>this = this | pArgument</code>.
   * <p>
   * When the operation has finished, this set will have been modified to
   * contain the integers present in at least one of sets.
   * 
   * @param pArgument
   *          The IntegerSet to do the union with. This set will be left
   *          unmodified by the call.
   */
  public void union(IntegerSet pArgument) {
    // Doing a union with oneself is a no-op.
    if (this == pArgument)
      return;

    // Loop over all units that are common for both sets and calculate
    // the logical OR of their individual bits.
    int aNumUnits = Math.min(fUnits.length, pArgument.fUnits.length);
    for (int i = 0; i < aNumUnits; i++)
      fUnits[i] |= pArgument.fUnits[i];

    // If the argument set is larger than this set, we must expand and copy
    // the argument values from those new units. Since this set logically
    // had zeroes in those bits, the result of this OR will be the value
    // of the argument set, thus a simple assignment will do the trick.
    if (fUnits.length < pArgument.fUnits.length) {
      expand(pArgument.fUnits.length * kNumBitsPerUnit - 1);
      for (int i = aNumUnits; i < fUnits.length; i++)
        fUnits[i] = pArgument.fUnits[i];
    }

    // Cached cardinality is no longer valid.
    fCardinality = -1;
  }

  /**
   * Calculate the difference of this set and another set. The result will be
   * stored in this set. This is equivalent to the logical AND NOT of this set
   * with the argument set, i.e. <code>this = this & ~pArgument</code>.
   * <p>
   * When the operation has finished, this set will have been modified to
   * contain the integers present in this set but not in the argument set.
   * 
   * @param pArgument
   *          The IntegerSet to do the difference with. This set will be left
   *          unmodified by the call.
   */
  public void difference(IntegerSet pArgument) {
    // Doing an difference with oneself results in an empty set.
    if (this == pArgument) {
      clear();
      return;
    }

    // Loop over all units that are common for both sets and calculate
    // the logical AND NOT of their individual bits. We don't need to check
    // any units that exist in only one of the sets. If this set has units
    // not in the arguments set it means that the bits encoded in those
    // units are not part of the argument set, and thus should be left
    // unmodified in this set. Should the argument set have more units, it
    // means that those bits are not present in this set and will thereby
    // not be present in the result set either.
    int aNumUnits = Math.min(fUnits.length, pArgument.fUnits.length);
    for (int i = 0; i < aNumUnits; i++)
      fUnits[i] &= ~(pArgument.fUnits[i]);

    // Cached cardinality is no longer valid.
    fCardinality = -1;
  }

  /**
   * Expand the number of segments so that the specified value can be encoded in
   * the set. The set is assumed to have been checked that the value can't be
   * encoded, no such checking will be done here.
   * 
   * @param pValue
   *          The value that the set should be able to encode once expanded.
   *          This is not necessarily the new largest value that can be encoded.
   */
  private void expand(int pValue) {
    // Calculate how many units we need for to encode pValue. Since the set
    // is 0-based, we need room for one more integer than this value.
    pValue++;
    int aNumUnits = pValue / kNumBitsPerUnit;
    if (pValue % kNumBitsPerUnit != 0)
      aNumUnits++;

    // Allocate a new unit array and copy the current values from the
    // existing segment array.
    int[] aNewUnits = new int[aNumUnits];
    System.arraycopy(fUnits, 0, aNewUnits, 0, fUnits.length);
    fUnits = aNewUnits;

    // The new maximum value in the set is the number of integers that can
    // be stored in all segments - 1.
    fCurMaxValue = aNumUnits * kNumBitsPerUnit - 1;
  }

  /**
   * Get the number of bits set in an integer.
   * <p>
   * There are (at least) two approaches to counting bits in an integer. One
   * common variant is to have an array with precalculated values of the number
   * of set bits in all possible values of a number of bits (commonly a byte,
   * but variants with all values for 11 bits have been seen). To count the
   * number of set bits, one splits the integer into several parts and gets the
   * bit count for each part from the precalculated array and return the sum of
   * these bit counts.
   * <p>
   * Another variant is to apply a sequence of esoteric bit manipulations to the
   * integer, which won't be described here. Check <a
   * href="http://www.mathematik.uni-bielefeld.de/~sillke/PROBLEMS/bitcount">here</a>
   * for an explanation of how this algorithm works.
   * <p>
   * Using this algorithm is slightly faster than using the precalculated array
   * variant on most JVMs, and it is also the method used in
   * <code>java.util.BitSet</code>. This implementation also uses the bit
   * manipulating algorithm.
   * <p>
   * 
   * @param pValue
   *          The integer to count the set bits in.
   * 
   * @return The number of bits set in pValue.
   */
  private static int getNumBitsSet(int pValue) {
    pValue -= (pValue & 0xaaaaaaaa) >>> 1;
    pValue = (pValue & 0x33333333) + ((pValue >>> 2) & 0x33333333);
    pValue = (pValue + (pValue >>> 4)) & 0x0f0f0f0f;
    pValue += pValue >>> 8;
    pValue += pValue >>> 16;
    return pValue & 0xff;
  }

  /**
   * Get the first bit set in an integer. This is the least significant bit in
   * the integer that has the value 1.
   * <p>
   * There are several ways to calculate the first set bit in an integer. A
   * somewhat naive approach is to shift the value 1 bit to the right until the
   * result has bit 0 set. Another way is to do a binary search in the integer
   * by first checking if the lowest 16 bits are zero, shifting 16 bits to the
   * right if so, then checking the new lowest 8 bits and so on.
   * <p>
   * Both approaches described above are slower, the first one significantly so,
   * than using an array with precalculated values of the first set bit in all
   * integer values from 0x00 to 0xff. The first bit set in an integer is then
   * the value from this table for the integer's least significant byte that has
   * a non-zero value. Incidentally, this variant is the one used in
   * <code>java.util.BitSet</code>.
   * 
   * @param pValue
   *          The integer to count the set bits in.
   * 
   * @return The position of the first (i.e. least significant) bit with value 1
   *         in pvalue, or -1 if pValue has no bits set (i.e. if pValue == 0).
   */
  private static int getFirstBit(int pValue) {
    // There are no set bits in the value 0.
    if (pValue == 0)
      return -1;

    // Check the first, i.e. least significant, byte.
    int aByteValue = pValue & 0xff;
    if (aByteValue != 0)
      return kFirstBitPosition[aByteValue];

    // First byte is 0x00, check the second byte.
    aByteValue = (pValue >>> 8) & 0xff;
    if (aByteValue != 0)
      return kFirstBitPosition[aByteValue] + 8;

    // First and second bytes are 0x00, check the third byte.
    aByteValue = (pValue >>> 16) & 0xff;
    if (aByteValue != 0)
      return kFirstBitPosition[aByteValue] + 16;

    // First, second and third bytes are all 0x00. Since we have checked
    // that the entire integer is != 0, it means that the fourth, i.e. most
    // significant, byte must be non-zero. Return its first set bit from the
    // precalculated table.
    aByteValue = (pValue >>> 24) & 0xff;
    return kFirstBitPosition[aByteValue] + 24;
  }

  /**
   * Precalculated values of the position of the first set bit in all values
   * from 0x00 to 0xff. This table can be used to get the position of the first
   * set bit in an integer by examining the table value of the integer's least
   * significant byte that is non-zero. For instance, the first set bit in the
   * integer 46 (0x2e) is kFirstBitPosition[46] == 1.
   */
  private final static byte kFirstBitPosition[] = { -1, 0, 1, 0, 2, 0, 1, 0, 3,
      0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5,
      0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3,
      0, 1, 0, 2, 0, 1, 0, 6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4,
      0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5, 0, 1, 0, 2, 0, 1, 0, 3,
      0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 7,
      0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3,
      0, 1, 0, 2, 0, 1, 0, 5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4,
      0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 6, 0, 1, 0, 2, 0, 1, 0, 3,
      0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5,
      0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3,
      0, 1, 0, 2, 0, 1, 0 };

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("{");
    int dno = 0;

    dno = this.getNextMember(0);
    sb.append(dno);

    while ((dno = this.getNextMember(dno + 1)) != -1)
      sb.append(" " + dno);
    sb.append("}");

    return sb.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.asimus.bytestorable.ByteStorable#fromBytes(java.nio.ByteBuffer)
   */
  public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
    int size = bb.getInt();

    // Create a set with max value.
    IntegerSet aSet = new IntegerSet(bb.getInt());
    aSet.fCardinality = bb.getInt();

    // Fill in data.
    size = bb.getInt();
    for (int i = 0; i < size; i++)
      aSet.fUnits[i] = bb.getInt();

    return aSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.asimus.bytestorable.ByteStorable#toBytes(java.nio.ByteBuffer)
   */
  public void toBytes(ByteBuffer bb) {
    // Save total size of hmap.
    bb.putInt(byteSize());

    bb.putInt(fCurMaxValue);
    bb.putInt(fCardinality);
    bb.putInt(fUnits.length);

    for (int i = 0; i < fUnits.length; i++)
      bb.putInt(fUnits[i]);
  } // toBytes

  /*
   * (non-Javadoc)
   * 
   * @see com.asimus.bytestorable.ByteStorable#byteSize()
   */
  public int byteSize() {
    // 4 integers + array.
    return (fUnits.length * 4) + 4 + 4 + 4 + 4;
  } // byteSize

  /*
   * (non-Javadoc)
   * 
   * @see com.asimus.bytestorable.ByteStorable#byteSize(java.nio.ByteBuffer)
   */
  public int byteSize(ByteBuffer bb) {
    int pos = bb.position();
    int nBytes = bb.getInt();
    bb.position(pos);
    return nBytes;
  } // byteSize

} // IntegerSet
