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
	
	private String unfinishedStatements = "";
	private int uid;
	
	public InteractiveTextArea(int uid) {
		super();
		this.uid = uid;
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
			executeStatement(unfinishedStatements);
			unfinishedStatements = "";
		}
	}
	
	private void executeStatement(String statement) {
		try {
			List<String> result = doPost(statement);
			String text = getText();
			for(String line : result)
				text = text + line + "\n";
			setText(text);
			
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

}
