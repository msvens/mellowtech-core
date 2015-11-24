package org.mellowtech.core.collections;

import java.util.*;

/**
 * Created by msvens on 14/11/15.
 */
abstract class AbstractRangeMap<A,B>{

  protected boolean fromInclusive, toInclusive = false;
  private boolean descending = false;
  protected A from, to;
  protected SortedDiscMap <A,B> map;


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
