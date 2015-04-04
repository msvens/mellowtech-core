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
