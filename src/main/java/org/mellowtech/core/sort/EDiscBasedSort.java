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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.CBUtil;
import org.mellowtech.core.util.Platform;

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
 * @param <A> wrapped BComparable class
 * @param <B> BComparable class
 * @author Martin Svensson
 * @version 1.0
 * @see org.mellowtech.core.bytestorable.BComparable
 */
public class EDiscBasedSort <A, B extends BComparable<A,B>> {
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

  private final B template;
  private final int complevel;
  private final String tempDir;

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
   * @param tempDir
   *          temporary directory for sort runs
   */
  public EDiscBasedSort(Class<B> template, String tempDir) {
    this(template, 0, tempDir);
  }

  /**
   * Create a new DiscBased sorter that will operate on a specific type of
   * objects with a specific ByteComparable object.
   * 
   * @param template
   *          the type of object to sort
   * @param complevel
   *          the level of GZIP compression for runs (1-9, where 1 is fastest)
   *          and 9 is highest compression)
   * @param tempDir
   *          temporary directory for sort runs
   */
  public EDiscBasedSort(Class<B> template, int complevel, String tempDir) {
    try {
      this.template = template.newInstance();
    } catch(Exception e){throw new Error("could not create template instance");}
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

    //if (tempDir == null)
    //tempDir = ".";

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
      Merge.mergeDirect(fNames, template, large, ob, output, tempDir,
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
        CoreLog.L().info("delete sort run " + files[i]);
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
      EDBSContainer <A,B> hb = new EDBSContainer <> (large, input, blockSize, template);
      Integer[] offsets = new Integer[10000];
      int i = 0;
      int numSorts = 0;
      int prod = 0, cons = 0;
      while (true) {
        long l = System.currentTimeMillis();
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
        long ll = System.currentTimeMillis();
        sortRun(hb.getBuffer(), input, offsets, c.getNumOffsets(), i, tempDir,
            ob);
        ll = System.currentTimeMillis() - ll;
        prod += hb.getTotalConsumed();
        cons += hb.getTotalConsumed();
        l = System.currentTimeMillis() - l;
        CoreLog.L().info("ediscbasedsort of "+c.getNumOffsets()+" took: "+ll/1000+" secs");
        CoreLog.L().info("ediscbasedsort run took "+l/1000+" secs");
      }
      return numSorts;
    }
    catch (Exception e) {
      CoreLog.L().log(Level.WARNING, "", e);
      return -1;
    }

  }

  private int sortRun(ByteBuffer bb, ReadableByteChannel c, Integer offsets[],
      int numOffsets, int i, String dir, ByteBuffer output) throws Exception {

    output.clear(); // clear output buffer:

    // Create output channel:
    WritableByteChannel fc;
    CoreLog.L().finer("SORT:sortRun():Sort Run "+i);
    if (complevel > 0)
      fc = Channels.newChannel(new DeflaterOutputStream(new FileOutputStream(
          dir + SEP + SORT_RUN_FILE + i), new Deflater(complevel)));
    else
      fc = (new FileOutputStream(dir + SEP + SORT_RUN_FILE + i)).getChannel();
    int size = 0, numBytes = 0;

    // sort offsets:
    //System.out.println("has array: "+bb.hasArray()+" "+numOffsets);
    if (bb.hasArray())
      Arrays.parallelSort(offsets, 0, numOffsets, new BComparatorArray <A,B>(template, bb.array()));
      //Sorters.quickSort(offsets, bc, bb.array(), numOffsets);
    else
      Arrays.parallelSort(offsets, 0, numOffsets, new BComparator <A,B> (template, bb));
      //Sorters.quickSort(offsets, bc, bb, numOffsets);
    
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
	  private EDBSContainer<?,?> hb;
	  private Object monitor = new Object();
	  public EDBSProducer(EDBSContainer<?,?> hb) {
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
	  private EDBSContainer <?,?> hb;
	  private int offset;
	  private Integer[] offsets;
	  private int numOffsets = 0;
      private Object monitor = new Object();
      
	  public EDBSConsumer(EDBSContainer <?,?> hb, Integer[] offsets) {
	    this.offsets = offsets;
	    this.hb = hb;
	  }

	  public int getNumOffsets() {
	    return numOffsets;
	  }

	  public Integer[] getOffsets() {
	    return offsets;
	  }

	  public void run() {
	    try {
	      while (!hb.consumedAll()) {
	        offset = hb.consume();
	        if (offset != -1) {
	          // just a test:
	          if (numOffsets == offsets.length) {
	            offsets = Arrays.copyOf(offsets, (int) (offsets.length * 1.75));
	            /*offsets = ArrayUtils
	            offsets = ArrayUtils
	                .setSize(offsets, (int) (offsets.length * 1.75));*/
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





class EDBSContainer <A, B extends BComparable<A,B>> {
  public boolean verbose = false;
  ByteBuffer buffer;
  ByteBuffer consumerBuffer;
  ReadableByteChannel c;
  boolean noMore = false, consumedAll = false, endOfStream = false;
  int slack = -1, totConsumed, totProduced, blockSize, read;
  B template;

  public EDBSContainer(ByteBuffer b, ReadableByteChannel c, int blockSize,
      B template) {

    this.blockSize = blockSize;
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
      CBUtil.copyToBeginning(buffer, slack);
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
      bSize = CBUtil.slackOrSize(consumerBuffer, template);
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
