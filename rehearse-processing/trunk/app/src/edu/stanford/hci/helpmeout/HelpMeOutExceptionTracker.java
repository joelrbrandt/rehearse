package edu.stanford.hci.helpmeout;

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
  
  /** given the old, broken source and Exception info we have in this object,
   * which line in the new source passed in as argument corresponds to the line
   * that last threw an exception and that we should watch?
   * @param newSource the source code that was changed by the user after the exception happened
   * @return the line in newSource that rehearse should watch
   */
  public int getLineToWatch(String newSource) {
    assert(eInfo!=null);

    // TODO: write this implementation. intuition: use diff/patch to find correspondence
    // here's some pseudo-code
    
    // compute character offset in source - make sure we're on the right line 
    // diff_match_patch d = new diff_match_patch();
    // int oldCharIndex = getCharIndexFromLine(source,line);
    // int newCharIndex = d.diff_xIndex(d.diff_main(source, newSource), oldCharIndex);
    // int newLineIndex = getLineFromCharIndex(newSource,newCharIndex);
    // return newLineIndex;
    
    return 0;
  }
  
  private int getCharIndexFromLine(String source, String line) {
    return source.indexOf(line);
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
}
