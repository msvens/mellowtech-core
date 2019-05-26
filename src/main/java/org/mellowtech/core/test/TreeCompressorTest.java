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

import org.mellowtech.core.codec.IntCodec;
import org.mellowtech.core.codec.StringCodec;
import org.mellowtech.core.codec.io.CodecInputStream;
import org.mellowtech.core.codec.io.CodecOutputStream;
import org.mellowtech.core.collections.BTree;
import org.mellowtech.core.collections.BTreeBuilder;
import org.mellowtech.core.collections.KeyValue;
import org.mellowtech.core.collections.impl.HybridTree;
import org.mellowtech.core.collections.impl.ReadOnlyHybridTree;
import org.mellowtech.core.collections.impl.TreeCompressor;
import org.mellowtech.core.io.compress.CFileBuilder;
import org.mellowtech.core.sort.EDiscBasedSort;
import org.mellowtech.core.util.Platform;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * @author msvens
 * @since 2018-05-12
 */
public class TreeCompressorTest {


  public static void main(String[] args) throws Exception{
    //parse();
    //sort();
    //createTree();
    //compressTree();
    openCompressedTree();
  }

  public static void compressTree() throws Exception {
    //open tree
    Path p = Paths.get("/Users/msvens/projects/collections/btree/english");
    BTreeBuilder<Integer,String> builder = new BTreeBuilder<>();
    builder.valueCodec(new StringCodec()).keyCodec(new IntCodec()).memoryIndex(true).filePath(p);
    BTree<Integer,String> tree = builder.build();
    System.out.println(tree.size());
    Path cp = Paths.get("/Users/msvens/projects/collections/btree");
    TreeCompressor<Integer,String> compressor = new TreeCompressor<>();
    compressor.compress(tree, cp, "englishcomp", builder.keyCodec(), builder.valueCodec(), 1024*1024*64);

  }

  public static void openCompressedTree() throws Exception {
    //open tree
    Path p = Paths.get("/Users/msvens/projects/collections/btree/english");
    BTreeBuilder<Integer,String> builder = new BTreeBuilder<>();
    builder.valueCodec(new StringCodec()).keyCodec(new IntCodec()).memoryIndex(true).filePath(p);
    BTree<Integer,String> tree = builder.build();
    System.out.println("size: "+tree.size());

    int cnt = 0;
    ArrayList<KeyValue<Integer,String>> keys = new ArrayList<>();
    for(KeyValue<Integer,String> e : tree){
      if(++cnt % 100 == 0){
        keys.add(e);
      }
    }

    System.out.println(keys.size());


    ReadOnlyHybridTree <Integer,String> readOnlyTree;
    Path cp = Paths.get("/Users/msvens/projects/collections/btree");
    CFileBuilder cfbuilder = new CFileBuilder();
    cfbuilder.read();
    cfbuilder.blockSize(1024*1024*64);
    readOnlyTree = new ReadOnlyHybridTree<>(cp,"englishcomp",new IntCodec(), new StringCodec(), cfbuilder);
    System.out.println("size: "+readOnlyTree.size());

    System.out.println("search for keys");
    cnt = 0;
    for(KeyValue<Integer,String> e : keys){
      if(!readOnlyTree.containsKey(e.getKey()))
        System.out.println("could not find key");
    }


  }


  public static void createTree() throws Exception {
    StringCodec strCodec = new StringCodec();
    IntCodec intCodec = new IntCodec();
    BTreeBuilder<Integer,String> builder = new BTreeBuilder<>();
    builder.memoryIndex(true);
    HybridTree<Integer,String> tree;
    Path dir = Paths.get("/Users/msvens/projects/collections/btree/english");
    builder.filePath(dir);
    builder.codecs(intCodec,strCodec);
    tree = (HybridTree<Integer,String>) builder.build();
    CodecInputStream<String> sis = new CodecInputStream<>(new FileInputStream("/Users/msvens/projects/collections/english-sorted.1024MB.bs"), strCodec);
    //WordCountIter iter = new WordCountIter(sis);
    tree.createTree(new KeyValueIter(sis));
    System.out.println("Tree Size: "+tree.size());
    tree.verifyOrder();
    tree.close();
  }

  public static void parse() throws Exception{
    Pattern p = Pattern.compile("[\\s|\\p{Punct}]+");
    InputStream is = new GZIPInputStream(new FileInputStream("/Users/msvens/projects/collections/english.1024MB.gz"));
    Scanner s = new Scanner(is);
    s.useDelimiter(p);
    StringCodec stringCodec = new StringCodec();
    CodecOutputStream sos = new CodecOutputStream(new FileOutputStream("/Users/msvens/projects/collections/english.1024MB.bs"), stringCodec);
    int i = 0;
    while(s.hasNext()){
      String n = s.next();
      if(n.length() > 1){
        sos.write(n);
        i++;
      }
      if(i % 1000000 == 0)
        System.out.println(i);
    }
    sos.flush();
  }

  public static void sort() throws Exception {
    long l = System.currentTimeMillis();
    StringCodec codec = new StringCodec();
    //EDiscBasedSort <String> edb = new EDiscBasedSort<>(codec,4096,4096*2,1,Platform.getTempDir().resolve("sort"));
    EDiscBasedSort <String> edb = new EDiscBasedSort<>(new StringCodec(),8096, 1024*1024*256,0,Paths.get("/tmp"));
    //EDiscBasedSort<String> edb = new EDiscBasedSort <> (new StringCodec(), Paths.get("/tmp"));
    edb.sort(Paths.get("/Users/msvens/projects/collections/english.1024MB.bs"),
        Paths.get("/Users/msvens/projects/collections/english-sorted.1024MB.bs"));
    //edb.sort(Paths.get("/tmp/english.1024MB.bs"), Paths.get("/tmp/english-sorted.bs"), 1024*1024*160);
    System.out.println("esort took: "+ (System.currentTimeMillis() - l) + "ms");
  }

  static class KeyValueIter implements Iterator<KeyValue<Integer,String>> {

    CodecInputStream<String> cis;
    String nextWord;
    int cnt = 1;
    boolean endOfStrem = false;
    KeyValue<Integer, String> next;

    public KeyValueIter(CodecInputStream<String> input){
      cis = input;
      getNext();

    }
    @Override
    public boolean hasNext() {
      return !endOfStrem;
    }

    @Override
    public KeyValue<Integer,String> next() {
      if(endOfStrem) return null;
      KeyValue<Integer,String> toRet = next;
      getNext();
      return toRet;
    }

    private void getNext() {
      if(endOfStrem) return;
      try {
        nextWord = cis.next();
        if(nextWord == null){
          endOfStrem = true;
        } else {
          next = new KeyValue<>(cnt,nextWord);
          cnt++;
        }
      }
      catch(IOException e){
        e.printStackTrace();
        endOfStrem = true;
      }
    }
  }

}
