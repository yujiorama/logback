package org.slf4j.impl;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.ILoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: ceki
 * Date: 17.03.13
 * Time: 18:46
 * To change this template use File | Settings | File Templates.
 */
public class SimpleLoggerFactory {
  final static SimpleLoggerFactory INSTANCE = new SimpleLoggerFactory();

  //ConcurrentMap<String, Logger> loggerMap;
  Map loggerMap;


   public SimpleLoggerFactory() {
     loggerMap = new HashMap();
     //loggerMap = new ConcurrentHashMap<String, Logger>();

   }

   /**
    * Return an appropriate {@link SimpleLogger} instance by name.
    */
   public Logger getLogger(String name) {
//     Logger simpleLogger = loggerMap.get(name);
//       if (simpleLogger != null) {
//         return simpleLogger;
//       } else {
//         Logger newInstance = new SimpleLogger(name);
//         Logger oldInstance = loggerMap.putIfAbsent(name, newInstance);
//         return oldInstance == null ? newInstance : oldInstance;
//       }

     Logger slogger = null;
    // protect against concurrent access of the loggerMap
     synchronized (this) {
       slogger = (Logger) loggerMap.get(name);
       if (slogger == null) {
         slogger = new SimpleLogger(name);
         loggerMap.put(name, slogger);
       }
     }
     return slogger;
   }

   void reset() {
     loggerMap.clear();
   }
}
