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

	public static void main(String[] args) {
		Rehearse SH = new Rehearse(1);
	}
	
	public Rehearse(int uid) {
		super("Edit that syntax...");
		this.uid = uid;
		
		BorderLayout bl = new BorderLayout();
		setLayout(bl);
		
		InteractiveTextArea ta = new InteractiveTextArea(uid);
		ta.setTokenMarker(new JavaScriptTokenMarker());
		add(ta);
		
		JButton doneButton = new JButton("Done");
		doneButton.addActionListener(this);
		add(doneButton, BorderLayout.SOUTH);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		pack();
		setVisible(true);
		
	}

	public void actionPerformed(ActionEvent ae) {
		if(ae.getActionCommand().equals("Done")) {
			resumeExecution();
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

