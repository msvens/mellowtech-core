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

package org.mellowtech.core.collections.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.mellowtech.core.codec.BBuffer;
import org.mellowtech.core.codec.BCodec;
import org.mellowtech.core.collections.BMap;
import org.mellowtech.core.collections.KeyValue;
import org.mellowtech.core.io.Record;
import org.mellowtech.core.io.RecordFile;
import org.mellowtech.core.io.RecordFileBuilder;
import org.mellowtech.core.util.DataTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author msvens
 *
 */
public class EHTableImp <A,B> implements BMap <A,B>{
  
  private static final int VERSION = 12;

  private final Logger logger = LoggerFactory.getLogger(EHTableImp.class);
  
  private int directory[];
  private int dirDepth;
  private int size = 0;
  private RecordFile bucketFile = null;
  private final KeyValueCodec <A,B> kvCodec;
  private int maxDirectory;
  private final BCodec<A> keyCodec;
  private final BCodec<B> valueCodec;
  private int bucketSize;
  private boolean inMemory;
  private IntBuffer reserve;
  private Path p;
  
  /*public EHTableImp(Path path, BCodec<A> keyCodec, BCodec<B> valueCodec, boolean inMemory) throws Exception{
    this.keyCodec = keyCodec;
    this.valueCodec = valueCodec;
    this.kvCodec = new KeyValueCodec<>(keyCodec,valueCodec);
    this.inMemory = inMemory;
    this.p = path;
    openFile();
  }*/
  
  public EHTableImp(Path path, BCodec<A> keyCodec, BCodec<B> valueCodec, boolean inMemory, int bucketSize, int maxBuckets)
    throws Exception{

    this.keyCodec = keyCodec;
    this.valueCodec = valueCodec;
    this.kvCodec = new KeyValueCodec<>(keyCodec,valueCodec);
    this.inMemory = inMemory;
    this.p = path;
    try {
      openFile();
    } catch(Exception e){
      createFile(bucketSize, maxBuckets);
    }
  }
  
  public boolean containsKey(A key) throws IOException {
    return readBucket(key).contains(toKV(key));
  }

  private KeyValue <A,B> toKV(A key){return new KeyValue <> (key);}
  
  @Override
  public final KeyValue<A,B> getKeyValue(A key) throws IOException {
    return readBucket(key).get(toKV(key));
  }
  
  public void put(A key, B value) throws IOException{
    KeyValue<A,B> kv = new KeyValue <> (key,value);
    if (kvCodec.byteSize(kv) > bucketSize / 10)
      throw new IOException("size of key value too large. you should increase bucket size");
    int rrn = find(kv);
    BBuffer <KeyValue<A,B>> bucket = readBucket(rrn);


    //delete any previous key
    if(bucket.contains(kv)){
      bucket.delete(kv);
      size--;
    }
    if(bucket.fits(kv)){
      size++;
      bucket.insert(kv);
      writeBucket(rrn, bucket);
    } else {
      logger.trace("slitting bucker {} {}", rrn, this);
      splitBucket(bucket);
      logger.trace(this.toString());
      put(key, value);
    }
  }
  
  public void putIfNotExists(A key, B value) throws IOException{
    if(!containsKey(key))
      put(key,value);
  }
  
