package edu.stanford.hci.processing.history;

import java.awt.Image;
import java.util.Date;

import processing.video.Movie;

public class VersionHistory {
  private int version = -1; // Version number
  
  private Image screenshot;
  
  private String recordingFilename; // This may not be needed -Abel
  //private Movie recording;
  
  private String code;
  private Date time;
  
  public VersionHistory(int version, Image screenshot, String code, Date time) {
    super();
    this.screenshot = screenshot;
    this.code = code;
    this.time = time;
    this.version = version;
    
    //this.recordingFilename = "history/" + recordingFilename;
  }
  
  public Image getScreenshot() {
    return screenshot;
  }
  
  public void setScreenshot(Image screenshot) {
    this.screenshot = screenshot;
  }
  
  public String getCode() {
    return code;
  }
  
  public void setCode(String code) {
    this.code = code;
  }
  
  public Date getTime() {
    return time;
  }
  
  public void setTime(Date time) {
    this.time = time;
  }
  
  public void setVideoFilename(String filename) {
    this.recordingFilename = filename;
  }
  
  public String getVideoFilename() { return this.recordingFilename; }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }
  
  
}
