package edu.stanford.hci.processing.history;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import processing.app.syntax.*;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import edu.stanford.hci.processing.history.VersionHistoryFrameiMovie.VersionHistoryPanel;

import processing.app.Sketch;

public class VersionHistoryFrameiMovie2 extends VersionHistoryFrame{
  
  private MovieClipView movieClipView;
  
  private boolean showMarkedOnly;
  private JButton showMarked;
 
  public VersionHistoryFrameiMovie2(final VersionHistoryController controller) {
    super(controller);
    
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 5));
    
    showMarked = new JButton("Show Marked");
    showMarked.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        toggleShowMarkedOnly();
      }
    });
    buttonPanel.add(showMarked);
        
    JSplitPane v2SplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
        vSplitPane, buttonPanel);
    v2SplitPane.setDividerLocation(625);
    v2SplitPane.setResizeWeight(1.0);
    v2SplitPane.setEnabled(false);
    
    add(v2SplitPane);
   
    movieClipView.init();
  }
  
  public void toggleShowMarkedOnly() {
    showMarkedOnly = !showMarkedOnly;
    if(showMarkedOnly) {
      movieClipView.filterMarkedVersions();
      showMarked.setText("Show All");
      
    } else {
      movieClipView.clearVersionFilter();
      showMarked.setText("Show Marked");
    }
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
