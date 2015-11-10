/**
 * 
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
