package edu.stanford.hci.processing.editor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import processing.app.Base;
import processing.app.EditorListener;
import processing.app.Preferences;
import processing.app.RehearseEditorListener;
import processing.app.Theme;
import processing.app.syntax.JEditTextArea;
import processing.app.syntax.PdeKeywords;
import processing.app.syntax.PdeTextAreaDefaults;
import processing.app.syntax.RehearseTextAreaDefaults;
import processing.app.syntax.TextAreaDefaults;
import processing.core.PApplet;

import edu.stanford.hci.processing.ExecutionTask;
import edu.stanford.hci.processing.ProcessingCanvas;
import edu.stanford.hci.processing.ProcessingMethods;
import edu.stanford.hci.processing.RehearsePApplet;

import bsh.ConsoleInterface;
import bsh.EvalError;
import bsh.Interpreter;

@Deprecated
public class ProcessingEditor extends JFrame implements ActionListener, ConsoleInterface {
	JButton runButton;
	JButton resumeButton;
	JEditTextArea textArea;
	JTextArea output;
	JTextArea breakpoints;

	JFrame canvasFrame;
	RehearsePApplet applet;
	
	PrintStream outputStream;
	
	
	Interpreter interpreter;
	ArrayList<Image> snapshotList = new ArrayList<Image>();
	
	
	public ProcessingEditor() {
		super();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		textArea = new JEditTextArea(new RehearseTextAreaDefaults());
		textArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		RehearseEditorListener listener = new RehearseEditorListener(this, textArea);
		listener.applyPreferences();
		textArea.getDocument().setTokenMarker(new PdeKeywords());

		setSize(400, 650);
		getContentPane().add(textArea, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		runButton = new JButton("Run");
		buttonPanel.add(runButton);
		resumeButton = new JButton("Resume");
		buttonPanel.add(resumeButton);
		runButton.addActionListener(this);
		resumeButton.addActionListener(this);
		getContentPane().add(buttonPanel, BorderLayout.NORTH);
		
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.PAGE_AXIS));
		
		output = new JTextArea();
		breakpoints = new JTextArea();
		textPanel.add(new JLabel("Console output:"));
		textPanel.add(output);
		textPanel.add(new JLabel("Breakpoints (space seperated):"));
		textPanel.add(breakpoints);
		output.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		output.setEditable(false);
		breakpoints.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		getContentPane().add(textPanel, BorderLayout.SOUTH);
//		getContentPane().add(output, BorderLayout.SOUTH);
		outputStream = new PrintStream(new TextAreaOutputStream());
		
		resumeButton.setEnabled(false);
		
		textArea.setText("void setup() {  \n"
						 + "background(100, 50, 200); \n" 
						 + "stroke(153); rect(30, 20, 55, 55); }\n"
						 + "void draw() {}");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ProcessingEditor editor = new ProcessingEditor();
		editor.setVisible(true);
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(runButton)) {
			doRun();
		} else if (arg0.getSource().equals(resumeButton)) {
			doResume();
		}
	}

	// runs the processing script
	private void doRun() {
		// clear previous context
		if (canvasFrame != null)
			canvasFrame.dispose();
		
		canvasFrame = new JFrame();
		canvasFrame.setLayout(new BorderLayout());
		canvasFrame.setSize(500, 500);
		
		//applet = new RehearsePApplet();
		canvasFrame.add(applet, BorderLayout.CENTER);
		canvasFrame.setVisible(true);
		//ProcessingMethods methods = new ProcessingMethods(canvas);
		
		// THIS CLASS IS DEPRECATED DONT USE.
		//interpreter = new Interpreter(this, applet);
		
		String[] breakpointArray = breakpoints.getText().split(" ");
		for (String str : breakpointArray) {
			try {
				Integer i = Integer.parseInt(str);
				interpreter.setBreakpoint(i);
			} catch( NumberFormatException e) {
				System.out.println("Breakpoint line was not formatted correctly.");
			}
		}
		resumeButton.setEnabled(false);
		String source = textArea.getText();
//		ExecutionTask task = new ExecutionTask(interpreter, source, output);
//		Thread thread = new Thread(task);
//		thread.start();
		
		output.setText("");
		try {
			// TODO: right now assumes that source has setup() and draw()
			// so this line just registers setup(), draw() and other
			// user-defined functions.
			Object obj = interpreter.eval(source);
			applet.init();
			
			if (obj != null)
				output.append(obj.toString());
		} catch (EvalError e) {
			output.append(e.toString());
			e.printStackTrace();
		}
		output.append("\n + Line numbers executed:");
		// we're not using this class anymore, won't bother fixing this.
//		for (Integer i : interpreter.getLineNumberSet()) {
//			output.append(" " + i);
//		}
	}

	private void doResume() {
		synchronized (interpreter.getBreakpointLock()) {
			interpreter.setSuspended(false);
			interpreter.getBreakpointLock().notify();
		}
	}
	
	public void error(Object o) {
		getOut().append(o.toString() + "\n");
	}

	public PrintStream getErr() {
		return outputStream;
	}

	public Reader getIn() {
		return new StringReader("");
	}

	public PrintStream getOut() {
		return outputStream;
	}

	public void print(Object o) {
		getOut().append(o.toString());
	}

	public void println(Object o) {
		getOut().append(o.toString() + "\n");	
	}

	public class TextAreaOutputStream extends OutputStream {
		public void write( int b ) throws IOException {
			System.out.println("TAOS gets call");
			output.append( String.valueOf( ( char )b ) );
		}
	}
	
	public void setResumable(boolean resumable) {
		resumeButton.setEnabled(resumable);
	}
}
