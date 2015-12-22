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
package org.mellowtech.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Compress/decompress byte arrays using GZIPOutputStream and GZIPInputStream
 * 
 * @author rickard.coster@asimus.se
 */
@Deprecated
public class Compressor {

  /**
   * Compress byte array using GZIP.
   * 
   * @param input
   *          the byte array to compress
   * @param offset
   *          the offset
   * @param length
   *          the data length
   * @return a newly allocated byte array containing the compressed data
   * @throws IOException
   *           if a write error occurs.
   */
  public byte[] compress(byte[] input, int offset, int length)
      throws IOException {
    ByteArrayOutputStream bao = new ByteArrayOutputStream();
    GZIPOutputStream gz = new GZIPOutputStream(bao);
    gz.write(input, offset, length);
    gz.finish();
    byte[] compressed = bao.toByteArray();
    return compressed;
  }

  private static final int DECOMPRESSION_CHUNK = 4096;

  /**
   * Decompress compressed byte array using GZIP.
   * 
   * @param compressedData
   *          the compressed data.
   * @param offset
   *          the offset.
   * @param length
   *          the compressed data length.
   * @return a newly allocated byte array containing the decompressed data.
   * @throws IOException
   *           if a read error occurs.
   */
  public CompResult decompress(byte[] compressedData, int offset, int length)
      throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(compressedData, offset,
        length);
    byte[] decompressed = new byte[DECOMPRESSION_CHUNK];
    return decompress(compressedData, offset, length, decompressed);
  }

  /**
   * Decompress compressed byte array using GZIP.
   * 
   * @param compressedData
   *          the compressed data.
   * @param offset
   *          the offset.
   * @param length
   *          the compressed data length.
   * @param decompressionBuffer 
   *          a pre-allocated buffer for decompression, can be discarded and a new
   *          array created if it is too small for decompressing the compressedData.          
   * @return the supplied array, or a newly allocated byte array containingthe decompressed data.
   * @throws IOException
   *           if a read error occurs.
   */
  public CompResult decompress(byte[] compressedData, int offset, int length,
      byte[] decompressionBuffer)
      throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(compressedData, offset,
        length);
    GZIPInputStream gz = new GZIPInputStream(bis);
    int read = 0;
    int start = offset;
    while (read >= 0) {
      read = gz.read(decompressionBuffer, offset, DECOMPRESSION_CHUNK);

      if (read > 0 && (decompressionBuffer.length - offset) <= DECOMPRESSION_CHUNK)
        decompressionBuffer = ArrayUtils.setSize(decompressionBuffer, 
            decompressionBuffer.length + DECOMPRESSION_CHUNK);
      else if (read < 0)
        break;
      offset += read;
    }
    return new CompResult(offset-start, decompressionBuffer);
  }

  private static String printBytes(byte[] bytes, int offset, int length) {
    StringBuffer sb = new StringBuffer();
    sb.append(length + "\t");
    for (int i = 0; i < length; i++) {
      sb.append(bytes[offset + i] + " ");
      if (i > 0 && (i % 80) == 0)
        sb.append(Platform.getLineSeparator());
    }
    return sb.toString();
  }
}
