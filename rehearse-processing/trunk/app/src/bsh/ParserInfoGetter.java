package bsh;

import java.io.StringReader;
import java.util.ArrayList;

public class ParserInfoGetter {
  ArrayList<SimpleNode> parsedNodes = new ArrayList<SimpleNode>();  
    
  public void parseString(String s) {
    StringReader sr = new StringReader(s);
    Parser p = new Parser(sr);

    boolean eof = false;
    while (!eof) {
      try {
        while (!(eof = p.Line())) {
          SimpleNode n = p.popNode();
          parsedNodes.add(n);
          System.out.println("Parsed a " + n.toString());
        }
      }
      catch (Throwable t) {
        System.out.println("throwing out a token: " + p.getNextToken().toString());
        // t.printStackTrace();
      }
    }
    System.out.println("done parsing");

    
  }
  
}

