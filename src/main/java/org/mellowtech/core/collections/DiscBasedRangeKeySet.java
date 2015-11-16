package org.mellowtech.core.collections;

import java.util.*;

/**
 * Created by msvens on 14/11/15.
 */
class DiscBasedRangeKeySet<A,B> extends AbstractRangeDiscMap<A,B> implements NavigableSet<A>{

  public DiscBasedRangeKeySet(SortedDiscMap<A,B> shared, boolean descending,
                              A from, boolean fromInclusive, A to, boolean toInclusive){
    super(shared, descending, from, fromInclusive, to, toInclusive);
  }

  @Override
  public A lower(A a) {
    return check(a) ? checkOrNull(map.lowerKey(a)) : null;
  }

  @Override
  public A floor(A a) {
    return check(a) ? checkOrNull(map.floorKey(a)) : null;
  }

  @Override
  public A ceiling(A a) {
    return check(a) ? checkOrNull(map.ceilingKey(a)) : null;
  }

  @Override
  public A higher(A a) {
    return check(a) ? checkOrNull(map.higherKey(a)) : null;
  }

  @Override
  public A pollFirst() {
    A firstKey= first();
    if(firstKey != null){
      map.remove(firstKey);
    }
    return firstKey;
  }

  @Override
  public A pollLast() {
    A lastKey= last();
    if(lastKey != null){
      map.remove(lastKey);
    }
    return lastKey;
  }

  @Override
  public int size() {
    if(noBounds()) return map.size();
    int i = 0;
    Iterator <A> iter = iterator();
    while(iter.hasNext()) {
      iter.next();
      i++;
    }
    return i;
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public boolean contains(Object o) {
    return check((A) o) ? map.containsKey(o) : false;
  }

  @Override
  public Iterator<A> iterator() {
    return new DBKeyIterator<>(map.iterator(descending, from, fromInclusive, to, toInclusive));
  }

  @Override
  public Object[] toArray() {
    ArrayList <A> toRet = new ArrayList<>();
    for(Iterator<A> iter = iterator(); iter.hasNext();){
      toRet.add(iter.next());
    }
    return toRet.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    ArrayList <A> toRet = new ArrayList<>();
    for(Iterator<A> iter = iterator(); iter.hasNext();){
      toRet.add(iter.next());
    }
    return toRet.toArray(a);
  }

  @Override
  public boolean add(A a) {
    throw new Error("add is not supported");
  }

  @Override
  public boolean remove(Object o) {
    return check((A) o) ? map.remove(o) != null : false;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    for(Object o : c){
      if(!contains(o))
        return false;
    }
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends A> c) {
    throw new Error("add is not supported");
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    boolean ret = false;
    for(Object o : c){
      boolean didRemove = remove(o);
      if(!ret) ret = didRemove;
    }
    return ret;
  }

  @Override
  public void clear() {
    throw new Error("not yet implemented");
  }

  @Override
  public NavigableSet<A> descendingSet() {
    return new DiscBasedRangeKeySet<>(map, !descending, from, fromInclusive, to, toInclusive);
  }

  @Override
  public Iterator<A> descendingIterator() {
    return new DBKeyIterator<>(map.iterator(!descending, from, fromInclusive, to, toInclusive));
  }

  @Override
  public NavigableSet<A> subSet(A fromElement, boolean fromInclusive, A toElement, boolean toInclusive) {
    if(check(fromElement) && check(toElement)){
      return new DiscBasedRangeKeySet<>(map, descending, fromElement, fromInclusive, toElement, toInclusive);
    } else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public NavigableSet<A> headSet(A toElement, boolean inclusive) {
    if(check(toElement))
      return new DiscBasedRangeKeySet<>(map, descending, from, fromInclusive, toElement, inclusive);
    else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public NavigableSet<A> tailSet(A fromElement, boolean inclusive) {
    if(check(fromElement))
      return new DiscBasedRangeKeySet<>(map, descending, fromElement, inclusive, to, toInclusive);
    else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public Comparator<? super A> comparator() {
    return null;
  }

  @Override
  public SortedSet<A> subSet(A fromElement, A toElement) {
    return subSet(fromElement, true, toElement, false);
  }

  @Override
  public SortedSet<A> headSet(A toElement) {
    return headSet(toElement, false);
  }

  @Override
  public SortedSet<A> tailSet(A fromElement) {
    return headSet(fromElement, true);
  }

  @Override
  public A first() {
    if(noBounds()) return map.firstKey();
    A key = from != null ? map.ceilingKey(from) : map.firstKey();
    return key != null ? checkOrNull(key) : null;
  }

  @Override
  public A last() {
    if(noBounds()) return map.lastKey();
    A key = to != null ? map.floorKey(to) : map.lastKey();
    return key != null ? checkOrNull(key) : null;
  }




 /*




  @Override
  public NavigableMap<A, B> descendingMap() {
    return new DiscBasedRangeKeySet<>(map, !descending, to, toInclusive, from, fromInclusive);
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
      return new DiscBasedRangeKeySet<>(map, descending, fromKey, fromInclusive, toKey, toInclusive);
    } else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public NavigableMap<A, B> headMap(A toKey, boolean inclusive) {
    if(check(toKey))
      return new DiscBasedRangeKeySet<>(map, descending, from, fromInclusive, toKey, inclusive);
    else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public NavigableMap<A, B> tailMap(A fromKey, boolean inclusive) {
    if(check(fromKey))
      return new DiscBasedRangeKeySet<>(map, descending, fromKey, inclusive, to, toInclusive);
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
  }*/


}
