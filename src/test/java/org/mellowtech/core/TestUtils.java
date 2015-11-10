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

package org.mellowtech.core;

import java.io.File;
import java.net.URL;
import java.util.*;

import org.mellowtech.core.bytestorable.CBString;
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
  public static CBString[] randomWords(String chars, int maxWordLength, int maxBytes){
    Random r = new Random();
    StringBuilder sb = new StringBuilder();
    List<CBString> words = new ArrayList<>();
    Set<CBString> dups = new TreeSet<CBString>();
    int totalBytes = 0;
    while(totalBytes < maxBytes){
      sb.setLength(0);
      int wlength = r.nextInt(maxWordLength) + 1;
      for(int i = 0; i < wlength; i++){
        sb.append(chars.charAt(r.nextInt(chars.length())));
      }
      CBString tmp = new CBString(sb.toString());
      if(!dups.contains(tmp)) {
        totalBytes += tmp.byteSize();
        words.add(tmp);
        dups.add(tmp);
      }
    }
    //System.out.println(totalBytes);
    return words.toArray(new CBString[]{});
  }

  public static String[] randomStrings(String chars, int maxWordLength, int maxBytes){
    CBString[] cbstrings = randomWords(chars, maxWordLength, maxBytes);
    String[] toRet = new String[cbstrings.length];
    for(int i = 0; i < cbstrings.length; i++){
      toRet[i] = cbstrings[i].get();
    }
    return toRet;
  }

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
