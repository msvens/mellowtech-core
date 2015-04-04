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
package org.mellowtech.core.disc.buffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ShortBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Implements an array of Shorts for the index. (Used as hit maps during
 * probabilistic retrieval.)
 * 
 * The array is either allocated as a ShortBuffer (in memory) or as a mapped
 * file object that is viewed as a Short[]. The file object is stored in the
 * temp files directory.
 * 
 * When the buffer is allocated directly (no hmap) then the backing array can be
 * directly used. [fArray.array()]
 */

public class MappedShortBuffer {

  /* Number of Shorts */
  int fNumElements;

  /* Temporary File */
  File fFile = null;

  /* Direct array or a view of underlying file. */
  public ShortBuffer fArray = null;

  /* Used for hmap mpde. */
  RandomAccessFile fRandomAccessFile = null;
  FileChannel fFileChannel = null;
  MappedByteBuffer fMappedBuffer = null;

  /**
   * Creates a Short array that has the same size as the number of documents in
   * the index. The array is either created in memory (not mapped) or ceated as
   * a read-write hmap to a temporary file.
   * 
   * @param pReader
   *          holds the source index reader.
   * @param pMapToFile
   *          if true then a temp file is created, otherwise the array is
   *          allocated as a normal array.
   */
  public MappedShortBuffer(int numElements, File f) throws IOException {
    fNumElements = numElements;
    fFile = f;
    allocateArray();
  }

  /**
   * Creates a Short array that has the same size as the number of documents in
   * the index. The array is either created in memory (not mapped) or ceated as
   * a read-write hmap to a temporary file.
   * 
   * @param pReader
   *          holds the source index reader.
   * @param pMapToFile
   *          if true then a temp file is created, otherwise the array is
   *          allocated as a normal array.
   */
  public MappedShortBuffer(int numElements) throws IOException {
    fNumElements = numElements;
    allocateArray();
  } // Constructor

  /**
   * Allocates a Short array or maps it to a file.
   */
  protected void allocateArray() throws IOException {
    // Get number of documents in index. recall that we need one
    // ..more entry since we usually access arrays for documents
    // ..starting from 1 (and not from zero c-style).
    // int aNumDocsInIndex = fIndexReader.getNumDocuments();

    // Create array directly in-memory.
    if (fFile == null) {
      fArray = ShortBuffer.allocate(fNumElements + 1);
      return;
    } // if no mapping

    // Make a maped file.
    // fFile = fIndexReader.getTempFile("Shorts");
    fRandomAccessFile = new RandomAccessFile(fFile, "rw");
    fFileChannel = fRandomAccessFile.getChannel();
    fMappedBuffer = fFileChannel.map(FileChannel.MapMode.READ_WRITE, 0,
        2 * (fNumElements + 1));

    fArray = fMappedBuffer.asShortBuffer();

    return;
  } // allocateArray

  /**
   * Destroys the Short buffer, closes the mapped file and deletes the file ((if
   * mapped to a file).
   * 
   * To be called especially in the case when the array is mapped to a temporary
   * file. Of course the IndexReader can also be asked to remove temp files, but
   * this is better. IIt also en- sures tha the open file is closed.
   */
  public void destroy() {
    fArray = null;
    if (fFileChannel != null) {
      try {
        fRandomAccessFile.close();
        fFileChannel.close();
        fMappedBuffer = null;
        fFile.delete(); 
        // delete() on a memory-mapped file does not work on windows, see
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4715154
        // http://bugs.sun.com/bugdatabase/view_bug.do;:YfiG?bug_id=4724038
      }
      catch (IOException ioe) {
        ; // do nothing for now
      }
    }
  } // destroy

  /*****************************************************************************
   * @return the allocated or mapped array.
   */
  public ShortBuffer getShortArray() {
    return fArray;
  } // getShortArray
 
} // MappedShortBuffer

