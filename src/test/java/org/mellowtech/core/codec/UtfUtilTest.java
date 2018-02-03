package org.mellowtech.core.codec;

import org.junit.jupiter.api.*;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author msvens
 * @since 4.0.0
 */
class UtfUtilTest {

  static final String one = "$";
  static final String two = "¢";
  static final String three = "€";
  static final String combine = one+two+three;

  @Nested
  @DisplayName("utf length should")
  class UtfLengths{
    @Test
    @DisplayName("return 0 for empty string length")
    void emptyString(){
      assertEquals(0, UtfUtil.utfLength(""));
    }

    @Test
    @DisplayName("return 1 for one byte encoded string")
    void oneByte(){
      assertEquals(1, UtfUtil.utfLength(one));
    }

    @Test
    @DisplayName("return 2 for two byte encoded string")
    void twoByte(){
      assertEquals(2, UtfUtil.utfLength(two));
    }

    @Test
    @DisplayName("return 3 for three byte encoded string")
    void threeByte(){
      assertEquals(3, UtfUtil.utfLength(three));
    }

    @Test
    @DisplayName("return 6 for one+two+three byte encoded string")
    void oneTwoThree(){
      assertEquals(6, UtfUtil.utfLength(one+two+three));
    }

    @Test
    @DisplayName("return 1 for one byte encoded char array")
    void oneByteChars(){
      assertEquals(1, UtfUtil.utfLength(one.toCharArray()));
    }

    @Test
    @DisplayName("return 2 for two byte encoded char array")
    void twoByteChars(){
      assertEquals(2, UtfUtil.utfLength(two.toCharArray()));
    }

    @Test
    @DisplayName("return 3 for three byte encoded char array")
    void threeByteChars(){
      assertEquals(3, UtfUtil.utfLength(three.toCharArray()));
    }

    @Test
    @DisplayName("return 6 for one+two+three byte encoded char array")
    void oneTwoThreeChars(){
      assertEquals(6, UtfUtil.utfLength((one+two+three).toCharArray()));
    }
  }


  @Nested
  @DisplayName("encoded should")
  class Encode{
    @Test
    @DisplayName("create a byte array of 6 chars from char[]")
    void encodeLengthCharArr(){
      assertEquals(6, UtfUtil.encode((one+two+three).toCharArray()).length);
    }

    @Test
    @DisplayName("when decode produce the same char[] as was encoded")
    void encodeDecodeCharArr(){
      String cmp = (one+two+three);
      byte[] enc = UtfUtil.encode(cmp.toCharArray());
      assertEquals(0, cmp.compareTo(UtfUtil.decode(enc)));
    }

    @Test
    @DisplayName("produce an empty array when encoding an empty char[]")
    void encodeEmptyCharArr(){
      assertEquals(0, UtfUtil.encode("".toCharArray()).length);
    }

    @Test
    @DisplayName("create a byte array of 6 chars")
     void encodeLength(){
      assertEquals(6, UtfUtil.encode(one+two+three).length);
    }

    @Test
    @DisplayName("when decode produce the same string as was encoded")
    void encodeDecode(){
      String cmp = one+two+three;
      byte[] enc = UtfUtil.encode(cmp);
      assertEquals(0, cmp.compareTo(UtfUtil.decode(enc)));
    }

    @Test
    @DisplayName("produce an empty array when encoding an empty string")
    void encodeEmpty(){
      assertEquals(0, UtfUtil.encode("").length);
    }



    @Test
    @DisplayName("throw null pointer expection for null input")
    void encodeNull(){
      assertThrows(NullPointerException.class, () -> {UtfUtil.encode((String) null);});
    }


  }

  @Nested
  @DisplayName("decode string should")
  class DecodeString{
    @Test
    @DisplayName("decode to orginal string")
    void decode(){
      byte[] encoded = UtfUtil.encode(combine);
      String decode = UtfUtil.decode(encoded);
      assertEquals(combine, decode);
    }

