package processing.app;

import java.io.*;
import java.util.*;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import processing.app.syntax.PdeKeywords;


public class RelatedLines implements CaretListener {
	static Hashtable<String, ArrayList<String> >functionToRelatedMap;
	private Editor editor;
	
	public RelatedLines (Editor ed) {
	  this.editor = ed;
		functionToRelatedMap = new Hashtable <String, ArrayList<String> > ();
		try {
			InputStream input = Base.getLibStream("relatedfunctions.txt");
			InputStreamReader isr = new InputStreamReader(input);
        	BufferedReader reader = new BufferedReader(isr);
            String line = null;
            while ((line = reader.readLine()) != null) {
              //if (line.trim().length() == 0) continue;
              String pieces[] = processing.core.PApplet.split(line, '\t');
            
              if (pieces.length >= 2) {
                // => at least one tab
            	ArrayList<String> relFunctions = new ArrayList<String> ();
            	for (int i = 0; i < pieces.length; i++) {
            		relFunctions.add(pieces[i].trim());
            	}
                String keyword = pieces[0].trim();
                functionToRelatedMap.put(keyword, relFunctions);
                }
            }
            reader.close();
		}
		
		catch (Exception ex) {
			Base.showWarning(null, "Rehearse cannot load database of related functions", ex);
		}
	}

  public ArrayList<String> getRelatedFunctions(String text) {
    if (functionToRelatedMap.containsKey(text)) {
      return functionToRelatedMap.get(text);
    }
    return null;
  }

  public void caretUpdate(CaretEvent e) {
    ((RehearseEditTextArea)editor.getTextArea()).bPainter.clearSidebar();
    String str = editor.textarea.getSelectedText();
    if (str != null) {
      str = str.trim();
      String referenceFile = PdeKeywords.getReference(str);
      if (referenceFile != null && functionToRelatedMap.containsKey(str)) {
        highlightRelated(str);
      }
    }
  }

  private void highlightRelated(String string) {
    String allText = editor.textarea.getText();
    ArrayList<String> relFns = functionToRelatedMap.get(string);
    String[] lines = allText.split("\n");
    for (int i = 0; i < lines.length; i++) {
      for (int j = 0; j < relFns.size(); j++) {
        if (lines[i].contains(relFns.get(j))) {
          ((RehearseEditTextArea)editor.getTextArea()).bPainter.paintSidebar(i);
        }
      }
    }
  } 
}
