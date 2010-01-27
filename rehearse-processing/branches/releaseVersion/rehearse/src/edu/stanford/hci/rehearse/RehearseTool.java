package edu.stanford.hci.rehearse;

import processing.app.Editor;

public class RehearseTool implements processing.app.tools.Tool {

	private Editor editor = null;
	
	public String getMenuTitle() {
		return "Rehearse!!!!";
	}

	public void init(Editor editor) {
		this.editor = editor;
		System.out.println("initialized Rehearse...");
		
	}

	public void run() {
		new Foobar();
		editor.setBackground(java.awt.Color.PINK);
		System.out.println("ran Rehearse...");
	}

}
