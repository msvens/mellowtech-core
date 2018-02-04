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

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mellowtech.core.codec.StringCodec;
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

  @BeforeEach
  public void before() throws Exception{
    //CBString.setLocale(new Locale("sv"));
    String file = LONG_TEXT ? "longText.txt" : "shortText.txt";
    StringCodec codec = new StringCodec();
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
    File sortDir = Platform.getTempDir().resolve("sort").toFile();
    sortDir.mkdirs();
  }

  @AfterEach
  public void after(){
    DelDir.d(Platform.getTempDir().resolve("sort"));
  }

  @Test
  @DisplayName("disc based sort with byte coppare")
  public void testQuickSort() throws Exception{
    StringCodec codec = new StringCodec();
    EDiscBasedSort <String> edb = new EDiscBasedSort<>(codec,4096,4096*2,1,Platform.getTempDir().resolve("sort"));
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    edb.sort(new ByteArrayInputStream(stringBuffer.array()), bos);

    //verify that things are the same
    //Collator col = Collator.getInstance(new Locale("sv"));
    Collections.sort(stringList);
    bos.flush();
    ByteBuffer sorted =  ByteBuffer.wrap(bos.toByteArray());
    System.out.println("sorted capacity: "+sorted.capacity());
    String tStr;
    for(String str : stringList){
      tStr = codec.from(sorted);
      assertEquals(str, tStr);
    }
  }
}
