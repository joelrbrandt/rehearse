package edu.stanford.hci.processing;

import java.awt.Image;
import java.util.ArrayList;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import processing.video.MovieMaker;
import edu.stanford.hci.processing.editor.RehearseEditor;

public class VersionHistoryController implements CaretListener {

  public static final int VIEW_IMOVIE = 1;
  public static final int VIEW_FISHEYE = 2;
  public static final int view_type = VIEW_IMOVIE;
  
  private RehearseEditor editor;
  private VersionHistoryIO historyIO;
  private ArrayList<VersionHistory> historyModels;
  
  private VersionHistoryFrame historyView;
  //private VersionHistoryFrameiMovie historyView;
  
  private int lastRunningVersionIndex = -1;
  
  public VersionHistoryController(RehearseEditor editor) {
    this.editor = editor;
    historyIO = new VersionHistoryIO(editor.getSketch().getFolder());
    historyModels = historyIO.loadHistory();
  }
  
  public VersionHistory getVersion(int versionNum) {
    return historyModels.get(versionNum);
  }
  
  public void addVersionHistory(VersionHistory vh) {
    
    //vh.setVideoFilename(historyIO.getVideoPath(vh.getVersion()));
    
    historyModels.add(vh);
    if (historyView != null) {
      historyView.addVersionHistory(vh);
    }
    setLastRunningVersionIndex(historyModels.size() - 1);
    
    historyIO.appendHistory(vh);
  }
  
  public void updateScreenshot(int index, Image screenshot) {
    historyModels.get(index).setScreenshot(screenshot);
//    if (historyView != null) {
//      historyView.updateScreenshot(index, screenshot);
//    }
    
    historyIO.updateImage(index, screenshot);
  }
  
  public void updateVideo(int index) {
    VersionHistory vh = historyModels.get(index);
    vh.setVideoFilename(historyIO.getVideoPath(vh.getVersion()));
    historyView.updateVideo(index, vh);
  }
  
  public void updateLastRunVideo(MovieMaker mm) {
    updateVideo(lastRunningVersionIndex);
  }
  
  public void updateLastRunScreenshot(Image screenshot) {
    updateScreenshot(lastRunningVersionIndex, screenshot);
  }
  
  private void setLastRunningVersionIndex(int index) {
    if (index == lastRunningVersionIndex) return;
    int oldIndex = lastRunningVersionIndex;
    lastRunningVersionIndex = index;
    if (historyView != null) {
      historyView.lastRunningVersionChanged(oldIndex, index);
    }
  }
  
  public void swapRunningCode(int index) {
    editor.swapRunningCode(historyModels.get(index).getCode());
    setLastRunningVersionIndex(index);
  }
  
  public void openHistoryView() {
    if (historyView == null) {
      
      if (this.view_type == VIEW_IMOVIE) {
        historyView = new VersionHistoryFrameiMovie(this);
      } else if (this.view_type == VIEW_FISHEYE) {
        try {
        historyView = new VersionHistoryFrameFishEye(this);
        } catch (Exception e) {
          System.out.println("oops: " + e);
        }
      }
      
      historyView.pack();
      historyView.setVisible(true);
      
      // Load saved history.
      for (VersionHistory vh : historyModels) {
        vh.setVideoFilename(historyIO.getVideoPath(vh.getVersion()));
        historyView.addVersionHistory(vh);
      }
      setLastRunningVersionIndex(historyModels.size() - 1);
    } else {
      historyView.setVisible(true);
    }
  }
  
  public void closeAndDisposeHistoryView() {
    if (historyView != null) {
      historyView.setVisible(false);
      historyView.dispose();
      historyView = null;
    }
  }
  
  // Returns number of version histories in this controller
  public int size() { return this.historyModels.size(); }
  
  public void runHistoryCode(String code) {
    editor.runHistoryCode(code);
  }

  public void caretUpdate(CaretEvent e) {
    int lineNum = editor.getTextArea().getLineOfOffset(e.getDot());
    //System.out.println("Caret changed to line " + lineNum);
    historyView.scrollWithEditorCaret(editor.appendCodeFromAllTabs(), lineNum);
  }
}
