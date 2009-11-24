package edu.stanford.hci.processing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

public class VersionHistoryFrameiMovie extends JFrame {
  
  public static final int ROW_HEIGHT = 120;
  private static final Color selectedColor = new Color(150,255,150);
  
  private final VersionHistoryController controller;
  
  private JPanel moviesPanel;
  private JTextArea codeArea;
  private BigMovieView bigMovie;
  
  private ArrayList<VersionHistoryPanel> versionPanels;
  private boolean showMarkedOnly;
 
  public VersionHistoryFrameiMovie(final VersionHistoryController controller) {
    super("Version History");
    this.controller = controller;
    this.versionPanels = new ArrayList<VersionHistoryPanel>();
    showMarkedOnly = false;
    
    moviesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 5));
    moviesPanel.setPreferredSize(new Dimension(700, 700));
    JScrollPane movieScrollPane = new JScrollPane(moviesPanel);
    movieScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    movieScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    movieScrollPane.setMinimumSize(new Dimension(0, 400));
    
    AdjustmentListener scrollListener = new ScrollAdjustmentListener();
//    movieScrollPane.getHorizontalScrollBar().addAdjustmentListener(scrollListener);
    movieScrollPane.getVerticalScrollBar().addAdjustmentListener(scrollListener);
    
    codeArea = new JTextArea();
    JScrollPane codeScrollPane = new JScrollPane(codeArea);
    
    bigMovie = new BigMovieView();
    
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 5));
    JButton showMarked = new JButton("Show Marked");
    showMarked.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) { 
        	toggleShowMarkedOnly();
        }
      });
    buttonPanel.add(showMarked);
   
    JSplitPane hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        codeScrollPane, bigMovie);
    
    JSplitPane vSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        hSplitPane, movieScrollPane);
      
    JSplitPane v2SplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
    		vSplitPane, buttonPanel);
    vSplitPane.setDividerLocation(300);
    hSplitPane.setDividerLocation(400);
    v2SplitPane.setDividerLocation(700);
    
    add(v2SplitPane);
   
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
    
    //moviesPanel.setPreferredSize(new Dimension(700, versionPanels.size() * ROW_HEIGHT));
    
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
  

  public void toggleShowMarkedOnly() {
	  showMarkedOnly = !showMarkedOnly;
	  if(showMarkedOnly) {
		  for (VersionHistoryPanel vhp : versionPanels) {
			  if (!vhp.recording.isMarked()) {
				  moviesPanel.remove(vhp);
			  }
		  }
	  } else {
		  moviesPanel.removeAll();
		  for (int i = 0; i < versionPanels.size(); i++) {
			  moviesPanel.add(versionPanels.get(i));
		  }
	  }
	  moviesPanel.revalidate();
  }
  
  public class VersionHistoryPanel extends JPanel {
    private VersionHistory model;
    private RecordingView recording;
    
    public VersionHistoryPanel(VersionHistory newModel) {
      super(new BorderLayout());
      //setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
      setPreferredSize(new Dimension(ROW_HEIGHT, ROW_HEIGHT));
      setBackground(Color.white);
      setBorder(BorderFactory.createLineBorder(Color.black, 3));
      
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

//      addMouseListener(new MouseAdapter() {
//        @Override
//        public void mouseClicked(MouseEvent e) { 
//          controller.swapRunningCode(versionPanels.indexOf(e.getSource()));
//          System.out.println("clicked");
//        }
//      });
    }
        
    public String getFilename() {
    	return model.getVideoFilename();
    }
    
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
  
  public class ScrollAdjustmentListener implements AdjustmentListener {
	  public void adjustmentValueChanged(AdjustmentEvent evt) {
		  moviesPanel.revalidate();
	  }
  }
}
