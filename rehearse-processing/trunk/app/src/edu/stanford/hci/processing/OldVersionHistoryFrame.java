//package edu.stanford.hci.processing;
//
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.FlowLayout;
//import java.awt.Image;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.text.DateFormat;
//import java.util.ArrayList;
//
//import javax.swing.BorderFactory;
//import javax.swing.Box;
//import javax.swing.BoxLayout;
//import javax.swing.ImageIcon;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JSplitPane;
//import javax.swing.JTextArea;
//import javax.swing.border.Border;
//
//import processing.app.Sketch;
//
//public class VersionHistoryFrame extends JFrame {
//  
//  public static final int ROW_HEIGHT = 120;
//  private static final Color selectedColor = new Color(150,255,150);
//  
//  private static final Border hoverBorder = BorderFactory.createLineBorder(Color.green);
//  private static final Border normalBorder = BorderFactory.createLineBorder(Color.black);
//  
//  private final VersionHistoryController controller;
//  
//  private JPanel moviesPanel;
//  private JTextArea codeArea;
//  
//  private ArrayList<VersionHistoryPanel> versionPanels;
// 
//  public VersionHistoryFrame(final VersionHistoryController controller) {
//    super("Version History");
//    this.controller = controller;
//    this.versionPanels = new ArrayList<VersionHistoryPanel>();
//    //this.sketch = sketch;
//    
//    moviesPanel = new JPanel();
//    moviesPanel.setMaximumSize(new Dimension(ROW_HEIGHT + 50, 0));
//    moviesPanel.setLayout(new BoxLayout(moviesPanel, BoxLayout.PAGE_AXIS));
//    moviesPanel.add(Box.createVerticalGlue());
//    JScrollPane movieScrollPane = new JScrollPane(moviesPanel);
//    movieScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//    movieScrollPane.setVerticalScrollBarPolicy((JScrollPane.VERTICAL_SCROLLBAR_ALWAYS));
//    movieScrollPane.setMinimumSize(new Dimension(120, 0));
//    
//    codeArea = new JTextArea();
//    JScrollPane codeScrollPane = new JScrollPane(codeArea);
//    
//    //add(new JTextArea(), BorderLayout.CENTER);
//    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
//        movieScrollPane, codeScrollPane);
//    splitPane.setDividerLocation(150);
//    add(splitPane);
//    
//    setPreferredSize(new Dimension(700, 500));
//  }
//  
//  public void addVersionHistory(VersionHistory vh) {
//    VersionHistoryPanel panel = new VersionHistoryPanel(vh);
//    
//    moviesPanel.add(Box.createRigidArea(new Dimension(10, 5)), 0);
//    moviesPanel.add(panel, 0);
//    versionPanels.add(panel);
//    
//    codeArea.setText(vh.getCode());
//    
//    validate();
//    panel.recording.init();
//  }
//  
//  public void lastRunningVersionChanged(int oldIndex, int newIndex) {
//    if (oldIndex != -1) {
//      versionPanels.get(oldIndex).setBackground(Color.white);
//    }
//    if (newIndex != -1) {
//      versionPanels.get(newIndex).setBackground(selectedColor);
//    }
//  }
//  
//  public void updateScreenshot(int index, Image screenshot) {
////    VersionHistoryPanel panel = versionPanels.get(index);
////    model.setScreenshot(screenshot);
////    panel.setModel(model);
////    repaint();
//  }
//  
//  public void updateVideo(int index, VersionHistory vh) {
//    VersionHistoryPanel panel = versionPanels.get(index);
//    panel.setModel(vh);
//    validate();
//  }
//  
//  public class VersionHistoryPanel extends JPanel {
//    private VersionHistory model;
//    private RecordingView recording;
//    
//    public VersionHistoryPanel(VersionHistory model) {
//      super(new BorderLayout());
//      //setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
//      setMaximumSize(new Dimension(ROW_HEIGHT, ROW_HEIGHT));
//      setMinimumSize(new Dimension(ROW_HEIGHT, ROW_HEIGHT));
//      setPreferredSize(new Dimension(ROW_HEIGHT, ROW_HEIGHT));
//      
//      setBackground(Color.white);
//      setBorder(BorderFactory.createLineBorder(Color.black));
//      
//      recording = new RecordingView(model.getVideoFilename());
//      //recording.sketchPath = sketch.getFolder().getAbsolutePath();
//      add(recording, BorderLayout.CENTER);
//      //recording.init();
//      
//      setModel(model);
//      
//      addMouseListener(new MouseAdapter() {
//        @Override
//        public void mouseEntered(MouseEvent e) {
//          setBorder(hoverBorder);
//        }
//        
//        @Override
//        public void mouseExited(MouseEvent e) {
//          setBorder(normalBorder);
//        }
//        
//        @Override
//        public void mouseClicked(MouseEvent e) { 
//          controller.swapRunningCode(versionPanels.indexOf(e.getSource()));
//        }
//      });
//    }
//    
//    public void setModel(VersionHistory model) {
//      this.model = model;
//      recording.setRecording(model.getVideoFilename());
//      validate();
//    }
//  }
//}

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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import processing.app.Sketch;

