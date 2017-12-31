/*
 * Copyright 2015 mellowtech.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mellowtech.core.collections.impl;

import org.mellowtech.core.collections.SortedDiscMap;

import java.util.*;

/**
 * Created by msvens on 21/11/15.
 */
class DescendingKeySet <A,B> extends AbstractRangeMap<A,B> implements NavigableSet<A> {


  DescendingKeySet(SortedDiscMap<A, B> shared, A from, boolean fromInclusive, A to, boolean toInclusive) {
    super(shared, true, from, fromInclusive, to, toInclusive);
  }

  @Override
  public A lower(A a) {
    return check(a) ? checkOrNull(map.higherKey(a)) : null;
  }

  @Override
  public A floor(A a) {
    return check(a) ? checkOrNull(map.ceilingKey(a)) : null;
  }

  @Override
  public A ceiling(A a) {
    return check(a) ? checkOrNull(map.floorKey(a)) : null;
  }

  @Override
  public A higher(A a) {
    return check(a) ? checkOrNull(map.lowerKey(a)) : null;
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
    for (A a : this) {
      i++;
    }
    return i;
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean contains(Object o) {
    return check((A) o) && map.containsKey(o);
  }

  @Override
  public Iterator<A> iterator() {
    return new DBKeyIterator<>(map.iterator(true, from, fromInclusive, to, toInclusive));
  }

  @Override
  public Object[] toArray() {
    ArrayList <A> toRet = new ArrayList<>();
    for (A a : this) {
      toRet.add(a);
    }
    return toRet.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    ArrayList <A> toRet = new ArrayList<>();
    for (A a1 : this) {
      toRet.add(a1);
    }
    return toRet.toArray(a);
  }

  @Override
  public boolean add(A a) {
    throw new Error("add is not supported");
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean remove(Object o) {
    return check((A) o) && map.remove(o) != null;
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
    return new RangeKeySet<>(map, to, toInclusive, from, fromInclusive);
  }

  @Override
  public Iterator<A> descendingIterator() {
    return new DBKeyIterator<>(map.iterator(false, from, fromInclusive, to, toInclusive));
  }

  @Override
  public NavigableSet<A> subSet(A fromElement, boolean fromInclusive, A toElement, boolean toInclusive) {
    if(check(fromElement) && check(toElement)){
      return new DescendingKeySet<>(map, fromElement, fromInclusive, toElement, toInclusive);
    } else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public NavigableSet<A> headSet(A toElement, boolean inclusive) {
    if(check(toElement))
      return new DescendingKeySet<>(map, from, fromInclusive, toElement, inclusive);
    else
      throw new IllegalArgumentException("bounds out of range");
  }

  @Override
  public NavigableSet<A> tailSet(A fromElement, boolean inclusive) {
    if(check(fromElement))
      return new DescendingKeySet<>(map, fromElement, inclusive, to, toInclusive);
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
    if(noBounds()) return map.lastKey();
    A key = from != null ? map.floorKey(from) : map.lastKey();
    return key != null ? checkOrNull(key) : null;
  }

  @Override
  public A last() {
    if(noBounds()) return map.firstKey();
    A key = to != null ? map.ceilingKey(to) : map.firstKey();
    return key != null ? checkOrNull(key) : null;
  }
}
