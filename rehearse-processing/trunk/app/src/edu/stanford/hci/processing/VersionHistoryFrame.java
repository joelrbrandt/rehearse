package edu.stanford.hci.processing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import edu.stanford.hci.processing.VersionHistoryFrameiMovie.VersionHistoryPanel;

public abstract class VersionHistoryFrame extends JFrame {
	public static final int ROW_HEIGHT = 120;
	protected static final Color selectedColor = new Color(150,255,150);
	
	protected final VersionHistoryController controller;

	protected JPanel moviesPanel;
	JTextArea codeArea;
	BigMovieView bigMovie;
	
	protected JSplitPane hSplitPane;
	protected JSplitPane vSplitPane;
	
	public VersionHistoryFrame(final VersionHistoryController controller) {
	    super("Version History");
	    this.controller = controller;
	    
	    moviesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 5));
	    moviesPanel.setPreferredSize(new Dimension(700, 700));
	    JScrollPane movieScrollPane = new JScrollPane(moviesPanel);
	    movieScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	    movieScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    movieScrollPane.setMinimumSize(new Dimension(0, 400));
	    
	    AdjustmentListener scrollListener = new ScrollAdjustmentListener();
	    movieScrollPane.getVerticalScrollBar().addAdjustmentListener(scrollListener);
	    
	    codeArea = new JTextArea();
	    JScrollPane codeScrollPane = new JScrollPane(codeArea);
	    
	    bigMovie = new BigMovieView();
   
	    hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
	        codeScrollPane, bigMovie);
	    
	    vSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
	        hSplitPane, movieScrollPane);
	      
	    vSplitPane.setDividerLocation(300);
	    hSplitPane.setDividerLocation(400);
   	   
	    setPreferredSize(new Dimension(700, 800));
	    bigMovie.init();
	  }
	  
	  public VersionHistoryController getController() {
	    return controller;
	  }
	  
	  abstract public void addVersionHistory(VersionHistory vh);
	  
	  abstract public void lastRunningVersionChanged(int oldIndex, int newIndex);
	  
	  abstract public void updateCodeArea(String filename);
	  
	  abstract public void updateScreenshot(int index, Image screenshot);
	  
	  abstract public void updateVideo(int index, VersionHistory vh);
	  
	  public class ScrollAdjustmentListener implements AdjustmentListener {
		  public void adjustmentValueChanged(AdjustmentEvent evt) {
			  moviesPanel.revalidate();
		  }
	  }
}