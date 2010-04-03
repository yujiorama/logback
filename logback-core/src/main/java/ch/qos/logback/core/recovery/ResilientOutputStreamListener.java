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
package ch.qos.logback.core.recovery;

import java.io.OutputStream;

public interface ResilientOutputStreamListener {

  /**
   * ResilientOutputStream instance will fire this event each time the
   * underlying output stream of the instance is changed.
   */
  void outputStreamChangedEvent(OutputStream os);
  
  void presumedInError();
}
