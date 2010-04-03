/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
// Contributors: Dan MacDonald <dan@redknee.com>
package ch.qos.logback.core.net;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;

import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.ObjectOutputStreamEncoder;
import ch.qos.logback.core.recovery.ResilientSocketStream;
import ch.qos.logback.core.spi.PreSerializationTransformer;

/**
 * 
 * This is the base class for module specific SocketAppender implementations.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */

public abstract class SocketAppenderBase<E> extends
    UnsynchronizedAppenderBase<E> {

  /**
   * The default port number of remote logging server (4560).
   */
  static final int DEFAULT_PORT = 4560;

  /**
   * We remember host name as String in addition to the resolved InetAddress so
   * that it can be returned via getOption().
   */
  protected String host;

  protected int port = DEFAULT_PORT;

  ObjectOutputStreamEncoder<Serializable> encoder = new ObjectOutputStreamEncoder<Serializable>();

  /**
   * Start this appender.
   */
  public void start() {
    InetAddress inetAddress = null;

    if (host == null) {
      addError("No remote address was configured for appender"
          + name
          + " For more information, please visit http://logback.qos.ch/codes.html#socket_no_host");
      return;
    } else {
      inetAddress = getAddressByName(host);
      if (inetAddress == null) {
        return;
      }
    }

    ResilientSocketStream ros = new ResilientSocketStream(inetAddress, port,
        encoder, context);

    try {
      encoder.init(ros);
    } catch (IOException e) {
      ros.addError("Failed to open [" + inetAddress.getHostName() + ":" + port
          + "]", e);
    }
    super.start();
  }

  InetAddress getAddressByName(String host) {
    try {
      return InetAddress.getByName(host);
    } catch (Exception e) {
      addError("Could not find address of [" + host + "].", e);
      return null;
    }
  }

  /**
   * Strop this appender.
   * 
   * <p>
   * This will mark the appender as closed and call then {@link #cleanUp}
   * method.
   */
  @Override
  public void stop() {
    if (!isStarted())
      return;

    encoder.stop();
    this.started = false;
  }

  @Override
  protected void append(E event) {
    if (event == null)
      return;
    if (!isStarted()) {
      return;
    }

    try {
      postProcessEvent(event);
      Serializable serEvent = getPST().transform(event);
      encoder.doEncode(serEvent);
    } catch (IOException e) {
    }
  }

  protected abstract void postProcessEvent(E event);

  protected abstract PreSerializationTransformer<E> getPST();

  /**
   * The <b>RemoteHost</b> option takes a string value which should be the host
   * name of the server where a {@link SocketNode} is running.
   */
  public void setRemoteHost(String host) {
    this.host = host;
  }

  /**
   * Returns value of the <b>RemoteHost</b> option.
   */
  public String getRemoteHost() {
    return host;
  }

  /**
   * The <b>Port</b> option takes a positive integer representing the port where
   * the server is waiting for connections.
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * Returns value of the <b>Port</b> option.
   */
  public int getPort() {
    return port;
  }

}
