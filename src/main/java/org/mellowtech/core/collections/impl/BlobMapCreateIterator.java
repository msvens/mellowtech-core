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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;

import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.collections.KeyValue;

/**
 * @author msvens
 *
 */
public class BlobMapCreateIterator <K extends BComparable <?,K>,
  V extends BStorable<?,V>> implements Iterator<KeyValue <K, BlobPointer>> {

  private final Iterator <KeyValue<K,V>> iter;
  private final FileChannel fc;
  
  public BlobMapCreateIterator(Iterator <KeyValue<K,V>> iter, FileChannel fc){
    this.iter = iter;
    this.fc = fc;
  }
  
  @Override
  public boolean hasNext() {
    return iter.hasNext();
  }

  @Override
  public KeyValue<K, BlobPointer> next() {
    KeyValue <K,V> n = iter.next();
    if(n == null) return null;
    int size = n.getValue().byteSize();
    try{
      long fpos = fc.size();
      BlobPointer bp = new BlobPointer(fpos, size);
      ByteBuffer bb = n.getValue().to(); bb.flip();
      fc.write(bb, fpos);
      return new KeyValue <>(n.getKey(),bp);
    } catch(IOException e){
      throw new Error("could not store");
    }
    
  }

  @Override
  public void remove() throws UnsupportedOperationException{
   throw new UnsupportedOperationException();
    
  }

}
