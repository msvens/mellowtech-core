package org.mellowtech.core.collections;

import java.util.*;

/**
 * Created by msvens on 21/11/15.
 */
class DescendingEntrySet <A,B> extends AbstractRangeMap<A,B> implements NavigableSet<Map.Entry<A,B>> {

  public DescendingEntrySet(SortedDiscMap<A, B> shared, A from, boolean fromInclusive, A to, boolean toInclusive) {
    super(shared, true, from, fromInclusive, to, toInclusive);
  }

  @Override
  public Map.Entry<A,B> lower(Map.Entry<A,B> a) {
    return check(a.getKey()) ? checkOrNull(map.higherEntry(a.getKey())) : null;
  }

  @Override
  public Map.Entry<A,B> floor(Map.Entry<A,B> a) {
    return check(a.getKey()) ? checkOrNull(map.ceilingEntry(a.getKey())) : null;
  }

  @Override
  public Map.Entry<A,B> ceiling(Map.Entry<A,B> a) {
    return check(a.getKey()) ? checkOrNull(map.floorEntry(a.getKey())) : null;
  }

  @Override
  public Map.Entry<A,B> higher(Map.Entry<A,B> a) {
    return check(a.getKey()) ? checkOrNull(map.lowerEntry(a.getKey())) : null;
  }


  @Override
  public Map.Entry<A,B> pollFirst() {
    Map.Entry<A,B> firstEntry = first();
    if(firstEntry != null){
      map.remove(firstEntry.getKey());
    }
    return firstEntry;
  }

  @Override
  public Map.Entry<A,B> pollLast() {
    Map.Entry<A,B> lastEntry= last();
    if(lastEntry != null){
      map.remove(lastEntry.getKey());
    }
    return lastEntry;
  }

  @Override
  public int size() {
    if(noBounds()) return map.size();
    int i = 0;
    Iterator <Map.Entry<A,B>> iter = iterator();
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
    Map.Entry<A,B> e = (Map.Entry<A,B>) o;
    return check(e.getKey()) ? map.containsKey(e.getKey()) : false;
  }

  @Override
  public Iterator<Map.Entry<A,B>> iterator() {
    return map.iterator(true, from, fromInclusive, to, toInclusive);
  }

  @Override
  public Object[] toArray() {
    ArrayList <Map.Entry<A,B>> toRet = new ArrayList<>();
    for(Iterator<Map.Entry<A,B>> iter = iterator(); iter.hasNext();){
      toRet.add(iter.next());
    }
    return toRet.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    ArrayList <Map.Entry<A,B>> toRet = new ArrayList<>();
    for(Iterator<Map.Entry<A,B>> iter = iterator(); iter.hasNext();){
      toRet.add(iter.next());
    }
    return toRet.toArray(a);
  }

  @Override
  public boolean add(Map.Entry<A,B> a) {
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
  public boolean addAll(Collection<? extends Map.Entry<A,B>> c) {
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
  public NavigableSet<Map.Entry<A,B>> descendingSet() {
    return new RangeEntrySet<>(map, to, toInclusive, from, fromInclusive);
  }

  @Override
  public Iterator<Map.Entry<A,B>> descendingIterator() {
    return map.iterator(false, to, toInclusive, from, fromInclusive);
  }

  @Override
  public NavigableSet<Map.Entry<A,B>> subSet(Map.Entry<A,B> fromElement, boolean fromInclusive,
                                             Map.Entry<A,B> toElement, boolean toInclusive) {
    if(check(fromElement.getKey()) && check(toElement.getKey())){
      return new DescendingEntrySet<>(map, fromElement.getKey(), fromInclusive,
          toElement.getKey(), toInclusive);
    } else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public NavigableSet<Map.Entry<A,B>> headSet(Map.Entry<A,B> toElement, boolean inclusive) {
    if(check(toElement.getKey()))
      return new DescendingEntrySet<>(map, from, fromInclusive,
          toElement.getKey(), inclusive);
    else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public NavigableSet<Map.Entry<A,B>> tailSet(Map.Entry<A,B> fromElement, boolean inclusive) {
    if(check(fromElement.getKey()))
      return new DescendingEntrySet<>(map, fromElement.getKey(), inclusive, to, toInclusive);
    else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public Comparator<? super Map.Entry<A,B>> comparator() {
    return null;
  }

  @Override
  public SortedSet<Map.Entry<A,B>> subSet(Map.Entry<A,B> fromElement, Map.Entry<A,B> toElement) {
    return subSet(fromElement, true, toElement, false);
  }

  @Override
  public SortedSet<Map.Entry<A,B>> headSet(Map.Entry<A,B> toElement) {
    return headSet(toElement, false);
  }

  @Override
  public SortedSet<Map.Entry<A,B>> tailSet(Map.Entry<A,B> fromElement) {
    return headSet(fromElement, true);
  }

  @Override
  public Map.Entry<A,B> first() {
    if(noBounds()) return map.lastEntry();
    Map.Entry<A,B> e = from != null ? map.floorEntry(from) : map.lastEntry();
    return e != null ? checkOrNull(e) : null;
  }

  @Override
  public Map.Entry<A,B> last() {
    if(noBounds()) return map.firstEntry();
    Map.Entry<A,B> e = to != null ? map.ceilingEntry(to) : map.firstEntry();
    return e != null ? checkOrNull(e) : null;
  }


}
