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
package com.mellowtech.core.bytestorable.ext;

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.bytestorable.CBString;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.logging.Level;


@Deprecated
public class CBURI extends ByteStorable {
  private URI uri;

  public CBURI() {
    setURI(null);
  }

  public CBURI(URI uri) {
    setURI(uri);
  }

  public void setURI(URI uri) {
    this.uri = uri;
  }

  public URI getURI() {
    return uri;
  }

  public String toString() {
    return uri.toString();
  }

  public int hashCode() {
    return uri.toString().hashCode();
  }

  public ByteStorable fromBytes(ByteBuffer bb, boolean doNew) {
    CBURI o = doNew ? new CBURI() : this;
    CBString uriString = (CBString) new CBString().fromBytes(bb);
    o.uri = null;
    try {
      o.uri = new URI(uriString.get());
    }
    catch (URISyntaxException e) {
      CoreLog.L().log(Level.WARNING, "", e);
    }
    return o;
  }

  public void toBytes(ByteBuffer bb) {
    CBString uriString = new CBString(uri.toString());
    uriString.toBytes(bb);
  }

  public int byteSize() {
    CBString uriString = new CBString(uri.toString());
    return uriString.byteSize();
  }

  public int byteSize(ByteBuffer bb) {
    return new CBString().byteSize(bb);
  }

}
