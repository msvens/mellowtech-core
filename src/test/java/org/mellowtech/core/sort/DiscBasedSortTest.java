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

package org.mellowtech.core.sort;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mellowtech.core.codec.StringCodec;
import org.mellowtech.core.sort.DiscBasedSort;
import org.mellowtech.core.util.DelDir;
import org.mellowtech.core.util.Platform;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Date: 2013-01-20
 * Time: 16:55
 *
 * @author Martin Svensson
 */
public class DiscBasedSortTest {

  public static final boolean LONG_TEXT = true;
  public static ByteBuffer stringBuffer;
  public static ArrayList <String> stringList;
  public static StringCodec codec = new StringCodec();

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
    stringList = new ArrayList <>();
    int charlen = 0;
    String tmpStr;
    int i = 0;
    while(s.hasNext() && i++ < 10000){
      String str = s.next();
      charlen += codec.byteSize(str);
      stringList.add(str);
    }
    stringBuffer = ByteBuffer.allocate(charlen);
    for(String str : stringList){
      codec.to(str, stringBuffer);
    }
    System.out.println(Platform.getTempDir()+"sort");
    File sortDir = new File(Platform.getTempDir()+"sort");
    sortDir.mkdirs();
  }

  @After
  public void after(){
    DelDir.d(Platform.getTempDir()+"sort");
  }

  @Test public void testQuickSort() throws Exception{
    DiscBasedSort <String> edb = new DiscBasedSort <> (new StringCodec(), 0, Platform.getTempDir()+"sort");
    //stringBuffer.flip();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ByteArrayInputStream bis = new ByteArrayInputStream(stringBuffer.array());
    Channel c = Channels.newChannel(bis);
    Channel co = Channels.newChannel(bos);
    int num = edb.sort((ReadableByteChannel)c, (WritableByteChannel) co, DiscBasedSort.getBlockSize()*4);
    //edb.sort(Channels.newChannel(c, bos, edb.getBlockSize()*1);

    //verify that things are the same
    //Collator col = Collator.getInstance(new Locale("sv"));
    Collections.sort(stringList);
    String tStr;
    bos.flush();
    ByteBuffer sorted =  ByteBuffer.wrap(bos.toByteArray());
    System.out.println(num+" "+stringList.size());
    for(String str : stringList){
      tStr = codec.from(sorted);
      //System.out.println(str+" "+tStr);
      Assert.assertEquals(str, tStr);
    }
  }
}
