package edu.stanford.hci.helpmeout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.googlecode.jj1.ServiceProxy;

import edu.stanford.hci.helpmeout.HelpMeOut.ErrorType;
import edu.stanford.hci.helpmeout.HelpMeOut.FixInfo;

public class HelpMeOutServerProxy {

  private static final long TIMEOUT = 5L;
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

  public ArrayList<HashMap<String,ArrayList<String>>> query(final String error, final String code) throws TimeoutException,RuntimeException, InterruptedException, ExecutionException {
    FutureTask<ArrayList<HashMap<String,ArrayList<String>>>> theTask = null;
    // create new task
    theTask = new FutureTask<ArrayList<HashMap<String,ArrayList<String>>>>(
        new Callable<ArrayList<HashMap<String,ArrayList<String>>>>() {
          public ArrayList<HashMap<String,ArrayList<String>>> call() throws Exception {
            return (ArrayList<HashMap<String,ArrayList<String>>>) proxy.call("query", error, code);

          }
        });
    // start task in a new thread
    new Thread(theTask).start();
    // wait for the execution to finish, timeout after 10 secs 
    return theTask.get(TIMEOUT, TimeUnit.SECONDS); 
  }

  public ArrayList<HashMap<String,ArrayList<String>>> queryexception(final String error, final String code, final String trace) throws RuntimeException, InterruptedException, ExecutionException, TimeoutException {
    FutureTask<ArrayList<HashMap<String,ArrayList<String>>>> theTask = null;
    // create new task
    theTask = new FutureTask<ArrayList<HashMap<String,ArrayList<String>>>>(
        new Callable<ArrayList<HashMap<String,ArrayList<String>>>>() {
          public ArrayList<HashMap<String,ArrayList<String>>> call() throws Exception {
            return (ArrayList<HashMap<String,ArrayList<String>>>) proxy.call("queryexception", error, code,trace);

          }
        });
    // start task in a new thread
    new Thread(theTask).start();
    // wait for the execution to finish, timeout after 10 secs 
    return theTask.get(TIMEOUT, TimeUnit.SECONDS); 
  }
  
  
  public String store2(final String error, final String s0, final String s1) throws InterruptedException, ExecutionException, TimeoutException {
    FutureTask<String> theTask = new FutureTask<String>(
        new Callable<String>() {
          public String call() throws Exception {
            return (String)proxy.call("store2", error,s0,s1);
          }
        });
    new Thread(theTask).start();
    return theTask.get(TIMEOUT,TimeUnit.SECONDS);
  }

  public String storeexception(final ExceptionInfo eInfo, final String source) throws InterruptedException, ExecutionException, TimeoutException {
    FutureTask<String> theTask = new FutureTask<String>(
        new Callable<String>() {
          public String call() throws Exception {
            return (String)proxy.call("storeexception",eInfo.getExceptionClass(), eInfo.getExceptionLine(), 
                                      eInfo.getStackTrace(), eInfo.getSourceCode(), source );
          }
        });
    new Thread(theTask).start();
    return theTask.get(TIMEOUT,TimeUnit.SECONDS);
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


}
