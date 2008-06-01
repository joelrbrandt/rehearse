package edu.stanford.rehearse.undo1;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.jedit.syntax.JEditTextArea;
import org.jedit.syntax.SyntaxDocument;
import org.jedit.syntax.TextAreaDefaults;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

import edu.stanford.rehearse.CodeElement;
import edu.stanford.rehearse.CodeTree;
import edu.stanford.rehearse.InteractiveTextAreaPainter;
import edu.stanford.rehearse.POWUtils;
import edu.stanford.rehearse.RehearseClient;
import edu.stanford.rehearse.RehearseHighlight;
import edu.stanford.rehearse.undo2.InteractiveTextArea2;

public class InteractiveTextArea extends JEditTextArea {


	protected String unfinishedStatements = "";
	protected int uid;
	protected int functionNum;
	protected CodeTree codeTree;
	
	protected ArrayList<String> codeQueue = new ArrayList<String>();
	
	protected RehearseHighlight highlight;
	
	protected InteractiveTextArea pairTextArea = null;
	
	public InteractiveTextArea(int uid, int functionNum, int initialSnapshot) {
		super();
		this.setDocument(new SyntaxDocument());
		this.uid = uid;
		this.functionNum = functionNum;
		this.codeTree = new CodeTree(initialSnapshot);
		setText("");
		highlight = new RehearseHighlight();
		this.getPainter().addCustomHighlight(highlight);
		
		removeAllMouseListeners();
		setupMouseListener();
		
		this.getPainter().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				giveFocus();
				setCaretPosition(getDocumentLength());
			}
		});
		
		setEnabled(true);
		giveFocus();
	}
	
	public void giveFocus() {
		requestFocus();
		setCaretVisible(true);
		focusedComponent = this;
	}
	
	public void setPairTextArea(InteractiveTextArea ta) {
		pairTextArea = ta;
	}
	
	protected void removeAllMouseListeners() {
		MouseListener[] listeners = this.getPainter().getMouseListeners();
		for(MouseListener listener: listeners)
			getPainter().removeMouseListener(listener);
	}
	
	protected void setupMouseListener() {
		this.getPainter().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int lineNum = yToLine(e.getY());

				System.out.println("LINE CLICKED: " + lineNum);
				if(lineNum != getLineCount() -1) {
				//if(getCaretLine() != getLineCount()-1) {
					redo(codeTree.getChildByLineNum(lineNum), true);
				} else {
					 Toolkit.getDefaultToolkit().beep();
				}
				setCaretPosition(getDocumentLength());
			}
		});
	}

	public void processKeyEvent(KeyEvent evt)
	{
		if(inputHandler == null)
			return;
		
		switch(evt.getID())
		{
		case KeyEvent.KEY_TYPED:
			inputHandler.keyTyped(evt);
			break;
		case KeyEvent.KEY_PRESSED:
			if(isMovementAllowed(evt))
				inputHandler.keyPressed(evt);
			break;
		case KeyEvent.KEY_RELEASED:
			if(evt.getKeyCode() == KeyEvent.VK_Z &&
					(evt.isControlDown() || evt.isMetaDown())) {
				undo(true);
			} else if(isMovementAllowed(evt)) {
				
				inputHandler.keyReleased(evt);
				if(evt.getKeyCode() == KeyEvent.VK_ENTER) parseLastLine();
			}
			break;
		}
	}
	
	private boolean isMovementAllowed(KeyEvent evt) {
		if(evt.getKeyCode() == KeyEvent.VK_DOWN || evt.getKeyCode() == KeyEvent.VK_UP)
			return false;
		
		int lastResponseLine = ((InteractiveTextAreaPainter)getPainter()).getLastResponseLine();
		if(evt.getKeyCode() == KeyEvent.VK_LEFT &&
			getCaretPosition() == getLineStartOffset(lastResponseLine+1))
			return false;
		if(evt.getKeyCode() == KeyEvent.VK_RIGHT &&
				getCaretPosition() == getLineEndOffset(getLineCount()-1))
			return false;
		return true;
	}
	
	public void parseLastLine() {
		
		Context cx = ContextFactory.getGlobal().enterContext();

		// Collect lines of source to test compilability.
		String toEvaluate = "";
		System.out.println("Linecount: " + getLineCount());
		int currLine = getLineCount() - 2;
		toEvaluate = getLineText(currLine);
		System.out.println("Original string:" + toEvaluate);
		
		if(!unfinishedStatements.equals("")) unfinishedStatements += "\n";
		unfinishedStatements += toEvaluate;
		
		System.out.println("Concatted string: " + unfinishedStatements);
		boolean compilable = cx.stringIsCompilableUnit(unfinishedStatements);
		if(compilable) {
			if(!unfinishedStatements.trim().equals("")) {
				executeStatement(unfinishedStatements, true);
				unfinishedStatements = "";
			}
		}
	}
	
	protected int countLines(String statement) {
		int count = 1;
		for(int i = 0; i < statement.length(); i++) {
			if(statement.charAt(i) == '\n')
				count++;
		}
		return count;
	}
	
	protected void executeStatement(String statement, boolean actual) {
		
		if(!actual){
			//TODO: fix this
			int lastResponseLine = ((InteractiveTextAreaPainter)getPainter()).getLastResponseLine();
			if(lastResponseLine < 0) lastResponseLine = -1;
			String currText = getText(0, getLineStartOffset(lastResponseLine+1));
			if(currText != null)
				setText(currText + statement + "\n");
			else
				setText(statement + "\n");
			codeTree.addNewCode(lastResponseLine+1, statement);
		} else {
			codeTree.addNewCode(getCaretLine()-countLines(statement), statement);
		}
		updateRedoLines();
		if(actual) {
			addCommandToQueue(statement, false);
			if(pairTextArea != null)
				pairTextArea.executeStatement(statement, false);
		}
	}
	
	
	protected void updateRedoLines() {
		highlight.setRedoLines(codeTree.getRedoLineNums());
	}
	
	public void undo(boolean actual) {
		int lineNum = codeTree.getCurrentCommandLine();
		int snapshotId = codeTree.undo();
		updateRedoLines();
		if(snapshotId == -1) return;
		((InteractiveTextAreaPainter)getPainter()).mark(lineNum, true);
		if(actual) {
			String command = "load(" + snapshotId + ");";
			addCommandToQueue(command, true);
			if(pairTextArea != null)
				pairTextArea.undo(false);
		}
	}
	
	public void redo(CodeElement codeElem, boolean actual) {
		if(codeElem == null)
			return;
		
		int snapshotId = codeTree.redo(codeElem);
		int lineNum = codeTree.getCurr().getLineNum();
		updateRedoLines();
		if(snapshotId == -1) return;
		((InteractiveTextAreaPainter)getPainter()).mark(lineNum, false);
		if(actual) {
			String command = "load(" + snapshotId + ");";
			addCommandToQueue(command, true);
			if(pairTextArea != null)
				((InteractiveTextArea2)pairTextArea).redo(codeTree.getCurr(), false);
		}
	}
	
	/*
	public void redo(int lineNum, boolean actual) {
		int snapshotId = codeTree.redo(lineNum);
		updateRedoLines();
		if(snapshotId == -1) return;
		((InteractiveTextAreaPainter)getPainter()).mark(lineNum, false);
		if(actual) {
			String command = "load(" + snapshotId + ");";
			addCommandToQueue(command, true);
			if(pairTextArea != null)
				((InteractiveTextArea2)pairTextArea).redo(codeTree.getCurr(), false);
		}
	}*/
	
	protected void addCommandToQueue(String command, boolean isUndo) {
		command = command.replace('\n', ' ');
		System.out.println("Adding command to queue: " + command);
		try {
			String params = "command=" + URLEncoder.encode(command, "UTF-8") +
			"&rehearse_uid=" + uid + "&function_num=" + functionNum
			+ "&is_undo=" + isUndo;
			List<String> result;
			do {
				result = POWUtils.callPOWScript(POWUtils.UPDATE_CODE_URL, params);
				System.out.println("update trying again");
			} while(result.get(0).startsWith("Error"));
			
			System.out.println("UPDATECODE: " + result);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		RehearseClient.reschedule();
	}
	
	public void appendResponse(int snapshotID, int errorCode, String response) {
		codeTree.setCurrentSnapshotId(snapshotID);
		codeTree.getCurr().setResponse(response);
		
		int startLine = getCaretLine(); 
		setText(getText() + response + "\n");
		int endLine = getCaretLine() - 1;
		((InteractiveTextAreaPainter)getPainter()).markResponse(startLine, endLine, errorCode == 1);
	}
	
	public ArrayList<String> getQueuedCode() {
		return codeQueue;
	}
	
	public String getCode() {
		return codeTree.getCode();
	}
	

	public void pasteCode(String code) {
		if(code != null) {
			setText(getText() + code);
		}
		setCaretPosition(getDocumentLength());
	}
	
}