package edu.stanford.hci.helpmeout;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.TargetError;

/**
 * Class that stores all relevant information about a runtime exception that occurred during an interactive run.
 * @author bjoern
 *
 */
public class ExceptionInfo {
  protected String sourceCode;

  protected String exceptionClass;
  protected String exceptionMessage;

  protected int exceptionLineNum;
  protected String exceptionLine;
  
  protected String stackTrace;
  
  protected HashMap<String, String> environment;
  
  private ExceptionInfo() {} //no default constructor
 
  public ExceptionInfo(EvalError e, Interpreter i, String source) {
    Throwable t;
    
    // if this EvalError just wraps a different Exception thrown by the script, then use that target
    if(e instanceof TargetError) {
      t = ((TargetError)e).getTarget();
    } else {
      t = e;
    }
    
    // store exception class name
    this.exceptionClass =t.getClass().getCanonicalName();
    // and error message
    this.exceptionMessage = t.getMessage();
    
    
    // Get the stacktrace into String form
    // http://www.devx.com/tips/Tip/27885
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw, true);
    t.printStackTrace(pw);
    pw.flush();
    sw.flush();
    this.stackTrace = sw.toString();
    
    // store source code of processing sketch
    this.sourceCode = source;
    // and an index to the line that threw the error
    // TODO: this only works for single-file sketches for now
    this.exceptionLineNum = e.getErrorLineNumber();
    this.exceptionLine = e.getErrorText();
   
    //store environment of accessible variables and values
    this.environment = (HashMap<String, String>) i.makeSnapshotModel().getVariableMap();
   }

  public String getSourceCode() {
    return sourceCode;
  }

  public String getExceptionClass() {
    return exceptionClass;
  }

  public String getExceptionMessage() {
    return exceptionMessage;
  }

  public int getExceptionLineNum() {
    return exceptionLineNum;
  }

  public String getExceptionLine() {
    return exceptionLine;
  }

  public String getStackTrace() {
    return stackTrace;
  }

  public HashMap<String, String> getEnvironment() {
    return environment;
  }
}
