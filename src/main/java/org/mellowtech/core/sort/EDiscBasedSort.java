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
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.mellowtech.core.codec.BCodec;
import org.mellowtech.core.codec.CodecUtil;
import org.mellowtech.core.util.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author Martin Svensson
 * @version 1.0
 * @see org.mellowtech.core.codec.BCodec
 */
public class EDiscBasedSort <A> {
  static final String SORT_RUN_FILE = "disc_sort_e_run.";
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

  private final BCodec<A> codec;
  private final int complevel;
  private final Path tempDir;

  private final Logger logger = LoggerFactory.getLogger(EDiscBasedSort.class);

  // Flag to control if execution is to wait occasionally to let other processes
  //..get a go for CPU usage.
  private boolean fGoSlower = false;
  
  // Number of milliseconds to sleep.
  private long fSleepMillis = 10;
  
  // Number of milliseconds to allow execution to continue before a sleep (approximately).
  private long fExeMillis = 100;
  
  // Holds the counter for milliseconds used by the slow going execution framework.
  private long fLastTimeStamp = 0;
  
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
   * @param codec
   *          the type of object to sort
   * @param tempDir
   *          temporary directory for sort runs
   */
  public EDiscBasedSort(BCodec<A> codec, Path tempDir) {
    this(codec, 0, tempDir);
  }

  /**
   * Create a new DiscBased sorter that will operate on a specific type of
   * objects with a specific ByteComparable object.
   * 
   * @param codec
   *          the type of object to sort
   * @param complevel
   *          the level of GZIP compression for runs (1-9, where 1 is fastest)
   *          and 9 is highest compression)
   * @param tempDir
   *          temporary directory for sort runs
   */
  public EDiscBasedSort(BCodec<A> codec, int complevel, Path tempDir) {
    try {
      this.codec = codec;
    } catch(Exception e){throw new Error("could not create template instance");}
    this.complevel = complevel;
    Path tDir;
    try {
      if(!Files.isDirectory(tempDir))
        throw new IllegalArgumentException("tempDir is not a directory: "+tempDir.toString());
      tDir = tempDir;
    }
    catch (Exception e) {
      logger.info("could not open temp dir, using default tempdir");
      tDir = Platform.getTempDir();
    }
    this.tempDir = tDir;
  }
  
