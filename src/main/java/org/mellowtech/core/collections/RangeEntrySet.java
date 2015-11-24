package org.mellowtech.core.collections;

import java.util.*;
import java.util.Map.Entry;

/**
 * Created by msvens on 14/11/15.
 */
class RangeEntrySet<A,B> extends AbstractRangeMap<A,B> implements NavigableSet<Entry<A,B>>{

  public RangeEntrySet(SortedDiscMap<A,B> shared,
                       A from, boolean fromInclusive, A to, boolean toInclusive){
    super(shared, false, from, fromInclusive, to, toInclusive);
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
    return map.iterator(false, from, fromInclusive, to, toInclusive);
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
    return new DescendingEntrySet<>(map, to, toInclusive, from, fromInclusive);
  }

  @Override
  public Iterator<Entry<A,B> > descendingIterator() {
    return map.iterator(true, from, fromInclusive, to, toInclusive);
  }

  @Override
  public NavigableSet<Entry<A,B>> subSet(Entry<A,B>  fromElement, boolean fromInclusive,
                                          Entry<A,B>  toElement, boolean toInclusive) {
    if(check(fromElement.getKey()) && check(toElement.getKey())){
      return new RangeEntrySet<>(map, fromElement.getKey(), fromInclusive,
          toElement.getKey(), toInclusive);
    } else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public NavigableSet<Entry<A,B> > headSet(Entry<A,B>  toElement, boolean inclusive) {
    if(check(toElement.getKey()))
      return new RangeEntrySet<>(map, from, fromInclusive,
          toElement.getKey(), inclusive);
    else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public NavigableSet<Entry<A,B> > tailSet(Entry<A,B>  fromElement, boolean inclusive) {
    if(check(fromElement.getKey()))
      return new RangeEntrySet<>(map, fromElement.getKey(), inclusive, to, toInclusive);
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

}
