package edu.stanford.hci.helpmeout;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HelpMeOutLog {
  
  /** LOG FORMAT:
   * TIMESTAMP \t TYPE \t EVENT \t DETAIL
   * TIMESTAMP: Datetime as String
   * TYPE: TYPE_LOG or TYPE_ERR
   * EVENT: ONE OF CANONICAL MSGS BELOW
   * DETAIL: OPTIONAL ADDTL INFO (cannot contain \t)
   * 
   */
  /**
   * Canonical list of possible messages to log.
   * If the variable name ends in "FOR" then it 
   * expects to be followed by additional information.
   */
  public static final String STARTED_COMPILED_RUN = "Compiled run started.";
  public static final String STARTED_INTERACTIVE_RUN = "Interactive run started.";
  public static final String COMPILE_BROKEN = "Compilation broken.";
  public static final String COMPILE_FIXED = "Compilation succeeded: saving fix to database.";
  public static final String COMPILE_FIXED_ALREADY = "Compilation succeeded.";
  public static final String EXCEPTION_OCCURRED = "Runtime exception in user code.";
  public static final String EXCEPTION_FIXED = "Runtime exception fixed: saving fix to database.";
  public static final String QUERY_SUCCESS = "Query for Compile error succeeded.";
  public static final String QUERY_FAIL = "Query for Compile error failed.";
  public static final String STORE_FAIL_COMPILE = "Couldn't store compilation error.";
  public static final String STORE_FAIL_EXCEPTION = "Couldn't store runtime exception.";
  public static final String STORE_FAIL_NULL= "Store called with at least one null argument.";
  public static final String CLICKED_COPY_FIX = "Clicked on copy link.";
  public static final String CLICKED_VOTE_UP = "Clicked on vote up link.";
  public static final String CLICKED_VOTE_DOWN = "Clicked on vote down link.";
  public static final String AUTO_PATCH_SUCCESS = "Auto-patch succeeded.";
  public static final String AUTO_PATCH_FAIL = "Auto-patch failed.";
  public static final String VOTE_FAIL = "Voting failed: unable to call errorvote servicemethod.";
  public static final String VOTE_FAIL_UNRECOGNIZED = "Voting failed: unable to recognize error type.";
  public static final String PROGRAM_FINISHED = "PROGRAM FINISHED";
  public static final String NEW_SESSION = "--- NEW SESSION ---";
  public static final String QUERY_EMPTY = "Query returned no results.";
   
  public static final String QUERYEXCEPTION_FAIL = "Query for Exception failed.";
  public static final String QUERYEXCEPTION_SUCCESS = "Query for Exception succeeded.";
  public static final String QUERYEXCEPTION_EMPTY = "Query for Exception returned no results.";
  public static final String TOKENIZER_ERROR = "Tokenizing string during match calculation failed.";
  public static final String DIFF_MATCH_PATCH_ERROR = "diff_match_patch() failed.";
  public static final String CLICKED_MORE_DETAIL = "Clicked more detail link.";
  public static final String STORE_FAIL_IDELOG = "Storing IDE Log failed.";
  public static final String CLICKED_FIND_LINE = "Clicked find line link.";
  public static final String INFO = "Info"; //generic info message
  public static final String CLEANED_QUERY = "Cleaned up query.";

  public static final String TYPE_LOG = "LOG";
  public static final String TYPE_ERR = "ERR";

  
  SimpleDateFormat dateFormat =
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");

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

  /** write log message without detail */
  public void write( String event, String detail) {
    String text = formatText(TYPE_LOG,event,detail);
    out.append(text);
    log.append(text);
    if (writeToStdOut) {
      System.out.print(text);
    }
  }
  
  public void write(String event) {
   write(event,"");
  }
  
  /** write error without detail */
  public void writeError(String event,String detail) {
    String text = formatText(TYPE_ERR,event,detail);
    err.append(text);
    log.append(text);
    if (writeToStdOut) {
      System.err.print(text);
    }
  }
  public void writeError(String event) {
    writeError(event,"");
  }
  
  /** Write the current log to the database using service method storeidelog(log) */
  public void saveToDatabase() {
    //write log to database
    try {
     HelpMeOutServerProxy.getInstance().storeidelog(getLogAsString());
   } catch (Exception e) {
     System.err.println(STORE_FAIL_IDELOG+" "+e.getMessage());
   }
  }
  /** Write current log to the specified file; either appends or overwrites */
  public void saveToFile(String filename, boolean append) {
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(filename, append)); // true appends to log file
      out.write(log.toString());
      out.close();
    } catch (IOException e) {
      System.err.println("Error writing HelpMeOut log file");
    }
  }
  
  public boolean didProgramFinish() {
    String logarr[] = log.toString().split("\n");
    String last = logarr[logarr.length-1];
    return last.contains(PROGRAM_FINISHED);
  }
  
  private String formatText(String type, String event, String detail) {
    if(detail == null)
      detail = "";
    String text = dateFormat.format(new Date()) + "\t" + type + "\t"+event.replaceAll("[\t\n]", " ") + "\t" + detail.replaceAll("[\t\n]", " ") +"\n";
    return text;
  }
  
  private void logNewSession() {
    write(NEW_SESSION);
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
  
  //return last line of log
  public String tail() {
    String[] lines = log.toString().split("\n");
    if(lines.length<1) return null;
    return lines[lines.length-1];
  }

  //return last n lines of log
  public String tail(int n) {
    String[] lines = log.toString().split("\n");
    if(lines.length<n) return null;
    String merged="";
    for(int i=lines.length-(1+n);i<lines.length;i++) {
      merged = merged+lines[i]+"\n";
    }
    return merged;
  }
  
  //has any error been logged since we started?
  public boolean hasErrorOccurred() {
    return (err.length()!=0);
  }
  
  public String getErrorLogAsString() {
    return err.toString();
  }
  /** return all lines matching a given event type */
  public List<String> query(String eventType) {
    List<String> matches = new ArrayList<String>();
    String[] lines = log.toString().split("\n");
    for(int i=0; i<lines.length; i++) {
      String[] fields = lines[i].split("\t");
      if(eventType.equals(fields[2])) {
        matches.add(lines[i]);
      }
    }
    return matches;
  }
}
