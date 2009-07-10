package edu.stanford.hci.helpmeout;

import java.util.HashMap;

import bsh.EvalError;
import bsh.Interpreter;

import com.googlecode.jj1.ServiceProxy;


public class HelpMeOutExceptionTracker {

  private ExceptionInfo eInfo;
  private String source;
  
  // make it a Singleton
  private static HelpMeOutExceptionTracker instance = new HelpMeOutExceptionTracker();
  private ServiceProxy proxy = new ServiceProxy(HelpMeOut.SERVICE_URL);
  private HelpMeOutExceptionTracker() {}
  public static HelpMeOutExceptionTracker getInstance() {
    return instance;
  }

  /** Set current source code - have to do this separately from exception reporting since we don't have access to source from RehearsePApplet (check this) */
  public void setSource(String source) {
    this.source=source;
  }
  
  /** record the runtime exception that just occurred */
  public void processRuntimeException(EvalError err,Interpreter i) {
    eInfo = new ExceptionInfo(err,i,source);
  }
  
  /** mark the previously recorded runtime exception as resolved */
  public void resolveRuntimeException() {
    System.out.println("Hooray, the exception was resolved!");
    //TODO: Store the fix in the HelpMeOut database.
    // old source and new source
    // error line
    // type of exception
  }
  
  /** given the old, broken source and Exception info we have in this object,
   * which line in the new source passed in as argument corresponds to the line
   * that last threw an exception and that we should watch?
   * @param newSource the source code that was changed by the user after the exception happened
   * @return the line in newSource that rehearse should watch
   */
  public int getLineToWatch() {
    assert(eInfo!=null);
    
    // compute character offset in source - make sure we're on the right line 
    diff_match_patch d = new diff_match_patch();
    int oldCharIndex = getCharIndexFromLine(eInfo.getSourceCode(), eInfo.getExceptionLineNum());
    int newCharIndex = d.diff_xIndex(d.diff_main(eInfo.getSourceCode(), source), oldCharIndex);
    int newLineIndex = getLineFromCharIndex(source,newCharIndex);
    return newLineIndex;
  }
  
  private int getCharIndexFromLine(String source, int line) {
    int newlinesToConsume = line-1;
    int charIndex = 0;
    while (newlinesToConsume > 0) {
      charIndex++;
      if (source.charAt(charIndex) == '\n') newlinesToConsume--;
    }
    if (line > 1) charIndex++; // move past the newline
    return charIndex;
  }

  private int getLineFromCharIndex(String newSource, int newCharIndex) {
    int newLine = 0;
    for (int i = 0; i < newCharIndex; i++) {
      if (newSource.charAt(i) == '\n') newLine++;
    }
    return newLine+1; // lines of code aren't 0-indexed
  }
  
  /**
   * 
   * @return true if an exception occurred and we're trying to see if it was fixed, false if no exception occurred in past
   */
  public boolean hasExceptionOccurred() {
    return eInfo!=null;
  }
  
  public boolean notifyLineReached(int line, int executionCount) {
    return (eInfo.getExecutionCount() == executionCount);
  }
}
