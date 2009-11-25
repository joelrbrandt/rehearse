package edu.stanford.hci.processing;

import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import processing.app.syntax.JEditTextArea;
import processing.app.syntax.RehearseTextAreaDefaults;

import edu.stanford.hci.processing.VersionHistoryFrameiMovie.VersionHistoryPanel;

public abstract class VersionHistoryFrame extends JFrame {
	public static final int ROW_HEIGHT = 60;
	protected static final Color selectedColor = new Color(150,255,150);
	
	protected final VersionHistoryController controller;
	
	private int currVersion;

	protected JPanel moviesPanel;
	
	JEditTextArea codeArea;
	//JTextArea codeArea;
	BigMovieView bigMovie;
	
	protected JSplitPane hSplitPane;
	protected JSplitPane vSplitPane;
	
	public VersionHistoryFrame(final VersionHistoryController controller) {
	    super("Version History");
	    this.controller = controller;
	    this.currVersion = -1;
	    
	    moviesPanel = new ScrollableFlowPanel(new FlowLayout(FlowLayout.LEFT, 1, 5));
	    moviesPanel.setPreferredSize(new Dimension(700, 500));
	    JScrollPane movieScrollPane = new JScrollPane(moviesPanel);
	    movieScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	    movieScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//	    movieScrollPane.setMinimumSize(new Dimension(0, 400));
//	    AdjustmentListener scrollListener = new ScrollAdjustmentListener();
//	    movieScrollPane.getVerticalScrollBar().addAdjustmentListener(scrollListener);
	    
	    movieScrollPane.getViewport().addChangeListener(new ChangeListener() {

        public void stateChanged(ChangeEvent e) {
          // TODO Auto-generated method stub
          moviesPanel.revalidate();
        }
        
	    });
	    
	    //codeArea = new JTextArea();
	    codeArea = new JEditTextArea(new RehearseTextAreaDefaults());
	    //JScrollPane codeScrollPane = new JScrollPane(codeArea);
	    
	    bigMovie = new BigMovieView();
   
	    hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
	        codeArea, bigMovie);
	    
	    vSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
	        hSplitPane, movieScrollPane);
	      
	    vSplitPane.setDividerLocation(300);
	    hSplitPane.setDividerLocation(400);
   	   
	    setPreferredSize(new Dimension(700, 600));
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
	  
	  /*
	   * Changes version number
	   * - update code window to show that version
	   * - diffs are used to shift scroll position of code box
	   *   to stay focused on same region
	   * - BigMovie view needs to change
	   * - (later) take care of highlighting appropriate lines in diff
	   */
	  private void setVersionAndScroll(int toVersion, String fromVersionCode, 
	                               int fromVersionLineNumber) {
	    VersionHistory toVersionModel = controller.getVersion(toVersion);
	    String toVersionCode = toVersionModel.getCode();
	    int toVersionLineNumber = getLineNumberToScrollTo(fromVersionCode, 
	        toVersionCode, fromVersionLineNumber);
	    
	    this.currVersion = toVersion;
	    codeArea.setText(toVersionCode);
	    
	    //  TODO (Abel): Perhaps a better scrolling scheme here.
	    codeArea.scrollTo(toVersionLineNumber, 1);
	    codeArea.setCaretPosition(codeArea.getLineStartOffset(toVersionLineNumber));
	    
	    bigMovie.setRecordingJump(toVersionModel.getVideoFilename(), 0);
	  }
    
    // Need to know
    //   1. Line number in current version
    //      This number changes only when the carrot pos changes
    //      in the editor
    //   2. Version number we're viewing in the history frame
    //        Frame needs to know version number when it is updated
    //        
	  
	  public void setVersionNumber(int toVersion) {
	    if (currVersion == toVersion) return;
	    setVersionAndScroll(toVersion, codeArea.getText(), codeArea.getCaretLine());
	  }
	  
	  public void scrollWithEditorCaret(String editorCode, int caretLineNumber) {
	    if (currVersion == -1) return;
	    setVersionAndScroll(currVersion, editorCode, caretLineNumber);
	  }
    
    private int getLineNumberToScrollTo(String fromVersionCode, String toVersionCode, 
        int fromVersionLineNumber) {
      // TODO (Abel): Integrate the line number routine.
      return fromVersionLineNumber;
    }
	  
	  
	  static public class ScrollableFlowPanel extends JPanel implements Scrollable {

	    public ScrollableFlowPanel(LayoutManager layout) {
	      super(layout);
	    }

	    public void setBounds( int x, int y, int width, int height ) {
	      super.setBounds( x, y, getParent().getWidth(), height );
	    }

	    public Dimension getPreferredSize() {
	      return new Dimension( getWidth(), getPreferredHeight() );
	    }

	    public Dimension getPreferredScrollableViewportSize() {
	      return super.getPreferredSize();
	    }

	    public int getScrollableUnitIncrement( Rectangle visibleRect, int orientation, int direction ) {
	      int hundredth = ( orientation ==  SwingConstants.VERTICAL
	          ? getParent().getHeight() : getParent().getWidth() ) / 100;
	      return ( hundredth == 0 ? 1 : hundredth ); 
	    }

	    public int getScrollableBlockIncrement( Rectangle visibleRect, int orientation, int direction ) {
	      return orientation == SwingConstants.VERTICAL ? getParent().getHeight() : getParent().getWidth();
	    }

	    public boolean getScrollableTracksViewportWidth() {
	      return true;
	    }

	    public boolean getScrollableTracksViewportHeight() {
	      return false;
	    }

	    private int getPreferredHeight() {
	      int rv = 0;
	      for ( int k = 0, count = getComponentCount(); k < count; k++ ) {
	        Component comp = getComponent( k );
	        Rectangle r = comp.getBounds();
	        int height = r.y + r.height;
	        if ( height > rv )
	          rv = height;
	      }
	      rv += ( (FlowLayout) getLayout() ).getVgap();
	      return rv;
	    }
	  }
}