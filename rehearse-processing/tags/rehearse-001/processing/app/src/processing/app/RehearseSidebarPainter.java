package processing.app;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.HashMap;

import javax.swing.JComponent;

import edu.stanford.hci.rehearse.RehearseLineModel;

import processing.app.syntax.JEditTextArea;
import processing.app.syntax.TextAreaPainter;

/**
 * Handles painting the left sidebar where "print points" or other
 * line-specific interactions could happen.
 *
 */
public class RehearseSidebarPainter extends JComponent {

        private TextAreaPainter textAreaPainter;
        private JEditTextArea textArea;
        private HashMap <Integer, Integer> highlight;
        
        public RehearseSidebarPainter(JEditTextArea textArea, TextAreaPainter textAreaPainter) {
                this.textArea = textArea;
                this.textAreaPainter = textAreaPainter;
                setAutoscrolls(true);
                setDoubleBuffered(true);
                setOpaque(true);
                highlight = new HashMap <Integer, Integer> ();
        }

        public void paint(Graphics gfx) {
            Rectangle clipRect = gfx.getClipBounds();

            gfx.setColor(Color.black);
            gfx.fillRect(clipRect.x,clipRect.y,clipRect.width,clipRect.height);

            // We don't use yToLine() here because that method doesn't
            // return lines past the end of the document
            int height = textAreaPainter.getFontHeight();
            int firstLine = textArea.getFirstLine();
            int firstInvalid = firstLine + clipRect.y / height;
            // Because the clipRect's height is usually an even multiple
            // of the font height, we subtract 1 from it, otherwise one
            // too many lines will always be painted.
            int lastInvalid = firstLine + (clipRect.y + clipRect.height - 1) / height;

            for (int line = firstInvalid; line <= lastInvalid; line++) {
                Color c = Color.gray;
                if (line < textArea.getLineCount()) {
                        if (highlight.containsKey(line))
                                c = Color.red;
                }
                gfx.setColor(c);
                    int y = textArea.lineToY(line);
                gfx.fillRect(0, y + 3, getWidth(), height);
            }

            int h = clipRect.y + clipRect.height;
        repaint(0,h,getWidth(),getHeight() - h);
          }
        
        public void paintSidebar (int line) {
        	highlight.put(line, 1);
        	repaint();
        }
        
        public void clearSidebar() {
        	highlight.clear();
        	repaint();
        }

}