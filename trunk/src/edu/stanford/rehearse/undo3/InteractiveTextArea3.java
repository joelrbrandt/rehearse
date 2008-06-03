package edu.stanford.rehearse.undo3;

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
import edu.stanford.rehearse.undo1.InteractiveTextArea;
import edu.stanford.rehearse.undo1.Rehearse;
import edu.stanford.rehearse.undo4.InteractiveTextArea4;

public class InteractiveTextArea3 extends InteractiveTextArea {
	

	public InteractiveTextArea3(int uid, int functionNum, int initialSnapshot, Rehearse rehearse) {
		super(uid, functionNum, initialSnapshot, rehearse);
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
				if(SwingUtilities.isRightMouseButton(e)) return;
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
		
		this.getPainter().addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				
					int lineNum = yToLine(e.getY());
					if(codeMap.isLineActive(lineNum)) {
						rehearse.updateInstructions("Click to undo to this line");
					} else if(!codeMap.getCodeAtLine(lineNum).equals("")){
						rehearse.updateInstructions("Click to paste this code to current cursor line");
					} else {
						rehearse.updateInstructions("");
					}
			}
		});
	}
}
