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
package org.mellowtech.core.sort;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.CBUtil;

/**
 * Merge a set of sorted files into one large file. This Merge operates on
 * ByteStorable objects.
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class Merge{

  private static int mBlockSize = 4096 * 4;

  private static class Container<A,B extends BComparable<A,B>> implements Comparable<Container<A,B>> {
    B store;
    int node;

    public Container(B bs, int node) {
      store = bs;
      this.node = node;
    }

    public int compareTo(Container<A,B> o) {
      return this.store.compareTo(o.store);
    }

    public String toString() {
      return node + " " + store.toString();
    }
  }

  /**
   * Set the blocksize. How much data to read/write from disc. This number
   * should seldom be no more than 4096.
   * 
   * @param blockSize
   *          the size
   */
  public static void setBlockSize(int blockSize) {
    mBlockSize = blockSize;
  }

  /**
   * Merges a set of files into one large file. Merge will split the input
   * buffer into chuncks of size blockSize, each corresponding to one input
   * file. If the buffer is not large enough to hold a block for each input file
   * the merge will be carried out in several steps (this to guarantee that
   * sufficient data are read during every disc read).
   * 
   * @param fNames
   *          the list of files to merge
   * @param template
   *          the type of objects to merge
   * @param input
   *          the input buffer
   * @param output
   *          the output buffer
   * @param outputChannel
   *          the channel to print the merged data
   * @param dir
   *          the dir where to find the input files
   * @param compressed
   *          true if the set of files are compressed using
   *          java.util.zip.DeflaterOutputStream.
   * @param <A> Wrapped BComparable type
   * @param <B> BComparable type
   * @exception Exception
   *              if an error occurs
   */
  @SuppressWarnings("unchecked")
  public static <A, B extends BComparable<A,B>> void merge(String fNames[], B template,
      ByteBuffer input, ByteBuffer output, WritableByteChannel outputChannel,
      String dir, boolean compressed) throws Exception {

    ReadableByteChannel[] channels = new ReadableByteChannel[fNames.length];
    // FileChannel[] channels = new FileChannel[fNames.length];
    ByteBuffer[] buffers = new ByteBuffer[fNames.length];
    input.clear();
    output.clear();
    Heap heap = new Heap(fNames.length);

    //check
    if(fNames.length * mBlockSize > input.capacity()){
      mBlockSize = input.capacity() / fNames.length;
      CoreLog.L().finer("changing block size to fit to buffer: "+mBlockSize);
    }

    // fill local containers:
    for (int i = 0; i < fNames.length; i++) {
      CoreLog.L().finer("opening "+dir + "/" + fNames[i]+" for merge");
      if (compressed)
        channels[i] = Channels.newChannel(new InflaterInputStream(
            new FileInputStream(dir + "/" + fNames[i]), new Inflater()));
      else
        channels[i] = (new FileInputStream(dir + "/" + fNames[i])).getChannel();
      // slice buffer:
      input.position(i * mBlockSize);
      input.limit((i + 1) * mBlockSize);
      buffers[i] = input.slice();
      buffers[i].clear();
      buffers[i].flip();

      // input buffer:
      input(channels, buffers, i, heap, template);
    }


    // merge files:
    Container <A,B> low;
    while (true) {
      low = (Container<A, B>) heap.delete();
      if (low == null)
        break;
      writeOutput(low.store, outputChannel, output);
      input(channels, buffers, low.node, heap, template);
    }
    // flush remaining:
    output.flip();
    outputChannel.write(output);
  }

  /**
   * Merges a set of files into one large file. This Merge does no conversion
   * from the input bytes to objects. Merge will split the input buffer into
   * chuncks of size blockSize, each corresponding to one input file. If the
   * buffer is not large enough to hold a block for each input file the merge
   * will be carried out in several steps (this to guarantee that sufficient
   * data are read during every disc read).
   * 
   * @param fNames
   *          the list of files to merge
   * @param template
   *          the type of objects to merge
   * @param input
   *          the input buffer
   * @param output
   *          the output buffer
   * @param outputChannel
   *          the channel to print the merged data
   * @param dir
   *          the dir where to find the input files
   * @param compressed
   *          true if the runs are compressed using
   *          java.util.zip.DeflaterOutputStream
   * @param <A> Wrapped BComparable type
   * @param <B> BComparable type
   * @exception Exception
   *              if an error occurs
   */
  public static <A, B extends BComparable<A,B>> void mergeDirect(String fNames[], B template,
      ByteBuffer input, ByteBuffer output, WritableByteChannel outputChannel,
      String dir, boolean compressed) throws Exception {
    // create local containers:
    ReadableByteChannel[] channels = new ReadableByteChannel[fNames.length];
    ByteBuffer[] buffers = new ByteBuffer[fNames.length];
    // int[] offsets = new int[fNames.length];
    input.clear();
    output.clear();
    BufferHeap bufferHeap;
    try {
      bufferHeap = new ByteHeap <> (input.array(), template);
    }
    catch (Exception e) {
      bufferHeap = new ByteBufferHeap <> (input, template);
    }

    // fill local containers:
    input.clear();

    //check
    if(fNames.length * mBlockSize > input.capacity()){
      mBlockSize = input.capacity() / fNames.length;
      CoreLog.L().finer("changing block size to fit to buffer: "+mBlockSize);
    }
    for (int i = 0; i < fNames.length; i++) {

      if (compressed)
        channels[i] = Channels.newChannel(new InflaterInputStream(
            new FileInputStream(dir + "/" + fNames[i]), new Inflater()));
      else
        channels[i] = (new FileInputStream(dir + "/" + fNames[i])).getChannel();
      // slice buffer:
      input.position(i * mBlockSize);
      input.limit((i + 1) * mBlockSize);
      buffers[i] = input.slice();
      buffers[i].clear();
      buffers[i].flip();

      // input buffer:
      input(channels, buffers, i * mBlockSize, bufferHeap, template);

    }

    // merge files:
    int low;
    while (true) {
      low = bufferHeap.delete();
      if (low == -1)
        break;
      writeOutput(low, input, outputChannel, output, template);
      input(channels, buffers, low, bufferHeap, template);
    }
    // flush remaining:
    output.flip();
    outputChannel.write(output);
  }

  private static void writeOutput(int offset, ByteBuffer input,
      WritableByteChannel outChannel, ByteBuffer output, BComparable<?,?> template)
      throws Exception {
    input.position(offset);

    /** CRITICAL Remove */
    /*
     * int saved = input.position();
     * 
     * ByteStorable sortTerm = template.fromBytes(input, true); String str =
     * sortTerm.toString();
     * 
     * input.position(saved);
     */
    /** ******************* */

    int size = template.byteSize(input);
    if (size > output.remaining()) {
      output.flip();
      outChannel.write(output);
      output.clear();
    }
    input.limit(offset + size);
    output.put(input);
    input.limit(input.capacity());
  }

  private static void writeOutput(BComparable<?,?> low,
      WritableByteChannel outChannel, ByteBuffer output) throws Exception {

    if (low.byteSize() > output.remaining()) {
      output.flip();
      outChannel.write(output);
      output.clear();
    }
    low.to(output);
  }

  private static <A, B extends BComparable<A,B>>void input(ReadableByteChannel[] channels,
      ByteBuffer[] buffers, int node, Heap heap, B template)
      throws Exception {
    if (!channels[node].isOpen())
      return;
    int slack = CBUtil.slackOrSize(buffers[node], template);
    if (slack <= 0) {
      CBUtil.copyToBeginning(buffers[node], Math.abs(slack));
      if (channels[node].read(buffers[node]) == -1) {
        CoreLog.L().finer("closing merge channel: " + slack);
        channels[node].close();
        if(Math.abs(slack) <= 0) //need to read the final bytes
          return;
      }
      buffers[node].flip();
      // slack = template.byteSize(buffers[node]);
    }
    heap.insert(new Container<A,B>(template.from(buffers[node]), node));
  }

  private static void input(ReadableByteChannel[] channels,
      ByteBuffer[] buffers, int offset, BufferHeap heap, BComparable<?,?> template) throws Exception {
    //if(!channels[node].isOpen()) return;
    int node = offset / mBlockSize;
    int slack = CBUtil.slackOrSize(buffers[node], template);
    if(!channels[node].isOpen()) return;
    if (slack <= 0) {
      CBUtil.copyToBeginning(buffers[node], Math.abs(slack));
      if (channels[node].read(buffers[node]) == -1) {
        channels[node].close();
        CoreLog.L().finer("closing merge channel "+node+" "+slack);
        if(Math.abs(slack) <= 0) //need to read the final bytes
        return;
      }
      buffers[node].flip();
      slack = template.byteSize(buffers[node]);
    }
    int pos = buffers[node].position();
    heap.insert((node * mBlockSize) + pos);
    numInserted++;
    buffers[node].position(pos + slack);
  }
  public static int numInserted = 0;
}
