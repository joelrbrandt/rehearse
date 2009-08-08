package edu.stanford.hci.helpmeout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class HelpMeOutServerProxy {
  
  private static HelpMeOutServerProxy instance = new HelpMeOutServerProxy();
  private HelpMeOutServerProxy(){}
  public static HelpMeOutServerProxy getInstance() {
    return instance;
  }
  
  /* Determines how much (if any) information is sent to or received from the HelpMeOut database.
   * Set from HelpMeOutPreferences.
   */
  private HelpMeOutPreferences.Usage usage = null;
  
  private static final long TIMEOUT = 10L;
  protected static final String SERVICE_URL = "http://rehearse.stanford.edu/helpmeout/server-dev.py"; //URL of DB server to hit with JSON-RPC calls
  private ServiceProxy proxy = new ServiceProxy(SERVICE_URL);


  public String echo(final String in) throws InterruptedException, ExecutionException, TimeoutException {
    FutureTask<String> theTask = new FutureTask<String>(
        new Callable<String>() {
          public String call() throws Exception {
            return (String)proxy.call("echo", in);
          }
        });
    new Thread(theTask).start();
    return theTask.get(TIMEOUT,TimeUnit.SECONDS);
  }

  public ArrayList<HashMap<String,ArrayList<String>>> query(final String error, final String lineOfCode) throws TimeoutException,RuntimeException, InterruptedException, ExecutionException {
    if (usage == HelpMeOutPreferences.Usage.QUERY || usage == HelpMeOutPreferences.Usage.QUERY_AND_SUBMIT) {
      FutureTask<ArrayList<HashMap<String,ArrayList<String>>>> theTask = null;
      // create new task
      theTask = new FutureTask<ArrayList<HashMap<String,ArrayList<String>>>>(
          new Callable<ArrayList<HashMap<String,ArrayList<String>>>>() {
            public ArrayList<HashMap<String,ArrayList<String>>> call() throws Exception {
              String clean_error = cleanCompilerError(error);

              Object o = proxy.call("query",clean_error,lineOfCode);
              if(o instanceof String) {
                if(((String)o).equals("ERROR")) {
                  HelpMeOutLog.getInstance().writeError(HelpMeOutLog.QUERY_FAIL, "database reported error");
                  return null;
                } else if(((String)o).equals("NO_RESULT")) {
                  HelpMeOutLog.getInstance().write(HelpMeOutLog.QUERY_EMPTY,error);
                  return null;
                }
              }
              return (ArrayList<HashMap<String,ArrayList<String>>>)o;

            }
          });
      // start task in a new thread
      new Thread(theTask).start();
      // wait for the execution to finish, timeout after 10 secs 
      return theTask.get(TIMEOUT, TimeUnit.SECONDS);
    } else {
      return null;
    }
  }

  protected String cleanCompilerError(String error) {

    String cleaned_error=null;

    if(error.startsWith("The local variable") && error.endsWith("may not have been initialized")) {
      cleaned_error= error.replaceFirst("The local variable .*? may", "The local variable % may");
    } 
    else if( error.startsWith("The function ") && error.endsWith(" does not exist.")) {
      cleaned_error = error.replaceFirst("The function .*? does", "The function % does");
    } 
    else if (error.startsWith("unexpected token:")) {
      cleaned_error = "unexpected token: %";
    } 
    //else if (error.startsWith("expecting ") && error.contains(", found ")) {
    //  cleaned_error = "expecting %, found %";
    //}
    else if(error.contains("\u201c") || error.contains("\"") || error.contains("\u201d")) {
      cleaned_error = error.replaceAll("[\"\u201c].*?[\"\u201d]", "%");
    }
    if (cleaned_error!=null) {
      HelpMeOutLog.getInstance().write(HelpMeOutLog.CLEANED_QUERY, cleaned_error);
      return cleaned_error;
    } else {
      return error;
    }

  }

  protected String cleanRuntimeError(String error) {
    String cleaned_error=null;
    //TODO: what is the right behavior for Syntax error, insert "AssignmentOperator Expression" to complete Expression ?
    if(error.startsWith("java.lang.ArrayIndexOutOfBoundsException:")) {
      cleaned_error = "java.lang.ArrayIndexOutOfBoundsException: %";
      
    }
    else if(error.contains("\u201c") || error.contains("\"") || error.contains("\u201d")) {
      cleaned_error = error.replaceAll("[\"\u201c].*?[\"\u201d]", "%");
    }
    if (cleaned_error!=null) {
      HelpMeOutLog.getInstance().write(HelpMeOutLog.CLEANED_QUERY, cleaned_error);
      return cleaned_error;
    } else {
      return error;
    }

  }
  
  public ArrayList<HashMap<String,ArrayList<String>>> queryexception(final String error, final String lineOfCode, final String trace) throws RuntimeException, InterruptedException, ExecutionException, TimeoutException {
    if (usage == HelpMeOutPreferences.Usage.QUERY || usage == HelpMeOutPreferences.Usage.QUERY_AND_SUBMIT) {
      FutureTask<ArrayList<HashMap<String,ArrayList<String>>>> theTask = null;
      // create new task
      theTask = new FutureTask<ArrayList<HashMap<String,ArrayList<String>>>>(
          new Callable<ArrayList<HashMap<String,ArrayList<String>>>>() {
            public ArrayList<HashMap<String,ArrayList<String>>> call() throws Exception {
              String clean_error = cleanRuntimeError(error);
              Object o = proxy.call("queryexception", clean_error, lineOfCode,trace);
              if(o instanceof String) {
                if(((String)o).equals("ERROR")) {
                  HelpMeOutLog.getInstance().writeError(HelpMeOutLog.QUERYEXCEPTION_FAIL, "database reported error");
                  return null;
                } else if(((String)o).equals("NO_RESULT")) {
                  HelpMeOutLog.getInstance().write(HelpMeOutLog.QUERYEXCEPTION_EMPTY, error);
                  return null;
                }
              }
              return (ArrayList<HashMap<String,ArrayList<String>>>)o;
            }
          });
      // start task in a new thread
      new Thread(theTask).start();
      // wait for the execution to finish, timeout after 10 secs 
      return theTask.get(TIMEOUT, TimeUnit.SECONDS);
    } else {
      return null;
    }
  }


  public String store2(final String error, final String pgmBroken, final String pgmFixed) throws InterruptedException, ExecutionException, TimeoutException {
    if (usage == HelpMeOutPreferences.Usage.QUERY_AND_SUBMIT) {
      FutureTask<String> theTask = new FutureTask<String>(
          new Callable<String>() {
            public String call() throws Exception {
              return (String)proxy.call("store2", error,pgmBroken,pgmFixed);
            }
          });
      new Thread(theTask).start();
      return theTask.get(TIMEOUT,TimeUnit.SECONDS);
    } else {
      return null;
    }
  }

  public String storeexception(final ExceptionInfo eInfo, final String pgmFixed) throws InterruptedException, ExecutionException, TimeoutException {
    if (usage == HelpMeOutPreferences.Usage.QUERY_AND_SUBMIT) {
      FutureTask<String> theTask = new FutureTask<String>(
          new Callable<String>() {
            public String call() throws Exception {
              return (String)proxy.call("storeexception",eInfo.getExceptionClass(), eInfo.getExceptionLine(), 
                                        eInfo.getStackTrace(), eInfo.getSourceCode(), pgmFixed );
            }
          });
      new Thread(theTask).start();
      return theTask.get(TIMEOUT,TimeUnit.SECONDS);
    } else {
      return null;
    }
  }


  public void errorvote(int id, int vote) throws InterruptedException, ExecutionException, TimeoutException {
    errorvoteAux("errorvote",id,vote);
  }
  public void errorvoteexception(int id, int vote) throws InterruptedException, ExecutionException, TimeoutException {
    errorvoteAux("errorvoteexception",id,vote);
  }
  private void errorvoteAux(final String which, final int id, final int vote) throws InterruptedException, ExecutionException, TimeoutException {
    FutureTask<?> theTask = new FutureTask<Object>(
        new Callable<Object>() {
          public Object call() throws Exception {
            proxy.call(which,id,vote);
            return null;
          }
        });
    new Thread(theTask).start();
    theTask.get(TIMEOUT,TimeUnit.SECONDS);
  }
  
  public void setUsage(HelpMeOutPreferences.Usage usage) {
    this.usage = usage;
  }
  
  /** save our ide log to the database 
   * @throws TimeoutException 
   * @throws ExecutionException 
   * @throws InterruptedException */
  public void storeidelog(final String log) throws InterruptedException, ExecutionException, TimeoutException {
    FutureTask<?> theTask = new FutureTask<Object>(
        new Callable<Object>() {
          public Object call() throws Exception {
            proxy.call("storeidelog",log);
            return null;
          }
        });
    new Thread(theTask).start();
    theTask.get(TIMEOUT*2,TimeUnit.SECONDS);
 
  }

}
