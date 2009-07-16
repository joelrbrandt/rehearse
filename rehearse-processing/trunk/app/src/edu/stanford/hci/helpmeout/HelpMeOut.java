package edu.stanford.hci.helpmeout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import processing.app.Editor;

import bsh.EvalError;

import com.googlecode.jj1.ServiceProxy;

import edu.stanford.hci.helpmeout.diff_match_patch.Patch;

/**
 * HelpMeOut
 * make sure jj1.0.1.jar and stringtree-json-2.0.5.jar are in build path
 * i copied to processing/lib and added to processing project build path
 * @author bjoern
 *
 */

public class HelpMeOut {

  //private class to store info of queried fixes
  class FixInfo {

    public FixInfo(int id, String brokenCode, String fixedCode) {
      this.id = id;
      this.brokenCode = brokenCode;
      this.fixedCode=  fixedCode;
    }

    public int id;
    public String fixedCode;
    public String brokenCode;
  }

  protected static final String SERVICE_URL = "http://rehearse.stanford.edu/helpmeout/server-dev.py"; //URL of DB server to hit with JSON-RPC calls
  private Map<Integer,FixInfo> currentFixes = new HashMap<Integer,FixInfo>(); // temp storage for currently displayed fixes, so we can copy them

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

  // keep track of msg and code for FSM
  String lastErrorMsg = null;
  String lastErrorCode = null;

  //store last query parameters in case we need to re-query
  String lastQueryMsg = null;
  String lastQueryCode = null;
  private int lastQueryLine;
  private Editor lastQueryEditor = null;

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
  
  protected void showQueryResult(ArrayList<HashMap<String,ArrayList<String>>> result, String error) {

    if(tool!=null) {
      tool.setLabelText("Querying...");
    }

    currentFixes.clear();
    int i=1;
    String suggestions ="<html><head>"+
    //    "<meta http-equiv='Content-Type' content='text/html; charset=ISO-8859-1' />"+
    //    "<title>HelpMeOut</title>"+
    "<style type=\"text/css\">"+
    "body {margin:7px; font-family:Arial,Helvetica; background-color:#f0f0f0;}"+
    "    table.diff {font-family:Courier; font-size:9px; border:medium; border-style:solid; border-width:1px; background-color:#ffffff;}"+
    "    .diff_header {background-color:#e0e0e0;}"+
    "    td.diff_header {text-align:right; }"+ //these are the line# cells
    "    .diff_next {background-color:#c0c0c0; border-style:solid border-width:1px; }"+
    "    .diff_add {background-color:#aaffaa}"+
    "    .diff_chg {background-color:#ffff77}"+
    "    .diff_sub {background-color:#ffaaaa}"+
    "    div {font-family:Arial,Helvetica; font-size:9px;}"+
    "</style>"+
    "</head><body>";

    //        String suggestions ="<html><body>";
    suggestions+="<h3>Error Message:</h3>"+error+"";
    for(HashMap<String,ArrayList<String>> m:result) {
      suggestions += "<h3>Suggestion "+Integer.toString(i)+"</h3>";


      //          suggestions += "<pre>";
      //          if(m.containsKey("old")) {
      //            //suggestions += "=== BEFORE ===\n";
      //            ArrayList<String> o = (ArrayList<String>)m.get("old");
      //            for(String s:o){
      //              suggestions+="BEFORE  "+s;
      //            }
      //          }
      //          if(m.containsKey("new")) {
      //            //suggestions += "=== AFTER ===\n";
      //            ArrayList<String> o = (ArrayList<String>)m.get("new");
      //            for(String s:o){
      //              suggestions += "AFTER   "+s;
      //            }
      //          }
      //          suggestions += "</pre>";

      //new format: generate html on server-side

      if(m.containsKey("table")) {
        //extract fix id 
        int fixId = Integer.parseInt(((ArrayList<String>)m.get("id")).get(0));

        // add python-generated diff table to the page
        // remove <br /> b/c java can't deal with them;
        // also remove <a href="">n</a> and <a href="">t</a> links because they are distracting
        suggestions+= ((ArrayList<String>)m.get("table")).get(0).replaceAll("<br />", "").replaceAll(">[nt]</a>", "></a>&nbsp;");

        // add links to vote up/down and to copy a fix 
        suggestions += "<div><a class=\"thumb_link\" href=\"http://rehearse.stanford.edu/?id="+Integer.toString(i)+"&action=up\">thumbs up</a> | <a class=\"thumb_link\" href=\"http://rehearse.stanford.edu/?id="+Integer.toString(i)+"&action=down\">thumbs down</a> | <a class=\"thumb_link\" href=\"http://rehearse.stanford.edu/?id="+Integer.toString(i)+"&action=copy\">copy this fix</a></div>";

        // assemble fixed code lines and save to currentFixes
        // so we can copy+paste easily later
        if(m.containsKey("new")) {
          String fixedLines = "";
          ArrayList<String> n = (ArrayList<String>)m.get("new");
          for(String s:n){
            fixedLines+=s;
          }
          String brokenLines = "";
          ArrayList<String> o = (ArrayList<String>)m.get("old");
          for(String s:o){
            brokenLines+=s;
          }

          currentFixes.put(i, new FixInfo(fixId,brokenLines,fixedLines));
        }

      }

      i++;
    }
    suggestions+="</body></html>";


    //now show the suggestion in the HelpMeOut window
    if(tool!=null) {
      tool.setLabelText(suggestions);
    }

  }

