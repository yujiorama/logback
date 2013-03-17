package org.slf4j.impl;

import ch.qos.logback.classic.corpus.CorpusModel;
import ch.qos.logback.core.contention.*;
import org.junit.Before;
import org.junit.Test;


/**
 * Created with IntelliJ IDEA.
 * User: ceki
 * Date: 17.03.13
 * Time: 18:46
 * To change this template use File | Settings | File Templates.
 */
public class SimpleLoggerFactoryPerfTest {
  static int THREAD_COUNT = 10;
    int totalTestDuration = 4000;

    SimpleLoggerFactory loggerContext = new SimpleLoggerFactory();

    ThreadedThroughputCalculator harness = new ThreadedThroughputCalculator(totalTestDuration);
    RunnableWithCounterAndDone[] runnableArray = buildRunnableArray();

    CorpusModel corpusMaker;

    @Before
    public void setUp() throws Exception {
    }

    private RunnableWithCounterAndDone[] buildRunnableArray() {
      RunnableWithCounterAndDone[] runnableArray = new  RunnableWithCounterAndDone[THREAD_COUNT];
      for(int i = 0; i < THREAD_COUNT; i++) {
        runnableArray[i] = new GetLoggerRunnable();
      }
      return runnableArray;
    }

    // Results computed on a Intel i7
    // 1 thread
    // 13'107 ops per milli using Hashtable for LoggerContext.loggerCache
    // 15'258 ops per milli using ConcurrentHashMap for LoggerContext.loggerCache

    // 10 threads
    //  8'468 ops per milli using Hashtable for LoggerContext.loggerCache
    // 58'945 ops per milli using ConcurrentHashMap for LoggerContext.loggerCache


    @Test
    public void computeResults() throws InterruptedException {
      harness.execute(runnableArray);
      harness.printThroughput("getLogger performance: ", true);
    }

    private class GetLoggerRunnable extends RunnableWithCounterAndDone {

      final int burstLength = 3;
      public void run() {
        while (!isDone()) {
          long i = counter % burstLength;

          loggerContext.getLogger("a");
          counter++;
          if(i == 0) {
            Thread.yield();
          }
        }
      }
    }
}
