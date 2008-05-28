package edu.stanford.rehearse.undo2;
import edu.stanford.rehearse.CodeElement;
import edu.stanford.rehearse.InteractiveTextAreaPainter;
import edu.stanford.rehearse.UndidLinesList;
import edu.stanford.rehearse.UndidLinesList.UndidLinesListModel;
import edu.stanford.rehearse.undo1.InteractiveTextArea;

public class InteractiveTextArea2 extends InteractiveTextArea {

	private UndidLinesList undidLines;
	
	public InteractiveTextArea2(int uid, int functionNum, int initialSnapshot, 
			UndidLinesList undidLines) {
		super(uid, functionNum, initialSnapshot);
		this.undidLines = undidLines;
	}
	
	protected void setupMouseListener() {
		// override and do nothing
	}
	
	protected void updateRedoLines() {
		undidLines.setRedoLines(codeTree.getRedoLineNums());
	}

	
	public void undo() {
		CodeElement curr = codeTree.getCurr();
		int lineNum = curr.getLineNum();
		int snapshotId = codeTree.undo();
		updateRedoLines();
		if(snapshotId == -1) return;
		setText(getText(0, getLineStartOffset(lineNum)) + getLineText(getCaretLine()));

		String command = "load(" + snapshotId + ");";
		addCommandToQueue(command, true);

		//((InteractiveTextAreaPainter)getPainter()).mark(lineNum, true);
		undidLines.getUndidLinesListModel().addCodeElement(curr);
	}
	
	public void redo(CodeElement codeElem) {
		int snapshotId = codeTree.redo(codeElem);
		codeElem.setLineNum(getCaretLine());
		updateRedoLines();
		if(snapshotId == -1) return;

		((InteractiveTextAreaPainter)getPainter()).shiftLines(getCaretLine(), 2);
		
		setText(getText(0, getLineStartOffset(getCaretLine())) + codeElem.getCode()
				+ "\n" + codeElem.getResponse() + "\n" + getLineText(getCaretLine()));
		String command = "load(" + snapshotId + ");";
		addCommandToQueue(command, true);
		//((InteractiveTextAreaPainter)getPainter()).mark(lineNum, false);
		undidLines.getUndidLinesListModel().removeCodeElement(codeElem);
	}
	
}