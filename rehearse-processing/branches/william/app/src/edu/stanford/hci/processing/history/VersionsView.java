package edu.stanford.hci.processing.history;

import java.util.ArrayList;

import processing.core.PApplet;

public abstract class VersionsView extends PApplet {

  VersionHistoryFrame frame;
  BigMovieView bigMovie;
  ArrayList<VersionHistory> histories = new ArrayList<VersionHistory>();
  
  public void reload(ArrayList<VersionHistory> histories) {
    for (VersionHistory vh : histories) {
      addVersion(vh);
    }
  }
  
  public abstract void addVersion(VersionHistory vh);
  
  public abstract VersionHistory getVersion(int n);
  
  public abstract VersionHistory getVersion(String filename);
  

}
