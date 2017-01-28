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

package org.mellowtech.core.bytestorable;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * BStorable wrapper for List. Elements needs to be of
 * a supported type defined in PrimitiveType.
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 * @see PrimitiveType
 * @param <E> type of element in this list needs to be a supported one
 */
public class CBList <E> extends BStorableImp <List<E>> implements List<E> {

  /**
   * Initialize to the empty list
   */
  public CBList(){super(new ArrayList <E> ());}

  /**
   * Initialize to the given list
   * @param elems list to set
   */
  public CBList(List <E> elems){super(elems);}

  @Override
  public CBList <E> from(ByteBuffer bb) {

    CBUtil.getSize(bb, false);
    int numElems = bb.getInt();
    if(numElems < 1) return new CBList <> ();
    
    //unpack elements:
    ArrayList <E> elems = new ArrayList <> (numElems);
    PrimitiveType pt = PrimitiveType.fromOrdinal(bb.get());
    @SuppressWarnings("unchecked")
    BStorable <E> template = PrimitiveType.fromType(pt);
    for(int i = 0; i < numElems; i++){
      elems.add(template.from(bb).get());
    }
    
    return new CBList <> (elems);
  }
  
  @Override
  public CBList <E> create(List <E> elems) {return new CBList <> (elems);}

  @Override
  public void to(ByteBuffer bb) {
    CBUtil.putSize(internalSize(), bb, false);
    bb.putInt(value.size());
    if(value.size() < 1)
      return;
    PrimitiveType pt = PrimitiveType.type(value.get(0));
    if(pt == null) throw new ByteStorableException("Unrecognized type "+value.get(0).getClass().getName());
    bb.put(pt.getByte());
    for(E o : value){
      PrimitiveType.fromType(pt,o).to(bb);
    }
  }

  @Override
  public int byteSize() {
    return CBUtil.byteSize(internalSize(), false);
  }
  
  private int internalSize() {
    int size = 4; //num elements;
    if(value.size() > 0){
      PrimitiveType pt = PrimitiveType.type(value.get(0));
      if(pt == null) throw new ByteStorableException("Unrecognized type");
      size += 1;
      for(E o : value){
        size += PrimitiveType.fromType(pt, o).byteSize();
      }
    }
    return size;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CBUtil.peekSize(bb, false);
  }

  @Override
  public int size() {
    return value.size();
  }

  @Override
  public boolean isEmpty() {
    return value.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return value.contains(o);
  }

  @Override
  public Iterator<E> iterator() {
    return value.iterator();
  }

  @Override
  public Object[] toArray() {
    return value.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return value.<T>toArray(a);
  }

  @Override
  public boolean add(E o) {
    return value.add(o);
  }

  @Override
  public boolean remove(Object o) {
    return value.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return value.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    return value.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    return value.addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return value.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return value.retainAll(c);
  }

  @Override
  public void clear() {
    value.clear();
  }

  @Override
  public E get(int index) {
    return value.get(index);
  }

  @Override
  public E set(int index, E element) {
    return value.set(index, element);
  }

  @Override
  public void add(int index, E element) {
    value.add(index, element);
  }

  @Override
  public E remove(int index) {
    return value.remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return value.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return value.lastIndexOf(o);
  }

  @Override
  public ListIterator<E> listIterator() {
    return value.listIterator();
  }

  @Override
  public ListIterator<E> listIterator(int index) {
    return value.listIterator(index);
  }

  @Override
  public List<E> subList(int fromIndex, int toIndex) {
    ArrayList <E> sub = (ArrayList <E>) value.subList(fromIndex, toIndex);
    return new CBList <> (sub);
  }
}
