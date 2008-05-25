package edu.stanford.rehearse;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class Rehearse1 extends Rehearse  {

	public Rehearse1(int uid, int functionNum, String functionName,
			String parameters, int initialSnapshot) {
		super(uid, functionNum, functionName, parameters, initialSnapshot);
		
		UndidLinesList list = new UndidLinesList();
		JScrollPane scrollPane = new JScrollPane(list);
		
		bottomPanel.add(scrollPane, BorderLayout.CENTER);
		validate();
	}
	
	public class UndidLinesList extends JList {
		
		private UndidLinesListModel model;
		
		public UndidLinesList() {
			super();
			model = new UndidLinesListModel();
			this.setModel(model);
		}
	}
	
	public class UndidLinesListModel extends AbstractListModel {
		
		private ArrayList<CodeElement> undidLines;
		
		public UndidLinesListModel() {
			undidLines = new ArrayList<CodeElement>();
		}

		public Object getElementAt(int index) {
			return undidLines.get(index).getCode();
		}

		public int getSize() {
			return undidLines.size(); 
		}
	
		public void addCodeElement(CodeElement c) {
			undidLines.add(c);
			fireIntervalAdded(this, undidLines.size()-1, undidLines.size()-1);
		}
		
		public void removeCodeElement(CodeElement c) {
			int index = undidLines.indexOf(c);
			if(index != -1)
				undidLines.remove(index);
			fireIntervalRemoved(this, index, index);
		}
	}
}
