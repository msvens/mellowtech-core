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

package org.mellowtech.core.disc.blockfile;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.ByteStorable;
import org.mellowtech.core.disc.SpanningBlockFile;

/**
 * A DynamicFile can contain both large and small objects, this is realized
 * through the use of two files: ByteStorableBlockFile and a SpanningBlockFile.
 * Objects larger than max small bytesize is inserted in the SpanningBlockFile.
 * The record numbers returned from insert() are positive if the object was
 * inserted in the "small" file, and negative if inserted into the "large" file.
 * <br>
 * Note that you should NOT mix insert(id, object) and insert(object) since this
 * is not supported and may generate errors.
 * 
 * @author rickard.coster@asimus.se
 */
public class DynamicFile <E> {

  static final int DEFAULT_SMALL_OBJECT_MAX_BYTES   = 8192;
  static final int DEFAULT_SMALL_OBJECT_BLOCK_SIZE  = 1024*64;
  static final int DEFAULT_LARGE_OBJECTS_BLOCK_SIZE = 1024;

  static final String INFO_EXT = ".dyn.info";
  static final String SMALL_EXT = ".small";
  static final String LARGE_EXT = ".large";

  static String GET_INFO_FILE_NAME(String fileName) {
    return fileName + INFO_EXT;
  }

  static String GET_SMALL_FILE_NAME(String fileName) {
    return fileName + SMALL_EXT;
  }

  static String GET_LARGE_FILE_NAME(String fileName) {
    return fileName + LARGE_EXT;
  }

  SpanningBlockFile largeObjectFile;
  ByteStorableBlockFile <E> smallObjectFile;
  String fileName;
  DynamicFileInfo fileInfo = new DynamicFileInfo();
  ByteStorable <E> template;

  static final int CACHE_SIZE_EXISTING_SMALL_FILE = 2; // cache 2 blocks if
                                                        // file already exists
  static final int CACHE_SIZE_NEW_SMALL_FILE = 2; // cache 2 blocks if file is
                                                  // created

  public DynamicFile(String fileName, ByteStorable <E> template) {
    this(fileName, DEFAULT_SMALL_OBJECT_MAX_BYTES,
        DEFAULT_SMALL_OBJECT_BLOCK_SIZE, DEFAULT_LARGE_OBJECTS_BLOCK_SIZE,
        template);
  }

  public DynamicFile(String fileName, int SMALL_OBJECT_MAX_BYTES,
      int SMALL_OBJECT_BLOCK_SIZE, int LARGE_OBJECT_BLOCK_SIZE,
      ByteStorable <E> template) {

    this.fileName = fileName;
    this.template = template;
    File f = new File(GET_INFO_FILE_NAME(fileName));
    if (!f.exists()) {
      fileInfo.get().smallObjectMaxBytes = SMALL_OBJECT_MAX_BYTES;
      fileInfo.get().smallObjectBlockSize = SMALL_OBJECT_BLOCK_SIZE;
      fileInfo.get().largeObjectBlockSize = LARGE_OBJECT_BLOCK_SIZE;
      try {
        DynamicFileInfo.write(f.getPath(), fileInfo);
      }
      catch (IOException e) {
        CoreLog.L().log(Level.WARNING, "", e);
      }
    }
    else {
      try {
        fileInfo = DynamicFileInfo.read(f.getPath());
      }
      catch (IOException e) {
       CoreLog.L().log(Level.WARNING, "", e);
      }
    }

    boolean newLargeFile = false;
    boolean errorLargeFile = false;
    String largeFileName = GET_LARGE_FILE_NAME(fileName);
    try {
      largeObjectFile = new SpanningBlockFile(largeFileName);
    }
    catch (IOException e) {
      newLargeFile = true;
    }

    if (newLargeFile) {
      try {
        largeObjectFile = new SpanningBlockFile(LARGE_OBJECT_BLOCK_SIZE,
            largeFileName);
        // circumvent spanning block file's record number starting from 0
        // by occupying that record number.
        largeObjectFile.insert(new byte[] { 1, 2, 3, 4, 5, 6 });
      }
      catch (IOException e) {
        CoreLog.L().log(Level.WARNING, "", e);
        errorLargeFile = true;
      }
    }

    boolean newSmallFile = false;
    boolean errorSmallFile = false;
    String smallFileName = GET_SMALL_FILE_NAME(fileName);
    try {
      smallObjectFile = new ByteStorableBlockFile(smallFileName, template,
          CACHE_SIZE_EXISTING_SMALL_FILE);
    }
    catch (IOException e) {
      newSmallFile = true;
    }

    if (newSmallFile) {
      try {
        smallObjectFile = new ByteStorableBlockFile(smallFileName,
            SMALL_OBJECT_BLOCK_SIZE, template, CACHE_SIZE_NEW_SMALL_FILE);
      }
      catch (IOException e) {
        errorSmallFile = false;
      }
    }
  }

