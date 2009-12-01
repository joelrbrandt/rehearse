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

public class VersionHistoryFrameFishEye extends VersionHistoryFrame{
  
  private FishEyeView fisheyeView;
 
  public VersionHistoryFrameFishEye(final VersionHistoryController controller) {
    super(controller);
    
//    fisheyeView = new FishEyeView();
//    fisheyeView.bigMovie = bigMovie;
//    fisheyeView.frame = VersionHistoryFrameFishEye.this;
//    moviesPanel.add(fisheyeView);
    
    add (vSplitPane);
   
    fisheyeView.init();
  }
  
  @Override
  protected JComponent makeMoviePane() {
    fisheyeView = new FishEyeView();
    fisheyeView.bigMovie = bigMovie;
    fisheyeView.frame = VersionHistoryFrameFishEye.this;
    moviesPanel = new JPanel(new BorderLayout());
    moviesPanel.add(fisheyeView, BorderLayout.CENTER);
    
    return moviesPanel;
  }
  
  public void addVersionHistory(VersionHistory vh) {
    
    bigMovie.addRecording(vh.getVideoFilename());
    fisheyeView.addVersion(vh);
    codeArea.setText(vh.getCode());
    
    validate();
  }
  
  // TODO (Abel): What is this for?
  public void lastRunningVersionChanged(int oldIndex, int newIndex) {
    /*
    if (oldIndex != -1) {
      versionPanels.get(oldIndex).setBackground(Color.white);
    }
    if (newIndex != -1) {
      versionPanels.get(newIndex).setBackground(selectedColor);
    }
    */
  }
  
  public void updateCodeArea(String filename) {
    
    VersionHistory vh = this.fisheyeView.getVersion(filename);
    if (vh != null) {
      codeArea.setText(vh.getCode());
    }
  	
  }
  
  public void updateCodeArea(int versionNumber) {
  
    // 
  
  }
  
  /*
  public void setCurrentVersion(VersionHistory vh) {
      
    System.out.println("= = = = = = = = = ");
    System.out.println("Current Version: " + currentVersion);
    
    String codeTxt = vh.getCode();
    
    int lineNumber = codeArea.getFirstLine() + (codeArea.getVisibleLines() / 2);
    int numInsertedBelow = 0;
    
    Patch patch = DiffUtils.diff(Arrays.asList(textArea.getText().split("\n")),
                   Arrays.asList(codeTxt.split("\n")));
    for (Delta delta: patch.getDeltas()) {
            System.out.println(delta);
            int pos = delta.getOriginal().getPosition();
            if (pos < lineNumber) {
              if (delta.type == Delta.INSERTION) {
                numInsertedBelow += delta.getRevised().getSize();
              } else if (delta.type == Delta.DELETION) {
                numInsertedBelow -= delta.getOriginal().getSize();
              } else if (delta.type == Delta.CHANGE) {
                
              }
            }
    }
    System.out.println("Num Inserted Below: " + numInsertedBelow);
    
    textArea.setText(codeTxt);
    //textArea.setCaretPosition(lineNumber + numInsertedBelow);
    int newLineNumber = (lineNumber+numInsertedBelow) - (textArea.getVisibleLines() / 2);
    textArea.setFirstLine( newLineNumber < 0 ? 0 : newLineNumber );
    
    this.versionLabel.setText("Version: " + version.versionNumber);
    this.textArea.updateScrollBars();
  }
  */
  
  public void updateScreenshot(int index, Image screenshot) {
//    VersionHistoryPanel panel = versionPanels.get(index);
//    model.setScreenshot(screenshot);
//    panel.setModel(model);
//    repaint();
  }
  
  public void updateVideo(int index, VersionHistory vh) {
    // VersionHistoryPanel panel = versionPanels.get(index);
    // panel.setModel(vh);
    
    this.fisheyeView.addVersion(vh);
    bigMovie.setRecordingAt(index, vh.getVideoFilename());
    validate();
  }

}
