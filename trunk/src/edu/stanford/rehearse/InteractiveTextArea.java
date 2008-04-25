package edu.stanford.rehearse;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.jedit.syntax.JEditTextArea;
import org.jedit.syntax.TextAreaDefaults;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

public class InteractiveTextArea extends JEditTextArea {

	private static final String REHEARSE_URL = "http://localhost:6670/rehearse/rehearse.sjs";
	private static final String UNDO_URL = "http://localhost:6670/rehearse/undo.sjs";
	
	private String unfinishedStatements = "";
	private int uid;
	private ArrayList<Integer> snapshot_ids = new ArrayList<Integer>();
	private ArrayList<String> commands = new ArrayList<String>();
	//private RehearseHighlight highlight = new RehearseHighlight();
	
	public InteractiveTextArea(int uid) {
		super();
		this.uid = uid;
		this.setText("");
		//this.getPainter().addCustomHighlight(highlight);
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
			System.out.println(unfinishedStatements + " -- Good to go!");
			commands.add(unfinishedStatements);
			executeStatement(unfinishedStatements);
			unfinishedStatements = "";
		}
	}
	
	public void undo() {
		try {
			if(snapshot_ids.isEmpty())
				return;
			
			URL myURL = new URL(UNDO_URL);
			URLConnection myUC = myURL.openConnection();
			myUC.setDoOutput(true);
			myUC.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			myUC.connect();
			
			PrintWriter out = new PrintWriter(myUC.getOutputStream());
			String cmdEnc = "rehearse_uid=" + uid + "&snapshot_id=" + popSnapshotID();
			out.print(cmdEnc);
			out.close();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(myUC.getInputStream()));
			String s;
			while ((s = in.readLine()) != null) {}
			in.close();
			
			((InteractiveTextAreaPainter)getPainter()).markUndo();
			commands.remove(commands.size()-1);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private int popSnapshotID() {
		assert(snapshot_ids.size() != 0);
		return snapshot_ids.remove(snapshot_ids.size()-1);
	}
	
	private void executeStatement(String statement) {
		try {
			List<String> result = doPost(statement);
			String text = "";
			for(String line : result)
				text = text + line + "\n";
			
			System.out.println("Result = " + text);
			
			int splitIndex = text.indexOf(' ');
			int sid = Integer.parseInt(text.substring(0, splitIndex));
			System.out.println("snapshot id = " + sid);
			snapshot_ids.add(sid);
			
			int startLine = getCaretLine(); 
			setText(getText() + text.substring(splitIndex+1));
			int endLine = getCaretLine() - 1;
			
			((InteractiveTextAreaPainter)getPainter()).markResponse(startLine, endLine);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private List<String> doPost(String command) throws Exception {
		URL myURL = new URL(REHEARSE_URL);
		URLConnection myUC = myURL.openConnection();
		myUC.setDoOutput(true);
		myUC.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		myUC.connect();

		PrintWriter out = new PrintWriter(myUC.getOutputStream());
		String cmdEnc = "command=" + URLEncoder.encode(command, "UTF-8") +
						"&rehearse_uid=" + uid;
		out.print(cmdEnc);
		out.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(myUC.getInputStream()));
		ArrayList<String> result = new ArrayList<String>();
		String s;
		while ((s = in.readLine()) != null) {
			result.add(s);
		}
		in.close();

		return result;
	}
	
	public String getCode() {
		String code = "";
		for(String s : commands) {
			code += s + "\n";
		}
		return code;
	}

}
