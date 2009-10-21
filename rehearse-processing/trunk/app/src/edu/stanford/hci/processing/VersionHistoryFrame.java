package edu.stanford.hci.processing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import edu.stanford.hci.processing.editor.RehearseEditor;

public class VersionHistoryFrame extends JFrame {
  
  private static final int ROW_HEIGHT = 120;
  private static final Color selectedColor = new Color(150,255,150);
  
  private static final Border hoverBorder = BorderFactory.createLineBorder(Color.green);
  private static final Border normalBorder = BorderFactory.createLineBorder(Color.black);
  
  
  private final RehearseEditor editor;
  
  private JPanel rootPanel;
  private ArrayList<VersionHistoryPanel> versionPanels;
  //private VersionHistoryTableModel tableModel;
  //private JTable table;
  
  private int lastRunningVersionIndex = -1;
  
  public VersionHistoryFrame(final RehearseEditor editor) {
    super("Version History");
    this.editor = editor;
    this.versionPanels = new ArrayList<VersionHistoryPanel>();
    
    rootPanel = new JPanel();
    rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.PAGE_AXIS));
    JScrollPane scrollPane = new JScrollPane(rootPanel);
    add(scrollPane, BorderLayout.CENTER);
    
    setPreferredSize(new Dimension(700, 500));
  }
  
  public void addVersionHistory(VersionHistory vh) {
    VersionHistoryPanel panel = new VersionHistoryPanel(vh);
    rootPanel.add(panel);
    rootPanel.add(Box.createRigidArea(new Dimension(10, 5)));
    versionPanels.add(panel);
    setLastRunningVersionIndex(versionPanels.size() - 1);
    validate();
  }
  
  private void setLastRunningVersionIndex(int index) {
    if (lastRunningVersionIndex != -1) {
      versionPanels.get(lastRunningVersionIndex).setBackground(Color.white);
    }
    lastRunningVersionIndex = index;
    if (lastRunningVersionIndex != -1) {
      versionPanels.get(lastRunningVersionIndex).setBackground(selectedColor);
    }
  }
  
  public void updateScreenshot(int index, Image screenshot) {
    VersionHistoryPanel panel = versionPanels.get(index);
    VersionHistory model = panel.getModel();
    model.setScreenshot(screenshot);
    panel.setModel(model);
    repaint();
  }
  
  public void updateLastRunScreenshot(Image screenshot) {
    updateScreenshot(lastRunningVersionIndex, screenshot);
  }
  
  public static class VersionHistory {
    private ImageIcon screenshot;
    private String code;
    private Date time;
    
    public VersionHistory(Image screenshot, String code, Date time) {
      super();
      this.screenshot = new ImageIcon(scaleImageDown(screenshot));
      this.code = code;
      this.time = time;
    }
    
    private Image scaleImageDown(Image image) {
      int width = 
        (int)(ROW_HEIGHT * (image.getWidth(null) / (double)image.getHeight(null)));
      return image.getScaledInstance(width, ROW_HEIGHT, Image.SCALE_DEFAULT);
    }
    
    public ImageIcon getScreenshot() {
      return screenshot;
    }
    
    public void setScreenshot(Image screenshot) {
      this.screenshot = new ImageIcon(scaleImageDown(screenshot));
    }
    
    public String getCode() {
      return code;
    }
    public void setCode(String code) {
      this.code = code;
    }
    public Date getTime() {
      return time;
    }
    public void setTime(Date time) {
      this.time = time;
    }
  }
  
  public class VersionHistoryPanel extends JPanel {
    private VersionHistory model;
    private JLabel screenshot;
    private JTextArea codeTextArea;
    private JLabel timeLabel;
    
    
    public VersionHistoryPanel(VersionHistory model) {
      super();
      setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
      setMaximumSize(new Dimension(700, ROW_HEIGHT + 20));
      
      setBackground(Color.white);
      setBorder(BorderFactory.createLineBorder(Color.black));
      
      screenshot = new JLabel();
      add(screenshot);
      
      codeTextArea = new JTextArea();
      JScrollPane scrollPane = new JScrollPane(codeTextArea);
      scrollPane.setPreferredSize(new Dimension(400, ROW_HEIGHT));
      add(scrollPane);
      
      timeLabel = new JLabel();
      add(timeLabel);
      
      add(Box.createHorizontalGlue());
      
      setModel(model);
      
      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
          setBorder(hoverBorder);
        }
        
        @Override
        public void mouseExited(MouseEvent e) {
          setBorder(normalBorder);
        }
        
        @Override
        public void mouseClicked(MouseEvent e) { 
          setLastRunningVersionIndex(versionPanels.indexOf(e.getSource()));
          editor.swapRunningCode(VersionHistoryPanel.this.model.getCode());
        }
      });
    }
    
    public void setModel(VersionHistory model) {
      this.model = model;
      screenshot.setIcon(model.getScreenshot());
      codeTextArea.setText(model.getCode());
      int caretPos = Math.min(codeTextArea.getText().indexOf("void draw")+40, 
          codeTextArea.getText().length() - 1);
      codeTextArea.setCaretPosition(caretPos);
      timeLabel.setText(DateFormat.getTimeInstance().format(model.getTime()));
      validate();
    }
    
    public VersionHistory getModel() {
      return model;
    }
    
  }
}
