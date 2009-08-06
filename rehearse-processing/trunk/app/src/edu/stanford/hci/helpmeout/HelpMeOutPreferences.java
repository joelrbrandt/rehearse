package edu.stanford.hci.helpmeout;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import edu.stanford.hci.helpmeout.HelpMeOutTool.ExitListener;

import processing.app.Editor;
import processing.app.tools.Tool;

public class HelpMeOutPreferences implements Tool, ActionListener {
  
  public static enum Usage {DISABLED, QUERY, QUERY_AND_SUBMIT};
  
  private static JFrame frame = null;
  private static boolean visible = false;
  private static Usage usageChoice = null;

  public String getMenuTitle() {
    return "HelpMeOut Preferences";
  }

  public void init(Editor editor) {
    if (frame == null) {
      frame = new JFrame("HelpMeOut Preferences");
      
      frame.setSize(400, 200);
      Container content = frame.getContentPane();
      content.setBackground(Color.gray);
      
      JRadioButton usage1 = new JRadioButton("Disable HelpMeOut");
      JRadioButton usage2 = new JRadioButton("Get suggestions from HelpMeOut");
      JRadioButton usage3 = new JRadioButton("Get suggestions from HelpMeOut and submit my fixes");
      usage1.setActionCommand(Usage.DISABLED.toString());
      usage2.setActionCommand(Usage.QUERY.toString());
      usage3.setActionCommand(Usage.QUERY_AND_SUBMIT.toString());
      
      ButtonGroup group = new ButtonGroup();
      group.add(usage1);
      group.add(usage2);
      group.add(usage3);
      
      usage1.setSelected(true);
      usageChoice = Usage.DISABLED;
      
      usage1.addActionListener(this);
      usage2.addActionListener(this);
      usage3.addActionListener(this);
      
      JPanel radioPanel = new JPanel(new GridLayout(3,1));
      radioPanel.add(usage1);
      radioPanel.add(usage2);
      radioPanel.add(usage3);
      
      content.add(radioPanel);
      
      HelpMeOut.getInstance().setUsage(usageChoice);
    }
  }

  public void run() {
    // toggle visibility of the window
    visible = !visible;
    frame.setVisible(visible);
  }

  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    
    if (command.equals(Usage.DISABLED.toString())) {
      usageChoice = Usage.DISABLED;
      //System.out.println("HelpMeOut usage changed to DISABLED");
    } else if (command.equals(Usage.QUERY.toString())) {
      usageChoice = Usage.QUERY;
      //System.out.println("HelpMeOut usage changed to QUERY");
    } else if (command.equals(Usage.QUERY_AND_SUBMIT.toString())) {
      usageChoice = Usage.QUERY_AND_SUBMIT;
      //System.out.println("HelpMeOut usage changed to QUERY_AND_SUBMIT");
    }
    
    HelpMeOut.getInstance().setUsage(usageChoice);
  }

}
