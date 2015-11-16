package org.mellowtech.core.collections;

import java.util.*;
import java.util.Map.Entry;

/**
 * Created by msvens on 14/11/15.
 */
class DiscBasedRangeEntrySet<A,B> extends AbstractRangeDiscMap<A,B> implements NavigableSet<Entry<A,B>>{

  public DiscBasedRangeEntrySet(SortedDiscMap<A,B> shared, boolean descending,
                                A from, boolean fromInclusive, A to, boolean toInclusive){
    super(shared, descending, from, fromInclusive, to, toInclusive);
  }

  @Override
  public Entry<A,B> lower(Entry<A,B> a) {
    return check(a.getKey()) ? checkOrNull(map.lowerEntry(a.getKey())) : null;
  }

  @Override
  public Entry<A,B> floor(Entry<A,B>  a) {
    return check(a.getKey()) ? checkOrNull(map.floorEntry(a.getKey())) : null;
  }

  @Override
  public Entry<A,B> ceiling(Entry<A,B>  a) {
    return check(a.getKey()) ? checkOrNull(map.ceilingEntry(a.getKey())) : null;
  }

  @Override
  public Entry<A,B> higher(Entry<A,B>  a) {
    return check(a.getKey()) ? checkOrNull(map.higherEntry(a.getKey())) : null;
  }


  @Override
  public Entry<A,B> pollFirst() {
    Entry<A,B>  firstEntry = first();
    if(firstEntry != null){
      map.remove(firstEntry.getKey());
    }
    return firstEntry;
  }

  @Override
  public Entry<A,B> pollLast() {
    Entry<A,B>  lastEntry= last();
    if(lastEntry != null){
      map.remove(lastEntry.getKey());
    }
    return lastEntry;
  }

  @Override
  public int size() {
    if(noBounds()) return map.size();
    int i = 0;
    Iterator <Entry<A,B> > iter = iterator();
    while(iter.hasNext())
      iter.next();
      i++;
    return i;
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public boolean contains(Object o) {
    Entry <A,B> e = (Entry <A,B>) o;
    return check(e.getKey()) ? map.containsKey(e.getKey()) : false;
  }

  @Override
  public Iterator<Entry<A,B> > iterator() {
    return map.iterator(descending, from, fromInclusive, to, toInclusive);
  }

  @Override
  public Object[] toArray() {
    ArrayList <Entry<A,B> > toRet = new ArrayList<>();
    for(Iterator<Entry<A,B> > iter = iterator(); iter.hasNext();){
      toRet.add(iter.next());
    }
    return toRet.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    ArrayList <Entry<A,B> > toRet = new ArrayList<>();
    for(Iterator<Entry<A,B> > iter = iterator(); iter.hasNext();){
      toRet.add(iter.next());
    }
    return toRet.toArray(a);
  }

  @Override
  public boolean add(Entry<A,B>  a) {
    throw new Error("add is not supported");
  }

  @Override
  public boolean remove(Object o) {
    Map.Entry <A,B> e = (Map.Entry<A,B>) o;
    return check(e.getKey()) ? map.remove(e.getKey()) != null : false;
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
  public boolean addAll(Collection<? extends Entry<A,B> > c) {
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
  public NavigableSet<Entry<A,B> > descendingSet() {
    return new DiscBasedRangeEntrySet<>(map, !descending, from, fromInclusive, to, toInclusive);
  }

  @Override
  public Iterator<Entry<A,B> > descendingIterator() {
    return map.iterator(!descending, from, fromInclusive, to, toInclusive);
  }

  @Override
  public NavigableSet<Entry<A,B>> subSet(Entry<A,B>  fromElement, boolean fromInclusive,
                                          Entry<A,B>  toElement, boolean toInclusive) {
    if(check(fromElement.getKey()) && check(toElement.getKey())){
      return new DiscBasedRangeEntrySet<>(map, descending, fromElement.getKey(), fromInclusive,
          toElement.getKey(), toInclusive);
    } else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public NavigableSet<Entry<A,B> > headSet(Entry<A,B>  toElement, boolean inclusive) {
    if(check(toElement.getKey()))
      return new DiscBasedRangeEntrySet<>(map, descending, from, fromInclusive,
          toElement.getKey(), inclusive);
    else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public NavigableSet<Entry<A,B> > tailSet(Entry<A,B>  fromElement, boolean inclusive) {
    if(check(fromElement.getKey()))
      return new DiscBasedRangeEntrySet<>(map, descending,
          fromElement.getKey(), inclusive, to, toInclusive);
    else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public Comparator<? super Entry<A,B> > comparator() {
    return null;
  }

  @Override
  public SortedSet<Entry<A,B> > subSet(Entry<A,B>  fromElement, Entry<A,B>  toElement) {
    return subSet(fromElement, true, toElement, false);
  }

  @Override
  public SortedSet<Entry<A,B> > headSet(Entry<A,B>  toElement) {
    return headSet(toElement, false);
  }

  @Override
  public SortedSet<Entry<A,B> > tailSet(Entry<A,B>  fromElement) {
    return headSet(fromElement, true);
  }

  @Override
  public Entry<A,B>  first() {
    if(noBounds()) return map.firstEntry();
    Entry<A,B>  e = from != null ? map.ceilingEntry(from) : map.firstEntry();
    return e != null ? checkOrNull(e) : null;
  }

  @Override
  public Entry<A,B>  last() {
    if(noBounds()) return map.lastEntry();
    Entry<A,B>  e = to != null ? map.floorEntry(to) : map.lastEntry();
    return e != null ? checkOrNull(e) : null;
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
