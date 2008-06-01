package edu.stanford.rehearse.undo1;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;

import org.jedit.syntax.JavaScriptTokenMarker;

import edu.stanford.rehearse.POWUtils;

public class Rehearse extends JFrame implements ActionListener{
	
	protected int uid;
	protected int functionNum;
	protected String functionName;
	protected int initialSnapshot;
	
	protected boolean done;
	
	protected InteractiveTextArea ta;
	
	protected JPanel bottomPanel;
	
	protected JLabel instructions = new JLabel("Instructions Bar");

	private static final String SAVE_FILE = "C:\\xampp\\htdocs\\study\\test\\rehearse_saves.js";

	public static void main(String[] args) {
		Rehearse SH = new Rehearse(1, 0, "testFunction", "", 0);
	}
	
	public Rehearse(String title) {
		super(title);
	}
	
	public Rehearse(int uid, int functionNum, String functionName, String parameters,
			int initialSnapshot) {
		super("Rehearse Option1");
		this.uid = uid;
		this.functionNum = functionNum;
		this.done = false;
		this.functionName = functionName;
		this.initialSnapshot = initialSnapshot;
		
		BorderLayout bl = new BorderLayout();
		setLayout(bl);
		
		initializeHeader(functionName, parameters);
		initTextArea();
		initBottomPanel();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		pack();
		setVisible(true);
		
		ta.requestFocusInWindow();
	}
	
	protected void initTextArea() {
		ta = new InteractiveTextArea(uid, functionNum, initialSnapshot, this);
		ta.setTokenMarker(new JavaScriptTokenMarker());
		add(ta, BorderLayout.CENTER);
	}
	
	protected void initBottomPanel() {
		bottomPanel = new JPanel(new BorderLayout());
		JPanel buttons = new JPanel();
		JButton undoButton = new JButton("Undo");
		undoButton.addActionListener(this);
		buttons.add(undoButton);
		
		JButton doneButton = new JButton("Done");
		doneButton.addActionListener(this);
		buttons.add(doneButton);
		
		bottomPanel.add(new JLabel("   }"), BorderLayout.NORTH);
		bottomPanel.add(buttons, BorderLayout.CENTER);
		instructions.setText("Instructions Bar");
		instructions.setOpaque(true);
		instructions.setBackground(Color.darkGray);
		bottomPanel.add(instructions, BorderLayout.SOUTH);
		add(bottomPanel, BorderLayout.SOUTH);
	}
	
	public void updateInstructions(String text) {
		if(text.trim().length() == 0)
			text = "...";
		instructions.setText("<html><font color=white size=+2>" + text + "</font></html>");
	}
	
	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getFunctionNum() {
		return functionNum;
	}

	public void setFunctionNum(int functionNum) {
		this.functionNum = functionNum;
	}
	
	public String getFunctionName() {
		return functionName;
	}

	protected void initializeHeader(String functionName, String parameters) {
		Panel p = new Panel();
		if(parameters == null) parameters = "";
		parameters = parameters.trim();
		String styleParam = "   ";
		String[] param_parts = parameters.split(",");
		for(String s: param_parts) {
			int index = s.indexOf('=');
			if(index != -1) {
				styleParam += "<b>" + s.substring(0, index) + "</b>";
				styleParam += "=";
				styleParam += "<font color=blue>" + s.substring(index+1) + "</font>";
			} else {
				styleParam += s;
			}
		}
		
		String prototype = "<html><font color=green>function</font> "
			+ functionName + " ( " + styleParam + " ) {</html>";
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel(prototype));
		this.add(panel, BorderLayout.NORTH);
		
	}
	
	public ArrayList<String> getQueuedCode() {
		return ta.getQueuedCode();
	}
	
	public boolean isDone() {
		return done;
	}

	public void actionPerformed(ActionEvent ae) {
		if(ae.getActionCommand().equals("Done")) {
			saveCode();
			done = true;
			this.dispose();
		} else if(ae.getActionCommand().equals("Undo")) {
			undo();
		}
	}
	
	protected void undo() {
		ta.undo(true);
	}
	
	protected void saveCode() {
		String code = ta.getCode();
		try {
			String params = "rehearse_uid=" + uid + "&code=" + URLEncoder.encode(code, "UTF-8");
			List<String> result;
			do {
				result = POWUtils.callPOWScript(POWUtils.INSERT_CODE_URL, params);
				System.out.println("save code trying again");
			} while(result.size() == 0 || result.get(0).startsWith("Error"));
			System.out.println("Saved code: " + result);
			
			boolean started = false;
			String codeToSave = "";
			for (String s : result) {
				if (started) {
					if (s.contains("ENDSAVE")) {
						break;
					} else {
						codeToSave = codeToSave + s + "\n"; 
					}
				}
				else if (!started && s.contains("STARTSAVE")) {
					started = true;
				}
			}
			
			System.out.println("CODE TO PUT IN THE FILE:");
			System.out.println(codeToSave);
			
			try {
				FileWriter fw = new FileWriter(SAVE_FILE, true); // second param says to append to file
				fw.write(codeToSave);
				fw.close();
			} catch (Exception e) {
				System.out.println("Error writing to the save file!");
				e.printStackTrace();
			}
			
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
	}
	
	public void appendResponse(int snapshotID, int errorCode, String response) {
		ta.appendResponse(snapshotID, errorCode, response);
	}
}

