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
package com.mellowtech.core.disc.blockfile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.cache.*;
import com.mellowtech.core.util.Platform;

/**
 * A BlockFile contains a number of Blocks of fixed size. Each Block stores one
 * or more variable-length records of data.
 * <p>
 * A BlockFile uses the LRU algorithm for caching Blocks.
 * <p>
 * The mapping between record numbers and block numbers is assumed to be managed
 * by the client.
 * 
 * @author rickard.coster@asimus.se
 * @version 1.0
 */
@Deprecated
public class BlockFileNoId {
  protected int cacheSize;
  protected CacheLRU <Integer, BlockNoId> cache;
  protected ByteBuffer blockBuffer;
  protected FileChannel fc;
  protected final int blockSize;
  protected String fileName;
  protected IOException exception = null;
  protected int highestBlockno = 0;
  protected final ByteStorable template;

  public BlockFileNoId(String fileName, int blockSize, ByteStorable template)
      throws IOException {
    this(fileName, blockSize, 10, template);
  }

  public BlockFileNoId(String fileName, int blockSize, int cacheSize,
      ByteStorable template) throws IOException {

    this.fileName = fileName;
    this.cacheSize = cacheSize;
    this.blockSize = blockSize;
    this.template = template;

    fc = new RandomAccessFile(new File(fileName), "rw").getChannel();

    if (fc.size() > 0)
      highestBlockno = (int) (fc.size() / blockSize);

    blockBuffer = ByteBuffer.allocate(blockSize);

    setCache();


  }

  public void close() throws IOException {
    // write all dirty objects to disc and close file
    flush();
    fc.close();
  }

  public void deleteFile() throws IOException {
    fc.close();
    File f = new File(fileName);
    f.delete();
  }

  public void flush() throws IOException {
    if(cache != null){
      for (Iterator <Map.Entry<Integer,CacheValue <BlockNoId>>> iter = cache.iterator(); iter.hasNext();) {
        Map.Entry <Integer, CacheValue<BlockNoId>> e = iter.next();
        Integer key = e.getKey();
        CacheValue <BlockNoId> cv = e.getValue();
        if (cv.isDirty()) {
          writeToFile(key.intValue(), cv.getValue());
          cv.setDirty(false);
        }
      }
    }
  }

  public long size() throws IOException {
    return fc.size();
  }

  public int highestBlockno() throws IOException {
    return highestBlockno;
  }

  public synchronized void write(int blockNo, BlockNoId block) throws IOException {
    if(cache != null)
      cache.put(blockNo, block);
    else
      writeToFile(blockNo, block);
    if (blockNo > highestBlockno)
      highestBlockno = blockNo;
  }

  public synchronized BlockNoId read(int blockNo) throws IOException {
    if(cache != null)
      try{
        return cache.get(blockNo);
      }
      catch(NoSuchValueException e){
        throw new IOException("could not find value");
      }
    return readFromFile(blockNo, new BlockNoId(blockSize, template));
  }

  protected synchronized void writeToFile(int blockNo, BlockNoId block)
      throws IOException {
    fc.position(blockNo * blockSize);
    blockBuffer.clear();
    block.toBytes(blockBuffer);
    blockBuffer.flip();
    fc.write(blockBuffer);
  }

  protected synchronized BlockNoId readFromFile(int blockNo, BlockNoId block)
      throws IOException {
    fc.position(blockNo * blockSize);
    blockBuffer.clear();
    int read = fc.read(blockBuffer);

    blockBuffer.flip();
    block = (BlockNoId) block.fromBytes(blockBuffer);
    int byteSize = block.byteSize();
    return block;
  }

  public String blockByteSizes() {
    StringBuffer sb = new StringBuffer();
    String sep = Platform.getLineSeparator();
    for (int b = 0; b <= highestBlockno; b++) {
      try {
        BlockNoId block = read(b);
        sb.append("block " + b + " byteSize()= " + block.byteSize() + sep);
      }
      catch (Exception e) {
        CoreLog.L().log(Level.WARNING, "", e);
      }
    }
    return sb.toString();
  }

  public BlockUtilization utilization() {
    BlockUtilization totalUtilization = new BlockUtilization();
    for (int b = 0; b <= highestBlockno; b++) {
      try {
        BlockNoId block = read(b);
        BlockUtilization u = block.utilization();
        totalUtilization.add(u);
      }
      catch (Exception e) {
        CoreLog.L().log(Level.WARNING, "", e);
      }
    }
    return totalUtilization;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (int b = 0; b <= highestBlockno; b++) {
      try {
        BlockNoId block = read(b);
        sb.append("block " + b + "\n" + block.toString() + "\n");
      }
      catch (Exception e) {
        CoreLog.L().log(Level.WARNING, "", e);
      }
    }
    return sb.toString();
  }

  private void setCache(){
    if(cacheSize < 1){
      cache = null;
      return;
    }
    Remover <Integer, BlockNoId> remover = new Remover<Integer, BlockNoId>() {
      @Override
      public void remove(Integer key, CacheValue<BlockNoId> value) {
        if(value.isDirty())
          try {
            writeToFile(key.intValue(), value.getValue());
          } catch (IOException e) {
            CoreLog.L().log(Level.SEVERE, "Could Not Write Block", e);
          }
      }
    };

    Loader<Integer, BlockNoId> loader = new Loader<Integer, BlockNoId>() {
      @Override
      public BlockNoId get(Integer key) throws Exception{
        return readFromFile(key.intValue(), new BlockNoId(blockSize, template));
      }
    };

    cache = new CacheLRU <Integer, BlockNoId> (remover, loader, cacheSize);
  }
}