  /**
   * Query the remote HelpMeOut database for relevant example fixes.
   * Via JSON-RPC
   * 
   * @param error The compile error string of the current error
   * @param code The line of code referenced by the compile error
   */
  public void query(String error, String code, int line, Editor editor) {
    lastQueryMsg = error;
    lastQueryCode = code;
    lastQueryLine = line;
    lastQueryEditor = editor;
    
    try {
      ArrayList<HashMap<String,ArrayList<String>>> result = 
        (ArrayList<HashMap<String,ArrayList<String>>>) proxy.call("query", error, code);
      showQueryResult(result, error);
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
    // Always save the last error, even if already broken
    System.out.println("processBroken: saving last error state");
    lastErrorCode = code;
    lastErrorMsg = error;
    codeState = CodeState.BROKEN;
  }

  /**
   * Save a reference to the HelpMeOutTool which we need to show text in the separate Tool window
   * @param toggleHelpMeOutWindowTool
   */
  private HelpMeOutTool tool=null;
  public void registerTool(HelpMeOutTool tool) {
    this.tool = tool;
  }
  
  protected HelpMeOutTool getTool() {
    return tool;
  }

  /**
   * Copy the source code of a currently displayed fix to the system clipboard
   * Gets called from HelpMeOutTool's hyperlinkUpdate() link-click event handler
   * http://www.devx.com/Java/Article/22326/0/page/4
   * @param i index into list of suggestions (1-3) - which fix to copy
   */
  public void handleCopyAction(int i) {
    assert(lastQueryEditor != null);
    String pasteText;
    FixInfo f = currentFixes.get(i);
    try {
      
      //first, try to auto-apply patch
      //TODO: make this smarter and token-based
      
      diff_match_patch dmp = new diff_match_patch();
      LinkedList<Patch> pList = dmp.patch_make(f.brokenCode, f.fixedCode);
      Object[] pResult = dmp.patch_apply(pList, lastQueryCode);
      String patchedText = (String)pResult[0];
      boolean[] patchFlags = (boolean[])pResult[1];
      boolean patchSuccess = false;
      
      //see if we actually applied anything
      for(boolean b : patchFlags) {
        if(b) {patchSuccess = true; break;}
      }
      //if we didn't, give up and let user merge manually
      if(!patchSuccess) throw new Exception("could not apply any patches");
      
      //otherwise, copy our patch (fingers crossed)
      pasteText = "// HELPMEOUT AUTO-PATCH. ORIGINAL: "+lastQueryCode+patchedText+"\n";

    } catch (Exception e) { //diff-match-path can throw StringIndexOutOfBoundsException 
      pasteText = "// HELPMEOUT MANUAL PATCH. ORIGINAL\n"+lastQueryCode+"// SUGGESTED FIX\n//"+currentFixes.get(i).fixedCode.replaceAll("\n","\n//")+"\n";
    }
    
    //now replace the error line with our fix (makes the assumption that the error was actually at that line
    pasteIntoEditor(lastQueryLine,lastQueryEditor,pasteText);

    //      Clipboard systemClipboard = 
    //        Toolkit.getDefaultToolkit().getSystemClipboard(); 
    //        Transferable transferableText =
    //          new StringSelection(currentFixes.get(i).fixedCode);
    //        systemClipboard.setContents(transferableText, null);
  }

  /**
   * Event handler for clicks on the vote up link in the helpmeout ui.
   * @param i suggestion id extracted from link
   */
  public void handleVoteUpAction(int i) {
    voteAndRequery(currentFixes.get(i).id, 1);

  }
  /**
   * Event handler for clicks on the vote down link in the helpmeout ui.
   * @param i suggestion id extracted from link
   */    
  public void handleVoteDownAction(int i) {
    voteAndRequery(currentFixes.get(i).id, -1);      
  }

  /**
   * Call the errorvote service method to store vote in table
   * @param fixid id of fix in db table
   * @param vote numeric up or down vote
   */
  private void voteAndRequery(int fixid, int vote) {
    try {
      // using id, call database method
      proxy.call("errorvote",fixid,vote);

    } catch (Exception e) {
      System.err.println("couldn't call errorvote servicemethod.");
      e.printStackTrace();
    }
    // then re-query?
    query(lastQueryMsg,lastQueryCode,lastQueryLine, lastQueryEditor);
  }

  private void pasteIntoEditor(int line, Editor editor,String fix) {
    editor.setLineText(line, fix);
    editor.setSelection(editor.getLineStartOffset(line), editor.getLineStartOffset(line)+fix.length());
    

  }
 
}