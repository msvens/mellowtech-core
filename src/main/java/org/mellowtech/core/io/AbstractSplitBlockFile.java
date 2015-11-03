package org.mellowtech.core.io;

import com.google.common.base.Objects;
import org.mellowtech.core.CoreLog;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

/**
 * A Block file that keeps a separate mapped region with block accessed via the
 * (mapped... methods). Two separate bitsets are used to mark which blocks have been used
 * Format of File
 * MAGIC_MARKER (4 bytes)
 * FILE_VERSION (4 bytes)
 * BLOCK_SIZE   (4 bytes)
 * MAX_BLOCKS   (4 bytes)
 * MAPPED_BLOCK_SIZE (4 bytes)
 * MAX_MAPPED_BLOCKS (4 bytes)
 * RESERVED_SIZE (4 bytes)
 * MAPPED_BLOCK_BIT_SET (4 bytes + bitset)
 * BLOCK_BIT_SET (4 bytes + bitset)
 * RESERVED
 * MAPPED_BLOCKS
 * BLOCKS
 * Created by msvens on 30/10/15.
 */
abstract class AbstractSplitBlockFile implements SplitRecordFile {

  public static final String FILE_EXT = ".mlf";
  public static final int MIN_BLOCK_SIZE = 256;
  public static final int MAGIC_MARKER = 4444;
  public static final int VERSION = 2;
  protected Path p;
  protected FileChannel fc;
  protected int maxBlocks;
  protected int mappedMaxBlocks;
  protected BitSet bitSet;
  protected BitSet mappedBitSet;
  protected MappedByteBuffer bitBuffer;
  protected MappedByteBuffer mappedBitBuffer;
  protected MappedByteBuffer mappedBlocks;
  protected int reserve;
  private int blockSize;
  private int mappedBlockSize;

  public AbstractSplitBlockFile(Path path) throws IOException {
    if (!openFile(path))
      throw new IOException("could not open split block file");
  }

  public AbstractSplitBlockFile(Path path, int blockSize, int maxBlocks,
                        int reserve, int mappedMaxBlocks, int mappedBlockSize) throws IOException {

    try {
      if (openFile(path)) return;
    } catch (IOException e) {
      CoreLog.L().log(Level.FINER, "Could Not Open Old File", e);
    }

    if (reserve < 0) reserve = 0;
    this.reserve = reserve;
    this.maxBlocks = maxBlocks;
    this.blockSize = blockSize < MIN_BLOCK_SIZE ? MIN_BLOCK_SIZE : blockSize;
    this.mappedBlockSize = mappedBlockSize;
    this.mappedMaxBlocks = mappedMaxBlocks;
    this.p = path;
    createFile(path);
    openFile(path);
  }

  @Override
  public void clear() throws IOException {
    bitSet.clear();
    mappedBitSet.clear();
  }

  @Override
  public void close() throws IOException {
    if(fc.isOpen()) {
      save();
      fc.close();
    }
  }

  @Override
  public boolean isOpen() {
    return fc.isOpen();
  }

  @Override
  public Map<Integer, Integer> compact() throws IOException {
    return null;
  }

  @Override
  public boolean contains(int record) {
    return bitSet.get(record);
  }

  @Override
  public boolean containsRegion(int record) {
    return mappedBitSet.get(record);
  }

  @Override
  public boolean delete(int record) throws IOException {
    if (bitSet.get(record)) {
      bitSet.flip(record);
      saveBitSet(bitSet, bitBuffer);
      return true;
    }
    return false;
  }

  @Override
  public boolean deleteRegion(int record) throws IOException {
    if (mappedBitSet.get(record)) {
      mappedBitSet.flip(record);
      saveBitSet(mappedBitSet, mappedBitBuffer);
      return true;
    }
    return false;
  }

  @Override
  public int getBlockSize() {
    return blockSize;
  }

  @Override
  public int getBlockSizeRegion() {
    return mappedBlockSize;
  }

  @Override
  public int getFirstRecord() {
    return bitSet.nextSetBit(0);
  }

  @Override
  public int getFirstRecordRegion() {
    return mappedBitSet.nextSetBit(0);
  }

  @Override
  public int getFreeBlocks() {
    return maxBlocks - size();
  }

  @Override
  public int getFreeBlocksRegion() {
    return mappedMaxBlocks - sizeRegion();
  }

  @Override
  public boolean getRegion(int record, byte[] buffer) throws IOException {
    if (mappedBitSet.get(record)) {
      mappedBlocks.position(record * getBlockSizeRegion());
      if (buffer.length > getBlockSizeRegion()) {
        mappedBlocks.get(buffer, 0, getBlockSizeRegion());
      } else
        mappedBlocks.get(buffer);
      return true;
    }
    return false;
  }

