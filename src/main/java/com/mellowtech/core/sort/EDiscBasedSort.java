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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteComparable;
import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.util.ArrayUtils;
import com.mellowtech.core.util.Platform;

/**
 * DiscBasedSort sorts large amounts of data by combining in-memory sorting with
 * disc-based merging. It uses quicksort for the in-memory sorting step. This
 * sort also uses overlapping IO-opertations to maximize speed (i.e. the
 * creation of the array of objects to sort works in parallel with reading input
 * data).
 * <p>
 * The Sorter operates on ByteStorable objects that can be compared on a byte
 * level.
 * <p>
 * The EDiscBased sort is optimal in the sense that it only operates on one
 * large buffer of bytes all through the sort process by making all comparisons
 * on a byte level (i.e. by only sorting offset in the buffer of bytes). This
 * has two main advantages:
 * <p>
 * 1. Speed - actual objects are never created in the sort.
 * </p>
 * <p>
 * 2. Memory - we have full control over memory utilization.
 * </p>
 * <p>
 * If the ByteStorable objects that are to be sorted can be compared at a byte
 * level this sort is highly preferred over ordinary DiscBasedSort
 * 
 * @author Martin Svensson
 * @version 1.0
 * @see com.mellowtech.core.bytestorable.ByteComparable
 */
public class EDiscBasedSort {
  public static final String SORT_RUN_FILE = "disc_sort_e_run.";
  private static final String SEP = System.getProperties().getProperty(
      "file.separator");

  /** If set to true some messages will be printed out for informative purposes. */

  // This is the size of "blocks" the output ByteBuffer's capacity. Note that
  // this value
  // ..may change if a ByteStorable object is encountered that don't fit into
  // the buffer
  // ..(and an exception is thrown).
  // ..In that case the blockSize is enlarged to the next 1K boundary larger
  // than before.
  // ..This value is then used when merge() is called so that also the merge
  // will be able
  // ..to handle the large objects.
  private int blockSize = 4096 * 4; // JC 040620, must have a dynamic thing.

  private ByteStorable template;
  private ByteComparable bc;
  private int complevel = 0;
  private String tempDir = null;

  // Flag to control if execution is to wait occasionally to let other processes
  //..get a go for CPU usage.
  protected boolean fGoSlower = false;
  
  // Number of milliseconds to sleep.
  protected long fSleepMillis = 10;
  
  // Number of milliseconds to allow execution to continue before a sleep (approximately).
  protected long fExeMillis = 100;
  
  // Holds the counter for milliseconds used by the slow going execution framework.
  protected long fLastTimeStamp = 0;
  
  /**
   * Set the block size. How much data to read/write from disc. This number
   * should seldom be no more than 4096.
   * 
   * @param size
   *          block size. (a multiple of 1024)
   */
  public void setBlockSize(int size) {
    blockSize = size;
  }

  /**
   * Get the block size. How much data to read/write from disc. This number
   * should seldom be no more than 4096.
   * 
   * @return the block size, in bytes (a multiple of 1024)
   */
  public int getBlockSize() {
    return blockSize;
  }

  /**
   * Create a new DiscBased sorter that will operate on a specific type of
   * objects with a specific ByteComparable object.
   * 
   * @param template
   *          the type of object to sort
   * @param bc
   *          the object that lets the sort compare the ByteStorable objects at
   *          a byte level.
   * @param tempDir
   *          temporary directory for sort runs
   */
  public EDiscBasedSort(ByteStorable template, ByteComparable bc, String tempDir) {
    this(template, bc, 0, tempDir);
  }

  /**
   * Create a new DiscBased sorter that will operate on a specific type of
   * objects with a specific ByteComparable object.
   * 
   * @param template
   *          the type of object to sort
   * @param bc
   *          the object that lets the sort compare the ByteStorable objects at
   *          a byte level.
   * @param complevel
   *          the level of GZIP compression for runs (1-9, where 1 is fastest)
   *          and 9 is highest compression)
   * @param tempDir
   *          temporary directory for sort runs
   */
  public EDiscBasedSort(ByteStorable template, ByteComparable bc,
      int complevel, String tempDir) {
    this.bc = bc;
    this.template = template;
    this.complevel = complevel;
    this.tempDir = tempDir;
    try {
      File file = new File(tempDir);
      if (!file.isDirectory())
        throw new Exception("");
    }
    catch (Exception e) {
      CoreLog.L().info("could not open temp dir, using default tempdir");
      tempDir = Platform.getTempDir();
    }
  }
  
