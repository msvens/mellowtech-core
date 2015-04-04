/**
 * 
 */
package org.mellowtech.core.bytestorable.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Scanner;

import org.mellowtech.core.bytestorable.CBString;

/**
 * Stream that emits ByteStorables from tokens pulled from a scanner
 * @author msvens
 *
 */
public class ScannerInputStream extends InputStream {

  ByteBuffer bb = ByteBuffer.allocate(1024);
  CBString temp = new CBString();
  
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
    temp.set(s.next());
    if(temp.get().length() < 1) System.out.println("zero length");
    int bSize = temp.byteSize();
    if(bb.capacity() < bSize) bb = ByteBuffer.allocate(bSize);
    bb.clear();
    bb.limit(bSize);
    temp.toBytes(bb);
    bb.flip();
  }
  
  private void readMinLength() {
    while(s.hasNext()){
      temp.set(s.next());
      if(temp.get().length() > minLength){
        int bSize = temp.byteSize();
        if(bb.capacity() < bSize) bb = ByteBuffer.allocate(bSize);
        bb.clear();
        bb.limit(bSize);
        temp.toBytes(bb);
        bb.flip();
        return;
      }
    }
    closed = true;
    return;
  }

}