  @Override
  public byte[] getReserve() throws IOException {
    if (reservedSize() < 1) return null;
    ByteBuffer bb = ByteBuffer.allocate(reservedSize());
    fc.read(bb, reservedOffset());
    return bb.array();
  }

  @Override
  public int insertRegion(byte[] bytes, int offset, int length) throws IOException {
    int index = mappedBitSet.nextClearBit(0);
    if (index >= mappedMaxBlocks)
      throw new IOException("no blocks left in mapped region");
    if (bytes != null && bytes.length > 0) {
      int l = length > getBlockSizeRegion() ? getBlockSizeRegion() : length;
      mappedBlocks.position(index * getBlockSizeRegion());
      mappedBlocks.put(bytes, offset, l);
    }
    mappedBitSet.set(index, true);
    saveBitSet(mappedBitSet, mappedBitBuffer);
    return index;
  }

  @Override
  public void insertRegion(int record, byte[] bytes) throws IOException {
    if (record > mappedMaxBlocks)
      throw new IOException("record out of bounce");

    mappedBitSet.set(record, true);
    saveBitSet(mappedBitSet, mappedBitBuffer);
    updateRegion(record, bytes);
  }

  @Override
  public Iterator<Record> iterator() {
    return new BlockIterator(null);
  }

  @Override
  public Iterator<Record> iterator(int record) {
    return new BlockIterator(record > -1 ? record : null);
  }

  @Override
  public Iterator<Record> iteratorRegion() {
    return new MappedBlockIterator(null);
  }

  @Override
  public Iterator<Record> iteratorRegion(int record) {
    return new MappedBlockIterator(record > -1 ? record : null);
  }

  @Override
  public MappedByteBuffer mapReserve() throws IOException {
    return fc.map(FileChannel.MapMode.READ_WRITE, reservedOffset(), reservedSize());
  }

  @Override
  public boolean save() throws IOException {
    ByteBuffer bb = ByteBuffer.allocate(headerSize());
    bb.putInt(MAGIC_MARKER);
    bb.putInt(VERSION);
    bb.putInt(blockSize);
    bb.putInt(maxBlocks);
    bb.putInt(mappedBlockSize);
    bb.putInt(mappedMaxBlocks);
    bb.putInt(reserve);
    bb.flip();
    fc.write(bb, 0);
    saveBitSet(mappedBitSet, mappedBitBuffer);
    mappedBitBuffer.force();
    saveBitSet(bitSet, bitBuffer);
    bitBuffer.force();
    mappedBlocks.force();
    fc.force(true);
    return true;
  }

  @Override
  public void setReserve(byte[] bytes) throws IOException {
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    if (bytes.length > reservedSize()) bb.limit(reservedSize());
    fc.write(bb, reservedOffset());
  }

  @Override
  public int size() {
    return bitSet.cardinality();
  }

  @Override
  public int sizeRegion() {
    return mappedBitSet.cardinality();
  }

  public String toString() {
    return Objects.toStringHelper(this).
        add("maxBlocks", maxBlocks).
        add("fileName", p.toString()).
        add("startMappedBlocks", regionBlocksOffset()).
        add("startBlocks", blocksOffset()).
        add("bitSize", bitSetSize()).
        add("mappedBitSize", regionBitSetSize()).
        add("blockSize", blockSize).
        add("maxBlocks", maxBlocks).
        add("mappedMaxBlocks", mappedMaxBlocks).
        add("reserve", reserve).
        add("mappedBitSet", mappedBitSet.toString()).
        add("bitSet", bitSet.toString()).toString();
  }

  @Override
  public boolean updateRegion(int record, byte[] bytes, int offset, int length) throws IOException {
    if (!mappedBitSet.get(record)) return false;
    mappedBlocks.position(record * getBlockSizeRegion());
    int l = length > getBlockSizeRegion() ? getBlockSizeRegion() : length;
    mappedBlocks.put(bytes, offset, l);
    return true;
  }

  protected long align(long offset) {
    return offset + (offset % blockSize);
  }

  protected long bitSetOffset() {
    return align(regionBitSetOffset() + regionBitSetSize());
  }

  protected int bitSetSize() {
    int bytes = (int) Math.ceil((maxBlocks / 8D));
    return 4 + bytes;
  }

  protected long blocksOffset() {
    return regionBlocksOffset() + regionBlocksSize();
  }

