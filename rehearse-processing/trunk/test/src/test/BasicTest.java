package test;

import org.uispec4j.*;
import org.uispec4j.interception.*;

import processing.app.EditorConsole;
import processing.app.syntax.JEditTextArea;

import edu.stanford.hci.processing.RehearseBase;
import edu.stanford.hci.processing.editor.RehearseEditor;

public class BasicTest extends UISpecTestCase {
	
	private Window window;
	
	protected void setUp() throws Exception {
		setAdapter(new MainClassAdapter(RehearseBase.class, new String[0]));
		window = getMainWindow();
	}
	
	public void testRun() {
		assertTrue(window != null);
		RehearseEditor editor = (RehearseEditor)window.getAwtContainer();
		EditorConsole.setEditor(editor);
		
		JEditTextArea textarea = editor.getTextArea();
		textarea.setText("println(\"it works!\");");
		editor.handleRun(false);
	}
	
	/*
	private void openFile(String filename) {
		MenuBar menu = window.getMenuBar();
		MenuItem file = menu.getMenu("File");
		MenuItem open = file.getSubMenu("Open");
		WindowInterceptor.init(window.getMenuBar().getMenu("File").getSubMenu("Open").triggerClick())
		.process(FileChooserHandler.init()
				.assertIsOpenDialog()
				.select("/Users/dmacdougall/Desktop/test.pde"))
		.run();
	}
	*/
}
