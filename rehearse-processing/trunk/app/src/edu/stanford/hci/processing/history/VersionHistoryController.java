package edu.stanford.hci.processing.history;

import java.awt.Image;
import java.util.*;

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
    int lastLineNum = editor.getTextArea().getLineOfOffset(e.getMark());
    //System.out.println("Caret changed to line " + lineNum);
    if (historyView != null) {
      historyView.scrollWithEditorCaret(editor.appendCodeFromAllTabs(), lineNum);
    }
    
    TokenMarker tm = ((SyntaxDocument) editor.getSketch().getCode()[0].getDocument()).getTokenMarker();
    
    Set<Integer> relevantVersions = new HashSet<Integer>();
    int start = Math.min(lineNum, lastLineNum);
    int end = Math.max(lineNum, lastLineNum);
    for (int i = start; i <= end; i++) {
      RehearseLineModel m = (RehearseLineModel) tm.getLineModelAt(i);
      if (m != null) {
        relevantVersions.addAll(m.relevantVersions);
      }
    }
    historyView.setVersionFilter(relevantVersions);
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
      List<String> currLines = Arrays.asList(currentCode.split("\n"));
      
      Patch patch = DiffUtils.diff(currLines, Arrays.asList(vh.getCode().split("\n")));
      
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
      int[] revisedLineNums = new int[currLines.size()];
      for (int i = 0; i < revisedLineNums.length; i++) {
        revisedLineNums[i] = i;
      }
      
      for (Delta delta: patch.getDeltas()) {
        if (delta.type == Delta.INSERTION) {
          int pos = delta.getOriginal().getPosition();
          for (int i = pos; i < revisedLineNums.length; i++) {
            if (revisedLineNums[i] != -1)
              revisedLineNums[i]++;
          }
        } else if (delta.type == Delta.DELETION) {
          int pos = delta.getOriginal().getPosition();
          for (int i = pos; i < pos + delta.getOriginal().getSize(); i++) {
            revisedLineNums[i] = -1;
          }
          
          pos = delta.getOriginal().last();
          for (int i = pos + 1; i < revisedLineNums.length; i++) {
            if (revisedLineNums[i] != -1)
              revisedLineNums[i]--;
          }
        }
      }
      
      Map<Integer, Integer> newLineCorrespondenceMap = new HashMap<Integer, Integer>();
      for (int i = 0; i < revisedLineNums.length; i++) {
        if (revisedLineNums[i] != -1) {
          Integer value = lineCorrespondenceMap.get(i);
          if (value != null) {
            newLineCorrespondenceMap.put(revisedLineNums[i], value);
          }
        }
      }
      
      lineCorrespondenceMap = newLineCorrespondenceMap;
      
      currentCode = vh.getCode();
    }
  }
}