    @Test
    @DisplayName("decode a single char string")
    void decodeSingle(){
      byte[] encoded = UtfUtil.encode(one);
      String decoded = UtfUtil.decode(encoded);
      assertEquals(one, decoded);
    }

    @Test
    @DisplayName("decode empty string to empty string")
    void decodeEmpty(){
      byte[] encoded = UtfUtil.encode("");
      String decode = UtfUtil.decode(encoded);
      assertEquals("", decode);
    }

    @Test
    @DisplayName("throw an expecption when decoding null input")
    void decodeNull(){
      assertThrows(NullPointerException.class, () -> {
        UtfUtil.decode(null);
      });
    }
  }

  @Nested
  @DisplayName("decode chars should")
  class DecodeChars{
    @Test
    @DisplayName("decode to orginal char[]")
    void decode(){
      byte[] encoded = UtfUtil.encode(combine.toCharArray());
      char[] decode = UtfUtil.decodeChars(encoded);
      assertArrayEquals(combine.toCharArray(), decode);
    }

    @Test
    @DisplayName("decode a single char[]")
    void decodeSingle(){
      byte[] encoded = UtfUtil.encode(one.toCharArray());
      char[] decoded = UtfUtil.decodeChars(encoded);
      assertArrayEquals(one.toCharArray(), decoded);
    }

    @Test
    @DisplayName("decode empty string to empty string")
    void decodeEmpty(){
      byte[] encoded = UtfUtil.encode("".toCharArray());
      char[] decode = UtfUtil.decodeChars(encoded);
      assertArrayEquals("".toCharArray(), decode);
    }

    @Test
    @DisplayName("throw an expecption when decoding null input")
    void decodeNull(){
      assertThrows(NullPointerException.class, () -> {
        UtfUtil.decodeChars(null);
      });
    }
  }

  @Nested
  @DisplayName("byte[] compare should")
  class ByteArrayCompare{
    StringCodec codec = new StringCodec();
    @Test
    @DisplayName("return 0 when comparing one and one")
    void cmpOne(){
      byte[] b1 = codec.to(one).array();
      byte[] b2 = codec.to(one).array();
      int cmp = UtfUtil.compare(b1, 0, b2, 0);
      assertEquals(0, cmp);
    }
    @Test
    @DisplayName("return 0 when comparing two and two")
    void cmpTwo(){
      byte[] b1 = codec.to(two).array();
      byte[] b2 = codec.to(two).array();
      int cmp = UtfUtil.compare(b1, 0, b2, 0);
      assertEquals(0, cmp);
    }
    @Test
    @DisplayName("return 0 when comparing three and three")
    void cmpThree(){
      byte[] b1 = codec.to(three).array();
      byte[] b2 = codec.to(three).array();
      int cmp = UtfUtil.compare(b1, 0, b2, 0);
      assertEquals(0, cmp);
    }
    @Test
    @DisplayName("return 0 when comparing combined and combined")
    void cmpCombine(){
      byte[] b1 = codec.to(combine).array();
      byte[] b2 = codec.to(combine).array();
      int cmp = UtfUtil.compare(b1, 0, b2, 0);
      assertEquals(0, cmp);
    }
    @Test
    @DisplayName("return 0 when comparing two empty strings")
    void cmpZero(){
      byte[] b1 = codec.to("").array();
      byte[] b2 = codec.to("").array();
      int cmp = UtfUtil.compare(b1, 0, b2, 0);
      assertEquals(0, cmp);
    }
    @Test
    @DisplayName("throw expetion when comparing null values")
    void cmpNull(){
      assertThrows(NullPointerException.class, () -> {
        UtfUtil.compare((byte[])null, 0, (byte[])null, 0);
      });
    }
    @Test
    @DisplayName("should return > 0 when comparing two to one")
    void cmpTwoOne(){
      byte[] b1 = codec.to(two).array();
      byte[] b2 = codec.to(one).array();
      int cmp = UtfUtil.compare(b1, 0, b2, 0);
      assertTrue(cmp > 0);
    }
    @Test
    @DisplayName("should return > 0 when comparing three to two")
    void cmpThreeTwo(){
      byte[] b1 = codec.to(three).array();
      byte[] b2 = codec.to(two).array();
      int cmp = UtfUtil.compare(b1, 0, b2, 0);
      assertTrue(cmp > 0);
    }
    @Test
    @DisplayName("should return > 0 when comparing twothreetwo to twothreeone")
    void cmpTwoThreeTwo(){
      byte[] b1 = codec.to(two+three+two).array();
      byte[] b2 = codec.to(two+three+one).array();
      int cmp = UtfUtil.compare(b1, 0, b2, 0);
      assertTrue(cmp > 0);
    }
  }

