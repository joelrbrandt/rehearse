package edu.stanford.rehearse.undo2;
import java.awt.Toolkit;

import edu.stanford.rehearse.CodeElement;
import edu.stanford.rehearse.InteractiveTextAreaPainter;
import edu.stanford.rehearse.UndidLinesList;
import edu.stanford.rehearse.UndidLinesList.UndidLinesListModel;
import edu.stanford.rehearse.undo1.InteractiveTextArea;
import edu.stanford.rehearse.undo1.Rehearse;

public class InteractiveTextArea2 extends InteractiveTextArea {

	private UndidLinesList undidLines;
	
	public InteractiveTextArea2(int uid, int functionNum, int initialSnapshot, 
			UndidLinesList undidLines, Rehearse rehearse) {
		super(uid, functionNum, initialSnapshot, rehearse);
		this.undidLines = undidLines;
	}
	
	protected void setupMouseListener() {
		// override and do nothing
	}
	
	protected void updateRedoLines() {
		undidLines.setRedoLines(codeTree.getRedoLineNums());
	}
	
	public void undo(boolean actual) {
		CodeElement curr = codeTree.getCurr();
		int lineNum = curr.getLineNum();
		int snapshotId = codeTree.undo();
		updateRedoLines();
		if(snapshotId == -1) return;
		int lineStart = getLineStartOffset(lineNum);
		if(lineStart < 0) lineStart = 0;
		setText(getText(0, lineStart) + getLineText(getCaretLine()));
		undidLines.getUndidLinesListModel().addCodeElement(curr);

		((InteractiveTextAreaPainter)getPainter()).setCeiling(getLineCount());
		if(actual) {
			String command = "load(" + snapshotId + ");";
			addCommandToQueue(command, true);
			if(pairTextArea != null)
				pairTextArea.undo(false);
		}
	}
	
	public void redo(CodeElement codeElem, boolean actual) {
		int snapshotId = codeTree.redo(codeElem);
		codeElem = codeTree.getCurr();
		codeElem.setLineNum(getCaretLine());
		updateRedoLines();
		if(snapshotId == -1) return;

		InteractiveTextAreaPainter painter = ((InteractiveTextAreaPainter)getPainter());
		//painter.shiftLines(getCaretLine(), countLines(codeElem.getCode()));
		
		int lineStart = getLineStartOffset(getCaretLine());
		if(lineStart < 0) lineStart = 0;
		setText(getText(0, lineStart) + codeElem.getCode()
				+ "\n" + codeElem.getResponse() + "\n" + getLineText(getCaretLine()));
		undidLines.getUndidLinesListModel().removeCodeElement(codeElem);
		
		painter.markCommandBreak();
		painter.mark(codeElem.getLineNum(), false, codeElem.isError());
		
		if(actual) {
			String command = "load(" + snapshotId + ");";
			addCommandToQueue(command, true);
			if(pairTextArea != null)
				pairTextArea.redo(codeTree.getCurr(), false);
		}
	}
	
}