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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;

@Deprecated
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
