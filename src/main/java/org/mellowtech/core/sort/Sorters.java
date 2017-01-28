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
import java.util.LinkedList;


/**
 * A number of static methods for sorting data
 * 
 * @author Martin Svensson
 * @version 1.0
 */
@Deprecated
public class Sorters {

  /**
   * Sort objects that are stored in a byte buffer using heap sort.
   * 
   * @param n
   *          start offsets in the buffer
   * @param codec
   *          a comparator for byte level comparisons
   * @param buffer
   *          has to be either a byte[] or java.nio.ByteBuffer
   */
  public static void heapSort(int n[], BCodec<?> codec, Object buffer) {
    if (buffer instanceof ByteBuffer)
      ByteBufferHeap.heapSort(n, (ByteBuffer) buffer, codec);
    else if (buffer instanceof byte[])
      ByteHeap.heapSort(n, (byte[]) buffer, codec);
  }

  /**
   * Sort using heap sort.
   * 
   * @param n
   *          objects to sort
   */
  public static void heapSort(Comparable n[]) {
    Heap.heapSort(n);
  }

  /**
   * Sort objects that are stored in a byte buffer using quickSort.
   * 
   * @param n
   *          start offsets in the buffer
   * @param codec
   *          a comparator for byte level comparisons
   * @param buffer
   *          has to be either a byte[] or java.nio.ByteBuffer
   * @param endPos
   *          only sort up to endPos offsets in the array.
   */
  public static final void quickSort(int n[], BCodec<?> codec, Object buffer,
      int endPos) {
    if (buffer instanceof ByteBuffer) {
      quickSort(n, 0, endPos - 1, (ByteBuffer) buffer, codec);
      insertionSort(n, 0, endPos - 1, (ByteBuffer) buffer, codec);
    }
    else if (buffer instanceof byte[]) {
      quickSort(n, 0, endPos - 1, (byte[]) buffer, codec);
      insertionSort(n, 0, endPos - 1, (byte[]) buffer, codec);
    }
  }

  /**
   * Sort objects that are stored in a byte buffer using quickSort.
   * 
   * @param n
   *          offsets in a byte buffer
   * @param codec
   *          a comparator for byte level comparisons
   * @param buffer
   *          has to be either a byte[] or java.nio.ByteBuffer
   * @see java.nio.ByteBuffer
   */
  public static final void quickSort(int n[], BCodec<?> codec, Object buffer) {
    quickSort(n, codec, buffer, n.length);
  }

  /**
   * Sort objects using non-recursive quickSort.
   * 
   * @param n
   *          objects to sort
   */
  public static final void quickSortStack(Comparable n[]) {
    LinkedList al = new LinkedList();
    al.addFirst(new LeftRight(0, n.length - 1));
    quickSortStack(n, al);
    insertionSort(n, 0, n.length - 1);
  }

  /**
   * Sort objects using non-recursive quickSort.
   * 
   * @param n
   *          objects to sort
   * @param endPos
   *          only sort endPos number of objects
   */
  public static final void quickSortStack(Comparable n[], int endPos) {
    LinkedList al = new LinkedList();
    al.addFirst(new LeftRight(0, endPos - 1));
    quickSortStack(n, al);
    insertionSort(n, 0, endPos - 1);
  }

  /**
   * Sort objects using quickSort.
   * 
   * @param n
   *          objects to sort
   */
  public static final void quickSort(Comparable n[]) {
    quickSort(n, 0, n.length - 1);
    insertionSort(n, 0, n.length - 1);
  }

  /**
   * Sort objects using quickSort.
   * 
   * @param n
   *          objects to sort
   * @param endPos
   *          only sort endPos number of objects
   */
  public static final void quickSort(Comparable n[], int endPos) {
    quickSort(n, 0, endPos - 1);
    insertionSort(n, 0, endPos - 1);
  }

  private static final void quickSort(int n[], int left, int right,
      ByteBuffer bb, BCodec<?> bc) {
    int min = 4;
    int middle, j;
    int tmp;

    if ((right - left) > min) {

      // Tri-median:
      middle = (right + left) / 2;
      if (bc.byteCompare(n[left], n[middle], bb) > 0)
        swap(n, left, middle);
      if (bc.byteCompare(n[left], n[right], bb) > 0)
        swap(n, left, right);
      if (bc.byteCompare(n[middle], n[right], bb) > 0)
        swap(n, middle, right);

      j = right - 1;
      swap(n, middle, j);
      middle = left;
      tmp = n[j];
      for (;;) {
        while (bc.byteCompare(n[++middle], tmp, bb) < 0)
          ;
        while (bc.byteCompare(n[--j], tmp, bb) > 0)
          ;
        if (j < middle)
          break;
        swap(n, middle, j);
      }
      swap(n, middle, right - 1);

      quickSort(n, left, j, bb, bc);
      quickSort(n, middle + 1, right, bb, bc);
    }
  }

