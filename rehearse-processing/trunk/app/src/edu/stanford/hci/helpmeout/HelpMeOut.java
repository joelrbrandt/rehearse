package edu.stanford.hci.helpmeout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import processing.app.Editor;

import bsh.EvalError;
import bsh.Interpreter;

import com.googlecode.jj1.ServiceProxy;

import edu.stanford.hci.helpmeout.diff_match_patch.Diff;
import edu.stanford.hci.helpmeout.diff_match_patch.Operation;
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
  
  // types of errors
  enum ErrorType {COMPILE, RUN};
  ErrorType errorType = ErrorType.COMPILE;
  
  // keep track of msg and code for FSM
  String lastErrorMsg = null;
  String lastErrorCode = null;

  //store last query parameters in case we need to re-query
  String lastQueryMsg = null;
  String lastQueryCode = null;
  private int lastQueryLine;
  private Editor lastQueryEditor = null;
  
  //store last query parameters for runtime exceptions
  private EvalError lastEvalError;
  private Interpreter lastInterpreter;

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
        HelpMeOutLog.getInstance().writeError("couldn't store");
        e.printStackTrace();
      }
    } else {
      HelpMeOutLog.getInstance().writeError("store called with at least one null argument. tsk tsk.");
    }
  }
  
  protected void showQueryResult(ArrayList<HashMap<String,ArrayList<String>>> result, String error, ErrorType errorType) {
    
    // Set the error type so voting knows which table to update
    this.errorType = errorType;

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
      showQueryResult(result, error, ErrorType.COMPILE);
      HelpMeOutLog.getInstance().write("HelpMeOutQuery for \"" +error+ "\" succeeded.");
    } catch (Exception e) {
      HelpMeOutLog.getInstance().writeError("HelpMeOutQuery: couldn't query or wrong type returned.");
      if(tool!=null) {

        tool.setLabelText("HelpMeOutQuery did not return any suggestions.");
      }
      //e.printStackTrace();
    }
  }

  public void processNoError(String code) {
    if(tool!=null) {
      tool.setLabelText("Code compiled successfully. No need to help you out (yet).");
    }
    switch(codeState) {
    case BROKEN:
      //went from broken to fixed - great!
      //shove it into our db
      HelpMeOutLog.getInstance().write("processFixed: saving fix to db...");

      store(lastErrorMsg,lastErrorCode,code);
      lastErrorMsg = null;
      lastErrorCode = null;
      codeState = CodeState.FIXED;
      break;
    case FIXED:
      //do nothing
      HelpMeOutLog.getInstance().write("processFixed: nothing to do");
    }
  }
  public void processBroken(String error, String code) {
    // Always save the last error, even if already broken
    HelpMeOutLog.getInstance().write("processBroken: saving last error state");
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
    
    // The line we're fixing may not be the exact line the error was thrown on.
    int lineToChange = searchFileForBestLine(f.brokenCode);
    
    // If we've changed which line we're patching, we also need to update
    // which "original" code is displayed to the user.
    String originalCode = lastQueryEditor.getLineText(lineToChange);
    
    int linesInFix = f.brokenCode.split("\n").length;
    
    if (linesInFix <= 1) {

      try {

        //first, try to auto-apply patch
        //TODO: make this smarter and token-based

        diff_match_patch dmp = new diff_match_patch();
        LinkedList<Patch> pList = dmp.patch_make(f.brokenCode, f.fixedCode);
        Object[] pResult = dmp.patch_apply(pList, originalCode);
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
        pasteText = "\n// HELPMEOUT AUTO-PATCH. ORIGINAL: "+originalCode+"\n"+patchedText+"\n";

      } catch (Exception e) { //diff-match-path can throw StringIndexOutOfBoundsException
        pasteText = commentCode(currentFixes.get(i).fixedCode, originalCode);
        HelpMeOutLog.getInstance().writeError("unable to auto-patch.");
      }
      
    } else { // the fix is a block of text, just paste it in as close as possible
      pasteText = commentCode(currentFixes.get(i).fixedCode, originalCode);
    }

    //now replace the error line with our fix (makes the assumption that the error was actually at that line)
    pasteIntoEditor(lineToChange,lastQueryEditor,pasteText);
  }
  
  private int searchFileForBestLine(String fix) {
    diff_match_patch dmp = new diff_match_patch();
    dmp.Match_Threshold = 0.9f; // this number probably needs tweaking; higher = more liberal matches; between 0 and 1
    int loc = lastQueryEditor.getTextArea().getLineStartOffset(lastQueryLine);
    int offset = dmp.match_main(lastQueryEditor.getText(), fix, loc);
    
    if (offset == -1) {
      return 0;
      
    } else {
      int line = lastQueryEditor.getTextArea().getLineOfOffset(offset);
      return line;
    }
  }
  
  /** Search one line above and one line below the error line to see if the fix
   *  is not the error line itself, but one above or below.  This manifests itself
   *  in missing semicolon errors, for example.
   *  
   *  We do this currently by doing a diff match/patch on each of the three lines:
   *  the error line and the lines above and below, and choosing the line with the
   *  least number of patches between it and the fixed line.
   *  
   * @param fix the chosen fix from the database that we are going to copy into the editor
   * @return the line we have chosen as the most likely line needing to be fixed
   */
