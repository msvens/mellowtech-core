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

package org.mellowtech.core.sort;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.mellowtech.core.codec.BCodec;
import org.mellowtech.core.codec.CodecUtil;
import org.mellowtech.core.util.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DiscBased sort sorts large amounts of data by combining in-memory sorting
 * with disc-based merging. It uses quicksort for the in-memory sorting step.
 * This sort also uses overlapping IO-opertations to maximize speed (i.e. the
 * creation of the array of objects to sort works in parallel with reading input
 * data). The Sorter operates on ByteStorable objects.
 * <p>
 * For sorting with an optimal memory utilization and minimal object conversion
 * turn to EDiscBasedSort.
 * </p>
 * 
 * @author Martin Svensson
 * @version 1.0
 * @see org.mellowtech.core.sort.EDiscBasedSort
 */
public class DiscBasedSort <A extends Comparable<A>> {
  static final String SORT_RUN_FILE = "disc_sort_d_run.";
  private static final String SEP = System.getProperties().getProperty(
      "file.separator");

  private final Logger logger = LoggerFactory.getLogger(DiscBasedSort.class);

  private static int blockSize = 1024;
  private BCodec<A> codec;
  private int complevel = 0;
  private Path tempDir = null;

  /**
   * Set the blocksize. How much data to read/write from disc. This number
   * should seldom be no more than 4096.
   * 
   * @param size
   *          block size. (a multiple of 1024)
   */
  public static void setBlockSize(int size) {
    blockSize = size;
  }

  public static int getBlockSize(){
    return blockSize;
  }

  /**
   * Create a new DiscBased sorter that will operate on a specific type of
   * objects.
   * 
   * @param codec
   *          the type of object to sort
   * @param tempDir
   *          temporary directory for sort runs
   */
  public DiscBasedSort(BCodec<A> codec, Path tempDir) {
    this(codec, 0, tempDir);
  }

  /**
   * Create a new DiscBased sorter that will operate on a specific type of
   * objects.
   * 
   * @param codec
   *          the type of object to sort
   * @param complevel
   *          the level of GZIP compression for runs (1-9, where 1 is fastest)
   * @param tempDir
   *          temporary directory for sort runs
   */
  public DiscBasedSort(BCodec<A> codec, int complevel, Path tempDir) {
    try {
      this.codec = codec;
    } catch(Exception e){throw new Error("could not create template instance");}
    this.complevel = complevel;
    Path tDir;
    try {
      tDir = tempDir;
      if(!Files.isDirectory(tDir))
        throw new IllegalArgumentException("tempDir is not a directory: "+tempDir);
    }
    catch (Exception e) {
      logger.info("Could not open temp dir: {}. Using default tempDir", tempDir);
      tDir = Platform.getTempDir();
    }
    this.tempDir = tDir;
  }

  /**
   * Sorts an inputfile and prints it to a designated outputfile. If these are
   * the same the inputfile will be overwritten.
   * 
   * @param fName
   *          File to sort
   * @param outputFile
   *          Ouputfile
   * @param memorySize
   *          The amount of memory that can be used for the in-memory sorting
   *          step
   * @return the number of objects sorted.
   */
  public int sort(String fName, String outputFile, int memorySize) {
    try(FileInputStream fis = new FileInputStream(fName);
        FileOutputStream fos = new FileOutputStream(outputFile)){
      FileChannel fc = fis.getChannel();
      FileChannel fo = fos.getChannel();
      return sort(fc, fo, memorySize);
    }
    catch(IOException e){
      logger.error("Could not sort",e);
      return -1;
    }
  }

