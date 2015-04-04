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

package org.mellowtech.core.disc.blockfile;

import junit.framework.Assert;

import org.junit.Test;
import org.mellowtech.core.disc.blockfile.MemoryFit;

/**
 * Date: 2013-01-26
 * Time: 11:35
 *
 * @author Martin Svensson
 */
public class MemoryFitTest {


  @Test
  public void test() {

    MemoryFit f = new MemoryFit();
    f.set(1, 100);
    f.set(2, 200);
    f.set(3, 300);
    f.set(4, 400);
    f.set(5, 500);

    Assert.assertEquals(3, f.firstFit(150));
    Assert.assertEquals(2, f.bestFit(150));
    Assert.assertEquals(4, f.bestFit(400));
    Assert.assertEquals(4, f.firstFit(400));

    f.clear(2);
    Assert.assertEquals(3, f.firstFit(150));
    Assert.assertEquals(3, f.bestFit(150));


    f.clear(5);


    f.set(3, 1000);
    f.set(17, 350);
  Assert.assertEquals(17, f.bestFit(350));
    Assert.assertEquals(3, f.bestFit(900));
    Assert.assertEquals(4, f.firstFit(390));

  }
}
