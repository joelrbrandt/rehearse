package processing.app;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import processing.app.syntax.JEditTextArea;
import processing.app.syntax.TextAreaDefaults;

public class RehearseEditTextArea extends JEditTextArea{
	public RehearseSidebarPainter bPainter;
	
	public RehearseEditTextArea(TextAreaDefaults defaults) {
		super(defaults);
		
	    // New painting component with breakpoints:
	    JPanel editPanel = new JPanel(new BorderLayout());
	    editPanel.add(painter, BorderLayout.CENTER);
	    bPainter = new RehearseSidebarPainter(this, painter);
	    bPainter.setPreferredSize(new Dimension(40,400));
	    editPanel.add(bPainter, BorderLayout.LINE_START);

	    add(CENTER, editPanel);
	    bPainter = new RehearseSidebarPainter(this, painter);
	    bPainter.setPreferredSize(new Dimension(40,400));
	    editPanel.add(bPainter, BorderLayout.LINE_START);
	}
	
	public RehearseSidebarPainter getBPainter() {
        return bPainter;
	}
	
	@Override
	public void setHorizontalOffset(int horizontalOffset) {
	    if(horizontalOffset == this.horizontalOffset)
	        return;
	      this.horizontalOffset = horizontalOffset;
	      if(horizontalOffset != horizontal.getValue())
	        updateScrollBars();
	      painter.repaint();
	      if (bPainter != null)
	    	  bPainter.repaint();
	};
	
	@Override
	public void setFirstLine(int firstLine) {
		super.setFirstLine(firstLine);
		if (bPainter != null)
			bPainter.repaint();
	}
	
	@Override
	public void setDocument(processing.app.syntax.SyntaxDocument document) {
	    if (this.document == document)
	        return;
	      if (this.document != null)
	        this.document.removeDocumentListener(documentHandler);
	      this.document = document;

	      document.addDocumentListener(documentHandler);

	      select(0, 0);
	      updateScrollBars();
	      painter.repaint();
	      if (bPainter != null)
	    	  bPainter.repaint();
	}
	
	@Override
	public void setDocument(processing.app.syntax.SyntaxDocument document, int start, int stop, int scroll) {
	    if (this.document == document)
	        return;
	      if (this.document != null)
	        this.document.removeDocumentListener(documentHandler);
	      this.document = document;

	      document.addDocumentListener(documentHandler);

	      select(start, stop);
	      updateScrollBars();
	      setScrollPosition(scroll);
	      painter.repaint();
	      if (bPainter != null)
	    	  bPainter.repaint();
	}
	
	@Override
	public boolean setOrigin(int firstLine, int horizontalOffset) {
		boolean changed = false;
		
	    if(horizontalOffset != this.horizontalOffset)
	      {
	        this.horizontalOffset = horizontalOffset;
	        changed = true;
	      }

	    if(firstLine != this.firstLine)
	      {
	        this.firstLine = firstLine;
	        changed = true;
	      }

	    if(changed)
	      {
	        updateScrollBars();
	        painter.repaint();
	        if (bPainter != null)
	        	bPainter.repaint();
	      }

	    return changed;
	}
}
