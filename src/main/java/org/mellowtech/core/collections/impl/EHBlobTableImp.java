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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static java.nio.file.StandardOpenOption.*;

import java.nio.file.Path;
import java.util.Iterator;

import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.codec.BCodec;
import org.mellowtech.core.collections.BMap;
import org.mellowtech.core.collections.KeyValue;

@SuppressWarnings("unchecked")
public class EHBlobTableImp <A,B> implements BMap <A,B>{
  
  private final FileChannel blobs;
  private final EHTableImp <A,BlobPointer> eht;
  private final BCodec<B> valueCodec;
  private Path fName;

  public EHBlobTableImp(Path fName, BCodec<A> keyCodec,
                        BCodec<B> valueCodec, boolean inMemory) throws Exception{
    this.fName = fName;
    //@SuppressWarnings("resource")
    eht = new EHTableImp <> (fName, keyCodec, new BlobPointerCodec(), inMemory);
    this.valueCodec = valueCodec;
    File f = new File(fName+".blb");
    blobs = FileChannel.open(f.toPath(), WRITE, READ); 
  }
  
  public EHBlobTableImp(Path fName, BCodec<A> keyCodec,
                        BCodec<B> valueCodec,
      boolean inMemory, int bucketSize, int maxBuckets) throws Exception{
    this.fName = fName;
    eht = new EHTableImp <> (fName, keyCodec, new BlobPointerCodec(), inMemory, bucketSize, maxBuckets);
    this.valueCodec = valueCodec;
    File f = new File(fName+".blb");
    blobs = FileChannel.open(f.toPath(), WRITE, READ, TRUNCATE_EXISTING, CREATE); 
  }
  
  @Override
  public void save() throws IOException {
    eht.save();
    blobs.force(true);
  }

  @Override
  public void close() throws IOException {
    eht.close();
    blobs.close();
  }

  @Override
  public void delete() throws IOException {
    eht.delete();
    blobs.close();
    File f = new File(fName+".blb");
    f.delete();
  }

  @Override
  public void truncate() throws IOException {
    eht.truncate();
    blobs.truncate(0);
  }

  @Override
  public int size() throws IOException {
    return eht.size();
  }

  @Override
  public boolean isEmpty() throws IOException {
    return eht.isEmpty();
  }

  @Override
  public boolean containsKey(A key) throws IOException {
    return eht.containsKey(key);
  }

  @Override
  public void put(A key, B value) throws IOException {
    int size = valueCodec.byteSize(value);
    long fpos = blobs.size();
    BlobPointer bp = new BlobPointer(fpos, size);
    ByteBuffer bb = valueCodec.to(value); bb.flip();
    eht.put(key, bp);
    blobs.write(bb, fpos);
    
  }

  @Override
  public B remove(A key) throws IOException {
    BlobPointer bp = eht.remove(key);
    return bp != null ? getValue(bp) : null;
  }

  @Override
  public KeyValue<A,B> getKeyValue(A key) throws IOException {
    BlobPointer tmp = eht.get(key);
    if(tmp != null){
      return new KeyValue<>(key, getValue(tmp));
    } else
      return null;
  }

  @Override
  public Iterator<KeyValue<A,B>> iterator() {
    return new EHBlobIterator();
  }

  
  private B getValue(BlobPointer bp) throws IOException{
    ByteBuffer bb = ByteBuffer.allocate(bp.bSize);
    blobs.read(bb, bp.fPointer);
    bb.flip();
    return valueCodec.from(bb);
  }
  
  private class EHBlobIterator implements Iterator <KeyValue <A,B>>{

    Iterator <KeyValue <A, BlobPointer>> iter;

    EHBlobIterator(){
      iter = eht.iterator();
    }


    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public KeyValue<A,B> next() {
      KeyValue <A, BlobPointer> next = iter.next();
      if(next == null) return null;
      KeyValue <A,B> toRet = new KeyValue<>(next.getKey(), null);
      if(next.getValue() != null){
        try{
          toRet.setValue(getValue(next.getValue()));
        }
        catch(IOException e){
          throw new RuntimeException(e);
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
