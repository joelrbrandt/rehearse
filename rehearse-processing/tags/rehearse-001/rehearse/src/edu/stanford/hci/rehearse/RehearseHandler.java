package edu.stanford.hci.rehearse;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import javax.swing.JFrame;

import edu.stanford.hci.rehearse.RehearsePApplet;
import processing.app.Base;
import processing.app.Editor;
import processing.app.EditorToolbar;
import processing.app.SketchCode;
import processing.app.syntax.JEditTextArea;
import processing.app.syntax.SyntaxDocument;
import processing.app.syntax.TextAreaPainter;
import processing.app.syntax.TextAreaPainter.Highlight;
import bsh.ConsoleInterface;
import bsh.EvalError;
import bsh.Interpreter;

public class RehearseHandler implements ConsoleInterface {

		private JFrame canvasFrame;
        private RehearsePApplet applet;
        private PrintStream outputStream;
        public Editor editor;
        
        private Interpreter interpreter;

        private ArrayList<SnapshotModel> snapshots = new ArrayList<SnapshotModel>();

        private boolean wasLastRunInteractive = false;

        public static boolean logTerminationMessage = true;

        private RehearseLineModel lastExecutedLineModel = null;

        public int linesExecutedCount = 0; // TODO: refactor all this crap also this will overflow

        private static final boolean USEHIGHLIGHT = true;

        public RehearseHandler(Editor editor) {
        		this.editor = editor;
        }

        
        public void handleRun(boolean present) {
                wasLastRunInteractive = false;
                editor.handleRun(present);
        }

       
        public void handleStop() {
                if (wasLastRunInteractive) {
                        applet.stop();
                        canvasFrame.dispose();
                } else {
                        editor.handleStop();
                }
        }

        public String appendCodeFromAllTabs() {
          return appendCodeFromAllTabs(true);
        }

        public String appendCodeFromAllTabs(boolean interactive) {
                StringBuffer bigCode = new StringBuffer();
                int bigCount = 0;
                for (SketchCode sc : editor.getSketch().getCode()) {
                  if (interactive)
                    sc.setPreprocOffset(bigCount);
                        if (sc == editor.getSketch().getCurrentCode()) {
                                bigCode.append(editor.getText());
                        bigCode.append('\n');
                        bigCount += editor.getLineCount();
                        } else {
                                bigCode.append(sc.getProgram());
                        bigCode.append('\n');
                        bigCount += sc.getLineCount();
                        }
                }

                return bigCode.toString();
        }

        public SketchCode lineToSketchCode(int line) {
                for (SketchCode sc : editor.getSketch().getCode()) {
                        int lineCount;
                        if (sc == editor.getSketch().getCurrentCode()) {
                                lineCount = editor.getLineCount();
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
         // HelpMeOutLog.getInstance().write(HelpMeOutLog.STARTED_INTERACTIVE_RUN);
        	wasLastRunInteractive = true;
        	 if (USEHIGHLIGHT) {
        		 editor.getTextArea().getPainter().removeCustomHighlights();
                 editor.getTextArea().getPainter().addCustomHighlight(new RehearseHighlight());
        	 }
        	editor.statusEmpty(); //clear the status area

                
                // clear previous context
                if (canvasFrame != null)
                        canvasFrame.dispose();
                if (applet != null) {
                  // We want to explicitly call the superclass stop() function here
                  // because our overridden stop() handles some exception tracking info,
                  // namely watching to see if the exception was resolved on the last line
                  // of the program.
                        applet.resolveException = false;
                        applet.stop();
                }

                canvasFrame = new JFrame();
                canvasFrame.setLayout(new BorderLayout());
                canvasFrame.setSize(100, 100);
                canvasFrame.setResizable(false);

                applet = new RehearsePApplet();

                //applet.setupFrameResizeListener();
                applet.frame = canvasFrame;
                applet.sketchPath = editor.getSketch().getFolder().getAbsolutePath();
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

                // NOTE: If this line fails with java.lang.NoSuchMethodError you probably have a the BeanShell bshXXX.jar
                // in the classpath, e.g., /Library/Java/Extensions on OSX
                // to test, run "java bsh.Console" from terminal - if the beanshell console pops up, you'll have this problem
                // @see http://www.beanshell.org/manual/quickstart.html#Download_and_Run_BeanShell
                interpreter = new Interpreter(this, applet);

                String source = appendCodeFromAllTabs();

                // now, add our script to the processing.core package
                try {
                        interpreter.eval("package processing.core;");
                } catch (EvalError e1) {
                        e1.printStackTrace();
                }

                editor.getConsole().clear();
                ensureDocumentExistsForEveryTab();
                clearExecutionInfoForLines();
                snapshots.clear();
                editor.getTextArea().repaint();
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
                        	editor.getConsole().message(obj.toString(), false, false);
                } catch (EvalError e) {
                //  HelpMeOutExceptionTracker.getInstance().processRuntimeException(e, interpreter);
                        editor.getConsole().message(e.toString(), true, false);
                        e.printStackTrace();
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
                //getOut().append(o.toString());
                System.out.print(o.toString());
        }

        public void println(Object o) {
                //getOut().append(o.toString() + "\n");
                System.out.println(o.toString());
        }

        private void ensureDocumentExistsForEveryTab() {
                SketchCode currentCode = editor.getSketch().getCurrentCode();
                for (SketchCode sc : editor.getSketch().getCode()) {
                        SyntaxDocument doc = (SyntaxDocument)sc.getDocument();
                        if (doc == null) {
                                // This code makes the document and associates it with the
                                // SketchCode object, performing appropriate initialization steps.
                                // This isn't ideal but now we need each tab to have a valid
                                // document reference even if that tab hasn't been clicked on yet.
                        	editor.setCode(sc);
                        }
                }
                // Set the code back to the one we started.
                editor.setCode(currentCode);
        }

        void clearExecutionInfoForLines() {
                for (SketchCode sc : editor.getSketch().getCode()) {
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

                editor.getTextArea().repaint();

                if (m.isPrintPoint) {
                	snapshots.add(interpreter.makeSnapshotModel());
                }
                lastExecutedLineModel = m;
        }

        public class TextAreaOutputStream extends OutputStream {
                public void write( int b ) throws IOException {
                	editor.getConsole().message( String.valueOf( ( char )b ), false, false);
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
                                (RehearseLineModel)(editor.getTextArea().getTokenMarker().getLineModelAt(line));
                        if (m != null) {
                        	int i = Math.min(linesExecutedCount - m.countAtLastExec, 150);
                        	c = new Color(78,127,78,200-i);
                        }
                        
                        int offset = 0;
                        if (c != null) {
                                FontMetrics fm = textarea.getPainter().getFontMetrics();
                                int height = fm.getHeight();
                                offset = fm.getLeading()+ fm.getMaxDescent();
                                y += offset;
                                gfx.setColor(c);
                                gfx.fillRect(0,y,editor.getWidth(),height);
                        }

                        if (next != null) {
                        	//y -= offset;
                        	next.paintHighlight(gfx, line, y);
                        }
                }
        }
}
