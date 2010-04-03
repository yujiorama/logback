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
import ch.qos.logback.core.recovery.ResilientOutputStreamListener;

public class ObjectOutputStreamEncoder<E> extends EncoderBase<E> implements
    ResilientOutputStreamListener {

  ObjectOutputStream oos;
  OutputStream os;
  protected int counter = 0;
  
  boolean presumedInError = false;
  
  @Override
  public void init(OutputStream os) throws IOException {
    this.os = os;
    oos = new ObjectOutputStream(os);
  }

  public void doEncode(E event) throws IOException {
    // if in error then provoke recovery
    if (presumedInError) {
      os.write(0);
      return;
    }
    
    oos.writeObject(event);
    oos.flush();
    if (++counter >= CoreConstants.OOS_RESET_FREQUENCY) {
      counter = 0;
      // Failing to reset the object output stream every now and
      // then creates a serious memory leak.
      oos.reset();
    }

  }

  public void close() {
    if (oos == null) {
      return;
    }
    try {
      oos.close();
    } catch (IOException e) {

    }
  }

  public void outputStreamChangedEvent(OutputStream os) {
    try {
      this.os = os;
      oos = new ObjectOutputStream(os);
      presumedInError = false;
    } catch (IOException e) {
    }
  }

  public void presumedInError() {
    presumedInError = true;
  }
}
