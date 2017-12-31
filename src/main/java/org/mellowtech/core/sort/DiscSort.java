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

package org.mellowtech.core.sort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 4.0.0
 * @param <A> key type
 */
public interface DiscSort <A> {


  /**
   * Sorts an inputfile and prints it to a designated outputfile. If these are
   * the same the inputfile will be overwritten.
   *
   * @param input
   *          File to sort
   * @param output
   *          Ouputfile
   * @return the number of objects sorted.
   */
  default int sort(Path input, Path output){
    try(SeekableByteChannel in = Files.newByteChannel(input, StandardOpenOption.READ);
        SeekableByteChannel out = Files.newByteChannel(output, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW)){
      return sort(in, out);
    }
    catch (IOException e) {
      throw new IllegalArgumentException("could not create input/output channel", e);
    }
  }

  /**
   * Sorts a input stream and print the result to a designated output stream. If
   * these are the same the input channel will be overwritten. This method
   * simply creates appropriate channesl for the input and output and calls sort
   * on Channels.
   *
   * @param input
   *          input stream
   * @param output
   *          output stream
   * @return number of objects sorted
   * @see #sort(ReadableByteChannel, WritableByteChannel)
   */
  default int sort(InputStream input, OutputStream output){
    return sort(Channels.newChannel(input), Channels.newChannel(output));
  }

  /**
   * Sorts a byte channel and print the result to a designated byte channel. If
   * these are the same the input channel will be overwritten.
   *
   * @param input
   *          input channel
   * @param output
   *          output channel
   * @return the number of objects sorted
   */
  int sort(ReadableByteChannel input, WritableByteChannel output);
}
