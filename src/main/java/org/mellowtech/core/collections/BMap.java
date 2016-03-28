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

import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;

import java.io.IOException;
import java.util.Iterator;

/**
 * Interface for a file based key-value map
 *
 * @param <A> Wrapped BComparable key class
 * @param <B> BComparable key class
 * @param <C> Wrapped BStorable value class
 * @param <D> BStorable value class
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 */
public interface BMap<A, B extends BComparable<A, B>, C, D extends BStorable<C, D>> {

  /**
   * Increase the capacity of this map
   */
  default void expand() throws IOException, UnsupportedOperationException{
    throw new UnsupportedOperationException();
  }

  /**
   * Close this map. After a close it has to be reopened
   *
   * @throws IOException if operation fails
   */
  void close() throws IOException;

  /**
   * Optional operation. Optimize (e.g. defragement, remove) any
   * file resources
   *
   * @throws IOException if an error occurs
   */
  default void compact() throws IOException, UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * Check if the tree contains a mapping for key
   *
   * @param key key to search for
   * @return true if key exists
   * @throws IOException if an error occurs
   */
  boolean containsKey(B key) throws IOException;

  /**
   * Delete this map and any file resources attached to it
   *
   * @throws IOException if operation fails
   */
  void delete() throws IOException;

  /**
   * Returns the value for a given key. Returns null if the key was not
   * found or the value is null. Use containsKey if you want to find out if the
   * key is stored in the map.
   *
   * @param key key to search for.
   * @return value mapped to key.
   * @throws java.io.IOException if an error occurs
   */
  default D get(B key) throws IOException {
    KeyValue<B, D> ret = getKeyValue(key);
    return ret != null ? ret.getValue() : null;
  }

  /**
   * Returns the key/value for a given key or null if the key was not found.
   *
   * @param key key to search for
   * @return value/key pair mapped to key.
   * @throws java.io.IOException if an error occurs
   */
  KeyValue<B, D> getKeyValue(B key) throws IOException;

  /**
   * Check if this map is empty
   *
   * @return true if map is empty
   * @throws IOException if an error occurs
   */
  boolean isEmpty() throws IOException;

  /**
   * Returns an iterator over the key/value pairs in this map.
   *
   * @return this tree's iterator
   * @see org.mellowtech.core.collections.KeyValue
   */
  Iterator<KeyValue<B, D>> iterator();

  /**
   * Insert a key into this the map. Any previous value will be
   * overwritten.
   *
   * @param key   the key to be inserted
   * @param value the value to be inserted/updated
   * @throws IOException if an error occurs
   */
  void put(B key, D value) throws IOException;

  /**
   * Insert a key into this the map if and only if the key
   * did not exist in the map.
   *
   * @param key   the key to be inserted
   * @param value the value to be inserted/updated
   * @throws IOException if an error occurs
   */
  default void putIfNotExists(B key, D value) throws IOException {
    if (!containsKey(key))
      put(key, value);
  }

  /**
   * Remove a key from this map.
   *
   * @param key the key to delete
   * @return the value corresponding to the key
   * @throws IOException if an error occurs
   */
  D remove(B key) throws IOException;

  /**
   * Save this map to disc
   *
   * @throws IOException if operation fails
   */
  void save() throws IOException;

  /**
   * Return the number of keys pairs stored in this tree
   *
   * @return number of keys mappings
   * @throws IOException if an error occurs
   */
  int size() throws IOException;

  /**
   * Empty this map
   *
   * @throws IOException if operation fails
   */
  void truncate() throws IOException;


}
