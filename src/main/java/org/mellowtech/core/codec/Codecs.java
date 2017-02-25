/*
 * Copyright 2015 mellowtech.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mellowtech.core.codec;


import java.util.*;

/**
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 4.0.0
 */
public class Codecs {

  private static BooleanCodec booleanCodec = new BooleanCodec();
  private static ByteCodec byteCodec = new ByteCodec();
  private static ShortCodec shortCodec = new ShortCodec();
  private static IntCodec intCodec = new IntCodec();
  private static LongCodec longCodec = new LongCodec();
  private static FloatCodec floatCodec = new FloatCodec();
  private static DoubleCodec doubleCodec = new DoubleCodec();
  private static CharCodec charCodec = new CharCodec();
  private static DateCodec dateCodec = new DateCodec();
  private static UUIDCodec uuidCodec = new UUIDCodec();
  private static StringCodec stringCodec = new StringCodec();
  private static CharArrayCodec charArrayCodec = new CharArrayCodec();
  private static ByteArrayCodec byteArrayCodec = new ByteArrayCodec();
  private static BitSetCodec bitSetCodec = new BitSetCodec();

  private static Map<Class,BCodec> classCodecs = new HashMap<>();


  public static void addMapping(Class<?> clazz, BCodec<?> codec){
    classCodecs.put(clazz,codec);
  }

  public static <A> BCodec<A> type(A obj) {
    if (obj instanceof Class)
      return fromClass((Class<A>) obj);
    else
      return fromClass((Class<A>) obj.getClass());
  }

  public static final <A> byte toByte(BCodec<A> codec){
    if(codec instanceof BooleanCodec)
      return 1;
    else if(codec instanceof ByteCodec)
      return 2;
    else if(codec instanceof ShortCodec)
      return 3;
    else if(codec instanceof IntCodec)
      return 4;
    else if(codec instanceof LongCodec)
      return 5;
    else if(codec instanceof FloatCodec)
      return 6;
    else if(codec instanceof DoubleCodec)
      return 7;
    else if(codec instanceof CharCodec)
      return 8;
    else if(codec instanceof DateCodec)
      return 9;
    else if(codec instanceof UUIDCodec)
      return 10;
    else if(codec instanceof StringCodec)
      return 11;
    else if(codec instanceof CharArrayCodec)
      return 12;
    else if(codec instanceof ByteArrayCodec)
      return 13;
    else if(codec instanceof BitSetCodec)
      return 14;
    else
      throw new Error("unknown codec");
  }

  public static final <A> BCodec<A> fromByte(byte b){
      if(b == 1)
        return (BCodec<A>) booleanCodec;
      else if(b == 2)
        return (BCodec<A>) byteCodec;
      else if(b == 3)
        return (BCodec<A>) shortCodec;
      else if(b == 4)
        return (BCodec<A>) intCodec;
      else if(b == 5)
        return (BCodec<A>) longCodec;
      else if(b == 6)
        return (BCodec<A>) floatCodec;
      else if(b == 7)
        return (BCodec<A>) doubleCodec;
      else if(b == 8)
        return (BCodec<A>) charCodec;
      else if(b == 9)
        return (BCodec<A>) dateCodec;
      else if(b == 10)
        return (BCodec<A>) uuidCodec;
      else if(b == 11)
        return (BCodec<A>) stringCodec;
      else if(b == 12)
        return (BCodec<A>) charArrayCodec;
      else if(b == 13)
        return (BCodec<A>) byteArrayCodec;
      else if(b == 14)
        return (BCodec<A>) bitSetCodec;
      else
        throw new Error("codec not found");
  }


  public static final <A> BCodec<A> fromClass(Class<A> clazz){
    BCodec codec = classCodecs.get(clazz);
    if(codec != null)
      return (BCodec<A>) codec;
    else if(clazz == Boolean.class)
      return (BCodec<A>) booleanCodec;
    else if(clazz == Byte.class)
      return (BCodec<A>) byteCodec;
    else if(clazz == Short.class)
      return (BCodec<A>) shortCodec;
    else if(clazz == Integer.class)
      return (BCodec<A>) intCodec;
    else if(clazz == Long.class)
      return (BCodec<A>) longCodec;
    else if(clazz == Float.class)
      return (BCodec<A>) floatCodec;
    else if(clazz == Double.class)
      return (BCodec<A>) doubleCodec;
    else if(clazz == Character.class)
      return (BCodec<A>) charCodec;
    else if(clazz == Date.class)
      return (BCodec<A>) dateCodec;
    else if(clazz == UUID.class)
      return (BCodec<A>) uuidCodec;
    else if(clazz == String.class)
      return (BCodec<A>) stringCodec;
    else if(clazz == char[].class)
      return (BCodec<A>) charArrayCodec;
    else if(clazz == byte[].class)
      return (BCodec<A>) byteArrayCodec;
    else if(clazz == BitSet.class)
      return (BCodec<A>) bitSetCodec;
    else
      throw new Error("codec not found");
  }


}
