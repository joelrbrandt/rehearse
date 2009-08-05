package edu.stanford.hci.helpmeout;

import java.util.ArrayList;
import java.util.List;

import processing.app.preproc.PdeLexer;
import antlr.TokenStream;

public class PdeLexingFilter extends TokenStreamModifyingFilter {

  public PdeLexingFilter(TokenStream arg0) {
    super(arg0);
  //construct a filter - see PreProcessor.java and our server code
    //#if it's a name, literal or comment abstract it, otherwise write it
    //skip " " and "\t" whitespace
    discardIfMatches(PdeLexer.WS,"[ \t]+");
    discard(PdeLexer.SL_COMMENT);
    discard(PdeLexer.ML_COMMENT);
    
    modify(PdeLexer.NUM_INT, "NI");
    modify(PdeLexer.NUM_FLOAT, "NF");
    modify(PdeLexer.NUM_DOUBLE, "ND");
    modify(PdeLexer.NUM_LONG,"NL");
    modify(PdeLexer.STRING_LITERAL, "LS");
    modify(PdeLexer.CHAR_LITERAL, "LC");
    
    // we want to keep all the built-in identifiers in place
    List<String> keepIdentifiers = new ArrayList<String>();
    keepIdentifiers.add("String");
    keepIdentifiers.add("setup");
    keepIdentifiers.add("draw");
    keepIdentifiers.add("mouseButton");
    keepIdentifiers.add("mousePressed");
    
    modifyIfNotInList(PdeLexer.IDENT, "ID",keepIdentifiers);
    
    //TODO: add all the other stuff
    
    
  }

}
