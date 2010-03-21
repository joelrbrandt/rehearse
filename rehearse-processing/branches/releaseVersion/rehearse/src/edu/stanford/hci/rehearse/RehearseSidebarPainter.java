package edu.stanford.hci.rehearse;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

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

        public RehearseSidebarPainter(JEditTextArea textArea, TextAreaPainter textAreaPainter) {
                this.textArea = textArea;
                this.textAreaPainter = textAreaPainter;
                setAutoscrolls(true);
                setDoubleBuffered(true);
                setOpaque(true);
                this.addMouseListener(new MouseEventHandler());
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
                	try { 
                        RehearseLineModel m =
                                (RehearseLineModel)textArea.getTokenMarker().getLineModelAt(line);
                        if (m != null && m.isPrintPoint)
                                c = Color.red;
                	} catch (NullPointerException ex) {};
                }
                gfx.setColor(c);
                    int y = textArea.lineToY(line);
                gfx.fillRect(0, y + 3, getWidth(), height);
            }

            int h = clipRect.y + clipRect.height;
        repaint(0,h,getWidth(),getHeight() - h);
          }

        class MouseEventHandler extends MouseAdapter {
                public void mousePressed(MouseEvent e) {
                        int height = textAreaPainter.getFontHeight();
                        int line = textArea.getFirstLine() + e.getY() / height;
                        //System.out.println(line);

                        if (line >= textArea.getLineCount())
                                return;

                        String lineText = textArea.getLineText(line);
                        RehearseLineModel m =
                        	(RehearseLineModel)textArea.getTokenMarker().getLineModelAt(line);
                        if (m == null) {
                        	m = new RehearseLineModel();
                        	textArea.getTokenMarker().setLineModelAt(line, m);
                        }
                        if (m.isPrintPoint) {
                        	m.isPrintPoint = false;
                        } else if (lineText != null && lineText.trim().length() != 0){
                        	m.isPrintPoint = true;
                        }
                        RehearseSidebarPainter.this.repaint();
                }            
        }
}