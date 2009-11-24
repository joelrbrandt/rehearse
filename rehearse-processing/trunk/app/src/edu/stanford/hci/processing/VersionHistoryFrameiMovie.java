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

public class VersionHistoryFrameiMovie extends JFrame {
  
  public static final int ROW_HEIGHT = 120;
  private static final Color selectedColor = new Color(150,255,150);
  
  private final VersionHistoryController controller;
  
  private JPanel moviesPanel;
  private JTextArea codeArea;
  private BigMovieView bigMovie;
  
  private ArrayList<VersionHistoryPanel> versionPanels;
 
  public VersionHistoryFrameiMovie(final VersionHistoryController controller) {
    super("Version History");
    this.controller = controller;
    this.versionPanels = new ArrayList<VersionHistoryPanel>();
    
    moviesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 5));
    JScrollPane movieScrollPane = new JScrollPane(moviesPanel);
    movieScrollPane.setMinimumSize(new Dimension(0, 400));
    
    codeArea = new JTextArea();
    JScrollPane codeScrollPane = new JScrollPane(codeArea);
    
    bigMovie = new BigMovieView();

    JSplitPane hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        codeScrollPane, bigMovie);
    
    JSplitPane vSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        hSplitPane, moviesPanel);
      
    vSplitPane.setDividerLocation(300);
    hSplitPane.setDividerLocation(400);
    add (vSplitPane);
   
    setPreferredSize(new Dimension(700, 800));
    bigMovie.init();
  }
  
  public VersionHistoryController getController() {
    return controller;
  }
  
  public void addVersionHistory(VersionHistory vh) {
    VersionHistoryPanel panel = new VersionHistoryPanel(vh);
    
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
  
  public void updateCodeArea(String filename) {
	for (VersionHistoryPanel vhp : versionPanels) {
		String f = vhp.getFilename();
		if (f != null && f.equals(filename)) {
			codeArea.setText(vhp.getCode());
			break;
		}

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
    bigMovie.setRecordingAt(index, vh.getVideoFilename());
    validate();
  }
  
  public class VersionHistoryPanel extends JPanel {
    private VersionHistory model;
    private RecordingView recording;
    
    public VersionHistoryPanel(VersionHistory newModel) {
      super(new BorderLayout());
      //setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
//      setMaximumSize(new Dimension(ROW_HEIGHT, ROW_HEIGHT));
//      setMinimumSize(new Dimension(ROW_HEIGHT, ROW_HEIGHT));
//      setPreferredSize(new Dimension(ROW_HEIGHT, ROW_HEIGHT));
      
      setBackground(Color.white);
//      setBorder(BorderFactory.createLineBorder(Color.black));
      
      
      String fileName = null;
      if (newModel != null) {
        fileName = newModel.getVideoFilename();
      }
      
      recording = new RecordingView(fileName, this);
      recording.bigMovie = bigMovie;
      recording.frame = VersionHistoryFrameiMovie.this;
      bigMovie.addRecording(fileName);
      
      //recording.sketchPath = sketch.getFolder().getAbsolutePath();
      add(recording, BorderLayout.CENTER);
      //recording.init();
      
      setModel(newModel);

      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) { 
          controller.swapRunningCode(versionPanels.indexOf(e.getSource()));
          System.out.println("clicked");
        }
        
        
      });
    }
    
   
    public String getFilename() {
    	return model.getVideoFilename();
    }
    
    // this is not good practice, methinks.
    public String getCode() {
    	return model.getCode();
    }
    public void setModel(VersionHistory model) {
      this.model = model;
      String fileName = null;
      if (model != null) {
        fileName = model.getVideoFilename();
      }
      recording.setRecording(fileName);
      validate();
    }
    
    public VersionHistory getModel() {
      return model;
    }
  }
}
