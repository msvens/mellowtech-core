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

package org.mellowtech.core.test;

import org.mellowtech.core.io.compress.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

/**
 * @author msvens
 * @since 2018-02-07
 */
public class CompressFileTest {

  final static int BLOCK_SIZE = 64*1024*1024;

  public static void main(String[] args) throws IOException{
    Path input = Paths.get("/Users/msvens/temp/english.1024MB");
    Path output = Paths.get("/Users/msvens/temp/english.lz4");
    Path output2 = Paths.get("/Users/msvens/temp/english.1024MB-2");
    Path output3 = Paths.get("/Users/msvens/temp/english.1024MB-3");
    //writeBlockFile(input, output);
    readBlockFile(output);
    //readMappedBlockFile(output);
    //uncompressMappedBlockFile(output, output2);
    //uncompressBlockFile(output, output3);
  }

  static void uncompressBlockFile(Path file, Path output) throws IOException {
    CFileReader reader = new CFileBuilder().blockSize(BLOCK_SIZE).path(file).reader();
    ByteBuffer bb = ByteBuffer.allocateDirect(BLOCK_SIZE);
    FileChannel fc = FileChannel.open(output, StandardOpenOption.CREATE_NEW,StandardOpenOption.APPEND);
    System.out.println("reader: "+reader.getClass());
    Iterator<BlockPointer> iter = reader.iterator();
    while(iter.hasNext()){
      BlockPointer ptr = iter.next();
      bb.clear();
      reader.read(ptr.getOffset(), bb, 0);
      bb.limit(ptr.getOrigSize());
      fc.write(bb);
    }
    System.out.println("closing files");
    fc.close();
    reader.close();
  }

  static void uncompressMappedBlockFile(Path file, Path output) throws IOException {
    CFileReader reader = new CFileBuilder().mapped().path(file).reader();
    ByteBuffer bb = ByteBuffer.allocateDirect(BLOCK_SIZE);
    FileChannel fc = FileChannel.open(output, StandardOpenOption.CREATE_NEW,StandardOpenOption.APPEND);
    Iterator<BlockPointer> iter = reader.iterator();
    System.out.println("reader: "+reader.getClass());
    while(iter.hasNext()){
      BlockPointer ptr = iter.next();
      bb.clear();
      reader.read(ptr.getOffset(), bb, 0);
      bb.limit(ptr.getOrigSize());
      fc.write(bb);
    }
    System.out.println("closing files");
    fc.close();
    reader.close();
  }

  static void readBlockFile(Path file) throws IOException{
    CFileReader reader = new CFileBuilder().path(file).blockSize(BLOCK_SIZE).reader();
    System.out.println("reader: "+reader.getClass());
    Iterator<BlockPointer> iter = reader.iterator();
    while(iter.hasNext()){
      BlockPointer ptr = iter.next();
      System.out.println("reading block: "+ptr);
      ByteBuffer bb = reader.read(ptr.getOffset());
    }
    reader.close();
  }

  static void readMappedBlockFile(Path file) throws IOException{
    CFileReader reader = new CFileBuilder().mapped().path(file).reader();
    System.out.println("reader: "+reader.getClass());
    Iterator<BlockPointer> iter = reader.iterator();
    while(iter.hasNext()){
      BlockPointer ptr = iter.next();
      System.out.println("reading block: "+ptr);
      ByteBuffer bb = reader.read(ptr.getOffset());
    }
    reader.close();
  }

  static void writeBlockFile(Path inputFile, Path outputFile) throws IOException{

    ByteBuffer bb = ByteBuffer.allocate(BLOCK_SIZE);
    CFileWriter writer = new CFileBuilder().blockSize(BLOCK_SIZE).path(outputFile).writer();
    FileChannel fc = FileChannel.open(inputFile, StandardOpenOption.READ);
    System.out.println("File Size: "+fc.size());
    while(true){
      bb.clear();
      System.out.println("file offset: "+fc.position());
      int read = fc.read(bb);
      bb.flip();
      System.out.println(bb.limit());
      if(read == -1)
        break;
      if(read > 0){
        writer.add(bb);
      }
    }
    writer.close();
  }
}
