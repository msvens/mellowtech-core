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
import org.mellowtech.core.collections.KeyValue;
import org.mellowtech.core.collections.impl.HybridTree;
import org.mellowtech.core.io.RecordFileBuilder;
import org.mellowtech.core.util.DelDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author msvens
 * @since 2017-01-03
 */
public class ManyHybridTreeMultiFileTest {

  public static final int MultiFileSize = 1024 * 1024 * 4;
  public static String chars = "abcdefghijklmnopqrstuvxyz";
  public static int MAX_WORD_LENGTH = 20;
  public static int MAX_BYTES = 4000;
  public String[] manyWords, mAscend, mDescend;

  public ManyHybridTreeMultiFileTest() {


  }

  public String dir;
  HybridTree<String, Integer> tree;

  public static void createTestDir(String dir) {
    String testRoot = "/Users/msvens/treetests";
    File f = new File(testRoot + "/" + dir);
    if (f.exists())
      System.out.println("DIRECTORY ALREADY EXISTS");
    f.mkdir();
  }

  public static void deleteTestDir(String dir) {
    //String tempDir = Platform.getTempDir();
    String testRoot = "/Users/msvens/treetests";
    DelDir.d(testRoot + "/" + dir);
  }

  public static String getAbsoluteTestDir(String dir) {
    String testRoot = "/Users/msvens/treetests";
    return new File(testRoot + "/" + dir).getAbsolutePath();
  }

  static Path getDir(String fName) {
    return Paths.get(fName).getParent();
  }

  /**
   * @param chars
   * @param maxWordLength
   * @param maxBytes
   * @return
   */
  public static String[] randomWords(String chars, int maxWordLength, int maxBytes) {
    Random r = new Random();
    StringCodec codec = new StringCodec();
    StringBuilder sb = new StringBuilder();
    List<String> words = new ArrayList<>();
    Set<String> dups = new TreeSet<String>();
    int totalBytes = 0;
    while (totalBytes < maxBytes) {
      sb.setLength(0);
      int wlength = r.nextInt(maxWordLength) + 1;
      if (wlength == 1)
        wlength++;
      for (int i = 0; i < wlength; i++) {
        sb.append(chars.charAt(r.nextInt(chars.length())));
      }
      String tmp = sb.toString();
      if (!dups.contains(tmp)) {
        totalBytes += codec.byteSize(tmp);
        words.add(tmp);
        dups.add(tmp);
      }
    }
    //System.out.println(totalBytes);
    return words.toArray(new String[]{});
  }