  /**
   * Sorts a byte channel and print the result to a designated byte channel. If
   * these are the same the input channel will be overwritten.
   * 
   * @param input
   *          input channel
   * @param output
   *          output channel
   * @param memorySize
   *          the number of bytes that can be used for in-memory sorting
   * @return the number of objects sorted
   */
  public int sort(ReadableByteChannel input, WritableByteChannel output,
      int memorySize) {

    ByteBuffer ob = ByteBuffer.allocate(blockSize);
    int inputSize = blockSize * 100;
    ByteBuffer large = ByteBuffer.allocate(inputSize);

    int numFiles = makeRuns(input, memorySize, large, ob, tempDir);
    if (numFiles <= 0)
      return -1;

    // now merge:
    File f = tempDir.toFile();
    String[] fNames = f.list(new DBFFilter());
    try {
      Merge.merge(fNames, codec, large, ob, output, tempDir, complevel > 0);
    }
    catch (Exception e) {
      logger.error("Could not merge files",e);
    }
    try {
      logger.debug("Removing temp files");
      File fileDir = tempDir.toFile();
      File[] files = fileDir.listFiles(new FilenameFilter() {
        public boolean accept(File file, String name) {
          return name.contains(SORT_RUN_FILE);
        }
      });
      if (files != null)
        for (File file : files) {
        logger.debug("delete sort run: {}", file);
          file.delete();
        }
    }
    catch (Exception e) {
      logger.warn("Could not delete sort run files", e);
    }
    return numFiles;

  }

  private int makeRuns(ReadableByteChannel input, int heapSize,
      ByteBuffer large, ByteBuffer ob, Path tmpDir) {
    try {
      DBSContainer <A> hb = new DBSContainer <> (large, input, blockSize, codec,
          heapSize);

      A [] objs = (A[]) new Comparable [1000000];
      int i = 0;
      int numObjs = 0;

      while (true) {
        i++;
        long l = System.currentTimeMillis();
        if (!hb.prepareRun())
          break;
        DBSProducer <A> p = new DBSProducer <> (hb);
        DBSConsumer <A> c = new DBSConsumer <> (hb, objs);
        p.start();
        c.start();
        c.join();

        // done filling a file:
        objs = c.getObjects();
        numObjs += c.getNumObjs();
        long ll = System.currentTimeMillis();
        sortRun(input, objs, c.getNumObjs(), i, tmpDir, ob);
        ll = System.currentTimeMillis() - ll;
        l = System.currentTimeMillis() - l;
        logger.info("Discbased inmemory sort of {} took {} secs", c.getNumObjs(), ll/1000);
        logger.info("Discbased sort run took {} secs", l/1000);
      }
      return numObjs;
    }
    catch (Exception e) {
      System.out.println("trying to consume 2");
      e.printStackTrace();
      return -1;
    }
  }

  private int sortRun(ReadableByteChannel c, A[] objs, int numObjs,
      int i, Path dir, ByteBuffer output) throws Exception {

    output.clear(); // clear output buffer:
    Path fRun = dir.resolve(SORT_RUN_FILE+i);
    try(FileOutputStream fos = new FileOutputStream(fRun.toFile())){
    // Create output channel:
    FileChannel fc = fos.getChannel();
    int size = 0, numBytes = 0;

    // sort offsets:
    Arrays.parallelSort(objs, 0, numObjs);
    //Sorters.quickSort(objs, numObjs);

    for (int j = 0; j < numObjs; j++) {
      if (codec.byteSize(objs[j]) > output.remaining()) {
        output.flip();
        fc.write(output);
        output.clear();
      }
      codec.to(objs[j], output);
      numBytes += codec.byteSize(objs[j]);
    }
    // flush outputbuffer:
    output.flip();
    fc.write(output);
    return numBytes;
    }
    catch(IOException e){
      throw e;
    }
  }
}

class DBSProducer <A> extends Thread {
  private DBSContainer <A> hb;
  private final Logger logger = LoggerFactory.getLogger(DBSProducer.class);
  DBSProducer(DBSContainer<A> hb) {
    this.hb = hb;
  }

  public void run() {
    try {
      while (!hb.producedAll())
        hb.produce();
    }
    catch (Exception e) {
      logger.warn("",e);
    }
  }
}

class DBSConsumer <A> extends Thread {
  private DBSContainer <A> hb;
  private A tmp;
  private A[] objs;
  private int numObjs = 0;
  private final Logger logger = LoggerFactory.getLogger(DBSProducer.class);

  DBSConsumer(DBSContainer<A> hb, A[] objs) {
    this.objs = objs;
    this.hb = hb;
  }

  int getNumObjs() {
    return numObjs;
  }

  public A[] getObjects() {
    return objs;
  }

  public void run() {
    try {
      while (!hb.consumedAll()) {
        tmp = hb.consume();
        if (tmp != null) {
          // just a test:
          if (numObjs == objs.length) {
            objs = Arrays.copyOf(objs, (int)(objs.length * 1.75));
            //objs = (ByteStorableOld[]) ArrayUtils.setSize(objs,
            //    (int) (objs.length * 1.75));
          }
          objs[numObjs++] = tmp;
        }
      }
    }
    catch (Exception e) {
      logger.warn("", e);
    }
  }
}

