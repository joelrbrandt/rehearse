package edu.stanford.rehearse.undo3;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jedit.syntax.JavaScriptTokenMarker;

import edu.stanford.rehearse.undo1.InteractiveTextArea;
import edu.stanford.rehearse.undo1.Rehearse;


public class Rehearse3 extends Rehearse  {
	
	public Rehearse3(int uid, int functionNum, String functionName,
			String parameters, int initialSnapshot) {
		super(uid, functionNum, functionName, parameters, initialSnapshot);
		setTitle("Rehearse Option3");
	}
	
	protected void initTextArea() {
		ta = new InteractiveTextArea3(uid, functionNum, initialSnapshot, this);
		ta.setTokenMarker(new JavaScriptTokenMarker());
		add(ta, BorderLayout.CENTER);
	}
}
