package edu.stanford.hci.processing;

import java.awt.Image;
import java.util.Date;

public class VersionHistory {
  private Image screenshot;
  private String code;
  private Date time;
  
  public VersionHistory(Image screenshot, String code, Date time) {
    super();
    this.screenshot = screenshot;
    this.code = code;
    this.time = time;
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
}