class DBSContainer <A> {

  private ByteBuffer buffer;
  private ByteBuffer consumerBuffer;
  ReadableByteChannel c;
  private boolean noMore = false, consumedAll = false, endOfStream = false;
  private int slack = -1, totConsumed, totProduced, blockSize, maxRead;
  private BCodec<A> codec;
  private final Logger logger = LoggerFactory.getLogger(DBSContainer.class);

  public DBSContainer(ByteBuffer b, ReadableByteChannel c, int blockSize,
      BCodec<A> codec, int maxRead) {

    this.blockSize = blockSize;
    this.maxRead = maxRead;
    this.codec = codec;
    totConsumed = totProduced = 0;
    this.c = c;
    buffer = b;
    consumerBuffer = b.asReadOnlyBuffer();
    consumerBuffer.limit(buffer.position());
  }

  boolean prepareRun() {
    if (endOfStream)
      return false;
    noMore = false;
    consumedAll = false;
    if (slack > 0) {
      buffer.position(buffer.limit() - slack);
      CodecUtil.copyToBeginning(buffer, slack);
      totProduced = slack;
      buffer.position(slack);
      buffer.limit(slack);
      slack = -1;
    }
    else {
      buffer.clear();
      totProduced = 0;
      buffer.limit(buffer.position());
    }
    totConsumed = 0;
    consumerBuffer.position(0);
    consumerBuffer.limit(buffer.position());
    return true;
  }

  public ByteBuffer getBuffer() {
    return buffer;
  }

  boolean producedAll() {
    return noMore;
  }

  public int getTotalConsumed() {
    return totConsumed;
  }

  public int getTotalProduced() {
    return totProduced;
  }

  boolean consumedAll() {
    return consumedAll;
  }

  synchronized A consume() { // as much as possible
    try {
      if ((noMore && slack >= 0) || consumedAll) {
        notifyAll();
        consumedAll = true;
        return null;
      }
      while (slack != -1) {
        wait();
      }
      A tmp;

      int bSize = CodecUtil.slackOrSize(consumerBuffer, codec);
      if(bSize < 0 && endOfStream) {
        bSize = Math.abs(bSize);
      }
      if (bSize <= 0) {
        slack = Math.abs(bSize);
        notifyAll();
        return null;
      }
      tmp = codec.from(consumerBuffer);
      totConsumed += bSize;
      notifyAll();
      return tmp;
    }
    catch (InterruptedException e) {
    }
    return null;
  }

  synchronized int produce() {
    int read = -1;
    if (noMore) {
      notifyAll();
      return -1;
    }
    try {
      int left = buffer.capacity() - buffer.position();
      int moveAhead = (left < blockSize) ? left : blockSize;
      while (slack == -1 && left == 0) {
        wait();
      }
      if (left > 0) {
        buffer.limit(buffer.position() + moveAhead);
        read = c.read(buffer);
      }
      else if (slack > 0) { // read buffer completely
        consumerBuffer.clear();
        buffer.position(buffer.capacity() - slack);
        CodecUtil.copyToBeginning(buffer, slack);
        left = buffer.capacity() - buffer.position();
        buffer.limit(left < blockSize ? left : buffer.position() + blockSize);
        read = c.read(buffer);
        slack = -1;
      }
      else { // read buffer completely
        consumerBuffer.clear();
        buffer.clear();
        buffer.limit(blockSize);
        read = c.read(buffer);
      }

      if (read == -1) { // end of stream
        endOfStream = true;
        noMore = true;
      }
      else {
        totProduced += read;
        consumerBuffer.limit(buffer.position());
        if (totProduced >= maxRead)
          noMore = true;
        slack = -1;
      }
      notifyAll();
      return read;

    }
    catch (Exception e) {
      logger.warn("cannot produce", e);
    }
    return -1;
  }

  public ByteBuffer getDBSConsumerBuffer() {
    return consumerBuffer;
  }
}

class DBFFilter implements java.io.FilenameFilter {

  public boolean accept(File dir, String name) {
    return name.startsWith(DiscBasedSort.SORT_RUN_FILE);
  }
}
