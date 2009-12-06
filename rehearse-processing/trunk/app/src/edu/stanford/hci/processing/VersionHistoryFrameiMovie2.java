package edu.stanford.hci.processing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import processing.app.syntax.*;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

import processing.app.Sketch;

public class VersionHistoryFrameiMovie2 extends VersionHistoryFrame{
  
  private MovieClipView movieClipView;
 
  public VersionHistoryFrameiMovie2(final VersionHistoryController controller) {
    super(controller);
    
    add (vSplitPane);
   
    movieClipView.init();
  }
  
  @Override
  protected JComponent makeMoviePane() {
    movieClipView = new MovieClipView();
    movieClipView.bigMovie = bigMovie;
    movieClipView.frame = VersionHistoryFrameiMovie2.this;
    moviesPanel = new JPanel(new BorderLayout());
    moviesPanel.add(movieClipView, BorderLayout.CENTER);
    
    return moviesPanel;
  }
  
  public void addVersionHistory(VersionHistory vh) {
    
    bigMovie.addRecording(vh.getVideoFilename());
    movieClipView.addVersion(vh);
    codeArea.setText(vh.getCode());
    
    validate();
  }
  
  public void lastRunningVersionChanged(int oldIndex, int newIndex) {
//    if (oldIndex != -1) {
//      versionPanels.get(oldIndex).setBackground(Color.white);
//    }
//    if (newIndex != -1) {
//      versionPanels.get(newIndex).setBackground(selectedColor);
//    }
  }
  
  public void updateCodeArea(String filename) {
    
    VersionHistory vh = this.movieClipView.getVersion(filename);
    if (vh != null) {
      codeArea.setText(vh.getCode());
    }
    
  }
  
  @Override
  public void setVersionFilter(Set<Integer> versions) {
    //
  }
  
  public void updateScreenshot(int index, Image screenshot) {
  }
  
  public void updateVideo(int index, VersionHistory vh) {
    this.movieClipView.addVersion(vh);
    bigMovie.setRecordingAt(index, vh.getVideoFilename());
    validate();
  }

}
