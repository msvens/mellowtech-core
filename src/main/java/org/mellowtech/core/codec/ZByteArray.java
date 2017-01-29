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

package org.mellowtech.core.codec;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author msvens
 * @since 2017-01-29
 */
public class ZByteArray {

  private static LZ4Compressor compressor = LZ4Factory.fastestInstance().fastCompressor();
  private static LZ4FastDecompressor decompressor = LZ4Factory.fastestInstance().fastDecompressor();

  protected byte[] comp = null;
  protected int origLength;


  public ZByteArray(byte[] bytes){
    if (bytes == null || bytes.length == 0) {
      return;
    }
    origLength = bytes.length;
    comp = new byte[compressor.maxCompressedLength(origLength)];
    int compressLength = compressor.compress(bytes, comp);
    comp = Arrays.copyOf(comp, compressLength);
  }

  private ZByteArray(byte[] compressed, int origLength) {
    this.comp = compressed;
    this.origLength = origLength;
  }

  public byte[] uncompress(){
    if (comp == null) return null;
    byte[] dest = new byte[origLength];
    decompressor.decompress(comp, dest);
    return dest;
  }

  public byte[] getCompressedData(){return comp;}

  public int getOriginalLength(){return origLength;}

}
