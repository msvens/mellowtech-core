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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;

public class NativeLibLoader {
  
  private static NativeLibLoader libLoader = null;
  
  private TreeSet <String> paths;
  
  private NativeLibLoader(){
    paths = new TreeSet <String> ();
  }
  
  
  private static NativeLibLoader ins(){
    if(libLoader == null)
      libLoader = new NativeLibLoader();
    return libLoader;
  }
  
  public static void addPath(String path){
    NativeLibLoader.ins().paths.add(path);
  }
  
  public static Iterator <String> getPaths(){
    return NativeLibLoader.ins().paths.iterator();
  }
  
  
  public static void load(String resource, String libName) throws Exception{
    NativeLibLoader.load(resource, libName, null);
  }
  
  public static void load(String resource, String libName, ClassLoader cl) throws Exception{
    //first try to load the library in the normal way
    
    try{
      System.loadLibrary(libName);
      return;
    }
    catch(Error e){
      ;
    }
    try{
      loadAsResource(resource, libName, cl);
    }
    catch(Exception e){
      CoreLog.L().log(Level.WARNING, "Could not load library", e);
    }
    //now try to load it with the paths:
    NativeLibLoader ins = NativeLibLoader.ins();
    String fn = new File(resource).getName();
    for(String str : ins.paths){
      try{
        String fName = str+"/"+fn;
        
        if(new File(fName).exists()){
          System.load(fName);
          return;
        }
      }
      catch(Error e){
        ;
      }
    }
    throw new Exception("Could not load library: "+resource+" "+libName);
  }
  
  public static void loadAsResource(String resource, String libName) throws Exception{
    NativeLibLoader.load(resource, libName, null);
  }
  
  public static void loadAsResource(String resource, String libName, ClassLoader cl) throws Exception{
    File tmpLib;
    try{
      if(cl == null)
        cl = NativeLibLoader.ins().getClass().getClassLoader();
      BufferedInputStream input =
        new BufferedInputStream(cl.getResourceAsStream(resource));
      if(input == null)
        throw new Exception("Could not find resource");
      tmpLib = File.createTempFile(libName, ".dll");
      tmpLib.deleteOnExit(); // ensure deletion
      BufferedOutputStream bos = new BufferedOutputStream(
          new FileOutputStream(tmpLib));
      int b;
      while((b = input.read()) != -1){
        bos.write(b);
      }
      input.close();
      bos.close();
    }
    catch(Exception e){
      throw new Exception(e);
    }
      try{
        System.load(tmpLib.getCanonicalPath());
      }
      catch(Error e){
        throw new Exception(e.toString());
      }
  }
}
