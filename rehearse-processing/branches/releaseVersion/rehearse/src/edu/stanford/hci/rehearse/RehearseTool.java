package edu.stanford.hci.rehearse;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import processing.app.Editor;
import processing.app.Preferences;

public class RehearseTool implements processing.app.tools.Tool
{
	private Editor editor = null;
	
	public String getMenuTitle() {
		return "Rehearse...";
	}

	public void init(Editor editor) {
		this.editor = editor;
	}

	public void run() {
		Object[] options = {"Yes",
                "No"};
		RehearsePreferences prefs = new RehearsePreferences(Frame.getFrames()[0]);
	    JOptionPane.showMessageDialog(null, prefs);
//		int n = JOptionPane.showOptionDialog(Frame.getFrames()[0],
//				"Would you like to enable Rehearse by default?",
//				"Rehearse preferences",
//				JOptionPane.YES_NO_CANCEL_OPTION,
//				JOptionPane.QUESTION_MESSAGE,
//				null,
//				options,
//				options[0]);
//		if (n == 0) {
//			Preferences.setBoolean("rehearse.default", true);
//		} else {
//			Preferences.setBoolean("rehearse.default", false);
//		}
	}
	
	/**
	 * Temporary preferences menu that opens on clicking the
	 * "Rehearse..." menu item in the Tools menu.
	 * @author vignan
	 *
	 */
	private class RehearsePreferences extends JPanel {
		JCheckBox cb;
		
		public RehearsePreferences(Frame f) {
			super();
			// Checkbox is initialized to be the value in prefs
			cb = new JCheckBox("Always run in Rehearse mode", 
					Preferences.getBoolean("rehearse.default"));
		    JPanel panel2 = new JPanel();
		    panel2.add(cb);
		    cb.addActionListener(new checkBoxPrefsAction());
			this.add(panel2);
			
		}
		
		private class checkBoxPrefsAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				Preferences.setBoolean("rehearse.default", cb.isSelected());
				editor.buildToolbar();
				//editor = new Editor (editor.getBase(), editor.getPath(), editor.getBaseLocation());
			}
		}
		
	}
}
