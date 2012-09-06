/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2010, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.rolling

import ch.qos.logback.core.encoder.EchoEncoder
import org.junit.Before
import ch.qos.logback.core.util.StatusPrinter
import ch.qos.logback.core.util.CoreTestConstants
import ch.qos.logback.core.util.Compare

import static org.junit.Assert.assertTrue
import org.junit.Test

import static org.junit.Assert.fail
import ch.qos.logback.core.testUtil.Env

class TimeBasedRollingTest extends RollingScaffolding {
  private RollingFileAppender<Object> rfa1 = new RollingFileAppender<Object>()
  private TimeBasedRollingPolicy<Object> tbrp1 = new TimeBasedRollingPolicy<Object>()
  private RollingFileAppender<Object> rfa2 = new RollingFileAppender<Object>()
  private TimeBasedRollingPolicy<Object> tbrp2 = new TimeBasedRollingPolicy<Object>()

  private EchoEncoder<Object> encoder = new EchoEncoder<Object>()

  @Before
  def void setUp() {
    setUpScaffolding()
  }

  private def void initRFA(RollingFileAppender<Object> rfa, String filename) {
    rfa.setContext(context)
    rfa.setEncoder(encoder)
    if (filename != null) {
      rfa.setFile(filename)
    }
  }

  private def void initTRBP(RollingFileAppender<Object> rfa, TimeBasedRollingPolicy<Object> tbrp,
                            String filenamePattern, long givenTime) {
    tbrp.setContext(context)
    tbrp.setFileNamePattern(filenamePattern)
    tbrp.setParent(rfa)
    tbrp.timeBasedFileNamingAndTriggeringPolicy = new DefaultTimeBasedFileNamingAndTriggeringPolicy<Object>()
    tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(givenTime)
    rfa.setRollingPolicy(tbrp)
    tbrp.start()
    rfa.start()
  }

  def genericTest = { checkFunction, String testId, String patternPrefix, String compressionSuffix, boolean fileOptionIsSet, int waitDuration ->
    def withCompression = compressionSuffix.length() > 0
    def fileName = null
    if (fileOptionIsSet)
      fileName = testId2FileName(testId);
    initRFA(rfa1, fileName);

    def fileNamePatternStr = randomOutputDir + patternPrefix + "-%d{" + DATE_PATTERN_WITH_SECONDS + "}" + compressionSuffix

    initTRBP(rfa1, tbrp1, fileNamePatternStr, currentTime);

    // compute the current filename
    addExpectedFileName_ByDate(fileNamePatternStr, getMillisOfCurrentPeriodsStart());

    incCurrentTime(1100);
    tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);

    for (int i : 0..2) {
      rfa1.doAppend("Hello---" + i);
      addExpectedFileNamedIfItsTime_ByDate(fileNamePatternStr) //, withCompression && (i != 2))
      incCurrentTime(500);
      tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime)
      if (withCompression)
        waitForCompression(tbrp1)
    }
    rfa1.stop()

    if (waitDuration != NO_RESTART) {
      doRestart(testId, patternPrefix, fileOptionIsSet, waitDuration);
    }

    println("before")
    expectedFilenameList.each { f -> println(f)}

    massageExpectedFilesToCorresponToCurrentTarget(fileName, fileOptionIsSet)

    println("after")
    expectedFilenameList.each { f -> println(f)}


    StatusPrinter.print(context)
    checkFunction(testId, withCompression, compressionSuffix)
  }


  def defaultCheck = {String testId, boolean withCompression, String compressionSuffix ->
    def i = 0;
    for (String fn : expectedFilenameList) {
      def String suffix = ""
      if (withCompression)
        suffix = addGZIfNotLast(i, compressionSuffix)
      def String witnessFileName = CoreTestConstants.TEST_DIR_PREFIX + "witness/rolling/tbr-" + testId + "." + i.toString() + suffix
      assertTrue(Compare.compare(fn, witnessFileName));
      i += 1
    }
  }

  def zCheck = {String testId, boolean withCompression, String compressionSuffix ->
    def lastFile = expectedFilenameList.last()
    def String witnessFileName = CoreTestConstants.TEST_DIR_PREFIX + "witness/rolling/tbr-" + testId
    println(lastFile+"  "+witnessFileName)
    assertTrue(Compare.compare(lastFile, witnessFileName));
  }


  // defaultTest uses the defaultCheck function
  def defaultTest = genericTest.curry(defaultCheck)

  def doRestart(String testId, String patternPart, boolean fileOptionIsSet, int waitDuration) {
    // change the timestamp of the currently actively file
    def File activeFile = new File(rfa1.getFile())
    activeFile.setLastModified(currentTime)

    incCurrentTime(waitDuration)

    def filePatternStr = randomOutputDir + patternPart + "-%d{" + DATE_PATTERN_WITH_SECONDS + "}"

    def fileName = null
    if (fileOptionIsSet)
      fileName = testId2FileName(testId);
    initRFA(rfa2, fileName)
    initTRBP(rfa2, tbrp2, filePatternStr, currentTime)
    for (int i : 0..2) {
      rfa2.doAppend("World---" + i)
      addExpectedFileNamedIfItsTime_ByDate(filePatternStr)
      incCurrentTime(100)
      tbrp2.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime)
    }
    rfa2.stop();
  }

  def int NO_RESTART = 0
  def int WITH_RESTART = 1
  def int WITH_RESTART_AND_LONG_WAIT = 2000

  @Test
  def void noCompression_FileBlank_NoRestart_1() {
    defaultTest("test1", "test1", "", FILE_OPTION_BLANK, NO_RESTART)
  }

  @Test
  def void withCompression_FileBlank_NoRestart_2() {
    defaultTest("test2", "test2", ".gz", FILE_OPTION_BLANK, NO_RESTART);
  }

  @Test
  def void noCompression_FileBlank_StopRestart_3() {
    defaultTest("test3", "test3", "", FILE_OPTION_BLANK, WITH_RESTART);
  }
  @Test
  def void noCompression_FileSet_StopRestart_4()  {
    defaultTest("test4", "test4", "", FILE_OPTION_SET, WITH_RESTART);
  }

  @Test
  def void noCompression_FileSet_StopRestart_WithLongWait_4B() {
    defaultTest("test4B", "test4B", "", FILE_OPTION_SET, WITH_RESTART_AND_LONG_WAIT);
  }

  @Test
  def void noCompression_FileSet_NoRestart_5() {
    defaultTest("test5", "test6", "", FILE_OPTION_SET, NO_RESTART);
  }


  @Test
  def void withCompression_FileSet_NoRestart_6() {
    defaultTest("test6", "test6", ".gz", FILE_OPTION_SET, NO_RESTART);
  }

  // LBCORE-169
  @Test
  def void withMissingTargetDirWithCompression() {
    defaultTest("test7", "%d{yyyy-MM-dd, aux}/", ".gz", FILE_OPTION_SET, NO_RESTART);
  }

  @Test
  def void withMissingTargetDirWithZipCompression() {
    defaultTest("test8", "%d{yyyy-MM-dd, aux}/", ".zip", FILE_OPTION_SET, NO_RESTART);
  }


  @Test
  def void failed_rename() {
    if(!Env.isWindows()) return

    FileOutputStream fos = null
    try {
      def fileName = testId2FileName("failed_rename");
      def file = new File(fileName)
      file.getParentFile().mkdirs()

      fos = new FileOutputStream(fileName)
      genericTest(zCheck, "failed_rename", "failed_rename", "", FILE_OPTION_SET, NO_RESTART)

    } finally {
      if(fos != null) fos.close();
    }
  }
}
