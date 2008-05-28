package edu.stanford.rehearse.undo1;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

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
		ta = new InteractiveTextArea(uid, functionNum, initialSnapshot);
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
		
		bottomPanel.add(buttons, BorderLayout.SOUTH);
		add(bottomPanel, BorderLayout.SOUTH);
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
		String prototype = "function " + functionName + " ( " + parameters + " ) ";
		p.add(new JLabel(prototype));
		this.add(p, BorderLayout.NORTH);
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
		ta.undo();
	}
	
	protected void saveCode() {
		String code = ta.getCode();
		try {
			String params = "rehearse_uid=" + uid + "&code=" + URLEncoder.encode(code, "UTF-8");
			ArrayList<String> savedCode = POWUtils.callPOWScript(POWUtils.INSERT_CODE_URL, params);
			System.out.println("Saved code: " + savedCode);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
	}
	
	public void appendResponse(int snapshotID, int errorCode, String response) {
		ta.appendResponse(snapshotID, errorCode, response);
	}
}

