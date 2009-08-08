/**
 * 
 */
package edu.stanford.hci.helpmeout;

import java.awt.Color;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import processing.app.Editor;
import processing.app.tools.Tool;

/**
 * Implements the Processing Tool interface and gets added to the IDE's Tool Menu
 * in Editor.java addInternalTools()
 * @author bjoern
 *
 */
public class HelpMeOutTool implements Tool, HyperlinkListener {

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
      pane.addHyperlinkListener(this);

      pane.setText("HelpMeOut suggestions will be shown here.");
      JScrollPane scroll = new JScrollPane(pane);
      scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      content.add(scroll);

      frame.addWindowListener(new ExitListener());
      
      // Set an initial editor for HelpMeOut.
      HelpMeOut.getInstance().setEditor(editor);

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

  /**
   * click handler; links are of form URL?action=<action>&id=<id>
   */
  public void hyperlinkUpdate(HyperlinkEvent e) {
    
    //only respond to this event if the link was actually clicked on
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {

      String query = e.getURL().getQuery();
      Map<String, String> map = getQueryMap(query);

      if("copy".matches(map.get("action"))) {
        HelpMeOut.getInstance().handleCopyAction(Integer.parseInt(map.get("id")));
      } 
      // for a "thumbs up link:
      else if("up".matches(map.get("action"))){
        HelpMeOut.getInstance().handleVoteUpAction(Integer.parseInt(map.get("id")));
      } 
      // for a "thumbs down" link:
      else if("down".matches(map.get("action"))) {
        HelpMeOut.getInstance().handleVoteDownAction(Integer.parseInt(map.get("id")));
      } 
      else if("detail".matches(map.get("action"))) {
        //launch web browser
        HelpMeOut.getInstance().handleShowDetailAction(Integer.parseInt(map.get("id")));
      }
      else {
        //just print for now
        Set<String> keys = map.keySet();

        for (String key : keys)
        {
          System.out.println("Name=" + key);
          System.out.println("Value=" + map.get(key));
        }
      }
    }
  }
  //from: http://www.coderanch.com/t/383310/Java-General-intermediate/java/parse-url-query-string-parameter

  private static Map<String, String> getQueryMap(String query)
  {
    String[] params = query.split("&");
    Map<String, String> map = new HashMap<String, String>();
    for (String param : params)
    {
      String name = param.split("=")[0];
      String value = param.split("=")[1];
      map.put(name, value);
    }
    return map;
  }

}
