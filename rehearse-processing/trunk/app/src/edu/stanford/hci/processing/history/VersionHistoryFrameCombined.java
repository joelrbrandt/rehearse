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

public class VersionHistoryFrameCombined extends VersionHistoryFrame{
  
  private VersionsView versionsView;
  
  private boolean showMarkedOnly;
  private boolean inMovieClipView = false;
  
  private JButton showMarked;
  private JButton viewMode;
  
 
  public VersionHistoryFrameCombined(final VersionHistoryController controller) {
    super(controller);
    
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 5));
    
    viewMode = new JButton("Movie Clip View");
    viewMode.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        toggleMovieView();
      }
    });
    buttonPanel.add(viewMode);
    
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
   
    versionsView.init();
  }
  
  public void toggleShowMarkedOnly() {
    // TODO: Support this in fisheye view.
    if (!inMovieClipView) return;
    
    MovieClipView movieClipView = (MovieClipView)versionsView;
    
    showMarkedOnly = !showMarkedOnly;
    if(showMarkedOnly) {
      movieClipView.filterMarkedVersions();
      showMarked.setText("Show All");
      
    } else {
      movieClipView.clearVersionFilter();
      showMarked.setText("Show Marked");
    }
  }
  
  public void toggleMovieView() {
    inMovieClipView = !inMovieClipView;
    
    VersionsView oldVersionsView = versionsView;
    moviesPanel.remove(versionsView);
    makeVersionsView();
    moviesPanel.add(versionsView, BorderLayout.CENTER);
    //versionsView.size(moviesPanel.getWidth(), moviesPanel.getHeight() + 100);
    versionsView.init();
    versionsView.reload(oldVersionsView.histories);
    
    showMarked.setEnabled(inMovieClipView);
    if (inMovieClipView) {
      viewMode.setText("Fish Eye View");
      if (showMarkedOnly)
        toggleShowMarkedOnly();
    } else {
      viewMode.setText("Movie Clip View");
    }
    
  }
  
  private void makeVersionsView() {
    if (inMovieClipView) {
      versionsView = new MovieClipView();
    } else {
      versionsView = new FishEyeView();
    }

    versionsView.bigMovie = bigMovie;
    versionsView.frame = VersionHistoryFrameCombined.this;
  }
  
  @Override
  protected JComponent makeMoviePane() {
    makeVersionsView();
    moviesPanel = new JPanel(new BorderLayout());
    moviesPanel.add(versionsView, BorderLayout.CENTER);
    
    return moviesPanel;
  }
  
  public void addVersionHistory(VersionHistory vh) {
    
    bigMovie.addRecording(vh.getVideoFilename());
    versionsView.addVersion(vh);
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
    
    VersionHistory vh = this.versionsView.getVersion(filename);
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
    this.versionsView.addVersion(vh);
    bigMovie.setRecordingAt(index, vh.getVideoFilename());
    validate();
  }

}
