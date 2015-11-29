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
