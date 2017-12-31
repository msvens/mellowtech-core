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

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.mellowtech.core.codec.BCodec;
import org.mellowtech.core.codec.CodecUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Merge a set of sorted files into one large file. This Merge operates on
 * ByteStorable objects.
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class Merge{

  private static int mBlockSize = 4096 * 4;

  private static final Logger logger = LoggerFactory.getLogger(Merge.class);

  private static class Container<A> implements Comparable<Container<A>> {
    A store;
    int node;
    Container(A store, int node){
      this.store = store;
      this.node = node;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compareTo(Container<A> o) {
      return ((Comparable<? super A>)store).compareTo(o.store);
    }

    public String toString(){return node + " " + store.toString();}
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
   * @param codec
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
   * @exception Exception
   *              if an error occurs
   */
  @SuppressWarnings("unchecked")
  public static <A> void merge(String fNames[], BCodec<A> codec,
      ByteBuffer input, ByteBuffer output, WritableByteChannel outputChannel,
      Path dir, boolean compressed) throws Exception {

    ReadableByteChannel[] channels = new ReadableByteChannel[fNames.length];
    // FileChannel[] channels = new FileChannel[fNames.length];
    ByteBuffer[] buffers = new ByteBuffer[fNames.length];
    input.clear();
    output.clear();
    Heap heap = new Heap(fNames.length);

    //check
    if(fNames.length * mBlockSize > input.capacity()){
      mBlockSize = input.capacity() / fNames.length;
      logger.debug("changing block size to fit to buffer: {}", mBlockSize);
    }

    // fill local containers:
    for (int i = 0; i < fNames.length; i++) {
      logger.debug("opening {}/{} for merge", dir, fNames[i]);
      if (compressed)
        channels[i] = Channels.newChannel(new InflaterInputStream(
            Files.newInputStream(dir.resolve(fNames[i])), new Inflater()));
        //new FileInputStream(dir + "/" + fNames[i]), new Inflater()));
      else
        channels[i] = new FileInputStream(dir.resolve(fNames[i]).toFile()).getChannel();
      input.position(i * mBlockSize);
      input.limit((i + 1) * mBlockSize);
      buffers[i] = input.slice();
      buffers[i].clear();
      buffers[i].flip();

      // input buffer:
      input(channels, buffers, i, heap, codec);
    }


    // merge files:
    Container <A> low;
    while (true) {
      low = (Container<A>) heap.delete();
      if (low == null)
        break;
      writeOutput(low.store, codec, outputChannel, output);
      input(channels, buffers, low.node, heap, codec);
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
   * @param codec
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
   * @exception Exception
   *              if an error occurs
   */
  public static <A> void mergeDirect(String fNames[], BCodec<A> codec,
                                     ByteBuffer input, ByteBuffer output, WritableByteChannel outputChannel,
                                     Path dir, boolean compressed) throws Exception {
    // create local containers:
    ReadableByteChannel[] channels = new ReadableByteChannel[fNames.length];
    ByteBuffer[] buffers = new ByteBuffer[fNames.length];
    // int[] offsets = new int[fNames.length];
    input.clear();
    output.clear();
    BufferHeap bufferHeap;
    try {
      bufferHeap = new ByteHeap <> (input.array(), codec);
    }
    catch (Exception e) {
      bufferHeap = new ByteBufferHeap <> (input, codec);
    }

    // fill local containers:
    input.clear();

    //check
    if(fNames.length * mBlockSize > input.capacity()){
      mBlockSize = input.capacity() / fNames.length;
      logger.debug("changing block size to fit to buffer: {}", mBlockSize);
    }
    for (int i = 0; i < fNames.length; i++) {

      if (compressed)
        channels[i] = Channels.newChannel(new InflaterInputStream(
            Files.newInputStream(dir.resolve(fNames[i])), new Inflater()));
            //new FileInputStream(dir + "/" + fNames[i]), new Inflater()));
      else
        channels[i] = new FileInputStream(dir.resolve(fNames[i]).toFile()).getChannel();
        //channels[i] = (Files.newInputStream(dir.resolve(fNames[i]))).getClass();
        //channels[i] = (new FileInputStream(dir + "/" + fNames[i])).getChannel();
      // slice buffer:
      input.position(i * mBlockSize);
      input.limit((i + 1) * mBlockSize);
      buffers[i] = input.slice();
      buffers[i].clear();
      buffers[i].flip();

      // input buffer:
      input(channels, buffers, i * mBlockSize, bufferHeap, codec);

    }

    // merge files:
    int low;
    while (true) {
      low = bufferHeap.delete();
      if (low == -1)
        break;
      writeOutput(low, input, outputChannel, output, codec);
      input(channels, buffers, low, bufferHeap, codec);
    }
    // flush remaining:
    output.flip();
    outputChannel.write(output);
  }

  private static void writeOutput(int offset, ByteBuffer input,
      WritableByteChannel outChannel, ByteBuffer output, BCodec<?> codec)
      throws Exception {
    input.position(offset);

    int size = codec.byteSize(input);
    if (size > output.remaining()) {
      output.flip();
      outChannel.write(output);
      output.clear();
    }
    input.limit(offset + size);
    output.put(input);
    input.limit(input.capacity());
  }

  private static <A> void writeOutput(A low, BCodec<A> codec,
      WritableByteChannel outChannel, ByteBuffer output) throws Exception {

    if (codec.byteSize(low) > output.remaining()) {
      output.flip();
      outChannel.write(output);
      output.clear();
    }
    codec.to(low,output);
  }

  private static <A>void input(ReadableByteChannel[] channels,
      ByteBuffer[] buffers, int node, Heap heap, BCodec<A> codec)
      throws Exception {
    if (!channels[node].isOpen())
      return;
    int slack = CodecUtil.slackOrSize(buffers[node], codec);
    if (slack <= 0) {
      CodecUtil.copyToBeginning(buffers[node], Math.abs(slack));
      if (channels[node].read(buffers[node]) == -1) {
        logger.debug("closing merge channel: {}",slack);
        channels[node].close();
        if(Math.abs(slack) <= 0) //need to read the final bytes
          return;
      }
      buffers[node].flip();
      // slack = template.byteSize(buffers[node]);
    }
    heap.insert(new Container<>(codec.from(buffers[node]), node));
  }

  private static void input(ReadableByteChannel[] channels,
      ByteBuffer[] buffers, int offset, BufferHeap heap, BCodec<?> template) throws Exception {
    //if(!channels[node].isOpen()) return;
    int node = offset / mBlockSize;
    int slack = CodecUtil.slackOrSize(buffers[node], template);
    if(!channels[node].isOpen()) return;
    if (slack <= 0) {
      CodecUtil.copyToBeginning(buffers[node], Math.abs(slack));
      if (channels[node].read(buffers[node]) == -1) {
        channels[node].close();
        logger.debug("closing merge channel {} {}",node,slack);
        if(Math.abs(slack) <= 0) //need to read the final bytes
        return;
      }
      buffers[node].flip();
      slack = template.byteSize(buffers[node]);
    }
    int pos = buffers[node].position();
    heap.insert((node * mBlockSize) + pos);
    //numInserted++;
    buffers[node].position(pos + slack);
  }
  //private static int numInserted = 0;
}
