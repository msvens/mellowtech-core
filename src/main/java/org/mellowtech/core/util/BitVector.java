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

import java.nio.ByteBuffer;

import org.mellowtech.core.bytestorable.ByteStorable;

class GolHashTable{

  public static int GOLHASHTABLESIZE = 8192;
  GolHash[] table;

  public GolHashTable(){
    table = new GolHash[GOLHASHTABLESIZE];
  }

  public int size(){
    return table.length;
  }

  public GolHash get(int i){
    return table[i]; 
  }

  public void set(int i, GolHash gh){
    table[i] = gh;
  }
}

class GolHash{
  int b;  /* b value used for lookup */
  int q;  /* q value that is floor(log_2(b)) */
}

/**
 * Vector of bits. Methods for compressing integers using Elias Gamma/Delta and
 * Variable Byte coding.
 * <p>
 * 
 * The algorithms for Elias delta/gamma coding was adapted from a C
 * implementation accompanying the paper <br>
 * <pre>
 *   H.E. Williams, and J. Zobel, Compressing Integers for Fast File 
 *    Access, Computer Journal, 42(3), 193-201, 1999. 
 * </pre>
 * 
 * @author rickard.coster@asimus.se
 * @version 1.0
 */
@Deprecated
public class BitVector extends ByteStorable {
  /**
   * Default size of the vector
   * 
   */
  public static int DEFAULT_BYTE_SIZE = 1024;
  /**
   * define log10 for speed
   * 
   */
  public static double log10 = 0.43429448190325176;
  private static int masks[] = { 0x0, 0x1, 0x3, 0x7, 0xf, 0x1f, 0x3f, 0x7f,
      0xff };
  private byte[] vector;// Sequence of bytes to hold bitvector
  private int pos; // Current byte number
  private int bit; // Current bit in byte
  private int cur; // Temporary space for putting, getting bits
  private int len; // Number of bits used

  private static GolHashTable staticGolHashTable = null;
  
  static {
    staticGolHashTable = new GolHashTable();
  }
  
  /**
   * Creates a new <code>BitVector</code> instance, whose vector is 1024 bytes
   * large
   */
  public BitVector() {
    this(DEFAULT_BYTE_SIZE);
  }

  /**
   * Creates a new <code>BitVector</code> instance, whose vector is 'bytesize'
   * bytes large
   * 
   * @param bytesize
   *          the byte size
   */
  public BitVector(int bytesize) {
    init(bytesize);
  }

  static double log10(double x) {
    return log10 * Math.log(x);
  }

  static double log2(double f) {
    return log10((f)) / 0.301029995; // log10(2.0)
  }

  /**
   * Reset markers, i.e set pos() and bit() to zero, and set current byte to the
   * first byte in vector
   */
  public void startRead() {
    pos = bit = 0;
    cur = (vector[0] & 0xFF);
  }

  /**
   * Start an append operation - i.e set pos() and bit() to the last values, and
   * set current byte to the last byte in vector
   */
  public void startAppend() {
    pos = len / 8;
    bit = len % 8;
    cur = (vector[pos] & 0xFF);
  }

  /**
   * End an append operation - i.e pad vector.
   * 
   */
  public void endAppend() {
    if (pos >= vector.length)
      expand();
    vector[pos] = (byte) ((cur << (8 - bit)) & 0xff);
    len = 8 * pos + bit;
  }

  /**
   * Initialize (and remove any previous data) current vector to 'bytesize'
   * number of zero-bytes, and reset markers
   * 
   * @param bytesize
   *          number of bytes
   */
  public void init(int bytesize) {
    vector = new byte[bytesize];
    startRead();
  }

  /**
   * Current byte position in vector
   * 
   * @return the byte position
   */
  public int pos() {
    return pos;
  }

  /**
   * Current bit position in current byte
   * 
   * @return the bit position
   */
  public int bit() {
    return bit;
  }

  /**
   * Length in bits of current vector, i.e 8 * pos() + bit()
   * 
   * @return length in bits of current vector
   */
  public int len() {
    return (8 * pos()) + bit();
  }

  /**
   * Size in bytes of allocated vector
   * 
   * @return size in bytes of allocated vector
   */
  public int size() {
    return (vector == null) ? 0 : vector.length;
  }

  public int byteSize() {
    int lenbits = len();
    int vectorlen = (lenbits / 8) +1;
    return sizeBytesNeeded(lenbits) + vectorlen;
  }

