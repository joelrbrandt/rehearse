package edu.stanford.rehearse;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.*;

import org.jedit.syntax.JEditTextArea;
import org.jedit.syntax.TextAreaPainter.Highlight;

public class RehearseHighlight implements Highlight {

	private JEditTextArea textArea;
	//private Set<Integer> responseLines = new HashSet<Integer>();
	//private Set<Integer> undoLines = new HashSet<Integer>();
	
	private Set<Integer> redoLines = new HashSet<Integer>();
	
	public String getToolTipText(MouseEvent evt) {
		return null;
	}

	public void init(JEditTextArea textArea, Highlight next) {
		this.textArea = textArea;
	}
	
	/*
	public void markResponse(int startLine, int endLine) {
		for(int i = startLine; i <= endLine; i++) {
			responseLines.add(i);
		}
		textArea.getPainter().invalidateLineRange(startLine, endLine);
	}
	
	public void markUndo() {
		for(int i = lastUndoUnit; i <= textArea.getCaretLine()-1; i++) {
			undoLines.add(i);
		}
		textArea.getPainter().invalidateLineRange(lastUndoUnit, textArea.getCaretLine()-1);
	}*/
	
	public void setRedoLines(Set<Integer> redoLines) {
		this.redoLines = redoLines;
		System.out.println("REDOLINES: " + redoLines);
		for(int line : redoLines) {
			textArea.getPainter().invalidateLine(line);
		}
	}

	public void paintHighlight(Graphics gfx, int line, int y) {
		int height = textArea.getPainter().getFontMetrics().getHeight() + 1;
		if(redoLines.contains(line)) {
			gfx.setColor(new Color(255, 250, 205));
		} else {
			gfx.setColor(Color.white);
		}
		gfx.fillRect(0,y + 3, textArea.getWidth(), height);
	}

}
