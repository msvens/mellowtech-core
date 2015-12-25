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
package org.mellowtech.core.bytestorable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Interface that defines base functionality for objects that can be
 * transformed to and from bytes.
 * 
 * @author Martin Svensson
 *
 * @param <A> type of value
 * @param <B> self type
 */
public interface BStorable <A, B extends BStorable<A,B>> {
  
  
  A get();
  
  B from(ByteBuffer bb);
  
  default B create(A t){
    try {
      @SuppressWarnings("unchecked")
      Constructor <B> c = (Constructor<B>) this.getClass().getConstructor(t.getClass());
      return c.newInstance(t);
    }
    catch(Exception e){
      throw new ByteStorableException("no such constructor method");
    }
  }
  
  default B from(byte[] b, int offset) {
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.position(offset);
    return from(bb);
  }
  
  default B from(InputStream is) throws IOException{
      ByteBuffer bb = ByteBuffer.allocate(4);
      int b;
      int i;
      for(i = 0; i < 4; i++){
        b = is.read();
        if(b == -1)
          break;
        bb.put((byte)b);
      }
      bb.flip();
      int byteSize = this.byteSize(bb);
      ByteBuffer bb1 = ByteBuffer.allocate(byteSize);
      bb1.put(bb);
      for(; i < byteSize; i++){
        b = is.read();
        if(b == -1)
          throw new IOException("Unexpected end of stream: read object");
        bb1.put((byte)b);
      }
      bb1.flip();
      return this.from(bb1);
  }
  
  default B from(ReadableByteChannel rbc) throws IOException{
    ByteBuffer bb = ByteBuffer.allocate(4);
    ByteBuffer one = ByteBuffer.allocate(1);
    //int b;
    int i;
    for(i = 0; i < 4; i++){
      int read = rbc.read(one);
      if(read == -1)
        break;
      bb.put(one.get(0));
      one.clear();
    }
    bb.flip();
    int byteSize = this.byteSize(bb);
    ByteBuffer bb1 = ByteBuffer.allocate(byteSize);
    bb1.put(bb);
    rbc.read(bb1);
    bb1.flip();
    return this.from(bb1);
  }
  
  void to(ByteBuffer bb);
  
  default ByteBuffer to() {
    ByteBuffer bb = ByteBuffer.allocate(byteSize());
    to(bb);
    return bb;
  }
  
  default int to(byte[] b, int offset) {
    ByteBuffer bb = ByteBuffer.wrap(b);
    bb.position(offset);
    to(bb);
    return bb.position() - offset;
  }
  
  default int to(OutputStream os) throws IOException{
    int byteSize = byteSize();
    byte[] b = new byte[byteSize];
    to(b, 0);
    for(int i = 0; i < b.length; i++)
      os.write(b[i]);
    return byteSize;
  }
  
  default int to(WritableByteChannel wbc) throws IOException {
    int byteSize = byteSize();
    ByteBuffer bb = ByteBuffer.allocate(byteSize);
    to(bb);
    bb.flip();
    wbc.write(bb);
    return byteSize;
  }
  
  int byteSize();
  int byteSize(ByteBuffer bb);
  
  default B deepCopy(){
    ByteBuffer bb = to();
    bb.flip();
    return from(bb);
  }
  
  default boolean isFixed() {return false;}

}
