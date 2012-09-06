package ch.qos.logback.core.rolling

import ch.qos.logback.core.util.CachingDateFormatter
import ch.qos.logback.core.Context
import ch.qos.logback.core.ContextBase
import ch.qos.logback.core.status.StatusManager
import ch.qos.logback.core.testUtil.RandomUtil
import ch.qos.logback.core.util.CoreTestConstants
import ch.qos.logback.core.rolling.helper.FileNamePattern
import java.util.concurrent.TimeUnit

class RollingScaffolding {
  final String DATE_PATTERN_WITH_SECONDS = "yyyy-MM-dd_HH_mm_ss";
  final CachingDateFormatter SDF = new CachingDateFormatter(DATE_PATTERN_WITH_SECONDS)
  Context context = new ContextBase()
  StatusManager sm = context.getStatusManager()

  int diff = RandomUtil.getPositiveInt()

  protected long currentTime = 0L
  protected String randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/"
  Calendar cal = Calendar.getInstance()
  protected long nextRolloverThreshold = 0;
  protected List<String> expectedFilenameList = []

  boolean FILE_OPTION_SET = true
  boolean FILE_OPTION_BLANK = false

  def setUpScaffolding() {
    context.setName("test")
    cal.set(Calendar.MILLISECOND, 333)
    currentTime = cal.getTimeInMillis()
    recomputeRolloverThreshold(currentTime)
  }

  protected def void incCurrentTime(long increment) {
    currentTime += increment
  }

  protected def long getMillisOfCurrentPeriodsStart() {
    long delta = currentTime % 1000
    return (currentTime - delta)
  }

  protected def Date getDateOfCurrentPeriodsStart() {
    long delta = currentTime % 1000
    return new Date(currentTime - delta)
  }

  protected def void addExpectedFileName_ByDate(String patternStr, long millis) {
    def fileNamePattern = new FileNamePattern(patternStr, context)
    def String fn = fileNamePattern.convert(new Date(millis))
    println("fn=" + fn)
    expectedFilenameList.add(fn)
  }

  protected def void addExpectedFileName_ByFileIndexCounter(String randomOutputDir, String, testId, long millis,
                                                            int fileIndexCounter, String compressionSuffix) {
    String fn = randomOutputDir + testId + "-" + SDF.format(millis) + "-" + fileIndexCounter + ".txt" + compressionSuffix
    expectedFilenameList.add(fn)
  }


  protected def void addExpectedFileNamedIfItsTime_ByDate(String fileNamePatternStr) {
    if (passThresholdTime(nextRolloverThreshold)) {
      addExpectedFileName_ByDate(fileNamePatternStr, getMillisOfCurrentPeriodsStart())
      recomputeRolloverThreshold(currentTime)
    }
  }

  protected def boolean passThresholdTime(long nextRolloverThreshold) {
    return currentTime >= nextRolloverThreshold
  }

  def recomputeRolloverThreshold(long ct) {
    long delta = ct % 1000
    nextRolloverThreshold = (ct - delta) + 1000
  }

  def String addGZIfNotLast(int i, String suff) {
    int lastIndex = expectedFilenameList.size() - 1
    if (i != lastIndex) suff else ""
  }

  def void waitForCompression(TimeBasedRollingPolicy<Object> tbrp) {
    if (tbrp.future != null && !tbrp.future.isDone()) {
      tbrp.future.get(1000, TimeUnit.MILLISECONDS)
    }
  }

  def String testId2FileName(String testId) {
    return randomOutputDir + testId + ".log"
  }

  // =========================================================================
  // utility methods
  // =========================================================================
  def massageExpectedFilesToCorresponToCurrentTarget(String file, boolean fileOptionIsSet) {
    String last = expectedFilenameList[-1]
    expectedFilenameList = expectedFilenameList[0..-2]

    if (fileOptionIsSet) {
      expectedFilenameList.add(file)
    } else if (last.endsWith(".gz")) {
      def stem = last[0..-4]
      expectedFilenameList.add(stem)
    }
  }
}
