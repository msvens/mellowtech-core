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
package org.mellowtech.core.cache;

/**
 * The Remover is used as the remover function when the various caches
 * remove a key/value from their cache.
 * @param <A> key
 * @param <B> value
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public interface Loader<A, B> {


  /**
   * Load a key from backend.
   * @param key the key
   * @return value
   * @throws NoSuchValueException if key is not found
   * @throws Exception if error
   */
  B get(A key) throws Exception, NoSuchValueException;
}
