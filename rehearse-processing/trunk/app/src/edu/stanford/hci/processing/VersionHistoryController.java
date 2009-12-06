package edu.stanford.hci.processing;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

import processing.app.syntax.SyntaxDocument;
import processing.app.syntax.TokenMarker;
import processing.video.MovieMaker;
import edu.stanford.hci.processing.editor.RehearseEditor;
import edu.stanford.hci.processing.editor.RehearseLineModel;

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
    
    updateLineModelsWithVersionInfo();
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
        historyView = new VersionHistoryFrameiMovie2(this);
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
      
      updateLineModelsWithVersionInfo();
      
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
    if (historyView != null) {
      historyView.scrollWithEditorCaret(editor.appendCodeFromAllTabs(), lineNum);
    }
    
    TokenMarker tm = ((SyntaxDocument) editor.getSketch().getCode()[0].getDocument()).getTokenMarker();
    RehearseLineModel m = (RehearseLineModel) tm.getLineModelAt(lineNum);
    if (m != null) {
      historyView.setVersionFilter(m.relevantVersions);
    }
  }
  
  public void updateLineModelsWithVersionInfo() {
    Map<Integer, Integer> lineCorrespondenceMap = new HashMap<Integer, Integer>();
    for (int i = 0; i < editor.getLineCount(); i++) {
      lineCorrespondenceMap.put(i, i);
    }
    
    String currentCode = editor.getText();
    // TODO: Make this work with multiple tabs.
    TokenMarker tm = ((SyntaxDocument) editor.getSketch().getCode()[0].getDocument()).getTokenMarker();
      
    for (int i = 0; i < tm.getLineCount(); i++) {
      RehearseLineModel m = (RehearseLineModel) tm.getLineModelAt(i);
      if (m == null) {
        m = new RehearseLineModel();
        tm.setLineModelAt(i, m);
      }
    }
    
    for (int versionNum = historyModels.size() - 1; versionNum >= 0; versionNum--) {
      VersionHistory vh = historyModels.get(versionNum);
      
      Patch patch = DiffUtils.diff(Arrays.asList(currentCode.split("\n")), 
          Arrays.asList(vh.getCode().split("\n")));
      
      // Annotate lines with relevant versions.
      for (Delta delta: patch.getDeltas()) {
        if (delta.type == Delta.INSERTION) {
          
        } else if (delta.type == Delta.DELETION) {
          int begin = delta.getOriginal().getPosition();
          int size = delta.getOriginal().getSize();
          for (int i=begin; i<begin+size; i++) {
            Integer value = lineCorrespondenceMap.get(i);
            if (value != null) {
              RehearseLineModel m = (RehearseLineModel) tm.getLineModelAt(value);
              m.relevantVersions.add(versionNum + 1);
            }
          }
        } else if (delta.type == Delta.CHANGE) {
          int begin = delta.getOriginal().getPosition();
          int size = delta.getOriginal().getSize();
          for (int i=begin; i<begin+size; i++) {
            Integer value = lineCorrespondenceMap.get(i);
            if (value != null) {
              RehearseLineModel m = (RehearseLineModel) tm.getLineModelAt(value);
              m.relevantVersions.add(versionNum + 1);
            }
          }
        }
      }
      
        // Update line correspondence map.
      for (Delta delta: patch.getDeltas()) {
        if (delta.type == Delta.INSERTION) {
          
        } else if (delta.type == Delta.DELETION) {
          int begin = delta.getOriginal().getPosition();
          int size = delta.getOriginal().getSize();
          for (int i=begin; i<begin+size; i++) {
            lineCorrespondenceMap.remove(i);
          }
        } else if (delta.type == Delta.CHANGE) {
          int size = delta.getRevised().getSize();
          for (int i = 0; i < size; i++) {
            int key = delta.getOriginal().getPosition() + i;
            Integer value = lineCorrespondenceMap.get(key);
            if (value != null) {
              lineCorrespondenceMap.remove(key);
              lineCorrespondenceMap.put(delta.getRevised().getPosition() + i, value);
            }
          } 
        }
      }
      
      currentCode = vh.getCode();
    }
  }
}
