package edu.stanford.rehearse.undo3;

import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.rehearse.CodeElement;
import edu.stanford.rehearse.CodeMap;
import edu.stanford.rehearse.InteractiveTextAreaPainter;
import edu.stanford.rehearse.undo1.InteractiveTextArea;
import edu.stanford.rehearse.undo4.InteractiveTextArea4;

public class InteractiveTextArea3 extends InteractiveTextArea {
	
	protected CodeMap codeMap;

	public InteractiveTextArea3(int uid, int functionNum, int initialSnapshot) {
		super(uid, functionNum, initialSnapshot);
		codeMap = new CodeMap();
	}
	
	protected void executeStatement(String statement, boolean actual) {
		
		super.executeStatement(statement, actual);
		codeMap.add(codeTree.getCurr());
	}
	
	protected void updateRedoLines() {
		//override and do nothing
	}
	
	public void undoToLine(int lineNum, boolean actual) {
		int numUndoSteps = codeTree.stepsToLine(lineNum);
		if(numUndoSteps != -1)
			undo(numUndoSteps, actual);
	}
	
	public void undo(boolean actual) {
		undo(1, actual);
	}
	
	public void undo(int numUndoSteps, boolean actual) {
		int snapshotId = codeTree.undo(numUndoSteps, (InteractiveTextAreaPainter)getPainter());
		if(snapshotId == -1) return;
		setCaretPosition(getDocumentLength());
		if(actual) {
			String command = "load(" + snapshotId + ");";
			addCommandToQueue(command, true);
			if(pairTextArea != null)
				((InteractiveTextArea4)pairTextArea).undo(numUndoSteps, false);
		}
	}
	
	protected void setupMouseListener() {
		this.getPainter().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				//int line = getCaretLine();
				int line = yToLine(e.getY());

				System.out.println("LINE CLICKED 3: " + line);
				if(line != getLineCount()-1) {
					if(codeMap.isLineActive(line)) {
						undoToLine(line, true);
						setCaretPosition(getDocumentLength());
					} else {
						String code = codeMap.getCodeAtLine(line);
						if(code.equals(""))  Toolkit.getDefaultToolkit().beep();
						pasteCode(code);
						if(pairTextArea != null)
							pairTextArea.pasteCode(code);
					}
				}
			}
		});
	}
}
