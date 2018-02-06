package org.mellowtech.core.codec;

/**
 * @author Martin Svensson
 * @since 2017-01-30
 */

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Codecs should")
public class CodecsTest {

  @Test void getStringCodec(){
    assertTrue(Codecs.fromClass(String.class) instanceof StringCodec);
    assertTrue(Codecs.type("") instanceof StringCodec);
  }

  @Test void getChangeStringCodec(){
    Codecs.addMapping(String.class, new StringCodec2());
    assertTrue(Codecs.fromClass(String.class) instanceof StringCodec2);
    assertTrue(Codecs.type("") instanceof StringCodec2);
    Codecs.clearMappings();
  }
}
