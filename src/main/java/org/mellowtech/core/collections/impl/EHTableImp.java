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
import java.security.Key;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.bytestorable.io.BCBlock;
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
    //KeyValue <B,D> kv = new KeyValue <> (key, null);
    return readBucket(key).contains(toKV(key));
  }

  private KeyValue <B,D> toKV(B key){return new KeyValue <> (key);}
  
  @Override
  public final KeyValue<B, D> getKeyValue(B key) throws IOException {
    return readBucket(key).get(toKV(key));
  }
  
  public void put(B key, D value) throws IOException{
    if (key.byteSize() + value.byteSize() > bucketSize / 10)
      throw new IOException("size of key value too large. you should increase bucket size");
    KeyValue <B,D> kv = new KeyValue <> (key,value);
    int rrn = find(kv);
    BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> bucket = readBucket(rrn);


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
      CoreLog.L().finest("Splitting bucket: "+ rrn + this);
      splitBucket(bucket);
      CoreLog.L().finest(this.toString());
      put(key, value);
    }
  }
  
  public void putIfNotExists(B key, D value) throws IOException{
    if(!containsKey(key))
      put(key,value);
  }
  
  public D remove(B key) throws IOException {
    //KeyValue <B,D> toDelete = new KeyValue <> (key, null);
    int rrn = find(key);
    BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> bucket = readBucket(rrn);
    KeyValue <B,D> deleted = bucket.delete(toKV(key));
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
    bucketFile.forEach(r -> {
      BCBlock<KeyValue.KV<B,D>, KeyValue<B,D>> tmp = new BCBlock<>(r.data, kvType);
      used.addAndGet(tmp.getDataAndPointersBytes());
      total.addAndGet(tmp.storageCapacity());
    });
    return used.doubleValue() / total.doubleValue();
  }
  
  public double emptyBuckets() throws IOException {
    final AtomicInteger empty = new AtomicInteger(0);
    bucketFile.forEach(r -> {
      BCBlock<KeyValue.KV<B,D>, KeyValue<B,D>> tmp = new BCBlock<>(r.data, kvType);
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

  private int findBuddy(BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> bucket) {
    int bucketDepth = readBucketDepth(bucket);
    if (dirDepth == 0)
      return -1;
    if (bucketDepth < dirDepth)
      return -1;
    int sharedAdress = makeAddress(bucketDepth, bucket.getFirst().getKey());
    return sharedAdress ^ 1;
  }

  private void combineBucket(BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> bucket, int rrn) throws IOException {
    int bAdress = findBuddy(bucket);
    if (bAdress == -1)
      return;
    int brrn = directory[bAdress];
    BCBlock <KeyValue.KV<B,D>, KeyValue<B,D>> bBucket = readBucket(brrn);
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
  
  private void splitBucket(BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> bucket) throws IOException {
    int bucketAddr = find(bucket.get(0));

    CoreLog.L().finest(DataTypeUtils.printBits((short) find(bucket
        .get(0))) + " " + (bucket.get(0)).getKey());

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
  
  private void redistribute(BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> oldBucket,
                            BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> newBucket, int oldAddr) {
    KeyValue <B,D> keyValue;
    for (Iterator <KeyValue <B,D>> it = oldBucket.iterator(); it.hasNext();) {
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
  
  private void writeBucket(int record, BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> bucket) throws IOException{
    bucketFile.update(record, bucket.getBlock());
  }

  private BCBlock<KeyValue.KV<B,D>, KeyValue<B,D>> readBucket(B key) throws IOException {
    return readBucket(find(key));
  }

  private BCBlock<KeyValue.KV<B,D>, KeyValue<B,D>> readBucket(int record) throws IOException{
    try{
      return new BCBlock<>(bucketFile.get(record), kvType);
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
  
  private Range findRange(BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> bucket) {
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
    try{
      BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> toRet = new BCBlock<>(bucketSize, kvType,
          BCBlock.PtrType.NORMAL, (short) 4);
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
    directory[0] = createNewBucket().record;
  }
  
  private void writeBucketDepth(BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> bucket, int depth){
    ByteBuffer bb = ByteBuffer.wrap(bucket.getBlock());
    bb.position(bucket.getReservedSpaceStart());
    bb.putInt(depth);
  }
  
  private int readBucketDepth(BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> bucket){
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
    BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> block;
    public BlockRecord(int r, BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> b){
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
    BCBlock <KeyValue.KV<B,D>, KeyValue <B,D>> sb;
    
    byte b[];

    public EHTIterator() {
      if (!fileIterator.hasNext())
        return;
      b = fileIterator.next().data;
      sb = new BCBlock<>(b, kvType);
      bucketIterator = sb.iterator();
    }

    public boolean hasNext() {
      if (bucketIterator.hasNext())
        return true;
      while (fileIterator.hasNext()) {
        b = fileIterator.next().data;
        sb = new BCBlock<>(b, kvType);
        bucketIterator = sb.iterator();
        if (bucketIterator.hasNext())
          return true;
      }
      return false;
    }

    public KeyValue <B,D> next() {
      return bucketIterator.next();
    }

  }
}