public class OldVersionHistoryFrame extends JFrame {
  
  public static final int ROW_HEIGHT = 120;
  private static final Color selectedColor = new Color(150,255,150);
  
  private static final Border hoverBorder = BorderFactory.createLineBorder(Color.green);
  private static final Border normalBorder = BorderFactory.createLineBorder(Color.black);
  
  private final VersionHistoryController controller;
  
  private JPanel moviesPanel;
  private JTextArea codeArea;
  private RecordingView bigMovie;
  
  private ArrayList<VersionHistoryPanel> versionPanels;
 
  public OldVersionHistoryFrame(final VersionHistoryController controller) {
    super("Version History");
    this.controller = controller;
    this.versionPanels = new ArrayList<VersionHistoryPanel>();
    
    moviesPanel = new JPanel();
//    moviesPanel.setMaximumSize(new Dimension(ROW_HEIGHT + 50, 0));
//    moviesPanel.setLayout(new BoxLayout(moviesPanel, BoxLayout.PAGE_AXIS));
//    moviesPanel.add(Box.createVerticalGlue());
    JScrollPane movieScrollPane = new JScrollPane(moviesPanel);
    movieScrollPane.setMinimumSize(new Dimension(0, 400));
    
    codeArea = new JTextArea();
    JScrollPane codeScrollPane = new JScrollPane(codeArea);
    
    bigMovie = new RecordingView(null);
    
    
    JSplitPane hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        codeScrollPane, bigMovie);
    
    JSplitPane vSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        hSplitPane, moviesPanel);
    
    add(vSplitPane);
    
    setPreferredSize(new Dimension(700, 500));
  }
  
  public void addVersionHistory(VersionHistory vh) {
    VersionHistoryPanel panel = new VersionHistoryPanel(vh);
    
    //moviesPanel.add(Box.createRigidArea(new Dimension(10, 5)), 0);
    moviesPanel.add(panel);
    versionPanels.add(panel);
    
    codeArea.setText(vh.getCode());
    
    validate();
    panel.recording.init();
  }
  
  public void lastRunningVersionChanged(int oldIndex, int newIndex) {
    if (oldIndex != -1) {
      versionPanels.get(oldIndex).setBackground(Color.white);
    }
    if (newIndex != -1) {
      versionPanels.get(newIndex).setBackground(selectedColor);
    }
  }
  
  public void updateScreenshot(int index, Image screenshot) {
//    VersionHistoryPanel panel = versionPanels.get(index);
//    model.setScreenshot(screenshot);
//    panel.setModel(model);
//    repaint();
  }
  
  public void updateVideo(int index, VersionHistory vh) {
    VersionHistoryPanel panel = versionPanels.get(index);
    panel.setModel(vh);
    validate();
  }
  
  public class VersionHistoryPanel extends JPanel {
    private VersionHistory model;
    private RecordingView recording;
    
    public VersionHistoryPanel(VersionHistory model) {
      super(new BorderLayout());
      //setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
      setMaximumSize(new Dimension(ROW_HEIGHT, ROW_HEIGHT));
      setMinimumSize(new Dimension(ROW_HEIGHT, ROW_HEIGHT));
      setPreferredSize(new Dimension(ROW_HEIGHT, ROW_HEIGHT));
      
      setBackground(Color.white);
      setBorder(BorderFactory.createLineBorder(Color.black));
      
      recording = new RecordingView(model.getVideoFilename());
      //recording.sketchPath = sketch.getFolder().getAbsolutePath();
      add(recording, BorderLayout.CENTER);
      //recording.init();
      
      setModel(model);
      
      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
          setBorder(hoverBorder);
        }
        
        @Override
        public void mouseExited(MouseEvent e) {
          setBorder(normalBorder);
        }
        
        @Override
        public void mouseClicked(MouseEvent e) { 
          controller.swapRunningCode(versionPanels.indexOf(e.getSource()));
        }
      });
    }
    
    public void setModel(VersionHistory model) {
      this.model = model;
      recording.setRecording(model.getVideoFilename());
      validate();
    }
  }
}