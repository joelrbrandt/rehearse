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
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.stanford.hci.processing.VersionHistoryFrame.ScrollableFlowPanel;

public class VersionHistoryFrameiMovie extends VersionHistoryFrame {
  
  private ArrayList<VersionHistoryPanel> versionPanels;
  private boolean showMarkedOnly;
 
  public VersionHistoryFrameiMovie(final VersionHistoryController controller) {
    super(controller);
    this.versionPanels = new ArrayList<VersionHistoryPanel>();
    showMarkedOnly = false;
      
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 5));
    JButton showMarked = new JButton("Show Marked");
    showMarked.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) { 
        	toggleShowMarkedOnly();
        }
      });
    buttonPanel.add(showMarked);
        
    JSplitPane v2SplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
    		vSplitPane, buttonPanel);
    v2SplitPane.setDividerLocation(520);
    
    add(v2SplitPane);
  }
  
  @Override
  protected JComponent makeMoviePane() {
    moviesPanel = new ScrollableFlowPanel(new FlowLayout(FlowLayout.LEFT, 1, 5));
    moviesPanel.setPreferredSize(new Dimension(700, 500));
    JScrollPane movieScrollPane = new JScrollPane(moviesPanel);
    movieScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    movieScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

    movieScrollPane.getViewport().addChangeListener(new ChangeListener() {

      public void stateChanged(ChangeEvent e) {
        moviesPanel.revalidate();
      }
      
    });
    
    return movieScrollPane;
  }
  
  public VersionHistoryController getController() {
    return controller;
  }
  
  public void addVersionHistory(VersionHistory vh) {
    VersionHistoryPanel panel = new VersionHistoryPanel(vh);
    
    moviesPanel.add(panel);
    versionPanels.add(panel);
    
    //codeArea.setText(vh.getCode());
    setVersionNumber(vh.getVersion());
    
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
    
    public static final int DEFAULT_BORDER_WIDTH = 1;
    
    public VersionHistoryPanel(VersionHistory newModel) {
      super(new BorderLayout());
      //setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
      setPreferredSize(new Dimension(ROW_HEIGHT, ROW_HEIGHT));
      setBackground(Color.white);
      setBorder(BorderFactory.createLineBorder(Color.black, DEFAULT_BORDER_WIDTH));
      
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
}
