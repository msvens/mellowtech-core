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
package org.mellowtech.core.util;

/**
 * Get size of memory (VM, supplied size) as a human-readable string.
 * 
 * @author rickard.coster@asimus.se
 *
 */
@Deprecated
public class ByteSizeUtils {
  
  public static final String getSizeString(long bytes) {
    if (bytes < 0)
      return "";
    if (bytes < KB)
      return "" + bytes + " B";
    else if (bytes < MB)
      return "" + DataTypeUtils.fmt((double) bytes / KB) + " KB";
    else if (bytes < GB)
      return "" + DataTypeUtils.fmt((double) bytes / MB) + " MB";
    else
      return "" + DataTypeUtils.fmt((double) bytes / GB) + " GB";
  }
  
  static final long KB = 1024;
  static final long MB = 1024 * 1024;
  static final long GB = 1024 * 1024 * 1024;
  
  public static final String getVMMemoryString() {
    Runtime r = Runtime.getRuntime();
    return "mem(free, total, max)=\t" + getSizeString(r.freeMemory()) + "\t" 
      + getSizeString(r.totalMemory()) + "\t" + getSizeString(r.maxMemory());
  }
}
