package org.mellowtech.core.collections.impl;

import org.mellowtech.core.collections.DiscMap;

import java.util.*;
import java.util.Map.Entry;

/**
 * Created by msvens on 11/11/15.
 */
public class Maps {

  @SuppressWarnings("unchecked")
  public static <A> int compare(A obj1, A obj2){
    return ((Comparable <? super A>) obj1).compareTo(obj2);
  }

  /*final int More ...compare(Object k1, Object k2) {
    1190        return comparator==null ? ((Comparable<? super K>)k1).compareTo((K)k2)
    1191            : comparator.compare((K)k1, (K)k2);
    1192    }
  */
}

/*class DBSubMap <A,B> implements NavigableMap <A,B> {

  private Optional<A> low, high;
  private boolean lowInclusive, highInclusive;
  private SortedDiscMap<A,B> map;

  public DBSubMap(Optional<A> low, boolean lowInclusive, Optional<A> high, boolean highInclusive,
                  SortedDiscMap<A,B> map){
    this.low = low;
    this.high = high;
    this.lowInclusive = lowInclusive;
    this.highInclusive = highInclusive;
    this.map = map;
  }

  private boolean noBounds(){
    return !(low.isPresent() && high.isPresent());
  }

  private boolean inRange(A key){
    boolean inRange = true;
    if(low.isPresent()){
      inRange = lowInclusive ? Maps.compare(low.get(), key) <= 0 : Maps.compare(low.get(),key) < 0;
    }
    if(inRange && high.isPresent()){
      inRange = highInclusive ? Maps.compare(high.get(), key) >= 0 : Maps.compare(high.get(),key) > 0;
    }
    return inRange;
  }

  private A rangeOrNull(A key){
    return inRange(key) ? key : null;
  }

  private Entry <A,B> rangeOrNull(Entry<A,B> entry){
    return inRange(entry.getKey()) ? entry : null;
  }

  private A key(A key, Function<A,A> func){
    return inRange(key) ? rangeOrNull(func.apply(key)) : null;
  }

  private Entry<A,B> entry(A key, Function<A,Entry<A,B>> func){
    return inRange(key) ? rangeOrNull(func.apply(key)) : null;
  }

  @Override
  public Entry<A, B> lowerEntry(A key) {
    return entry(key, k -> map.lowerEntry(k));
  }

  @Override
  public A lowerKey(A key) {
    return key(key, k -> map.lowerKey(k));
  }

  @Override
  public Entry<A, B> floorEntry(A key) {
    return entry(key, k -> map.floorEntry(key));
  }

  @Override
  public A floorKey(A key) {
    return key(key, k -> map.floorKey(k));
  }

  @Override
  public Entry<A, B> ceilingEntry(A key) {
    return entry(key, k -> map.ceilingEntry(key));
  }

  @Override
  public A ceilingKey(A key) {
    return key(key, k -> map.ceilingKey(k));
  }

  @Override
  public Entry<A, B> higherEntry(A key) {
    return entry(key, k -> map.higherEntry(k));
  }

  @Override
  public A higherKey(A key) {
    return key(key, k -> map.higherKey(k));
  }

  @Override
  public Entry<A, B> firstEntry() {
    if(noBounds()) return map.firstEntry();
    Entry <A,B> e = low.isPresent() ? map.ceilingEntry(low.get()) : map.firstEntry();
    return e != null ? rangeOrNull(e) : null;
  }

  @Override
  public Entry<A, B> lastEntry() {
    if(noBounds()) return map.lastEntry();
    Entry <A,B> e = high.isPresent() ? map.floorEntry(high.get()) : map.lastEntry();
    return e != null ? rangeOrNull(e) : null;
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
    if(noBounds()) return map.descendingMap();
    throw new Error("views not supported");
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
    if(!(inRange(fromKey) && inRange(toKey))){
      return null;
    }
    return new DBSubMap<>(Optional.ofNullable(fromKey), fromInclusive,
        Optional.ofNullable(toKey), toInclusive, map);
  }

  @Override
  public NavigableMap<A, B> headMap(A toKey, boolean inclusive) {
    if(!inRange(toKey)) return null;
    return new DBSubMap<>(Optional.empty(), false, Optional.of(toKey), inclusive, map);
  }

  @Override
  public NavigableMap<A, B> tailMap(A fromKey, boolean inclusive) {
    if(!inRange(fromKey)) return null;
    return new DBSubMap<>(Optional.of(fromKey), inclusive, Optional.empty(), false, map);
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
    A key = low.isPresent() ? map.ceilingKey(low.get()) : map.firstKey();
    return key != null ? rangeOrNull(key) : null;
  }

  @Override
  public A lastKey() {
    if(noBounds()) return map.lastKey();
    A key = high.isPresent() ? map.floorKey(high.get()) : map.lastKey();
    return key != null ? rangeOrNull(key) : null;
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
    return inRange((A) key) ? map.containsKey(key) : false;
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
    return inRange((A) key) ? map.get(key) : null;
  }

  @Override
  public B put(A key, B value) {
    if(inRange((A) key)){
      return map.put(key,value);
    }
    else throw new IllegalArgumentException("key out of range");
  }

  @Override
  public B remove(Object key) {
    if(inRange((A)key))
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
    throw new Error("not yet implemented");
  }

  @Override
  public Set<Entry<A, B>> entrySet() {
    if(noBounds()) return map.entrySet();
    throw new Error("not yet implemented");
  }
}
*/