  @Nested
  @DisplayName("bytebuffer compare should")
  class ByteBufferCompare{
    StringCodec codec = new StringCodec();
    @Test
    @DisplayName("return 0 when comparing one and one")
    void cmpOne(){
      ByteBuffer b1 = codec.to(one);
      ByteBuffer b2 = codec.to(one);
      int cmp = UtfUtil.compare(b1, 0, b2, 0);
      assertEquals(0, cmp);
    }
    @Test
    @DisplayName("return 0 when comparing two and two")
    void cmpTwo(){
      ByteBuffer b1 = codec.to(two);
      ByteBuffer b2 = codec.to(two);
      int cmp = UtfUtil.compare(b1, 0, b2, 0);
      assertEquals(0, cmp);
    }
    @Test
    @DisplayName("return 0 when comparing three and three")
    void cmpThree(){
      ByteBuffer b1 = codec.to(three);
      ByteBuffer b2 = codec.to(three);
      int cmp = UtfUtil.compare(b1, 0, b2, 0);
      assertEquals(0, cmp);
    }
    @Test
    @DisplayName("return 0 when comparing combined and combined")
    void cmpCombine(){
      ByteBuffer b1 = codec.to(combine);
      ByteBuffer b2 = codec.to(combine);
      int cmp = UtfUtil.compare(b1, 0, b2, 0);
      assertEquals(0, cmp);
    }
    @Test
    @DisplayName("return 0 when comparing two empty strings")
    void cmpZero(){
      ByteBuffer b1 = codec.to("");
      ByteBuffer b2 = codec.to("");
      int cmp = UtfUtil.compare(b1, 0, b2, 0);
      assertEquals(0, cmp);
    }
    @Test
    @DisplayName("throw expetion when comparing null values")
    void cmpNull(){
      assertThrows(NullPointerException.class, () -> {
        UtfUtil.compare((ByteBuffer)null, 0, (ByteBuffer)null, 0);
      });
    }
    @Test
    @DisplayName("should return > 0 when comparing two to one")
    void cmpTwoOne(){
      ByteBuffer b1 = codec.to(two);
      ByteBuffer b2 = codec.to(one);
      int cmp = UtfUtil.compare(b1, 0, b2, 0);
      assertTrue(cmp > 0);
    }
    @Test
    @DisplayName("should return > 0 when comparing three to two")
    void cmpThreeTwo(){
      ByteBuffer b1 = codec.to(three);
      ByteBuffer b2 = codec.to(two);
      int cmp = UtfUtil.compare(b1, 0, b2, 0);
      assertTrue(cmp > 0);
    }
    @Test
    @DisplayName("should return > 0 when comparing twothreetwo to twothreeone")
    void cmpTwoThreeTwo(){
      ByteBuffer b1 = codec.to(two+three+two);
      ByteBuffer b2 = codec.to(two+three+one);
      int cmp = UtfUtil.compare(b1, 0, b2, 0);
      assertTrue(cmp > 0);
    }
  }