  public synchronized ByteStorable <E> get(DynamicFilePointer ptr)
      throws IOException {
    if (ptr.isSpanningBlockFile()) {
      byte[] buffer = new byte[fileInfo.get().largeObjectBlockSize];
      int recno = ptr.getRecno();
      int length = largeObjectFile.get(recno, buffer);
      if (length < 0) {
        buffer = new byte[-length + 1];
        length = largeObjectFile.get(recno, buffer);
      }
      return template.fromBytes(buffer, 0, true);
    }
    else {
      return smallObjectFile.read(ptr.getBlockno(), ptr.getId());
    }
  }

  /**
   * Insert new object in DynamicFile, returning a handle to the object.
   * This method will generate a id for the object if the object can fit
   * in the small file, making the DynamicFilePtr occupy 8 bytes. 
   * 
   * <pre>
   * void insert(ByteStorable object) {
   *    DynamicFilePtr ptr = dynamicFile.insert(object);
   *    ptrFile.insert(ptr);
   * }
   * 
   * ByteStorable get(DynamicFilePtr ptr) {
   *    return dynamicFile.get(ptr);
   * }
   * </pre>
   *
   * 
   * If you already have a id associated with the object, you should use the 
   * method 
   * <pre>
   * insert(int id, ByteStorable object)
   * </pre>
   * 
   * Note that you should NOT mix insert(id, object) and insert(object) since this
   * is not supported and may generate errors.
   *
   * @param object the object
   * @return a handle to the object
   * @throws IOException if an error occurs
   */
  public synchronized DynamicFilePointer insert(ByteStorable object)
      throws IOException {
    int byteSize = object.byteSize();
    if (byteSize > fileInfo.get().smallObjectMaxBytes) {
      byte[] buffer = new byte[byteSize];
      object.toBytes(buffer, 0);
      int recno = largeObjectFile.insert(buffer);
      DynamicFilePointer ptr = new DynamicFilePointer(recno);
      return ptr;
    }
    else {
      int id = fileInfo.get().freeSmallObjectIds.nextSetBit(0);
      if (id < 0) {
        id = fileInfo.get().highestId + 1;
        fileInfo.get().highestId = id;
      }
      int blockno = smallObjectFile.write(id, object);
      DynamicFilePointer ptr = new DynamicFilePointer(blockno, id);
      return ptr;
    }
  }
  /** 
   * Insert new object in DynamicFile, returning a handle to the object.
   * This method will use the supplied id for the object if the object can fit
   * in the small file, and the returned DynamicFilePtr will occupy 8 bytes but
   * you should separate the blockno (4 bytes) and the id that you
   * already have (4 bytes) to save space. 
   *
   * The intended use is thus the following
   *
   * <pre>
   * void insert(int id, ByteStorable object) {
   *    DynamicFilePtr ptr = dynamicFile.insert(id, object);
   *    int blockno = ptr.getBlockNo();
   *    blocknoFile.insert(id, blockno);
   * }
   * 
   * ByteStorable get(int id) {
   *    int blockno = blocknoFile.get(id);
   *    DynamicFilePtr ptr = new DynamicFilePtr(id, blockno);
   *    return dynamicFile.get(ptr);
   * }
   * </pre>
   * 
   * If you do not have a id associated with the object, you should use the 
   * method 
   * <pre>insert(ByteStorable object).</pre>
   * which will generate a id.
   * Note that you should NOT mix insert(id, object) and insert(object) since this
   * is not supported and may generate errors.
   * 
   * @param id the object id
   * @param object the object
   * @return a handle to the object
   * @throws IOException if an error occurs
   */
  public synchronized DynamicFilePointer insert(int id, ByteStorable object)
    throws IOException {
    int byteSize = object.byteSize();
    if (byteSize > fileInfo.get().smallObjectMaxBytes) {
      byte[] buffer = new byte[byteSize];
      object.toBytes(buffer, 0);
      int recno = largeObjectFile.insert(buffer);
      DynamicFilePointer ptr = new DynamicFilePointer(recno);
      return ptr;
    }
    else {
      int blockno = smallObjectFile.write(id, object);
      DynamicFilePointer ptr = new DynamicFilePointer(blockno, id);
      return ptr;
    }
  }

