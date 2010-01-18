package edu.stanford.hci.processing;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import processing.app.Base;
import processing.app.Sketch;

public class RehearseLogger {
  
  public static final boolean LOG_RUN_FEEDBACK = false;
  
  public enum EventType { 
    CODE_PASTE, 
    COMPILED_RUN,
    INTERACTIVE_RUN,
    COMPILED_RUN_FEEDBACK,
    INTERACTIVE_RUN_FEEDBACK,
    COMPILE_ERROR,
    RUNTIME_ERROR,
    RUN_FEEDBACK,
    SAVE,
    SAVE_AS, 
    OPEN
  };
  
  private static final String DELIMITER = "[*** ";
  
  private static RehearseLogger instance;
  private Logger logger;

  public static String getLogFilePath() {
    return Base.getSketchbookFolder().getAbsolutePath() + File.separator + "rehearse.log";
  }
  
  private RehearseLogger() {
    String logFilePath = RehearseLogger.getLogFilePath(); 

    try {
      FileHandler fileHandler = new FileHandler(logFilePath, true);
      fileHandler.setFormatter(new Formatter() {
        public String format(LogRecord rec) {
          StringBuffer buf = new StringBuffer();
          buf.append(DELIMITER);
          buf.append(new java.util.Date() + " ***] ");
          buf.append(formatMessage(rec));
          buf.append("\r\n");
          return buf.toString();
        }
      });
      logger = Logger.getLogger("edu.stanford.hci.processing.RehearseLogger");
      logger.addHandler(fileHandler);
      logger.setUseParentHandlers(false);
    } catch (IOException e) {
      System.err.println("Log file could not be written at " + logFilePath);
    }
  }
  
  public static RehearseLogger getInstance() {
    if (instance == null) {
      instance = new RehearseLogger();
    }
    
    return instance;
  }
  
  public void log(EventType type, Sketch sketch, String message) {
    StringBuffer buf = new StringBuffer();
    buf.append(type.toString());
    if (sketch != null) {
      buf.append(" " + sketch.getName() + " ");
      buf.append(sketch.getFolder().getAbsolutePath());
    }
    buf.append("\r\n" + message);
    logger.info(buf.toString());
    for (Handler h : logger.getHandlers()) {
      if (h instanceof FileHandler) {
        ((FileHandler) h).flush();
      }
    }
  }
}
