package edu.stanford.rehearse;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.LineBorder;

public class UndidLinesList extends JList {

	private UndidLinesListModel model;
	private Set<Integer> redoLines;

	public UndidLinesList(UndidLinesListModel model) {
		super(model);
		this.model = model;
		redoLines = new HashSet<Integer>();
		this.setCellRenderer(new MyCellRenderer());
	}

	public void setRedoLines(Set<Integer> redoLines) {
		this.redoLines = redoLines;
		repaint();
	}

	public UndidLinesListModel getUndidLinesListModel() {
		return model;
	}
	
	public boolean isRedoLine(int index) {
		CodeElement codeElem = (CodeElement)model.getElementAt(index);
		return redoLines.contains(codeElem.getLineNum());
	}

	public class MyCellRenderer extends JLabel implements ListCellRenderer {

		public Component getListCellRendererComponent(
				JList list,
				Object value,            // value to display
				int index,               // cell index
				boolean isSelected,      // is the cell selected
				boolean cellHasFocus)    // the list and the cell have the focus
		{
			// String s = value.toString();
			CodeElement codeElem = (CodeElement)value;
			Color bgColor = list.getBackground();
			if(redoLines.contains(codeElem.getLineNum())) {
				bgColor = new Color(255, 250, 205);
			}
			setToolTipText("Response was " + codeElem.getResponse() + 
					"\tLine number was: " + codeElem.getLineNum());
			setText(codeElem.getCode());
			setBackground(bgColor);
			setForeground(list.getForeground());
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			setBorder(new LineBorder(Color.gray));
			return this;
		}
	}



	public static class UndidLinesListModel extends AbstractListModel {

		private ArrayList<CodeElement> undidLines;

		public UndidLinesListModel() {
			undidLines = new ArrayList<CodeElement>();
		}

		public Object getElementAt(int index) {
			return undidLines.get(index);
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
