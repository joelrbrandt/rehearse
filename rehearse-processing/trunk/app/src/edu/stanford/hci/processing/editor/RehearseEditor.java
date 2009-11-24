package edu.stanford.hci.processing.editor;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.Date;

import javax.swing.JMenu;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import processing.app.Base;
import processing.app.Editor;
import processing.app.EditorToolbar;
import processing.app.SketchCode;
import processing.app.syntax.JEditTextArea;
import processing.app.syntax.SyntaxDocument;
import processing.app.syntax.TextAreaPainter;
import processing.app.syntax.TextAreaPainter.Highlight;
import processing.video.MovieMaker;
import bsh.ConsoleInterface;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.ParserInfoGetter;
import edu.stanford.hci.helpmeout.HelpMeOut;
import edu.stanford.hci.helpmeout.HelpMeOutExceptionTracker;
import edu.stanford.hci.helpmeout.HelpMeOutLog;
import edu.stanford.hci.processing.ModeException;
import edu.stanford.hci.processing.RehearseCanvasFrame;
import edu.stanford.hci.processing.RehearseLogger;
import edu.stanford.hci.processing.RehearsePApplet;
import edu.stanford.hci.processing.VersionHistory;
import edu.stanford.hci.processing.VersionHistoryController;
import edu.stanford.hci.processing.VersionHistoryFrame;

public class RehearseEditor extends Editor implements ConsoleInterface {

	private RehearseCanvasFrame canvasFrame;
	private RehearsePApplet applet;
	private PrintStream outputStream;

	private Interpreter interpreter;

	private boolean wasLastRunInteractive = false;
	
	private boolean isInInteractiveRun = false;

	public static boolean logTerminationMessage = true;

	private RehearseLineModel lastExecutedLineModel = null;
	private ParserInfoGetter pig = null;
	private DocumentListener documentListener = new RehearseDocumentListener();
	
	public int linesExecutedCount = 0; // TODO: refactor all this crap also this
										// will overflow
	
	private VersionHistoryController historyController;

  private  boolean useHighlight = false;
	
	private static Image defaultImage = new BufferedImage(100,100,BufferedImage.TYPE_INT_ARGB_PRE);
	
	private static final boolean OPEN_VERSION_HISTORY = true;
	
	public static final String VIDEO_RECORDING_EXTENTION = ".mov";
	
	public RehearseEditor(Base ibase, String path, int[] location) {
		super(ibase, path, location);
	}

	@Override
	public EditorToolbar newEditorToolbar(Editor editor, JMenu menu) {
		System.out.println("Making a Reherase Editor toolbar");
		return new RehearseEditorToolbar(editor, menu);
	}
	
	@Override
	protected boolean handleOpenInternal(String path) {
	  boolean opened = super.handleOpenInternal(path);
	  if (OPEN_VERSION_HISTORY) {
	    if (historyController != null) {
	      historyController.closeAndDisposeHistoryView();
	    }
      historyController = new VersionHistoryController(this);
      historyController.openHistoryView();
    }
	  return opened;
	}

	@Override
	public void handleRun(boolean present) {
		wasLastRunInteractive = false;
		HelpMeOutLog.getInstance().write(HelpMeOutLog.STARTED_COMPILED_RUN);
		RehearseLogger.getInstance().log(RehearseLogger.EventType.COMPILED_RUN,
				getSketch(), appendCodeFromAllTabs(false));
		super.handleRun(present);
	}
	
	public void handleInteractiveRunEnd() {
	  isInInteractiveRun = false;
	  
	  applet.stop();
	  
	  // This check is needed since save also calls handleStop.
    if (canvasFrame.isShowing()) {
      logRunFeedback(true);
      
      historyController.updateLastRunVideo(applet.getVideoRecording());
      historyController.updateLastRunScreenshot(applet.get().getImage());
    }
	}

	@Override
	public void handleStop() {
		if (wasLastRunInteractive) {
		  handleInteractiveRunEnd();
			canvasFrame.dispose();
		} else {
			super.handleStop();
		}
		isInInteractiveRun = false;
	}

	public String appendCodeFromAllTabs() {
		return appendCodeFromAllTabs(true);
	}

