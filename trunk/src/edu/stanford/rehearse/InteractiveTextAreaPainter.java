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
	
	public void markUndo() {
		int lastCommandStart = commandBreakLines.get(commandBreakLines.size()-2);
		int caretLine = textArea.getCaretLine();
		for(int i = lastCommandStart; i <= caretLine-1; i++) {
			undoLines.add(i);
		}
		invalidateLineRange(lastCommandStart, caretLine-1);
		commandBreakLines.remove(commandBreakLines.size()-1);
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
}
