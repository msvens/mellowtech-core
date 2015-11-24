/**
 * 
 */
package org.mellowtech.core.collections.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.bytestorable.io.SortedBlock;
import org.mellowtech.core.collections.BMap;
import org.mellowtech.core.collections.KeyValue;
import org.mellowtech.core.io.Record;
import org.mellowtech.core.io.RecordFile;
import org.mellowtech.core.io.RecordFileBuilder;
import org.mellowtech.core.util.DataTypeUtils;

/**
 * @author msvens
 *
 */
public class EHTableImp <A, B extends BComparable <A,B>, C, D extends BStorable<C,D>>
  implements BMap <A,B,C,D>{
  
  public static final int VERSION = 12;
  //public static final int DEFAULT_BUCKET_SIZE = 1024;
  //public static final int DEFAULT_MAX_DIRECTORY = (int) Math.pow(2, 16);
  
  private int directory[];
  private int dirDepth;
  private int size = 0;
  private RecordFile bucketFile = null;
  private KeyValue <B,D> kvType = null;
  private int maxDirectory;
  private B keyType;
  private D valueType;
  private int bucketSize;
  private boolean inMemory;
  IntBuffer reserve;
  private Path p;
  
  public EHTableImp(Path path, Class<B> keyType, Class<D> valueType, boolean inMemory) throws Exception{
    this.keyType = keyType.newInstance();
    this.valueType = valueType.newInstance();
    kvType = new KeyValue <> (this.keyType,this.valueType);
    this.inMemory = inMemory;
    this.p = path;
    openFile();
  }
  
  public EHTableImp(Path path, Class<B> keyType, Class<D> valueType, boolean inMemory, int bucketSize, int maxBuckets)
    throws Exception{
    
    this.keyType = keyType.newInstance();
    this.valueType = valueType.newInstance();
    kvType = new KeyValue <> (this.keyType,this.valueType);
    this.inMemory = inMemory;
    this.p = path;
    createFile(bucketSize, maxBuckets);
  }
  
  public boolean containsKey(B key) throws IOException {
    KeyValue <B,D> kv = new KeyValue <> (key, null);
    return readBucket(find(key)).containsKey(kv);
  }
  
  @Override
  public final KeyValue<B, D> getKeyValue(B key) throws IOException {
    KeyValue <B,D> kv = new KeyValue <> (key, null);
    SortedBlock <KeyValue <B,D>> block = readBucket(find(key));
    return block.getKey(kv);
  }
  
  public void put(B key, D value) throws IOException{
    if (key.byteSize() + value.byteSize() > bucketSize / 10)
      throw new IOException("size of key value too large. you should increase bucket size");
    KeyValue <B,D> kv = new KeyValue <> (key,value);
    int rrn = find(kv);
    SortedBlock <KeyValue <B,D>> bucket;
    bucket = readBucket(rrn);

    //delete any previous key
    if(bucket.containsKey(kv)){
      bucket.deleteKey(kv);
      size--;
    }
    if(bucket.fitsKey(kv)){
      size++;
      bucket.insertKey(kv);
      writeBucket(rrn, bucket);
    } else {
      CoreLog.L().finest("Splitting bucket: "+ rrn + this);
      splitBucket(bucket/*, kv*/);
      CoreLog.L().finest(this.toString());
      put(key, value);
    }
  }
  
  public void putIfNotExists(B key, D value) throws IOException{
    if(!containsKey(key))
      put(key,value);
  }
  
  public D remove(B key) throws IOException {
    KeyValue <B,D> toDelete = new KeyValue <> (key, null);
    int rrn = find(toDelete);
    SortedBlock <KeyValue <B,D>> bucket = readBucket(rrn);
    KeyValue <B,D> deleted = bucket.deleteKey(toDelete);
    if (deleted == null)
      return null;
    size--;
    writeBucket(rrn, bucket);
    combineBucket(bucket, rrn);
    return deleted.getValue();
  }
  
  public boolean isEmpty(){
    return size < 1;
  }
  
  public Iterator <KeyValue <B,D>> iterator(){
    return new EHTIterator();
  }
  
  public int size() {
    return size;
  }
  
  public void save() throws IOException {
    writeDepth(dirDepth);
    writeDirectory(directory);
    writeNumItems(size);
    writeVersion(VERSION);
    bucketFile.save();
  }
  
  public void close() throws IOException {
    save();
    bucketFile.close();
  }
  
  public void delete() throws IOException {
    this.bucketFile.close();
    Files.delete(p);
    size = 0;
  }

  @Override
  public void truncate() throws IOException {
    clearFile();
  }

  public double density() throws IOException {
    final AtomicInteger total = new AtomicInteger(0);
    final AtomicInteger used = new AtomicInteger(0);
    SortedBlock <KeyValue <B,D>> tmp = new SortedBlock <> ();
    bucketFile.forEach(r -> {
      tmp.setBlock(r.data, kvType);
      used.addAndGet(tmp.getDataAndPointersBytes());
      total.addAndGet(tmp.storageCapacity());
    });
    return used.doubleValue() / total.doubleValue();
  }
  
  public double emptyBuckets() throws IOException {
    final AtomicInteger empty = new AtomicInteger(0);
    SortedBlock <KeyValue <B,D>> tmp = new SortedBlock <> ();
    bucketFile.forEach(r -> {
      tmp.setBlock(r.data, kvType);
      if(tmp.getNumberOfElements() < 1)
        empty.incrementAndGet();
    });
    return empty.doubleValue() / bucketFile.size();
  }
  
  @Override
  public void compact() throws IOException, UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * The directory size needs to be of power of 2 since it is contionously
   * doubled, thus the number of blocks needs to be the same
   * @param maxBlocks suggested max number of blocks
   * @return new maxBlocks which is a power of 2
   */
  private int alignMaxBlocks(int maxBlocks){
    int current = 2;
    while(true) {
      int next = current * 2;
      if (next >= maxBlocks) {
        if ((maxBlocks - current) < (next - maxBlocks))
          return current;
        return next;
      }
      current = next;
    }
  }

  
  private int find(KeyValue <B,D> kv){
    return find(kv.getKey());
  }
  
  private int find(B key){
    return directory[makeAddress(dirDepth, key)];
  }

  private int findBuddy(SortedBlock <KeyValue <B,D>> bucket) {
    int bucketDepth = readBucketDepth(bucket);
    if (dirDepth == 0)
      return -1;
    if (bucketDepth < dirDepth)
      return -1;
    int sharedAdress = makeAddress(bucketDepth, bucket.getFirstKey().getKey());
    return sharedAdress ^ 1;
  }

  private void combineBucket(SortedBlock <KeyValue <B,D>> bucket, int rrn) throws IOException {
    int bAdress = findBuddy(bucket);
    if (bAdress == -1)
      return;
    int brrn = directory[bAdress];
    SortedBlock <KeyValue <B,D>> bBucket = readBucket(brrn);
    if(bucket.canMerge(bBucket)){
      bucket.mergeBlock(bBucket);
      directory[bAdress] = rrn;
      int ndepth = readBucketDepth(bucket);
      writeBucketDepth(bucket, ndepth-1);
      writeBucket(rrn, bucket);
      deleteBucket(brrn);
      if(collapseDir()){
        combineBucket(bucket, rrn);
      }
    }
  }

  private boolean collapseDir() {
    if (dirDepth == 0)
      return false;
    int dirSize = (int) Math.pow(2, dirDepth);
    for (int i = 0; i < (dirSize - 1); i = i + 2) {
      if (directory[i] != directory[i + 1])
        return false;
    }
    int nDirSize = dirSize / 2;
    int nDir[] = new int[nDirSize];
    for (int i = 0; i < nDirSize; i++)
      nDir[i] = directory[i * 2];
    directory = nDir;
    dirDepth--;
    return true;
  }
  
  private void splitBucket(SortedBlock <KeyValue<B,D>> bucket/*, KeyValue <K,V> tmp*/) throws IOException {
    int bucketAddr = find(bucket.getKey(0));

    CoreLog.L().finest(DataTypeUtils.printBits((short) find(bucket
        .getKey(0))) + " " + (bucket.getKey(0)).getKey());

    if (this.readBucketDepth(bucket) == dirDepth) {
      doubleDir();
    }
    
    BlockRecord newBucket = createNewBucket();
    Range r = findRange(bucket);

    //CoreLog.L().finest("Bucket Range: " + r.from + " " + r.to + " "
    //      + bucketAddr + " " + find(tmp));

    insertBucket(newBucket.record, r);
    writeBucketDepth(bucket, readBucketDepth(bucket)+1);
    writeBucketDepth(newBucket.block, readBucketDepth(bucket));
    redistribute(bucket, newBucket.block, bucketAddr);
    writeBucket(bucketAddr, bucket);
    writeBucket(newBucket.record, newBucket.block);
    writeDirectory(directory);
}
  
  private void redistribute(SortedBlock <KeyValue <B,D>> oldBucket,
      SortedBlock <KeyValue <B,D>> newBucket, int oldAddr) {
    KeyValue <B,D> keyValue;
    for (Iterator <KeyValue <B,D>> it = oldBucket.iterator(); it.hasNext();) {
      keyValue = it.next();
      if (find(keyValue) != oldAddr) {
        it.remove();
        newBucket.insertKeyUnsorted(keyValue);
      }
    }
  }

  private void deleteBucket(int rrn) throws IOException {
    bucketFile.delete(rrn);
  }
  
  private void writeBucket(int record, SortedBlock <KeyValue <B,D>> bucket) throws IOException{
    bucketFile.update(record, bucket.getBlock());
  }

  private SortedBlock <KeyValue <B,D>> readBucket(int record) throws IOException{
    SortedBlock <KeyValue<B,D>> toRet = new SortedBlock <> ();
    try{
      toRet.setBlock(bucketFile.get(record), kvType, false, SortedBlock.PTR_NORMAL, (short)4); 
      return toRet;
    } catch(IOException e){
      CoreLog.L().log(Level.WARNING, "could not read block", e);
      throw e;
    }
  }
  
  private void doubleDir() {
    int currentSize = (int) Math.pow(2, dirDepth);
    int newSize = currentSize * 2;
    if(newSize > maxDirectory){
      throw new Error("could not double directory...no more free blocks");
    }
    int[] tmp = new int[newSize];
    for (int i = 0; i < currentSize; i++) {
      tmp[i * 2] = directory[i];
      tmp[(i * 2) + 1] = directory[i];
    }
    directory = tmp;
    dirDepth++;
  }
  
  private void insertBucket(int bucketAddr, Range range) {
    for (int i = range.from; i <= range.to; i++)
      directory[i] = bucketAddr;
  }
  
  private Range findRange(SortedBlock <KeyValue <B,D>> bucket) {
    int depth = this.readBucketDepth(bucket);
    int shared = makeAddress(depth, bucket.getFirstKey().getKey());
    int toFill = dirDepth - (depth + 1);
    int newShared = shared << 1;
    newShared = newShared | 1;
    Range r = new Range();
    r.to = r.from = newShared;
    for (int i = 0; i < toFill; i++) {
      r.from = r.from << 1;
      r.to = r.to << 1;
      r.to = r.to | 1;
    }
    return r;
  }

  private static <B extends BComparable <?,B>> int makeAddress(int depth, B key){
    int hashVal = key.hashCode();
    int ret = 0;
    int mask = 1;
    int lowbit;
    for (int i = 0; i < depth; i++) {
      ret = ret << 1;
      lowbit = hashVal & mask;
      ret = ret | lowbit;
      hashVal = hashVal >> 1;
    }
    return ret;
  }
  
  private BlockRecord createNewBucket() throws IOException {
    SortedBlock <KeyValue <B,D>> toRet = new SortedBlock <> ();
    try{
      toRet.setBlock(new byte[bucketSize], kvType, true, SortedBlock.PTR_NORMAL, (short) 4);
      writeBucketDepth(toRet, 0);
      int rrn = bucketFile.insert(toRet.getBlock());
      return new BlockRecord(rrn, toRet);
    } catch(IOException e){
      CoreLog.L().severe("could not create new bucket: "+e.toString());
      throw new IOException(e);
    }
  }

  private void openFile() throws IOException {
    RecordFileBuilder rfb = new RecordFileBuilder();
    bucketFile = (inMemory ? rfb.mem() : rfb.disc()).maxBlocks(null).build(p);
    reserve = bucketFile.mapReserve().asIntBuffer();
    if(EHTableImp.VERSION != readVersion()) {
      bucketFile = null;
      reserve = null;
      throw new IOException("wrong version of map file");
    }
    dirDepth = readDepth();
    size = readNumItems();
    directory = readDirectory();
    this.bucketSize = bucketFile.getBlockSize();
  }

  private void createFile(int bucketSize, int maxBuckets) throws IOException {
    RecordFileBuilder rfb = new RecordFileBuilder();
    int maxBlocks = alignMaxBlocks(maxBuckets);
    (inMemory ? rfb.mem() : rfb.disc()).maxBlocks(maxBlocks);
    bucketFile = rfb.blockSize(bucketSize).reserve(16+(4*maxBlocks)).build(p);
    clearFile();
    /*bucketFile.clear();
    directory = new int[1];
    dirDepth = 0;
    size = 0;
    maxDirectory = maxBlocks;
    this.bucketSize = bucketSize;
    reserve = bucketFile.mapReserve().asIntBuffer();
    writeVersion(EHTableImp.VERSION);
    writeDepth(dirDepth);
    writeNumItems(size);
    //BlockRecord br = createNewBucket();
    directory[0] = createNewBucket().record;*/
  }

  private void clearFile() throws IOException{
    bucketFile.clear();
    directory = new int[1];
    dirDepth = 0;
    size = 0;
    maxDirectory = bucketFile.getFreeBlocks();
    bucketSize = bucketFile.getBlockSize();
    reserve = bucketFile.mapReserve().asIntBuffer();
    writeVersion(EHTableImp.VERSION);
    writeDepth(dirDepth);
    writeNumItems(size);
    //BlockRecord br = createNewBucket();
    directory[0] = createNewBucket().record;
  }
  
  private void writeBucketDepth(SortedBlock <KeyValue <B,D>> bucket, int depth){
    ByteBuffer bb = ByteBuffer.wrap(bucket.getBlock());
    bb.position(bucket.getReservedSpaceStart());
    bb.putInt(depth);
  }
  
  private int readBucketDepth(SortedBlock <KeyValue <B,D>> bucket){
    ByteBuffer bb = ByteBuffer.wrap(bucket.getBlock());
    bb.position(bucket.getReservedSpaceStart());
    return bb.getInt();
  }
  
  private int readVersion(){
    return reserve.get(0);
  }
  
  private void writeVersion(int version){
    reserve.put(0, version);
  }
  
  private int readDepth(){
    return reserve.get(1);
  }
  
  private void writeDepth(int depth){
    reserve.put(1, depth);
  }
  
  private int readNumItems(){
    return reserve.get(2);
  }
  
  private void writeNumItems(int items){
    reserve.put(2, items);
  }
  
  private int[] readDirectory(){
    int size = reserve.get(3);
    int dir[] = new int[size];
    reserve.position(4);
    reserve.get(dir);
    return dir;
  }
  
  private void writeDirectory(int dir[]){
    reserve.put(3, dir.length);
    reserve.position(4);
    reserve.put(dir);
  }
  
  class BlockRecord {
    int record = 0;
    SortedBlock <KeyValue <B,D>> block;
    public BlockRecord(int r, SortedBlock <KeyValue <B,D>> b){
      record = r;
      block = b;
    }
  }
  
  class Range {
    int from, to;
  }
  
  class EHTIterator implements Iterator <KeyValue<B, D>> {
    Iterator <Record> fileIterator = bucketFile.iterator();
    
    Iterator <KeyValue <B,D>> bucketIterator;
    SortedBlock <KeyValue <B,D>> sb = new SortedBlock <> ();
    
    byte b[];

    public EHTIterator() {
      if (!fileIterator.hasNext())
        return;
      b = fileIterator.next().data;
      sb.setBlock(b, kvType);
      bucketIterator = sb.iterator();
    }

    public boolean hasNext() {
      if (bucketIterator.hasNext())
        return true;
      while (fileIterator.hasNext()) {
        b = fileIterator.next().data;
        sb.setBlock(b, kvType);
        bucketIterator = sb.iterator();
        if (bucketIterator.hasNext())
          return true;
      }
      return false;
    }

    public KeyValue <B,D> next() {
      return bucketIterator.next();
    }

    public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }
  }
}
