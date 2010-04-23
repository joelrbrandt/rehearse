package edu.stanford.hci.helpmeout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;

import edu.stanford.hci.helpmeout.HelpMeOutTool.ExitListener;

import processing.app.Editor;
import processing.app.Preferences;
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
  
  private static boolean uploadLogsChoice;
  private static JCheckBox uploadLogs = null;

  private static final String DEFAULT_USAGE_STR = "QUERY_AND_SUBMIT";
  private static final boolean DEFAULT_UPLOAD_LOGS = true;
  
  public String getMenuTitle() {
    return "HelpMeOut Preferences";
  }

  public void init(Editor editor) {
    if (frame == null) {
      frame = new JFrame("HelpMeOut Preferences");
      frame.setResizable(false);
      
      Container content = frame.getContentPane();
      content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
      
      /* Usage selection */
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
      
      JPanel usageChoicePanel = new JPanel();
      usageChoicePanel.setLayout(new BoxLayout(usageChoicePanel, BoxLayout.Y_AXIS));
      usageChoicePanel.add(usage1);
      usageChoicePanel.add(usage2);
      usageChoicePanel.add(usage3);
      
      content.add(usageChoicePanel);
      
      /* Upload logs: yes or no? */
      uploadLogs = new JCheckBox("Send us your usage information to help improve HelpMeOut");
      uploadLogs.setSelected(uploadLogsChoice);
      
      JPanel uploadLogsPanel = new JPanel();
      uploadLogsPanel.setLayout(new BoxLayout(uploadLogsPanel, BoxLayout.Y_AXIS));
      uploadLogsPanel.add(uploadLogs);
      
      JSeparator separator1 = new JSeparator();
      content.add(separator1);
      content.add(uploadLogsPanel);
      
      /* Ok and Cancel buttons */
      JButton ok = new JButton("OK");
      JButton cancel = new JButton("Cancel");
      ok.setActionCommand("OK");
      cancel.setActionCommand("Cancel");
      ok.addActionListener(this);
      cancel.addActionListener(this);
      
      JPanel okCancelButtonPanel = new JPanel();
      okCancelButtonPanel.setLayout(new BoxLayout(okCancelButtonPanel, BoxLayout.X_AXIS));
      okCancelButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      okCancelButtonPanel.add(ok);
      okCancelButtonPanel.add(cancel);
      
      JSeparator separator2 = new JSeparator();
      content.add(separator2);
      content.add(okCancelButtonPanel);
      
      HelpMeOut.getInstance().updatePreferences(usageChoice, uploadLogsChoice);
      
      frame.pack();
    }
  }

  public void run() {
    // toggle visibility of the window
    visible = !visible;
    frame.setVisible(visible);
  }

  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    
    if (command.equals("OK")) {
      if (usage1.isSelected()) {
        usageChoice = Usage.DISABLED;
      } else if (usage2.isSelected()) {
        usageChoice = Usage.QUERY;
      } else if (usage3.isSelected()) {
        usageChoice = Usage.QUERY_AND_SUBMIT;
      }
      
      if (uploadLogs.isSelected()) {
        uploadLogsChoice = true;
      } else {
        uploadLogsChoice = false;
      }
      
      visible = false;
      frame.setVisible(visible);
      
    } else if (command.equals("Cancel")) {
      if (usageChoice.equals(Usage.DISABLED)) {
        usage1.setSelected(true);
      } else if (usageChoice.equals(Usage.QUERY)) {
        usage2.setSelected(true);
      } else if (usageChoice.equals(Usage.QUERY_AND_SUBMIT)) {
        usage3.setSelected(true);
      }
      
      uploadLogs.setSelected(uploadLogsChoice);
      
      visible = false;
      frame.setVisible(visible);
    }
    
    HelpMeOut.getInstance().updatePreferences(usageChoice, uploadLogsChoice);
  }
  
  public static Usage getUsage() {
    return usageChoice;
  }
  
  public static boolean getUploadLogs() {
    return uploadLogsChoice;
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
  
  public static void setInitialUploadLogs(String uploadLogsStr) {
    
    if (uploadLogsStr == null) {
      uploadLogsChoice = DEFAULT_UPLOAD_LOGS;
      
    } else {
      boolean upload = Boolean.parseBoolean(uploadLogsStr);
      uploadLogsChoice = upload;
    }
  }
  
  /**
   * Called from RehearseBase.handleQuit() to save HelpMeOut preferences.
   * Appends all HelpMeOut variables to normal Processing preferences file.
   * @param prefsFile the normal Processing preferences file
   */
  public static void save(File prefsFile) {
    String usage = getUsage().toString();
    String uploadLogs = Boolean.toString(getUploadLogs());
    try {
       BufferedWriter out = new BufferedWriter(new FileWriter(prefsFile, true)); // true appends
       out.write("helpmeout.usage="+usage+"\n");
       out.write("helpmeout.uploadLogs="+uploadLogs);
       out.close();
     } catch (IOException e) {
       System.out.println("Error writing HelpMeOut preferences");
     }
  }
  
  /**
   * Called from RehearseBase.main() immediately after Preferences.init() has been called.
   */
  public static void load() {
    String usage = Preferences.get("helpmeout.usage");
    setInitialUsage(usage);
    
    String uploadLogs = Preferences.get("helpmeout.uploadLogs");
    setInitialUploadLogs(uploadLogs);
  }

}
