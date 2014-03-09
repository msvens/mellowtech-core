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

package com.mellowtech.core.bytestorable.ext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.bytestorable.ext.AutoByteStorable;
import com.mellowtech.core.util.CompResult;
import com.mellowtech.core.util.Compressor;



/** 
 * ByteStorable class that automatically reads/writes all instances of 
 * (non-private and not null) ByteStorable fields in the object using 
 * Java reflection, and also compresses the byteStorable output. 
 * <p>
 * The intended usage is to subclass CompressedAutoByteStorable 
 * so that all (non-private and non-null)  fields that are instances of 
 * ByteStorable are written (compressed) in toBytes() and read in fromBytes(). 
 * <p>
 * It is assumed that the bytestorable elements are not null. Therefore, you 
 * must ensure that the fields are non-null at all times.
 * 
 * @see com.mellowtech.core.bytestorable.ext.AutoByteStorable
 * @author rickard.coster@asimus.se
 *
 */
@Deprecated
public class CompressedAutoByteStorable extends AutoByteStorable {

  private static Compressor compressor = new Compressor();
   
  @Override
   public int byteSize() {
     int superByteSize = super.byteSize();
     ByteBuffer bb = ByteBuffer.allocate(superByteSize);
     super.toBytes(bb);
     byte[] bytes = new byte[superByteSize];
     bb.flip();
     bb.get(bytes);
     byte[] compressed = null;
     try {
       compressed = compressor.compress(bytes, 0, bytes.length);
     } catch (IOException e) {
       CoreLog.L().log(Level.WARNING, "", e);
       return -1;
     }
     return 4 + compressed.length;
   }

   @Override
   public int byteSize(ByteBuffer bb) {
     int position = bb.position();
     int size = bb.getInt();
     bb.position(position);
     return 4 + size;
   }

   @Override
   public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
     int size = bb.getInt();
     byte[] bytes = new byte[size];
     bb.get(bytes);
     CompResult decompressed =  null;
     try {
       decompressed = compressor.decompress(bytes, 0, bytes.length);
     } catch (IOException e) {
       CoreLog.L().log(Level.WARNING, "", e);
       return null;
     }
     ByteBuffer bb2 = ByteBuffer.wrap(decompressed.getBuffer());
     return super.fromBytes(bb2, doNew);
   }

   @Override
   public void toBytes(ByteBuffer bb) {
     int superByteSize = super.byteSize();
     ByteBuffer bb2 = ByteBuffer.allocate(superByteSize);
     super.toBytes(bb2);
     byte[] bytes = new byte[superByteSize];
     bb2.flip();
     bb2.get(bytes);
     byte[] compressed = null;
     try {
       compressed = compressor.compress(bytes, 0, bytes.length);
     } catch (IOException e) {
       CoreLog.L().log(Level.SEVERE, "", e);
     }
     bb.putInt(compressed.length);
     bb.put(compressed);
   }

   
 }

