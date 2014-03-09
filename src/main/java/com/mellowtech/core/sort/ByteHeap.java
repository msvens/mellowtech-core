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
package com.mellowtech.core.sort;

import com.mellowtech.core.bytestorable.ByteComparable;

/**
 * A Heap that is backed up by a byte[].
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class ByteHeap implements BufferHeap {
  private int heap[];
  private float inc;
  private int size;
  private byte[] bb;
  private ByteComparable bc;

  /**
   * Create a new heap that uses a specified byte[] for comparing objects and a
   * specific comparator for the actal byte comparison
   * 
   * @param bb
   *          buffer of bytes
   * @param bc
   *          byte comparator
   * @exception Exception
   *              if an error occurs
   */
  public ByteHeap(byte[] bb, ByteComparable bc) throws Exception {
    this(100, 2.0f, bb, bc);
  }

  /**
   * Create a new heap that uses a specified byte[] for comparing objects and a
   * specific comparator for the actal byte comparison
   * 
   * @param initSize
   *          preallocate X number of offsets, defaults to 100
   * @param bb
   *          buffer of bytes
   * @param bc
   *          byte comparator
   * @exception Exception
   *              if an error occurs
   */
  public ByteHeap(int initSize, byte[] bb, ByteComparable bc) throws Exception {
    this(initSize, 2.0f, bb, bc);
  }

  /**
   * Create a new heap that uses a specified byte[] for comparing objects and a
   * specific comparator for the actal byte comparison
   * 
   * @param initSize
   *          preallocate X number of offsets
   * @param incrementFactor
   *          how much the array of offsets should grow when it is full,
   *          defaults to 200 %
   * @param bb
   *          buffer of bytes
   * @param bc
   *          byte comparator
   * @exception Exception
   *              if an error occurs
   */
  public ByteHeap(int initSize, float incrementFactor, byte[] bb,
      ByteComparable bc) throws Exception {

    if (bb == null || bc == null)
      throw new Exception("The ByteBuffer and ByteComparable can not be null");
    this.bb = bb;
    this.bc = bc;
    heap = new int[initSize];
    inc = incrementFactor;
    size = 0;
  }

  /**
   * Returns the backing buffer
   * 
   * @return buffer
   */
  public byte[] getBuffer() {
    return bb;
  }

  /** *********IMPLEMENTED BUFFERHEAP METHODS********************** */
  public void insert(int c) throws Exception {
    if (c < 0)
      throw new Exception("Only positive pointers allowed");

    if (size == heap.length)
      resize();

    // bubble up:
    int child = ++size;

    while ((child > 1) && bc.byteCompare(c, heap[(child / 2) - 1], bb) < 0) {
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
    bubbleDown(heap, 1, size, bb, bc);
    return T;
  }

  /** *************END IMPLEMENTED BUFFERHEAP METHODS**************** */

  /**
   * Clears this heap by setting the size to 0
   * 
   */
  public void clear() {
    size = 0;
  }

  /**
   * The number of offsets (objects) in this heap
   * 
   * @return number of offsets
   */
  public int size() {
    return size;
  }

  /**
   * Turn an array of offsets into a heap of offsets.
   * 
   * @param objs
   *          array of offsets
   * @param bb
   *          a buffer that stores the objects
   * @param bc
   *          a byte comparator
   * @return a new ByteBufferHeap
   * @exception Exception
   *              if an error occurs
   */
  public static final ByteHeap heapify(int[] objs, byte[] bb, ByteComparable bc)
      throws Exception {

    ByteHeap h = new ByteHeap(bb, bc);
    int N = objs.length;
    for (int k = N / 2; k > 0; k--) {
      bubbleDown(objs, k, N, bb, bc);
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
   * @param bc
   *          a byte comparator
   */
  public static final void heapSort(int[] objs, byte[] bb, ByteComparable bc) {
    int N = objs.length;
    for (int k = N / 2; k > 0; k--) {
      bubbleDownReverse(objs, k, N, bb, bc);
    }
    do {
      int T = objs[0];
      objs[0] = objs[N - 1];
      objs[N - 1] = T;
      N = N - 1;
      bubbleDownReverse(objs, 1, N, bb, bc);
    }
    while (N > 1);
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < size; i++)
      sb.append(heap[i] + "\t");
    return sb.toString();
  }

  private void resize() {
    int o[] = new int[Math.round(heap.length * inc)];
    System.arraycopy(heap, 0, o, 0, heap.length);
    heap = o;
  }

  private static final void bubbleDown(int[] objs, int node, int max,
      byte[] bb, ByteComparable bc) {

    int T = objs[node - 1];
    int half = max / 2;
    while (node <= half) {
      int j = node + node;

      if ((j < max) && (bc.byteCompare(objs[j - 1], objs[j], bb) > 0)) {
        j++;
      }

      if (bc.byteCompare(T, objs[j - 1], bb) > 0) {
        objs[node - 1] = objs[j - 1];
        node = j;
      }
      else
        break;
    }
    objs[node - 1] = T;
  }

  private static final void bubbleDownReverse(int[] objs, int k, int N,
      byte[] bb, ByteComparable bc) {
    // int T = a[k - 1];
    int T = objs[k - 1];
    while (k <= N / 2) {
      int j = k + k;
      if ((j < N) && bc.byteCompare(objs[j - 1], objs[j], bb) < 0) {
        j++;
      }
      if (bc.byteCompare(T, objs[j - 1], bb) >= 0) {
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
