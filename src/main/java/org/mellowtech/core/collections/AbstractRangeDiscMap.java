package org.mellowtech.core.collections;

import java.util.*;

/**
 * Created by msvens on 14/11/15.
 */
abstract class AbstractRangeDiscMap<A,B>{

  protected boolean descending, fromInclusive, toInclusive = false;
  protected A from, to;
  protected SortedDiscMap <A,B> map;


  public AbstractRangeDiscMap(SortedDiscMap<A,B> shared, boolean descending,
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
    return from == null ||
        (fromInclusive ? ((Comparable<? super A>) from).compareTo(key) <= 0 : ((Comparable<? super A>) from).compareTo(key) < 0);
  }

  @SuppressWarnings("unchecked")
  protected boolean toCheck(A key) {
    return to == null ||
        (toInclusive ? ((Comparable<? super A>) to).compareTo(key) >= 0 : ((Comparable<? super A>) to).compareTo(key) > 0);
  }

  protected boolean check(A key){
    return fromCheck(key) && toCheck(key);
  }

  protected boolean noBounds(){
    return (from == null) && (to == null);
  }

  protected Map.Entry<A,B> checkOrNull(Map.Entry<A,B> e){
    return check(e.getKey()) ? e : null;
  }

  protected A checkOrNull(A k){
    return check(k) ? k : null;
  }



  /*
  @Override
  public Entry<A, B> lowerEntry(A key) {
    return check(key) ? checkOrNull(map.lowerEntry(key)) : null;
  }

  @Override
  public A lowerKey(A key) {
    return check(key) ? checkOrNull(map.lowerKey(key)) : null;
  }

  @Override
  public Entry<A, B> floorEntry(A key) {
    return check(key) ? checkOrNull(map.floorEntry(key)) : null;
  }

  @Override
  public A floorKey(A key) {
    return check(key) ? checkOrNull(map.floorKey(key)) : null;
  }

  @Override
  public Entry<A, B> ceilingEntry(A key) {
    return check(key) ? checkOrNull(map.ceilingEntry(key)) : null;
  }

  @Override
  public A ceilingKey(A key) {
    return check(key) ? checkOrNull(map.ceilingKey(key)) : null;
  }

  @Override
  public Entry<A, B> higherEntry(A key) {
    return check(key) ? checkOrNull(map.higherEntry(key)) : null;
  }

  @Override
  public A higherKey(A key) {
    return check(key) ? checkOrNull(map.higherKey(key)) : null;
  }

  @Override
  public Entry<A, B> firstEntry() {
    if(noBounds()) return map.firstEntry();
    Entry <A,B> e = from != null ? map.ceilingEntry(from) : map.firstEntry();
    return e != null ? checkOrNull(e) : null;
  }

  @Override
  public Entry<A, B> lastEntry() {
    if(noBounds()) return map.lastEntry();
    Entry <A,B> e = to != null ? map.floorEntry(to) : map.lastEntry();
    return e != null ? checkOrNull(e) : null;
  }


  @Override
  public Entry<A, B> pollFirstEntry() {
    Entry <A,B> firstEntry = firstEntry();
    if(firstEntry != null){
      map.remove(firstEntry.getKey());
    }
    return firstEntry;
  }

  @Override
  public Entry<A, B> pollLastEntry() {
    Entry <A,B> firstEntry = firstEntry();
    if(firstEntry != null){
      map.remove(firstEntry.getKey());
    }
    return firstEntry;

  }

  @Override
  public NavigableMap<A, B> descendingMap() {
    return new AbstractRangeDiscMap<>(map, !descending, to, toInclusive, from, fromInclusive);
  }

  @Override
  public NavigableSet<A> navigableKeySet() {
    if(noBounds()) return map.navigableKeySet();
    throw new Error("views not supported");
  }

  @Override
  public NavigableSet<A> descendingKeySet() {
    if(noBounds()) return map.navigableKeySet();
    throw new Error("views not supported");
  }

  @Override
  public NavigableMap<A, B> subMap(A fromKey, boolean fromInclusive, A toKey, boolean toInclusive) {
    //first verify in range
    //TODO: need to think about inclusiveness
    if(check(fromKey) && check(toKey)){
      return new AbstractRangeDiscMap<>(map, descending, fromKey, fromInclusive, toKey, toInclusive);
    } else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public NavigableMap<A, B> headMap(A toKey, boolean inclusive) {
    if(check(toKey))
      return new AbstractRangeDiscMap<>(map, descending, from, fromInclusive, toKey, inclusive);
    else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public NavigableMap<A, B> tailMap(A fromKey, boolean inclusive) {
    if(check(fromKey))
      return new AbstractRangeDiscMap<>(map, descending, fromKey, inclusive, to, toInclusive);
    else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public Comparator<? super A> comparator() {
    return null;
  }

  @Override
  public SortedMap<A, B> subMap(A fromKey, A toKey) {
    return subMap(fromKey, true, toKey, false);
  }

  @Override
  public SortedMap<A, B> headMap(A toKey) {
    return headMap(toKey, false);
  }

  @Override
  public SortedMap<A, B> tailMap(A fromKey) {
    return tailMap(fromKey, true);
  }

  @Override
  public A firstKey() {
    if(noBounds()) return map.firstKey();
    A key = from != null ? map.ceilingKey(from) : map.firstKey();
    return key != null ? checkOrNull(key) : null;
  }

  @Override
  public A lastKey() {
    if(noBounds()) return map.lastKey();
    A key = to != null ? map.floorKey(to) : map.lastKey();
    return key != null ? checkOrNull(key) : null;
  }

  @Override
  public int size() {
    if(noBounds()) return map.size();
    Iterator <A> iter = keySet().iterator();
    int i = 0;
    while(iter.hasNext()){
      i++;
    }
    return i;
  }

  @Override
  public boolean isEmpty() {
    if(noBounds()) return map.isEmpty();
    return keySet().isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return check((A)key) ? map.containsKey(key) : false;
  }

  @Override
  public boolean containsValue(Object value) {
    Iterator <Entry<A,B>> iter = entrySet().iterator();
    while(iter.hasNext()){
      if(iter.next().getValue().equals(value)){
        return true;
      }
    }
    return false;
  }

  @Override
  public B get(Object key) {
    return check((A) key) ? map.get(key) : null;
  }

  @Override
  public B put(A key, B value) {
    if(check((A) key)){
      return map.put(key,value);
    }
    else throw new IllegalArgumentException("key out of range");
  }

  @Override
  public B remove(Object key) {
    if(check((A)key))
      return map.remove(key);
    else
      throw new IllegalArgumentException("key out of range");
  }

  @Override
  public void putAll(Map<? extends A, ? extends B> m) {
    for(Entry <? extends A,? extends B> e : m.entrySet()){
      put(e.getKey(), e.getValue());
    }
  }

  @Override
  public void clear() {
    throw new Error("not yet implemented");
  }

  @Override
  public Set<A> keySet() {
    if(noBounds()) return map.keySet();
    throw new Error("not yet implemented");
  }

  @Override
  public Collection<B> values() {
    if(noBounds()) return map.values();
    throw new Error("not implemented because of performance reasons");
  }

  @Override
  public Set<Entry<A, B>> entrySet() {
    if(noBounds()) return map.entrySet();
    throw new Error("not yet implemented");
  }
  */


}
