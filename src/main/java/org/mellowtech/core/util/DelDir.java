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





import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * DelDir is a simple utility for deleting non empty directories using Files.walkFileTree
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @see java.nio.file.Files#walkFileTree(Path, FileVisitor)
 */
public class DelDir {


  final static Logger logger = LoggerFactory.getLogger(DelDir.class);
  /**
   * Deletes all files including the given path
   * @param path directory to delete
   * @return true if no exception was thrown
   */
  public static boolean d(Path path){
    return d(path, false);
  }

  /**
   * Delete all files in the given path
   * @param path directory to empty
   * @param excludeTop - don't delete the top most directory
   * @return true if no exception was thrown
   */
  public static boolean d(Path path, boolean excludeTop){
    Path top = path;
    try {
      Files.walkFileTree(top, new SimpleFileVisitor<Path>() {

        @Override
        public FileVisitResult visitFile(Path file,
                                         BasicFileAttributes attrs) throws IOException {
          logger.debug("deleting file: {}", file);
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir,
                                                  IOException exc) throws IOException {
          logger.debug("Deleteding dir: {}", dir);
          if (exc == null) {
            if(!excludeTop || !dir.equals(top))
              Files.delete(dir);
            return FileVisitResult.CONTINUE;
          } else {
            throw exc;
          }
        }

      });
    } catch (IOException e) {
      logger.warn("",e);
      return false;
    }
    return true;
  }

  /**
   * Delete all files in the given path
   * @param path directory to empty
   * @param excludeTop - don't delete the top most directory
   * @return true if no exception was thrown
   */
  public static boolean d(String path, boolean excludeTop){
    return d(Paths.get(path), excludeTop);
  }

  /**
   * Deletes all files including the given path
   * @param path directory to delete
   * @return true if no exception was thrown
   */
  public static boolean d(String path) {
    return d(path,false);
  }
}
