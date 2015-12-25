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

package org.mellowtech.core.collections.impl;

import org.mellowtech.core.collections.SortedDiscMap;

import java.util.*;

/**
 * Created by msvens on 14/11/15.
 */
abstract class AbstractRangeMap<A,B>{

  protected boolean fromInclusive, toInclusive = false;
  private boolean descending = false;
  protected A from, to;
  protected SortedDiscMap<A,B> map;


  public AbstractRangeMap(SortedDiscMap<A,B> shared, boolean descending,
                          A from, boolean fromInclusive, A to, boolean toInclusive){

    this.map = shared;
    this.descending = descending;
    this.from = from;
    this.to = to;
    this.toInclusive = toInclusive;
    this.fromInclusive = fromInclusive;
  }

  @SuppressWarnings("unchecked")
  protected boolean fromCheck(A key) {
    if(from == null) return true;
    if (descending) {
      return fromInclusive ? ((Comparable<? super A>) from).compareTo(key) >= 0 :  ((Comparable<? super A>) from).compareTo(key) < 0;
    } else {
      return fromInclusive ? ((Comparable<? super A>) from).compareTo(key) <= 0 : ((Comparable<? super A>) from).compareTo(key) < 0;
    }
  }

  @SuppressWarnings("unchecked")
  protected boolean toCheck(A key) {
    if (to == null) return true;
    if (descending) {
      return toInclusive ?  ((Comparable<? super A>) to).compareTo(key) <= 0 : ((Comparable<? super A>) to).compareTo(key) > 0;
    } else {
      return toInclusive ? ((Comparable<? super A>) to).compareTo(key) >= 0 : ((Comparable<? super A>) to).compareTo(key) > 0;
    }
  }

  protected boolean check(A key){
    return fromCheck(key) && toCheck(key);
  }

  protected boolean noBounds(){
    return (from == null) && (to == null);
  }

  protected Map.Entry<A,B> checkOrNull(Map.Entry<A,B> e){
    return e != null && check(e.getKey()) ? e : null;
  }

  protected A checkOrNull(A k){
    return k != null && check(k) ? k : null;
  }



}
