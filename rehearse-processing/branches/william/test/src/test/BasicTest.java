package test;

import org.uispec4j.*;
import org.uispec4j.interception.*;

import processing.app.EditorConsole;
import processing.app.syntax.JEditTextArea;

import edu.stanford.hci.helpmeout.HelpMeOutLog;
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
		final RehearseEditor editor = (RehearseEditor)window.getAwtContainer();
		EditorConsole.setEditor(editor);
		
		JEditTextArea textarea = editor.getTextArea();
		textarea.setText("\ninnnt i = 1;\n i++;\n\n");
		
		boolean present= false;
		editor.handleRun(present);
		
		
		//look through the log if any actions failed - if so, use the log as the assertion message.
		assertFalse(HelpMeOutLog.getInstance().getLogAsString(),HelpMeOutLog.getInstance().hasErrorOccurred());
	}
	
	private void runInteractive() {
		assertTrue(window != null);
		final RehearseEditor editor = (RehearseEditor)window.getAwtContainer();
		EditorConsole.setEditor(editor);
		
		JEditTextArea textarea = editor.getTextArea();
		textarea.setText("String a = null;a.concat(null);");
		
		Window w = WindowInterceptor.run(new Trigger() {
			public void run() {
				editor.handleInteractiveRun();
			}
		});
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
