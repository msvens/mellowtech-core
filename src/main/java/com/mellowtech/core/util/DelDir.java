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

package com.mellowtech.core.util;





import com.mellowtech.core.CoreLog;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;

/**
 * Date: 2012-12-31
 * Time: 16:58
 *
 * @author Martin Svensson
 */
public class DelDir {

  public static boolean d(String path){
    Path dir = Paths.get(path);
    try {
      Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

        @Override
        public FileVisitResult visitFile(Path file,
                                         BasicFileAttributes attrs) throws IOException {

          CoreLog.L().finer("Deleting file: " + file);
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir,
                                                  IOException exc) throws IOException {

          CoreLog.L().finer("Deleting dir: " + dir);
          if (exc == null) {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
          } else {
            throw exc;
          }
        }

      });
    } catch (IOException e) {
     CoreLog.L().log(Level.WARNING, "", e);
    }
    return true;
  }
}
