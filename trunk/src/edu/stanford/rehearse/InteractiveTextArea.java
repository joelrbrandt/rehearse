package edu.stanford.rehearse;
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

public class InteractiveTextArea extends JEditTextArea {


	private String unfinishedStatements = "";
	private int uid;
	private int functionNum;
	private CodeList codeList;
	
	private ArrayList<String> codeQueue = new ArrayList<String>();
	
	private RehearseHighlight highlight;
	
	public InteractiveTextArea(int uid, int functionNum, int initialSnapshot) {
		super();
		this.setDocument(new SyntaxDocument());
		this.uid = uid;
		this.functionNum = functionNum;
		this.codeList = new CodeList(initialSnapshot);
		setText("");
		highlight = new RehearseHighlight();
		this.getPainter().addCustomHighlight(highlight);
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
				//commands.add(unfinishedStatements);
				//executeStatement(unfinishedStatements);
				codeList.addNewCode(getCaretLine()-1, unfinishedStatements);
				highlight.setRedoLines(codeList.getRedoLineNums());
				addCommandToQueue(unfinishedStatements, false);
				unfinishedStatements = "";
			}
		}
	}
	
	public void undo() {
		int lineNum = codeList.getCurrentCommandLine();
		int snapshotId = codeList.undo();
		highlight.setRedoLines(codeList.getRedoLineNums());
		if(snapshotId == -1) return;

		String command = "load(" + snapshotId + ");";
		addCommandToQueue(command, true);
		/*
		String params = "rehearse_uid=" + uid + "&snapshot_id=" + popSnapshotID();
		POWUtils.callPOWScript(POWUtils.UNDO_URL, params);
		 */

		((InteractiveTextAreaPainter)getPainter()).mark(lineNum, true);
		//commands.remove(commands.size()-1);
	}
	/*
	private int popSnapshotID() {
		assert(snapshot_ids.size() != 0);
		return snapshot_ids.remove(snapshot_ids.size()-1);
	}*/
	
	public void redo(int lineNum) {
		int snapshotId = codeList.redo(lineNum);
		highlight.setRedoLines(codeList.getRedoLineNums());
		if(snapshotId == -1) return;

		String command = "load(" + snapshotId + ");";
		addCommandToQueue(command, true);
		((InteractiveTextAreaPainter)getPainter()).mark(lineNum, false);
	}
	
	private void addCommandToQueue(String command, boolean isUndo) {
		System.out.println("Adding command to queue: " + command);
		try {
			String params = "command=" + URLEncoder.encode(command, "UTF-8") +
			"&rehearse_uid=" + uid + "&function_num=" + functionNum
			+ "&is_undo=" + isUndo;
			System.out.println("UPDATECODE: " + POWUtils.callPOWScript(POWUtils.UPDATE_CODE_URL, params));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void appendResponse(int snapshotID, int errorCode, String response) {
		//snapshot_ids.add(snapshotID);
		codeList.setCurrentSnapshotId(snapshotID);
		
		int startLine = getCaretLine(); 
		setText(getText() + response + "\n");
		int endLine = getCaretLine() - 1;
		((InteractiveTextAreaPainter)getPainter()).markResponse(startLine, endLine, errorCode == 1);
	}
	
	public ArrayList<String> getQueuedCode() {
		return codeQueue;
	}
	
	public String getCode() {
		return codeList.getCode();
	}
	
}