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
