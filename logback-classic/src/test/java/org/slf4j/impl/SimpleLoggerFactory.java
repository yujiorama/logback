package org.slf4j.impl;


import java.util.HashMap;
import java.util.Map;

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

   Map loggerMap;

   public SimpleLoggerFactory() {
     loggerMap = new HashMap();
   }

   /**
    * Return an appropriate {@link SimpleLogger} instance by name.
    */
   public Logger getLogger(String name) {
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
