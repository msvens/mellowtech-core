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

package org.mellowtech.core.bytestorable;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * List with primitive objects (various)
 * Date: 2013-02-17
 * Time: 12:58
 *
 * @author Martin Svensson
 */
public class CBMixedList extends BStorableImp <List <Object>, CBMixedList> implements List<Object> {


  public CBMixedList(){
    super(new ArrayList <Object> ());
  }
  
  public CBMixedList(List <Object> elems){
    super(elems);
  }
  
  @Override
  public CBMixedList create(List <Object> elems){return new CBMixedList(elems);}

  @Override
  public CBMixedList from(ByteBuffer bb) {
    CBMixedList toRet = new CBMixedList();
    PrimitiveObject po = new PrimitiveObject();
    CBUtil.getSize(bb, true);
    int elems = bb.getInt();
    for(int i = 0; i < elems; i++){
      toRet.add(po.from(bb).get());
    }
    return toRet;
  }

  @Override
  public void to(ByteBuffer bb) {
    CBUtil.putSize(internalSize(), bb, true);
    bb.putInt(value.size());
    
    for(Object o : value){
      PrimitiveObject po = new PrimitiveObject(o);
      po.to(bb);
    }
  }

  @Override
  public int byteSize() {
    return CBUtil.byteSize(internalSize(), true);
  }
  
  private int internalSize() {
    int size = 4; //num elements
    for(Object o : value){
      PrimitiveObject po = new PrimitiveObject(o);
      size += po.byteSize();
    }
    return size;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CBUtil.peekSize(bb, true);
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
  public Iterator<Object> iterator() {
    return value.iterator();
  }

  @Override
  public Object[] toArray() {
    return value.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return value.toArray(a);
  }

  @Override
  public boolean add(Object o) {
    check(o);
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
  public boolean addAll(Collection<? extends Object> c) {
    for(Object o : c){
      check(o);
    }
    return value.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends Object> c) {
    for(Object o : c)
    check(o);
    return addAll(index, c);
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
  public Object get(int index) {
    return value.get(index);
  }

  @Override
  public Object set(int index, Object element) {
    check(element);
    return value.set(index, element);
  }

  @Override
  public void add(int index, Object element) {
    check(element);
    value.add(index, element);
  }

  @Override
  public Object remove(int index) {
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
  public ListIterator<Object> listIterator() {
    return value.listIterator();
  }

  @Override
  public ListIterator<Object> listIterator(int index) {
    return value.listIterator(index);
  }

  @Override
  public List<Object> subList(int fromIndex, int toIndex) {
    ArrayList <Object> sub = (ArrayList <Object>) value.subList(fromIndex, toIndex);
    CBMixedList toRet = new CBMixedList();
    toRet.value = sub;
    return toRet;
  }

  private void check(Object obj){
    if(PrimitiveType.type(obj) == null)
      throw new ByteStorableException("Unknown Object Type");
  }
}
