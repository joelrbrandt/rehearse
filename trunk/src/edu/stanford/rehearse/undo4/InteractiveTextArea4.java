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

public class InteractiveTextArea4 extends InteractiveTextArea {
	
	protected CodeMap codeMap;
	private UndidLinesList undidLines;

	public InteractiveTextArea4(int uid, int functionNum, int initialSnapshot,
			UndidLinesList undidLines) {
		super(uid, functionNum, initialSnapshot);
		codeMap = new CodeMap();
		this.undidLines = undidLines;
	}
	
	protected void executeStatement() {
		super.executeStatement();
		codeMap.add(codeTree.getCurr());
	}
	
	protected void updateRedoLines() {
		//override and do nothing
	}
	
	public void undo() {
		CodeElement curr = codeTree.getCurr();
		int lineNum = curr.getLineNum();
		int snapshotId = codeTree.undo();
		if(snapshotId == -1) return;
		setText(getText(0, getLineEndOffset(lineNum)));

		String command = "load(" + snapshotId + ");";
		addCommandToQueue(command, true);

		undidLines.getUndidLinesListModel().addCodeElement(curr);
	}
	
	public void undo(int lineNum) {
		int snapshotId = codeTree.undo(lineNum, undidLines.getUndidLinesListModel());
		if(snapshotId == -1) return;
		
		setText(getText(0, getLineEndOffset(getCaretLine())-1));
		setCaretPosition(getDocumentLength());
		
		String command = "load(" + snapshotId + ");";
		addCommandToQueue(command, true);
	}
	
	protected void setupMouseListener() {
		this.getPainter().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int line = getCaretLine();
				if(line != getLineCount()-1) {
					if(codeMap.isLineActive(line)) {
						undo(line);
					} 
				}
			}
		});
	}

}