  /**
   * Makes thread sleep for a while. Uses a caller supplied object that
   * method is allowed to use as a sleeper.
   * 
   * @param pMonitor holds an object to use for wait() call. This is 
   * necessary since threads cannot wait() on objects that don't belong
   * to them.
   */
  protected void allowSleep(Object pMonitor) {
	  if(fGoSlower == false)
		  return;
	  long currentTime = System.currentTimeMillis();
	  if(currentTime - fLastTimeStamp > fExeMillis) {
		  try {
			pMonitor.wait(fSleepMillis);
		  } catch (InterruptedException e) {
			  // Nothing here
		  } // catch
		  fLastTimeStamp = System.currentTimeMillis();
	  } // if
  } // allowSleep
  
  /**
   * If this method is called with true in parameter, then execution will
   * be halted for a number of milliseconds once in a while.
   * 
   * @param pGoSlower set to true of slower execution is wanted.
   * @param pSleepMillis holds the number of milliseconds to sleep.
   * @param pExeMillis holds the approximate number of milliseconds
   * that execution is allowed before next sleep.
   */
  public void setGoSlower(boolean pGoSlower, long pSleepMillis, long pExeMillis) {
	  fGoSlower = pGoSlower;
	  fSleepMillis = pSleepMillis;
	  fExeMillis = pExeMillis;
	  if(fGoSlower)
		  fLastTimeStamp = System.currentTimeMillis();
  } // setGoSlower
  

