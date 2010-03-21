package edu.stanford.hci.rehearse;

import java.awt.Frame;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import processing.app.Base;
import processing.app.Editor;
import processing.app.Preferences;
import processing.app.syntax.PdeTextAreaDefaults;

public class RehearseTool implements processing.app.tools.Tool
{
	private Editor editor = null;
	private JCheckBox cb;
	private String PROCESSING_VERSION = "0176";
	public String getMenuTitle() {
		return "Rehearse...";
	}

	public void init (Editor editor) {
		if (Base.VERSION_NAME != PROCESSING_VERSION) {
			System.err.println("Could not load Rehearse tool. Please download processing version #"+PROCESSING_VERSION);
			return;
		}
		this.editor = editor;
		if (Preferences.getBoolean("rehearse.default")) {
			editor.setCustomToolbar(new RehearseToolbar(editor, editor.getToolbarMenu()), this);
			editor.setCustomTextArea(new RehearseEditTextArea(new PdeTextAreaDefaults()), this);
		}
	}
	
	public void run() {
		if (Base.VERSION_NAME != PROCESSING_VERSION) {
			JOptionPane.showMessageDialog(editor, 
					"Could not load Rehearse tool. Please download processing version #"+PROCESSING_VERSION);
			return;
		}
		
		RehearsePreferences prefs = new RehearsePreferences(Frame.getFrames()[0]);
	    //JOptionPane.showMessageDialog(null, prefs);
		int selected = JOptionPane.showOptionDialog(editor, prefs, "Rehearse Preferences", 
				JOptionPane.OK_CANCEL_OPTION, 0, null, null, JOptionPane.YES_OPTION);
		if (selected == JOptionPane.YES_OPTION) {
			Preferences.setBoolean("rehearse.default", cb.isSelected());
			if (cb.isSelected()) {
				editor.setCustomToolbar(new RehearseToolbar(editor, editor.getToolbarMenu()), this);
				editor.setCustomTextArea(new RehearseEditTextArea(new PdeTextAreaDefaults()), this);
			} else {
				editor.setCustomToolbar(null, this);
				editor.setCustomTextArea(null, this);
			}
			editor.rebuildToolbarTextArea();
		}
	}
	
	/**
	 * Temporary preferences menu that opens on clicking the
	 * "Rehearse..." menu item in the Tools menu.
	 * @author vignan
	 *
	 */
	private class RehearsePreferences extends JPanel {
		
		public RehearsePreferences(Frame f) {
			super();
			// Checkbox is initialized to be the value in prefs
			cb = new JCheckBox("Always run in Rehearse mode", 
					Preferences.getBoolean("rehearse.default"));
		    JPanel panel2 = new JPanel();
		    panel2.add(cb);
			this.add(panel2);
		}
	}
}
