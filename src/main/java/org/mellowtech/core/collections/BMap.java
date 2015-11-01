/**
 * 
 */
package org.mellowtech.core.collections;

import java.io.IOException;
import java.util.Iterator;

import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;

/**
 * @author msvens
 *
 */
public interface BMap <A,B extends BComparable<A,B>, C, D extends BStorable<C,D>> {
  
  //IO:
  void save() throws IOException;
  void close() throws IOException;
  void delete() throws IOException;

  //size, etc
  /**
   * Return the number of key/value pairs stored in this tree
   * @return number of key/value mappings
   * @throws IOException if an error occurs
   */
  int size() throws IOException;

  /**
   * Check if this map is empty
   * @return true if map is empty
   * @throws IOException if an error occurs
   */
  boolean isEmpty() throws IOException;

  /**
   * Check if the tree contains a mapping for key
   * @param key key to search for
   * @return true if key exists
   * @throws IOException if an error occurs
   */
  boolean containsKey(B key) throws IOException;

  /**
   * Inserts a key into this the Btree. Any previous value will be
   * overwritten.
   *
   * @param key    the key to be inserted
   * @param value  the value to be inserted/updated
   * @throws IOException if an error occurs
   */
  void put(B key, D value) throws IOException;

  /**
   * Inserts a key into this the Btree if it did not previously
   * exist.
   *
   * @param key    the key to be inserted
   * @param value  the value to be inserted/updated
   * @throws IOException if an error occurs
   */
  default void putIfNotExists(B key, D value) throws IOException{
    if(!containsKey(key))
      put(key,value);
  }

  /**
   * Remove a key from this tree.
   *
   * @param key the key to delete
   * @return the value corresponding to the key
   * @throws IOException if an error occurs
   */
  D remove(B key) throws IOException;

  /**
   * Returns the value for a given key. Returns null either if the key was not
   * found or the value is null. Use containsKey if you want to find out if the
   * key is stored in the tree.
   *
   * @param key key to search for.
   * @return value mapped to key.
   * @throws java.io.IOException if an error occurs
   */
  default D get(B key) throws IOException{
    KeyValue <B,D> ret = getKeyValue(key);
    return ret != null ? ret.getValue() : null;
  }

  /**
   * Returns the key/value for a given key or null if the key was not found.
   *
   * @param key key to search for
   * @return value/key pair mapped to key.
   * @throws java.io.IOException if an error occurs
   */
  KeyValue<B,D> getKeyValue(B key) throws IOException;

  //Iterators:
  /**
   * Returns an iterator over the key/value pairs in this btree. The next method
   * returns objects of type KeyValue.
   *
   * @return this tree's iterator
   * @see org.mellowtech.core.collections.KeyValue
   */
  Iterator<KeyValue <B, D>> iterator();


  //Maintenance:

  /**
   * Over time a disc based tree can be fragmented. Ensures optimal
   * disc representation
   * @throws IOException if an error occurs
   */
  void compact() throws IOException, UnsupportedOperationException;

}
