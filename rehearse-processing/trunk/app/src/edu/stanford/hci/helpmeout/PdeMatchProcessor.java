/**
 * 
 */
package edu.stanford.hci.helpmeout;

import java.io.StringReader;

import antlr.Token;
import antlr.TokenStreamException;

import processing.app.preproc.PdeLexer;

/**
 * @author bjoern
 * Class to help us match code lines by preprocessing code to substitute certain tokens 
 */
public class PdeMatchProcessor {
  public PdeMatchProcessor() {
    
  }
  public String process(String program) throws TokenStreamException {
    String filteredProgram = "";
    PdeLexer lex = new PdeLexer(new StringReader(program));
    // and our custom filter
    PdeLexingFilter filter = new PdeLexingFilter(lex);
    
  //now read tokens one-by-one
    while(true) {
      Token tok = filter.nextToken();
      filteredProgram += tok.getText();
      
      if(tok.getType()==Token.EOF_TYPE) {
        break;
      }
    }
    return filteredProgram;
  }
}
