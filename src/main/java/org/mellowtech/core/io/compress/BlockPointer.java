package org.mellowtech.core.io.compress;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Martin Svensson
 * @since 2018-02-10
 */
public class BlockPointer {

  private int size;
  private int origSize;
  private boolean deleted;
  private long offset;

  static final int ByteSize = 12;



  public static BlockPointer read(FileChannel fc, long offset) throws IOException{
    ByteBuffer bb = ByteBuffer.allocate(12);
    fc.read(bb, offset);
    return read(bb, 0, offset);
  }

  public static BlockPointer read(ByteBuffer bb, int bufferOffset, long fileOffset) throws IOException{
    int type = bb.getInt(bufferOffset);
    int size = bb.getInt(bufferOffset+4);
    int origSize = bb.getInt(bufferOffset+8);
    boolean deleted = false;
    if(type == CFile.DELETED_BLOCK){
      deleted = true;
    }
    else if(type != CFile.BLOCK)
      throw new IOException("Could not read block");
    return new BlockPointer(fileOffset, size, origSize, deleted);
  }

  public static BlockPointer empty(){
    return new BlockPointer(-1, -1, -1, false);
  }


  BlockPointer(long offset, int size, int origSize, boolean deleted){
    this.offset = offset;
    this.size = size;
    this.origSize = origSize;
    this.deleted = deleted;
  }

  public int getSize() {
    return size;
  }

  public int getOrigSize() {
    return origSize;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public long getOffset() {
    return offset;
  }

  public long getDataOffset() {
    return offset + ByteSize;
  }

  void write(FileChannel fc, long offset) throws IOException{
    ByteBuffer bb = ByteBuffer.allocate(12);
    write(bb, 0);
    fc.write(bb);
  }

  void write(ByteBuffer bb, int offset){
    bb.putInt(offset, deleted ? CFile.DELETED_BLOCK : CFile.BLOCK);
    bb.putInt(offset+4, size);
    bb.putInt(offset+8, origSize);
  }

  public String toString(){
    return String.format("offset: %d, size: %d, origSize: %d, deleted: %b", offset, size, origSize, deleted);
    //return "offset: "+offset+"\tsize: "+size+"\torigSize: "+origSize+"\tdeleted: "+deleted;
  }




}
