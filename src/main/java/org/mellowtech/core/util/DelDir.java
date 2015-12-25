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





import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;

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
