package edu.stanford.rehearse.undo3;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.rehearse.CodeElement;
import edu.stanford.rehearse.CodeMap;
import edu.stanford.rehearse.InteractiveTextAreaPainter;
import edu.stanford.rehearse.undo1.InteractiveTextArea;

public class InteractiveTextArea3 extends InteractiveTextArea {
	
	protected CodeMap codeMap;

	public InteractiveTextArea3(int uid, int functionNum, int initialSnapshot) {
		super(uid, functionNum, initialSnapshot);
		codeMap = new CodeMap();
	}
	
	protected void executeStatement() {
		super.executeStatement();
		codeMap.add(codeTree.getCurr());
	}
	
	protected void updateRedoLines() {
		//override and do nothing
	}
	
	public void undo(int lineNum) {
		int snapshotId = codeTree.undo(lineNum, (InteractiveTextAreaPainter)getPainter());
		if(snapshotId == -1) return;
		String code = codeTree.getLastUndidCode();
		if(code != null)
			setText(getText() + code);
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
					} else {
						String code = codeMap.getCodeAtLine(line);
						if(code != null) {
							setText(getText() + code);
						}
					}
					setCaretPosition(getDocumentLength());
				}
			}
		});
	}

}
