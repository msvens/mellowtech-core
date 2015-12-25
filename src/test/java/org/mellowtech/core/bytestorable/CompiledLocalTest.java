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

package org.mellowtech.core.bytestorable;

import junit.framework.Assert;

import org.junit.Test;
import org.mellowtech.core.util.CompiledLocale;

import java.util.Locale;

/**
 * Date: 2013-01-14
 * Time: 21:58
 *
 * @author Martin Svensson
 */
public class CompiledLocalTest {

  @Test
  public void test() {
    CompiledLocale cl = new CompiledLocale();
    char[] enc = cl.getCompiledLocale(new Locale("us"));
    Assert.assertTrue(enc.length > 0);
  }
}
