package edu.stanford.rehearse;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.*;

import org.jedit.syntax.JavaScriptTokenMarker;

public class Rehearse extends JFrame implements ActionListener{
	
	private int uid;
	
	private static final String RESUME_EXECUTION_URL = 
		"http://localhost:6670/rehearse/resume_execution.sjs";
	
	private static final String INSERT_CODE_URL = 
		"http://localhost:6670/rehearse/insert_code.sjs";
	
	private InteractiveTextArea ta;


	public static void main(String[] args) {
		Rehearse SH = new Rehearse(1, "testFunction", "");
	}
	
	public Rehearse(int uid, String functionName, String parameters) {
		super("Edit that syntax...");
		this.uid = uid;
		
		BorderLayout bl = new BorderLayout();
		setLayout(bl);
		
		initializeHeader(functionName, parameters);
		
		ta = new InteractiveTextArea(uid);
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
	
	private void initializeHeader(String functionName, String parameters) {
		Panel p = new Panel();
		String prototype = functionName + "(" + parameters + ")";
		p.add(new JLabel(prototype));
		this.add(p, BorderLayout.NORTH);
	}

	public void actionPerformed(ActionEvent ae) {
		if(ae.getActionCommand().equals("Done")) {
			saveCode();
			resumeExecution();
		} else if(ae.getActionCommand().equals("Undo")) {
			ta.undo();
		}
	}
	
	private void saveCode() {
		String code = ta.getCode();
		try {
			URL myURL = new URL(INSERT_CODE_URL);
			URLConnection myUC = myURL.openConnection();
			myUC.setDoOutput(true);
			myUC.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			myUC.connect();
			
			PrintWriter out = new PrintWriter(myUC.getOutputStream());
			String cmdEnc = "rehearse_uid=" + uid + "&code=" + URLEncoder.encode(code, "UTF-8");
			out.print(cmdEnc);
			out.close();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(myUC.getInputStream()));
			String result = "";
			String s;
			while ((s = in.readLine()) != null) {result += s + "\n";}
			in.close();

			System.out.println("Saved Code: \n" + result);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void resumeExecution() {
		try {
			URL myURL = new URL(RESUME_EXECUTION_URL);
			URLConnection myUC = myURL.openConnection();
			myUC.setDoOutput(true);
			myUC.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			myUC.connect();
			
			PrintWriter out = new PrintWriter(myUC.getOutputStream());
			String cmdEnc = "rehearse_uid=" + uid;
			out.print(cmdEnc);
			out.close();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(myUC.getInputStream()));
			String s;
			while ((s = in.readLine()) != null) {}
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//dispose the window
		this.dispose();
	}
}

