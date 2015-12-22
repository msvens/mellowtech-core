package org.mellowtech.core.util;

import com.google.common.base.Stopwatch;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by msvens on 20/12/15.
 */
public final class TGenerator {

  private static Random rnd = new Random(System.nanoTime());

  private static void incrR(char[] arr, int idx, char from, char to) {
    if (idx == -1)
      return;
    else {
      if (arr[idx] < to) {
        arr[idx]++;
        return;
      }
      arr[idx] = from;
      incrR(arr, idx - 1, from, to);
    }
  }

  private static void incrR(byte[] arr, int idx, byte from, byte to) {
    if (idx == -1)
      return;
    else {
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

  static final <T> Iterator<T> of(Class<T> clazz, int length, char from, char to, boolean mutable) {
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

  static final <T> Iterator<T> of(Class<T> clazz, T start, char from, char to, boolean mutable) {
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

    public CharGen(int length, char from, char to, boolean mutable) {
      this.from = from;
      this.to = to;
      this.mutable = mutable;
      arr = new char[length];
      Arrays.fill(arr, from);
    }

    public CharGen(char[] arr, char from, char to, boolean mutable) {
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

    public ByteGen(int length, byte from, byte to, boolean mutable) {
      this.from = from;
      this.to = to;
      this.mutable = mutable;
      arr = new byte[length];
      Arrays.fill(arr, from);
    }

    public ByteGen(byte[] arr, byte from, byte to, boolean mutable) {
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

    public StringGen(String start, char from, char to) {
      iter = new CharGen(start.toCharArray(), from, to, true);
    }

    public StringGen(int length, char from, char to) {
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
