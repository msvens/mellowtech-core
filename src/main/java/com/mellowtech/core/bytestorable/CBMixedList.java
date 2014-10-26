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
public class CBMixedList extends ByteStorable <List <Object>> implements List<Object> {

  private List <Object> list;

  public CBMixedList(){
    this.list = new ArrayList <Object> ();
  }

  @Override
  public ByteStorable <List<Object>> fromBytes(ByteBuffer bb, boolean doNew) {
    CBMixedList toRet = doNew ? new CBMixedList() : this;
    toRet.list.clear();
    PrimitiveObject po = new PrimitiveObject();

    bb.getInt(); //read past int
    int elems = bb.getInt();
    for(int i = 0; i < elems; i++){
      toRet.list.add(po.fromBytes(bb, true).get());
    }
    return toRet;
  }

  @Override
  public void toBytes(ByteBuffer bb) {
    bb.putInt(byteSize());
    bb.putInt(list.size());
    PrimitiveObject po = new PrimitiveObject();
    for(Object o : list){
      po.set(o);
      po.toBytes(bb);
    }
  }

  @Override
  public int byteSize() {
    int byteSize = 8; //byteSize indicator + num elements;
    PrimitiveObject po = new PrimitiveObject();
    for(Object o : list){
      po.set(o);
      byteSize += po.byteSize();
    }
    return byteSize;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return getSizeFour(bb);
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
  public Iterator<Object> iterator() {
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
  public boolean add(Object o) {
    check(o);
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
  public boolean addAll(Collection<? extends Object> c) {
    for(Object o : c){
      check(o);
    }
    return list.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends Object> c) {
    for(Object o : c)
    check(o);
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
  public Object get(int index) {
    return list.get(index);
  }

  @Override
  public Object set(int index, Object element) {
    check(element);
    return list.set(index, element);
  }

  @Override
  public void add(int index, Object element) {
    check(element);
    list.add(index, element);
  }

  @Override
  public Object remove(int index) {
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
  public ListIterator<Object> listIterator() {
    return list.listIterator();
  }

  @Override
  public ListIterator<Object> listIterator(int index) {
    return list.listIterator(index);
  }

  @Override
  public List<Object> subList(int fromIndex, int toIndex) {
    ArrayList <Object> sub = (ArrayList <Object>) list.subList(fromIndex, toIndex);
    CBMixedList toRet = new CBMixedList();
    toRet.list = sub;
    return toRet;
  }

  private void check(Object obj){
    if(PrimitiveType.type(obj) == null)
      throw new ByteStorableException("Unknown Object Type");
  }
}
