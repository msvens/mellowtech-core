package org.mellowtech.core.collections.impl;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static java.nio.file.StandardOpenOption.*;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.collections.BMap;
import org.mellowtech.core.collections.KeyValue;

public class EHBlobTableImp <A, B extends BComparable <A,B>, C, D extends BStorable<C,D>>
implements BMap <A,B,C,D>{
  
  private final FileChannel blobs;
  EHTableImp <A,B, ?,BlobPointer> eht;
  D template;
  private Path fName;

  public EHBlobTableImp(Path p, Class <B> keyType, Class <D> valueType, boolean inMemory) throws Exception{
    this.fName = fName;
    //@SuppressWarnings("resource")
    eht = new EHTableImp <> (fName, keyType, BlobPointer.class, inMemory);
    template = valueType.newInstance();
    File f = new File(fName+".blb");
    blobs = FileChannel.open(f.toPath(), WRITE, READ); 
  }
  
  public EHBlobTableImp(Path fName, Class <B> keyType, Class <D> valueType,
      boolean inMemory, int bucketSize, int maxBuckets) throws Exception{
    this.fName = fName;
    eht = new EHTableImp <> (fName, keyType, BlobPointer.class, inMemory, bucketSize, maxBuckets);
    File f = new File(fName+".blb");
    blobs = FileChannel.open(f.toPath(), WRITE, READ, TRUNCATE_EXISTING, CREATE); 
  }
  
  @Override
  public void save() throws IOException {
    eht.save();
    blobs.force(true);
  }

  @Override
  public void close() throws IOException {
    eht.close();
    blobs.close();
  }

  @Override
  public void delete() throws IOException {
    eht.delete();
    blobs.close();
    File f = new File(fName+".blb");
    f.delete();
  }

  @Override
  public void truncate() throws IOException {
    eht.truncate();
    blobs.truncate(0);
  }

  @Override
  public int size() throws IOException {
    return eht.size();
  }

  @Override
  public boolean isEmpty() throws IOException {
    return eht.isEmpty();
  }

  @Override
  public boolean containsKey(B key) throws IOException {
    return eht.containsKey(key);
  }

  @Override
  public void put(B key, D value) throws IOException {
    int size = value.byteSize();
    long fpos = blobs.size();
    BlobPointer bp = new BlobPointer(fpos, size);
    eht.put(key, bp);
    ByteBuffer bb = value.to(); bb.flip();
    blobs.write(bb, fpos);
    
  }

  @Override
  public D remove(B key) throws IOException {
    BlobPointer bp = eht.remove(key);
    return bp != null ? getValue(bp) : null;
  }

  @Override
  public KeyValue<B, D> getKeyValue(B key) throws IOException {
    KeyValue <B, BlobPointer> tmp = eht.getKeyValue(key);
    if(tmp != null){
      KeyValue <B, D> kv = new KeyValue<>(tmp.getKey(), null);
      if(tmp.getValue() != null)
        kv.setValue(getValue(tmp.getValue()));
    }
    return null;
  }

  @Override
  public Iterator<KeyValue<B, D>> iterator() {
    return new EHBlobIterator();
  }

  @Override
  public void compact() throws IOException, UnsupportedOperationException {
    // TODO Auto-generated method stub
    
  }
  
  private final D getValue(BlobPointer bp) throws IOException{
    ByteBuffer bb = ByteBuffer.allocate(bp.getbSize());
    blobs.read(bb, bp.getfPointer());
    bb.flip();
    return template.from(bb);
  }
  
  class EHBlobIterator implements Iterator <KeyValue <B,D>>{

    Iterator <KeyValue <B, BlobPointer>> iter;

    public EHBlobIterator(){
      iter = eht.iterator();
    }


    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public KeyValue<B, D> next() {
      KeyValue <B, BlobPointer> next = iter.next();
      if(next == null) return null;
      KeyValue <B, D> toRet = new KeyValue<>(next.getKey(), null);
      if(next.getValue() != null){
        try{
          toRet.setValue(getValue(next.getValue()));
        }
        catch(IOException e){
          CoreLog.L().log(Level.WARNING, "could not iterate", e);

        }
      }
      return toRet;
    }

    @Override
    public void remove() throws UnsupportedOperationException{
      throw new UnsupportedOperationException();
    }
  }
  
  

}
