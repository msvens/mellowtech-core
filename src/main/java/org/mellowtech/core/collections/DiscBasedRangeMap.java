package org.mellowtech.core.collections;

import java.util.*;

/**
 * Created by msvens on 14/11/15.
 */
class DiscBasedRangeMap <A,B> extends AbstractRangeDiscMap <A,B> implements NavigableMap<A,B>{

  public DiscBasedRangeMap(SortedDiscMap<A,B> shared, boolean descending,
                         A from, boolean fromInclusive, A to, boolean toInclusive){
    super(shared, descending, from, fromInclusive, to, toInclusive);
  }

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
    return new DiscBasedRangeMap<>(map, !descending, to, toInclusive, from, fromInclusive);
  }

  @Override
  public NavigableSet<A> navigableKeySet() {
    return new DiscBasedRangeKeySet<>(map, descending, to, toInclusive, from, fromInclusive);
  }

  @Override
  public NavigableSet<A> descendingKeySet() {
    return new DiscBasedRangeKeySet<>(map, !descending, to, toInclusive, from, fromInclusive);
  }

  @Override
  public NavigableMap<A, B> subMap(A fromKey, boolean fromInclusive, A toKey, boolean toInclusive) {
    //first verify in range
    //TODO: need to think about inclusiveness
    if(check(fromKey) && check(toKey)){
      return new DiscBasedRangeMap<>(map, descending, fromKey, fromInclusive, toKey, toInclusive);
    } else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public NavigableMap<A, B> headMap(A toKey, boolean inclusive) {
    if(check(toKey))
      return new DiscBasedRangeMap<>(map, descending, from, fromInclusive, toKey, inclusive);
    else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public NavigableMap<A, B> tailMap(A fromKey, boolean inclusive) {
    if(check(fromKey))
      return new DiscBasedRangeMap<>(map, descending, fromKey, inclusive, to, toInclusive);
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
    return keySet().size();
  }

  @Override
  public boolean isEmpty() {
    if(noBounds()) return map.isEmpty();
    return keySet().isEmpty();
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean containsKey(Object key) {
    return check((A) key) && map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    for (Entry<A, B> abEntry : entrySet()) {
      if (abEntry.getValue().equals(value)) {
        return true;
      }
    }
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public B get(Object key) {
    return check((A) key) ? map.get(key) : null;
  }

  @Override
  public B put(A key, B value) {
    if(check(key)){
      return map.put(key,value);
    }
    else throw new IllegalArgumentException("key out of range");
  }

  @Override
  @SuppressWarnings("unchecked")
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
    return new DiscBasedRangeKeySet<>(map, descending, from, fromInclusive, to, toInclusive);
  }

  @Override
  public Collection<B> values() {
    if(noBounds()) return map.values();
    throw new Error("not implemented because of performance reasons");
  }

  @Override
  public Set<Entry<A, B>> entrySet() {
    if(noBounds()) return map.entrySet();
    return new DiscBasedRangeEntrySet<>(map, descending, from, fromInclusive, to, toInclusive);
  }




}
