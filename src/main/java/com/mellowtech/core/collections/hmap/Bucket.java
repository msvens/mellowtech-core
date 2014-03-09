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


public class Bucket extends ByteStorable {
  public int depth;
  private int bSize;
  private LinkedList<KeyValue> keyValues;
  private KeyValue keyValueTemplate;

  public Bucket() {
    keyValues = new LinkedList<KeyValue>();
    bSize = 12; // size, depth, number of keys
    depth = 0;
  }

  public void setKeyValueTemplate(KeyValue kv) {
    keyValueTemplate = kv;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("bucket depth: " + depth + " ");
    for (Iterator it = iterator(); it.hasNext();) {
      sb.append(it.next() + "\n");
    }
    return sb.toString();
  }

  public Iterator <KeyValue> iterator() {
    return new BucketIterator();
  }

  public KeyValue getKey(KeyValue keyValue) {
    int cmp = -1;
    KeyValue toGet;
    for (ListIterator it = keyValues.listIterator(); it.hasNext();) {
      toGet = (KeyValue) it.next();
      cmp = keyValue.compareTo(toGet);
      if (cmp == 0)
        return toGet;
      else if (cmp < 0) // key is smaller...break
        return null;
    }
    return null;
  }

  public int size() {
    return keyValues.size();
  }

  public void addKey(KeyValue keyValue) {
    // first remove:
    int cmp = -1;
    KeyValue toRemove;
    bSize += keyValue.byteSize();
    for (ListIterator<KeyValue> it = keyValues.listIterator(); it.hasNext();) {
      toRemove = (KeyValue) it.next();
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
    keyValues.add(keyValue);
  }

  public KeyValue removeKey(KeyValue keyValue) {
    int cmp = -1;
    KeyValue toRemove;
    for (ListIterator<KeyValue> it = keyValues.listIterator(); it.hasNext();) {
      toRemove = (KeyValue) it.next();
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

  public KeyValue getKey(int index) {
    return (KeyValue) keyValues.get(index);
  }

  public void addLast(KeyValue keyValue) {
    keyValues.add(keyValue);
    bSize += keyValue.byteSize();
  }

  // ***************************:
  public int byteSize() {
    return bSize;
  }

  public int byteSize(ByteBuffer bb) {
    int bSize = bb.getInt();
    bb.position(bb.position() - 4);
    return bSize;
  }

  public void toBytes(ByteBuffer bb) {
    bb.putInt(bSize);
    bb.putInt(depth);
    bb.putInt(keyValues.size());
    KeyValue tmp;
    for (Iterator i = keyValues.iterator(); i.hasNext();) {
      tmp = (KeyValue) i.next();
      tmp.toBytes(bb);
    }
  }

  public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
    Bucket bucket = doNew ? new Bucket() : this;
    int numKeys = -1;
    bucket.setKeyValueTemplate(keyValueTemplate);
    bucket.keyValues.clear();
    bucket.bSize = bb.getInt();
    bucket.depth = bb.getInt();
    numKeys = bb.getInt();
    for (int i = 0; i < numKeys; i++)
      bucket.keyValues.add((KeyValue) bucket.keyValueTemplate.fromBytes(bb,
          doNew));
    return bucket;
  }

  class BucketIterator implements Iterator <KeyValue> {
    Iterator <KeyValue> keyValueIterator;
    KeyValue lastReturned;

    public BucketIterator() {
      keyValueIterator = keyValues.iterator();

    }

    public boolean hasNext() {
      return keyValueIterator.hasNext();
    }

    public KeyValue next() {
      lastReturned = keyValueIterator.next();
      return lastReturned;
    }

    public void remove() {
      keyValueIterator.remove();
      bSize -= lastReturned.byteSize();
    }
  }
}
