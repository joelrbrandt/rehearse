package edu.stanford.rehearse.undo4;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jedit.syntax.JavaScriptTokenMarker;

import edu.stanford.rehearse.CodeElement;
import edu.stanford.rehearse.UndidLinesList;
import edu.stanford.rehearse.UndidLinesList.UndidLinesListModel;
import edu.stanford.rehearse.undo1.InteractiveTextArea;
import edu.stanford.rehearse.undo1.Rehearse;

public class Rehearse4 extends Rehearse  {
	

	private UndidLinesListModel undidLinesListModel;
	private UndidLinesList list;
	
	public Rehearse4(int uid, int functionNum, String functionName,
			String parameters, int initialSnapshot) {
		super(uid, functionNum, functionName, parameters, initialSnapshot);
		this.setTitle("Rehearse Option4");
	}
		
	protected void initTextArea() {

		undidLinesListModel = new UndidLinesListModel();
		list = new UndidLinesList(undidLinesListModel);
		ta = new InteractiveTextArea4(uid, functionNum, initialSnapshot, list);
		ta.setTokenMarker(new JavaScriptTokenMarker());
		add(ta, BorderLayout.CENTER);
	}
	
	protected void initBottomPanel() {
		super.initBottomPanel();
		
		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = list.locationToIndex(e.getPoint());
					CodeElement ce = (CodeElement)list.getUndidLinesListModel().getElementAt(index);
					ta.setText(ta.getText() + ce.getCode());
					ta.setCaretPosition(ta.getDocumentLength());
					ta.giveFocus();
				}
			}
		});
		list.setPreferredSize(new Dimension(200, 150));
		bottomPanel.add(list, BorderLayout.CENTER);
	}
	
	protected void undo() {
		ta.undo(true);
	}
}
