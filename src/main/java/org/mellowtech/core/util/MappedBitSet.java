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

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

/**
 * @author msvens
 * @since 24/04/16
 */
public class MappedBitSet implements Iterable<Integer> {

  private final static int ADDRESS_BITS_PER_WORD = 6;
  private final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
  private final static int BIT_INDEX_MASK = BITS_PER_WORD - 1;
  private static final long WORD_MASK = 0xffffffffffffffffL;

  private LongBuffer words;
  private int wordsInUse;
  private final int max;

  public MappedBitSet(int n) {
    checkLow(n-1);
    int len = maxWordsUsed(n);
    words = LongBuffer.allocate(len);
    wordsInUse = 0;
    max = words.capacity() * 64;
  }

  public MappedBitSet(LongBuffer lb) {
    words = lb.duplicate();
    wordsInUse = lb.capacity();
    recalculateWordsInUse();
    max = words.capacity() * 64;
  }

  public MappedBitSet(ByteBuffer bb) {
    this(bb.asLongBuffer());
  }

  public MappedBitSet(Path p, int n) {
    checkLow(n-1);
    long bytes = maxBytesUsed(n);
    try {
      FileChannel fc;
      if (Files.notExists(p)) {
        fc = FileChannel.open(p, StandardOpenOption.CREATE_NEW, StandardOpenOption.READ, StandardOpenOption.WRITE);
        words = (fc.map(FileChannel.MapMode.READ_WRITE, 0, bytes)).asLongBuffer();
        wordsInUse = 0;
        fc.close();
      } else {
        fc = FileChannel.open(p, StandardOpenOption.READ, StandardOpenOption.WRITE);
        words = (fc.map(FileChannel.MapMode.READ_WRITE, 0, bytes)).asLongBuffer();
        wordsInUse = words.capacity();
        recalculateWordsInUse();
        fc.close();
      }
    } catch (Exception e) {
      throw new Error(e);
    }
    max = words.capacity() * 64;
  }

  public static int maxWordsUsed(int n){
    return wordIdx(n-1) + 1;
  }

  public static int maxBytesUsed(int n){
    return maxWordsUsed(n-1) * 8;
  }

  private static int wordIdx(int n) {
    return n >> ADDRESS_BITS_PER_WORD;
  }

  public int cardinality() {
    int sum = 0;
    for (int i = 0; i < wordsInUse; i++)
      sum += Long.bitCount(words.get(i));
    return sum;
  }

  public int capacity() {
    return max;
  }

  public void clear(int n) {
    check(n);
    int idx = wordIdx(n);
    if (idx >= wordsInUse) return;
    long l = words.get(idx) & ~(1L << n);
    words.put(idx, l);
    recalculateWordsInUse();
  }

  public void clear() {
    for (int i = 0; i < wordsInUse; i++) {
      words.put(i, 0);
    }
  }

  public boolean contains(int n) {
    check(n);
    int wordIdx = wordIdx(n);
    long l = words.get(wordIdx) & (1L << n);
    return l != 0;
  }

  public void flip(int n) {
    check(n);
    int idx = wordIdx(n);
    long l = words.get(idx) ^ (1L << n);
    words.put(idx, l);
    recalculateWordsInUse();
  }

  public LongBuffer getBuffer() {
    return words;
  }

  @Override
  public Iterator<Integer> iterator() {
    return new MappedBitSetIterator();
  }

  public Iterator<Integer> iterator(int from) {
    return new MappedBitSetIterator(from);
  }

  public int nextClearBit(int from) {
    check(from);
    int idx = wordIdx(from);
    if (idx >= wordsInUse)
      return from;
    long word = ~(words.get(idx)) & (WORD_MASK << from);
    while (true) {
      if (word != 0)
        return (idx * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
      if (++idx == wordsInUse) {
        int ret = wordsInUse * BITS_PER_WORD;
        return ret >= max ? -1 : ret;
      }
      word = ~(words.get(idx));
    }
  }

  public int nextSetBit(int from) {
    if(from >= max)
      return -1;

    check(from);

    int u = wordIdx(from);

    //System.out.println(u + " " + u);
    if (u >= wordsInUse)
      return -1;

    long word = words.get(u) & (WORD_MASK << from);

    while (true) {
      if (word != 0)
        return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
      if (++u == wordsInUse)
        return -1;
      word = words.get(u);
    }
  }

  public int previousClearBit(int from) {
    if (from >= max)
      from = max - 1;

    if (from == -1) return -1; //why?
    checkLow(from);

    int u = wordIdx(from);
    if (u >= wordsInUse)
      return from;

    long word = ~words.get(u) & (WORD_MASK >>> -(from + 1));

    while (true) {
      if (word != 0)
        return (u + 1) * BITS_PER_WORD - 1 - Long.numberOfLeadingZeros(word);
      if (u-- == 0)
        return -1;
      word = ~words.get(u);
    }
  }

  public int previousSetBit(int from) {
    if (from == -1) return -1; //why?
    //checkInvariants();
    int u = wordIdx(from);
    if (u >= wordsInUse)
      return length() - 1;

    long word = words.get(u) & (WORD_MASK >>> -(from + 1));

    while (true) {
      if (word != 0)
        return (u + 1) * BITS_PER_WORD - 1 - Long.numberOfLeadingZeros(word);
      if (u-- == 0)
        return -1;
      word = words.get(u);
    }
  }

  public void set(int n) {
    check(n);
    int idx = wordIdx(n);
    expandWordsInUse(idx);
    long l = words.get(idx) | (1L << n);
    words.put(idx, l);
  }

  public void set(int n, boolean value){
    if(value)
      set(n);
    else
      clear(n);
  }

  public int length() {
    if (wordsInUse == 0)
      return 0;

    return BITS_PER_WORD * (wordsInUse - 1) +
        (BITS_PER_WORD - Long.numberOfLeadingZeros(words.get(wordsInUse - 1)));
  }

  private void checkLow(int n) {
    if (n < 0)
      throw new IndexOutOfBoundsException(n + " < 0");
  }

  private void checkHigh(int n) {
    if (n >= max)
      throw new IndexOutOfBoundsException(n + " >= " + max);
  }

  private void check(int n) {
    checkLow(n);
    checkHigh(n);
  }

  private void expandWordsInUse(int wordIdx) {
    int req = wordIdx + 1;
    if (wordsInUse < req) {
      wordsInUse = req;
    }
  }

  private void recalculateWordsInUse() {
    // Traverse the bitset until a used word is found
    int i;
    for (i = wordsInUse - 1; i >= 0; i--)
      if (words.get(i) != 0)
        break;
    wordsInUse = i + 1; // The new logical size
  }

  private class MappedBitSetIterator implements Iterator<Integer> {

    int next;
    int n;

    public MappedBitSetIterator() {
      this(0);
    }

    public MappedBitSetIterator(int from) {
      n = next = from;
      getNext();
    }

    @Override
    public boolean hasNext() {
      return n > -1;
    }

    @Override
    public Integer next() {
      int toRet = n;
      getNext();
      return toRet;
    }

    private void getNext() {
      if (next < 0) return;
      n = next = nextSetBit(next);
      if (next > -1) {
        next++;
      }
    }
  }
}