  /**
   * Makes thread sleep for a while. Uses a caller supplied object that
   * method is allowed to use as a sleeper.
   * 
   * @param pMonitor holds an object to use for wait() call. This is 
   * necessary since threads cannot wait() on objects that don't belong
   * to them.
   */
  private void allowSleep(Object pMonitor) {
	  if(!fGoSlower) return;
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
   * @param input
   *          File to sort
   * @param output
   *          Ouputfile
   * @param memorySize
   *          The amount of memory that can be used for the in-memory
   *          opertaions, must be at least as large as blockSize().
   * @return the number of objects sorted.
   */
  public int sort(Path input, Path output, int memorySize) {
    try(SeekableByteChannel in = Files.newByteChannel(input, StandardOpenOption.READ);
        SeekableByteChannel out = Files.newByteChannel(output, StandardOpenOption.WRITE)){
      int ret = sort(in, out, memorySize);
      return ret;
    }
    catch (IOException e) {
      logger.warn("could not sort", e);
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

    logger.debug("SORT:sort():Making runs.");
    int numFiles = makeRuns(input, large, ob, tempDir);
    if (numFiles <= 0)
      return -1;

    // now merge:
    //File f = new File(tempDir);
    File f = tempDir.toFile();
    String[] fNames = f.list(new FFilter());
    try {
      logger.debug("SORT:sort():Merging runs");
      // Clock time to complete.
      long timeStart = System.currentTimeMillis();
      Merge.mergeDirect(fNames, codec, large, ob, output, tempDir, complevel > 0);

      logger.debug("Merge took {} secs", (System.currentTimeMillis() - timeStart) / 1000);
    }
    catch (Exception e) {
      logger.warn("",e);
    }
    try {
      logger.debug("SORT:sort():Removing temp files.");
      //File fileDir = new File(tempDir);
      File fileDir = tempDir.toFile();
      File[] files = fileDir.listFiles(new FilenameFilter() {
        public boolean accept(File file, String name) {
          return name.contains(SORT_RUN_FILE);
        }
      });
      for (File file : files) {
        logger.debug("delete sort run " + file);
        file.delete();
      }
    }
    catch (Exception e) {
      logger.warn("could not delete sort file", e);
    }
    return numFiles;
  }

  private int makeRuns(ReadableByteChannel input, ByteBuffer large,
      ByteBuffer ob, Path tempDir) {
    try {
      EDBSContainer <A> hb = new EDBSContainer <> (large, input, blockSize, codec);
      Integer[] offsets = new Integer[10000];
      int i = 0;
      int numSorts = 0;
      //int prod = 0, cons = 0;
      while (true) {
        long l = System.currentTimeMillis();
        i++;
        if (!hb.prepareRun()) {
          break;
        }
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
        //prod += hb.getTotalConsumed();
        //cons += hb.getTotalConsumed();
        l = System.currentTimeMillis() - l;
        logger.info("ediscbasedsort of {} took {} secs",c.getNumOffsets(), ll/1000);
        logger.info("ediscbasedsort run took {} secs",+l/1000);
      }
      return numSorts;
    }
    catch (Exception e) {
      logger.warn("", e);
      return -1;
    }

  }

  private int sortRun(ByteBuffer bb, ReadableByteChannel c, Integer offsets[],
      int numOffsets, int i, Path dir, ByteBuffer output) throws Exception {

    output.clear(); // clear output buffer:

    // Create output channel:
    WritableByteChannel fc;
    logger.debug("SORT:sortRun():Sort Run "+i);
    Path fRun = dir.resolve(SORT_RUN_FILE+i);
    if (complevel > 0)
      fc = Channels.newChannel(new DeflaterOutputStream(new FileOutputStream(fRun.toFile()), new Deflater(complevel)));
    else
      fc = new FileOutputStream(fRun.toFile()).getChannel();
    int size = 0, numBytes = 0;

    // sort offsets:
    if (bb.hasArray())
      Arrays.parallelSort(offsets, 0, numOffsets, new BComparatorArray <A>(codec, bb.array()));
    else
      Arrays.parallelSort(offsets, 0, numOffsets, new BComparator <A> (codec, bb));

    for (int j = 0; j < numOffsets; j++) {
      bb.limit(bb.capacity());
      bb.position(offsets[j]);
      size = codec.byteSize(bb);

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
        logger.warn("offset j = " + j, boe);
      }
      numBytes += size;
    }
    // flush outputbuffer:
    output.flip();
    fc.write(output);
    fc.close();
    return numBytes;
  }

 
  private class EDBSProducer extends Thread {
	  private EDBSContainer<?> hb;
	  private Object monitor = new Object();

	  EDBSProducer(EDBSContainer<?> hb) {
	    this.hb = hb;
	  }

	  public void run() {
	    try {
	      while (!hb.producedAll())
	        hb.produce();
	        allowSleep(monitor);
	    }
	    catch (Exception e) {
	     logger.warn("", e);
	    }
	  }
	} // EDBSProducer
  
  private class EDBSConsumer extends Thread {
	  private EDBSContainer <?> hb;
	  private int offset;
	  private Integer[] offsets;
	  private int numOffsets = 0;
      private Object monitor = new Object();
      
	  EDBSConsumer(EDBSContainer<?> hb, Integer[] offsets) {
	    this.offsets = offsets;
	    this.hb = hb;
	  }

	  int getNumOffsets() {
	    return numOffsets;
	  }

	  Integer[] getOffsets() {
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
	      logger.warn("", e);
	    }
      logger.debug("SORT:consumer.run(): consumed all: {} {}", hb.consumedAll(), numOffsets);
	  }
	} // EDBSConsumer
  
  
  
} // EDiscBasedSort





class EDBSContainer <A> {
  public boolean verbose = false;
  private ByteBuffer buffer;
  private ByteBuffer consumerBuffer;
  ReadableByteChannel c;
  private final Logger logger = LoggerFactory.getLogger(EDBSContainer.class);

  private boolean noMore = false, consumedAll = false, endOfStream = false;
  private int slack = -1, totConsumed, totProduced, blockSize, read;
  private BCodec<A> codec;

  EDBSContainer(ByteBuffer b, ReadableByteChannel c, int blockSize,
                BCodec<A> codec) {

    this.blockSize = blockSize;
    this.codec = codec;
    totConsumed = totProduced = 0;
    this.c = c;
    buffer = b;
    consumerBuffer = b.asReadOnlyBuffer();
    consumerBuffer.limit(buffer.position());
    logger.debug("SORT:container():consumer buffer limit: {}",consumerBuffer.limit());
  }

  boolean prepareRun() {
    if (endOfStream)
      return false;
    noMore = false;
    consumedAll = false;
    buffer.limit(buffer.capacity());
    if (slack > 0) {
      buffer.position(buffer.capacity() - slack);
      CodecUtil.copyToBeginning(buffer, slack);
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

  public ByteBuffer getEDBSConsumerBuffer() {
    return consumerBuffer;
  }

  synchronized int consume() { // as much as possible
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
      bSize = CodecUtil.slackOrSize(consumerBuffer, codec);
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

  synchronized int produce() {
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
      logger.warn("", e);
    }
    return -1;
  }
}

class FFilter implements java.io.FilenameFilter {

  public boolean accept(File dir, String name) {
    return name.startsWith(EDiscBasedSort.SORT_RUN_FILE);
  }
}
