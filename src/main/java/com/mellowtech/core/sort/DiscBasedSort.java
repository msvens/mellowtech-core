/*
 * Copyright (c) 2013 mellowtech.org.
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 *
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 *
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 */
package com.mellowtech.core.sort;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.util.ArrayUtils;
import com.mellowtech.core.util.Platform;

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
 * @see com.mellowtech.core.sort.EDiscBasedSort
 */
public class DiscBasedSort {
  public static final String SORT_RUN_FILE = "disc_sort_d_run.";
  private static final String SEP = System.getProperties().getProperty(
      "file.separator");

  private static int blockSize = 1024;
  private ByteStorable template;
  private int complevel = 0;
  private String tempDir = null;

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
   * @param template
   *          the type of object to sort
   * @param tempDir
   *          temporary directory for sort runs
   */
  public DiscBasedSort(ByteStorable template, String tempDir) {
    this(template, 0, tempDir);
  }

  /**
   * Create a new DiscBased sorter that will operate on a specific type of
   * objects.
   * 
   * @param template
   *          the type of object to sort
   * @param complevel
   *          the level of GZIP compression for runs (1-9, where 1 is fastest)
   * @param tempDir
   *          temporary directory for sort runs
   */
  public DiscBasedSort(ByteStorable template, int complevel, String tempDir) {
    this.template = template;
    this.complevel = complevel;
    this.tempDir = tempDir;
    try {
      File file = new File(tempDir);
      if (!file.isDirectory())
        throw new Exception("");
    }
    catch (Exception e) {
      CoreLog.L().info("Could not open temp dir.." + tempDir+" using default");
      tempDir = Platform.getTempDir();
    }
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
    try {
      FileChannel fc = (new FileInputStream(fName)).getChannel();
      FileChannel fo = (new FileOutputStream(outputFile)).getChannel();
      int ret = sort(fc, fo, memorySize);
      fc.close();
      fo.close();
      return ret;
    }
    catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "could not sort", e);
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
    File f = new File(tempDir);
    String[] fNames = f.list(new DBFFilter());
    try {
      Merge.merge(fNames, template, large, ob, output, tempDir,
          complevel > 0 ? true : false);
    }
    catch (Exception e) {
      CoreLog.L().log(Level.WARNING, "could not merge files", e);
    }
    try {
      CoreLog.L().finer("SORT:sort():Removing temp files.");
      File fileDir = new File(tempDir);
      File[] files = fileDir.listFiles(new FilenameFilter() {
        public boolean accept(File file, String name) {
          return name.indexOf(SORT_RUN_FILE) >= 0 ? true : false;
        }
      });
      if (files != null)
        for (int i = 0; i < files.length; i++) {
          CoreLog.L().finer("delete sort run " + files[i]);
          files[i].delete();
        }
    }
    catch (Exception e) {
      CoreLog.L().log(Level.WARNING, "could not delete sort runs", e);
    }
    return numFiles;

  }

  private int makeRuns(ReadableByteChannel input, int heapSize,
      ByteBuffer large, ByteBuffer ob, String tmpDir) {
    try {
      DBSContainer hb = new DBSContainer(large, input, blockSize, template,
          heapSize);

      ByteStorable[] objs = new ByteStorable[10000];
      int i = 0;
      int numObjs = 0;

      while (true) {
        i++;
        if (!hb.prepareRun())
          break;
        DBSProducer p = new DBSProducer(hb);
        DBSConsumer c = new DBSConsumer(hb, objs);
        p.start();
        c.start();
        c.join();

        // done filling a file:
        objs = c.getObjects();
        numObjs += c.getNumObjs();
        sortRun(input, objs, c.getNumObjs(), i, tmpDir, ob);
      }
      return numObjs;
    }
    catch (Exception e) {
      CoreLog.L().log(Level.WARNING, "", e);
      return -1;
    }
  }

  private int sortRun(ReadableByteChannel c, ByteStorable[] objs, int numObjs,
      int i, String dir, ByteBuffer output) throws Exception {

    output.clear(); // clear output buffer:

    // Create output channel:
    FileChannel fc = (new FileOutputStream(dir + SEP + SORT_RUN_FILE + i))
        .getChannel();
    int size = 0, numBytes = 0;

    // sort offsets:
    long l = System.currentTimeMillis();
    Sorters.quickSort(objs, numObjs);

    for (int j = 0; j < numObjs; j++) {
      if (objs[j].byteSize() > output.remaining()) {
        output.flip();
        fc.write(output);
        output.clear();
      }
      objs[j].toBytes(output);
      numBytes += objs[j].byteSize();
    }
    // flush outputbuffer:
    output.flip();
    fc.write(output);
    fc.close();
    return numBytes;
  }
}

