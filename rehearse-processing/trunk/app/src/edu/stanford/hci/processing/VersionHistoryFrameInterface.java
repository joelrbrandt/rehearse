package edu.stanford.hci.processing;

import java.awt.Image;

public interface VersionHistoryFrameInterface {

  public VersionHistoryController getController();
  public void addVersionHistory(VersionHistory vh);
  public void lastRunningVersionChanged(int oldIndex, int newIndex);
  public void updateCodeArea(String filename);
  public void updateScreenshot(int index, Image screenshot);
  public void updateVideo(int index, VersionHistory vh);
  
  public void pack();
  public void setVisible(boolean v);
  public void dispose();
  
}
