/*
 * Copyright (c) 2013 mellowtech.org.
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 *
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 *
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 */

package com.mellowtech.core.bytestorable;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * List with primitive objects (various)
 * Date: 2013-02-17
 * Time: 12:58
 *
 * @author Martin Svensson
 */
public class CBList <E> extends ByteStorable implements List<E> {

  private List <E> list;

  public CBList(){
    this.list = new ArrayList <E> ();
  }

  @Override
  public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
    CBList toRet = doNew ? new CBList() : this;
    toRet.list.clear();


    int size = bb.getInt();
    int elems = bb.getInt();
    if(elems < 1) return toRet;
    //unpack elements:
    PrimitiveType pt = PrimitiveType.fromOrdinal(bb.get());
    ByteStorable template =PrimitiveType.fromType(pt);
    for(int i = 0; i < elems; i++){
      toRet.list.add((E) template.fromBytes(bb, false).get());
    }
    return toRet;

  }

  @Override
  public void toBytes(ByteBuffer bb) {
    bb.putInt(byteSize());
    bb.putInt(list.size());
    if(list.size() < 1)
      return;
    PrimitiveType pt = PrimitiveType.type(list.get(0));
    if(pt == null) throw new ByteStorableException("Unrecognized type");
    bb.put(pt.getByte());
    ByteStorable template = PrimitiveType.fromType(pt);
    for(Object o : list){
      template.set(o);
      template.toBytes(bb);
    }
  }

  @Override
  public int byteSize() {
    int byteSize = 8; //byteSize indicator + num elements;
    if(list.size() > 0){
      PrimitiveType pt = PrimitiveType.type(list.get(0));
      if(pt == null) throw new ByteStorableException("Unrecognized type");
      byteSize += 1;
      ByteStorable temp = PrimitiveType.fromType(pt);
      for(Object o : list){
        temp.set(o);
        byteSize += temp.byteSize();
      }
    }
    return byteSize;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return getSizeFour(bb);
  }

  @Override
  public List get(){
    return this.list;
  }

  @Override
  public void set(Object l){
    this.list = (List) l;
  }

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public boolean isEmpty() {
    return list.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return list.contains(o);
  }

  @Override
  public Iterator<E> iterator() {
    return list.iterator();
  }

  @Override
  public Object[] toArray() {
    return list.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return list.toArray(a);
  }

  @Override
  public boolean add(E o) {
    return list.add(o);
  }

  @Override
  public boolean remove(Object o) {
    return list.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return list.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    return list.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    return addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return list.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return list.retainAll(c);
  }

  @Override
  public void clear() {
    list.clear();
  }

  @Override
  public E get(int index) {
    return list.get(index);
  }

  @Override
  public E set(int index, E element) {
    return list.set(index, element);
  }

  @Override
  public void add(int index, E element) {
    list.add(index, element);
  }

  @Override
  public E remove(int index) {
    return list.remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return list.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return list.lastIndexOf(o);
  }

  @Override
  public ListIterator<E> listIterator() {
    return list.listIterator();
  }

  @Override
  public ListIterator<E> listIterator(int index) {
    return list.listIterator(index);
  }

  @Override
  public List<E> subList(int fromIndex, int toIndex) {
    ArrayList <E> sub = (ArrayList) list.subList(fromIndex, toIndex);
    CBList toRet = new CBList();
    toRet.list = sub;
    return toRet;
  }

  private void check(Object obj){
    if(PrimitiveType.type(obj) == null)
      throw new ByteStorableException("Unknown Object Type");
  }
}