class DBKeySet <A,B> extends AbstractSet <A> {

  DiscMap <A,B> map;

  public DBKeySet(DiscMap <A,B> map){
    this.map = map;
  }

  @Override
  public Iterator<A> iterator() {
    return new DBKeyIterator<>(map.iterator());
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean contains(Object o) {
    return map.containsKey(o);
  }

  @Override
  public boolean remove(Object o) {
    return map.remove(o) != null;
  }
}

class DBValueCollection<A,B> extends AbstractCollection<B> {

  DiscMap<A,B> map;

  public DBValueCollection(DiscMap<A,B> map){
    this.map = map;
  }

  @Override
  public Iterator<B> iterator() {
    return new DBValueIterator<>(map.iterator());
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean remove(Object o) {
    throw new Error("removal is not supported");
  }
}

class DBEntrySet<A,B> extends AbstractSet<Entry<A,B>>{

  DiscMap <A,B> map;

  public DBEntrySet(DiscMap <A,B> map){
    this.map = map;
  }
  @Override
  public Iterator<Entry<A, B>> iterator() {
    return map.iterator();
  }

  @Override
  public boolean contains(Object o) {
    Entry<A,B> e = (Entry<A,B>) o;
    return map.containsKey(e.getKey());
  }

  @Override
  public boolean remove(Object o) {
    Entry<A,B> e = (Entry<A,B>) o;
    return map.remove(e.getKey()) != null;
  }

  @Override
  public void clear() {
    super.clear();
  }

  @Override
  public int size() {
    return map.size();
  }
}


class DBValueIterator<A,B> implements Iterator<B> {

  Iterator <Entry<A,B>> iter;

  public DBValueIterator(Iterator<Entry<A,B>> iter){
    this.iter = iter;
  }
  @Override
  public boolean hasNext() {
    return iter.hasNext();
  }

  @Override
  public B next() {
    return iter.next().getValue();
  }
}

class DBKeyIterator<A,B> implements Iterator<A> {

  Iterator <Entry<A,B>> iter;

  public DBKeyIterator(Iterator<Entry<A,B>> iter){
    this.iter = iter;
  }

  @Override
  public boolean hasNext() {
    return iter.hasNext();
  }

  @Override
  public A next() {
    return iter.next().getKey();
  }
}