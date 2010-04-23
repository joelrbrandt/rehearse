
package edu.stanford.hci.processing;

import java.awt.Frame;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import edu.stanford.hci.helpmeout.HelpMeOut;
import edu.stanford.hci.helpmeout.HelpMeOutLog;
import edu.stanford.hci.helpmeout.HelpMeOutPreferences;
import edu.stanford.hci.processing.editor.RehearseEditor;
import processing.app.Base;
import processing.app.Editor;
import processing.app.Preferences;
import processing.app.Theme;
import processing.core.PApplet;

/**
 * Entry class for the application. 
 * Wraps around the Base class from Processing in order to use our
 * specific Rehearse classes.
 */
public class RehearseBase extends Base {

	public RehearseBase(String[] args) {
		super(args);
	}
	
	@Override
	public boolean handleClose(Editor editor) {
	  boolean closing = super.handleClose(editor);
	  if (closing) {
	    ((RehearseEditor)editor).getHistoryController().closeAndDisposeHistoryView();
	  }
	  
	  return closing;
	}
	
	@Override
	public void handleOpenReplace(String path) {
	  RehearseLogger.getInstance().log(RehearseLogger.EventType.OPEN, null, path);
	  super.handleOpenReplace(path);
	}
	
	@Override
	protected Editor handleOpen(String path, int[] location) {
	  RehearseLogger.getInstance().log(RehearseLogger.EventType.OPEN, null, path);
	  System.out.println("handling open in rehearse base");
	  File file = new File(path);
	  if (!file.exists()) return null;

	  // Cycle through open windows to make sure that it's not already open.
	  for (Editor editor : editors) {
	    if (editor.getSketch().getMainFilePath().equals(path)) {
	      System.out.println("already open");
	      editor.toFront();
	      return editor;
	    }
	  }
	  System.out.println("about to make a new rehearse editor");
	  Editor editor = new RehearseEditor(this, path, location);

	  // Make sure that the sketch actually loaded
	  if (editor.getSketch() == null) {
	    return null;  // Just walk away quietly
	  }

	  editors.add(editor);

	  // now that we're ready, show the window
	  // (don't do earlier, cuz we might move it based on a window being closed)
	  editor.setVisible(true);

	  try {
	    //System.out.println("finished: " + editor);
	  }
	  catch (Exception e) {
	    e.printStackTrace();
	  }

	  return editor;
	  }
	
	 public boolean handleQuit() {
	   
	   //save to database
	   HelpMeOutLog.getInstance().saveToDatabase();
	   
	   // Write to HelpMeOut log
	   HelpMeOutLog.getInstance().saveToFile(Base.getSketchbookFolder().getAbsolutePath() + File.separator + "helpmeoutlog.txt",true);
	   
	   return super.handleQuit();
	 }

	
	 /* (non-Javadoc)
   * @see processing.app.Base#handleActivated(processing.app.Editor)
   */
  @Override
  protected void handleActivated(Editor whichEditor) {
    HelpMeOut.getInstance().setEditor(whichEditor);
    
    super.handleActivated(whichEditor);
  }

