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

package org.mellowtech.core.collections.impl;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.collections.BTree;
import org.mellowtech.core.collections.KeyValue;
import org.mellowtech.core.collections.TreePosition;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.logging.Level;

import static java.nio.file.StandardOpenOption.*;

/**
 * A BPTreeImp that allows for large values
 * Date: 2013-03-22
 * Time: 07:52
 *
 * @author Martin Svensson
 */
public class BTreeBlobImp<A,B extends BComparable<A,B>,C,D extends BStorable<C,D>>
  implements BTree<A,B,C,D> {

  FileChannel blobs;
  BTreeImp <A,B,?,BlobPointer> tree;
  D template;


  public BTreeBlobImp(Path dir, String name, Class<B> keyType, Class<D> valueType,
                      boolean oneFileTree, boolean mappedIndex,
                      boolean mappedValues) throws Exception {
    tree = new BTreeImp<>(dir, name, keyType, BlobPointer.class, oneFileTree, mappedIndex, mappedValues);
    this.template = valueType.newInstance();
    blobs = FileChannel.open(blobPath(), WRITE, READ);
  }

  public BTreeBlobImp(Path dir, String name, Class<B> keyType, Class<D> valueType,
                      int indexBlockSize, int valueBlockSize,
                      int maxIndexBlocks, int maxValueBlocks,
                      boolean oneFileTree, boolean mappedIndex, boolean mappedValues) throws Exception {
    tree = new BTreeImp <> (dir, name, keyType, BlobPointer.class,
        indexBlockSize, valueBlockSize, maxIndexBlocks, maxValueBlocks,
        oneFileTree, mappedIndex, mappedValues, true);
    this.template = valueType.newInstance();
    blobs = FileChannel.open(blobPath(), WRITE, READ, TRUNCATE_EXISTING, CREATE);
  }

  private Path blobPath(){
    return tree.dir.resolve(tree.name+".blb");
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
    Files.delete(blobPath());
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
    ByteBuffer bb = value.to(); bb.flip();
    tree.put(key, bp);
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
    KeyValue <B,BlobPointer> tmp = tree.getKeyValue(key);
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
  }*/

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
  public void rebuildIndex() throws IOException {
    tree.rebuildIndex();
  }

  @Override
  public void createTree(Iterator<KeyValue<B,D>> iterator) throws IOException {
    blobs.truncate(0);
    BlobMapCreateIterator <B,D> iter = new BlobMapCreateIterator <> (iterator,blobs);
    tree.createTree(iter);
  }

}
