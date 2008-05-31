package edu.stanford.rehearse.undo4;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.rehearse.CodeElement;
import edu.stanford.rehearse.CodeMap;
import edu.stanford.rehearse.InteractiveTextAreaPainter;
import edu.stanford.rehearse.UndidLinesList;
import edu.stanford.rehearse.undo1.InteractiveTextArea;
import edu.stanford.rehearse.undo3.InteractiveTextArea3;

public class InteractiveTextArea4 extends InteractiveTextArea {
	
	protected CodeMap codeMap;
	private UndidLinesList undidLines;

	public InteractiveTextArea4(int uid, int functionNum, int initialSnapshot,
			UndidLinesList undidLines) {
		super(uid, functionNum, initialSnapshot);
		codeMap = new CodeMap();
		this.undidLines = undidLines;
	}
	
	protected void executeStatement(String statement, boolean active) {
		super.executeStatement(statement, active);
		codeMap.add(codeTree.getCurr());
	}
	
	protected void updateRedoLines() {
		//override and do nothing
	}
	
	public void undo(boolean active) {
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
		if(actual) {
			String command = "load(" + snapshotId + ");";
			addCommandToQueue(command, true);
			if(pairTextArea != null)
				((InteractiveTextArea3)pairTextArea).undo(numUndoSteps, false);
		}
	}
	
	protected void setupMouseListener() {
		this.getPainter().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int line = getCaretLine();
				if(line != getLineCount()-1) {
					if(codeMap.isLineActive(line)) {
						undoToLine(line, true);
					} 
				}
			}
		});
	}

}