  public B remove(A key) throws IOException {
    int rrn = find(key);
    BBuffer <KeyValue<A,B>> bucket = readBucket(rrn);
    KeyValue<A,B> deleted = bucket.delete(toKV(key));
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
  
  public Iterator <KeyValue <A,B>> iterator(){
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
    bucketFile.forEach(r -> {
      BBuffer<KeyValue<A,B>> tmp = new BBuffer<>(ByteBuffer.wrap(r.data), kvCodec);
      used.addAndGet(tmp.getDataAndPointersBytes());
      total.addAndGet(tmp.storageCapacity());
    });
    return used.doubleValue() / total.doubleValue();
  }
  
  public double emptyBuckets() throws IOException {
    final AtomicInteger empty = new AtomicInteger(0);
    bucketFile.forEach(r -> {
      BBuffer<KeyValue<A,B>> tmp = new BBuffer<>(ByteBuffer.wrap(r.data), kvCodec);
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

  
  private int find(KeyValue <A,B> kv){
    return find(kv.getKey());
  }
  
  private int find(A key){
    return directory[makeAddress(dirDepth, key)];
  }

  private int findBuddy(BBuffer <KeyValue<A,B>> bucket) {
    int bucketDepth = readBucketDepth(bucket);
    if (dirDepth == 0)
      return -1;
    if (bucketDepth < dirDepth)
      return -1;
    int sharedAdress = makeAddress(bucketDepth, bucket.getFirst().getKey());
    return sharedAdress ^ 1;
  }

  private void combineBucket(BBuffer <KeyValue<A,B>> bucket, int rrn) throws IOException {
    int bAdress = findBuddy(bucket);
    if (bAdress == -1)
      return;
    int brrn = directory[bAdress];
    BBuffer <KeyValue<A,B>> bBucket = readBucket(brrn);
    if(bucket.fits(bBucket)){
      bucket.merge(bBucket);
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
  
  private void splitBucket(BBuffer <KeyValue<A,B>> bucket) throws IOException {
    int bucketAddr = find(bucket.get(0));

    logger.trace(DataTypeUtils.printBits((short) find(bucket.get(0))) + " " + (bucket.get(0)).getKey());

    if (this.readBucketDepth(bucket) == dirDepth) {
      doubleDir();
    }
    
    BlockRecord newBucket = createNewBucket();
    Range r = findRange(bucket);

    insertBucket(newBucket.record, r);
    writeBucketDepth(bucket, readBucketDepth(bucket)+1);
    writeBucketDepth(newBucket.block, readBucketDepth(bucket));
    redistribute(bucket, newBucket.block, bucketAddr);
    writeBucket(bucketAddr, bucket);
    writeBucket(newBucket.record, newBucket.block);
    writeDirectory(directory);
}
  
  private void redistribute(BBuffer <KeyValue<A,B>> oldBucket,
                            BBuffer <KeyValue<A,B>> newBucket, int oldAddr) {
    KeyValue<A,B> keyValue;
    for (Iterator <KeyValue <A,B>> it = oldBucket.iterator(); it.hasNext();) {
      keyValue = it.next();
      if (find(keyValue) != oldAddr) {
        it.remove();
        newBucket.insertUnsorted(keyValue);
      }
    }
  }

  private void deleteBucket(int rrn) throws IOException {
    bucketFile.delete(rrn);
  }
  
  private void writeBucket(int record, BBuffer <KeyValue<A,B>> bucket) throws IOException{
    bucketFile.update(record, bucket.getArray());
  }

  private BBuffer<KeyValue<A,B>> readBucket(A key) throws IOException {
    return readBucket(find(key));
  }

  private BBuffer<KeyValue<A,B>> readBucket(int record) throws IOException{
    try{
      return new BBuffer<>(ByteBuffer.wrap(bucketFile.get(record)), kvCodec);
    } catch(IOException e){

      logger.warn("Could not read block", e);
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
  
  private Range findRange(BBuffer <KeyValue<A,B>> bucket) {
    int depth = this.readBucketDepth(bucket);
    int shared = makeAddress(depth, bucket.getFirst().getKey());
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

  private static <B> int makeAddress(int depth, B key){
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
    try{
      BBuffer <KeyValue<A,B>> toRet = new BBuffer<>(bucketSize, kvCodec,
          BBuffer.PtrType.NORMAL, (short) 4);
      writeBucketDepth(toRet, 0);
      int rrn = bucketFile.insert(toRet.getArray());
      return new BlockRecord(rrn, toRet);
    } catch(IOException e){
      logger.error("could not create new bucker", e);
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
    directory[0] = createNewBucket().record;
  }
  
  private void writeBucketDepth(BBuffer <KeyValue<A,B>> bucket, int depth){
    ByteBuffer bb = bucket.getBlock();
    bb.position(bucket.getReservedSpaceStart());
    bb.putInt(depth);
  }
  
  private int readBucketDepth(BBuffer <KeyValue<A,B>> bucket){
    ByteBuffer bb = bucket.getBlock();
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
  
  private class BlockRecord {
    int record = 0;
    BBuffer <KeyValue<A,B>> block;
    BlockRecord(int r, BBuffer<KeyValue<A,B>> b){
      record = r;
      block = b;
    }
  }
  
  private class Range {
    int from, to;
  }
  
  private class EHTIterator implements Iterator <KeyValue<A,B>> {
    Iterator <Record> fileIterator = bucketFile.iterator();
    
    Iterator <KeyValue <A,B>> bucketIterator;
    BBuffer <KeyValue<A,B>> sb;
    
    byte b[];

    EHTIterator() {
      if (!fileIterator.hasNext())
        return;
      b = fileIterator.next().data;
      sb = new BBuffer<>(ByteBuffer.wrap(b), kvCodec);
      bucketIterator = sb.iterator();
    }

    public boolean hasNext() {
      if (bucketIterator.hasNext())
        return true;
      while (fileIterator.hasNext()) {
        b = fileIterator.next().data;
        sb = new BBuffer<>(ByteBuffer.wrap(b), kvCodec);
        bucketIterator = sb.iterator();
        if (bucketIterator.hasNext())
          return true;
      }
      return false;
    }

    public KeyValue <A,B> next() {
      return bucketIterator.next();
    }

  }
}
