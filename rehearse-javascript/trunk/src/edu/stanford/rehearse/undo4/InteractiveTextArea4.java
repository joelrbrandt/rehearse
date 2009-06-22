package edu.stanford.rehearse.undo4;

import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import edu.stanford.rehearse.CodeElement;
import edu.stanford.rehearse.CodeMap;
import edu.stanford.rehearse.InteractiveTextAreaPainter;
import edu.stanford.rehearse.UndidLinesList;
import edu.stanford.rehearse.undo1.InteractiveTextArea;
import edu.stanford.rehearse.undo1.Rehearse;
import edu.stanford.rehearse.undo3.InteractiveTextArea3;

public class InteractiveTextArea4 extends InteractiveTextArea {
	
	private UndidLinesList undidLines;

	public InteractiveTextArea4(int uid, int functionNum, int initialSnapshot,
			UndidLinesList undidLines, Rehearse rehearse) {
		super(uid, functionNum, initialSnapshot, rehearse);
		this.undidLines = undidLines;
	}
	
	protected void updateRedoLines() {
		//override and do nothing
	}
	
	public void undo(boolean active) {
		undo(1, active);
		/*
		CodeElement curr = codeTree.getCurr();
		int lineNum = curr.getLineNum();
		int snapshotId = codeTree.undo();
		if(snapshotId == -1) return;
		setText(getText(0, getLineEndOffset(lineNum)));

		if(active) {
			String command = "load(" + snapshotId + ");";
			addCommandToQueue(command, true);
			if(pairTextArea != null)
				((InteractiveTextArea3)pairTextArea).undo(false);
		}

		undidLines.getUndidLinesListModel().addCodeElement(curr);
		*/
	}
	
	public void undoToLine(int lineNum, boolean actual) {
		int numUndoSteps = codeTree.stepsToLine(lineNum);
		if(numUndoSteps != -1)
			undo(numUndoSteps, actual);
	}
	
	public void undo(int numUndoSteps, boolean actual) {
		
		int snapshotId = codeTree.undo(numUndoSteps, undidLines.getUndidLinesListModel());
		if(snapshotId == -1) return;
		
		int lineNum = codeTree.getLastUndid().getLineNum();
		setText(getText(0, getLineStartOffset(lineNum)));
		setCaretPosition(getDocumentLength());
		((InteractiveTextAreaPainter)getPainter()).setCeiling(getLineCount());
		if(actual) {
			String command = "load(" + snapshotId + ");";
			addCommandToQueue(command, true);
			if(pairTextArea != null)
				((InteractiveTextArea3)pairTextArea).undo(numUndoSteps, false);
		}
	}
	
	protected void setupMouseListener() {
		//removeAllMouseListeners();
		this.getPainter().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e)) return;
				//int line = getCaretLine();
				int line = yToLine(e.getY());
				if(line != getLineCount()-1 ) {
					if(codeMap.isLineActive(line) && e.getClickCount() == 2) {
						undoToLine(line, true);
					} else{
						 Toolkit.getDefaultToolkit().beep();
					}
				} else {
					int offset = xToOffset(line, e.getX());
					int dot = getLineStartOffset(line) + offset;
					setCaretPosition(dot);
				}
			}
		});
		
		this.getPainter().addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				
					int lineNum = yToLine(e.getY());
					if(codeMap.isLineActive(lineNum)) {
						rehearse.updateInstructions("Double-click to undo to this line");
					} else {
						rehearse.updateInstructions("");
					}
			}
		});
	}

}
