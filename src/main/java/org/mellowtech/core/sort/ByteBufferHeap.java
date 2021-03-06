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

package org.mellowtech.core.sort;

import org.mellowtech.core.codec.BCodec;

import java.nio.ByteBuffer;


/**
 * A Heap that is backed up by a java.nio.ByteBuffer.
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class ByteBufferHeap <A> implements BufferHeap {

  private int heap[];
  private float inc;
  private int size;
  private ByteBuffer bb;
  //private BComparable<A> bc;
  private BCodec<A> codec;

  /**
   * Create a new heap that uses a specified ByteBuffer for comparing objects
   * and a specific comparator for the actal byte comparison
   * 
   * @param bb
   *          buffer of bytes
   * @param codec
   *          byte comparator
   * @exception Exception
   *              if an error occurs
   */
  public ByteBufferHeap(ByteBuffer bb, BCodec<A> codec) throws Exception {
    this(100, 2.0f, bb, codec);
  }

  /**
   * Create a new heap that uses a specified ByteBuffer for comparing objects
   * and a specific comparator for the actal byte comparison
   * 
   * @param initSize
   *          preallocate X number of offsets
   * @param bb
   *          buffer of bytes
   * @param codec
   *          byte comparator
   * @exception Exception
   *              if an error occurs
   */
  public ByteBufferHeap(int initSize, ByteBuffer bb, BCodec<A> codec)
      throws Exception {
    this(initSize, 2.0f, bb, codec);
  }

  /**
   * Create a new heap that uses a specified ByteBuffer for comparing objects
   * and a specific comparator for the actal byte comparison
   * 
   * @param initSize
   *          preallocate X number of offsets
   * @param incrementFactor
   *          how much the array of offsets should grow when it is full,
   *          defaults to 200 %
   * @param bb
   *          buffer of bytes
   * @param codec
   *          byte comparator
   * @exception Exception
   *              if an error occurs
   */
  public ByteBufferHeap(int initSize, float incrementFactor, ByteBuffer bb,
      BCodec <A> codec) throws Exception {

    if (bb == null || codec == null)
      throw new Exception("The ByteBuffer and ByteComparable can not be null");
    this.bb = bb;
    this.codec = codec;
    heap = new int[initSize];
    inc = incrementFactor;
    size = 0;
  }

  /**
   * Clears this heap by setting the size to 0
   * 
   */
  public void clear() {
    size = 0;
  }

  /** **********IMPLEMENTED BUFFERHEAP METHODS**************************** */
  public void insert(int c) throws Exception {
    if (c < 0)
      throw new Exception("Only positive pointers allowed");

    if (size == heap.length)
      resize();

    // bubble up:
    int child = ++size;

    while ((child > 1) && codec.byteCompare(c, heap[(child / 2) - 1], bb) < 0) {
      heap[child - 1] = heap[(child / 2) - 1];
      child /= 2;
    }
    heap[child - 1] = c;

  }

  public int get() {
    return heap[0];
  }

  public int delete() {
    if (size == 0)
      return -1;

    int T = heap[0];
    size--;
    heap[0] = heap[size];
    bubbleDown(heap, 1, size, bb, codec);
    return T;
  }

  //*********************END IMPLEMENTED BUFFERHEAP METHODS***************

  /**
   * Turn an array of offsets into a heap of offsets.
   * 
   * @param objs
   *          array of offsets
   * @param bb
   *          a buffer that stores the objects
   * @param codec
   *          a byte comparator
   * @param <A> Wrapped BComparable class
   * @return a new ByteBufferHeap
   * @exception Exception
   *              if an error occurs
   */
  public static <A> ByteBufferHeap <A> heapify(int[] objs, ByteBuffer bb,
      BCodec<A> codec) throws Exception {

    ByteBufferHeap <A> h = new ByteBufferHeap <> (bb, codec);
    int N = objs.length;
    for (int k = N / 2; k > 0; k--) {
      bubbleDown(objs, k, N, bb, codec);
    }
    h.size = objs.length;
    h.heap = objs;
    return h;
  }

  /**
   * Sort an array of offsets
   * 
   * @param objs
   *          an array of offsets to be sorted
   * @param bb
   *          the buffer that stores the objects
   * @param codec
   *          a byte comparator
   */
  public static void heapSort(int[] objs, ByteBuffer bb, BCodec <?> codec) {
    int N = objs.length;
    for (int k = N / 2; k > 0; k--) {
      bubbleDownReverse(objs, k, N, bb, codec);
    }
    do {
      int T = objs[0];
      objs[0] = objs[N - 1];
      objs[N - 1] = T;
      N = N - 1;
      bubbleDownReverse(objs, 1, N, bb, codec);
    }
    while (N > 1);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < size; i++)
      sb.append(heap[i]).append("\t");
    return sb.toString();
  }

  private void resize() {
    int o[] = new int[Math.round(heap.length * inc)];
    System.arraycopy(heap, 0, o, 0, heap.length);
    heap = o;
  }

  private static void bubbleDown(int[] objs, int node, int max,
      ByteBuffer bb, BCodec <?> codec) {

    int T = objs[node - 1];
    int half = max / 2;
    while (node <= half) {
      int j = node + node;

      if ((j < max) && (codec.byteCompare(objs[j - 1], objs[j], bb) > 0)) {
        j++;
      }

      if (codec.byteCompare(T, objs[j - 1], bb) > 0) {
        objs[node - 1] = objs[j - 1];
        node = j;
      }
      else
        break;
    }
    objs[node - 1] = T;
  }

  private static void bubbleDownReverse(int[] objs, int k, int N,
      ByteBuffer bb, BCodec <?> codec) {
    // int T = a[k - 1];
    int T = objs[k - 1];
    while (k <= N / 2) {
      int j = k + k;
      if ((j < N) && codec.byteCompare(objs[j - 1], objs[j], bb) < 0) {
        j++;
      }
      if (codec.byteCompare(T, objs[j - 1], bb) >= 0) {
        break;
      }
      else {
        objs[k - 1] = objs[j - 1];
        k = j;
      }
    }
    objs[k - 1] = T;
  }
}
