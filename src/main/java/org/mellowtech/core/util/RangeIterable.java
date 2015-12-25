
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

package org.mellowtech.core.util;

import java.util.Iterator;

/**
 * Created by msvens on 28/11/15.
 */
public interface RangeIterable <A, B> extends Iterable <A> {

  Iterator<A> iterator(boolean descend, B from, boolean fromInclusive, B to, boolean toInclusive);

  default Iterator <A> iterator(B from){
    return iterator(false, from, true, null, false);
  }

  default Iterator <A> iterator(boolean descending){
    return iterator(descending, null, false, null, false);
  }

  default Iterator <A> iterator(boolean descending, B from) {
    return iterator(descending, from, true, null, false);
  }

  @Override
  default Iterator<A> iterator(){
    return iterator(false, null, false, null, false);
  }
}
