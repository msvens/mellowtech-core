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
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Martin Svensson
 * Date: 2012-10-21
 * Time: 11:04
 * To change this template use File | Settings | File Templates.
 */
public interface DiscMap <K,V> extends Map<K,V> {

  /**
   * persist the disc map
   * @throws IOException if an error occurs
   */
  void save() throws IOException;
  
  /**
   * close this disc map. After close has to reopen it again
   * @throws IOException if an error occurs
   */
  void close() throws IOException;


  /**
   * Perform a compaction of this disc map
   * @throws IOException if an error occurs
   */
  void compact() throws IOException, UnsupportedOperationException;

  /**
   * Delete this disc map on disc
   * @throws IOException if an error occurs
   */
  void delete() throws IOException;

  /**
   * Iterate over the entries of this disc map...
   * @return iterator
   */
  Iterator <Entry <K,V>> iterator();


}
