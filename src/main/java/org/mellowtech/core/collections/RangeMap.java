package org.mellowtech.core.collections;

import java.util.*;

/**
 * Created by msvens on 14/11/15.
 */
class RangeMap<A, B> extends AbstractRangeMap<A, B> implements NavigableMap<A, B> {

  public RangeMap(SortedDiscMap<A, B> shared,
                  A from, boolean fromInclusive, A to, boolean toInclusive) {
    super(shared, false, from, fromInclusive, to, toInclusive);
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
  public void clear() {
    throw new Error("not yet implemented");
  }

  @Override
  public Comparator<? super A> comparator() {
    return null;
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
  public NavigableSet<A> descendingKeySet() {
    return new DescendingKeySet<>(map, to, toInclusive, from, fromInclusive);
  }

  @Override
  public NavigableMap<A, B> descendingMap() {
    return new DescendingMap<>(map, to, toInclusive, from, fromInclusive);
  }

  @Override
  public Set<Entry<A, B>> entrySet() {
    if (noBounds()) return map.entrySet();
    return new RangeEntrySet<>(map, from, fromInclusive, to, toInclusive);
  }

  @Override
  public Entry<A, B> firstEntry() {
    if (from == null) return checkOrNull(map.firstEntry());
    return checkOrNull(fromInclusive ? map.ceilingEntry(from) : map.higherEntry(from));
  }

  @Override
  public A firstKey() {
    if (from == null) return checkOrNull(map.firstKey());
    return checkOrNull(fromInclusive ? map.ceilingKey(from) : map.higherKey(from));
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
  @SuppressWarnings("unchecked")
  public B get(Object key) {
    return check((A) key) ? map.get(key) : null;
  }

  @Override
  public NavigableMap<A, B> headMap(A toKey, boolean inclusive) {
    if (check(toKey))
      return new RangeMap<>(map, from, fromInclusive, toKey, inclusive);
    else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public SortedMap<A, B> headMap(A toKey) {
    return headMap(toKey, false);
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
  public boolean isEmpty() {
    if (noBounds()) return map.isEmpty();
    return keySet().isEmpty();
  }

  @Override
  public Set<A> keySet() {
    if (noBounds()) return map.keySet();
    return new RangeKeySet<>(map, from, fromInclusive, to, toInclusive);
  }

  @Override
  public Entry<A, B> lastEntry() {
    if (to == null) return checkOrNull(map.lastEntry());
    return checkOrNull(toInclusive ? map.floorEntry(from) : map.lowerEntry(from));
  }

  @Override
  public A lastKey() {
    if (to == null) return checkOrNull(map.lastKey());
    return checkOrNull(toInclusive ? map.floorKey(to) : map.lowerKey(to));
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
  public NavigableSet<A> navigableKeySet() {
    return new RangeKeySet<>(map, from, fromInclusive, to, toInclusive);
  }

  @Override
  public Entry<A, B> pollFirstEntry() {
    Entry<A, B> firstEntry = firstEntry();
    if (firstEntry != null) {
      map.remove(firstEntry.getKey());
    }
    return firstEntry;
  }

  @Override
  public Entry<A, B> pollLastEntry() {
    Entry<A, B> firstEntry = firstEntry();
    if (firstEntry != null) {
      map.remove(firstEntry.getKey());
    }
    return firstEntry;

  }

  @Override
  public B put(A key, B value) {
    if (check(key)) {
      return map.put(key, value);
    } else throw new IllegalArgumentException("key out of range");
  }

  @Override
  public void putAll(Map<? extends A, ? extends B> m) {
    for (Entry<? extends A, ? extends B> e : m.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public B remove(Object key) {
    if (check((A) key))
      return map.remove(key);
    else
      throw new IllegalArgumentException("key out of range");
  }

  @Override
  public int size() {
    if (noBounds()) return map.size();
    return keySet().size();
  }

  @Override
  public NavigableMap<A, B> subMap(A fromKey, boolean fromInclusive, A toKey, boolean toInclusive) {
    //first verify in range
    //TODO: need to think about inclusiveness
    if (check(fromKey) && check(toKey)) {
      return new RangeMap<>(map, fromKey, fromInclusive, toKey, toInclusive);
    } else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public SortedMap<A, B> subMap(A fromKey, A toKey) {
    return subMap(fromKey, true, toKey, false);
  }

  @Override
  public NavigableMap<A, B> tailMap(A fromKey, boolean inclusive) {
    if (check(fromKey))
      return new RangeMap<>(map, fromKey, inclusive, to, toInclusive);
    else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public SortedMap<A, B> tailMap(A fromKey) {
    return tailMap(fromKey, true);
  }

  @Override
  public Collection<B> values() {
    if (noBounds()) return map.values();
    throw new Error("not implemented because of performance reasons");
  }


}
