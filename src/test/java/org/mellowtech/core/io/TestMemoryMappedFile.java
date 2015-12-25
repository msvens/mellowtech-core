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

package org.mellowtech.core.io;

import java.io.File;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mellowtech.core.TestUtils;

/**
 * @author msvens
 *
 */
public class TestMemoryMappedFile {
  
  public static final String dir = "dbmtest";
  public static final String name = "memMappedFile.txt";


  @Before
  public void before() throws Exception {
    TestUtils.createTempDir(dir);

  }

  @Test public void testFile() throws Exception{
   
    String tmpFile = TestUtils.getAbsolutDir(dir+"/"+name);
    File f = new File(tmpFile);
    if(f.exists()) f.delete();
    //f.createNewFile();
    f.createNewFile();
    
    long l = (long) (1024l*1024l*1024l*3l);
    MemoryMappedFile mmf = new MemoryMappedFile(tmpFile, l);
    mmf.putByte(l-1, (byte)'a');
    
    mmf = new MemoryMappedFile(tmpFile, l);
    char c = (char) mmf.getByte(l-1);
    Assert.assertEquals('a', c);
  }
  
  
  @After
  public void after() throws Exception {
    TestUtils.deleteTempDir(dir);
  }

}
