package org.mellowtech.core.collections.impl;

import org.mellowtech.core.collections.DiscMap;

import java.util.*;
import java.util.Map.Entry;

class DBKeySet <A,B> extends AbstractSet <A> {

  DiscMap <A,B> map;

  DBKeySet(DiscMap <A,B> map){
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

  DBValueCollection(DiscMap<A,B> map){
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

  DBEntrySet(DiscMap <A,B> map){
    this.map = map;
  }
  @Override
  public Iterator<Entry<A, B>> iterator() {
    return map.iterator();
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean contains(Object o) {
    Entry<A,B> e = (Entry<A,B>) o;
    return map.containsKey(e.getKey());
  }

  @Override
  @SuppressWarnings("unchecked")
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

  DBValueIterator(Iterator<Entry<A,B>> iter){
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

  DBKeyIterator(Iterator<Entry<A,B>> iter){
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