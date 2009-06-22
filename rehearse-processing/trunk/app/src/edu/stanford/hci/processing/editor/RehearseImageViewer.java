package edu.stanford.hci.processing.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

public class RehearseImageViewer extends JFrame implements ChangeListener, ActionListener {
	private List<SnapshotModel> snapshots;
	private int currSnapshotIndex;
	
	private JLabel snapshotNumLabel;
	private JButton next;
	private JButton previous;
	private JSlider snapshotSlider;
	private JTable variableTable;
	private SnapshotImageViewer imageViewer;

	public RehearseImageViewer(List<SnapshotModel> snapshots) {
		super("Snapshots");
		setSize(750, 600);
		this.snapshots = snapshots;
		currSnapshotIndex = 0;
		getContentPane().setLayout(new BorderLayout());
		
		snapshotSlider = new JSlider(JSlider.HORIZONTAL, 0, snapshots.size() - 1, 0);
		snapshotSlider.setPreferredSize(new Dimension(500, 50));
		snapshotSlider.setMajorTickSpacing(1);
		snapshotSlider.setPaintTicks(true);
		snapshotSlider.setSnapToTicks(true);
		snapshotSlider.addChangeListener(this);
		
		previous = new JButton("Prev");
		previous.addActionListener(this);
		next = new JButton("Next");
		next.addActionListener(this);

		JPanel headerPanel = new JPanel();
		snapshotNumLabel = new JLabel("Snapshot 1");
		headerPanel.add(snapshotNumLabel);
		headerPanel.add(snapshotSlider);
		headerPanel.add(previous);
		headerPanel.add(next);
		add(headerPanel, BorderLayout.PAGE_START);
		
		variableTable = new JTable(new VariableTableModel(snapshots.get(0)));
		JScrollPane scrollPane = new JScrollPane(variableTable);
		scrollPane.setPreferredSize(new Dimension(300,500));
		// JRB: This is only available in Java 1.6, and I can't get Processing to build with 1.6
		// variableTable.setFillsViewportHeight(true);
		add(scrollPane, BorderLayout.LINE_END);

		imageViewer = new SnapshotImageViewer(snapshots.get(0).getImage());
		imageViewer.setPreferredSize(new Dimension(500,500));
		add(imageViewer, BorderLayout.CENTER);

		manageButtonStates();
	}
	
	private void manageButtonStates() {
		previous.setEnabled(!(currSnapshotIndex == 0));
		next.setEnabled(!(currSnapshotIndex == snapshots.size() - 1));
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == previous) {
			//setCurrentSnapshot(currSnapshotIndex - 1);
			snapshotSlider.setValue(currSnapshotIndex - 1);
			manageButtonStates();
		} else if (e.getSource() == next) {
			//setCurrentSnapshot(currSnapshotIndex + 1);
			snapshotSlider.setValue(currSnapshotIndex + 1);
			manageButtonStates();
		}
	}

	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
		int snapshotIndex = (int)source.getValue();
		setCurrentSnapshot(snapshotIndex);
		manageButtonStates();
	}

	private void setCurrentSnapshot(int snapshotIndex) {
		currSnapshotIndex = snapshotIndex;
		snapshotNumLabel.setText("Snapshot " + (currSnapshotIndex+1));
		SnapshotModel snapshot = snapshots.get(snapshotIndex);
		imageViewer.setImage(snapshot.getImage());
		variableTable.setModel(new VariableTableModel(snapshot));
	}

	public static class SnapshotImageViewer extends JPanel {

		private Image image;

		public SnapshotImageViewer(Image image) {
			this.image = image;
		}

		public void setImage(Image image) {
			this.image = image;
			repaint();
		}

		public void paint(Graphics g) {
			BufferedImage i = (BufferedImage)image;
			
			int x = this.getWidth() / 2 - i.getHeight(null) / 2;
			int y = this.getHeight() / 2 - i.getHeight(null) / 2;
			
			g.drawImage(i, x, y, i.getWidth(null), 
					i.getHeight(null), null);
		}
	}

	public static class VariableTableModel extends AbstractTableModel {

		private static String[] columnNames = {"Name", "Values"};
		private SnapshotModel snapshot;
		private List<String> variableNames;
		private List<String> variableValues;

		public VariableTableModel(SnapshotModel snapshot) {
			this.snapshot = snapshot;
			variableNames = new ArrayList<String>();
			variableValues = new ArrayList<String>();

			for (String key : snapshot.getVariableMap().keySet()) {
				variableNames.add(key);
				variableValues.add(snapshot.getVariableMap().get(key));
			}
		}

		public int getColumnCount() {
			return 2;
		}

		public String getColumnName(int column) {
			return columnNames[column];
		}

		public int getRowCount() {
			return snapshot.getVariableMap().size();
		}

		public Object getValueAt(int row, int col) {
			if (col == 0) {
				return variableNames.get(row);
			} else {
				return variableValues.get(row);
			}
		}	
	}
}
