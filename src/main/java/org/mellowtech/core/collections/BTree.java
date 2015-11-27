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

package org.mellowtech.core.collections;

import java.io.IOException;
import java.util.Iterator;

import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;

/**
 * Representation of a file based sorted tree
 * Date: 2013-03-22
 * Time: 07:06
 *
 * @param <A> Wrapped BComparable key class
 * @param <B> BComparable key class
 * @param <C> Wrapped BStorable value class
 * @param <D> BStorable value class
 * @author Martin Svensson
 */
public interface BTree <A,B extends BComparable<A,B>, C, D extends BStorable<C,D>> 
  extends BMap <A,B,C,D>{

  
  //bulk creation:
  void createIndex(Iterator<KeyValue <B,D>> iterator) throws IOException;


  /**
   * Returns the Key at the given position in the tree.
   * @param position position in the tree
   * @return key
   * @throws IOException if an error occurs
   */
  B getKey(int position) throws IOException;

  /**
   * Returns the tree position for a given key. TreePosition contains the the
   * number of smaller and greater key/value pairs in the block and in the tree
   * as a whole.
   *
   * @param key key to search for
   * @return null if the key was not found
   * @throws IOException if an error occurs
   */
  TreePosition getPosition(B key) throws IOException;

  /**
   * Returns the tree position for a given key or the position in which the
   * key would have been. TreePosition contains the the
   * number of smaller and greater key/value pairs in the block and in the tree
   * as a whole.
   *
   * @param key key to search for
   * @return null if the key was not found
   * @throws java.io.IOException if an error occurs
   */
  TreePosition getPositionWithMissing(B key) throws IOException;

  /**
   * Returns an iterator over the key/value pairs in this btree starting at a
   * given position.
   *
   * @param descending iterator going from the largest to smallest element
   * @param from where to start iterating. If the key does not exist start at
   *            position just greater than key
   * @param fromInclusive if from exists include it in the iterator
   * @param to where to stop the iteration
   * @param toInclusive if to exists include it in the iterator
   * @return this tree's iterator
   * @see org.mellowtech.core.collections.KeyValue
   */
  Iterator<KeyValue <B, D>> iterator(boolean descending, B from, boolean fromInclusive, B to, boolean toInclusive);


  default Iterator<KeyValue<B,D>> iterator() {
    return iterator(false, null, false, null, false);
  }

  default Iterator <KeyValue<B,D>> iterator(B from){
    return iterator(false, from, true, null, false);
  }

  default Iterator <KeyValue<B,D>> iterator(boolean descending){
    return iterator(descending, null, false, null, false);
  }

  default Iterator <KeyValue<B,D>> iterator(boolean descending, B from){
    return iterator(descending, from, true, null, false);
  }



}
