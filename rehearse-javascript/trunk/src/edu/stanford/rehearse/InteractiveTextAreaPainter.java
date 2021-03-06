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
		setEOLMarkersPainted(false);
		setInvalidLinesPainted(false);
		commandBreakLines.add(0);
		System.out.println("NEW PAINTER SIZE:" + responseLines.size());
	}
	
	public void shiftLines2(int startIndex, int delta) {
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
	
	public void markCommandBreak() {
		commandBreakLines.add(textArea.getCaretLine());
	}
	
	public void mark(int cmdLine, boolean undo, boolean error) {
		int index = commandBreakLines.indexOf(cmdLine);
		int endLine = 0;
		if(index == commandBreakLines.size() -1) {
			endLine = textArea.getCaretLine();
		} else {
			endLine = commandBreakLines.get(index+1);
		}
		for(int i = cmdLine; i < endLine; i++) {
			if(undo) {
				undoLines.add(i);
				if(i == endLine - 1) {
					if(error)
						errorLines.remove(i);
					else
						responseLines.remove(i);
				}
			} else {
				undoLines.remove(i);
				if(i == endLine - 1) {
					if(error)
						errorLines.add(i);
					else
						responseLines.add(i);
				}
			}
		}
		invalidateLineRange(cmdLine, endLine-1);
	}
	
	@Override
	protected void paintSyntaxLine(Graphics gfx, TokenMarker tokenMarker,
			int line, Font defaultFont, Color defaultColor, int x, int y) {
		
		Color c;
		Font f = defaultFont;
		if(undoLines.contains(line)) {
			c = Color.lightGray;
		} else if(responseLines.contains(line)) {
			c = Color.blue;
			f = defaultFont.deriveFont(Font.ITALIC);
		} else if(errorLines.contains(line)) {
			c = Color.red;
			f = defaultFont.deriveFont(Font.ITALIC);
		} else {
			c = defaultColor;
		}
		
		
		if(c == defaultColor)
			super.paintSyntaxLine(gfx, tokenMarker, line, f, c, x, y);
		else
			super.paintPlainLine(gfx, line, f, c, x, y);
	}
	
	public int getLastResponseLine() {
		Set<Integer> c = new HashSet<Integer>();
		c.addAll(responseLines);
		c.addAll(errorLines);
		if(c.isEmpty()) return -1;
		return Collections.max(c);
	}
	
	public void setCeiling(int lineNum) {
		HashSet<Integer> newSet = new HashSet<Integer>();
		for(int r: responseLines) {
			if(r < lineNum)
				newSet.add(r);
		}
		responseLines = newSet;
		
		HashSet<Integer> newSet2 = new HashSet<Integer>();
		for(int r: errorLines) {
			if(r < lineNum)
				newSet2.add(r);
		}
		errorLines = newSet2;
		
		List<Integer> list = new ArrayList<Integer>();
		for(int r: commandBreakLines) {
			if(r < lineNum)
				list.add(r);
		}
		commandBreakLines = list;
	}
}
