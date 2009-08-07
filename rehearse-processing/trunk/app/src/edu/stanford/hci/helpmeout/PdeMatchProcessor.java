/**
 * 
 */
package edu.stanford.hci.helpmeout;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import processing.app.preproc.PdeLexer;
import antlr.Token;
import antlr.TokenStreamException;

/**
 * @author bjoern
 * Class to help us match code lines by preprocessing code to substitute certain tokens 
 */
public class PdeMatchProcessor {
  public PdeMatchProcessor() {

  }
  public String process(String program) throws TokenStreamException {
    //handle edge case where lexer dies if code ends in a comment:
    //(how many others like this are there?
    boolean removeLastToken =false;
    if(!program.endsWith("\n")) {
      program +="\n";
      removeLastToken = true;
    }
    String filteredProgram = "";
    PdeLexer lex = new PdeLexer(new StringReader(program));
    // and our custom filter
    PdeLexingFilter filter = new PdeLexingFilter(lex);

    //now read tokens one-by-one
    while(true) {
      Token tok = filter.nextToken();


      if(tok.getType()==Token.EOF_TYPE) {
        break;
      }
      filteredProgram += tok.getText();
    }
    if(removeLastToken) {
      return filteredProgram.substring(0, filteredProgram.length()-1);
    }else {
      return filteredProgram;
    }
  }


  /** Lexes String program and returns resulting tokens as a List */
  public List<Token> getUnfilteredTokenArray(String program) throws TokenStreamException {

    List<Token> tokens = new ArrayList<Token>();
    boolean removeLastToken = false;
    if(!program.endsWith("\n")) {
      program +="\n";
      removeLastToken = true;
    }
    String filteredProgram = "";
    PdeLexer lex = new PdeLexer(new StringReader(program));
    // don't add our custom filter = we need the original token text values!
    // PdeLexingFilter filter = new PdeLexingFilter(lex);

    //now read tokens one-by-one
    while(true) {
      Token tok = lex.nextToken();


      if(tok.getType()==Token.EOF_TYPE) {
        break;
      }
      tokens.add(tok);
    }
    if(removeLastToken) {
      return tokens.subList(0, tokens.size()-1);
    } else {
      return tokens;
    }
  }

}
