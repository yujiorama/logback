/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2010, QOS.ch. All rights reserved.
 * 
 * This program and the accompanying materials are dual-licensed under either
 * the terms of the Eclipse Public License v1.0 as published by the Eclipse
 * Foundation
 * 
 * or (per the licensee's choosing)
 * 
 * under the terms of the GNU Lesser General Public License version 2.1 as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.encoder;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import ch.qos.logback.core.CoreConstants;

public class ObjectOutputStreamEncoder<E> extends EncoderBase<E> {

  ObjectOutputStream oos;
  protected int counter = 0;

  @Override
  public void init(OutputStream os) throws IOException {
    oos = new ObjectOutputStream(os);
  }

  public void doEncode(E event) throws IOException {
    oos.writeObject(event);
    oos.flush();
    if (++counter >= CoreConstants.OOS_RESET_FREQUENCY) {
      counter = 0;
      // Failing to reset the object output stream every now and
      // then creates a serious memory leak.
      oos.reset();
    }

  }

  public void close() throws IOException {
    oos.close();
  }

}