  private static void quickSort(int n[], int left, int right, byte[] bb,
      BCodec<?> bc) {
    int min = 4;
    int middle, j;
    int tmp;

    if ((right - left) > min) {

      // Tri-median:
      middle = (right + left) / 2;
      if (bc.byteCompare(n[left], n[middle], bb) > 0)
        swap(n, left, middle);
      if (bc.byteCompare(n[left], n[right], bb) > 0)
        swap(n, left, right);
      if (bc.byteCompare(n[middle], n[right], bb) > 0)
        swap(n, middle, right);

      j = right - 1;
      swap(n, middle, j);
      middle = left;
      tmp = n[j];
      for (;;) {
        while (bc.byteCompare(n[++middle], tmp, bb) < 0)
          ;
        while (bc.byteCompare(n[--j], tmp, bb) > 0)
          ;
        if (j < middle)
          break;
        swap(n, middle, j);
      }
      swap(n, middle, right - 1);

      quickSort(n, left, j, bb, bc);
      quickSort(n, middle + 1, right, bb, bc);
    }
  }

  private static void quickSort(Comparable n[], int left, int right) {
    int min = 4;
    int middle, j;
    Comparable tmp;

    if ((right - left) > min) {

      // Tri-median:
      middle = (right + left) / 2;
      if (n[left].compareTo(n[middle]) > 0)
        swap(n, left, middle);
      if (n[left].compareTo(n[right]) > 0)
        swap(n, left, right);
      if (n[middle].compareTo(n[right]) > 0)
        swap(n, middle, right);

      j = right - 1;
      swap(n, middle, j);
      middle = left;
      tmp = n[j];
      for (;;) {
        while (n[++middle].compareTo(tmp) < 0)
          ;
        while (n[--j].compareTo(tmp) > 0)
          ;
        if (j < middle)
          break;
        swap(n, middle, j);
      }
      swap(n, middle, right - 1);

      quickSort(n, left, j);
      quickSort(n, middle + 1, right);
    }
  }

  private static void quickSortStack(Comparable n[], LinkedList stack) {
    int min = 4;
    int middle, j;
    Comparable tmp;
    int left, right;
    LeftRight lr;

    while (stack.size() > 0) {

      lr = (LeftRight) stack.removeFirst();
      left = lr.left;
      right = lr.right;

      if ((right - left) < min)
        continue;

      // Tri-median:
      middle = (right + left) / 2;
      if (n[left].compareTo(n[middle]) > 0)
        swap(n, left, middle);
      if (n[left].compareTo(n[right]) > 0)
        swap(n, left, right);
      if (n[middle].compareTo(n[right]) > 0)
        swap(n, middle, right);

      j = right - 1;
      swap(n, middle, j);
      middle = left;
      tmp = n[j];
      for (;;) {
        while (n[++middle].compareTo(tmp) < 0)
          ;
        while (n[--j].compareTo(tmp) > 0)
          ;
        if (j < middle)
          break;
        swap(n, middle, j);
      }
      swap(n, middle, right - 1);
      lr.left = middle + 1;
      lr.right = right;
      stack.addFirst(lr);
      stack.addFirst(new LeftRight(left, j));
    }
  }

  private static final void swap(Object[] n, int i, int j) {
    Object tmp;
    tmp = n[i];
    n[i] = n[j];
    n[j] = tmp;
  }

  private static final void swap(int[] n, int i, int j) {
    int tmp;
    tmp = n[i];
    n[i] = n[j];
    n[j] = tmp;
  }

  private static final void insertionSort(Comparable a[], int lo0, int hi0) {
    int i;
    int j;
    Comparable v;

    for (i = lo0 + 1; i <= hi0; i++) {
      v = a[i];
      j = i;
      while ((j > lo0) && (a[j - 1].compareTo(v) > 0)) {
        a[j] = a[j - 1];
        j--;
      }
      a[j] = v;
    }
  }

  private static final void insertionSort(int a[], int lo0, int hi0,
      ByteBuffer bb, BCodec<?> bc) {
    int i;
    int j;
    int v;

    for (i = lo0 + 1; i <= hi0; i++) {
      v = a[i];
      j = i;
      while ((j > lo0) && (bc.byteCompare(a[j - 1], v, bb) > 0)) {
        a[j] = a[j - 1];
        j--;
      }
      a[j] = v;
    }
  }

  private static final void insertionSort(int a[], int lo0, int hi0, byte[] bb,
      BCodec<?> bc) {
    int i;
    int j;
    int v;

    for (i = lo0 + 1; i <= hi0; i++) {
      v = a[i];
      j = i;
      while ((j > lo0) && (bc.byteCompare(a[j - 1], v, bb) > 0)) {
        a[j] = a[j - 1];
        j--;
      }
      a[j] = v;
    }
  }
}

final class LeftRight {
  int left, right;

  public LeftRight(int l, int r) {
    left = l;
    right = r;
  }
}
