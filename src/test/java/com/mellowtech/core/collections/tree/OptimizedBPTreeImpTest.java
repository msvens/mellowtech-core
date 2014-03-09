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
package com.mellowtech.core.collections.tree;

import com.mellowtech.core.TestUtils;
import com.mellowtech.core.bytestorable.CBInt;
import com.mellowtech.core.bytestorable.CBString;
import com.mellowtech.core.collections.KeyValue;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeMap;


/**
 * @author Martin Svensson
 */
public class OptimizedBPTreeImpTest {

  public OptimizedBPTreeImp<CBString, CBInt> dbMap;

  public static final String dir = "dbmtest";
  public static final String name = "optDiscBasedMap";
  public TestTree tt;


  @Before
  public void before() throws Exception {
    TestUtils.createTempDir(dir);

    String fileName = TestUtils.getAbsolutDir(dir+"/"+name);
    this.dbMap = new OptimizedBPTreeImp(fileName,  new CBString(), new CBInt(), 1024, 1024);
    tt = new TestTree(dbMap);

  }

  @Test
  public void doTest() throws Exception {
    tt.insert();
    tt.testContains();
    tt.testValues();
    tt.testDeleteHalf();
    tt.testSimpleDelete();
    tt.testValues();
    tt.testIterator();
    dbMap.save();
    String fileName = TestUtils.getAbsolutDir(dir+"/"+name);
    dbMap = new OptimizedBPTreeImp(fileName,  new CBString(), new CBInt());
    tt.setDbMap(dbMap);
    tt.testSize();
    tt.testValues();
    tt.testDeleteAll();
  }

  @After
  public void after() throws Exception {

    this.dbMap.save();
    this.dbMap.delete();
    TestUtils.deleteTempDir(dir);
  }


}
