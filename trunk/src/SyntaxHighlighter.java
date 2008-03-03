
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.*;

public class SyntaxHighlighter extends JFrame {
	
	public SyntaxHighlighter() {
		super("Edit that syntax...");
		
		BorderLayout bl = new BorderLayout();
		setLayout(bl);
		
		JEditTextArea ta = new JEditTextArea();
		ta.setTokenMarker(new JavaScriptTokenMarker());
		ta.setText("TEST YOUR JAVASCRIPT CODE\n"
		    + "    ***\n"
		    + "    ***\n"
		    + "    ***\n"
		    + "    ***\n"
		    + "\n\n\n");
		
		add(ta);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		pack();
		setVisible(true);
		
	}

	public static void main(String[] args) {
		SyntaxHighlighter SH = new SyntaxHighlighter();
	}
}

