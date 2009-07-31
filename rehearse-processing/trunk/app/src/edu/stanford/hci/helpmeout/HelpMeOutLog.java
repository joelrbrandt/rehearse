package edu.stanford.hci.helpmeout;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class HelpMeOutLog {
  
  /**
   * Canonical list of possible messages to log.
   * If the variable name ends in "FOR" then it 
   * expects to be followed by additional information.
   */
  public static final String CLICKED_COMPILED_RUN = "Clicked on compiled run";
  public static final String CLICKED_INTERACTIVE_RUN = "Clicked on interactive run";
  public static final String COMPILE_BROKEN_FOR = "Compilation broken: ";
  public static final String COMPILE_FIXED = "Compilation fixed: saving fix to database";
  public static final String COMPILE_FIXED_ALREADY = "Compilation fixed: nothing to do";
  public static final String EXCEPTION_BROKEN_FOR = "Runtime exception: ";
  public static final String EXCEPTION_FIXED = "Runtime exception fixed: saving fix to database";
  public static final String QUERY_SUCCESS_FOR = "Query successful for: ";
  public static final String QUERY_FAIL = "Query failure: couldn't query or wrong type returned";
  public static final String STORE_FAIL_COMPILE = "Couldn't store compilation error";
  public static final String STORE_FAIL_EXCEPTION = "Couldn't store runtime exception";
  public static final String STORE_FAIL_NULL= "Store called with at least one null argument";
  public static final String CLICKED_COPY_FOR = "Clicked on copy link for id: ";
  public static final String CLICKED_VOTE_UP_FOR = "Clicked on vote up link for id: ";
  public static final String CLICKED_VOTE_DOWN_FOR = "Clicked on vote down link for id: ";
  public static final String AUTO_PATCH_SUCCESS = "Auto-patching successful";
  public static final String AUTO_PATCH_FAIL = "Auto-patching failed";
  public static final String VOTE_FAIL = "Voting failed: unable to call errorvote servicemethod";
  public static final String VOTE_FAIL_UNRECOGNIZED = "Voting failed: unable to recognize error type";
  public static final String PROGRAM_FINISHED = "PROGRAM FINISHED";
  
  // make it a Singleton
  private static HelpMeOutLog instance = new HelpMeOutLog();
  private HelpMeOutLog(){
    logNewSession();
  }
  public static HelpMeOutLog getInstance() {
    return instance;
  }
  
  private StringBuffer log = new StringBuffer();
  private StringBuffer out = new StringBuffer();
  private StringBuffer err = new StringBuffer();
  
  private boolean writeToStdOut = true;
  
  public void write(String text) {
    text = formatText(text);
    out.append(text);
    log.append(text);
    if (writeToStdOut) {
      System.out.println(text);
    }
  }
  
  public void writeError(String text) {
    text = formatText(text);
    err.append(text);
    log.append(text);
    if (writeToStdOut) {
      System.err.println(text);
    }
  }
  
  public void saveToFile(String filename) {
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(filename, true)); // true appends to log file
      out.write(log.toString());
      out.close();
    } catch (IOException e) {
      System.out.println("Error writing HelpMeOut log file");
    }
  }
  
  public boolean didProgramFinish() {
    String logarr[] = log.toString().split("\n");
    String last = logarr[logarr.length-1];
    return last.contains(PROGRAM_FINISHED);
  }
  
  private String formatText(String text) {
    text = (new Date()).toString().concat("  "+text);
    text = text.concat("\n");
    return text;
  }
  
  private void logNewSession() {
    Date now = new Date();
    log.append("\n\n--------------------------\n");
    log.append("NEW SESSION - " + now.toString());
    log.append("\n--------------------------\n\n");
  }
  
  public StringBuffer getLog() {
    return log;
  }
  
  public String getLogAsString() {
    return log.toString();
  }
  
  public String toString() {
    return log.toString();
  }
}
