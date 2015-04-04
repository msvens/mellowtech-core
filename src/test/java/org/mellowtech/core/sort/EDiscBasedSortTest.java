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

package org.mellowtech.core.sort;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.sort.EDiscBasedSort;
import org.mellowtech.core.util.DelDir;
import org.mellowtech.core.util.Platform;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Date: 2013-01-20
 * Time: 16:55
 *
 * @author Martin Svensson
 */
public class EDiscBasedSortTest {

  public static final boolean LONG_TEXT = true;
  public static ByteBuffer stringBuffer;
  public static ArrayList <String> stringList;

  @Before public void before() throws Exception{

    //CBString.setLocale(new Locale("sv"));
    String file = LONG_TEXT ? "longText.txt" : "shortText.txt";

    URL url = this.getClass().getResource("/com/mellowtech/core/sort/"+file);
    File f = new File(url.getFile());
    BufferedReader br = new BufferedReader(new FileReader(f));
    Pattern p = Pattern.compile("\\W+");
    Scanner s = new Scanner(f);
    s.useDelimiter(p);
    String line;
    stringList = new ArrayList();
    int charlen = 0;
    CBString tmpStr = new CBString();
    int i = 0;
    while(s.hasNext() && i++ < 10000){
      String str = s.next();
      tmpStr.set(str);
      charlen += tmpStr.byteSize();
      stringList.add(str);
    }
    stringBuffer = ByteBuffer.allocate(charlen);
    for(String str : stringList){
      tmpStr.set(str);
      tmpStr.toBytes(stringBuffer);
    }
    File sortDir = new File(Platform.getTempDir()+"/sort");
    sortDir.mkdirs();
  }

  @After
  public void after(){
    DelDir.d(Platform.getTempDir()+"/sort");
  }

  @Test public void testQuickSort() throws Exception{
    EDiscBasedSort edb = new EDiscBasedSort(new CBString(), new CBString(),
    1, Platform.getTempDir()+"/sort");
    //stringBuffer.flip();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    edb.sort(new ByteArrayInputStream(stringBuffer.array()), bos, edb.getBlockSize()*1);

    //verify that things are the same
    //Collator col = Collator.getInstance(new Locale("sv"));
    Collections.sort(stringList);
    CBString tStr = new CBString();
    bos.flush();
    ByteBuffer sorted =  ByteBuffer.wrap(bos.toByteArray());
    for(String str : stringList){

      tStr.fromBytes(sorted, false);
      Assert.assertEquals(str, tStr.get());
    }
  }
}