  static public void main(String args[]) {

	     System.out.println("Starting RehearseBase...");
	    try {
	      File versionFile = getContentFile("lib/version.txt");
	      if (versionFile.exists()) {
	        VERSION_NAME = PApplet.loadStrings(versionFile)[0];
	      }
	    } catch (Exception e) {
	      e.printStackTrace();
	    }

//	    if (System.getProperty("mrj.version") != null) {
//	      //String jv = System.getProperty("java.version");
//	      String ov = System.getProperty("os.version");
//	      if (ov.startsWith("10.5")) {
//	        System.setProperty("apple.laf.useScreenMenuBar", "true");
//	      }
//	    }

	    /*
	    commandLine = false;
	    if (args.length >= 2) {
	      if (args[0].startsWith("--")) {
	        commandLine = true;
	      }
	    }

	    if (PApplet.javaVersion < 1.5f) {
	      //System.err.println("no way man");
	      Base.showError("Need to install Java 1.5",
	                     "This version of Processing requires    \n" +
	                     "Java 1.5 or later to run properly.\n" +
	                     "Please visit java.com to upgrade.", null);
	    }
	    */

	    initPlatform();

//	    // Set the look and feel before opening the window
//	    try {
//	      platform.setLookAndFeel();
//	    } catch (Exception e) {
//	      System.err.println("Non-fatal error while setting the Look & Feel.");
//	      System.err.println("The error message follows, however Processing should run fine.");
//	      System.err.println(e.getMessage());
//	      //e.printStackTrace();
//	    }

	    // Use native popups so they don't look so crappy on osx
	    JPopupMenu.setDefaultLightWeightPopupEnabled(false);

	    // Don't put anything above this line that might make GUI,
	    // because the platform has to be inited properly first.

	    // Make sure a full JDK is installed
	    initRequirements();

	    // run static initialization that grabs all the prefs
	    Preferences.init(null);
	    HelpMeOutPreferences.load();

	    // setup the theme coloring fun
	    Theme.init();

	    if (Base.isMacOS()) {
	      String properMenuBar = "apple.laf.useScreenMenuBar";
	      String menubar = Preferences.get(properMenuBar);
	      if (menubar != null) {
	        // Get the current menu bar setting and use it
	        System.setProperty(properMenuBar, menubar);

	      } else {
	        // 10.4 is not affected, 10.5 (and prolly 10.6) are
	        if (System.getProperty("os.version").startsWith("10.4")) {
	          // Don't bother checking next time
	          Preferences.set(properMenuBar, "true");
	          // Also set the menubar now
	          System.setProperty(properMenuBar, "true");

	        } else {
	          // Running 10.5 or 10.6 or whatever, give 'em the business
	          String warning =
	            "<html>" +
	            "<head> <style type=\"text/css\">"+
	            "b { font: 13pt \"Lucida Grande\" }"+
	            "p { font: 11pt \"Lucida Grande\"; margin-top: 8px }"+
	            "</style> </head> <body>" +
            "<b>Some menus have been disabled.</b>" +
            "<p>Due to an Apple bug, the Sketchbook and Example menus " +
            "are unusable. <br>" +
            "As a workaround, these items will be disabled from the " +
            "standard menu bar, <br>" +
            "but you can use the Open button on " +
            "the toolbar to access the same items. <br>" +
            "If this bug makes you sad, " +
	            "please contact Apple via bugreporter.apple.com.</p>" +
	            "</body> </html>";
	          Object[] options = { "OK", "More Info" };
	          int result = JOptionPane.showOptionDialog(new Frame(),
	                                                    warning,
	                                                    "Menu Bar Problem",
	                                                    JOptionPane.YES_NO_OPTION,
	                                                    JOptionPane.WARNING_MESSAGE,
	                                                    null,
	                                                    options,
	                                                    options[0]);
	          if (result == -1) {
	            // They hit ESC or closed the window, so just hide it for now
	            // But don't bother setting the preference in the file
	          } else {
	            // Shut off in the preferences for next time
            //Preferences.set(properMenuBar, "false");
            // For 1.0.4, we'll stick with the Apple menu bar,
            // and just disable the sketchbook and examples sub-menus.
            Preferences.set(properMenuBar, "true");
	            if (result == 1) {  // More Info
	              Base.openURL("http://dev.processing.org/bugs/show_bug.cgi?id=786");
	            }
	          }
	          // Whether or not canceled, set to false (right now) if we're on 10.5
          //System.setProperty(properMenuBar, "false");
          // Changing this behavior for 1.0.4
          System.setProperty(properMenuBar, "true");
	        }
	      }
	    }

	    // Set the look and feel before opening the window
	    // For 0158, moving it lower so that the apple.laf.useScreenMenuBar stuff works
	    try {
	      platform.setLookAndFeel();
	    } catch (Exception e) {
	      System.err.println("Non-fatal error while setting the Look & Feel.");
	      System.err.println("The error message follows, however Processing should run fine.");
	      System.err.println(e.getMessage());
	      //e.printStackTrace();
	    }

	    // Create a location for untitled sketches
	    untitledFolder = createTempFolder("untitled");
	    untitledFolder.deleteOnExit();

	    new RehearseBase(args);
	  }
	 
	 
}
