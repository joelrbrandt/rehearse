package edu.stanford.hci.processing;

import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

import processing.app.syntax.JEditTextArea;
import processing.app.syntax.RehearseTextAreaDefaults;
import processing.app.syntax.TextAreaPainter;
import processing.app.syntax.TextAreaPainter.Highlight;

//import edu.stanford.hci.processing.VersionHistoryFrameiMovie.VersionHistoryPanel;
//import edu.stanford.hci.processing.editor.RehearseLineModel;


public abstract class VersionHistoryFrame extends JFrame {
	public static final int ROW_HEIGHT = 60;
	protected static final Color selectedColor = new Color(150,255,150);
	
	protected final VersionHistoryController controller;
	protected static final Color DELETION = new Color(0xff, 0xa0, 0xa0);
	protected static final Color INSERTION = new Color(0xa0, 0xff, 0xa0);
	protected static final Color CHANGE = new Color(0xff, 0xff, 0xa0);
	
	private int currVersion;

	protected JPanel moviesPanel;
	
	JEditTextArea codeArea;
	BigMovieView bigMovie;
	
	protected JSplitPane hSplitPane;
	protected JSplitPane vSplitPane;
	
	protected Color[] lineColors;
	
	public VersionHistoryFrame(final VersionHistoryController controller) {
	    super("Version History");
	    this.controller = controller;
	    this.currVersion = -1;
	    
	    codeArea = new JEditTextArea(new RehearseTextAreaDefaults());
	    codeArea.setMinimumSize(new Dimension(50,50));
	    
	    bigMovie = new BigMovieView();
	    bigMovie.frame = this;
	    final JPanel bigMoviePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
	    bigMoviePanel.add(bigMovie);
	    bigMoviePanel.addComponentListener(new ComponentAdapter() {
	      @Override
	      public void componentResized(ComponentEvent e) {
	        Dimension d = bigMoviePanel.getSize();
	        int dim = Math.min(d.width, d.height);
	        bigMovie.setPreferredSize(new Dimension(dim,dim));
	        bigMoviePanel.revalidate();
	      }
	    });
   
	    hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
	        codeArea, bigMoviePanel);
	    
	    vSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
	        hSplitPane, makeMoviePane());
	      
	    vSplitPane.setDividerLocation(300);
	    hSplitPane.setDividerLocation(400);
   	   
	    setPreferredSize(new Dimension(700, 700));
	    bigMovie.init();
	  }
	
	  protected JComponent makeMoviePane() {
	    return new JPanel();
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
     
      //System.out.println("Carrot line: " + codeArea.getCaretLine());
	    
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
      int result = fromVersionLineNumber;
      
      int lineNumber = fromVersionLineNumber;
      int numInsertedBelow = 0;
      
      Patch patch = DiffUtils.diff(Arrays.asList(fromVersionCode.split("\n")),
                     Arrays.asList(toVersionCode.split("\n")));
      
      lineColors = new Color[toVersionCode.split("\n").length];
      Arrays.fill(lineColors, Color.WHITE);
      for (Delta delta: patch.getDeltas()) {
        if (delta.type == Delta.INSERTION) {
          int begin = delta.getRevised().getPosition();
          int size = delta.getRevised().getSize();
          for (int i=begin; i<begin+size; i++) {
            lineColors[i] = VersionHistoryFrame.INSERTION;
          }
        } else if (delta.type == Delta.DELETION) {
          int begin = delta.getRevised().getPosition(); 
          lineColors[ Math.min(begin, lineColors.length-1)] = 
             VersionHistoryFrame.DELETION;
        } else if (delta.type == Delta.CHANGE) {
          int begin = delta.getRevised().getPosition();
          int size = delta.getRevised().getSize();
          for (int i=begin; i<begin+size; i++) {
            lineColors[i] = VersionHistoryFrame.CHANGE;
          } 
        }
      }
      this.codeArea.getPainter().removeCustomHighlights();
      this.codeArea.getPainter().addCustomHighlight(new VersionHistoryHighlight());
      
      for (Delta delta: patch.getDeltas()) {
        //System.out.println(delta);
        int pos = delta.getOriginal().getPosition();
        if (pos <= lineNumber) {
          if (delta.type == Delta.INSERTION) {
            numInsertedBelow += delta.getRevised().getSize();
          } else if (delta.type == Delta.DELETION) {
            numInsertedBelow -= delta.getOriginal().getSize();
          } else if (delta.type == Delta.CHANGE) {
             
          }
        }
      }
      //System.out.println("Num Inserted Below: " + numInsertedBelow);
      
      result += numInsertedBelow;
      //result -= (codeArea.getVisibleLines() / 2);
      
      result = Math.max(0, result);
      result = Math.min(toVersionCode.split("\n").length, result);
    
      return result;
    }
    
    public void setVersionFilter(Set<Integer> versions) {
      
    }
	  
    private class VersionHistoryHighlight implements TextAreaPainter.Highlight {
      JEditTextArea textarea;
      Highlight next;

      public String getToolTipText(MouseEvent evt) {
        if (next != null) {
          return null;
        }
        return null;
      }

      public void init(JEditTextArea textArea, Highlight next) {
        textarea = textArea;
        this.next = next;
      }

      public void paintHighlight(Graphics gfx, int line, int y) {
        // Interpreter uses one-offset, processing uses zero-offset.
        Color c = null;
        //RehearseLineModel m = (RehearseLineModel) getTextArea()
        //    .getTokenMarker().getLineModelAt(line);
        
        if (lineColors != null && line < lineColors.length) {
          c = lineColors[line];
        }
        
//        if (m != null) {
//          /*
//           * if (m.executedInLastRun) c = Color.yellow; if
//           * (m.isMostRecentlyExecuted) c = Color.green;
//           */
//
//          int i = Math.min(linesExecutedCount - m.countAtLastExec, 150);
//          // c = new Color(i,255,i);
//          c = new Color(78, 127, 78, 200 - i);
//        }

        // Color c = lineHighlights.get(line + 1);
        if (c != null) {
          FontMetrics fm = textarea.getPainter().getFontMetrics();
          int height = fm.getHeight();
          y += fm.getLeading() + fm.getMaxDescent();
          gfx.setColor(c);
          if (c.equals(VersionHistoryFrame.DELETION)) {
            gfx.fillRect(0, y-2, getWidth(), 4);
          } else {
            gfx.fillRect(0, y, getWidth(), height);
          }
          
        }

        if (next != null) {
          next.paintHighlight(gfx, line, y);
        }
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