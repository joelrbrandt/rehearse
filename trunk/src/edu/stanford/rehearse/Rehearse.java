package edu.stanford.rehearse;

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

public class Rehearse extends JFrame implements ActionListener{
	
	private int uid;
	private int functionNum;
	
	private boolean done;
	
	private static final String RESUME_EXECUTION_URL = 
		"http://localhost:6670/rehearse/resume_execution.sjs";
	
	private static final String INSERT_CODE_URL = 
		"http://localhost:6670/rehearse/insert_code.sjs";
	
	private InteractiveTextArea ta;


	public static void main(String[] args) {
		Rehearse SH = new Rehearse(1, 0, "testFunction", "");
	}
	
	public Rehearse(int uid, int functionNum, String functionName, String parameters) {
		super("Edit that syntax...");
		this.uid = uid;
		this.functionNum = functionNum;
		this.done = false;
		
		BorderLayout bl = new BorderLayout();
		setLayout(bl);
		
		initializeHeader(functionName, parameters);
		
		ta = new InteractiveTextArea(uid, functionNum);
		ta.setTokenMarker(new JavaScriptTokenMarker());
		add(ta, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		
		JButton undoButton = new JButton("Undo");
		undoButton.addActionListener(this);
		bottomPanel.add(undoButton);
		
		JButton doneButton = new JButton("Done");
		doneButton.addActionListener(this);
		bottomPanel.add(doneButton);
		
		add(bottomPanel, BorderLayout.SOUTH);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		pack();
		setVisible(true);
		
		ta.requestFocusInWindow();
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

	private void initializeHeader(String functionName, String parameters) {
		Panel p = new Panel();
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
			ta.undo();
		}
	}
	
	private void saveCode() {
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