  public int byteSize(ByteBuffer bb) {
    int position = bb.position();
    int lenbits = getSize(bb);
    bb.position(position);
    return sizeBytesNeeded(lenbits) + (lenbits / 8) + 1;
  }

  public ByteStorable fromBytes(ByteBuffer bb) {
    len = getSize(bb);
    if (vector == null || vector.length < ((len / 8) + 1))
      vector = new byte[(len / 8) + 1];
    bb.get(vector, 0, (len / 8) + 1);
    pos = len / 8;
    bit = len % 8;
    cur = vector[pos];
    return this;
  }

  public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
    if (!doNew)
      return fromBytes(bb);
    BitVector bv = new BitVector();
    bv.len = getSize(bb);
    if (bv.vector == null || bv.vector.length < ((bv.len / 8) + 1))
      bv.vector = new byte[(bv.len / 8) + 1];
    bb.get(bv.vector, 0, (bv.len / 8) + 1);
    bv.pos = bv.len / 8;
    bv.bit = bv.len % 8;
    bv.cur = bv.vector[bv.pos];
    return bv;
  }

  public void toBytes(ByteBuffer bb) {
    endAppend();
    putSize(len(), bb);
    bb.put(vector, 0, (len() / 8) + 1);
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("\nsize of byte vector        = " + vector.length);
    sb.append("\nposition in byte vector    = " + pos);
    sb.append("\nposition in current byte   = " + bit);
    sb.append("\nbyte value of current byte = " + (int) (cur & 0xff));
    sb.append("\nlength of vector in bits   = " + len());
    sb.append("\nbytes for length indicator = " + sizeBytesNeeded(len()));
    int unusedBits = ((vector.length * 8) - len() - (sizeBytesNeeded(len()) * 8));
    sb.append("\nunused bits                = " + unusedBits);
    sb.append("\nfill percentage            = "
        + (((double) len()) / (vector.length * 8)));
    return sb.toString();
  }
  
  public String toString(boolean showBits) {
    String s = toString();
    if (!showBits) return s;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i <= pos; i++) {
      sb.append(org.mellowtech.core.util.DataTypeUtils.printBits(vector[i]) + " ");
      if (i >=3 && ((i+1) % 4) == 0)
        sb.append("\n");
    }
    return s + "\n" + sb.toString();
  }
  
  /**
   * Clear (set to zero) the last 'n' bits in current vector, not including the
   * current bit, since it is not set. Does NOT work with the variableByte
   * methods.
   * 
   * @param n
   *          number of bits to clear
   */
  public void clearLastBits(int n) {
    if (n < bit) {
      // case 1. n < bit, only clear n bits from cur
      cur = (cur >> n);
      bit -= n;
    }
    else {
      // case 2. n >= bit, clear several bytes
      n -= bit; // discard cur, contains bit bits
      pos--;
      int bytes = n / 8; // number of full bytes to clear
      while (bytes > 0) {
        vector[pos--] = (byte) 0;
        bytes--;
      }
      n = n % 8; // number of bits left
      if (n == 0) {
        bit = 0; // first bit in next byte
        cur = 0; // empty
        pos++; // next byte
      }
      else {
        cur = vector[pos]; // current byte
        cur = (cur >> n); // remove last n bits
        bit = 8 - n; // n bits removed
        vector[pos] = (byte) 0;
      }
    }
  }

  /**
   * Get next Gamma coded value.
   * 
   * @return returns next value or -1 when end of vector is reached
   */
  public int getGamma() {
    int b, mag, val;
    for (mag = 0; (b = getBit()) == 0; mag++)
      ;
    if (b < 0)
      return -1;
    val = getBits(mag);
    if (val < 0)
      return -1;
    return (1 << mag) | val;
  }

  /**
   * Standard Gamma (Elias)
   * 
   * @param val
   *          the number to compress
   * @return number of bits put
   */
  public int putGamma(int val) {
    int mag;
    int ret;
    //mag = (int) Math.floor(log2((double) val));     mag = (val <= 1024) ? MAG[val] : (int) Math.floor(log2((double) val));     mag = (val <= 1024) ? MAG[val] : (int) Math.floor(log2((double) val));
    mag = (val <= 1024) ? MAG[val] : (int) Math.floor(log2((double) val));
    ret = putBits(0, mag);
    ret += putBits(val, mag + 1);
    return ret;
  }

  /**
   * Calculate number of bits for val using Elias Gamma coding
   * 
   * @param val
   *          the number to compress
   * @return number of bits
   */
  public int gammaBits(int val) {
    int mag;
    int ret;
    //mag = (int) Math.floor(log2((double) val));     
    mag = (val <= 1024) ? MAG[val] : (int) Math.floor(log2((double) val));
    ret = mag;
    ret += mag + 1;
    return ret;
  }

  /**
   * Get next Delta coded value.
   * 
   * @return returns next value or -1 when end of vector is reached,
   */
  public int getDelta() {
    int mag, val;
    mag = getGamma() - 1;
    if (mag < 0)
      return -1;
    val = getBits(mag);
    if (val < 0)
      return -1;
    return (1 << mag) | val;
  }

  /**
   * Standard Delta (Elias)
   * 
   * @param val
   *          the number to compress
   * @return number of bits put
   */
  public int putDelta(int val) {
    int mag;
    int ret;
    //mag = (int) Math.floor(log2((double) val));     
    mag = (val <= 1024) ? MAG[val] : (int) Math.floor(log2((double) val));
    ret = putGamma(mag + 1);
    ret += putBits(val, mag);
    return ret;
  }

  /**
   * Calculate number of bits for val using Elias Delta coding
   * 
   * @param val
   *          the number to compress
   * @return number of bits
   */
  public int deltaBits(int val) {
    int mag;
    int ret;
    //mag = (int) Math.floor(log2((double) val));     
    mag = (val <= 1024) ? MAG[val] : (int) Math.floor(log2((double) val));
    // ret = putGamma(mag+1);
    ret = gammaBits(mag + 1);
    // ret += putBits(val, mag);
    ret += mag;
    return ret;
  }

  /**
   * Put num bits into the vector from n
   * 
   * @param val
   *          number to put
   * @param num
   *          number of bits to put
   * @return return number of bits written (i.e num)
   */
  public int putBits(int val, int num) {
    int i, ret;
    for (ret = 0, i = num - 1; i >= 0; i--)
      ret += putBit((val >> i) & 0x1);
    return ret;
  }

  /**
   * Get num bits from vector.
   * 
   * @param num
   *          number of bits
   * @return -1 if end of vector
   */
  public int getBits(int num) {
    int mask, shift, val, b;
    b = val = 0;
    for (shift = 8 - bit; num >= shift; num -= shift, shift = 8 - bit) {
      if (8 * pos + bit >= len)
        return -1;
      mask = masks[shift];
      val = (val << shift) | (cur & mask);
      bit = 0;
      pos++;
      cur = vector[pos];
    }
    for (; num > 0 && (b = getBit()) >= 0; num--)
      val = (val << 1) | (b & 0x1);
    return (b < 0) ? -1 : val;
  }

  /**
   * Get next byte from vector. 
   * 
   * @return -1 if end of vector, otherwise the unsigned byte as an integer
   *         value from 0..255
   */
  public int getByte() {
    if (8 * pos + bit >= len)
      return -1;
    int b = getBits(8);
    if (b < 0) 
      return -1;
    return (b & 0xFF);
  }
  
 
  /**
   * Put byte (first 8 bits from num) into vector. 
   * 
   * @return number of bits put, i.e 8
   */
  public int putByte(int num) {
    if (pos >= vector.length) // expand the vector
      expand();
    putBits(num, 8);
    return 8;
  }
  
  public int getAlignedByte() {
    if (8 * pos >= len) 
      return -1;
    return vector[pos++] & 0xFF;
  }
  
  public int putAlignedByte(int num) {
    if (pos >= vector.length) // expand the vector
      expand();
    vector[pos++] = (byte) (num & 0xFF);
    return 8;
  }
  
  
  public int putVariableHalfByte(int num) {
    int c, bits = 0;
    c = (num & 0x07);
    num = (num >> 3);
    while (num > 0) {
      bits += putBits(c,4);
      c = (num & 0x07);
      num = num >> 3;
    }
    bits += putBits(c | 0x08, 4);
    return bits;
  }
  /**
   * Calculate number of bits for value using variable byte coding. 
   * 
   * @param num
   *          the number
   * @return number of bits put
   */
  public int variableHalfByteBits(int num) {
    int bits = 0;
    num = (num >> 3);
    while (num > 0) {
      bits += 4;
      num = num >> 3;
    }
    bits += 4;
    return bits;
  }

  /**
   * Read next variable byte coded integer from vector. 
   * 
   * @return next integer
   */
  public int getVariableHalfByte() {
    int c, num = 0, i = 0;
    c = getBits(4);
    if (c < 0) 
      return -1;
    while ((c & 0x08) == 0) {
      num |= (c << (3 * i));
      c = getBits(4);
      i++;
    }
    num |= ((c & ~(0x08)) << (3 * i));
    return num;
  }
    
  /**
   * Code num by variable byte coding. 
   * 
   * @param num
   *          the number
   * @return number of bits put
   */
  public int putVariableByte(int num) {
    int c, bits = 0;
    c = (num & 0x7F);
    num = (num >> 7);
    while (num > 0) {
      bits += putByte(c);
      c = (num & 0x7F);
      num = num >> 7;
    }
    bits += putByte(c | 0x80);
    return bits;
  }

  /**
   * Calculate number of bits for value using variable byte coding. 
   * 
   * @param num
   *          the number
   * @return number of bits put
   */
  public int variableByteBits(int num) {
    int bits = 0;
    num = (num >> 7);
    while (num > 0) {
      bits += 8;
      num = num >> 7;
    }
    bits += 8;
    return bits;
  }

  /**
   * Read next variable byte coded integer from vector. 
   * 
   * @return next integer
   */
  public int getVariableByte() {
    int c, num = 0, i = 0;
    c = getByte();
    if (c < 0) 
      return -1;
    while ((c & 0x80) == 0) {
      num |= (c << (7 * i));
      c = getByte();
      i++;
    }
    num |= ((c & ~(0x80)) << (7 * i));
    return num;
  }

  
  /**
   * Code num by variable byte coding. 
   * 
   * @param num
   *          the number
   * @return number of bits put
   */
  public int putAlignedVariableByte(int num) {
    int c, bits = 0;
    c = (num & 0x7F);
    num = (num >> 7);
    while (num > 0) {
      bits += putAlignedByte(c);
      c = (num & 0x7F);
      num = num >> 7;
    }
    bits += putAlignedByte(c | 0x80);
    return bits;
  }

  
  /**
   * Read next variable byte coded integer from vector. 
   * 
   * @return next integer
   */
  public int getAlignedVariableByte() {
    int c, num = 0, i = 0;
    c = getAlignedByte();
    if (c < 0) 
      return -1;
    while ((c & 0x80) == 0) {
      num |= (c << (7 * i));
      c = getAlignedByte();
      i++;
    }
    num |= ((c & ~(0x80)) << (7 * i));
    return num;
  }
  
  
  /**
   * Put a bit (zero or one) at current bit position
   * 
   * @param b
   *          zero or one
   * @return number of bits put, i.e 1
   */
  public int putBit(int b) {
    cur = (cur << 1) | (b & 0x1);
    bit++;
    if (bit == 8) { // go to next byte
      if (pos >= vector.length) // expand the vector
        expand();
      vector[pos] = (byte) (cur & 0xff);
      cur = 0;
      pos++;
      bit = 0;
    }
    return 1;
  }

  /**
   * Get bit (zero or one) from current position
   * 
   * @return 0 or 1
   */
  public int getBit() {
    int b;
    if (8 * pos + bit >= len)
      return -1;
    b = (cur >> (7 - bit)) & 0x1;
    bit++;
    if (bit == 8) {
      pos++;
      bit = 0;
      cur = vector[pos];
    }
    return b;
  }

  /**
   * Expand (1.75 times the size of) current vector while keeping the old data
   * intact
   */
  public void expand() {
    byte[] newVector = null;
    int newsize = (int) (((double) vector.length) * 1.75);
    newVector = new byte[newsize];
    System.arraycopy(vector, 0, newVector, 0, vector.length);
    vector = newVector;
  }
  

  /**
   * Get next Golomb number using parameter b
   *
   * @param b coding parameter
   * @return next value
   */
  public int getGolomb(int b) {
    int mag;
    int q, d;
    int rem;
    int ret;
    int slot;
    GolHash golHash;
    
    for(mag = 0; getBit() == 0; mag++);
    
    slot = b % staticGolHashTable.size();
    golHash = staticGolHashTable.get(slot);
        
    if(golHash != null && golHash.b == b)
      q = golHash.q;
    else{
      golHash = new GolHash();
      q = (int) Math.floor(log2 ((double) b));
      golHash.b = b;
      golHash.q = q;
      staticGolHashTable.set(slot, golHash);
    }
        
    rem = getBits(q);   
    d = (1 << (q+1)) - b; 
        
    if(rem >= d)
      rem = ((rem << 1) | getBit()) - d;
    ret =  mag * b + rem + 1;
    ret--;
    return ret; 
  }
  
  /**
   * Code value using Golomb coding with parameter b
   *
   * @param val the value
   * @param b coding parameter
   * @return next value
   */
  public int putGolomb(int val, int b){
    int mag;
    int ret;
    int rem;
    int q;
    int d;

    val++; //ensure non-zero values

    /* work out floor of (x - 1)/b */
    mag = (int) Math.floor(((double) (val - 1)) / ((double) b));    
    ret = putBits(0, mag);
    ret += putBit(1);
    
    rem = val - (mag * b) - 1;
    q = (int) Math.floor(log2((double) b));
    d = (1 << (q+1)) - b; 

    if (rem < d)
      ret += putBits(rem, q);   
    else
      ret += putBits(d+rem, q+1);
    
    return ret; 
  }
  
  /**
   * Calculate number of bits for Golomb code for val with parameter b
   *
   * @param val the value
   * @param b coding parameter
   * @return number of bits for the Golomb code
   */
  public int golombBits(int val, int b){
    int mag;
    int ret;
    int rem;
    int q;
    int d;

    val++; //ensure non-zero values

    /* work out floor of (x - 1)/b */
    mag = (int) Math.floor(((double) (val - 1)) / ((double) b));    
    //ret = putBits(0, mag);
    //ret += putBit(1);
    ret = mag + 1;
    
    rem = val - (mag * b) - 1;
    q = (int) Math.floor(log2((double) b));
    d = (1 << (q+1)) - b; 

    if (rem < d)
      // ret += putBits(rem, q);    
      ret += q;
    else
      // ret += putBits(d+rem, q+1);
      ret += q + 1;
    return ret; 
  }
  
  static final byte[] MAG = new byte[] {
	  (byte) 0,  //0
	  (byte) 0, (byte) 1, (byte) 1, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 3,  //8
	  (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 4,  //16
	  (byte) 4, (byte) 4, (byte) 4, (byte) 4, (byte) 4, (byte) 4, (byte) 4, (byte) 4,  //24
	  (byte) 4, (byte) 4, (byte) 4, (byte) 4, (byte) 4, (byte) 4, (byte) 4, (byte) 5,  //32
	  (byte) 5, (byte) 5, (byte) 5, (byte) 5, (byte) 5, (byte) 5, (byte) 5, (byte) 5,  //40
	  (byte) 5, (byte) 5, (byte) 5, (byte) 5, (byte) 5, (byte) 5, (byte) 5, (byte) 5,  //48
	  (byte) 5, (byte) 5, (byte) 5, (byte) 5, (byte) 5, (byte) 5, (byte) 5, (byte) 5,  //56
	  (byte) 5, (byte) 5, (byte) 5, (byte) 5, (byte) 5, (byte) 5, (byte) 5, (byte) 6,  //64
	  (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6,  //72
	  (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6,  //80
	  (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6,  //88
	  (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6,  //96
	  (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6,  //104
	  (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6,  //112
	  (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6,  //120
	  (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 6, (byte) 7,  //128
	  (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7,  //136
	  (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7,  //144
	  (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7,  //152
	  (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7,  //160
	  (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7,  //168
	  (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7,  //176
	  (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7,  //184
	  (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7,  //192
	  (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7,  //200
	  (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7,  //208
	  (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7,  //216
	  (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7,  //224
	  (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7,  //232
	  (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7,  //240
	  (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7,  //248
	  (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 7, (byte) 8,  //256
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //264
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //272
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //280
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //288
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //296
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //304
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //312
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //320
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //328
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //336
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //344
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //352
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //360
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //368
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //376
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //384
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //392
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //400
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //408
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //416
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //424
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //432
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //440
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //448
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //456
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //464
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //472
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //480
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //488
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //496
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8,  //504
	  (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 9,  //512
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //520
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //528
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //536
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //544
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //552
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //560
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //568
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //576
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //584
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //592
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //600
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //608
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //616
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //624
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //632
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //640
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //648
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //656
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //664
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //672
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //680
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //688
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //696
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //704
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //712
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //720
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //728
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //736
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //744
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //752
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //760
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //768
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //776
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //784
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //792
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //800
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //808
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //816
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //824
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //832
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //840
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //848
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //856
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //864
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //872
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //880
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //888
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //896
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //904
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //912
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //920
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //928
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //936
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //944
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //952
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //960
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //968
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //976
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //984
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //992
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //1000
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //1008
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9,  //1016
	  (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 9, (byte) 10  //1024
  };
	  
  }

