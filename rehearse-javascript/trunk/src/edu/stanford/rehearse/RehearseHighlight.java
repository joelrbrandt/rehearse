package edu.stanford.rehearse;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.*;

import org.jedit.syntax.JEditTextArea;
import org.jedit.syntax.TextAreaPainter.Highlight;

public class RehearseHighlight implements Highlight {

	private JEditTextArea textArea;
	
	private Set<Integer> redoLines = new HashSet<Integer>();
	
	public String getToolTipText(MouseEvent evt) {
		return null;
	}

	public void init(JEditTextArea textArea, Highlight next) {
		this.textArea = textArea;
	}
	
	public void setRedoLines(Set<Integer> redoLines) {
		Set<Integer> oldLines = this.redoLines;
		this.redoLines = redoLines;
		for(int line : redoLines) {
			textArea.getPainter().invalidateLine(line);
		}
		for(int line : oldLines) {
			textArea.getPainter().invalidateLine(line);
		}
	}

	public void paintHighlight(Graphics gfx, int line, int y) {
		//int height = textArea.getPainter().getFontMetrics().getHeight() + 1;
		int height = gfx.getFontMetrics().getHeight();
		y += gfx.getFontMetrics().getLeading() + gfx.getFontMetrics().getMaxDescent();
		if(redoLines.contains(line)) {
			gfx.setColor(new Color(255, 250, 205));
		} else if(textArea.getCaretLine() == line) {
			gfx.setColor(new Color(240, 240, 240));
		} else {
			gfx.setColor(Color.white);
		}
		gfx.fillRect(0, y, textArea.getWidth(), height);
	}

}
