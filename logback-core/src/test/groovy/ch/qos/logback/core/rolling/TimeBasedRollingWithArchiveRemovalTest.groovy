package ch.qos.logback.core.rolling

import ch.qos.logback.core.Context
import ch.qos.logback.core.ContextBase
import ch.qos.logback.core.encoder.EchoEncoder
import org.junit.Before
import ch.qos.logback.core.testUtil.RandomUtil
import ch.qos.logback.core.util.CoreTestConstants
import org.junit.Test
import ch.qos.logback.core.rolling.helper.RollingCalendar
import scala.Array
import java.util.concurrent.TimeUnit

/**
 * Created with IntelliJ IDEA.
 * User: ceki
 * Date: 06.09.12
 * Time: 12:30
 * To change this template use File | Settings | File Templates.
 */
class TimeBasedRollingWithArchiveRemovalTest {
  def Context context  = new ContextBase()
  def EchoEncoder<Object> encoder = new EchoEncoder<Object>()

  def String MONTHLY_DATE_PATTERN = "yyyy-MM"

  def String MONTHLY_CRONOLOG_DATE_PATTERN = "yyyy/MM"
  final def String DAILY_CRONOLOG_DATE_PATTERN  = "yyyy/MM/dd"

  def long MILLIS_IN_MINUTE  = 60 * 1000
  def long MILLIS_IN_HOUR  = 60 * MILLIS_IN_MINUTE
  def long MILLIS_IN_DAY  = 24 * MILLIS_IN_HOUR
  def long MILLIS_IN_MONTH = (long) ((365.242199 / 12) * MILLIS_IN_DAY)
  def long  MONTHS_IN_YEAR = 12

  def int diff
  def String randomOutputDir;
  def int slashCount = 0

   // by default tbfnatp is an instance of DefaultTimeBasedFileNamingAndTriggeringPolicy
 def TimeBasedFileNamingAndTriggeringPolicy tbfnatp = new DefaultTimeBasedFileNamingAndTriggeringPolicy()

 def now = System.currentTimeMillis()


 @Before
 def void setUp() {
   context.setName("test")
   diff = RandomUtil.getPositiveInt()
   randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/"
 }

 def int computeSlashCount(String datePattern) {
   if (datePattern == null) 0
   else datePattern.count('/')
 }

    // test that the number of files at the end of the test is same as the expected number taking into account end dates
  // near the beginning of a new year. This test has been run in a loop with start date varying over a two years
  // with success.
  @Test def void monthlyRolloverOverManyPeriods() {

    slashCount = computeSlashCount(MONTHLY_CRONOLOG_DATE_PATTERN)
    def numPeriods = 40
    def maxHistory = 2
    def fileNamePattern = randomOutputDir + "/%d{" + MONTHLY_CRONOLOG_DATE_PATTERN + "}/clean.txt.zip"
    def (startTime, endTime) = logOverMultiplePeriods(now, fileNamePattern, MILLIS_IN_MONTH, maxHistory, numPeriods)
    def differenceInMonths = RollingCalendar.diffInMonths(startTime, endTime)
    def startTimeAsCalendar = Calendar.getInstance()
    startTimeAsCalendar.setTimeInMillis(startTime)
    def indexOfStartPeriod = startTimeAsCalendar.get(Calendar.MONTH)
    def withExtraFolder = extraFolder(differenceInMonths, MONTHS_IN_YEAR, indexOfStartPeriod, maxHistory)


    //StatusPrinter.print(context)
    check(expectedCountWithFolders())
  }


  def void waitForCompression(TimeBasedRollingPolicy<Object> tbrp) {
    if (tbrp.future != null && !tbrp.future.isDone()) {
      tbrp.future.get(1000, TimeUnit.MILLISECONDS)
    }
  }

  def  logOverMultiplePeriods(long currentTime, String fileNamePattern, long periodDurationInMillis, int maxHistory,
                 int simulatedNumberOfPeriods, int startInactivity = 0,
                 int numInactivityPeriods) {
    def startTime = currentTime
    def rfa_tbrp = buildRollingFileAppender(currentTime, fileNamePattern, maxHistory)
    def ticksPerPeriod = 512
    def runLength = simulatedNumberOfPeriods * ticksPerPeriod
    def startInactivityIndex = 1 + startInactivity * ticksPerPeriod
    def endInactivityIndex = startInactivityIndex + numInactivityPeriods * ticksPerPeriod
    def tickDuration = periodDurationInMillis / ticksPerPeriod

    for (int i: 0..runLength) {
      if (i < startInactivityIndex || i > endInactivityIndex) {
        rfa.doAppend("Hello ----------------------------------------------------------" + i)
      }
      tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(addTime(tbrp.timeBasedFileNamingAndTriggeringPolicy.getCurrentTime, tickDuration))
      if (i % (ticksPerPeriod / 2) == 0) {
        waitForCompression(tbrp)
      }
    }

    println("Last date" + new Date(tbrp.timeBasedFileNamingAndTriggeringPolicy.getCurrentTime()));
    waitForCompression(tbrp)
    rfa.stop
    [startTime, tbrp.timeBasedFileNamingAndTriggeringPolicy.getCurrentTime]
  }


  def int expectedCountWithFolders() {
    def numLogFiles = (maxHistory + 1)
    def numLogFilesAndFolders = numLogFiles * 2
    def int result = numLogFilesAndFolders + slashCount
    if (withExtraFolder) result += 1
    result
  }

  def genericFindMatching(matchFunc, File dir, List<File> fileList, String pattern, boolean includeDirs = false) {
    if (dir.isDirectory()) {
      def  File[] matchArray = dir.listFiles(new FileFilter() {
        def boolean accept(File f) {
          return f.isDirectory() || matchFunc(f, pattern)
        }
      })
      for (File f: matchArray) {
        if (f.isDirectory()) {
          if (includeDirs) fileList += f
          genericFindMatching(matchFunc, f, fileList, pattern, includeDirs)
        } else
          fileList += f
      }
    }
  }


  def void findAllFoldersInFolderRecursively(File dir, List<File> fileList) {
    genericFindMatching({f, p -> false}, dir, fileList, null, true);
  }

  def void findAllDirsOrStringContainsFilesRecursively(File dir , List<File> fileList, String pattern) {
    genericFindMatching({f, p -> f.getName.contains(p)}, dir, fileList, pattern, true)
  }

  def void  findFilesInFolderRecursivelyByPatterMatch(File dir, List<File> fileList, String pattern) {
    genericFindMatching({f, p -> f.getName.matches(p)}, dir, fileList, pattern);
  }

  def void check(int expectedCount) {
    def File dir  = new File(randomOutputDir)
    def fileList = new ArrayList<File>()
    findAllDirsOrStringContainsFilesRecursively(dir, fileList, "clean")
    assertEquals(expectedCount, fileList.size)
  }

}
