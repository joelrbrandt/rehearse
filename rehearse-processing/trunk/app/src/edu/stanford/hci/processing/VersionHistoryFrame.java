package edu.stanford.hci.processing;

import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public abstract class VersionHistoryFrame extends JFrame {
	public static final int ROW_HEIGHT = 60;
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
	    
	    moviesPanel = new ScrollableFlowPanel(new FlowLayout(FlowLayout.LEFT, 1, 5));
	    moviesPanel.setPreferredSize(new Dimension(700, 500));
	    JScrollPane movieScrollPane = new JScrollPane(moviesPanel);
	    movieScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	    movieScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//	    movieScrollPane.setMinimumSize(new Dimension(0, 400));
//	    AdjustmentListener scrollListener = new ScrollAdjustmentListener();
//	    movieScrollPane.getVerticalScrollBar().addAdjustmentListener(scrollListener);
	    
	    movieScrollPane.getViewport().addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          moviesPanel.revalidate();
        }
	    });
	    
	    codeArea = new JTextArea();
	    JScrollPane codeScrollPane = new JScrollPane(codeArea);
	    
	    bigMovie = new BigMovieView();
   
	    hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
	        codeScrollPane, bigMovie);
	    
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