package bsh;

import java.io.StringReader;
import java.util.ArrayList;

public class ParserInfoGetter {
  ArrayList<SimpleNode> parsedNodes = new ArrayList<SimpleNode>();
  ArrayList<ThrowawaySegment> throwawaySegments = new ArrayList<ThrowawaySegment>();

  public SimpleNode returnNodeAtCaretPosition(int caretLine, int caretColumn) {
    ArrayList<SimpleNode> sameLineNodes = new ArrayList<SimpleNode>();
    for (SimpleNode node : parsedNodes) {
      if (node.firstToken.beginLine > caretLine) {
        break;
      }
      recursiveFindNodesInLine(sameLineNodes, node, caretLine);
    }
    
    for (SimpleNode node : sameLineNodes) {
      System.out.println(node.getText());
    }
    
    return null;
  }
  
  private void recursiveFindNodesInLine(ArrayList<SimpleNode> sameLineNodes,
      SimpleNode node, int caretLine) {
    for (int i = 0; i < node.jjtGetNumChildren(); i++) {
      SimpleNode child = node.getChild(i);
      
      if (child.firstToken.beginLine <= caretLine &&
          child.lastToken.endLine >= caretLine) {
        sameLineNodes.remove(node);
        sameLineNodes.add(child);
        
        if (child.firstToken.beginLine < caretLine ||
            child.lastToken.endLine > caretLine) {
          recursiveFindNodesInLine(sameLineNodes, child, caretLine);
        }
      }
      
      if (child.firstToken.beginLine > caretLine) {
        break;
      }
    }
  }
  
  /**
   * Parses code. If there are parse errors, best effort is made to parse
   * the code without the error segments. 
   * @param s code to parse.
   */
  public void parseCode(String s) {
    while (true) {
      ThrowawaySegment ts = parseString(s);
      if (ts == null) break;
      throwawaySegments.add(ts);
      s = removeParseErrorSegment(s, ts);
    }

    System.out.println("Parse done. Parsed: \n" + s);
  }

  private String removeParseErrorSegment(String s, ThrowawaySegment ts) {
    int lineStartIndex = 0;
    int lineNum = 1;
    while (ts.line > lineNum) {
      lineStartIndex = s.indexOf('\n', lineStartIndex) + 1;
      lineNum++;	
    }

    int beginIndex = lineStartIndex + ts.beginColumn - 1;
    int endIndex = lineStartIndex + ts.endColumn - 1;

    return s.substring(0, beginIndex) + s.substring(endIndex + 1);
  }
   
  /**
   * Given a string and line number and column number (both one-indexed), returns
   * a zero-indexed index of the corresponding position.
   * 
   * @param s String to search within.
   * @param lineNum line number from 1.
   * @param columnNum column number from 1.
   * @return
   */
  private int lineColumnToIndex(String s, int lineNum, int columnNum) {
    int lineStartIndex = 0;
    while (lineNum > 1) {
      lineStartIndex = s.indexOf('\n', lineStartIndex) + 1;
      lineNum--;
    }
    
    return lineStartIndex + columnNum - 1;
  }

  /**
   * Attempts to parse the given string. If there is a parse error, it returns an
   * object that specifies which segment of the string to throw away.
   * 
   * @param s String to parse.
   * @return object specifying which segment of the string to throw away when
   *     there is a parse error; null otherwise.
   */
  private ThrowawaySegment parseString(String s) {
    parsedNodes.clear();
    StringReader sr = new StringReader(s);
    Parser p = new Parser(sr);

    boolean eof = false;
    try {
      while (!(eof = p.Line())) {
        SimpleNode n = p.popNode();
        parsedNodes.add(n);
      }
    } catch (Throwable t) {
      Token errorToken = p.token.next;
      
      ThrowawaySegment ts = new ThrowawaySegment();
      if (errorToken != null) {
        ts.line = errorToken.beginLine;
        ts.beginColumn = errorToken.beginColumn;
        ts.endColumn = errorToken.endColumn;
      } else {
        // If next token is null, we delete the next non-newline character 
        // following the last successfully parsed token.
        ts.line = p.token.beginLine;
        ts.beginColumn = ts.endColumn = p.token.endColumn + 1;
        int index = lineColumnToIndex(s, p.token.beginLine, p.token.endColumn) + 1;
        while (s.charAt(index) == '\n') {
          index++;
          ts.line++;
          ts.beginColumn = ts.endColumn = 1;
        }
      }
      
      return ts;
    }

    return null;
  }

  private static class ThrowawaySegment {
    int line;
    int beginColumn;
    int endColumn;
  }
}
