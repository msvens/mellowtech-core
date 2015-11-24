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

package org.mellowtech.core.collections.impl;

import static java.nio.file.StandardOpenOption.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.collections.BTree;
import org.mellowtech.core.collections.KeyValue;
import org.mellowtech.core.collections.TreePosition;

/**
 * A BPTreeImp that allows for large values
 * Date: 2013-03-22
 * Time: 07:52
 *
 * @author Martin Svensson
 */
public class BPBlobTreeImp <A,B extends BComparable<A,B>, C, D extends BStorable<C,D>>
  implements BTree<A,B,C,D> {

  FileChannel blobs;
  BPTreeImp <A,B, ?,BlobPointer> tree;
  D template;
  private final String fName;

  public BPBlobTreeImp(String fName, Class <B> keyType, Class <D> valueType) throws Exception {
    tree = new BPTreeImp<>(fName, keyType, BlobPointer.class);
    this.template = valueType.newInstance();
    this.fName = fName;
    File f = new File(fName+".blb");
    blobs = FileChannel.open(f.toPath(), WRITE, READ); 
  }

  public BPBlobTreeImp(String fName, Class<B> keyType, Class<D> valueType, int valueBlockSize, int indexBlockSize,
      int maxBlocks, int maxIndexBlocks) throws Exception {
    tree = new BPTreeImp <>(fName, keyType, BlobPointer.class, valueBlockSize, indexBlockSize, maxBlocks, maxIndexBlocks);
    this.template = valueType.newInstance();
    this.fName = fName;
    File f = new File(fName+".blb");
    blobs = FileChannel.open(f.toPath(), WRITE, READ, TRUNCATE_EXISTING, CREATE); 
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
  public void truncate() throws IOException {
    tree.truncate();
    blobs.truncate(0);
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
  public boolean containsKey(B key) throws IOException {
    return tree.containsKey(key);
  }

  @Override
  public void put(B key, D value) throws IOException {
    int size = value.byteSize();
    long fpos = blobs.size();
    BlobPointer bp = new BlobPointer(fpos, size);
    tree.put(key, bp);
    ByteBuffer bb = value.to(); bb.flip();
    blobs.write(bb, fpos);
  }

  @Override
  public void putIfNotExists(B key, D value) throws IOException {
    if(containsKey(key)) return;
    put(key, value);
  }

  @Override
  public D remove(B key) throws IOException{
    BlobPointer bp = tree.remove(key);
    return bp != null ? getValue(bp) : null;
  }

  @Override
  public D get(B key) throws IOException {
    BlobPointer bp = tree.get(key);
    return bp != null ? getValue(bp) : null;
  }

  @Override
  public B getKey(int position) throws IOException {
    return tree.getKey(position);
  }

  @Override
  public KeyValue<B,D> getKeyValue(B key) throws IOException {
    KeyValue <B, BlobPointer> tmp = tree.getKeyValue(key);
    if(tmp != null){
      KeyValue <B,D> kv = new KeyValue<>(tmp.getKey(), null);
      if(tmp.getValue() != null)
        kv.setValue(getValue(tmp.getValue()));
      return kv;
    } else
      return null;
  }

  @Override
  public TreePosition getPosition(B key) throws IOException {
    return tree.getPosition(key);
  }

  @Override
  public TreePosition getPositionWithMissing(B key) throws IOException {
    return tree.getPositionWithMissing(key);
  }

  /*@Override
  public Iterator<KeyValue<B,D>> iterator() {
    return new BPBlobIterator();
  }
  */

  @Override
  public Iterator<KeyValue<B,D>> iterator(boolean descending, B from, boolean inclusive, B to, boolean toInclusive) {
    return new BPBlobIterator(descending, from, inclusive, to, toInclusive);
  }

  @Override
  public void compact() throws IOException {
    tree.compact();
  }

  private D getValue(BlobPointer bp) throws IOException{
    ByteBuffer bb = ByteBuffer.allocate(bp.getbSize());
    blobs.read(bb, bp.getfPointer());
    bb.flip();
    return template.from(bb);
  }

  class BPBlobIterator implements Iterator <KeyValue <B,D>>{

    Iterator <KeyValue <B, BlobPointer>> iter;

    public BPBlobIterator(){
      iter = tree.iterator();
    }

    public BPBlobIterator(boolean descending, B from, boolean inclusive, B to, boolean toInclusive){
      iter = tree.iterator(descending, from, inclusive, to, toInclusive);
    }

    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public KeyValue<B,D> next() {
      KeyValue <B, BlobPointer> next = iter.next();
      if(next == null) return null;
      KeyValue <B,D> toRet = new KeyValue<>(next.getKey(), null);
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

  @Override
  public void createIndex(Iterator<KeyValue<B,D>> iterator) throws IOException {
    BlobMapCreateIterator <B,D> iter = new BlobMapCreateIterator <> (iterator,blobs);
    tree.createIndex(iter);
  }

}