  public static void main(String[] args){
    try {
      ManyHybridTreeMultiFileTest mht = new ManyHybridTreeMultiFileTest();
     /*mht.loadFaultyManyRemove();
      mht.setup();
      mht.putMany();
      mht.tree.printBlocks();
      System.out.println("verify ascending order: "+mht.tree.verifyOrder());
      System.out.println("verify descending order: "+mht.tree.verifyReverseOrder());
      System.out.println(mht.manyRemove());
      mht.teardown();*/
      //mht.findFaultyManyReverseIteratorRangeExclusive();
      //mht.findFaultyManyRemove();
      mht.findFaultyManyIteratorRangeExclusive();
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  public String fName() {
    return "hybridtreemultifile";
  }

  public HybridTree<String, Integer> init(String fileName, int valueBlockSize,
                                                      int indexBlockSize, int maxValueBlocks,
                                                      int maxIndexBlocks) throws Exception {

    RecordFileBuilder builder = new RecordFileBuilder().multi().blockSize(valueBlockSize).
        multiFileSize(MultiFileSize);

    return new HybridTree<>(getDir(fileName), fName(), new StringCodec(), new IntCodec(), builder);
  }

  public void loadFaultyManyRemove() throws Exception {
    loadWords("manyRemoveBroken.bs");
  }

  public void loadFaultyManyReverseIteratorRangeExclusive() throws Exception {
    loadWords("manyReverseIteratorRangeExclusiveBroken.bs");
  }

  public void findFaultyManyRemove() throws Exception {
    for(int i = 0; i < 1000; i++){
      setupWords();
      setup();
      if(!manyRemove()){
        System.out.println("many remove broke at the "+i+"th iteration saving words");
        saveWords("manyRemoveBroken.bs");
        teardown();
        break;
      }
      teardown();
    }
  }

  public void findFaultyManyReverseIteratorRangeExclusive() throws Exception {
    for(int i = 0; i < 1000; i++){
      setupWords();
      setup();
      if(!manyReverseIteratorRangeExclusive()){
        System.out.println("broke at the "+i+"th iteration saving words");
        saveWords("manyReverseIteratorRangeExclusiveBroken.bs");
        teardown();
        break;
      }
      teardown();
    }
  }

  public void findFaultyManyIteratorRangeExclusive() throws Exception {
    for(int i = 0; i < 1000; i++){
      setupWords();
      setup();
      if(!manyReverseIteratorRangeExclusive()){
        System.out.println("broke at the "+i+"th iteration saving words");
        saveWords("manyIteratorRangeExclusiveBroken.bs");
        teardown();
        break;
      }
      teardown();
    }
  }

  public boolean manyRemove() throws IOException{
    putMany();
    tree.save();
    for(String w : manyWords) {
      Integer rem = tree.remove(w);
      if(rem == null){
        System.out.println("this is word: "+w);
      }
      if(rem == null || !val(w).equals(rem)){
        System.out.println(val(w)+" "+rem);
        return false;
      }

    }
    return true;
  }

  public boolean manyIteratorRangeExclusive() throws Exception{
    putMany();
    int from = 51;
    int to = mAscend.length - 51;
    Iterator<KeyValue<String, Integer>> iter = tree.iterator(false, mAscend[from-1], false, mAscend[to+1], false);
    while(iter.hasNext()){
      String str = iter.next().getKey();
      if(mAscend[from].equals(str))
        from++;
      else {
        System.err.println(from+" "+str);
        return false;
      }
    }
    from--;
    if(from != to){
      System.err.println(from+" "+to);
      return false;
    }
    return true;
  }

  public boolean manyReverseIteratorRangeExclusive() throws Exception {
    putMany();
    int from = mAscend.length - 51;
    int to = 51;

    Iterator<KeyValue<String, Integer>> iter = tree.iterator(true, mAscend[from + 1], false, mAscend[to - 1], false);
    while (iter.hasNext()) {
      String str = iter.next().getKey();
      if (mAscend[from].equals(str))
        from--;
      else {
        System.err.println(from+" "+str);
        return false;
      }

    }
    from++;
    if (from != to) {
      System.err.println(from+" "+to);
      return false;
    }
    return true;
  }

  public BTree<String, Integer> reopen(String fileName, int valueBlockSize,
                                                        int indexBlockSize, int maxValueBlocks,
                                                        int maxIndexBlocks) throws Exception {

    RecordFileBuilder builder = new RecordFileBuilder().multi().blockSize(valueBlockSize).
        multiFileSize(MultiFileSize);

    return new HybridTree<>(getDir(fileName), fName(), new StringCodec(), new IntCodec(), builder);
  }

  public void setupWords(){
    manyWords = randomWords(chars, MAX_WORD_LENGTH, MAX_BYTES);
    mAscend = Arrays.copyOf(manyWords, manyWords.length);
    mDescend = Arrays.copyOf(manyWords, manyWords.length);
    Arrays.sort(mAscend);
    Arrays.sort(mDescend, Collections.reverseOrder());
  }

  public void setup() throws Exception {
    dir = "rftests";
    createTestDir(dir);
    tree = init(getAbsoluteTestDir(dir + "/" + fName()), 1024, 1024, 20, 5);
    //tree = init(TestUtils.getAbsolutDir(dir+"/"+fName()), 124, 124, 200, 200);
  }

  public void teardown() throws Exception {
    tree.close();
    tree.delete();
    deleteTestDir(dir);
    //TestUtils.deleteTempDir(dir);
  }

  protected void putMany() throws IOException {
    for (String w : manyWords) {
      tree.put(w, val(w));

    }
  }

  public void loadWords(String fName) throws Exception {
    System.out.println("loading: "+"/Users/msvens/treetests/"+fName);
    FileInputStream fis = new FileInputStream("/Users/msvens/treetests/"+fName);
    CodecInputStream<String> sis = new CodecInputStream<>(fis, new StringCodec());
    ArrayList<String> al = new ArrayList<>();
    while(true){
      String str = sis.next();
      if(str == null)
        break;
      al.add(str);
    }
    System.out.println(al.size());
    manyWords = al.toArray(new String[al.size()]);
    mAscend = Arrays.copyOf(manyWords, manyWords.length);
    mDescend = Arrays.copyOf(manyWords, manyWords.length);
    Arrays.sort(mAscend);
    Arrays.sort(mDescend, Collections.reverseOrder());
  }

  public void saveWords(String fName) throws Exception {
    FileOutputStream fos = new FileOutputStream("/Users/msvens/treetests/"+fName);
    CodecOutputStream <String> sos = new CodecOutputStream <> (fos, new StringCodec());
    for(String w : manyWords){
      sos.write(w);
    }
    sos.flush();
    fos.close();
  }

  protected Integer val(String key) {
    return key.length();
  }
}
