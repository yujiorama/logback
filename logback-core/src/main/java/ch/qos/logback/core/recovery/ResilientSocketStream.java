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

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAware;

public class ResilientSocketStream extends ResilientOutputStreamBase implements
    ContextAware {

  int port;
  InetAddress inetAddress;

  public ResilientSocketStream(InetAddress inetAddress, int port) {
    super();
    this.inetAddress = inetAddress;
    this.port = port;
    try {
      this.os = openNewOutputStream();
      this.presumedClean = true;
    } catch (IOException e) {
      postIOFailure(e);
    }
  }

  public ResilientSocketStream(InetAddress inetAddress, int port, ResilientOutputStreamListener listener, Context context) {
    super();
    this.inetAddress = inetAddress;
    this.port = port;
    setContext(context);
    addResilientOutputStreamListener(listener);
    try {
      this.os = openNewOutputStream();
      this.presumedClean = true;
    } catch (IOException e) {
      postIOFailure(e);
    }
  }

  
  
  final public boolean isPresumedInError() {
    return (recoveryCoordinator != null && !presumedClean);
  }

  @Override
  String getDescription() {
    return "socket stream [" + inetAddress.getHostName() + ":" + port + "]";
  }

  @Override
  public OutputStream openNewOutputStream() throws IOException {
    Socket socket = new Socket(inetAddress, port);
    return socket.getOutputStream();
  }

  @Override
  public String toString() {
    return "c.q.l.c.recovery.ResilientSocketStream@"
        + System.identityHashCode(this);
  }
}
