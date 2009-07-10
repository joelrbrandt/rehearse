package edu.stanford.hci.processing.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JMenu;

import edu.stanford.hci.helpmeout.HelpMeOut;
import edu.stanford.hci.helpmeout.HelpMeOutExceptionTracker;
import edu.stanford.hci.processing.RehearsePApplet;
import edu.stanford.hci.processing.ModeException;
import processing.app.Base;
import processing.app.Editor;
import processing.app.EditorToolbar;
import processing.app.SketchCode;
import processing.app.syntax.JEditTextArea;
import processing.app.syntax.SyntaxDocument;
import processing.app.syntax.TextAreaPainter;
import processing.app.syntax.TextAreaPainter.Highlight;
import bsh.CallStack;
import bsh.ConsoleInterface;
import bsh.EvalError;
import bsh.Interpreter;

public class RehearseEditor extends Editor implements ConsoleInterface {
	
	private JFrame canvasFrame;
	private RehearsePApplet applet;
	private PrintStream outputStream;

	private Interpreter interpreter;

	private ArrayList<SnapshotModel> snapshots = new ArrayList<SnapshotModel>();

	private boolean wasLastRunInteractive = false;

	private RehearseLineModel lastExecutedLineModel = null;

	public int linesExecutedCount = 0; // TODO: refactor all this crap also this will overflow
	
	public RehearseEditor(Base ibase, String path, int[] location) {
		super(ibase, path, location);
		getTextArea().getPainter().addCustomHighlight(new RehearseHighlight());
	}

	@Override
	public EditorToolbar newEditorToolbar(Editor editor, JMenu menu) {
	    System.out.println("Making a Reherase Editor toolbar");	    
		return new RehearseEditorToolbar(editor, menu);
	}

	@Override
	public void handleRun(boolean present) {
		wasLastRunInteractive = false;
		super.handleRun(present);
	}

	@Override
	public void handleStop() {
		if (wasLastRunInteractive) {
			applet.stop();
			canvasFrame.dispose();
		} else {
			super.handleStop();
		}
	}
	
	private String appendCodeFromAllTabs() {
		StringBuffer bigCode = new StringBuffer();
		int bigCount = 0;
		for (SketchCode sc : getSketch().getCode()) {
			sc.setPreprocOffset(bigCount);
			if (sc == getSketch().getCurrentCode()) {
				bigCode.append(getText());
		        bigCode.append('\n');
		        bigCount += getLineCount();
			} else {
				bigCode.append(sc.getProgram());
		        bigCode.append('\n');
		        bigCount += sc.getLineCount();
			}
		}
		
		return bigCode.toString();
	}
	
	private SketchCode lineToSketchCode(int line) {
		for (SketchCode sc : getSketch().getCode()) {
			int lineCount;
			if (sc == getSketch().getCurrentCode()) {
				lineCount = getLineCount();
			} else {
				lineCount = sc.getLineCount();
			}
			
			if (line >= sc.getPreprocOffset() && line < sc.getPreprocOffset() + lineCount) {
				return sc;
			}
		}
		
		return null;
	}