//  private int searchNearbyForBetterLine(String fix) {
//    diff_match_patch dmp = new diff_match_patch();
//    LinkedList<Patch> pList = dmp.patch_make(lastQueryEditor.getLineText(lastQueryLine), fix);
//    double bestScore = patchEqualityScore(pList, lastQueryEditor.getLineText(lastQueryLine).length(), fix.length());
//    int bestLine = lastQueryLine;
//    
//    // check above
//    if (lastQueryLine > 0) {
//      int above = lastQueryLine-1;
//      pList = dmp.patch_make(lastQueryEditor.getLineText(above), fix);
//      double score = patchEqualityScore(pList, lastQueryEditor.getLineText(above).length(), fix.length());
//      if (score > bestScore) {
//        bestScore = score;
//        bestLine = above;
//      }
//    }
//
//    //check below
//    if (lastQueryLine < lastQueryEditor.getTextArea().getLineCount()) {
//      int below = lastQueryLine+1;
//      pList = dmp.patch_make(lastQueryEditor.getLineText(below), fix);
//      double score = patchEqualityScore(pList, lastQueryEditor.getLineText(below).length(), fix.length());
//      if (score > bestScore) {
//        bestScore = score;
//        bestLine = below;
//      }
//    }
//    
//    return bestLine;
//  }
  
  /** 
   * Computes how close a patch already is to the target text
   * by summing the length of the number of "EQUAL" sections of the patch.
   * 
   * This uses the algorithm defined in Python's difflib.ratio() function
   *  
   * @param pList list of patches
   * @param errorLength the length of the error code string
   * @param fixLength the length of the broken code in the fix
   * @return the ratio of equal characters over total characters in the two strings
   */
//  private double patchEqualityScore(LinkedList<Patch> pList, int errorLength, int fixLength) {
//    double ratio = 0;
//    for (Patch p : pList) {
//      for (Diff d : p.diffs) {
//        if (d.operation == Operation.EQUAL)
//          ratio += d.text.length();
//      }
//    }
//    
//    ratio = ratio*2/(errorLength+fixLength);
//    return ratio;
//  }
  
  private String commentCode(String fix, String original) {
    String comment = "// --- HELPMEOUT ---\n";
    fix = fix.replaceAll("\n$", "");
    fix = "//".concat(fix);
    comment = comment.concat(fix.replaceAll("\n", "\n//"));
    comment = comment.concat("\n");
    comment = comment.concat("//------------------\n");
    comment = comment.concat(original);
    if (!comment.endsWith("\n")) {
      comment = comment.concat("\n");
    }
    return comment;
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
      if (errorType == ErrorType.COMPILE) {
        // using id, call database method for compiler errors
        proxy.call("errorvote",fixid,vote);
      } else if (errorType == ErrorType.RUN) {
        // call database method for runtime errors
        proxy.call("errorvoteexception",fixid,vote);
      } else {
        HelpMeOutLog.getInstance().writeError("HelpMeOut Error: did not recognize error type");
      }

    } catch (Exception e) {
      HelpMeOutLog.getInstance().writeError("couldn't call errorvote servicemethod.");
      e.printStackTrace();
    }
    
    // then re-query?
    if (errorType == ErrorType.COMPILE) {
      query(lastQueryMsg,lastQueryCode,lastQueryLine, lastQueryEditor);
    } else if (errorType == ErrorType.RUN) {
      HelpMeOutExceptionTracker.getInstance().processRuntimeException(lastEvalError, lastInterpreter);
    } else {
      HelpMeOutLog.getInstance().writeError("HelpMeOut Error: did not recognize error type");
    }
  }

  private void pasteIntoEditor(int line, Editor editor,String fix) {
   
    editor.setLineText(line, fix);
    editor.setSelection(editor.getLineStartOffset(line), editor.getLineStartOffset(line)+fix.length());
   

  }
  public void saveExceptionInfo(EvalError err, Interpreter i, String msg, String code, int line) {    
    lastEvalError = err;
    lastInterpreter = i;
    lastErrorMsg = msg;
    lastErrorCode = code;
    lastQueryMsg = msg;
    lastQueryCode = code;
    lastQueryLine = line;
  }
  
  public void setEditor(Editor editor) {
    this.lastQueryEditor = editor;
  }
 
}