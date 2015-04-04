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

import java.io.IOException;
import java.util.Iterator;

import org.mellowtech.core.bytestorable.ByteComparable;
import org.mellowtech.core.bytestorable.ByteStorable;
import org.mellowtech.core.collections.KeyValue;

/**
 * Representation of a file based sorted tree
 * Date: 2013-03-22
 * Time: 07:06
 *
 * @author Martin Svensson
 */
@SuppressWarnings("rawtypes")
public interface BTree <K extends ByteComparable, V extends ByteStorable> {

  //IO:
  public void save() throws IOException;
  public void close() throws IOException;
  public void delete() throws IOException;
  
  //bulk creation:
  public void createIndex(Iterator<KeyValue <K,V>> iterator) throws IOException;

  //size, etc

  /**
   * Return the number of key/value pairs stored in this tree
   * @return number of key/value mappings
   * @throws IOException
   */
  public int size() throws IOException;

  /**
   * Check if this map is empty
   * @return true if map is empty
   * @throws IOException
   */
  public boolean isEmpty() throws IOException;

  /**
   * Check if the tree contains a mapping for key
   * @param key key to search for
   * @return true if key exists
   * @throws IOException
   */
  public boolean containsKey(K key) throws IOException;

  /**
   * Inserts a key into this the Btree. Any previous value will be
   * overwritten.
   *
   * @param key    the key to be inserted
   * @param value  the value to be inserted/updated
   * @throws IOException if an error occurs
   */
  public void put(K key, V value) throws IOException;

  /**
   * Inserts a key into this the Btree if it did not previously
   * exist.
   *
   * @param key    the key to be inserted
   * @param value  the value to be inserted/updated
   * @throws IOException if an error occurs
   */
  public void putIfNotExists(K key, V value) throws IOException;

  /**
   * Remove a key from this tree.
   *
   * @param key the key to delete
   * @return the value corresponing to the key
   */
  public V remove(K key) throws IOException;

  /**
   * Returns the value for a given key. Returns null either if the key was not
   * found or the value is null. Use containsKey if you want to find out if the
   * key is stored in the tree.
   *
   * @param key key to search for.
   * @return value mapped to key.
   * @throws java.io.IOException if an error occurs
   */
  public V get(K key) throws IOException;

  /**
   * Returns the Key at the given position in the tree.
   * @param position
   * @return
   * @throws IOException
   */
  public K getKey(int position) throws IOException;

  /**
   * Returns the key/value for a given key or null if the key was not found.
   *
   * @param key key to search for
   * @return value/key pair mapped to key.
   * @throws java.io.IOException if an error occurs
   */
  public KeyValue<K,V> getKeyValue(K key) throws IOException;

  /**
   * Returns the tree position for a given key. TreePosition contains the the
   * number of smaller and greater key/value pairs in the block and in the tree
   * as a whole.
   *
   * @param key key to search for
   * @return null if the key was not found
   * @throws java.io.IOException if an error occurs
   */
  public TreePosition getPosition(K key) throws IOException;

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
  public TreePosition getPositionWithMissing(K key) throws IOException;

  //Iterators:
  /**
   * Returns an iterator over the key/value pairs in this btree. The next method
   * returns objects of type KeyValue.
   *
   * @return this tree's iterator
   * @see org.mellowtech.core.collections.KeyValue
   */
  public Iterator<KeyValue <K, V>> iterator();

  /**
   * Returns an iterator over the key/value pairs in this btree starting at a
   * given position.
   *
   * @param from where to start iterating. If the key does not exist start at
   *            position just greater than key
   * @return this tree's iterator
   * @see org.mellowtech.core.collections.KeyValue
   */
  public Iterator<KeyValue <K, V>> iterator(K from);


  //Maintenance:

  /**
   * Over time a discbased tree can be fragmented. Ensures optimal
   * disc representation
   * @throws IOException
   */
  public void compact() throws IOException, UnsupportedOperationException;

}
