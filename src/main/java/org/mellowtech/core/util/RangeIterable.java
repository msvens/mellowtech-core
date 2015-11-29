package org.mellowtech.core.util;

import org.mellowtech.core.collections.KeyValue;

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
