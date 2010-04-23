package edu.stanford.hci.helpmeout;

import java.util.HashMap;
import java.util.List;

import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamBasicFilter;
import antlr.TokenStreamException;

//change Token stream by discarding some tokens and modifying the text of others
//@author bjoern
public class TokenStreamModifyingFilter extends TokenStreamBasicFilter {
  private class ListStringPair {
    public List<String> list;
    public String str;
    public ListStringPair(List<String> l, String s) {
      list = l;
      str =s;
    }
  }
  
  HashMap<Integer,String> modifiers = new HashMap<Integer,String>();
  HashMap<Integer,String> discarders = new HashMap<Integer,String>();
  HashMap<Integer,ListStringPair> listModifiers = new HashMap<Integer,ListStringPair>();

  /** change the string of a token type */
  public void modify(int ttype,String newOutput) {
    modifiers.put(ttype, newOutput);
  }
  /** change the string of a token type, but only if it's not in the list; if it is,just pass through */
  public void modifyIfNotInList(int ttype,String newOutput,List<String>list) {
    listModifiers.put(ttype,new ListStringPair(list,newOutput));
  }
  /** discard a tokentype, but only if its string matches the passed in regular expression */
  public void discardIfMatches(int ttype,String regex) {
    discarders.put(ttype, regex);
  }

  public TokenStreamModifyingFilter(TokenStream arg0) {
    super(arg0);
    // TODO Auto-generated constructor stub
  }
  @Override
  public Token nextToken() throws TokenStreamException {
    Token tok = super.nextToken();
    if(modifiers.containsKey(tok.getType())) {
      tok.setText(modifiers.get(tok.getType()));
    }
    if(listModifiers.containsKey(tok.getType())) {
      if(!listModifiers.get(tok.getType()).list.contains(tok.getText())) {
        tok.setText(listModifiers.get(tok.getType()).str);
      }
    }
    if(discarders.containsKey(tok.getType())) {
      if(tok.getText().matches(discarders.get(tok.getType()))) {
        return nextToken();
      }
    }
    
    return tok;
  }

}

