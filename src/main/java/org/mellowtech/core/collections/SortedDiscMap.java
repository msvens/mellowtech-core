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

import java.util.Iterator;
import java.util.NavigableMap;
import java.util.SortedMap;

/**
 *
 * Created with IntelliJ IDEA.
 * User: Martin Svensson
 * Date: 2012-10-21
 * Time: 11:04
 * To change this template use File | Settings | File Templates.
 */
public interface SortedDiscMap<K,V> extends DiscMap<K,V>, NavigableMap<K,V>{

    /**
     * Iterate over the entries in this map starting from
     * position pos
     * @param descending - iterate in reverse order
     * @param from - start from or null if no start
     * @param fromInclusive - include the from key in the iterator (if it exists)
     * @param to - iterate until to
     * @param toInclusive - include to in the iterator (if it exists)
     * @return iterator
     */

    Iterator <Entry <K,V>> iterator(boolean descending, K from,
                                    boolean fromInclusive, K to, boolean toInclusive);

    default Iterator <Entry<K,V>> iterator() {
      return iterator(false, null, false, null, false);
    }
    default Iterator <Entry<K,V>> iterator(K from){
        return iterator(false, from, true, null, false);
    }

    default Iterator <Entry<K,V>> iterator(boolean descending){
        return iterator(descending, null, false, null, false);
    }

    default Iterator <Entry<K,V>> iterator(boolean descending, K from) {
        return iterator(descending, from, true, null, false);
    }
}
