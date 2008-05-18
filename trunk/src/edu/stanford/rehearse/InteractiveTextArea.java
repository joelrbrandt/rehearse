package edu.stanford.rehearse;
import java.awt.event.KeyEvent;
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
	private ArrayList<Integer> snapshot_ids = new ArrayList<Integer>();
	private ArrayList<String> commands = new ArrayList<String>();
	
	private ArrayList<String> codeQueue = new ArrayList<String>();
	
	public InteractiveTextArea(int uid, int functionNum) {
		super();
		this.setDocument(new SyntaxDocument());
		this.uid = uid;
		this.functionNum = functionNum;
		setText("");
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
				commands.add(unfinishedStatements);
				//executeStatement(unfinishedStatements);
				addCommandToQueue(unfinishedStatements);
				unfinishedStatements = "";
			}
		}
	}
	
	public void undo() {
		if(snapshot_ids.isEmpty())
			return;

		String params = "rehearse_uid=" + uid + "&snapshot_id=" + popSnapshotID();
		POWUtils.callPOWScript(POWUtils.UNDO_URL, params);


		((InteractiveTextAreaPainter)getPainter()).markUndo();
		commands.remove(commands.size()-1);

	}
	
	private int popSnapshotID() {
		assert(snapshot_ids.size() != 0);
		return snapshot_ids.remove(snapshot_ids.size()-1);
	}
	
	private void addCommandToQueue(String command) {
		try {
			String params = "command=" + URLEncoder.encode(command, "UTF-8") +
			"&rehearse_uid=" + uid + "&function_num=" + functionNum;
			ArrayList<String> r = POWUtils.callPOWScript(POWUtils.UPDATE_CODE_URL, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void appendResponse(int snapshotID, int errorCode, String response) {
		snapshot_ids.add(snapshotID);
		
		int startLine = getCaretLine(); 
		setText(getText() + response + "\n");
		int endLine = getCaretLine() - 1;
		((InteractiveTextAreaPainter)getPainter()).markResponse(startLine, endLine, errorCode == 1);
	}
	
	private void executeStatement(String statement) {
		try {
			System.out.println("Execute statement: " + statement);
			List<String> result = doPost(statement);
			
			int sid = Integer.parseInt(result.get(0));
			System.out.println("snapshot id = " + sid);
			snapshot_ids.add(sid);
			
			int errorCode = Integer.parseInt(result.get(1));
			
			String response = "";
			for(int i = 2; i < result.size(); i++) {
				response += result.get(i);
				if(i != result.size()-1)
					response += "\n";
			}
			
			System.out.println("result = " + response);
			
			int startLine = getCaretLine(); 
			setText(getText() + response);
			int endLine = getCaretLine() - 1;
			
			((InteractiveTextAreaPainter)getPainter()).markResponse(startLine, endLine, errorCode == 1);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public ArrayList<String> getQueuedCode() {
		return codeQueue;
	}
	
	private List<String> doPost(String command) throws Exception {
		String params = "command=" + URLEncoder.encode(command, "UTF-8") +
							"&rehearse_uid=" + uid;
		return POWUtils.callPOWScript(POWUtils.REHEARSE_URL, params);
	}
	
	public String getCode() {
		String code = "";
		for(String s : commands) {
			code += s + "\n";
		}
		return code;
	}

}