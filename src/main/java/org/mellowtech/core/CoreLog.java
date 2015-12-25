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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;

/**
 * Created with IntelliJ IDEA.
 * User: Martin Svensson
 * Date: 2012-10-20
 * Time: 17:11
 * To change this template use File | Settings | File Templates.
 */
public class CoreLog {
  private static CoreLog ourInstance = new CoreLog();

  public static CoreLog getInstance() {
    return ourInstance;
  }

  public static CoreLog I(){
    return getInstance();
  }

  private CoreLog() {
  }

  public Logger l(){
    return Logger.getLogger("org.mellowtech.core");
  }

  public static Logger L(){
    return I().l();
  }
  
  public static void setLevel(Level level){
    Logger logger = L();
    while(logger != null){
      logger.setLevel(level);
      for(Handler handler : logger.getHandlers())
        handler.setLevel(level);
      logger = logger.getParent();
    }
  }
}
