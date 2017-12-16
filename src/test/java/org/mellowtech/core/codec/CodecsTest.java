package org.mellowtech.core.codec;

/**
 * @author Martin Svensson
 * @since 2017-01-30
 */

import org.junit.Assert;
import org.junit.Test;

public class CodecsTest {

  @Test public void getStringCodec(){
    Assert.assertTrue(Codecs.fromClass(String.class) instanceof StringCodec);
    Assert.assertTrue(Codecs.type("") instanceof StringCodec);
  }

  @Test public void getChangeStringCodec(){
    Codecs.addMapping(String.class, new StringCodec2());
    Assert.assertTrue(Codecs.fromClass(String.class) instanceof StringCodec2);
    Assert.assertTrue(Codecs.type("") instanceof StringCodec2);
    Codecs.clearMappings();
  }
}
