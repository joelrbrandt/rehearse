package edu.stanford.hci.helpmeout;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class HelpMeOutLog {
  
  // make it a Singleton
  private static HelpMeOutLog instance = new HelpMeOutLog();
  private HelpMeOutLog(){
    addTimestamp();
  }
  public static HelpMeOutLog getInstance() {
    return instance;
  }
  
  private StringBuffer log = new StringBuffer();
  private StringBuffer out = new StringBuffer();
  private StringBuffer err = new StringBuffer();
  
  private boolean writeToStdOut = true;
  
  public void write(String text) {
    text = text.concat("\n");
    out.append(text);
    log.append(text);
    if (writeToStdOut) {
      System.out.println(text);
    }
  }
  
  public void writeError(String text) {
    text = text.concat("\n");
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
  
  private void addTimestamp() {
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