  protected void createFile(Path p) throws IOException {
    fc = FileChannel.open(p, StandardOpenOption.CREATE_NEW, StandardOpenOption.READ, StandardOpenOption.WRITE);
    mappedBitBuffer = fc.map(FileChannel.MapMode.READ_WRITE, regionBitSetOffset(), regionBitSetSize());
    bitBuffer = fc.map(FileChannel.MapMode.READ_WRITE, bitSetOffset(), bitSetSize());
    bitSet = new BitSet();
    mappedBitSet = new BitSet();
    mappedBlocks = fc.map(FileChannel.MapMode.READ_WRITE, regionBlocksOffset(),
        regionBlocksSize());
    close();
  }

  protected long getOffset(int record) {
    return blocksOffset() + (record * blockSize);
  }

  protected long headerOffset() {
    return 0;
  }

  protected int headerSize() {
    return 28;
  }

  protected boolean openFile(Path p) throws IOException {
    if (Files.notExists(p)) return false;

    fc = FileChannel.open(p, StandardOpenOption.READ, StandardOpenOption.WRITE);
    this.p = p;

    //read header
    ByteBuffer bb = ByteBuffer.allocate(headerSize());
    fc.read(bb, headerOffset());
    bb.flip();
    if (bb.limit() < headerSize()) throw new IOException("not a split block file");
    if (bb.getInt() != MAGIC_MARKER) throw new IOException("magic marker not present");
    int fversion = bb.getInt();
    if (fversion != VERSION) throw new IOException("file version dont match: " + fversion + "::" + VERSION);
    blockSize = bb.getInt();
    maxBlocks = bb.getInt();
    mappedBlockSize = bb.getInt();
    mappedMaxBlocks = bb.getInt();
    reserve = bb.getInt();

    //bitsets
    mappedBitBuffer = fc.map(FileChannel.MapMode.READ_WRITE, regionBitSetOffset(), regionBitSetSize());
    int numBytes = mappedBitBuffer.getInt();
    ByteBuffer lb = mappedBitBuffer.slice();
    //System.out.println("mapped size: "+numBytes);
    lb.limit(numBytes);
    mappedBitSet = BitSet.valueOf(lb);

    bitBuffer = fc.map(FileChannel.MapMode.READ_WRITE, bitSetOffset(), bitSetSize());
    numBytes = bitBuffer.getInt();
    lb = bitBuffer.slice();
    lb.limit(numBytes);
    bitSet = BitSet.valueOf(lb);

    //region blocks
    mappedBlocks = fc.map(FileChannel.MapMode.READ_WRITE, regionBlocksOffset(),
        regionBlocksSize());

    return true;

  }

  protected long regionBitSetOffset() {
    return blockSize;
  }

  protected int regionBitSetSize() {
    int bytes = (int) Math.ceil((mappedMaxBlocks / 8D));
    return 4 + bytes;
  }

  protected long regionBlocksOffset() {
    return align(reservedOffset() + reservedSize());
  }

  protected void saveBitSet(BitSet toSave, ByteBuffer bb) throws IOException {
    bb.clear();
    byte[] bits = toSave.toByteArray();
    bb.putInt(bits.length);
    bb.put(bits);
  }

  private long regionBlocksSize() {
    return mappedBlockSize * (long) mappedMaxBlocks;
  }

  private long reservedOffset() {
    return align(bitSetOffset() + bitSetSize());
  }

  private int reservedSize() {
    return this.reserve;
  }

  class BlockIterator implements Iterator<Record> {

    Record next = null;
    int record;

    public BlockIterator(Integer from) {
      record = from != null ? from : 0;
      getNext();
    }

    @Override
    public boolean hasNext() {
      return record > 0;
    }

    @Override
    public Record next() {
      Record toRet = next;
      getNext();
      return toRet;
    }

    private void getNext() {
      if (record < 0) return;
      record = bitSet.nextSetBit(record);
      if (record > -1) {
        try {
          next = new Record(record, get(record));
          record++;
        } catch (IOException e) {
          record = -1;
        }
      }
    }

  }

  class MappedBlockIterator implements Iterator<Record> {

    Record next = null;
    int record;

    public MappedBlockIterator(Integer from) {
      record = from != null ? from : 0;
      getNext();
    }

    @Override
    public boolean hasNext() {
      return record > 0;
    }

    @Override
    public Record next() {
      Record toRet = next;
      getNext();
      return toRet;
    }

    private void getNext() {
      if (record < 0) return;
      record = mappedBitSet.nextSetBit(record);
      if (record > -1) {
        try {
          next = new Record(record, getRegion(record));
          record++;
        } catch (IOException e) {
          record = -1;
        }
      }
    }
  }
}
