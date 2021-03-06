package edu.stanford.hci.rehearse;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;

import javax.swing.JMenu;

import processing.app.Base;
import processing.app.Editor;
import processing.app.EditorToolbar;

public class RehearseToolbar extends EditorToolbar{
	private Editor editor;
	private RehearseHandler rh;
	boolean useHighlight = true;
	
	  /** Rollover titles for each button. */
	static final String newTitle[] = {
		"Run", "Stop", "New", "Open", "Save", "Export", "Interactive Run", "Clear Highlights"	
	  };
	  
	  /** Titles for each button when the shift key is pressed. */ 
	static final String newTitleShift[] = {
		"Present", "Stop", "New Editor Window", "Open in Another Window", "Save", 
		"Export to Application", "Interactive Run", "Turn Highlights Off"
	    };
	
	protected static final int INTERACTIVE_RUN = 6;
	protected static final int TOGGLE_HIGHLIGHTS = 7;
	
	public RehearseToolbar(Editor editor, JMenu menu) {
		rh = new RehearseHandler(editor);
		title = newTitle;
		titleShift = newTitleShift;
		this.editor = editor;
	    this.menu = menu;
	    BUTTON_COUNT += 2;
	    bgcolor = new Color(0, 51, 102);
	    buttonCount = 0;
	    which = new int[BUTTON_COUNT];
	    
	    which[buttonCount++] = RUN;
	    which[buttonCount++] = STOP;
	    which[buttonCount++] = NEW;
	    which[buttonCount++] = OPEN;
	    which[buttonCount++] = SAVE;
	    which[buttonCount++] = EXPORT;
	    which[buttonCount++] = INTERACTIVE_RUN;
	    which[buttonCount++] = TOGGLE_HIGHLIGHTS;
	}
	
	@Override
	  public void mousePressed(MouseEvent e) {
	    final int x = e.getX();
	    final int y = e.getY();

	    int sel = findSelection(x, y);
	    
	    if (sel == -1) return;
	    currentRollover = -1;

	    switch (sel) {
	    case INTERACTIVE_RUN:
	    	rh.handleInteractiveRun();
	    	break;
	    
	    case TOGGLE_HIGHLIGHTS:
	    	editor.getTextArea().getPainter().removeCustomHighlights();    	
	    	break;
	    	
	    case RUN:
	      rh.handleRun(e.isShiftDown());
	      break;

	    case STOP:
	      rh.handleStop();
	      break;

	    case OPEN:
	      popup = menu.getPopupMenu();
	      popup.show(RehearseToolbar.this, x, y);
	      break;

	    case NEW:
	      if (shiftPressed) {
	        editor.getBase().handleNew();
	      } else {
	        editor.getBase().handleNewReplace();
	      }
	      break;

	    case SAVE:
	      editor.handleSave(false);
	      break;

	    case EXPORT:
	      if (e.isShiftDown()) {
	        editor.handleExportApplication();
	      } else {
	    	  editor.handleExport();
	      	}
	      break;
	    }
	}
	
	protected void loadButtons() {
	    Image allButtons = Base.getThemeImage("rehearsebuttons.gif", this);
	    buttonImages = new Image[BUTTON_COUNT][3];
	    
	    for (int i = 0; i < BUTTON_COUNT; i++) {
	      for (int state = 0; state < 3; state++) {
	        Image image = createImage(BUTTON_WIDTH, BUTTON_HEIGHT);
	        Graphics g = image.getGraphics();
	        g.drawImage(allButtons, 
	                    -(i*BUTTON_IMAGE_SIZE) - 3, 
	                    (-2 + state)*BUTTON_IMAGE_SIZE, null);
	        buttonImages[i][state] = image;
	      }
	    }
	  }
}
