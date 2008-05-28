package edu.stanford.rehearse.undo1;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

import edu.stanford.rehearse.CodeTree;
import edu.stanford.rehearse.InteractiveTextAreaPainter;
import edu.stanford.rehearse.POWUtils;
import edu.stanford.rehearse.RehearseClient;
import edu.stanford.rehearse.RehearseHighlight;

public class InteractiveTextArea extends JEditTextArea {


	protected String unfinishedStatements = "";
	protected int uid;
	protected int functionNum;
	protected CodeTree codeTree;
	
	protected ArrayList<String> codeQueue = new ArrayList<String>();
	
	protected RehearseHighlight highlight;
	
	public InteractiveTextArea(int uid, int functionNum, int initialSnapshot) {
		super();
		this.setDocument(new SyntaxDocument());
		this.uid = uid;
		this.functionNum = functionNum;
		this.codeTree = new CodeTree(initialSnapshot);
		setText("");
		highlight = new RehearseHighlight();
		this.getPainter().addCustomHighlight(highlight);
		setupMouseListener();
	}
	
	protected void setupMouseListener() {
		this.getPainter().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				System.out.println("CLICK ON LINE: " + getCaretLine());
				if(getCaretLine() != getLineCount()-1) {
					redo(getCaretLine());
					setCaretPosition(getDocumentLength());
				}
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
			inputHandler.keyPressed(evt);
			break;
		case KeyEvent.KEY_RELEASED:
			inputHandler.keyReleased(evt);
			if(evt.getKeyCode() == KeyEvent.VK_ENTER) parseLastLine();
			break;
		}
	}
	
	public void parseLastLine() {
		
		Context cx = ContextFactory.getGlobal().enterContext();

		// Collect lines of source to test compilability.
		String toEvaluate = "";
		System.out.println("Linecount: " + getLineCount());
		int currLine = getLineCount() - 2;
		toEvaluate = getLineText(currLine);
		System.out.println("Original string:" + toEvaluate);
		
		unfinishedStatements += " " + toEvaluate;
		
		System.out.println("Concatted string: " + unfinishedStatements);
		boolean compilable = cx.stringIsCompilableUnit(unfinishedStatements);
		if(compilable) {
			unfinishedStatements = unfinishedStatements.trim();
			if(!unfinishedStatements.equals("")) {
				executeStatement();
			}
		}
	}
	
	protected void executeStatement() {
		codeTree.addNewCode(getCaretLine()-1, unfinishedStatements);
		updateRedoLines();
		addCommandToQueue(unfinishedStatements, false);
		unfinishedStatements = "";
	}
	
	protected void updateRedoLines() {
		highlight.setRedoLines(codeTree.getRedoLineNums());
	}
	
	public void undo() {
		int lineNum = codeTree.getCurrentCommandLine();
		int snapshotId = codeTree.undo();
		updateRedoLines();
		if(snapshotId == -1) return;

		String command = "load(" + snapshotId + ");";
		addCommandToQueue(command, true);
		((InteractiveTextAreaPainter)getPainter()).mark(lineNum, true);
	}
	
	public void redo(int lineNum) {
		int snapshotId = codeTree.redo(lineNum);
		updateRedoLines();
		if(snapshotId == -1) return;

		String command = "load(" + snapshotId + ");";
		addCommandToQueue(command, true);
		((InteractiveTextAreaPainter)getPainter()).mark(lineNum, false);
	}
	
	protected void addCommandToQueue(String command, boolean isUndo) {
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
	
}