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
package org.mellowtech.core.collections.tree;

/**
 * Position of a key in a BPlusTree.
 * 
 * @author rickard.coster@asimus.se
 */
public class TreePosition {
  int smaller = -1, elements = -1;
  int smallerInBlock = -1, elementsInBlock = -1;
  boolean exists = true;

  /**
   * Creates a new, empty <code>TreePosition</code> instance.
   */
  public TreePosition() {
  }
  
  /**
   * Creates a new <code>TreePosition</code> instance.
   * 
   * @param smaller
   *          an <code>int</code> value
   * @param elements
   *          an <code>int</code> value
   * @param smallerInBlock
   *          an <code>int</code> value
   * @param elementsInBlock
   *          an <code>int</code> value
   * @param exists
   *          true if exists
   */
  public TreePosition(int smaller, int elements, int smallerInBlock,
      int elementsInBlock, boolean exists) {
    this.smaller = smaller;
    this.exists = exists;
    this.elements = elements;
    this.smallerInBlock = smallerInBlock;
    this.elementsInBlock = elementsInBlock;
  }

  /**
   * Creates a new <code>TreePosition</code> instance.
   * 
   * @param smaller
   *          an <code>int</code> value
   * @param elements
   *          an <code>int</code> value
   * @param smallerInBlock
   *          an <code>int</code> value
   * @param elementsInBlock
   *          an <code>int</code> value
   */
  public TreePosition(int smaller, int elements, int smallerInBlock,
      int elementsInBlock) {
    this.smaller = smaller;
    this.elements = elements;
    this.smallerInBlock = smallerInBlock;
    this.elementsInBlock = elementsInBlock;
  }

  /**
   * Copy the values from 'pos' to this object.
   * 
   * @param pos
   *          a <code>TreePosition</code> value
   */
  public void set(TreePosition pos) {
    smaller = pos.getSmaller();
    elements = pos.getElements();
    smallerInBlock = pos.getSmallerInBlock();
    elementsInBlock = pos.getElementsInBlock();
  }

  /**
   * Set the number of elements in the block where the element is contained.
   * 
   * @param elementsInBlock
   *          an <code>int</code> value
   */
  public void setElementsInBlock(int elementsInBlock) {
    this.elementsInBlock = elementsInBlock;
  }

  /**
   * Get the total number of elements 'smaller' than the current element in the
   * entire BPlusTree.
   * 
   * @return an <code>int</code> value
   */
  public int getSmaller() {
    return smaller;
  }
  
  public boolean exists(){
    return this.exists;
  }

  /**
   * Get the total number of elements in the BPlusTree.
   * 
   * @return an <code>int</code> value
   */
  public int getElements() {
    return elements;
  }

  /**
   * Get number of elements that are smaller than the current element in the
   * block where the element is contained.
   * 
   * @return an <code>int</code> value
   */
  public int getSmallerInBlock() {
    return smallerInBlock;
  }

  /**
   * Get total number of elements in the block where the element is contained.
   * 
   * @return an <code>int</code> value
   */
  public int getElementsInBlock() {
    return elementsInBlock;
  }

  public String toString() {
    return "pos=" + (smaller + 1) + ", " + smallerInBlock + " smaller of "
        + elementsInBlock + " in block, " + smaller + " smaller of " + elements
        + " in total";
  }

  public boolean isExists() {
    return exists;
  }

  public void setExists(boolean exists) {
    this.exists = exists;
  }
}
