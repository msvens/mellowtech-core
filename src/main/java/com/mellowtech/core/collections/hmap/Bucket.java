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
package com.mellowtech.core.collections.hmap;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.collections.KeyValue;


public class Bucket <K extends ByteStorable <?>,V extends ByteStorable <?>> extends ByteStorable <Bucket <K,V>.BE> {
  
  public class BE {
    public int depth;
    public LinkedList<KeyValue <K,V>> keyValues;
    
    public String toString(){
      StringBuffer sb = new StringBuffer();
      sb.append("bucket depth: " + depth + " ");
      for (Iterator <KeyValue <K,V>> it = iterator(); it.hasNext();) {
        sb.append(it.next() + "\n");
      }
      return sb.toString();
    }
  }
  
  private BE be;
  
  
  private int bSize;
  
  private KeyValue <K, V> kvTemplate;

  public Bucket() {
    be = new BE();
    be.keyValues = new LinkedList<KeyValue <K,V>>();
    bSize = 12; // size, depth, number of keys
    be.depth = 0;
  }
  
  public Bucket(KeyValue <K,V> template){
    this();
    kvTemplate = template;
  }

  public void setKeyValueTemplate(KeyValue <K,V> template) {
    kvTemplate = template;
  }

  public String toString() {
    return be.toString();
  }

  public Iterator <KeyValue <K,V>> iterator() {
    return new BucketIterator();
  }

  public KeyValue <K,V> getKey(KeyValue  <K,V> keyValue) {
    int cmp = -1;
    KeyValue <K,V> toGet;
    for (ListIterator <KeyValue <K,V>> it = be.keyValues.listIterator(); it.hasNext();) {
      toGet = it.next();
      cmp = keyValue.compareTo(toGet);
      if (cmp == 0)
        return toGet;
      else if (cmp < 0) // key is smaller...break
        return null;
    }
    return null;
  }

  public int size() {
    return be.keyValues.size();
  }

  public void addKey(KeyValue <K,V> keyValue) {
    // first remove:
    int cmp = -1;
    KeyValue <K,V> toRemove;
    bSize += keyValue.byteSize();
    for (ListIterator<KeyValue <K,V>> it = be.keyValues.listIterator(); it.hasNext();) {
      toRemove = it.next();
      cmp = keyValue.compareTo(toRemove);
      if (cmp == 0) { // replace old value
        bSize -= toRemove.byteSize();
        it.set(keyValue);
        return;
      }
      else if (cmp < 0) { // key is smaller...insert
        it.previous();
        it.add(keyValue);
        return;
      }
    }
    be.keyValues.add(keyValue);
  }

  public KeyValue <K,V> removeKey(KeyValue <K,V> keyValue) {
    int cmp = -1;
    KeyValue <K,V> toRemove;
    for (ListIterator<KeyValue <K,V>> it = be.keyValues.listIterator(); it.hasNext();) {
      toRemove = it.next();
      cmp = keyValue.compareTo(toRemove);
      if (cmp == 0) { // key was found...remove
        bSize -= toRemove.byteSize();
        it.remove();
        return toRemove;
      }
      else if (cmp < 0) { // the keyVaue is smaller...break
        return null;
      }
    }
    return null;
  }

  public KeyValue <K,V> getKey(int index) {
    return be.keyValues.get(index);
  }

  public void addLast(KeyValue <K,V> keyValue) {
    be.keyValues.add(keyValue);
    bSize += keyValue.byteSize();
  }

  // ***************************:
  public int byteSize() {
    return bSize;
  }

  public int byteSize(ByteBuffer bb) {
    return ByteStorable.getSizeFour(bb);
  }

  public void toBytes(ByteBuffer bb) {
    bb.putInt(bSize);
    bb.putInt(be.depth);
    bb.putInt(be.keyValues.size());
    KeyValue <K,V> tmp;
    for (Iterator <KeyValue <K,V>> i = be.keyValues.iterator(); i.hasNext();) {
      tmp = i.next();
      tmp.toBytes(bb);
    }
  }

  public ByteStorable <BE> fromBytes(ByteBuffer bb, boolean doNew) {
    Bucket <K,V> bucket = doNew ? new Bucket <K,V>() : this;
    int numKeys = -1;
    bucket.setKeyValueTemplate(kvTemplate);
    bucket.be.keyValues.clear();
    bucket.bSize = bb.getInt();
    bucket.be.depth = bb.getInt();
    numKeys = bb.getInt();
    for (int i = 0; i < numKeys; i++)
      bucket.be.keyValues.add((KeyValue <K,V>) bucket.kvTemplate.fromBytes(bb,
          doNew));
    return bucket;
  }
  
  

  @Override
  public Bucket<K, V>.BE get() {
    return be;
  }



  class BucketIterator implements Iterator <KeyValue <K,V>> {
    Iterator <KeyValue <K,V>> keyValueIterator;
    KeyValue <K,V> lastReturned;

    public BucketIterator() {
      keyValueIterator = be.keyValues.iterator();
    }

    public boolean hasNext() {
      return keyValueIterator.hasNext();
    }

    public KeyValue <K,V> next() {
      lastReturned = keyValueIterator.next();
      return lastReturned;
    }

    public void remove() {
      keyValueIterator.remove();
      bSize -= lastReturned.byteSize();
    }
  }
}
