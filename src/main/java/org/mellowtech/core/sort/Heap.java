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
package org.mellowtech.core.sort;

/**
 * A Simple Heap (priority queue).
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class Heap {
  private Comparable<?> heap[];
  private float inc;
  private int size;

  private Heap() {
    ;
  }

  /**
   * Create a new heap with an initial capacity.
   * 
   * @param initSize
   *          this heaps initial capacity
   */
  public Heap(int initSize) {
    size = 0;
    heap = new Comparable[initSize];
    inc = 2.0f;
  }

  /**
   * Create a new heap with an inital capacity and specificed growth factor.
   * 
   * @param initSize
   *          this heap's initial capacity
   * @param incrementFactor
   *          how much the heap shold grow when reallocation is needed, defaults
   *          to 200%
   */
  public Heap(int initSize, float incrementFactor) {
    heap = new Comparable[initSize];
    inc = incrementFactor;
    size = 0;
  }

  /**
   * Insert a new object into this heap
   * 
   * @param c
   *          the object to insert
   */
  public void insert(Comparable c) {
    if (size == heap.length)
      resize();

    // bubble up:
    int child = ++size;

    while (child > 1 && c.compareTo(heap[(child / 2) - 1]) < 0) {
      heap[child - 1] = heap[(child / 2) - 1];
      child /= 2;
    }
    heap[child - 1] = c;
  }

  /**
   * Set this heap's size to 0.
   * 
   */
  public void clear() {
    size = 0;
  }

  /**
   * Return the number of objects in this heap
   * 
   * @return number of objects
   */
  public int size() {
    return size;
  }

  /**
   * Get the first (smallest) object in this heap.
   * 
   * @return the smallest object
   */
  public Comparable get() {
    return heap[0];
  }

  public Comparable get(int i) {
    return heap[i];
  }

  /**
   * Delete the first (smallest) object in this heap.
   * 
   * @return the deleted object
   */
  public Comparable delete() {
    if (size == 0)
      return null;
    Comparable T = heap[0];
    size--;
    heap[0] = heap[size];
    bubbleDown(heap, 1, size);
    return T;
  }

  /**
   * Turn an array of objects into a heap.
   * 
   * @param objs
   *          the objects to convert.
   * @return a new heap
   */
  public static Heap heapify(Comparable[] objs) {
    int N = objs.length;
    for (int k = N / 2; k > 0; k--) {
      bubbleDown(objs, k, N);
    }
    Heap h = new Heap();
    h.size = objs.length;
    h.heap = objs;
    return h;
  }

  /**
   * Sort an array of objects.
   * 
   * @param objs
   *          the objects to sort
   */
  public static void heapSort(Comparable[] objs) {
    int N = objs.length;
    for (int k = N / 2; k > 0; k--) {
      bubbleDownReverse(objs, k, N);
    }

    do {
      Comparable T = objs[0];
      objs[0] = objs[N - 1];
      objs[N - 1] = T;
      N = N - 1;
      bubbleDownReverse(objs, 1, N);
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
    Comparable o[] = new Comparable[Math.round(heap.length * inc)];
    System.arraycopy(heap, 0, o, 0, heap.length);
    heap = o;
  }

  private static void bubbleDown(Comparable[] objs, int node, int max) {
    // node = node, objs = objects to bubble, max = max size;
    Comparable T = objs[node - 1];
    int half = max / 2;
    while (node <= half) {
      int j = node + node;

      if ((j < max) && (objs[j - 1].compareTo(objs[j]) > 0)) {
        j++;
      }

      if (T.compareTo(objs[j - 1]) > 0) {
        objs[node - 1] = objs[j - 1];
        node = j;
      }
      else
        break;
    }
    objs[node - 1] = T;
  }

  private static void bubbleDownReverse(Comparable[] objs, int k, int N) {
    // int T = a[k - 1];
    Comparable T = objs[k - 1];
    while (k <= N / 2) {
      int j = k + k;
      if ((j < N) && (objs[j - 1].compareTo(objs[j]) < 0)) {
        j++;
      }
      if (T.compareTo(objs[j - 1]) >= 0) {
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
