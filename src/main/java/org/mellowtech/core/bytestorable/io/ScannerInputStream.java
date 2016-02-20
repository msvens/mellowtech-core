
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

package org.mellowtech.core.bytestorable.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Scanner;

import org.mellowtech.core.bytestorable.CBString;

/**
 * Stream that emits BStorables from tokens pulled from a scanner
 * @author msvens
 *
 */
@Deprecated
public class ScannerInputStream extends InputStream {

  ByteBuffer bb = ByteBuffer.allocate(1024);
  
  private boolean closed = false;
  
  private final Scanner s;
  private final int minLength;
  
  public ScannerInputStream(Scanner s){
    this(s, -1);
    
  }
  
  public ScannerInputStream(Scanner s, int minLength){
   this.s = s;
   this.minLength = minLength;
   readNext();
  }
  
  @Override
  public int available() throws IOException {
    return bb.remaining();
  }

  @Override
  public int read() throws IOException {
    if(closed == true) return -1;
    if(bb.hasRemaining()){
      return bb.get();
    } 
    readNext();
    if(closed){
      return -1;
    } else {
      return bb.get();
    }
  }
  
  private void readNext(){
    if(closed) return;
    if(minLength > -1){
      readMinLength();
      return;
    }
    if(!s.hasNext()){
      closed = true;
      return;
    }
    CBString temp = new CBString(s.next());
    if(temp.get().length() < 1) System.out.println("zero length");
    int bSize = temp.byteSize();
    if(bb.capacity() < bSize) bb = ByteBuffer.allocate(bSize);
    bb.clear();
    bb.limit(bSize);
    temp.to(bb);
    bb.flip();
  }
  
  private void readMinLength() {
    while(s.hasNext()){
      CBString temp = new CBString(s.next());
      //temp.set(s.next());
      if(temp.get().length() > minLength){
        int bSize = temp.byteSize();
        if(bb.capacity() < bSize) bb = ByteBuffer.allocate(bSize);
        bb.clear();
        bb.limit(bSize);
        temp.to(bb);
        bb.flip();
        return;
      }
    }
    closed = true;
    return;
  }

}
