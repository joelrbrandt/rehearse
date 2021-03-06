package edu.stanford.rehearse;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.jedit.syntax.JavaScriptTokenMarker;

import edu.stanford.rehearse.UndidLinesList.UndidLinesListModel;
import edu.stanford.rehearse.undo1.InteractiveTextArea;
import edu.stanford.rehearse.undo1.Rehearse;
import edu.stanford.rehearse.undo2.InteractiveTextArea2;
import edu.stanford.rehearse.undo2.Rehearse2;
import edu.stanford.rehearse.undo3.InteractiveTextArea3;
import edu.stanford.rehearse.undo4.InteractiveTextArea4;

public class RehearseFlatUndo extends Rehearse {
	
	private InteractiveTextArea4 ta2;
	
	private UndidLinesListModel undidLinesListModel;
	private UndidLinesList list;
	
	public RehearseFlatUndo(int uid, int functionNum, String functionName,
			String parameters, int initialSnapshot) {
		super(uid, functionNum, functionName, parameters, initialSnapshot);
		this.setTitle("Flat Undo");
	}
	
	protected void initTextArea() {
		
		JTabbedPane tabbedPane = new JTabbedPane();
		
		ta = new InteractiveTextArea3(uid, functionNum, initialSnapshot, this);
		ta.setTokenMarker(new JavaScriptTokenMarker());
		tabbedPane.addTab("View 1", ta);
		
		JPanel panel = new JPanel(new BorderLayout());
		undidLinesListModel = new UndidLinesListModel();
		list = new UndidLinesList(undidLinesListModel);
		ta2 = new InteractiveTextArea4(uid, functionNum, initialSnapshot, list, this);
		ta2.setTokenMarker(new JavaScriptTokenMarker());
		ta.setPairTextArea(ta2);
		ta2.setPairTextArea(ta);
		
		panel.add(ta2, BorderLayout.CENTER);
		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = list.locationToIndex(e.getPoint());
					CodeElement ce = (CodeElement)list.getUndidLinesListModel().getElementAt(index);
					ta.pasteCode(ce.getCode());
					ta2.pasteCode(ce.getCode());
				}
				
				if(SwingUtilities.isRightMouseButton(e)) {
					int index = list.locationToIndex(e.getPoint());
					CodeElement ce = (CodeElement)list.getUndidLinesListModel().getElementAt(index);
					ta.pasteCode(ce.getCode());
					ta2.pasteCode(ce.getCode());
				}
			}
			
			public void mouseExited(MouseEvent e) {
				updateInstructions("");
			}
		});
		list.addMouseMotionListener( new MouseMotionAdapter()
		{
			public void mouseMoved(MouseEvent e)
			{
				int row = list.locationToIndex( e.getPoint() );
				if(row >= 0 && row < list.getModel().getSize())
					updateInstructions("Double-click to copy this code to current cursor line");
				else
					updateInstructions("");
			}
		});
		list.setPreferredSize(new Dimension(200, 150));
		panel.add(list, BorderLayout.SOUTH);
		tabbedPane.addTab("View 2", panel);
		
		add(tabbedPane, BorderLayout.CENTER);
	}
	
	public void appendResponse(int snapshotID, int errorCode, String response) {
		ta.appendResponse(snapshotID, errorCode, response);
		ta2.appendResponse(snapshotID, errorCode, response);
		if(errorCode != 1) {
			RehearseClient.timer.cancel();
			RehearseClient.lock.unlock();
			System.out.println("lock released early in appendResponse");
			undo();	
			RehearseClient.lock.lock();
		}
	}
	
	protected void undo() {
		((InteractiveTextArea3)ta).undo(1,true);
		
	}
}
