/**
 * 
 */
package org.mellowtech.core.bytestorable;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.text.Collator;
import java.util.concurrent.atomic.AtomicInteger;

import org.mellowtech.core.bytestorable.ext.CompiledLocale;

/**
 * Utility methods for working with ByteStorables
 * 
 * @author msvens
 *
 */
public class CBUtil {

  public static Charset Utf8 = Charset.forName("UTF-8");

  /********************* ENCODING/DECODING*******************************/
  public static int encodeInt(int val, ByteBuffer bb) {
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

  public static int encodeInt(int val, byte[] b, int offset) {
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.position(offset);
    return encodeInt(val, bb);
  }

  public static final int encodeLong(long val, ByteBuffer bb) {
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

  /******************** Utility methods for ByteStorable Sizes ***************/
  public static final int peekInt(ByteBuffer bb, boolean encoded) {
    int pos = bb.position();
    int toRet = encoded ? decodeInt(bb) : bb.getInt();
    bb.position(pos);
    return toRet;
  }

  public static final int getInt(ByteBuffer bb, boolean encoded) {
    return encoded ? decodeInt(bb) : bb.getInt();
  }

  public static final int putSizeInt(int size, ByteBuffer bb, boolean encoded) {
    if (encoded) {
      return encodeInt(size, bb);
    }
    bb.putInt(size);
    return 4;
  }

  public static final int getSizeInt(ByteBuffer bb, boolean encoded) {
    int val = getInt(bb, encoded);
    return encoded ? encodeLength(val) + val : 4 + val;
  }

  public static final int peekSizeInt(ByteBuffer bb, boolean encoded) {
    int val = peekInt(bb, encoded);
    return encoded ? encodeLength(val) + val : 4 + val;
  }

  /******************* END methods for ByteStorable Sizes ********************/

}
