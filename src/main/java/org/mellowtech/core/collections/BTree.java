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

package org.mellowtech.core.collections;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.util.RangeIterable;

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
  extends BMap <A,B,C,D>, RangeIterable<KeyValue<B,D>, B>{

  
  //bulk creation:
  void createIndex(Iterator<KeyValue <B,D>> iterator) throws IOException;

  default void createIndex() throws IOException, UnsupportedOperationException{
    throw new UnsupportedOperationException();
  }


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

  @Override
  default Iterator<KeyValue<B,D>> iterator(){
    return iterator(false, null, false, null, false);
  }

  //Iterator<KeyValue <B, D>> iterator();
  /*
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
  */



}
