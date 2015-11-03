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
 * Defines methods for heaps that operates on byte buffers need to support.
 * Heaps that implement this interface should use some sort of byte buffer that
 * stores the actual data.
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public interface BufferHeap {

  /**
   * Insert a new offset into the heap.
   * 
   * @param c
   *          offset
   * @exception Exception
   *              if an error occurs
   */
  void insert(int c) throws Exception;

  /**
   * Detete and return the offset of the smallest object in this heap
   * 
   * @return offset
   */
  int delete();

  /**
   * Return the offset of the smallest object in this heap.
   * 
   * @return offset
   */
  int get();

}
