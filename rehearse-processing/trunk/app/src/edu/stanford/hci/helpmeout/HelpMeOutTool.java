/**
 * 
 */
package edu.stanford.hci.helpmeout;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import processing.app.Editor;
import processing.app.tools.Tool;

/**
 * Implements the Processing Tool interface and gets added to the IDE's Tool Menu
 * in Editor.java addInternalTools()
 * @author bjoern
 *
 */
public class HelpMeOutTool implements Tool {

  public class ExitListener implements WindowListener {

    public void windowActivated(WindowEvent e) {
      // TODO Auto-generated method stub

    }

    public void windowClosed(WindowEvent e) {
      // TODO Auto-generated method stub

    }

    public void windowClosing(WindowEvent e) {
      // TODO Auto-generated method stub

    }

    public void windowDeactivated(WindowEvent e) {
      // TODO Auto-generated method stub

    }

    public void windowDeiconified(WindowEvent e) {
      // TODO Auto-generated method stub

    }

    public void windowIconified(WindowEvent e) {
      // TODO Auto-generated method stub

    }

    public void windowOpened(WindowEvent e) {
      // TODO Auto-generated method stub

    }

  }

  //make these static so we only have one helpmeout window, even if there are many processing windows
  private static JFrame frame = null;  // the window itself
  private static JTextPane pane = null;
  private static boolean visible = false;  

  /* (non-Javadoc)
   * @see processing.app.tools.Tool#getMenuTitle()
   */
  public String getMenuTitle() {
    return "Show/Hide HelpMeOut Window";
  }

  /* (non-Javadoc)
   * @see processing.app.tools.Tool#init(processing.app.Editor)
   */
  public void init(Editor editor) {
    if(frame==null) {
      // construct our default frame (but don't show it yet)
      frame = new JFrame("HelpMeOut");

      // Would be nice not to show the "close window" box.
      // frame.setUndecorated(true);
      // frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);

      frame.setSize(400, 400);
      Container content = frame.getContentPane();
      content.setBackground(Color.white);
      //content.setLayout(new FlowLayout()); 

      // use a JTextPane instead so we can show HTML and people can copy & paste
      pane = new JTextPane();
      pane.setContentType("text/html");
      pane.setEditable(false);
      pane.setText("HelpMeOut suggestions will be shown here.");
      JScrollPane scroll = new JScrollPane(pane);
      scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      content.add(scroll);

      frame.addWindowListener(new ExitListener());

      // register ourselves with the HelpMeOut class
      // TODO: this is problematic if we close down an earlier window,
      //may have a null ref?
      HelpMeOut.getInstance().registerTool(this);
    }
  }

  /* (non-Javadoc)
   * @see processing.app.tools.Tool#run()
   */
  public void run() {
    // toggle visibility of the window
    visible = !visible;
    frame.setVisible(visible);
  }

  /**
   * Set text of the label in this frame
   * @param text can contain HTML
   */
  public void setLabelText(String text){

    pane.setText(text);
  }
}