	public void handleInteractiveRun() {
		wasLastRunInteractive = true;
		// clear previous context
		if (canvasFrame != null)
			canvasFrame.dispose();
		if (applet != null)
			applet.stop();

		canvasFrame = new JFrame();
		canvasFrame.setLayout(new BorderLayout());
		canvasFrame.setSize(100, 100);
		canvasFrame.setResizable(false);

		applet = new RehearsePApplet();
		//applet.setupFrameResizeListener();
		applet.frame = canvasFrame;
		applet.sketchPath = getSketch().getFolder().getAbsolutePath();
		canvasFrame.add(applet, BorderLayout.CENTER);
		canvasFrame.setVisible(true);
		canvasFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		canvasFrame.addWindowListener(new WindowAdapter() {		
			public void windowClosing(WindowEvent e) {
				applet.stop();
				if (snapshots.size() > 0) {
					RehearseImageViewer viewer = new RehearseImageViewer(snapshots);
					viewer.setVisible(true);
				}
			}
		});
		//canvasFrame.setDefaultCloseOperation();
		//ProcessingMethods methods = new ProcessingMethods(canvas);

		// NOTE: If this line fails with java.lang.NoSuchMethodError you probably have a the BeanShell bshXXX.jar 
		// in the classpath, e.g., /Library/Java/Extensions on OSX
		// to test, run "java bsh.Console" from terminal - if the beanshell console pops up, you'll have this problem
		// @see http://www.beanshell.org/manual/quickstart.html#Download_and_Run_BeanShell
		interpreter = new Interpreter(this, applet);

		/* No longer sure this is needed, I think just setting the package solves all problems
		// Add current classpath to the interpreter's classpath
		String classpath = System.getProperty("java.class.path");
		String[] paths = classpath.split(":");
		for (String p : paths) {
			URL processingClassPath = null;
			try {
				processingClassPath = new URL("file://" + p);
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (processingClassPath != null) {
				try {
					interpreter.getClassManager().addClassPath(processingClassPath);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		 */
		
		// Compiles the interactive program to check for compile errors.
		// If it fails, stop running.
		boolean compiled = HelpMeOutCompile();
		if (!compiled) {
		  return;
		}

		String source = appendCodeFromAllTabs();
		
		// now, add our script to the processing.core package
		try {
			interpreter.eval("package processing.core;");
		} catch (EvalError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		//String source = super.getText();
//		ExecutionTask task = new ExecutionTask(interpreter, source, output);
//		Thread thread = new Thread(task);
//		thread.start();

		console.clear();
		ensureDocumentExistsForEveryTab();
		clearExecutionInfoForLines();
		//lineHighlights.clear();
		snapshots.clear();
		getTextArea().repaint();
		try {
			Object obj;
			try {
				obj = interpreter.eval(source, true);
			} catch (ModeException e) {
				if (e.isJavaMode()) {
					throw new RuntimeException("We don't do java mode yet!");
				} else {
					// Code was written in static mode, let's try again.
					System.out.println("Code written in static mode, wrapping and restarting.");
					// this is kind of gross...
					obj = interpreter.eval("setup() {" + source + "}");
				}
			}
			// This actually starts the program.
			applet.init();

			if (obj != null)
				console.message(obj.toString(), false, false);
		} catch (EvalError e) {
		  HelpMeOutExceptionTracker.getInstance().processRuntimeException(e, interpreter);
			console.message(e.toString(), true, false);
			e.printStackTrace();
		}
	}

	private boolean HelpMeOutCompile() {
    try {
      String appletClassName = getSketch().compile();
      if (appletClassName != null) {
        HelpMeOut.getInstance().processNoError(textarea.getText());
        // if no exception has occurred yet, send text to HelpMeOut Exception class
        HelpMeOutExceptionTracker.getInstance().setSource(textarea.getText());
        if(HelpMeOutExceptionTracker.getInstance().hasExceptionOccurred()) {
          // TODO here or in handleInteractiveRun() above:
          int lineToWatch = HelpMeOutExceptionTracker.getInstance().getLineToWatch();
          interpreter.setLineToWatch(lineToWatch);
          //System.out.println("Watching line " + lineToWatch);
        }
        return true;
      }
    } catch (Exception e) {
      statusError(e);
    }
    return false;
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
		//getOut().append(o.toString());
		System.out.print(o.toString());
	}

	public void println(Object o) {
		//getOut().append(o.toString() + "\n");	
		System.out.println(o.toString());
	}
	
	private void ensureDocumentExistsForEveryTab() {
		SketchCode currentCode = getSketch().getCurrentCode();
		for (SketchCode sc : getSketch().getCode()) {
			SyntaxDocument doc = (SyntaxDocument)sc.getDocument();
			if (doc == null) {
				// This code makes the document and associates it with the
				// SketchCode object, performing appropriate initialization steps.
				// This isn't ideal but now we need each tab to have a valid
				// document reference even if that tab hasn't been clicked on yet.
				setCode(sc);
			}
		}
		// Set the code back to the one we started.
		setCode(currentCode);
	}

	private void clearExecutionInfoForLines() {
		for (SketchCode sc : getSketch().getCode()) {
			SyntaxDocument doc = (SyntaxDocument)sc.getDocument();
				for (int line = 0; line < doc.getTokenMarker().getLineCount(); line++) {
					RehearseLineModel m = 
						(RehearseLineModel)doc.getTokenMarker().getLineModelAt(line);
					if (m != null) {
						m.executedInLastRun = false;
						m.isMostRecentlyExecuted = false;
					}
				}
		}
	}

	public void notifyLineExecution(int lineNumber) {
		linesExecutedCount++;
		
		if (lastExecutedLineModel != null)
			lastExecutedLineModel.isMostRecentlyExecuted = false;

		// snapshotPoints is zero-indexed, interpreter is one-indexed.
		int line = lineNumber - 1;
		
		SketchCode sc = lineToSketchCode(line);
		SyntaxDocument doc = (SyntaxDocument)sc.getDocument();
		
		RehearseLineModel m = 
			(RehearseLineModel)doc.getTokenMarker().getLineModelAt(line - sc.getPreprocOffset());
		if (m == null) {
			m = new RehearseLineModel();
			doc.getTokenMarker().setLineModelAt(line - sc.getPreprocOffset(), m);
		}

		
		m.executedInLastRun = true;
		m.isMostRecentlyExecuted = true;
		m.countAtLastExec = linesExecutedCount;
		
		getTextArea().repaint();
		
		if (m.isPrintPoint) {
			snapshots.add(interpreter.makeSnapshotModel());
		}

		lastExecutedLineModel = m;
	}

	public class TextAreaOutputStream extends OutputStream {
		public void write( int b ) throws IOException {
			console.message( String.valueOf( ( char )b ), false, false);
		}
	}

	private class RehearseHighlight implements TextAreaPainter.Highlight {
		JEditTextArea textarea;
		Highlight next;

		public String getToolTipText(MouseEvent evt) {
			if (next != null) {
				return null;
			}
			return null;
		}

		public void init(JEditTextArea textArea, Highlight next) {
			textarea = textArea;
			this.next = next;
		}

		public void paintHighlight(Graphics gfx, int line, int y) {
			// Interpreter uses one-offset, processing uses zero-offset.
			Color c = null;
			RehearseLineModel m = 
				(RehearseLineModel)getTextArea().getTokenMarker().getLineModelAt(line);
			if (m != null) {
				/*
				if (m.executedInLastRun)
					c = Color.yellow;
				if (m.isMostRecentlyExecuted)
					c = Color.green;
				*/
				
				int i = Math.min(linesExecutedCount - m.countAtLastExec, 150);
				// c = new Color(i,255,i);
				c = new Color(78,127,78,200-i);
			}

			//Color c = lineHighlights.get(line + 1);
			if (c != null) {
				FontMetrics fm = textarea.getPainter().getFontMetrics();
				int height = fm.getHeight();
				y += fm.getLeading() + fm.getMaxDescent();
				gfx.setColor(c);
				gfx.fillRect(0,y,getWidth(),height);
			}

			if (next != null) {
				next.paintHighlight(gfx, line, y);
			}
		}
	}

	
//	class RehearseDocumentListener implements DocumentListener {
//	public void changedUpdate(DocumentEvent e) {
//	lineHighlights.clear();
//	getTextArea().repaint();
//	}

//	public void insertUpdate(DocumentEvent e) {
//	lineHighlights.clear();
//	getTextArea().repaint();
//	}

//	public void removeUpdate(DocumentEvent e) {
//	lineHighlights.clear();
//	getTextArea().repaint();
//	}

//	}
}