  @Nested
  @DisplayName("bytebuffer cmp should")
  class ByteBufferCmp{
    @Test
    @DisplayName("return 0 when comparing one and one")
    void cmpOne(){
      ByteBuffer b1 = ByteBuffer.wrap(UtfUtil.encode(one));
      ByteBuffer b2 = ByteBuffer.wrap(UtfUtil.encode(one));
      int cmp = UtfUtil.cmp(b1, 0, b2, 0, b1.capacity(), b2.capacity());
      assertEquals(0, cmp);
    }
    @Test
    @DisplayName("return 0 when comparing two and two")
    void cmpTwo(){
      ByteBuffer b1 = ByteBuffer.wrap(UtfUtil.encode(two));
      ByteBuffer b2 = ByteBuffer.wrap(UtfUtil.encode(two));
      int cmp = UtfUtil.cmp(b1, 0, b2, 0, b1.capacity(), b2.capacity());
      assertEquals(0, cmp);
    }
    @Test
    @DisplayName("return 0 when comparing three and three")
    void cmpThree(){
      ByteBuffer b1 = ByteBuffer.wrap(UtfUtil.encode(three));
      ByteBuffer b2 = ByteBuffer.wrap(UtfUtil.encode(three));
      int cmp = UtfUtil.cmp(b1, 0, b2, 0, b1.capacity(), b2.capacity());
      assertEquals(0, cmp);
    }
    @Test
    @DisplayName("return 0 when comparing combined and combined")
    void cmpCombine(){
      ByteBuffer b1 = ByteBuffer.wrap(UtfUtil.encode(combine));
      ByteBuffer b2 = ByteBuffer.wrap(UtfUtil.encode(combine));
      int cmp = UtfUtil.cmp(b1, 0, b2, 0, b1.capacity(), b2.capacity());
      assertEquals(0, cmp);
    }
    @Test
    @DisplayName("return 0 when comparing two empty strings")
    void cmpZero(){
      ByteBuffer b1 = ByteBuffer.wrap(UtfUtil.encode(""));
      ByteBuffer b2 = ByteBuffer.wrap(UtfUtil.encode(""));
      int cmp = UtfUtil.cmp(b1, 0, b2, 0, b1.capacity(), b2.capacity());
      assertEquals(0, cmp);
    }
    @Test
    @DisplayName("throw expetion when comparing null values")
    void cmpNull(){
      assertThrows(NullPointerException.class, () -> {
        UtfUtil.cmp(null, 0, null, 0, 10, 10);
      });
    }
    @Test
    @DisplayName("should return > 0 when comparing two to one")
    void cmpTwoOne(){
      ByteBuffer b1 = ByteBuffer.wrap(UtfUtil.encode(two));
      ByteBuffer b2 = ByteBuffer.wrap(UtfUtil.encode(one));
      int cmp = UtfUtil.cmp(b1, 0, b2, 0, b1.capacity(), b2.capacity());
      assertTrue(cmp > 0);
    }
    @Test
    @DisplayName("should return > 0 when comparing three to two")
    void cmpThreeTwo(){
      ByteBuffer b1 = ByteBuffer.wrap(UtfUtil.encode(three));
      ByteBuffer b2 = ByteBuffer.wrap(UtfUtil.encode(two));
      int cmp = UtfUtil.cmp(b1, 0, b2, 0, b1.capacity(), b2.capacity());
      assertTrue(cmp > 0);
    }
    @Test
    @DisplayName("should return > 0 when comparing twothreetwo to twothreeone")
    void cmpTwoThreeTwo(){
      ByteBuffer b1 = ByteBuffer.wrap(UtfUtil.encode(two+three+two));
      ByteBuffer b2 = ByteBuffer.wrap(UtfUtil.encode(two+three+one));
      int cmp = UtfUtil.cmp(b1, 0, b2, 0, b1.capacity(), b2.capacity());
      assertTrue(cmp > 0);
    }
  }

}
