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

import org.mellowtech.core.codec.BCodec;
import org.mellowtech.core.collections.BTree;
import org.mellowtech.core.collections.KeyValue;
import org.mellowtech.core.collections.TreePosition;
import org.mellowtech.core.io.RecordFileBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import static java.nio.file.StandardOpenOption.*;

/**
 * A BPTreeImp that allows for large values
 * Date: 2013-03-22
 * Time: 07:52
 *
 * @author Martin Svensson
 */
@SuppressWarnings("unchecked")
public class BTreeBlobImp<A,B> implements BTree<A,B> {

  private final FileChannel blobs;
  private final BTreeImp <A,BlobPointer> tree;
  private final BCodec<B> valueCodec;

  public BTreeBlobImp(Path dir, String name, BCodec<A> keyCodec, BCodec<B> valueCodec,
                      int indexBlockSize, int maxIndexBlocks, RecordFileBuilder valueFileBuilder) throws Exception {
    tree = new BTreeImp <> (dir, name, keyCodec, new BlobPointerCodec(),
        indexBlockSize, maxIndexBlocks, valueFileBuilder);
    this.valueCodec = valueCodec;
    blobs = FileChannel.open(blobPath(), CREATE, WRITE, READ);
    if(tree.isEmpty() && blobs.size() > 0){
      blobs.truncate(0);
    }
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
  public boolean containsKey(A key) throws IOException {
    return tree.containsKey(key);
  }

  @Override
  public void put(A key, B value) throws IOException {
    ByteBuffer bb = valueCodec.to(value);
    long fpos = blobs.size();
    BlobPointer bp = new BlobPointer(fpos, bb.capacity());
    bb.flip();
    tree.put(key, bp);
    blobs.write(bb, fpos);
  }

  @Override
  public void putIfNotExists(A key, B value) throws IOException {
    if(containsKey(key)) return;
    put(key, value);
  }

  @Override
  public B remove(A key) throws IOException{
    BlobPointer bp = tree.remove(key);
    return bp != null ? getValue(bp) : null;
  }

  @Override
  public B get(A key) throws IOException {
    BlobPointer bp = tree.get(key);
    return bp != null ? getValue(bp) : null;
  }

  @Override
  public A getKey(int position) throws IOException {
    return tree.getKey(position);
  }

  @Override
  public KeyValue<A,B> getKeyValue(A key) throws IOException {
    BlobPointer tmp = tree.get(key);
    if(tmp != null){
      return new KeyValue<>(key, getValue(tmp));
    } else
      return null;
  }

  @Override
  public TreePosition getPosition(A key) throws IOException {
    return tree.getPosition(key);
  }

  @Override
  public TreePosition getPositionWithMissing(A key) throws IOException {
    return tree.getPositionWithMissing(key);
  }


  @Override
  public Iterator<KeyValue<A,B>> iterator(boolean descending, A from, boolean inclusive,
                                          A to, boolean toInclusive) {
    return new BPBlobIterator(descending, from, inclusive, to, toInclusive);
  }

  @Override
  public void compact() throws IOException {
    tree.compact();
  }

  private B getValue(BlobPointer bp) throws IOException{
    ByteBuffer bb = ByteBuffer.allocate(bp.bSize);
    blobs.read(bb, bp.fPointer);
    bb.flip();
    return valueCodec.from(bb);
  }

  private class BPBlobIterator implements Iterator <KeyValue <A,B>>{

    Iterator <KeyValue <A, BlobPointer>> iter;

    BPBlobIterator(boolean descending, A from, boolean inclusive,
                   A to, boolean toInclusive){
      iter = tree.iterator(descending, from, inclusive, to, toInclusive);
    }

    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public KeyValue<A,B> next() {
      KeyValue <A, BlobPointer> next = iter.next();
      if(next == null) return null;
      KeyValue <A,B> toRet = new KeyValue<>(next.getKey());
      if(next.getValue() != null){
        try{
          toRet.setValue(getValue(next.getValue()));
        }
        catch(IOException e){
          throw new Error("could not iterate", e);

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
  public void createTree(Iterator<KeyValue<A,B>> iterator) throws IOException {
    blobs.truncate(0);
    BlobMapCreateIterator <A,B> iter = new BlobMapCreateIterator <> (iterator,blobs, valueCodec);
    tree.createTree(iter);
  }

}
