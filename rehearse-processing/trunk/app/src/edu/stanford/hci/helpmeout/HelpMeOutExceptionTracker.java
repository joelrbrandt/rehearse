package edu.stanford.hci.helpmeout;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import processing.app.SketchCode;
import processing.app.debug.RunnerException;
import processing.app.debug.RuntimeRunnerException;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.TargetError;
import edu.stanford.hci.processing.editor.RehearseEditor;


public class HelpMeOutExceptionTracker {

  private ExceptionInfo eInfo;
  private String source;
  
  // make it a Singleton
  private static HelpMeOutExceptionTracker instance = new HelpMeOutExceptionTracker();
 
  private HelpMeOutServerProxy serverProxy = new HelpMeOutServerProxy();
  private HelpMeOutExceptionTracker() {}
  public static HelpMeOutExceptionTracker getInstance() {
    return instance;
  }

  /** Set current source code - have to do this separately from exception reporting since we don't have access to source from RehearsePApplet (check this) */
  public void setSource(String source) {
    this.source=source;
  }
  
  
  /** IF an exception occurred during non-interactive run, we cannot try to record a fix, but we can at least query for it
   * and show any available fixes. 
   */
  // TODO: NOT COMPLETE YET. write this function that only queries if we run in non-interactive mode; 
  public void processRuntimeExceptionNonInteractive(RunnerException rre) {

    RehearseEditor editor = (RehearseEditor) HelpMeOut.getInstance().getEditor();
    
    String error = rre.getMessage();

    String code = editor.getTextArea().getLineText(rre.getCodeLine());
    
    // Get the stacktrace into String form
    // http://www.devx.com/tips/Tip/27885
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw, true);
    rre.printStackTrace(pw);
    pw.flush();
    sw.flush();
    String trace = sw.toString();
    
    HelpMeOutTool tool = HelpMeOut.getInstance().getTool();
    
    try {
      ArrayList<HashMap<String,ArrayList<String>>> result = 
        serverProxy.queryexception(error, code, trace);
      HelpMeOutLog.getInstance().write(HelpMeOutLog.QUERY_SUCCESS_FOR + error);
      HelpMeOut.getInstance().showQueryResult(result, error, HelpMeOut.ErrorType.RUN);
    } catch (Exception e) {
      HelpMeOutLog.getInstance().writeError(HelpMeOutLog.QUERY_FAIL);
      e.printStackTrace();
      if(tool!=null) {
        tool.setLabelText("HelpMeOutQuery did not return any suggestions.");
      }
      //e.printStackTrace();
    }
  }
  
  /** record the runtime exception that just occurred in interactive mode and query for suggestions*/
  public void processRuntimeException(EvalError err,Interpreter i) {
    
    
    ///// First, try to notify editor of our exception so we get syntax highlighting and an error status bar update
    // analogous to Runner.java: reportException()
    // we need to know: String message, int file, int line, int column
    // TODO: currently only works for single-tab sketches! figure out how to extend to multi-tab sketches
    
    try {
      // if this EvalError just wraps a different Exception thrown by the script, then use that target
      Throwable t;
      if(err instanceof TargetError) {
        t = ((TargetError)err).getTarget();
      } else {
        t = err;
      }
      
      RehearseEditor editor = (RehearseEditor) HelpMeOut.getInstance().getEditor();
      
      SketchCode sc = editor.lineToSketchCode(err.getErrorLineNumber()-1);
      
      RuntimeRunnerException rre = new RuntimeRunnerException(t.getMessage(), 
                                                              editor.getSketch().getCodeIndex(sc),
                                                              err.getErrorLineNumber()-1-sc.getPreprocOffset(), -1, false); //need -1???
      HelpMeOut.getInstance().getEditor().statusError(rre);
    } catch (Exception e) {
      //something went wrong while we tried to notify editor
      e.printStackTrace();
    }
    
    
    // now save exception info and do HelpMeOut-specific stuff.
    eInfo = new ExceptionInfo(err,i,source);
    
    try{
      HelpMeOutLog.getInstance().write(HelpMeOutLog.EXCEPTION_BROKEN_FOR + eInfo.getExceptionClass());
    }catch (Exception e) {
      e.printStackTrace();
    }
      
    String error = eInfo.getExceptionClass();
    String code = eInfo.getExceptionLine();
    String trace = eInfo.getStackTrace();
    int line = eInfo.getExceptionLineNum();
    HelpMeOutTool tool = HelpMeOut.getInstance().getTool();
    
    // make sure we save the EvalError and Interpreter in HelpMeOut in case it needs to call this method after a re-query
    // save error and code in case we need to copy/paste fix
    HelpMeOut.getInstance().saveExceptionInfo(err, i, error, code, line);
    
    try {
      ArrayList<HashMap<String,ArrayList<String>>> result = 
        serverProxy.queryexception(error, code, trace);
      HelpMeOutLog.getInstance().write(HelpMeOutLog.QUERY_SUCCESS_FOR + error);
      HelpMeOut.getInstance().showQueryResult(result, error, HelpMeOut.ErrorType.RUN);
    } catch (Exception e) {
      HelpMeOutLog.getInstance().writeError(HelpMeOutLog.QUERY_FAIL);
      e.printStackTrace();
      if(tool!=null) {

        tool.setLabelText("HelpMeOutQuery did not return any suggestions.");
      }
      //e.printStackTrace();
    }
  }
  
  /** mark the previously recorded runtime exception as resolved */
  public void resolveRuntimeException() {
    HelpMeOutLog.getInstance().write(HelpMeOutLog.EXCEPTION_FIXED);
    try {
      
      String result = serverProxy.storeexception(eInfo, source);
      
    }catch (Exception e) {
      HelpMeOutLog.getInstance().writeError(HelpMeOutLog.STORE_FAIL_EXCEPTION);
      e.printStackTrace();
    }
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
    return newLine; ///was: +1; // lines of code aren't 0-indexed
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