  /**
   * Sorts an inputfile and prints it to a designated outputfile. If these are
   * the same the inputfile will be overwritten.
   * 
   * @param fName
   *          File to sort
   * @param outputFile
   *          Ouputfile
   * @param memorySize
   *          The amount of memory that can be used for the in-memory
   *          opertaions, must be at least as large as blockSize().
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
   * Sorts a input stream and print the result to a designated output stream. If
   * these are the same the input channel will be overwritten. This method
   * simply creates appropriate channesl for the input and output and calls sort
   * on Channels.
   * 
   * @param input
   *          input stream
   * @param output
   *          output stream
   * @param memorySize
   *          number of bytes used for in-memory sorting, must be at least as
   *          large as blockSize().
   * @return number of objects sorted
   * @see #sort(ReadableByteChannel, WritableByteChannel, int)
   */
  public int sort(InputStream input, OutputStream output, int memorySize) {
    return sort(Channels.newChannel(input), Channels.newChannel(output),
        memorySize);
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
   *          the number of bytes that can be used for in-memory sorting, must
   *          be at least as large as blockSize().
   * @return the number of objects sorted
   */
  public int sort(ReadableByteChannel input, WritableByteChannel output,
      int memorySize) {

    if (memorySize < blockSize)
      memorySize = blockSize;
    ByteBuffer ob = ByteBuffer.allocate(blockSize);
    ByteBuffer large = ByteBuffer.allocate(memorySize);

    if (tempDir == null)
      tempDir = ".";

    CoreLog.L().finer("SORT:sort():Making runs.");
    int numFiles = makeRuns(input, large, ob, tempDir);

    if (numFiles <= 0)
      return -1;

    // now merge:
    File f = new File(tempDir);
    String[] fNames = f.list(new FFilter());
    try {
      CoreLog.L().finer("SORT:sort():Merging runs");
      // Clock time to complete.
      long timeStart = System.currentTimeMillis();
      Merge.merge(fNames, template, large, ob, output, bc, tempDir,
          complevel > 0 ? true : false);

      CoreLog.L().finer("Merge took "
              + (System.currentTimeMillis() - timeStart) / 1000 + " secs");
    }
    catch (Exception e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
    try {
      CoreLog.L().finer("SORT:sort():Removing temp files.");
      File fileDir = new File(tempDir);
      File[] files = fileDir.listFiles(new FilenameFilter() {
        public boolean accept(File file, String name) {
          return name.indexOf(SORT_RUN_FILE) >= 0 ? true : false;
        }
      });
      for (int i = 0; i < files.length; i++) {
        CoreLog.L().finer("delete sort run " + files[i]);
        files[i].delete();
      }
    }
    catch (Exception e) {
      CoreLog.L().log(Level.WARNING, "could not delete sort file", e);
    }
    return numFiles;
  }

  private int makeRuns(ReadableByteChannel input, ByteBuffer large,
      ByteBuffer ob, String tempDir) {
    try {
      EDBSContainer hb = new EDBSContainer(large, input, blockSize, template,
          bc);
      int[] offsets = new int[10000];
      int i = 0;
      int numSorts = 0;
      int prod = 0, cons = 0;
      while (true) {
        i++;
        if (!hb.prepareRun())
          break;
        EDBSProducer p = new EDBSProducer(hb);

        EDBSConsumer c = new EDBSConsumer(hb, offsets);
        p.start();
        c.start();
        c.join();

        // done filling a file:
        offsets = c.getOffsets();
        numSorts += c.getNumOffsets();
        sortRun(hb.getBuffer(), input, offsets, c.getNumOffsets(), i, tempDir,
            ob);
        prod += hb.getTotalConsumed();
        cons += hb.getTotalConsumed();
      }
      return numSorts;
    }
    catch (Exception e) {
      CoreLog.L().log(Level.WARNING, "", e);
      return -1;
    }

  }

  private int sortRun(ByteBuffer bb, ReadableByteChannel c, int[] offsets,
      int numOffsets, int i, String dir, ByteBuffer output) throws Exception {

    output.clear(); // clear output buffer:

    // Create output channel:
    WritableByteChannel fc;
    if (complevel > 0)
      fc = Channels.newChannel(new DeflaterOutputStream(new FileOutputStream(
          dir + SEP + SORT_RUN_FILE + i), new Deflater(complevel)));
    else
      fc = (new FileOutputStream(dir + SEP + SORT_RUN_FILE + i)).getChannel();
    int size = 0, numBytes = 0;

    // sort offsets:
    if (bb.hasArray())
      Sorters.quickSort(offsets, bc, bb.array(), numOffsets);
    else
      Sorters.quickSort(offsets, bc, bb, numOffsets);

    //Just do a test:
    Set<Integer> testSet = new HashSet();

    for (int j = 0; j < numOffsets; j++) {
      bb.limit(bb.capacity());
      bb.position(offsets[j]);
      size = template.byteSize(bb);

      if (size > output.remaining()) {
        output.flip();
        fc.write(output);
        output.clear();
      }
      bb.limit(offsets[j] + size);
      try {
        output.put(bb);
      }
      catch (BufferOverflowException boe) {
        CoreLog.L().log(Level.WARNING, "offset j = " + j, boe);
      }
      numBytes += size;
    }
    // flush outputbuffer:
    output.flip();
    fc.write(output);
    fc.close();
    return numBytes;
  }

 
  class EDBSProducer extends Thread {
	  private EDBSContainer hb;
	  private Object monitor = new Object();
	  public EDBSProducer(EDBSContainer hb) {
	    this.hb = hb;
	  }

	  public void run() {
	    try {
	      while (!hb.producedAll())
	        hb.produce();
	        allowSleep(monitor);
	    }
	    catch (Exception e) {
	     CoreLog.L().log(Level.WARNING, "", e);
	    }
	  }
	} // EDBSProducer
  
  class EDBSConsumer extends Thread {
	  private EDBSContainer hb;
	  private int offset;
	  private int[] offsets;
	  private int numOffsets = 0;
      private Object monitor = new Object();
      
	  public EDBSConsumer(EDBSContainer hb, int[] offsets) {
	    this.offsets = offsets;
	    this.hb = hb;
	  }

	  public int getNumOffsets() {
	    return numOffsets;
	  }

	  public int[] getOffsets() {
	    return offsets;
	  }

	  public void run() {
	    try {
	      while (!hb.consumedAll()) {
	        offset = hb.consume();
	        if (offset != -1) {
	          // just a test:
	          if (numOffsets == offsets.length) {
	            offsets = ArrayUtils
	                .setSize(offsets, (int) (offsets.length * 1.75));
	          }
	          offsets[numOffsets++] = offset;
	        } // if offset != -1
	        allowSleep(monitor);
	      } // while not all consumed
	    }
	    catch (Exception e) {
	      CoreLog.L().log(Level.WARNING, "", e);
	    }
      CoreLog.L().finer("SORT:consumer.run(): consumed all: "
              + hb.consumedAll() + " " + numOffsets);
	  }
	} // EDBSConsumer
  
  
  
} // EDiscBasedSort





class EDBSContainer {
  public boolean verbose = false;
  ByteBuffer buffer;
  ByteBuffer consumerBuffer;
  ByteComparable bc;
  ReadableByteChannel c;
  boolean noMore = false, consumedAll = false, endOfStream = false;
  int slack = -1, totConsumed, totProduced, blockSize, read;
  ByteStorable template;

  public EDBSContainer(ByteBuffer b, ReadableByteChannel c, int blockSize,
      ByteStorable template, ByteComparable bc) {

    this.blockSize = blockSize;
    this.bc = bc;
    this.template = template;
    totConsumed = totProduced = 0;
    this.c = c;
    buffer = b;
    consumerBuffer = b.asReadOnlyBuffer();
    consumerBuffer.limit(buffer.position());
    CoreLog.L().finer("SORT:container():consumer buffer limit: "
              + consumerBuffer.limit());
  }

  public boolean prepareRun() {
    if (endOfStream)
      return false;
    noMore = false;
    consumedAll = false;
    buffer.limit(buffer.capacity());
    if (slack > 0) {
      buffer.position(buffer.capacity() - slack);
      ByteStorable.copyToBeginning(buffer, slack);
      totProduced = slack;
    }
    else {
      buffer.clear();
      totProduced = 0;
    }
    totConsumed = 0;
    consumerBuffer.limit(buffer.position());
    consumerBuffer.position(0);
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

  public ByteBuffer getEDBSConsumerBuffer() {
    return consumerBuffer;
  }

  public synchronized int consume() { // as much as possible
    try {

      if ((noMore && slack >= 0) || consumedAll){
        notifyAll();
        consumedAll = true;
        return -1;
      }

      if (slack != -1) {
        wait();
      }

      consumerBuffer.position(totConsumed);
      consumerBuffer.mark();
      int bSize;
      bSize = ByteStorable.slackOrSize(consumerBuffer, template);
      if(bSize < 0 && endOfStream) {
      bSize = Math.abs(bSize);
      }
      consumerBuffer.reset();
      if (bSize <= 0) {
        slack = Math.abs(bSize);
        notifyAll();
        return -1;
      }
      totConsumed += bSize;
      notifyAll();
      return totConsumed - bSize;
    }
    catch (InterruptedException e) {
      ;
    }
    return -1;
  }

  public synchronized int produce() {
    read = -1;
    // slack = -1;
    if (noMore) {
      notifyAll();
      return -1;
    }
    try {
      int left = buffer.capacity() - buffer.position();
      int moveAhead = (left < blockSize) ? left : blockSize;
      buffer.limit(buffer.position() + moveAhead);
      read = c.read(buffer);
      if (read == -1) { // end of stream
        noMore = true;
        endOfStream = true;
        slack = -1;
      }
      else {
        totProduced += read;
        consumerBuffer.limit(buffer.position());
        if (totProduced == buffer.capacity())
          noMore = true;
        slack = -1;
      }
      notifyAll();
      return read;
    }
    catch (Exception e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return -1;
  }
}

class FFilter implements java.io.FilenameFilter {

  public boolean accept(File dir, String name) {
    if (name.startsWith(EDiscBasedSort.SORT_RUN_FILE))
      return true;
    return false;
  }
}
