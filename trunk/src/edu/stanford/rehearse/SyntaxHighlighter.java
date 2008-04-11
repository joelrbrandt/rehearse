package edu.stanford.rehearse;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.*;

import org.jedit.syntax.JavaScriptTokenMarker;

public class SyntaxHighlighter extends JFrame {
	
	public SyntaxHighlighter() {
		super("Edit that syntax...");
		
		BorderLayout bl = new BorderLayout();
		setLayout(bl);
		
		InteractiveTextArea ta = new InteractiveTextArea();
		ta.setTokenMarker(new JavaScriptTokenMarker());
		add(ta);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		pack();
		setVisible(true);
		
	}

	public static void main(String[] args) {
		SyntaxHighlighter SH = new SyntaxHighlighter();
	}
}