  public synchronized DynamicFilePointer update(DynamicFilePointer ptr,
      ByteStorable object) throws IOException {
    int byteSize = object.byteSize();
    if (ptr.isSpanningBlockFile()) {
      // was in large file
      if (byteSize > fileInfo.get().smallObjectMaxBytes) {
        // was in large file and will remain there, simply update
        byte[] buffer = new byte[byteSize];
        object.toBytes(buffer, 0);
        largeObjectFile.update(ptr.getRecno(), buffer, 0);
        return ptr;
      }
      else {
        // was in large file and will move to small file
        // first remove from large file
        largeObjectFile.delete(ptr.getRecno());
        // insert in small file
        int id = fileInfo.get().freeSmallObjectIds.nextSetBit(0);
        if (id < 0) {
          id = fileInfo.get().highestId + 1;
          fileInfo.get().highestId = id;
        }
        int blockno = smallObjectFile.write(id, object);
        return new DynamicFilePointer(blockno, id);
      }
    }
    else {
      // was in small file
      if (byteSize > fileInfo.get().smallObjectMaxBytes) {
        // was in small file and will move to large file

        // delete from small file
        ByteStorable <?> deletedObject = smallObjectFile.delete(ptr.getBlockno(),
            ptr.getId());
        // save id for later user
        fileInfo.get().freeSmallObjectIds.set(ptr.getId());
        // insert in large file
        byte[] buffer = new byte[byteSize];
        object.toBytes(buffer, 0);
        int recno = largeObjectFile.insert(buffer, 0);
        return new DynamicFilePointer(recno);
      }
      else {
        // was in small file and will remain there, simply update
        int blockno = smallObjectFile.update(ptr.getBlockno(), ptr.getId(),
            object);
        return new DynamicFilePointer(blockno, ptr.getId());
      }
    }
  }

  public synchronized void delete(DynamicFilePointer ptr) throws IOException {
    if (ptr.isSpanningBlockFile()) {
      int recno = ptr.getRecno();
      largeObjectFile.delete(recno);
    }
    else {
      smallObjectFile.delete(ptr.getBlockno(), ptr.getId());
    }
  }

  public synchronized void close() throws IOException {
    largeObjectFile.closeFile();
    smallObjectFile.close();
    try {
      DynamicFileInfo.write(GET_INFO_FILE_NAME(fileName), fileInfo);
    }
    catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
  }

  public synchronized void delete() throws IOException{
    File f = new File(GET_INFO_FILE_NAME(fileName));
    f.delete();
    largeObjectFile.deleteFile();
    smallObjectFile.deleteFile();

  }

  public synchronized void flush() throws IOException {
    largeObjectFile.flushFile();
    smallObjectFile.flush();
    try {
      DynamicFileInfo.write(GET_INFO_FILE_NAME(fileName), fileInfo);
    }
    catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
  }
}
