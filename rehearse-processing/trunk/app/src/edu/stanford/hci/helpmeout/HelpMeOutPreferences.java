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
  
  private static ButtonGroup usageGroup = null;
  private static JRadioButton usage1 = null;
  private static JRadioButton usage2 = null;
  private static JRadioButton usage3 = null;

  private static final String DEFAULT_USAGE_STR = "DISABLED";
  
  public String getMenuTitle() {
    return "HelpMeOut Preferences";
  }

  public void init(Editor editor) {
    if (frame == null) {
      frame = new JFrame("HelpMeOut Preferences");
      
      frame.setSize(400, 200);
      Container content = frame.getContentPane();
      content.setBackground(Color.gray);
      
      usage1 = new JRadioButton("Disable HelpMeOut");
      usage2 = new JRadioButton("Get suggestions from HelpMeOut");
      usage3 = new JRadioButton("Get suggestions from HelpMeOut and submit my fixes");
      usage1.setActionCommand(Usage.DISABLED.toString());
      usage2.setActionCommand(Usage.QUERY.toString());
      usage3.setActionCommand(Usage.QUERY_AND_SUBMIT.toString());
      
      usageGroup = new ButtonGroup();
      usageGroup.add(usage1);
      usageGroup.add(usage2);
      usageGroup.add(usage3);
      
      if (usageChoice.equals(Usage.DISABLED)) {
        usage1.setSelected(true);
      } else if (usageChoice.equals(Usage.QUERY)) {
        usage2.setSelected(true);
      } else if (usageChoice.equals(Usage.QUERY_AND_SUBMIT)) {
        usage3.setSelected(true);
      }
      
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
  
  public static Usage getUsage() {
    return usageChoice;
  }

  public static void setInitialUsage(String usage) {
    
    if (usage == null)
      usage = DEFAULT_USAGE_STR;
    
    if (usage.equals(Usage.DISABLED.toString())) {
      usageChoice = Usage.DISABLED;
    } else if (usage.equals(Usage.QUERY.toString())) {
      usageChoice = Usage.QUERY;
    } else if (usage.equals(Usage.QUERY_AND_SUBMIT.toString())) {
      usageChoice = Usage.QUERY_AND_SUBMIT;
    }
  }

}
