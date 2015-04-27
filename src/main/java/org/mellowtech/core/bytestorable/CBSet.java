/**
 * 
 */
package org.mellowtech.core.bytestorable;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author msvens
 *
 */
public class CBSet<E> extends BStorableImp <Set<E>, CBSet<E>> implements Set <E>{


  public CBSet (){ super(new HashSet <E> ());}
  public CBSet (Set <E> set) {super(set);}

  @Override
  public CBSet<E> from(ByteBuffer bb) {
    CBSet <E> toRet = new CBSet <E> ();
    toRet.clear();
    bb.getInt(); //past size;
    int elems = bb.getInt();
    if(elems < 1) return toRet;
    PrimitiveType elemType = PrimitiveType.fromOrdinal(bb.get());

    if(elemType == null)
      throw new ByteStorableException("Unrecognized type");

    BStorable <E,?> elemTemp = PrimitiveType.fromType(elemType);

    for(int i = 0; i < elems; i++){
      E e = elemTemp.from(bb).get();
      toRet.add(e);
    }
    return toRet;
  }

  @Override
  public void to(ByteBuffer bb) {
    CBUtil.putSize(internalSize(), bb, false);
    bb.putInt(value.size());

    if(value.size() < 1)
      return;

    PrimitiveType pt = null;
    for(E entry : value){
      if(pt == null){
        pt = PrimitiveType.type(entry);
        bb.put(pt.getByte());
      }
      PrimitiveType.fromType(pt, entry).to(bb);
    }
  }

  @Override
  public int byteSize() {
    return CBUtil.byteSize(internalSize(), false);
  }
  
  public int internalSize() {
    int size = 4; //elems
    if(size() < 1)
      return size;
    size++; //type indicator
    PrimitiveType pt = null;
    for(E entry : value){
      if(pt == null){
        pt = PrimitiveType.type(entry);
      }
      size += PrimitiveType.fromType(pt, entry).byteSize();
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
    return value.toArray(a);
  }

  @Override
  public boolean add(E e) {
    return value.add(e);
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
  public boolean retainAll(Collection<?> c) {
    return value.retainAll(c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return value.remove(c);
  }

  @Override
  public void clear() {
    value.clear();
  }

}
