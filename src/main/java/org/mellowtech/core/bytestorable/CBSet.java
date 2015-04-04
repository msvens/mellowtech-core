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
public class CBSet<E> extends ByteStorable<Set<E>> implements Set <E>{
  
  private Set<E> set;


  public CBSet (){
    this.set = new HashSet<E>();
  }
  
  

  @Override
  public Set<E> get() {
    return set;
  }

  @Override
  public void set(Set<E> obj) {
    this.set = obj;
  }



  @Override
  public ByteStorable<Set<E>> fromBytes(ByteBuffer bb, boolean doNew) {
    CBSet <E> toRet = doNew ? new CBSet <E> () : this;
    toRet.clear();
    bb.getInt(); //past size;
    int elems = bb.getInt();
    if(elems < 1) return toRet;
    PrimitiveType elemType = PrimitiveType.fromOrdinal(bb.get());

    if(elemType == null)
      throw new ByteStorableException("Unrecognized type");

    ByteStorable <E> elemTemp = PrimitiveType.fromType(elemType);

    for(int i = 0; i < elems; i++){
      E e = elemTemp.fromBytes(bb).get();
      toRet.add(e);
    }
    return toRet;
  }

  @Override
  public void toBytes(ByteBuffer bb) {
    bb.putInt(byteSize()); //this could be done more efficiently
    bb.putInt(set.size());

    if(set.size() < 1)
      return;

    ByteStorable <E> elemTemp = null;
    byte elemType = 0;

    int pos = bb.position(); //we will later write in here:
    bb.put(elemType); //just write dummy values;
    
    for(E entry : set){
      if(elemTemp == null){
        PrimitiveType pt = PrimitiveType.type(entry);
        if(pt == null) throw new ByteStorableException("Unrecognized key type");
        elemType = pt.getByte();
        elemTemp = PrimitiveType.fromType(pt);
      }
      elemTemp.set(entry);
      elemTemp.toBytes(bb);
    }
    bb.put(pos, elemType);
  }

  @Override
  public int byteSize() {
    int size = 8 + 1; //size + elems + type
    if(size() < 1)
      return size;
    ByteStorable <E> elemTemp = null;
    byte elemType;
    
    for(E entry : set){
      if(elemTemp == null){
        PrimitiveType pt = PrimitiveType.type(entry);
        if(pt == null) throw new ByteStorableException("Unrecognized key type");
        elemType = pt.getByte();
        elemTemp = PrimitiveType.fromType(pt);
      }
      elemTemp.set(entry);
      size += elemTemp.byteSize();
      size++;
    }
    return size;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int size() {
    return set.size();
  }

  @Override
  public boolean isEmpty() {
    return set.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return set.contains(o);
  }

  @Override
  public Iterator<E> iterator() {
    return set.iterator();
  }

  @Override
  public Object[] toArray() {
    return set.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return set.toArray(a);
  }

  @Override
  public boolean add(E e) {
    return set.add(e);
  }

  @Override
  public boolean remove(Object o) {
    return set.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return set.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    return set.addAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return set.retainAll(c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return set.remove(c);
  }

  @Override
  public void clear() {
    set.clear();
    
  }

}
