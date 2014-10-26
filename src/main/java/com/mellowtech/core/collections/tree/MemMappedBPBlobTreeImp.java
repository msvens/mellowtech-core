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

package com.mellowtech.core.collections.tree;

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteComparable;
import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.collections.KeyValue;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * A BPTreeImp that allows for large values
 * Date: 2013-03-22
 * Time: 07:52
 *
 * @author Martin Svensson
 */
public class MemMappedBPBlobTreeImp<K extends ByteComparable, V extends ByteStorable>
  implements BTree <K, V>{

  FileChannel blobs;
  MemMappedBPTreeImp <K, BlobPointer> tree;
  V template;
  String fName;


  public MemMappedBPBlobTreeImp(String fName, K keyType, V valueType, boolean inMemory) throws Exception {
    tree = new MemMappedBPTreeImp<>(fName, keyType, new BlobPointer(), inMemory);
    this.template = valueType;
    this.fName = fName;
    @SuppressWarnings("resource")
    RandomAccessFile raf = new RandomAccessFile(fName+".blb", "rw");
    blobs = raf.getChannel();
  }

  public MemMappedBPBlobTreeImp(String fName, K keyType, V valueType, int indexSize, int valueSize,
      boolean inMemory, int maxBlocks) throws IOException {
    tree = new MemMappedBPTreeImp <> (fName, keyType, new BlobPointer(), indexSize, valueSize, inMemory, maxBlocks);
    this.template = valueType;
    this.fName = fName;
    @SuppressWarnings("resource")
    RandomAccessFile raf = new RandomAccessFile(fName+".blb", "rw");
    raf.setLength(0);
    blobs = raf.getChannel();
  }

  @Override
  public void save() throws IOException {
    tree.save();
    blobs.force(true);
  }

  @Override
  public void close() throws IOException {
    tree.close();
    blobs.close();
  }

  @Override
  public void delete() throws IOException {
    tree.delete();
    blobs.close();
    File f = new File(fName+".blb");
    f.delete();
  }

  @Override
  public int size() throws IOException {
    return tree.size();
  }

  @Override
  public boolean isEmpty() throws IOException {
    return tree.isEmpty();
  }

  @Override
  public boolean containsKey(K key) throws IOException {
    return tree.containsKey(key);
  }

  @Override
  public void put(K key, V value) throws IOException {
    int size = value.byteSize();
    long fpos = blobs.size();
    BlobPointer bp = new BlobPointer(fpos, size);
    ByteBuffer bb = value.toBytes(); bb.flip();
    tree.put(key, bp);
    blobs.write(bb, fpos);
  }

  @Override
  public void putIfNotExists(K key, V value) throws IOException {
    if(containsKey(key)) return;
    put(key, value);
  }

  @Override
  public V remove(K key) throws IOException{
    BlobPointer bp = tree.remove(key);
    return bp != null ? getValue(bp) : null;
  }

  @Override
  public V get(K key) throws IOException {
    BlobPointer bp = tree.get(key);
    return bp != null ? getValue(bp) : null;
  }

  @Override
  public K getKey(int position) throws IOException {
    return tree.getKey(position);
  }

  @Override
  public KeyValue<K, V> getKeyValue(K key) throws IOException {
    KeyValue <K, BlobPointer> tmp = tree.getKeyValue(key);
    if(tmp != null){
      KeyValue <K, V> kv = new KeyValue<>(tmp.getKey(), null);
      if(tmp.getValue() != null)
        kv.setValue(getValue(tmp.getValue()));
    }
    return null;
  }

  @Override
  public TreePosition getPosition(K key) throws IOException {
    return tree.getPosition(key);
  }

  @Override
  public TreePosition getPositionWithMissing(K key) throws IOException {
    return tree.getPositionWithMissing(key);
  }

  @Override
  public Iterator<KeyValue<K, V>> iterator() {
    return new BPBlobIterator();
  }

  @Override
  public Iterator<KeyValue<K, V>> iterator(K from) {
    return new BPBlobIterator(from);
  }

  @Override
  public void compact() throws IOException {
    tree.compact();
  }

  private V getValue(BlobPointer bp) throws IOException{
    ByteBuffer bb = ByteBuffer.allocate(bp.getbSize());
    blobs.read(bb, bp.getfPointer());
    bb.flip();
    return (V) template.fromBytes(bb, true);
  }

  class BPBlobIterator implements Iterator <KeyValue <K,V>>{

    Iterator <KeyValue <K, BlobPointer>> iter;

    public BPBlobIterator(){
      iter = tree.iterator();
    }

    public BPBlobIterator(K from){
      iter = tree.iterator(from);
    }

    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public KeyValue<K, V> next() {
      KeyValue <K, BlobPointer> next = iter.next();
      if(next == null) return null;
      KeyValue <K, V> toRet = new KeyValue<>(next.getKey(), null);
      if(next.getValue() != null){
        try{
          toRet.setValue(getValue(next.getValue()));
        }
        catch(IOException e){
          CoreLog.L().log(Level.WARNING, "could not iterate", e);

        }
      }
      return toRet;
    }

    @Override
    public void remove() throws UnsupportedOperationException{
      throw new UnsupportedOperationException();
    }
  }

}
