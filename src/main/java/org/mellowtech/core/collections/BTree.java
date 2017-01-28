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
import java.util.Iterator;

import org.mellowtech.core.util.RangeIterable;

/**
 * Interface for of a file based sorted tree
 *
 * @param <A> keyType
 * @param <B> valueType
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 */
public interface BTree <A,B>
  extends BMap <A,B>, RangeIterable<KeyValue<A,B>, A>{


  /**
   * Create a sorted tree from an iterator of sorted key-value pairs
   * @param iterator iterator with key-values
   * @throws IOException if an exception occurs
   */
  void createTree(Iterator<KeyValue <A,B>> iterator) throws IOException;

  /**
   * Rebuild any index structure for this sorted tree using existing
   * key values
   * @throws IOException if the index cannot be rebuilt
   * @throws UnsupportedOperationException if this sorted tree does not support the operation
   */
  default void rebuildIndex() throws IOException, UnsupportedOperationException{
    throw new UnsupportedOperationException();
  }


  /**
   * Returns the Key at the given position in the tree.
   * @param position position in the tree
   * @return key
   * @throws IOException if an error occurs
   */
  A getKey(int position) throws IOException;

  /**
   * Returns the tree position for a given key. TreePosition contains the the
   * number of smaller and greater key/value pairs in the block and in the tree
   * as a whole.
   *
   * @param key key to search for
   * @return null if the key was not found
   * @throws IOException if an error occurs
   */
  TreePosition getPosition(A key) throws IOException;

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
  TreePosition getPositionWithMissing(A key) throws IOException;

  @Override
  default Iterator<KeyValue<A,B>> iterator(){
    return iterator(false, null, false, null, false);
  }



}