class DBSProducer extends Thread {
  private DBSContainer hb;

  public DBSProducer(DBSContainer hb) {
    this.hb = hb;
  }

  public void run() {
    try {
      while (!hb.producedAll())
        hb.produce();
    }
    catch (Exception e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
  }
}

class DBSConsumer extends Thread {
  private DBSContainer hb;
  private ByteStorable tmp;
  private ByteStorable[] objs;
  private int numObjs = 0;

  public DBSConsumer(DBSContainer hb, ByteStorable[] objs) {
    this.objs = objs;
    this.hb = hb;
  }

  public int getNumObjs() {
    return numObjs;
  }

  public ByteStorable[] getObjects() {
    return objs;
  }

  public void run() {
    try {
      while (!hb.consumedAll()) {
        tmp = hb.consume();
        if (tmp != null) {
          // just a test:
          if (numObjs == objs.length) {
            objs = (ByteStorable[]) ArrayUtils.setSize(objs,
                (int) (objs.length * 1.75));
          }
          objs[numObjs++] = tmp;
        }
      }
    }
    catch (Exception e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
  }
}

class DBSContainer {

  ByteBuffer buffer;
  ByteBuffer consumerBuffer;
  ReadableByteChannel c;
  boolean noMore = false, consumedAll = false, endOfStream = false;
  int slack = -1, totConsumed, totProduced, blockSize, maxRead;
  ByteStorable template;

  public DBSContainer(ByteBuffer b, ReadableByteChannel c, int blockSize,
      ByteStorable template, int maxRead) {

    this.blockSize = blockSize;
    this.maxRead = maxRead;
    this.template = template;
    totConsumed = totProduced = 0;
    this.c = c;
    buffer = b;
    consumerBuffer = b.asReadOnlyBuffer();
    consumerBuffer.limit(buffer.position());

  }

  public boolean prepareRun() {
    if (endOfStream)
      return false;
    noMore = false;
    consumedAll = false;
    if (slack > 0) {
      buffer.position(buffer.limit() - slack);
      ByteStorable.copyToBeginning(buffer, slack);
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

    /*
     * buffer.limit(buffer.capacity()); if(slack > 0){
     * buffer.position(buffer.capacity() - slack);
     * ByteStorable.copyToBeginning(buffer, slack); totProduced = slack; } else{
     * buffer.clear(); totProduced = 0; } totConsumed = 0;
     * consumerBuffer.limit(buffer.position()); consumerBuffer.position(0);
     */
    return true;
  }

  public ByteBuffer getBuffer() {
    return buffer;
  }

  public boolean producedAll() {
    return noMore;
  }

  public int getTotalConsumed() {
    return totConsumed;
  }

  public int getTotalProduced() {
    return totProduced;
  }

  public boolean consumedAll() {
    return consumedAll;
  }

  public synchronized ByteStorable consume() { // as much as possible
    try {
      if ((noMore && slack >= 0) || consumedAll) {
        notifyAll();
        consumedAll = true;
        return null;
      }
      while (slack != -1) {
        wait();
      }
      ByteStorable tmp;

      int bSize = ByteStorable.slackOrSize(consumerBuffer, template);
      if(bSize < 0 && endOfStream) {
        bSize = Math.abs(bSize);
      }
      if (bSize <= 0) {
        slack = Math.abs(bSize);
        notifyAll();
        return null;
      }
      tmp = template.fromBytes(consumerBuffer);
      totConsumed += bSize;
      notifyAll();
      return tmp;
    }
    catch (InterruptedException e) {
      ;
    }
    return null;
  }

  public synchronized int produce() {
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
        ByteStorable.copyToBeginning(buffer, slack);
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
      CoreLog.L().log(Level.WARNING, "in produce", e);
    }
    return -1;
  }

  public ByteBuffer getDBSConsumerBuffer() {
    return consumerBuffer;
  }
}

class DBFFilter implements java.io.FilenameFilter {

  public boolean accept(File dir, String name) {
    if (name.startsWith(DiscBasedSort.SORT_RUN_FILE))
      return true;
    return false;
  }
}
