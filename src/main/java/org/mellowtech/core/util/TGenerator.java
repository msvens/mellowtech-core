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

package org.mellowtech.core.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by msvens on 20/12/15.
 */
public final class TGenerator {

  private static Random rnd = new Random(System.nanoTime());

  private static void incrR(char[] arr, int idx, char from, char to) {
    if (idx != -1) {
      if (arr[idx] < to) {
        arr[idx]++;
        return;
      }
      arr[idx] = from;
      incrR(arr, idx - 1, from, to);
    }
  }

  private static void incrR(byte[] arr, int idx, byte from, byte to) {
    if (idx != -1) {
      if (arr[idx] < to) {
        arr[idx]++;
        return;
      }
      arr[idx] = from;
      incrR(arr, idx - 1, from, to);
    }
  }

  private static void increment(char[] arr) {
    incrR(arr, arr.length - 1, 'A', 'Z');
  }

  @SuppressWarnings("unchecked")
  public static <T> Iterator<T> of(Class<T> clazz, int length, char from, char to, boolean mutable) {
    if (clazz == String.class) {
      return (Iterator<T>) new StringGen(length, from, to);
    } else if (clazz == char[].class) {
      return (Iterator<T>) new CharGen(length, from, to, mutable);
    } else if (clazz == byte[].class) {
      return (Iterator<T>) new ByteGen(length, (byte) from, (byte) to, mutable);
    } else {
      throw new Error("unknown clazz type: " + clazz.getName());
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> Iterator<T> of(Class<T> clazz, T start, char from, char to, boolean mutable) {
    if (clazz == String.class) {
      return (Iterator<T>) new StringGen((String) start, from, to);
    } else if (clazz == char[].class) {
      return (Iterator<T>) new CharGen((char[]) start, from, to, mutable);
    } else if (clazz == byte[].class) {
      return (Iterator<T>) new ByteGen((byte[]) start, (byte) from, (byte) to, mutable);
    } else {
      throw new Error("unknown clazz type: " + clazz.getName());
    }
  }

  public static char[] randomChar(Random r, char[] arr, char from, char to) {
    //10 till 15
    int toAligned = (to + 1) - from;
    for (int i = 0; i < arr.length; i++) {
      char next = (char) (r.nextInt(toAligned) + from);
      arr[i] = next;
    }
    return arr;
  }

  public static String randomStr(Random r, int length, char from, char to) {
    char[] arr = new char[length];
    return new String(randomChar(r, arr, from, to));
  }

  private static class CharGen implements Iterator<char[]> {
    private char[] arr;
    private char from, to;
    private boolean mutable;

    CharGen(int length, char from, char to, boolean mutable) {
      this.from = from;
      this.to = to;
      this.mutable = mutable;
      arr = new char[length];
      Arrays.fill(arr, from);
    }

    CharGen(char[] arr, char from, char to, boolean mutable) {
      this.from = from;
      this.to = to;
      this.mutable = mutable;
      this.arr = arr;
    }

    @Override
    public boolean hasNext() {
      return true;
    }

    @Override
    public char[] next() {
      incrR(arr, arr.length - 1, from, to);
      if (mutable) {
        return arr;
      } else {
        return Arrays.copyOf(arr, arr.length);
      }
    }
  }

  private static class ByteGen implements Iterator<byte[]> {
    private byte[] arr;
    private byte from, to;
    private boolean mutable;

    ByteGen(int length, byte from, byte to, boolean mutable) {
      this.from = from;
      this.to = to;
      this.mutable = mutable;
      arr = new byte[length];
      Arrays.fill(arr, from);
    }

    ByteGen(byte[] arr, byte from, byte to, boolean mutable) {
      this.from = from;
      this.to = to;
      this.mutable = mutable;
      this.arr = arr;
      Arrays.fill(arr, from);
    }

    @Override
    public boolean hasNext() {
      return true;
    }

    @Override
    public byte[] next() {
      incrR(arr, arr.length - 1, from, to);
      if (mutable) {
        return arr;
      } else {
        return Arrays.copyOf(arr, arr.length);
      }
    }
  }

  private static class StringGen implements Iterator<String> {

    Iterator<char[]> iter;

    StringGen(String start, char from, char to) {
      iter = new CharGen(start.toCharArray(), from, to, true);
    }

    StringGen(int length, char from, char to) {
      iter = new CharGen(length, from, to, true);
    }

    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public String next() {
      return new String(iter.next());
    }
  }


}
