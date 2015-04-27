/**
 * 
 */
package org.mellowtech.core.bytestorable;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Utility methods for working with ByteStorables
 * 
 * @author msvens
 *
 */
public class CBUtil {

  public static Charset Utf8 = Charset.forName("UTF-8");

  /********************* ENCODING/DECODING*******************************/
  /**
   * Variable encode an int. Value has to be greater or equal to zero.
   * @param val value to encode
   * @param bb buffer to store it
   * @return number of bytes
   */
  public static int encodeInt(int val, ByteBuffer bb) {
    if(val < 0) throw new ByteStorableException("negative value");
    int c, count = 1;
    c = (val & 0x7F);
    val = (val >> 7);
    while (val > 0) {
      bb.put((byte) (c & 0xFF));
      c = (val & 0x7F);
      val = val >> 7;
      count++;
    }
    c = (c | 0x80);
    bb.put((byte) (c & 0xFF));
    return count;
  }

  /**
   * Variable encode an int. Value has to be greater or equal to zero.
   * @param val value to encode
   * @param b buffer to store it
   * @param offset position in buffer
   * @return number of bytes
   */
  public static int encodeInt(int val, byte[] b, int offset) {
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.position(offset);
    return encodeInt(val, bb);
  }

  /**
   * Variable encode a long. Value has to be greater or equal to zero.
   * @param val value to encode
   * @param bb buffer to store it
   * @return number of bytes
   */
  public static final int encodeLong(long val, ByteBuffer bb) {
    if(val < 0) throw new ByteStorableException("negative value");
    long c;
    int count = 1;
    c = (val & 0x7F);
    val = (val >> 7);
    while (val > 0) {
      bb.put((byte) (c & 0xFF));
      c = (val & 0x7F);
      val = val >> 7;
      count++;
    }
    c = (c | 0x80);
    bb.put((byte) (c & 0xFF));
    return count;
  }

  /**
   * Variable encode a long. Value has to be greater or equal to zero.
   * @param val value to encode
   * @param b buffer to store it
   * @param offset position in buffer
   * @return number of bytes
   */
  public static final int encodeLong(long val, byte[] b, int offset) {
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.position(offset);
    return encodeLong(val, bb);
  }

  public static final int encodeLength(int val) {
    int count = 1;
    val = (val >> 7);
    while (val > 0) {
      count++;
      val = val >> 7;
    }
    return count;
  }

  public static final int encodeLength(long val) {
    int count = 1;
    val = (val >> 7);
    while (val > 0) {
      count++;
      val = val >> 7;
    }
    return count;
  }

  public static int decodeInt(ByteBuffer bb) {
    int c, num = 0, i = 0;

    c = (bb.get() & 0xFF);
    while ((c & 0x80) == 0) {
      num |= (c << (7 * i));
      c = (bb.get() & 0xFF);
      i++;
    }
    num |= ((c & ~(0x80)) << (7 * i));
    return num;
  }

  public static int decodeInt(byte[] b, int offset) {
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.position(offset);
    return decodeInt(bb);
  }

  public static long decodeLong(ByteBuffer bb) {
    long c, i = 0;
    long num = 0;

    c = (bb.get() & 0xFF);
    while ((c & 0x80) == 0) {
      num |= (c << (7 * i));
      c = (bb.get() & 0xFF);
      i++;
    }
    num |= ((c & ~(0x80)) << (7 * i));
    return num;
  }

  public static long decodeLong(byte[] b, int offset) {
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.position(offset);
    return decodeLong(bb);
  }

  //******************** Utility methods for ByteStorable Sizes ***************/
  public static final int peekInt(ByteBuffer bb, boolean encoded) {
    int pos = bb.position();
    int toRet = encoded ? decodeInt(bb) : bb.getInt();
    bb.position(pos);
    return toRet;
  }

  public static final int getInt(ByteBuffer bb, boolean encoded) {
    return encoded ? decodeInt(bb) : bb.getInt();
  }

  public static final int putSize(int size, ByteBuffer bb, boolean encoded) {
    if (encoded) {
      return encodeInt(size, bb);
    }
    bb.putInt(size);
    return 4;
  }

  public static final int getSize(ByteBuffer bb, boolean encoded) {
    return getInt(bb, encoded);
  }
  
  public static final int byteSize(int val, boolean encoded) {
    if(val < 0 || val >= Integer.MAX_VALUE - 5) 
      throw new ByteStorableException("valute out of range");
    return encoded ? encodeLength(val) + val : 4 + val;
  }

  public static final int peekSize(ByteBuffer bb, boolean encoded) {
    int val = peekInt(bb, encoded);
    return encoded ? encodeLength(val) + val : 4 + val;
  }

  //******************* END methods for ByteStorable Sizes ********************
  @SuppressWarnings("unchecked")
  public final static <A,B extends BComparable <A,B>> B separate(B first, B second){
    if(first instanceof CBString){
      return (B) separate((CBString) first, (CBString) second);
    }
    else if(first instanceof CBCharArray){
      return (B) separate((CBCharArray)first, (CBCharArray) second);
    }
    return first.compareTo(second) < 0 ? first : second;
  }
  
  public final static CBString separate(CBString first, CBString second){
    String small, large;

    if (first.compareTo(second) < 0) {
      small = ((CBString) first).get();
      large = ((CBString) second).get();
    }
    else {
      large = ((CBString) first).get();
      small = ((CBString) second).get();
    }
    int i;
    for(i = 0; i < small.length(); i++){
      if(small.charAt(i) != large.charAt(i))
        break;
    }

    //CBString newStr = new CBString();
    if (small.length() == large.length() && i == large.length()) {
      return new CBString(large);
    }
    
    return new CBString((new String(large.substring(0, i + 1))));
  }
  
  public final static CBCharArray separate(CBCharArray first, CBCharArray second){
      char[] small, large;

      if (first.compareTo(second) < 0) {
        small = first.get();
        large = second.get();
      }
      else {
        large = first.get();
        small = second.get();
      }
      int i;
      for(i = 0; i < small.length; i++){
        if(small[i] != large[i])
          break;
      }

      
      if (small.length == large.length && i == large.length) {
        return new CBCharArray(large);
      }
      return new CBCharArray((Arrays.copyOf(large, i+1)));
  }
  
  
  
  /**
   * Starting at the current position copy a number bytes to the beginning of
   * the buffer and set the new postion to just after the copied bytes.
   * 
   * @param bb
   *          buffer
   * @param numBytes
   *          number of bytes to copy
   */
  public final static void copyToBeginning(ByteBuffer bb, int numBytes) {
    if (numBytes == 0) {
      bb.clear();
      return;
    }
    byte[] b = new byte[numBytes];
    bb.get(b);
    bb.clear();
    bb.put(b);
  }

  /**
   * Calculates the number of bytes that the next ByteStorable will need to be
   * fully read. If the buffer does not fully contain the next ByteStorable it
   * will return the number of bytes that are left in this buffer as a negative
   * value.
   * 
   * @param bb
   *          a buffer
   * @param template
   *          a ByteStorable template to be used for calculating the size
   * @return the next ByteStorable's size or -(bytes left in buffer)
   */
  public final static int slackOrSize(ByteBuffer bb, BStorable <?,?> template) {
    int left = bb.remaining();
    if (bb.remaining() < 4){
      return -left;
    }
    bb.mark();
    int bSize = template.byteSize(bb);
    bb.reset();
    if (bSize > left){
      return -left;
    }
    return bSize;
  }

}
