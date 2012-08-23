package org.dummy;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class JCLInvocation {

  @Test
  public void basic() {
    Log log = LogFactory.getLog("basic-test");
    log.debug("HELLO");
  }
}
