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
import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.ContextAwareImpl;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;

abstract public class ResilientOutputStreamBase extends OutputStream implements
    ContextAware {

  final static int STATUS_COUNT_LIMIT = 2 * 4;

  private int noContextWarning = 0;
  private int statusCount = 0;

  protected RecoveryCoordinator recoveryCoordinator;

  protected OutputStream os;
  protected boolean presumedClean = true;

  private ContextAwareImpl cai = new ContextAwareImpl(this);

  List<ResilientOutputStreamListener> listenerList = new ArrayList<ResilientOutputStreamListener>();

  final private boolean isPresumedInError() {
    // existence of recoveryCoordinator indicates failed state
    return (recoveryCoordinator != null && !presumedClean);
  }

  public void write(byte b[], int off, int len) {
    if (isPresumedInError()) {
      if (!recoveryCoordinator.isTooSoon()) {
        attemptRecovery();
      }
      return; // return regardless of the success of the recovery attempt
    }

    try {
      os.write(b, off, len);
      postSuccessfulWrite();
    } catch (IOException e) {
      postIOFailure(e);
    }
  }

  @Override
  public void write(int b) {
    if (isPresumedInError()) {
      if (!recoveryCoordinator.isTooSoon()) {
        attemptRecovery();
      }
      return; // return regardless of the success of the recovery attempt
    }
    try {
      os.write(b);
      postSuccessfulWrite();
    } catch (IOException e) {
      postIOFailure(e);
    }
  }

  @Override
  public void flush() {
    if (os != null) {
      try {
        os.flush();
        postSuccessfulWrite();
      } catch (IOException e) {
        postIOFailure(e);
      }
    }
  }

  abstract String getDescription();

  abstract OutputStream openNewOutputStream() throws IOException;

  final private void postSuccessfulWrite() {
    if (recoveryCoordinator != null) {
      recoveryCoordinator = null;
      statusCount = 0;
      addStatus(new InfoStatus("Recovered from IO failure on "
          + getDescription(), this));
    }
  }

  void postIOFailure(IOException e) {
    addStatusIfCountNotOverLimit(new ErrorStatus("IO failure while writing to "
        + getDescription(), this, e));
    presumedClean = false;
    if (recoveryCoordinator == null) {
      recoveryCoordinator = new RecoveryCoordinator();
      firePresumedInErrorChangedEvent();
    }
  }

  @Override
  public void close() throws IOException {
    if (os != null) {
      os.close();
    }
  }

  void attemptRecovery() {
    try {
      close();
    } catch (IOException e) {
    }

    addStatusIfCountNotOverLimit(new InfoStatus(
        "Attempting to recover from IO failure on " + getDescription(), this));

    // subsequent writes must always be in append mode
    try {
      os = openNewOutputStream();
      presumedClean = true;
      fireOutputStreamChangedEvent();
    } catch (IOException e) {
      addStatusIfCountNotOverLimit(new ErrorStatus("Failed to open "
          + getDescription(), this, e));
    }
  }

  void fireOutputStreamChangedEvent() {
    for (ResilientOutputStreamListener listener : listenerList) {
      listener.outputStreamChangedEvent(this);
    }
  }

  void firePresumedInErrorChangedEvent() {
    for (ResilientOutputStreamListener listener : listenerList) {
      listener.presumedInError();
    }
  }

  public void addResilientOutputStreamListener(
      ResilientOutputStreamListener listener) {
    listenerList.add(listener);
  }

  void addStatusIfCountNotOverLimit(Status s) {
    ++statusCount;
    if (statusCount < STATUS_COUNT_LIMIT) {
      addStatus(s);
    }

    if (statusCount == STATUS_COUNT_LIMIT) {
      addStatus(s);
      addStatus(new InfoStatus("Will supress future messages regarding "
          + getDescription(), this));
    }
  }

  public void addStatus(Status status) {
    if (cai.getContext() == null) {
      if (noContextWarning++ == 0) {
        System.out.println("LOGBACK: No context given for " + this);
      }
      return;
    }

    StatusManager sm = cai.getStatusManager();
    if (sm != null) {
      sm.add(status);
    }
  }

  public Context getContext() {
    return cai.getContext();
  }

  public void setContext(Context context) {
    cai.setContext(context);
  }

  public void addError(String msg) {
    cai.addError(msg);
  }

  public void addError(String msg, Throwable ex) {
    cai.addError(msg, ex);
  }

  public void addInfo(String msg) {
    cai.addInfo(msg);
  }

  public void addInfo(String msg, Throwable ex) {
    cai.addInfo(msg, ex);
  }

  public void addWarn(String msg) {
    cai.addWarn(msg);
  }

  public void addWarn(String msg, Throwable ex) {
    cai.addWarn(msg, ex);
  }
}
