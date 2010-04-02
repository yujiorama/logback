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
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.PreSerializationTransformer;

/**
 * 
 * This is the base class for module specific SocketAppender implementations.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */

public abstract class SocketAppenderBase<E> extends AppenderBase<E> {

  /**
   * The default port number of remote logging server (4560).
   */
  static final int DEFAULT_PORT = 4560;

  /**
   * The default reconnection delay (30000 milliseconds or 30 seconds).
   */
  static final int DEFAULT_RECONNECTION_DELAY = 30000;

  /**
   * We remember host name as String in addition to the resolved InetAddress so
   * that it can be returned via getOption().
   */
  protected String host;

  protected int port = DEFAULT_PORT;
  protected ObjectOutputStream oos;
  protected int reconnectionDelay = DEFAULT_RECONNECTION_DELAY;

  protected int counter = 0;

  /**
   * Start this appender.
   */
  public void start() {
    int errorCount = 0;
    if (port == 0) {
      errorCount++;
      addError("No port was configured for appender"
          + name
          + " For more information, please visit http://logback.qos.ch/codes.html#socket_no_port");
    }

    if (host == null) {
      errorCount++;
      addError("No remote address was configured for appender"
          + name
          + " For more information, please visit http://logback.qos.ch/codes.html#socket_no_host");
    }

    //connect(address, port);

    if (errorCount == 0) {
      this.started = true;
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

    this.started = false;
    cleanUp();
  }

  /**
   * Drop the connection to the remote host and release the underlying connector
   * thread if it has been created
   */
  public void cleanUp() {
    if (oos != null) {
      try {
        oos.close();
      } catch (IOException e) {
        addError("Could not close oos.", e);
      }
      oos = null;
    }
  }


  @Override
  protected void append(E event) {

    if (event == null)
      return;

//    if (address == null) {
//      addError("No remote host is set for SocketAppender named \""
//          + this.name
//          + "\". For more information, please visit http://logback.qos.ch/codes.html#socket_no_host");
//      return;
//    }

    if (oos != null) {
      try {
        postProcessEvent(event);
        Serializable serEvent = getPST().transform(event);
        oos.writeObject(serEvent);
        oos.flush();
        if (++counter >= CoreConstants.OOS_RESET_FREQUENCY) {
          counter = 0;
          // Failing to reset the object output stream every now and
          // then creates a serious memory leak.
          // System.err.println("Doing oos.reset()");
          oos.reset();
        }
      } catch (IOException e) {
        if (oos != null) {
          try {
            oos.close();
          } catch (IOException ignore) {
          }
        }

        oos = null;
        addWarn("Detected problem with connection: " + e);
      }
    }
  }

  protected abstract void postProcessEvent(E event);
  protected abstract PreSerializationTransformer<E> getPST();


  protected static InetAddress getAddressByName(String host) {
    try {
      return InetAddress.getByName(host);
    } catch (Exception e) {
      // addError("Could not find address of [" + host + "].", e);
      return null;
    }
  }

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
   * The <b>Port</b> option takes a positive integer representing the port
   * where the server is waiting for connections.
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
