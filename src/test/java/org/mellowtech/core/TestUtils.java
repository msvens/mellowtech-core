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

package org.mellowtech.core;

import java.io.File;
import java.net.URL;
import java.util.*;
import org.mellowtech.core.codec.StringCodec;
import org.mellowtech.core.util.DelDir;
import org.mellowtech.core.util.Platform;

/**
 * Date: 2013-01-27
 * Time: 11:14
 *
 * @author Martin Svensson
 */
public class TestUtils {

  /**
   *
   * @param chars
   * @param maxWordLength
   * @param maxBytes
   * @return
   */
  public static String[] randomWords(String chars, int maxWordLength, int maxBytes){
    StringCodec codec = new StringCodec();
    Random r = new Random();
    StringBuilder sb = new StringBuilder();
    List<String> words = new ArrayList<>();
    Set<String> dups = new TreeSet<>();
    int totalBytes = 0;
    while(totalBytes < maxBytes){
      sb.setLength(0);
      int wlength = r.nextInt(maxWordLength) + 1;
      if(wlength == 1)
        wlength++;
      for(int i = 0; i < wlength; i++){
        sb.append(chars.charAt(r.nextInt(chars.length())));
      }
      String tmp = sb.toString();
      if(!dups.contains(tmp)) {
        totalBytes += codec.byteSize(tmp);
        words.add(tmp);
        dups.add(tmp);
      }
    }
    //System.out.println(totalBytes);
    return words.toArray(new String[]{});
  }

  /*public static String[] randomStrings(String chars, int maxWordLength, int maxBytes){
    CBString[] cbstrings = randomWords(chars, maxWordLength, maxBytes);
    String[] toRet = new String[cbstrings.length];
    for(int i = 0; i < cbstrings.length; i++){
      toRet[i] = cbstrings[i].get();
    }
    return toRet;
  }*/

  public static String getAbsolutDir(String dir){
     return new File(Platform.getTempDir()+"/"+dir).getAbsolutePath();
  }

  public static File getTempFile(String dir, String fname){
    String tempDir = Platform.getTempDir();
    if(dir != null)
      return new File(tempDir+"/"+dir+"/"+fname);
    else
      return new File(tempDir+"/"+fname);
  }

  public static File getTempFile(String fname){
    return getTempFile(null, fname);
  }

  public static void deleteTempDir(String dir){
    String tempDir = Platform.getTempDir();
    DelDir.d(tempDir+"/"+dir);
  }

  public static void createTempDir(String dir){
    File f = new File(Platform.getTempDir() + "/" + dir);
    f.mkdir();
  }

  public static File getResourceAsFile(String resource){
    URL url = TestUtils.class.getResource("/"+resource);
    return new File(url.getFile());
  }

  public static File getResourceAsFile(String pkg, String resource){
    if(pkg == null)
      return getResourceAsFile(resource);
    if(pkg.contains("."))
      pkg = pkg.replace('.', '/');
    if(!pkg.startsWith("/"))
      pkg = "/" + pkg;
    return getResourceAsFile(pkg + "/" + resource);
  }
}
