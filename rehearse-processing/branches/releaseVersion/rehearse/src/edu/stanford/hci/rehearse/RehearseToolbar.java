package edu.stanford.hci.rehearse;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JMenu;

import processing.app.Base;
import processing.app.Editor;
import processing.app.EditorToolbar;
import processing.app.Theme;

public class RehearseToolbar extends EditorToolbar{

	  /** Rollover titles for each button. */
	static final String newTitle[] = {
		"Interactive Run", "Run", "Stop", "New", "Open", "Save", "Export"
	  };
	  
	  /** Titles for each button when the shift key is pressed. */ 
	static final String newTitleShift[] = {
		"Interactive Run", "Present", "Stop", "New Editor Window", 
		"Open in Another Window", "Save", "Export to Application"
	    };
	  
	static final int INTERACTIVE_RUN = 6;
	public RehearseToolbar(Editor editor, JMenu menu) {
		title = newTitle;
		titleShift = newTitleShift;
		this.editor = editor;
	    this.menu = menu;
	    BUTTON_COUNT++;
	    
	    buttonCount = 0;
	    which = new int[BUTTON_COUNT];

	    which[buttonCount++] = INTERACTIVE_RUN;
	    which[buttonCount++] = RUN;
	    which[buttonCount++] = STOP;
	    which[buttonCount++] = NEW;
	    which[buttonCount++] = OPEN;
	    which[buttonCount++] = SAVE;
	    which[buttonCount++] = EXPORT;
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
