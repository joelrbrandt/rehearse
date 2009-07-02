package edu.stanford.hci.helpmeout;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.googlecode.jj1.ServiceProxy;

/**
 * HelpMeOut
 * make sure jj1.0.1.jar and stringtree-json-2.0.5.jar are in build path
 * i copied to processing/lib and added to processing project build path
 * @author bjoern
 *
 */

public class HelpMeOut {
  
  private static final String SERVICE_URL = "http://rehearse.stanford.edu/helpmeout/server.py";
  
  // make it a Singleton
  private static HelpMeOut instance = new HelpMeOut();
  private ServiceProxy proxy = new ServiceProxy(SERVICE_URL);
  private HelpMeOut(){}
  public static HelpMeOut getInstance() {
    return instance;
  }
 
  // states of the FSM
  private enum CodeState {BROKEN,FIXED};
  CodeState codeState = CodeState.FIXED;
  
  String lastErrorMsg = null;
  String lastErrorCode = null;
  
  /**
   * Simple test: call an echo function that takes a string and returns that same string
   */
  private void echo() {
    String result = (String)proxy.call("echo", "hello you!");
    System.out.println(result);
  }

  /**
   * Store a compile error fix in the remote database via JSON-RPC
   * 
   * @param error The error string
   * @param s0 The "old" file contents (i.e., the one with the compile error)
   * @param s1 The "new" file contents (without compile error)
   */
  private void store(String error, String s0, String s1) {
    if((error!=null)&&(s0!=null)&&(s1!=null)) {
      try {

        String result = (String)proxy.call("store2",error, s0, s1);

      }catch (Exception e) {
        System.err.println("couldn't store.");
        e.printStackTrace();
      }
    } else {
      System.err.println("store called with at least one null argument. tsk tsk.");
    }
  }

    /**
     * Query the remote HelpMeOut database for relevant example fixes.
     * Via JSON-RPC
     * 
     * @param error The compile error string of the current error
     * @param code The line of code referenced by the compile error
     */
    public void query(String error, String code, JFrame frame) {
      
      if(tool!=null) {
        tool.setLabelText("Querying...");
      }
      
      try {
        int i=1;
        String suggestions ="<html><body>";
        ArrayList<HashMap<String,ArrayList<String>>> result = 
          (ArrayList<HashMap<String,ArrayList<String>>>) proxy.call("query", error, code);
        suggestions+="<h3>Error</h3><p>"+error+"</p>";
        for(HashMap<String,ArrayList<String>> m:result) {
          suggestions += "<h3>Suggestion "+Integer.toString(i++)+"</h3>";
          suggestions += "<pre>";
          if(m.containsKey("old")) {
            //suggestions += "=== BEFORE ===\n";
            ArrayList<String> o = (ArrayList<String>)m.get("old");
            for(String s:o){
              suggestions+="BEFORE  "+s;
            }
          }
          if(m.containsKey("new")) {
            //suggestions += "=== AFTER ===\n";
            ArrayList<String> o = (ArrayList<String>)m.get("new");
            for(String s:o){
              suggestions += "AFTER   "+s;
            }
          }
          suggestions += "</pre>";
        }
        suggestions+="</body></html>";
        
        //show in a popup window:
        //JLabel label= new JLabel(suggestions);
        //JOptionPane.showMessageDialog(frame, label,"HelpMeOut Suggestions",JOptionPane.PLAIN_MESSAGE);
        
        //now show the suggestion in the HelpMeOut window
        if(tool!=null) {
          tool.setLabelText(suggestions);
        }

      } catch (Exception e) {
        System.err.println("HelpMeOutQuery: couldn't query or wrong type returned.");
        if(tool!=null) {
          
          tool.setLabelText("HelpMeOutQuery did not return any suggestions.");
        }
        //e.printStackTrace();
      }
    }
    
    public void processNoError(String code) {
      switch(codeState) {
      case BROKEN:
        //went from broken to fixed - great!
        //shove it into our db
        System.out.println("processFixed: saving fix to db...");
        
        store(lastErrorMsg,lastErrorCode,code);
        lastErrorMsg = null;
        lastErrorCode = null;
        codeState = CodeState.FIXED;
        break;
      case FIXED:
        //do nothing
        System.out.println("processFixed: nothing to do");
      }
    }
    public void processBroken(String error, String code) {
      switch(codeState) {
      case FIXED:
        System.out.println("processBroken: saving initial error state");
        lastErrorCode = code;
        lastErrorMsg = error;
        codeState = CodeState.BROKEN;
        break;
      case BROKEN:
        //do nothing;
        System.out.println("processBroken: nothing to do");
      }
      
    }
    
    /**
     * Save a reference to the HelpMeOutTool which we need to show text in the separate Tool window
     * @param toggleHelpMeOutWindowTool
     */
    private HelpMeOutTool tool=null;
    public void registerTool(HelpMeOutTool tool) {
      this.tool = tool;
    }
    
}