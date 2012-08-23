package org.dummy;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SLF4JInvocation {

  @Test
  public void basic() {
    Logger logger = LoggerFactory.getLogger("basic-test");
    logger.debug("HELLO");
  }
}
