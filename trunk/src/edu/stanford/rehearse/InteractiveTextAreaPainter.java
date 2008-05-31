package edu.stanford.rehearse;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.*;

import org.jedit.syntax.JEditTextArea;
import org.jedit.syntax.TextAreaDefaults;
import org.jedit.syntax.TextAreaPainter;
import org.jedit.syntax.TokenMarker;

public class InteractiveTextAreaPainter extends TextAreaPainter {

	private Set<Integer> responseLines = new HashSet<Integer>();
	private Set<Integer> errorLines = new HashSet<Integer>();
	private Set<Integer> undoLines = new HashSet<Integer>();
	private List<Integer> commandBreakLines = new ArrayList<Integer>();
	
	public InteractiveTextAreaPainter(JEditTextArea textArea,
			TextAreaDefaults defaults) {
		super(textArea, defaults);
		commandBreakLines.add(0);
	}
	
	public void shiftLines(int startIndex, int delta) {
		Set<Integer> newResponseLines = new HashSet<Integer>();
		for(int i : responseLines) {
			if(i >= startIndex)
				newResponseLines.add(i+delta);
			else
				newResponseLines.add(i);
		}
		responseLines = newResponseLines;
		
		Set<Integer> newErrorLines = new HashSet<Integer>();
		for(int i : errorLines) {
			if(i >= startIndex)
				newErrorLines.add(i+delta);
			else
				newErrorLines.add(i);
		}
		errorLines = newErrorLines;
		
		Set<Integer> newUndoLines = new HashSet<Integer>();
		for(int i : undoLines) {
			if(i >= startIndex)
				newUndoLines.add(i+delta);
			else
				newUndoLines.add(i);
		}
		undoLines = newUndoLines;
		
		List<Integer> newBreakLines = new ArrayList<Integer>();
		for(int i : commandBreakLines) {
			if(i >= startIndex)
				newBreakLines.add(i+delta);
			else
				newBreakLines.add(i);
		}
		commandBreakLines = newBreakLines;
	}
	
	public void markResponse(int startLine, int endLine, boolean noError) {
		for(int i = startLine; i <= endLine; i++) {
			if(noError)
				responseLines.add(i);
			else
				errorLines.add(i);
				
		}
		invalidateLineRange(startLine, endLine);
		commandBreakLines.add(textArea.getCaretLine());
	}
	
	public void mark(int cmdLine, boolean undo) {
		int index = commandBreakLines.indexOf(cmdLine);
		int endLine = 0;
		if(index == commandBreakLines.size() -1) {
			endLine = textArea.getCaretLine();
		} else {
			endLine = commandBreakLines.get(index+1);
		}
		for(int i = cmdLine; i < endLine; i++) {
			if(undo)
				undoLines.add(i);
			else
				undoLines.remove(i);
		}
		invalidateLineRange(cmdLine, endLine-1);
	}
	
	@Override
	protected void paintSyntaxLine(Graphics gfx, TokenMarker tokenMarker,
			int line, Font defaultFont, Color defaultColor, int x, int y) {
		
		Color c;
		if(undoLines.contains(line)) {
			c = Color.lightGray;
		} else if(responseLines.contains(line)) {
			c = Color.blue;
		} else if(errorLines.contains(line)) {
			c = Color.red;
		} else {
			c = defaultColor;
		}
		
		if(c == defaultColor)
			super.paintSyntaxLine(gfx, tokenMarker, line, defaultFont, c, x, y);
		else
			super.paintPlainLine(gfx, line, defaultFont, c, x, y);
	}
	
	public int getLastResponseLine() {
		if(responseLines.isEmpty())
			return -1;
		return Collections.max(responseLines);
	}
}
