/**
 * 
 */
package org.mellowtech.core.collections.hmap;
import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.collections.BMap;

/**
 * @author msvens
 *
 */
public class EHTableBuilder {

  public static final int DEFAULT_MAX_BUCKETS = 1024 * 1024 * 2;
  public static final int DEFAULT_BUCKET_SIZE = 1024;


  private int maxBuckets = DEFAULT_MAX_BUCKETS;
  private int bucketSize = DEFAULT_BUCKET_SIZE;
  private boolean inMemory = false;
  private boolean forceNew = false;
  private boolean blobValues = false;

  public EHTableBuilder maxBuckets(int max) {
    this.maxBuckets = max;
    return this;
  }

  public EHTableBuilder bucketSize(int size) {
    this.bucketSize = size;
    return this;
  }

  public EHTableBuilder blobValues(boolean blobs) {
    this.blobValues = blobs;
    return this;
  }

  public EHTableBuilder inMemory(boolean inMemory){
    this.inMemory = inMemory;
    return this;
  }
  public EHTableBuilder forceNew(boolean force){
    this.forceNew = force;
    return this;
  }

  public final <A,B extends BComparable<A,B>, C, D extends BStorable<C,D>> BMap <A,B,C,D> 
  build(Class<B> keyType, Class<D> valueType, String fileName) throws Exception{
    if(blobValues) return buildBlob(keyType, valueType, fileName);
    EHTableImp <A,B,C,D> toRet = null;
    //first try to open
    try {
      toRet = new EHTableImp <> (fileName, keyType, valueType, inMemory);
    } catch (Exception e){
      return new EHTableImp <> (fileName, keyType, valueType, inMemory, bucketSize, maxBuckets);
    }
    if(!forceNew) return toRet;

    //delete old and create new:
    toRet.delete();
    return new EHTableImp <> (fileName, keyType, valueType, inMemory, bucketSize, maxBuckets);
  }

  private final <A,B extends BComparable<A,B>,C,D extends BStorable<C,D>> BMap <A,B,C,D> 
  buildBlob(Class <B> keyType, Class<D> valueType, String fileName) throws Exception{
    BMap <A,B,C,D> toRet = null;
    //first try to open
    try {
      toRet = new EHBlobTableImp <> (fileName, keyType, valueType, inMemory);
    } catch (Exception e){
      return new EHBlobTableImp <> (fileName, keyType, valueType, inMemory, bucketSize, maxBuckets);
    }
    if(!forceNew) return toRet;

    //delete old and create new:
    toRet.delete();
    return new EHBlobTableImp <> (fileName, keyType, valueType, inMemory, bucketSize, maxBuckets);
  }


}