	public String appendCodeFromAllTabs(boolean interactive) {
		StringBuffer bigCode = new StringBuffer();
		int bigCount = 0;
		for (SketchCode sc : getSketch().getCode()) {
			if (interactive)
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

	public SketchCode lineToSketchCode(int line) {
		for (SketchCode sc : getSketch().getCode()) {
			int lineCount;
			if (sc == getSketch().getCurrentCode()) {
				lineCount = getLineCount();
			} else {
				lineCount = sc.getLineCount();
			}

			if (line >= sc.getPreprocOffset()
					&& line < sc.getPreprocOffset() + lineCount) {
				return sc;
			}
		}

		return null;
	}
	
	public RehearseCanvasFrame getCanvasFrame() {
	  return canvasFrame;
	}
	
	public Interpreter getInterpreter() {
	  return interpreter;
	}
	
	public VersionHistoryController getHistoryController() {
	  return historyController;
	}
	
	public void setIsInInteractiveRun(boolean isInInteractiveRun) {
	  this.isInInteractiveRun = isInInteractiveRun;
	}
	
	public void runHistoryCode(String source) {
	  RehearsePApplet applet = new RehearsePApplet();
    applet.sketchPath = null;
    RehearseCanvasFrame canvasFrame = new RehearseCanvasFrame(null, applet);
    applet.frame = canvasFrame;
    
    Interpreter interpreter = new Interpreter(this, applet);
 // Add the sketch classpath to BeanShell interpreter
    if (getSketch().getClassPath() != null) {
      String[] classPaths = getSketch().getClassPath().split(";");
      for (String classPath : classPaths) {
        try {
          File file = new File(classPath);
          interpreter.getClassManager()
              .addClassPath(file.toURI().toURL());
        } catch (MalformedURLException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    // now, add our script to the processing.core package
    try {
      interpreter.eval("package processing.core;");
    } catch (EvalError e1) {
      e1.printStackTrace();
    }

    try {
      Object obj;
      try {
        obj = interpreter.eval(source, true);

      } catch (ModeException e) {
        if (e.isJavaMode()) {
          throw new RuntimeException("We don't do java mode yet!");
        } else {
          // Code was written in static mode, let's try again.
          System.out
              .println("Code written in static mode, wrapping and restarting.");
          // this is kind of gross...
          obj = interpreter.eval("setup() {" + source + "}");
        }
      }
      // This actually starts the program.
      applet.init();
    } catch (EvalError e) {
      e.printStackTrace();
    }
	}

	public void handleInteractiveRun() {
		HelpMeOutLog.getInstance().write(HelpMeOutLog.STARTED_INTERACTIVE_RUN);

		statusEmpty(); // clear the status area
		
		this.getTextArea().getDocument().removeDocumentListener(documentListener);
		this.getTextArea().getDocument().addDocumentListener(documentListener);

		wasLastRunInteractive = true;
		// clear previous context
		if (canvasFrame != null)
			canvasFrame.dispose();
		if (applet != null) {
			// We want to explicitly call the superclass stop() function here
			// because our overridden stop() handles some exception tracking
			// info,
			// namely watching to see if the exception was resolved on the last
			// line
			// of the program.
			applet.resolveException = false;
			applet.stop();
		}

		applet = new RehearsePApplet();
		applet.sketchPath = getSketch().getFolder().getAbsolutePath();
		canvasFrame = new RehearseCanvasFrame(this, applet);
		applet.frame = canvasFrame;

		// NOTE: If this line fails with java.lang.NoSuchMethodError you
		// probably have a the BeanShell bshXXX.jar
		// in the classpath, e.g., /Library/Java/Extensions on OSX
		// to test, run "java bsh.Console" from terminal - if the beanshell
		// console pops up, you'll have this problem
		// @see
		// http://www.beanshell.org/manual/quickstart.html#Download_and_Run_BeanShell
		interpreter = new Interpreter(this, applet);
		// interpreter.setStrictJava(true);

		/*
		 * No longer sure this is needed, I think just setting the package
		 * solves all problems // Add current classpath to the interpreter's
		 * classpath String classpath = System.getProperty("java.class.path");
		 * String[] paths = classpath.split(":"); for (String p : paths) { URL
		 * processingClassPath = null; try { processingClassPath = new
		 * URL("file://" + p); } catch (MalformedURLException e1) {
		 * e1.printStackTrace(); }
		 * 
		 * if (processingClassPath != null) { try {
		 * interpreter.getClassManager().addClassPath(processingClassPath); }
		 * catch (IOException e1) { e1.printStackTrace(); } } }
		 */

		// Compiles the interactive program to check for compile errors.
		// If it fails, stop running.
		boolean compiled = HelpMeOutCompile();
		if (!compiled) {
			return;
		}

		String source = appendCodeFromAllTabs();
		RehearseLogger.getInstance().log(
				RehearseLogger.EventType.INTERACTIVE_RUN, getSketch(), source);
		
		// Add entry to history.
		int versionNo = historyController.size();
		VersionHistory vh = new VersionHistory(versionNo, defaultImage, source, 
		                                       new Date());
		historyController.addVersionHistory(vh);
		applet.setVersion(versionNo);

		// Add the sketch classpath to BeanShell interpreter
		String[] classPaths = getSketch().getClassPath().split(";");
		for (String classPath : classPaths) {
			try {
				File file = new File(classPath);
				interpreter.getClassManager()
						.addClassPath(file.toURI().toURL());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// now, add our script to the processing.core package
		try {
			interpreter.eval("package processing.core;");
		} catch (EvalError e1) {
			e1.printStackTrace();
		}

		console.clear();
		ensureDocumentExistsForEveryTab();
		clearExecutionInfoForLines();
		// lineHighlights.clear();
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
					System.out
							.println("Code written in static mode, wrapping and restarting.");
					// this is kind of gross...
					obj = interpreter.eval("setup() {" + source + "}");
				}
			}
			// This actually starts the program.
			applet.init();
			pig = null;
			isInInteractiveRun = true;

			if (obj != null)
				console.message(obj.toString(), false, false);
		} catch (EvalError e) {
			RehearseLogger.getInstance().log(
					RehearseLogger.EventType.RUNTIME_ERROR, getSketch(),
					e.toString());
			HelpMeOutExceptionTracker.getInstance().processRuntimeException(e,
					interpreter);
			console.message(e.toString(), true, false);
			e.printStackTrace();
		}
	}

	@Override
	public boolean handleSave(boolean immediately) {
		RehearseLogger.getInstance().log(RehearseLogger.EventType.SAVE,
				getSketch(), appendCodeFromAllTabs());
		return super.handleSave(immediately);
	}

	@Override
	public boolean handleSaveAs() {
		String oldPath = getSketch().getFolder().getAbsolutePath();
		boolean saved = super.handleSaveAs();
		if (saved) {
			String logMessage = "Oldpath: " + oldPath + "\r\n"
					+ appendCodeFromAllTabs();
			RehearseLogger.getInstance().log(RehearseLogger.EventType.SAVE_AS,
					getSketch(), logMessage);
		}

		return saved;
	}

	@Override
	public void handlePaste() {
		Clipboard clipboard = getToolkit().getSystemClipboard();
		try {
			String pasteText = ((String) clipboard.getContents(this)
					.getTransferData(DataFlavor.stringFlavor)).replace('\r',
					'\n');
			RehearseLogger.getInstance()
					.log(RehearseLogger.EventType.CODE_PASTE, getSketch(),
							pasteText);
		} catch (Exception e) {
			// Do nothing. Exception will be handled in the superclass.
		}

		super.handlePaste();
	}

	private boolean HelpMeOutCompile() {
		try {

			String appletClassName = getSketch().compile();
			if (appletClassName != null) {
				HelpMeOut.getInstance().processNoError(
						appendCodeFromAllTabs(false));
				// if no exception has occurred yet, send text to HelpMeOut
				// Exception class
				HelpMeOutExceptionTracker.getInstance().setSource(
						appendCodeFromAllTabs(false));
				if (HelpMeOutExceptionTracker.getInstance()
						.hasExceptionOccurred()) {
					
				  // TODO (Will): here or in handleInteractiveRun() above:
					int lineToWatch = HelpMeOutExceptionTracker.getInstance()
							.getLineToWatch();
					interpreter.setLineToWatch(lineToWatch + 1); // interpreter
																	// is
																	// 1-indexed

				} else {
					// Just in case the interpreter is reused
					interpreter.setLineToWatch(-1);
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
		// getOut().append(o.toString());
		System.out.print(o.toString());
	}

	public void println(Object o) {
		// getOut().append(o.toString() + "\n");
		System.out.println(o.toString());
	}

	private void ensureDocumentExistsForEveryTab() {
		SketchCode currentCode = getSketch().getCurrentCode();
		for (SketchCode sc : getSketch().getCode()) {
			SyntaxDocument doc = (SyntaxDocument) sc.getDocument();
			if (doc == null) {
				// This code makes the document and associates it with the
				// SketchCode object, performing appropriate initialization
				// steps.
				// This isn't ideal but now we need each tab to have a valid
				// document reference even if that tab hasn't been clicked on
				// yet.
				setCode(sc);
			}
		}
		// Set the code back to the one we started.
		setCode(currentCode);
	}

	private void clearExecutionInfoForLines() {
		for (SketchCode sc : getSketch().getCode()) {
			SyntaxDocument doc = (SyntaxDocument) sc.getDocument();
			for (int line = 0; line < doc.getTokenMarker().getLineCount(); line++) {
				RehearseLineModel m = (RehearseLineModel) doc.getTokenMarker()
						.getLineModelAt(line);
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
		SyntaxDocument doc = (SyntaxDocument) sc.getDocument();

		RehearseLineModel m = (RehearseLineModel) doc.getTokenMarker()
				.getLineModelAt(line - sc.getPreprocOffset());
		if (m == null) {
			m = new RehearseLineModel();
			doc.getTokenMarker()
					.setLineModelAt(line - sc.getPreprocOffset(), m);
		}

		m.executedInLastRun = true;
		m.isMostRecentlyExecuted = true;
		m.countAtLastExec = linesExecutedCount;

		getTextArea().repaint();

		if (m.isPrintPoint) {
		  canvasFrame.addSnapshot(interpreter.makeSnapshotModel());
		}

		lastExecutedLineModel = m;
	}

	public class TextAreaOutputStream extends OutputStream {
		public void write(int b) throws IOException {
			console.message(String.valueOf((char) b), false, false);
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
			RehearseLineModel m = (RehearseLineModel) getTextArea()
					.getTokenMarker().getLineModelAt(line);
			if (m != null) {
				/*
				 * if (m.executedInLastRun) c = Color.yellow; if
				 * (m.isMostRecentlyExecuted) c = Color.green;
				 */

				int i = Math.min(linesExecutedCount - m.countAtLastExec, 150);
				// c = new Color(i,255,i);
				c = new Color(78, 127, 78, 200 - i);
			}

			// Color c = lineHighlights.get(line + 1);
			if (c != null) {
				FontMetrics fm = textarea.getPainter().getFontMetrics();
				int height = fm.getHeight();
				y += fm.getLeading() + fm.getMaxDescent();
				gfx.setColor(c);
				gfx.fillRect(0, y, getWidth(), height);
			}

			if (next != null) {
				next.paintHighlight(gfx, line, y);
			}
		}
	}

	public void breakOnDrawEdit() {
	  interpreter.setBreakpoint(pig.getDrawMethodFirstLine());
	  System.out.println("Breaking on draw");
	}
	
	public boolean resumeWithDrawUpdate() {
	  String source = appendCodeFromAllTabs();
	  boolean noError = pig.parseCode(source);
	  if (!noError) return false;
	  
	  // Add entry to history.
	  // (Abel) Includes version number and video filename
	  applet.startNewMovie();
    VersionHistory vh = new VersionHistory(historyController.size(), defaultImage, source, 
                                           new Date());
    historyController.addVersionHistory(vh);
	  
	  interpreter.updateDrawMethod(pig.getDrawMethodNode());
	  interpreter.resume();
	  
	  return true;
	}
	
	public void swapRunningCode(String code) {
	  if (pig == null) {
	    pig = new ParserInfoGetter();
	  }
    pig.parseCode(code);
    historyController.updateLastRunScreenshot(applet.get().getImage());
    interpreter.updateDrawMethod(pig.getDrawMethodNode());
	}

	public void uploadSketchToServer() {
		RehearseSketchUploader.uploadSketchToServer(this);
	}

	class RehearseDocumentListener implements DocumentListener {
		public void changedUpdate(DocumentEvent e) {
		  // Do nothing.
		}
		
		private void checkForDrawMethodEdits(DocumentEvent e) {
		  System.out.println("Is in interactive run: " + isInInteractiveRun);
		  if (!isInInteractiveRun) return;
		  
		  if(pig == null){
        pig = new ParserInfoGetter();
        pig.parseCode(appendCodeFromAllTabs());
      }
      
      if (pig.getDrawMethodNode() == null) {
        pig.parseCode(appendCodeFromAllTabs());
      } else if(pig.isEditInDrawMethod(getTextArea().getLineOfOffset(e.getOffset()))){
        //pig.parseCode(appendCodeFromAllTabs());
        breakOnDrawEdit();
      }
		}

		public void insertUpdate(DocumentEvent e) {
		  checkForDrawMethodEdits(e);
		}

		public void removeUpdate(DocumentEvent e) {
		  checkForDrawMethodEdits(e);
		}
	}

  public void toggleHighlights() {
    useHighlight = !useHighlight;
    
    if (useHighlight) {
      getTextArea().getPainter().addCustomHighlight(new RehearseHighlight());
    } else {
      getTextArea().getPainter().removeCustomHighlights();
    }
  }
}
