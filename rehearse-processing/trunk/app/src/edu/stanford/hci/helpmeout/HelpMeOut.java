package edu.stanford.hci.helpmeout;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import processing.app.Base;
import processing.app.Editor;
import processing.app.Preferences;
import processing.app.SketchCode;
import antlr.Token;
import antlr.TokenStreamException;
import bsh.EvalError;
import bsh.Interpreter;
import edu.stanford.hci.helpmeout.HelpMeOutPreferences.Usage;

/**
 * HelpMeOut
 * make sure stringtree-json-2.0.5.jar is in build path (jj1.0.1.jar - removed, we now use source code directly)
 * i copied to processing/lib and added to processing project build path
 * @author bjoern
 *
 */

public class HelpMeOut {

  private static final String HELPMEOUT_WWW_DETAIL_URL = "http://rehearse.stanford.edu/helpmeout-www/detail.psp";
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

  private Map<Integer,FixInfo> currentFixes = new HashMap<Integer,FixInfo>(); // temp storage for currently displayed fixes, so we can copy them

  // make it a Singleton
  private static HelpMeOut instance = new HelpMeOut();
  private HelpMeOutServerProxy serverProxy = HelpMeOutServerProxy.getInstance();

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

  private SketchCode lastQuerySketchCode=null;

  /**
   * Simple test: call an echo function that takes a string and returns that same string
   */
  protected void echo() {
    String result;
    try {
      result = serverProxy.echo("hello");
      System.out.println(result);
    } catch (Exception e) {
      e.printStackTrace();
    }
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
        serverProxy.store2(error, s0, s1);
      }catch (Exception e) {
        HelpMeOutLog.getInstance().writeError(HelpMeOutLog.STORE_FAIL_COMPILE,e.getMessage());
        e.printStackTrace();
      }
    } else {
      HelpMeOutLog.getInstance().writeError(HelpMeOutLog.STORE_FAIL_NULL);
    }
  }

  protected void showNoResult(String error) {
    if (tool != null) {
      String suggestions ="<html><head>"+
      "<style type=\"text/css\">"+
      "body {margin:7px; font-family:Arial,Helvetica; background-color:#f0f0f0;}"+
      "    div {font-family:Arial,Helvetica; font-size:9px;}"+
      "    a  {color:#8D2A2B; font-weight:bold;}"+
      "</style>"+
      "</head><body>";
      suggestions+="<h3>Error Message:</h3>"+error+"";
      suggestions+="<p>HelpMeOutQuery did not return any suggestions.</p>";
      
      String encodedError="";
      try {
        encodedError = URLEncoder.encode(error, "utf-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      suggestions+="<div><p><a class=\"thumb_link\" href=\"http://rehearse.stanford.edu/?action=google&error="+encodedError+"\">Search Google for this error</a></p></div>";
      suggestions+="</body></html>";

      //now show the suggestion in the HelpMeOut window
      tool.setHtml(suggestions);      
    }
  }

  protected void showQueryResult(ArrayList<HashMap<String,ArrayList<String>>> result, String error, ErrorType errorType) {

    // Set the error type so voting knows which table to update
    this.errorType = errorType;

    if(tool!=null) {
      tool.setLabelText("Searching for suggestions in the HelpMeOut database...");
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
    "    a  {color:#8D2A2B; font-weight:bold;}"+
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

        // add links to detail page, vote up/down and to copy a fix 
        suggestions += "<div><a class=\"thumb_link\" href=\"http://rehearse.stanford.edu/?id="+Integer.toString(i)+"&action=detail\">more info</a> | "+
        "<a class=\"thumb_link\" href=\"http://rehearse.stanford.edu/?id="+Integer.toString(i)+"&action=up\">vote up</a> | "+
        "<a class=\"thumb_link\" href=\"http://rehearse.stanford.edu/?id="+Integer.toString(i)+"&action=down\">vote down</a> | "+
        "<a class=\"thumb_link\" href=\"http://rehearse.stanford.edu/?id="+Integer.toString(i)+"&action=findline\">find line</a> | "+
        "<a class=\"thumb_link\" href=\"http://rehearse.stanford.edu/?id="+Integer.toString(i)+"&action=copy\">copy fix</a></div>";

        //add the comment, if there is one
        if(m.containsKey("comment")) {
          String comment = ((ArrayList<String>)m.get("comment")).get(0);
          if((comment!=null) && !("".equals(comment.trim()))){
            suggestions += "<p>"+comment+"<br><br></p>";
          }
        }
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
    String encodedError="";
    try {
      encodedError = URLEncoder.encode(error, "utf-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    suggestions+="<div><p><a class=\"thumb_link\" href=\"http://rehearse.stanford.edu/?action=google&error="+encodedError+"\">Search Google for this error</a></p></div>";
    suggestions+="</body></html>";


    //now show the suggestion in the HelpMeOut window
    if(tool!=null) {
      tool.setHtml(suggestions);
    }

  }


  /**
   * Query the remote HelpMeOut database for relevant example fixes.
   * Via JSON-RPC
   * 
   * @param error The compile error string of the current error
   * @param code The line of code referenced by the compile error
   * @param sketchCode 
   */
  public void query(final String error, final String code, int line, SketchCode sketchCode, Editor editor) {
    lastQueryMsg = error;
    lastQueryCode = code;
    lastQueryLine = line;
    lastQueryEditor = editor;
    lastQuerySketchCode = sketchCode;
    try {
      //query database - this call may time out or throw other exceptions

      ArrayList<HashMap<String,ArrayList<String>>> result = serverProxy.query(error, code);
      if(result!=null) {

        HelpMeOutLog.getInstance().write(HelpMeOutLog.QUERY_SUCCESS, makeIdListFromQueryResult(result));
        showQueryResult(result, error, ErrorType.COMPILE);
      } else {
        showNoResult(error);
      }
    } catch(TimeoutException te) {
      HelpMeOutLog.getInstance().writeError(HelpMeOutLog.QUERY_FAIL,"Timeout");
      showNoResult(error);
    }catch (Exception e) { //can end up here with a timeout exception
      HelpMeOutLog.getInstance().writeError(HelpMeOutLog.QUERY_FAIL,e.getMessage());
      showNoResult(error);
    }
  }

  protected String makeIdListFromQueryResult(ArrayList<HashMap<String, ArrayList<String>>> result) {
    String ids ="IDs:";
    try{
      for(HashMap<String,ArrayList<String>> m:result) {
        if(m.containsKey("id")) {
          ids+=((ArrayList<String>)m.get("id")).get(0)+",";
        }
      }
    }catch (Exception e) {

    }
    return ids;
  }

  public void processNoError(String code) {
    if(tool!=null) {
      tool.setLabelText("Your code compiled successfully. There's no need to help you out.");
    }
    switch(codeState) {
    case BROKEN:
      //went from broken to fixed - great!
      //shove it into our db
      HelpMeOutLog.getInstance().write(HelpMeOutLog.COMPILE_FIXED);

      store(lastErrorMsg,lastErrorCode,code);
      lastErrorMsg = null;
      lastErrorCode = null;
      codeState = CodeState.FIXED;
      break;
    case FIXED:
      //do nothing
      HelpMeOutLog.getInstance().write(HelpMeOutLog.COMPILE_FIXED_ALREADY);
    }
  }
  public void processBroken(String error, String code) {
    // Always save the last error, even if already broken
    HelpMeOutLog.getInstance().write(HelpMeOutLog.COMPILE_BROKEN, error);
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
   * NOTE: all line numbers in this method should be relative to current tab!
   */
  public void handleCopyAction(int i) {

    HelpMeOutLog.getInstance().write(HelpMeOutLog.CLICKED_COPY_FIX, makeTypeAndIdStringFromIndex(i));

    assert(lastQueryEditor != null);
    String pasteText;
    FixInfo f = currentFixes.get(i);

    // The line we're fixing may not be the exact line the error was thrown on.
    int lineToChange = searchTokenStreamForBestLine(f.brokenCode);//searchFileForBestLine(f.brokenCode);
    if(lineToChange < 0) {
      lineToChange=0;
    }
    // If we've changed which line we're patching, we also need to update
    // which "original" code is displayed to the user.
    String originalCode = lastQueryEditor.getLineText(lineToChange);

    int linesInFix = f.brokenCode.split("\n").length;

    if (linesInFix <= 1) {

      try {
        //first, try to apply smart, token-based patch

        String patchedText = tokenBasedAutoPatch(originalCode,f.fixedCode);
        boolean patchSuccess = (patchedText!=null);

        //if we didn't, give up and let user merge manually
        if(!patchSuccess) throw new Exception("could not apply any patches");

        //otherwise, copy our patch (fingers crossed)
        pasteText = "\n// HELPMEOUT AUTO-PATCH. ORIGINAL: "+originalCode+"\n"+patchedText+"\n";
        HelpMeOutLog.getInstance().write(HelpMeOutLog.AUTO_PATCH_SUCCESS);

      } catch (Exception e) { //diff-match-path can throw StringIndexOutOfBoundsException
        pasteText = commentCode(currentFixes.get(i).fixedCode, originalCode);
        HelpMeOutLog.getInstance().writeError(HelpMeOutLog.AUTO_PATCH_FAIL);
      }

    } else { // the fix is a block of text, just paste it in as close as possible
      pasteText = commentCode(currentFixes.get(i).fixedCode, originalCode);
    }

    //now replace the error line with our fix (makes the assumption that the error was actually at that line)
    pasteIntoEditor(lineToChange,lastQueryEditor,pasteText);
  }



  public String tokenBasedAutoPatch(String line1, String line2) {

    //token comparator tests equality of token types, not content
    Comparator<Token> ct = new Comparator<Token>() {
      public int compare(Token o1, Token o2) {
        return o2.getType()-o1.getType();
      }
    };

    //tokenize each line
    PdeMatchProcessor proc = new PdeMatchProcessor();
    List<Token> tokens1,tokens2;
    try {
      tokens1 = proc.getUnfilteredTokenArray(line1);
      tokens2 = proc.getUnfilteredTokenArray(line2);
    } catch (TokenStreamException e) {

      e.printStackTrace();
      return null;
    } 

    // do a diff on the token level
    Diff<Token> diff = new Diff<Token>(tokens1, tokens2,ct);
    List<Difference> differences = diff.diff();
    //    for(Difference d:differences) {
    //      System.out.println(d);
    //    }

    // now transform tokens1 into tokens2 by stepping through diffs
    List<Token> tokensOut = new ArrayList<Token>();
    int diffIndex =0;
    int maxLen = Math.max(tokens1.size(), tokens2.size());
    for(int i=0; i<maxLen; i++) {

      Difference d = differences.get(diffIndex);
      //copy everything that's unchanged until next difference
      if(i<d.getDeletedStart()) {
        if (i < tokens1.size())
          tokensOut.add(tokens1.get(i));
      } else {
        //now were at the difference
        //handle deletion - skip forward in ptr
        if(d.getDeletedEnd()!=Difference.NONE) {
          i=d.getDeletedEnd();
        } 
        //handle addition - insert into output
        if(d.getAddedEnd()!=Difference.NONE) {
          tokensOut.addAll(tokens2.subList(d.getAddedStart(), d.getAddedEnd()+1));
          if(d.getDeletedEnd()==Difference.NONE && i < tokens1.size()) {
            tokensOut.add(tokens1.get(i));
          }
        }

        diffIndex++;
        if(diffIndex>=differences.size()) {
          //copy remaining
          if (i < tokens1.size()-1) {
            tokensOut.addAll(tokens1.subList(i+1, tokens1.size())); //subList 1st arg is inclusive, 2nd arg is exclusive
          }
          break;
        }
      }
    }
    String result = "";
    //print what we have so far
    for(Token t: tokensOut) {
      result+=t.getText();
    }
    return result;
  }


  /**
   * Search for the best line of code based on a fuzzy string matching algorithm OVER TOKENIZED CODE
   * @param fix the broken version of chosen fix from the database that we are going to copy into the editor
   * @return the line we have chosen as the most likely line needing to be fixed; or -1 if we couldn't find one.
   */
  private int searchTokenStreamForBestLine(String fix) {
    PdeMatchProcessor proc = new PdeMatchProcessor();
    try {
      String program = lastQueryEditor.getText();
      program = proc.process(program); //look only at current tab
      fix = proc.process(fix);
      diff_match_patch dmp = new diff_match_patch();
      dmp.Match_Threshold = 1.0f; // this number probably needs tweaking; higher = more liberal matches; between 0 and 1

      int l = lastQueryLine;

      int loc = getLineStartOffet(program,l); //get line start offset of lastQueryLine in processed program textlastQueryEditor.getTextArea().getLineStartOffset(lastQueryLine);
      int offset = dmp.match_main(program, fix, loc); // This only searches in the currently viewed tab
      if (offset == -1) {
        HelpMeOutLog.getInstance().write(HelpMeOutLog.INFO, "match_main() did not return useful offset (-1).");
        return -1;
      } else {
        int line = getLineOfOffset(program,offset);//get line of found offset in tokenized pgm
        HelpMeOutLog.getInstance().write(HelpMeOutLog.INFO, "match_main() returned offset "+Integer.toString(offset)+" at line "+Integer.toString(line));
        return line;
      }
    } catch (TokenStreamException e) {
      HelpMeOutLog.getInstance().writeError(HelpMeOutLog.TOKENIZER_ERROR, e.getMessage());
      return -1;

    } catch (Exception e) {
      HelpMeOutLog.getInstance().writeError(HelpMeOutLog.DIFF_MATCH_PATCH_ERROR, e.getMessage());
    }

    // ...
    return -1;
  }

  /** return zero-based line# of offset */
  public int getLineOfOffset(String program, int offset) {
    String lines[] = program.split("\n");
    int chars =0;
    for(int i=0; i<lines.length; i++) {
      chars += lines[i].length()+1;//1 is of \n which split removed
      if(chars>offset) //>= - \n is part of earlier line; > - \n is part of following line
        return i;
    }
    return 0;
  }

  /** return character offset of line# lineIndex in String program; zero-based */
  public int getLineStartOffet(String program, int lineIndex) {
    String lines[] = program.split("\n");
    int chars = 0;
    for(int i=0; i<lineIndex; i++) {
      chars+=lines[i].length()+1; //1 is for \n which split consumed
    }
    return chars;
  }


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
    HelpMeOutLog.getInstance().write(HelpMeOutLog.CLICKED_VOTE_UP, makeTypeAndIdStringFromIndex(i));
    voteAndRequery(currentFixes.get(i).id, 1);
  }
  /**
   * Event handler for clicks on the vote down link in the helpmeout ui.
   * @param i suggestion id extracted from link
   */    
  public void handleVoteDownAction(int i) {
    HelpMeOutLog.getInstance().write(HelpMeOutLog.CLICKED_VOTE_DOWN, makeTypeAndIdStringFromIndex(i));
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
        serverProxy.errorvote(fixid, vote);
      } else if (errorType == ErrorType.RUN) {
        // call database method for runtime errors
        serverProxy.errorvoteexception(fixid, vote);
      } else {
        HelpMeOutLog.getInstance().writeError(HelpMeOutLog.VOTE_FAIL_UNRECOGNIZED);
      }

    } catch (Exception e) {
      HelpMeOutLog.getInstance().writeError(HelpMeOutLog.VOTE_FAIL);
      e.printStackTrace();
    }

    // then re-query?
    if (errorType == ErrorType.COMPILE) {
      query(lastQueryMsg,lastQueryCode,lastQueryLine, lastQuerySketchCode, lastQueryEditor);
    } else if (errorType == ErrorType.RUN) {
      HelpMeOutExceptionTracker.getInstance().processRuntimeException(lastEvalError, lastInterpreter);
    } else {
      HelpMeOutLog.getInstance().writeError(HelpMeOutLog.VOTE_FAIL_UNRECOGNIZED);
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
    //only reset our state if we actually changed editor
    //(and not just deactivated and reactivated window)
    if(!editor.equals(lastQueryEditor)) {
      lastQueryEditor = editor;
      //reset our state
      codeState = CodeState.FIXED;
      errorType = ErrorType.COMPILE;
      // keep track of msg and code for FSM
      lastErrorMsg = null;
      lastErrorCode = null;

      //store last query parameters in case we need to re-query
      lastQueryMsg = null;
      lastQueryCode = null;
      lastQueryLine = -1;

      //store last query parameters for runtime exceptions
      lastEvalError=null;
      lastInterpreter=null;
      
      // reset eInfo
      HelpMeOutExceptionTracker.getInstance().resetEInfo();
    }
  }
  public Editor getEditor() {
    return lastQueryEditor;
  }

  public void updatePreferences(Usage usage, boolean uploadLogs) {
    //TODO: tool is probably going to be null. Maybe check for usage in HelpMeOut.registerTool() instead?
    if (tool != null) {
      tool.reportUsage(usage);
    }
    HelpMeOutServerProxy.getInstance().setUsage(usage);
    HelpMeOutServerProxy.getInstance().setUploadLogs(uploadLogs);

    Preferences.set("helpmeout.usage", usage.toString());
    Preferences.set("helpmeout.uploadLogs", Boolean.toString(uploadLogs));
  }

  public void handleShowDetailAction(int index) {
    int id = currentFixes.get(index).id;
    int type = errorType.ordinal(); 
    HelpMeOutLog.getInstance().write(HelpMeOutLog.CLICKED_MORE_DETAIL,makeTypeAndIdStringFromIndex(index));        
    Base.openURL(HELPMEOUT_WWW_DETAIL_URL+"?id="+id+"&type="+type);

  }
  private String makeTypeAndIdStringFromIndex(int index) {
    int id = currentFixes.get(index).id;
    int type = errorType.ordinal();
    return "TYPE:"+type+";ID:"+id;
  }

  /** find the best line for a fix and highlight it */
  public void handleFindLineAction(int i) {
    HelpMeOutLog.getInstance().write(HelpMeOutLog.CLICKED_FIND_LINE, makeTypeAndIdStringFromIndex(i));

    assert(lastQueryEditor != null);
    FixInfo f = currentFixes.get(i);

    int lineToChange = searchTokenStreamForBestLine(f.brokenCode);//searchFileForBestLine(f.brokenCode);
    if((lineToChange >=0) && (lineToChange<lastQueryEditor.getLineCount())) {
      lastQueryEditor.setSelection(
                                   lastQueryEditor.getLineStartOffset(lineToChange),
                                   lastQueryEditor.getLineStopOffset(lineToChange));
    } else {
      lastQueryEditor.setSelection(0,0);
    }
  }
}
